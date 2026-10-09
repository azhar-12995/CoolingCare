package com.coolingcare.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.EventKind
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.domain.Planner
import com.coolingcare.app.domain.addressLine
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.Lang
import com.coolingcare.app.model.Prefs
import com.coolingcare.app.model.Profile
import com.coolingcare.app.platform.FirebaseEnv
import com.coolingcare.app.platform.KeyValueStore
import com.coolingcare.app.platform.Launcher
import com.coolingcare.app.platform.OsReminder
import com.coolingcare.app.platform.PhotoFiles
import com.coolingcare.app.platform.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * What lives on the phone: preferences, plus a cached copy of the signed-in customer's profile and orders
 * (Firestore is the source of truth — [Cloud] writes the copies) so the app opens instantly and reminders
 * can be re-created after a reboot without the network.
 */
class Repository(private val kv: KeyValueStore, private val scheduler: ReminderScheduler, private val photos: PhotoFiles) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _prefs = MutableStateFlow(read("prefs") { json.decodeFromString(Prefs.serializer(), it) } ?: Prefs())
    private val _profile = MutableStateFlow(read("profile") { json.decodeFromString(Profile.serializer(), it) } ?: Profile())
    private val _bookings = MutableStateFlow(read("orders") { json.decodeFromString(ListSerializer(Booking.serializer()), it) } ?: emptyList())
    val prefs: StateFlow<Prefs> = _prefs.asStateFlow()
    val profile: StateFlow<Profile> = _profile.asStateFlow()
    /** The signed-in customer's orders, newest first. */
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private inline fun <T> read(key: String, decode: (String) -> T): T? = kv.get(key)?.let { runCatching { decode(it) }.getOrNull() }

    fun updatePrefs(f: (Prefs) -> Prefs) {
        val p = f(_prefs.value); _prefs.value = p; kv.put("prefs", json.encodeToString(Prefs.serializer(), p)); resync()
    }

    fun setLang(lang: Lang) = updatePrefs { it.copy(lang = lang) }

    internal fun cacheProfile(p: Profile) { _profile.value = p; kv.put("profile", json.encodeToString(Profile.serializer(), p)) }

    internal fun cacheBookings(list: List<Booking>) {
        if (list == _bookings.value) return
        _bookings.value = list; kv.put("orders", json.encodeToString(ListSerializer(Booking.serializer()), list)); resync()
    }

    fun booking(id: String) = _bookings.value.firstOrNull { it.id == id }

    fun markNotificationsSeen() = updatePrefs { it.copy(notifSeenAt = Dates.nowMs()) }

    fun dismissMaintenance(bookingId: String) = updatePrefs { it.copy(dismissedMaintenance = (it.dismissedMaintenance + bookingId).distinct()) }

    /** Log out / account deleted: forget the cached account data, keep language + onboarding. */
    fun clearAccount() {
        photos.deleteAll()
        _bookings.value = emptyList(); _profile.value = Profile()
        kv.put("orders", "[]"); kv.put("profile", json.encodeToString(Profile.serializer(), Profile()))
        updatePrefs { Prefs(lang = it.lang, onboarded = it.onboarded, visitReminders = it.visitReminders, maintenanceReminders = it.maintenanceReminders) }
    }

    /** Re-creates OS reminders from the current orders (call on launch, after boot and after every change). */
    fun resync() {
        val lang = _prefs.value.lang ?: Lang.EN
        val list = Planner.scheduled(_bookings.value, _prefs.value).take(60).map {
            OsReminder(it.notificationId, it.at, it.title.get(lang), it.body.get(lang), it.route,
                if (it.kind == EventKind.MAINTENANCE) OsReminder.Channel.MAINTENANCE else OsReminder.Channel.VISITS)
        }
        runCatching { scheduler.sync(list) }
    }
}

/** The order being filled in (shared by the form, its sheets and Review). */
class Draft {
    var serviceId by mutableStateOf("ac_deep")
    var applianceId by mutableStateOf("split_ac")
    var date by mutableStateOf<String?>(null)
    var slotId by mutableStateOf<String?>(null)
    var issue by mutableStateOf("")
    /** Local JPEG paths; uploaded to Storage when the order is placed. */
    val photos = mutableStateListOf<String>()
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var address by mutableStateOf("")
    var showErrors by mutableStateOf(false)
    /** Same value for every "Place Order" try of this form (see Booking.clientRef). */
    var clientRef = ""
        private set

    fun start(profile: Profile, lang: Lang, serviceId: String? = null, applianceId: String? = null) {
        val services = Graph.cloud.services.value
        val svc = serviceId?.let { id -> services.firstOrNull { it.id == id } } ?: services.firstOrNull { it.id == "ac_deep" } ?: services.firstOrNull()
        this.serviceId = svc?.id ?: "ac_deep"
        this.applianceId = applianceId?.takeIf { a -> Catalog.appliance(a)?.category == svc?.category || svc == null } ?: defaultAppliance(svc?.category, this.serviceId)
        val first = Dates.firstOpen()
        date = first?.first?.toString(); slotId = first?.second?.id
        issue = ""; photos.forEach { Graph.photos.delete(it) }; photos.clear(); showErrors = false
        clientRef = Dates.nowMs().toString(36) + "-" + kotlin.random.Random.nextLong(1L shl 40).toString(36)
        name = profile.name; phone = if (profile.phone.isBlank()) "" else Phone.display(profile.phone)
        address = if (profile.house.isNotBlank()) profile.addressLine(lang) else ""
    }

    companion object {
        fun defaultAppliance(category: String?, serviceId: String): String = when (category) {
            "refrigeration" -> "refrigerator"
            "commercial" -> when (serviceId) { "cassette" -> "cassette"; "vrf" -> "vrf"; else -> "commercial" }
            else -> if (serviceId == "ac_install") "installation" else "split_ac"
        }
    }
}

/** App-wide singletons, created by the Android Application / iOS entry point. */
object Graph {
    lateinit var repo: Repository
    lateinit var cloud: Cloud
    lateinit var launcher: Launcher
    lateinit var photos: PhotoFiles
    val draft = Draft()
    val ready get() = ::repo.isInitialized

    fun init(kv: KeyValueStore, launcher: Launcher, photos: PhotoFiles, scheduler: ReminderScheduler, env: FirebaseEnv = FirebaseEnv()) {
        if (ready) return
        this.launcher = launcher; this.photos = photos
        repo = Repository(kv, scheduler, photos)
        cloud = Cloud(repo, env)
        repo.resync()
        cloud.start()
    }
}
