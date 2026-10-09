package com.coolingcare.app.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import com.coolingcare.app.MainActivity
import com.coolingcare.app.R
import com.coolingcare.app.data.Graph
import com.coolingcare.app.domain.Phone
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

const val EXTRA_ROUTE = "route"
private const val AUTHORITY_SUFFIX = ".files"

object AndroidApp {
    lateinit var ctx: Context
    /** The visible activity — Firebase Phone Auth needs it for app verification (Play Integrity / reCAPTCHA). */
    var activity: WeakReference<Activity>? = null
}

actual val isAndroid: Boolean = true

class PrefsStore(ctx: Context) : KeyValueStore {
    private val sp = ctx.getSharedPreferences("cooling_care", Context.MODE_PRIVATE)
    override fun get(key: String): String? = sp.getString(key, null)
    override fun put(key: String, value: String) = sp.edit { putString(key, value) }
    override fun clear() = sp.edit { clear() }
}

class AndroidLauncher(private val ctx: Context) : Launcher {
    private val biz get() = Graph.cloud.business.value
    override val storeLink = "https://play.google.com/store/apps/details?id=${ctx.packageName}"

    private fun installed(pkg: String) = try { ctx.packageManager.getPackageInfo(pkg, 0); true } catch (_: PackageManager.NameNotFoundException) { false }
    private fun start(i: Intent): Boolean = try { ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true } catch (_: ActivityNotFoundException) { false } catch (_: SecurityException) { false }

    override fun whatsApp(text: String, toDigits: String?): Sent {
        val to = toDigits ?: biz.whatsapp
        val pkg = listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { installed(it) }
        val uri = "https://api.whatsapp.com/send?phone=$to&text=${Uri.encode(text)}".toUri()
        if (pkg != null && start(Intent(Intent.ACTION_VIEW, uri).setPackage(pkg))) return Sent.OPENED
        return Sent.NO_APP
    }

    override fun sms(text: String, toDial: String?) = start(Intent(Intent.ACTION_SENDTO, "smsto:${toDial ?: biz.phoneDial}".toUri()).putExtra("sms_body", text))
    override fun dial(number: String?) { start(Intent(Intent.ACTION_DIAL, "tel:${number?.let { Phone.e164(it) ?: it } ?: biz.phoneDial}".toUri())) }
    override fun share(text: String) { start(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null)) }
    override fun openStore() {
        if (!start(Intent(Intent.ACTION_VIEW, "market://details?id=${ctx.packageName}".toUri()))) start(Intent(Intent.ACTION_VIEW, storeLink.toUri()))
    }
}

class AndroidPhotoFiles(private val ctx: Context) : PhotoFiles {
    val dir: File get() = File(ctx.filesDir, "photos").apply { mkdirs() }
    override fun read(path: String): ByteArray? = runCatching { File(path).readBytes() }.getOrNull()
    override fun delete(path: String) { runCatching { File(path).delete() } }
    override fun deleteAll() { runCatching { dir.deleteRecursively() } }

    /** Decodes (EXIF-rotated on Android 9+), downsizes to 1600 px and stores as JPEG 82% (~300 KB). */
    fun import(uri: Uri): String? = runCatching {
        val bmp: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri)) { dec, info, _ ->
                val s = info.size; val k = 1600f / max(s.width, s.height)
                if (k < 1f) dec.setTargetSize((s.width * k).roundToInt(), (s.height * k).roundToInt())
                dec.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1; while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1600) sample *= 2
            ctx.contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })!! }
        }
        val f = File(dir, UUID.randomUUID().toString() + ".jpg")
        FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.JPEG, 82, it) }
        f.absolutePath
    }.getOrNull()
}

@Composable
actual fun rememberPhotoPicker(remaining: Int, onPicked: (List<String>) -> Unit): PhotoPicker {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val files = Graph.photos as AndroidPhotoFiles
    fun handle(uris: List<Uri>) { if (uris.isEmpty()) return; scope.launch { val paths = withContext(Dispatchers.IO) { uris.mapNotNull { files.import(it) } }; onPicked(paths) } }
    val many = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(3)) { handle(it.take(remaining)) }
    val one = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let { u -> handle(listOf(u)) } }
    var pending by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> pending?.let { u -> if (ok) handle(listOf(u)) }; pending = null }
    return PhotoPicker(
        gallery = {
            val req = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (remaining > 1) many.launch(req) else if (remaining == 1) one.launch(req)
        },
        camera = {
            val f = File(ctx.cacheDir, "camera").apply { mkdirs() }.let { File(it, "shot_${System.currentTimeMillis()}.jpg") }
            val u = FileProvider.getUriForFile(ctx, ctx.packageName + AUTHORITY_SUFFIX, f)
            pending = u
            runCatching { camera.launch(u) }
        },
    )
}

actual fun decodeImage(bytes: ByteArray, minSide: Int): ImageBitmap? = runCatching {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    var s = 1; while (max(opts.outWidth, opts.outHeight) / (s * 2) >= minSide) s *= 2
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = s })?.asImageBitmap()
}.getOrNull()

actual object ImageDiskCache {
    private val dir get() = File(AndroidApp.ctx.cacheDir, "img").apply { mkdirs() }
    private fun file(key: String) = File(dir, MessageDigest.getInstance("SHA-1").digest(key.toByteArray()).joinToString("") { "%02x".format(it) })
    actual fun get(key: String): ByteArray? = runCatching { file(key).takeIf { it.exists() }?.readBytes() }.getOrNull()
    actual fun put(key: String, bytes: ByteArray) { runCatching { file(key).writeBytes(bytes) } }
    actual fun clear() { runCatching { dir.deleteRecursively() } }
}

actual fun ByteArray.toStorageData(): dev.gitlive.firebase.storage.Data = dev.gitlive.firebase.storage.Data(this)
actual fun dev.gitlive.firebase.storage.Data.toByteArray(): ByteArray = data

actual object PhoneAuth {
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    actual suspend fun sendCode(phoneE164: String, resend: Boolean): String? = suspendCancellableCoroutine { cont ->
        val activity = AndroidApp.activity?.get()
        if (activity == null) { cont.resumeWithException(IllegalStateException("No activity for phone verification")); return@suspendCancellableCoroutine }
        val auth = FirebaseAuth.getInstance()
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                resendToken = token
                if (cont.isActive) cont.resume(verificationId)
            }
            // Instant verification or SMS auto-retrieval (also after the code screen is open): sign in straight away.
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential).addOnCompleteListener { t ->
                    if (cont.isActive) { if (t.isSuccessful) cont.resume(null) else cont.resumeWithException(t.exception ?: IllegalStateException("sign-in failed")) }
                }
            }
            override fun onVerificationFailed(e: FirebaseException) { if (cont.isActive) cont.resumeWithException(e) }
        }
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneE164).setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity).setCallbacks(callbacks)
        if (resend) resendToken?.let { options.setForceResendingToken(it) }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }
}

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) = androidx.activity.compose.BackHandler(enabled, onBack)

actual val appVersion: String
    get() = runCatching { AndroidApp.ctx.packageManager.getPackageInfo(AndroidApp.ctx.packageName, 0).versionName }.getOrNull() ?: "1.0.0"

@Composable
actual fun rememberNotificationPermission(onResult: (Boolean) -> Unit): NotificationPermission {
    val ctx = LocalContext.current
    fun check() = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    var granted by remember { mutableStateOf(check()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it; onResult(it) }
    return NotificationPermission(granted) { if (Build.VERSION.SDK_INT >= 33) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else onResult(true) }
}

object Channels {
    const val VISITS = "visits"; const val MAINTENANCE = "maintenance"; const val ORDERS = "orders"
    fun ensure(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val ur = Graph.ready && Graph.repo.prefs.value.lang == com.coolingcare.app.model.Lang.UR
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(listOf(
            NotificationChannel(VISITS, if (ur) "وزٹ کی اطلاعات اور یاد دہانی" else "Visit updates & reminders", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(MAINTENANCE, if (ur) "مینٹیننس یاد دہانی" else "Maintenance reminders", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(ORDERS, if (ur) "نئے آرڈر (ایڈمن)" else "New orders (admin)", NotificationManager.IMPORTANCE_HIGH),
        ))
    }
}

/** Posts a notification that opens [route] in the app. */
@SuppressLint("MissingPermission")
internal fun postNotification(ctx: Context, id: Int, channel: String, title: String?, body: String, route: String) {
    Channels.ensure(ctx)
    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
    val open = Intent(ctx, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP; putExtra(EXTRA_ROUTE, route); data = "coolingcare://open/$id".toUri()
    }
    val pi = PendingIntent.getActivity(ctx, id, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val n = NotificationCompat.Builder(ctx, channel)
        .setSmallIcon(R.drawable.ic_stat_snowflake).setColor(0xFF0A66C8.toInt())
        .setContentTitle(title).setContentText(body).setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setContentIntent(pi).build()
    runCatching { NotificationManagerCompat.from(ctx).notify(id, n) }
}

/** AlarmManager reminders: fire with the app closed and in Doze; re-created after reboot by [BootReceiver]. */
class AndroidReminderScheduler(private val ctx: Context) : ReminderScheduler {
    private val am get() = ctx.getSystemService(AlarmManager::class.java)
    private val sp get() = ctx.getSharedPreferences("alarms", Context.MODE_PRIVATE)

    private fun pending(r: OsReminder?, id: Int, flags: Int): PendingIntent? {
        val i = Intent(ctx, ReminderReceiver::class.java).apply {
            action = "com.coolingcare.REMINDER"; data = "reminder://$id".toUri()
            if (r != null) { putExtra("id", r.id); putExtra("title", r.title); putExtra("body", r.body); putExtra(EXTRA_ROUTE, r.route); putExtra("channel", r.channel.name) }
        }
        return PendingIntent.getBroadcast(ctx, id, i, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    @SuppressLint("MissingPermission")
    override fun sync(reminders: List<OsReminder>) {
        Channels.ensure(ctx)
        val old = sp.getStringSet("ids", emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }
        val now = reminders.map { it.id }.toSet()
        for (id in old - now) pending(null, id, PendingIntent.FLAG_NO_CREATE)?.let { am.cancel(it); it.cancel() }
        for (r in reminders) {
            val pi = pending(r, r.id, PendingIntent.FLAG_UPDATE_CURRENT) ?: continue
            val exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
            try { if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.at, pi) else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.at, pi) }
            catch (_: SecurityException) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.at, pi) }
        }
        sp.edit { putStringSet("ids", now.map { it.toString() }.toSet()) }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val channel = if (intent.getStringExtra("channel") == OsReminder.Channel.MAINTENANCE.name) Channels.MAINTENANCE else Channels.VISITS
        postNotification(ctx, intent.getIntExtra("id", 0), channel, intent.getStringExtra("title"), intent.getStringExtra("body").orEmpty(), intent.getStringExtra(EXTRA_ROUTE).orEmpty())
    }
}

/** Re-creates alarms after reboot, app update and time / time-zone changes. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch { try { if (Graph.ready) Graph.repo.resync() } finally { pending.finish() } }
    }
}

/**
 * FCM: order pushes from the Cloud Functions (new order → admins, status change → customer).
 * In the background Android shows them itself (channel + icon from the payload / manifest); in the foreground we post them here.
 */
class CcMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) { if (Graph.ready) Graph.cloud.onNewToken(token) }

    override fun onMessageReceived(message: RemoteMessage) {
        val n = message.notification ?: return
        val route = message.data[EXTRA_ROUTE].orEmpty()
        val channel = n.channelId?.takeIf { it == Channels.ORDERS || it == Channels.VISITS } ?: Channels.VISITS
        postNotification(this, (route + (message.messageId ?: System.currentTimeMillis().toString())).hashCode() and 0x7fffffff, channel, n.title, n.body.orEmpty(), route)
    }
}
