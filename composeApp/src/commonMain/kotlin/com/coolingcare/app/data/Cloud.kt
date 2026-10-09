package com.coolingcare.app.data

import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.BookingStatus
import com.coolingcare.app.model.Business
import com.coolingcare.app.model.HistoryEntry
import com.coolingcare.app.model.Lang
import com.coolingcare.app.model.Profile
import com.coolingcare.app.model.Service
import com.coolingcare.app.platform.FirebaseEnv
import com.coolingcare.app.platform.PhoneAuth
import com.coolingcare.app.platform.toByteArray
import com.coolingcare.app.platform.toStorageData
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.PhoneAuthProvider
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.code
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.code
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.messaging.messaging
import dev.gitlive.firebase.storage.FirebaseStorageException
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.storageMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

sealed interface Session {
    data object Loading : Session
    data object SignedOut : Session
    /** [phone] is the verified login number in E.164 (+923001234567). */
    data class SignedIn(val uid: String, val phone: String) : Session
}

/** What went wrong, in terms the UI can explain. */
enum class Problem { OFFLINE, INVALID_NUMBER, INVALID_CODE, CODE_EXPIRED, TOO_MANY, SERVICE_CLOSED, NOT_ALLOWED, RELOGIN, OTHER }

class CloudException(val problem: Problem, cause: Throwable? = null) : Exception(problem.name, cause)

fun Throwable.problem(): Problem {
    val text = (message ?: "") + " " + (cause?.message ?: "")
    return when (this) {
        is CloudException -> problem
        is FirebaseNetworkException -> Problem.OFFLINE
        is FirebaseTooManyRequestsException -> Problem.TOO_MANY
        is FirebaseAuthRecentLoginRequiredException -> Problem.RELOGIN
        is FirebaseAuthInvalidCredentialsException -> {
            val c = (code ?: "") + " " + text
            when {
                c.contains("PHONE", ignoreCase = true) -> Problem.INVALID_NUMBER
                c.contains("EXPIRED", ignoreCase = true) -> Problem.CODE_EXPIRED
                else -> Problem.INVALID_CODE
            }
        }
        is FirebaseFirestoreException -> when (code) {
            FirestoreExceptionCode.UNAVAILABLE, FirestoreExceptionCode.DEADLINE_EXCEEDED -> Problem.OFFLINE
            FirestoreExceptionCode.PERMISSION_DENIED, FirestoreExceptionCode.UNAUTHENTICATED -> Problem.NOT_ALLOWED
            else -> Problem.OTHER
        }
        is FirebaseStorageException -> if (text.contains("retry limit", true) || text.contains("network", true)) Problem.OFFLINE else Problem.OTHER
        else -> when {
            text.contains("INVALID_PHONE_NUMBER") -> Problem.INVALID_NUMBER
            text.contains("TOO_MANY_REQUESTS") -> Problem.TOO_MANY
            text.contains("offline", true) || text.contains("network", true) || text.contains("timed out", true) || text.contains("Timeout", false) -> Problem.OFFLINE
            else -> Problem.OTHER
        }
    }
}

/** Loading → value, or missing (no such document / no access). */
sealed interface Live<out T> {
    data object Loading : Live<Nothing>
    data object Missing : Live<Nothing>
    data class Value<T>(val value: T) : Live<T>
}

/**
 * Everything that talks to Firebase: phone sign-in, the customer's profile, the service catalogue,
 * orders (customer + admin) and FCM tokens. Live data is exposed as StateFlows fed by snapshot listeners;
 * Firestore's offline cache keeps them filled when the phone is offline.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Cloud(private val local: Repository, private val env: FirebaseEnv) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val auth get() = Firebase.auth
    private val db get() = Firebase.firestore
    private val storage get() = Firebase.storage

    private val _session = MutableStateFlow<Session>(Session.Loading)
    val session: StateFlow<Session> = _session.asStateFlow()

    private val _profile = MutableStateFlow<Live<Profile>>(Live.Loading)
    /** users/{uid}. [Live.Missing] = a new account that has not saved its details yet. */
    val profile: StateFlow<Live<Profile>> = _profile.asStateFlow()

    private val _adminPhones = MutableStateFlow<List<String>?>(null)
    val adminPhones: StateFlow<List<String>> = _adminPhones.map { it.orEmpty() }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The signed-in phone is listed in config/roles.adminPhones. */
    val isAdmin: StateFlow<Boolean> = combine(_session, _adminPhones) { s, a -> s is Session.SignedIn && a != null && s.phone in a }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /** Splash can route once auth, the profile and the admin role are known. */
    val ready: StateFlow<Boolean> = combine(_session, _profile, _adminPhones) { s, p, a -> s is Session.SignedOut || (s is Session.SignedIn && p !is Live.Loading && a != null) }
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val _business = MutableStateFlow(Catalog.business)
    /** Shop phone / WhatsApp / hours — config/business, editable by admins. */
    val business: StateFlow<Business> = _business.asStateFlow()

    private val _services = MutableStateFlow(Catalog.services.sortedBy { it.sort })
    /** Customers: open services only. Admins: every service (open and closed). Starts with the bundled list. */
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    private val _allOrders = MutableStateFlow<Live<List<Booking>>>(Live.Loading)
    /** Admins: every order, newest first (latest 500). */
    val allOrders: StateFlow<Live<List<Booking>>> = _allOrders.asStateFlow()

    /** This phone's FCM token, once known. */
    private var pushToken: String? = null
    private var userJob: Job? = null

    fun service(id: String) = _services.value.firstOrNull { it.id == id }

    private fun log(msg: String) = println("CoolingCare Cloud: $msg")

    fun start() {
        if (env.useEmulators) runCatching {
            auth.useEmulator(env.emulatorHost, 9099)
            db.useEmulator(env.emulatorHost, 8085)
            storage.useEmulator(env.emulatorHost, 9199)
        }
        // Fail an upload after 30 s without network instead of the default 10 minutes.
        runCatching { storage.setMaxUploadRetryTime(30.seconds); storage.setMaxOperationRetryTime(20.seconds) }

        scope.launch { auth.authStateChanged.collect { onUser(it) } }

        scope.launch {
            db.document("config/business").snapshots.catch { }.collect { snap ->
                if (!snap.exists) return@collect
                val b = runCatching { snap.data(Business.serializer()) }.getOrNull() ?: return@collect
                val d = Catalog.business
                _business.value = b.copy(
                    name = b.name.ifBlank { d.name }, tagline = b.tagline.takeUnless { it.isBlank } ?: d.tagline, city = b.city.takeUnless { it.isBlank } ?: d.city,
                    phoneDisplay = b.phoneDisplay.ifBlank { d.phoneDisplay }, phoneDial = b.phoneDial.ifBlank { d.phoneDial }, whatsapp = b.whatsapp.ifBlank { d.whatsapp },
                    hours = b.hours.takeUnless { it.isBlank } ?: d.hours, hoursNote = b.hoursNote.takeUnless { it.isBlank } ?: d.hoursNote,
                )
            }
        }

        // Customers may only read open services (rules), so the query must ask for exactly those.
        scope.launch {
            isAdmin.collectLatest { admin ->
                val q = if (admin) db.collection("services") else db.collection("services").where { "active" equalTo true }
                q.snapshots.catch { }.collect { qs ->
                    val list = qs.documents.mapNotNull { d -> runCatching { d.data(Service.serializer()).copy(id = d.id) }.getOrNull() }
                        .sortedWith(compareBy<Service>({ it.sort }, { it.id }))
                    // An empty answer from the cache on a first offline launch says nothing — keep the bundled list.
                    if (list.isNotEmpty() || !qs.metadata.isFromCache) _services.value = list
                }
            }
        }
    }

    private fun onUser(u: FirebaseUser?) {
        val current = _session.value
        if (u == null) {
            if (current is Session.SignedOut) return
            userJob?.cancel(); userJob = null
            _session.value = Session.SignedOut
            _profile.value = Live.Missing; _adminPhones.value = null; _allOrders.value = Live.Loading
            if (current is Session.SignedIn) local.clearAccount()
            return
        }
        if (current is Session.SignedIn && current.uid == u.uid) return
        userJob?.cancel()
        _profile.value = Live.Loading; _adminPhones.value = null; _allOrders.value = Live.Loading
        _session.value = Session.SignedIn(u.uid, u.phoneNumber.orEmpty())
        userJob = scope.launch {
            val uid = u.uid
            launch {
                // includeMetadataChanges: a "missing" answer from the local cache is confirmed by the server before we believe it.
                db.document("users/$uid").snapshots(includeMetadataChanges = true).catch { if (_profile.value is Live.Loading) _profile.value = Live.Missing }.collect { snap ->
                    if (snap.exists) {
                        val p = runCatching { snap.data(Profile.serializer()) }.getOrNull() ?: return@collect
                        _profile.value = Live.Value(p); local.cacheProfile(p)
                    } else if (!snap.metadata.isFromCache) _profile.value = Live.Missing
                }
            }
            launch {
                db.document("config/roles").snapshots(includeMetadataChanges = true).catch { if (_adminPhones.value == null) _adminPhones.value = emptyList() }.collect { snap ->
                    if (snap.exists) _adminPhones.value = runCatching { snap.get("adminPhones", ListSerializer(String.serializer())) }.getOrDefault(emptyList())
                    else if (!snap.metadata.isFromCache) _adminPhones.value = emptyList()
                }
            }
            launch {
                db.collection("orders").where { "uid" equalTo uid }.snapshots.catch { }.collect { qs ->
                    local.cacheBookings(qs.documents.mapNotNull { it.booking() }.sortedByDescending { it.createdAt })
                }
            }
            launch {
                isAdmin.collectLatest { admin ->
                    if (!admin) { _allOrders.value = Live.Loading; return@collectLatest }
                    db.collection("orders").orderBy("createdAt", dev.gitlive.firebase.firestore.Direction.DESCENDING).limit(500).snapshots
                        .catch { _allOrders.value = Live.Missing }
                        .collect { qs -> _allOrders.value = Live.Value(qs.documents.mapNotNull { it.booking() }) }
                }
            }
            launch { registerToken() }
        }
    }

    private fun DocumentSnapshot.booking(): Booking? = runCatching { data(Booking.serializer()).copy(id = id) }.getOrNull()

    /** One order, live (status screen, admin order detail, notification taps). */
    fun order(id: String): Flow<Live<Booking>> = db.document("orders/$id").snapshots(includeMetadataChanges = true)
        .map { snap -> if (snap.exists) snap.booking()?.let { Live.Value(it) } ?: Live.Missing else if (snap.metadata.isFromCache) Live.Loading else Live.Missing }
        .catch { emit(Live.Missing) }

    private val uid get() = (_session.value as? Session.SignedIn)?.uid ?: throw CloudException(Problem.NOT_ALLOWED)
    private val phone get() = (_session.value as? Session.SignedIn)?.phone.orEmpty()

    private inline fun <T> guard(block: () -> T): T = try { block() } catch (e: TimeoutCancellationException) {
        // A Firebase call that never answered (no network) — not a cancellation of the screen.
        println("CoolingCare Cloud: timeout " + e.message)
        throw CloudException(Problem.OFFLINE, e)
    } catch (e: CancellationException) { throw e } catch (e: Throwable) {
        // Shows up in logcat / the Xcode console; the UI gets a friendly message from [problem].
        println("CoolingCare Cloud: " + e::class.simpleName + ": " + e.message + (e.cause?.let { " <- " + it::class.simpleName + ": " + it.message } ?: ""))
        throw CloudException(e.problem(), e)
    }

    // ---------------------------------------------------------------- sign-in

    /** Sends the SMS code to a Pakistani mobile ("0300 1234567"). Returns the verification id, or null if Android signed in by itself. */
    suspend fun sendCode(localNumber: String, resend: Boolean = false): String? = guard {
        val e164 = Phone.e164(localNumber) ?: throw CloudException(Problem.INVALID_NUMBER)
        withTimeout(90.seconds) { PhoneAuth.sendCode(e164, resend) }
    }

    suspend fun verifyCode(verificationId: String, code: String) = guard {
        auth.signInWithCredential(PhoneAuthProvider(auth).credential(verificationId, code))
        Unit
    }

    /** Removes this phone's push token from the account, then signs out. */
    suspend fun signOut() {
        val s = _session.value as? Session.SignedIn
        val p = (profile.value as? Live.Value)?.value
        val t = pushToken
        if (s != null && p != null && t != null && t in p.tokens) {
            withTimeoutOrNull(5.seconds) { runCatching { db.document("users/${s.uid}").updateFields { "tokens" to (p.tokens - t) } } }
        }
        runCatching { withTimeoutOrNull(5.seconds) { Firebase.messaging.deleteToken() } }
        pushToken = null
        auth.signOut()
    }

    /** Deletes the profile and the login. Orders stay with the shop (its records). */
    suspend fun deleteAccount() = guard {
        val user = auth.currentUser ?: throw CloudException(Problem.NOT_ALLOWED)
        withTimeout(15.seconds) { db.document("users/${user.uid}").delete() }
        user.delete()
        runCatching { withTimeoutOrNull(5.seconds) { Firebase.messaging.deleteToken() } }
        pushToken = null
    }

    // ---------------------------------------------------------------- profile & push

    /** Saves name / address (+ language and this phone's push token). Offline the write is queued and synced later. */
    suspend fun saveProfile(p: Profile) = guard {
        val now = Dates.nowMs()
        val old = (profile.value as? Live.Value)?.value
        val tokens = (p.tokens + listOfNotNull(pushToken)).distinct().takeLast(10)
        val doc = p.copy(phone = phone, tokens = tokens, lang = (local.prefs.value.lang ?: Lang.EN).code,
            name = p.name.trim().take(60), house = p.house.trim().take(200), landmark = p.landmark.trim().take(120),
            createdAt = old?.createdAt?.takeIf { it > 0 } ?: now, updatedAt = now)
        local.cacheProfile(doc)
        _profile.value = Live.Value(doc)
        withTimeoutOrNull(8.seconds) { db.document("users/$uid").set(Profile.serializer(), doc) }
        Unit
    }

    /** First admin log-in: creates the account record (name "Cooling Care") so new-order pushes can reach this phone. */
    fun ensureAdminProfile() {
        if (profile.value !is Live.Missing || !isAdmin.value) return
        scope.launch { runCatching { saveProfile(Profile(name = Catalog.business.name)) } }
    }

    /** Keeps users/{uid}.lang in step with the app language so pushes arrive in it. */
    fun syncLanguage(lang: Lang) {
        val p = (profile.value as? Live.Value)?.value ?: return
        if (p.lang == lang.code) return
        scope.launch { runCatching { withTimeoutOrNull(8.seconds) { db.document("users/$uid").updateFields { "lang" to lang.code } } } }
    }

    fun setAdminAlerts(on: Boolean) {
        scope.launch { runCatching { withTimeoutOrNull(8.seconds) { db.document("users/$uid").updateFields { "adminAlerts" to on } } } }
    }

    /** Called on sign-in and whenever FCM hands out a new token (Android service / iOS delegate). */
    fun onNewToken(token: String) { scope.launch { registerToken(token) } }

    private suspend fun registerToken(token: String? = null) {
        val t = token ?: runCatching { Firebase.messaging.getToken() }.getOrNull() ?: return
        pushToken = t
        val s = _session.value as? Session.SignedIn ?: return
        // A new account gets the token with its first save (saveProfile).
        val p = _profile.first { it !is Live.Loading } as? Live.Value ?: return
        if (t in p.value.tokens) return
        val tokens = (p.value.tokens - t + t).takeLast(10)
        runCatching { withTimeoutOrNull(8.seconds) { db.document("users/${s.uid}").updateFields { "tokens" to tokens } } }
    }

    // ---------------------------------------------------------------- customer orders

    /**
     * Places an order: checks the service is still open (needs internet), uploads the photos to Storage,
     * then takes the next order number and creates the order in one transaction. Returns "CC-1004".
     */
    suspend fun placeOrder(d: Draft, lang: Lang): String = guard {
        val me = uid
        val svc = service(d.serviceId) ?: throw CloudException(Problem.SERVICE_CLOSED)
        val ap = Catalog.appliance(d.applianceId) ?: throw CloudException(Problem.OTHER)
        val slot = d.slotId?.let { Catalog.slot(it) } ?: throw CloudException(Problem.OTHER)
        val date = d.date ?: throw CloudException(Problem.OTHER)
        // A previous try whose answer got lost may already have created the order.
        existingOrder(me, d.clientRef)?.let { log("order: already placed as $it"); return@guard it }
        // Server read: fails fast offline, and customers cannot read a closed service.
        log("order: checking service ${svc.id}")
        try {
            val snap = withTimeout(15.seconds) { db.document("services/${svc.id}").get(Source.SERVER) }
            if (!snap.exists || snap.get<Boolean?>("active") != true) throw CloudException(Problem.SERVICE_CLOSED)
        } catch (e: FirebaseFirestoreException) {
            throw CloudException(if (e.code == FirestoreExceptionCode.PERMISSION_DENIED) Problem.SERVICE_CLOSED else e.problem(), e)
        }
        log("order: uploading ${d.photos.size} photo(s)")
        val folder = "o" + Dates.nowMs().toString(36) + Random.nextInt(1000, 9999)
        val photos = d.photos.toList().mapIndexedNotNull { i, path ->
            val bytes = Graph.photos.read(path) ?: return@mapIndexedNotNull null
            val ref = "orders/$me/$folder/${i + 1}.jpg"
            storage.reference(ref).putData(bytes.toStorageData(), storageMetadata { contentType = "image/jpeg" })
            ref
        }
        val now = Dates.nowMs()
        val order = Booking(
            id = "", uid = me, serviceId = svc.id, service = svc.title, serviceImage = svc.image, serviceImagePath = svc.imagePath,
            applianceId = ap.id, appliance = ap.name, date = date, dateLabel = Dates.short(Dates.parse(date)), slotId = slot.id, slot = slot.label,
            issue = d.issue.trim().take(1000), photos = photos, name = d.name.trim().take(60), phone = (Phone.normalize(d.phone) ?: d.phone.trim()).take(20),
            address = d.address.trim().take(300), status = BookingStatus.REQUESTED, history = listOf(HistoryEntry(BookingStatus.REQUESTED, now, "customer", date = date, slotId = slot.id)),
            lang = lang.code, createdAt = now, updatedAt = now, clientRef = d.clientRef,
        )
        val counter = db.document("counters/orders")
        log("order: transaction")
        val id = try { withTimeout(30.seconds) {
            db.runTransaction {
                val next = get(counter).get<Long>("next")
                val id = "CC-$next"
                updateFields(counter) { "next" to next + 1 }
                set(db.document("orders/$id"), Booking.serializer(), order.copy(id = id))
                id
            }
        } } catch (e: TimeoutCancellationException) {
            // The commit may still have gone through — look before reporting a failure.
            existingOrder(me, d.clientRef) ?: throw e
        }
        // Show it right away, before the listener catches up.
        local.cacheBookings((listOf(order.copy(id = id)) + local.bookings.value.filterNot { it.id == id }).sortedByDescending { it.createdAt })
        id
    }

    /** The order this form already created, if any (server answer only; null when unknown). */
    private suspend fun existingOrder(uid: String, clientRef: String): String? = if (clientRef.isBlank()) null else runCatching {
        withTimeout(10.seconds) {
            db.collection("orders").where { "uid" equalTo uid }.where { "clientRef" equalTo clientRef }.get(Source.SERVER).documents.firstOrNull()?.id
        }
    }.getOrNull()

    /** Runs [change] on the latest version of the order inside a transaction (needs internet, so it never silently fails later). */
    private suspend fun changeOrder(id: String, change: suspend dev.gitlive.firebase.firestore.Transaction.(Booking, Long) -> Unit) = guard {
        val ref = db.document("orders/$id")
        withTimeout(20.seconds) {
            db.runTransaction {
                val b = get(ref).booking() ?: throw CloudException(Problem.NOT_ALLOWED)
                this.change(b, Dates.nowMs())
            }
        }
    }

    suspend fun cancelByCustomer(id: String, reason: String) = changeOrder(id) { b, now ->
        if (!b.status.customerCanChange) throw CloudException(Problem.NOT_ALLOWED)
        val history = b.history + HistoryEntry(BookingStatus.CANCELLED, now, "customer", note = reason, date = b.date, slotId = b.slotId)
        updateFields(db.document("orders/$id")) { "status" to BookingStatus.CANCELLED; "cancelReason" to reason; "history" to history; "updatedAt" to now }
    }

    /** Customer asks for another time: back to "requested" so the shop confirms it again. */
    suspend fun rescheduleByCustomer(id: String, date: String, slotId: String) = changeOrder(id) { b, now ->
        if (!b.status.customerCanChange) throw CloudException(Problem.NOT_ALLOWED)
        val slot = Catalog.slot(slotId) ?: throw CloudException(Problem.OTHER)
        val history = b.history + HistoryEntry(BookingStatus.REQUESTED, now, "customer", date = date, slotId = slotId)
        updateFields(db.document("orders/$id")) {
            "date" to date; "dateLabel" to Dates.short(Dates.parse(date)); "slotId" to slotId; "slot" to slot.label
            "status" to BookingStatus.REQUESTED; "history" to history; "updatedAt" to now
        }
    }

    suspend fun rate(id: String, stars: Int) = guard {
        withTimeout(15.seconds) { db.document("orders/$id").updateFields { "rating" to stars.coerceIn(1, 5); "updatedAt" to Dates.nowMs() } }
    }

    // ---------------------------------------------------------------- admin: orders

    /** Admin moves the order on (the Cloud Function pushes it to the customer). [note] = cancel reason / message. */
    suspend fun setStatus(id: String, status: BookingStatus, note: String = "") = changeOrder(id) { b, now ->
        if (b.status == status) return@changeOrder
        val history = b.history + HistoryEntry(status, now, "admin", note = note.trim(), date = b.date, slotId = b.slotId)
        updateFields(db.document("orders/$id")) {
            "status" to status; "history" to history; "updatedAt" to now
            if (status == BookingStatus.COMPLETED) "completedAt" to now
            if (status == BookingStatus.CANCELLED) "cancelReason" to note.trim()
        }
    }

    /** Admin sets a different visit time and confirms it in one step. */
    suspend fun confirmAtTime(id: String, date: String, slotId: String) = changeOrder(id) { b, now ->
        val slot = Catalog.slot(slotId) ?: throw CloudException(Problem.OTHER)
        val history = b.history + HistoryEntry(BookingStatus.CONFIRMED, now, "admin", date = date, slotId = slotId)
        updateFields(db.document("orders/$id")) {
            "date" to date; "dateLabel" to Dates.short(Dates.parse(date)); "slotId" to slotId; "slot" to slot.label
            "status" to BookingStatus.CONFIRMED; "history" to history; "updatedAt" to now
        }
    }

    suspend fun saveAdminNote(id: String, note: String) = guard {
        withTimeoutOrNull(8.seconds) { db.document("orders/$id").updateFields { "adminNote" to note.trim().take(500) } }
        Unit
    }

    // ---------------------------------------------------------------- admin: services & business

    fun newServiceId(): String = "svc_" + Dates.nowMs().toString(36) + Random.nextInt(100, 999)

    /**
     * Creates or replaces a service. [photo] = a new local JPEG to upload (becomes [Service.imagePath]);
     * [previousPath] = the uploaded photo the service had before, deleted when it is no longer used.
     */
    suspend fun saveService(s: Service, photo: String?, previousPath: String) = guard {
        val now = Dates.nowMs()
        var imagePath = s.imagePath
        if (photo != null) {
            val bytes = Graph.photos.read(photo) ?: throw CloudException(Problem.OTHER)
            imagePath = "services/${s.id}_${now.toString(36)}.jpg"
            withTimeout(60.seconds) { storage.reference(imagePath).putData(bytes.toStorageData(), storageMetadata { contentType = "image/jpeg" }) }
        }
        val doc = s.copy(imagePath = imagePath, createdAt = s.createdAt.takeIf { it > 0 } ?: now, updatedAt = now)
        withTimeout(20.seconds) { db.document("services/${s.id}").set(Service.serializer(), doc) }
        _services.value = (_services.value.filterNot { it.id == s.id } + doc.copy(id = s.id)).sortedWith(compareBy<Service>({ it.sort }, { it.id }))
        if (previousPath.isNotBlank() && previousPath != imagePath) runCatching { storage.reference(previousPath).delete() }
    }

    /** Opens / closes a service for customers. Queued when offline. */
    suspend fun setServiceOpen(id: String, open: Boolean) = guard {
        _services.value = _services.value.map { if (it.id == id) it.copy(active = open) else it }
        withTimeoutOrNull(8.seconds) { db.document("services/$id").updateFields { "active" to open; "updatedAt" to Dates.nowMs() } }
        Unit
    }

    suspend fun deleteService(s: Service) = guard {
        withTimeout(20.seconds) { db.document("services/${s.id}").delete() }
        if (s.imagePath.isNotBlank()) runCatching { storage.reference(s.imagePath).delete() }
    }

    suspend fun saveBusiness(b: Business) = guard {
        withTimeout(20.seconds) { db.document("config/business").set(Business.serializer(), b.copy(updatedAt = Dates.nowMs()), merge = true) }
    }

    // ---------------------------------------------------------------- images

    /** Downloads a Storage file (order photo / service picture), max 4 MB. */
    suspend fun download(path: String): ByteArray = guard { storage.reference(path).getData(4L * 1024 * 1024).toByteArray() }
}
