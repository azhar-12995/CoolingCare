package com.coolingcare.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Tiny persistent key → string store (SharedPreferences / NSUserDefaults). */
interface KeyValueStore {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun clear()
}

enum class Sent { OPENED, NO_APP }

/** Everything that leaves the app: WhatsApp, SMS, phone calls, share sheet, store pages. */
interface Launcher {
    /** Opens a WhatsApp chat with [toDigits] (country code, no +; default: the shop) and the [text] typed in. */
    fun whatsApp(text: String, toDigits: String? = null): Sent
    fun sms(text: String, toDial: String? = null): Boolean
    /** Dials [number] (E.164 or local; default: the shop). */
    fun dial(number: String? = null)
    fun share(text: String)
    fun openStore()
    val storeLink: String
}

/** Compressed JPEGs picked for a booking (or a service photo), kept in the app's private storage until uploaded. */
interface PhotoFiles {
    fun read(path: String): ByteArray?
    fun delete(path: String)
    fun deleteAll()
}

data class OsReminder(val id: Int, val at: Long, val title: String, val body: String, val route: String, val channel: Channel) {
    enum class Channel { VISITS, MAINTENANCE }
}

/** Schedules OS notifications that fire even when the app is closed. */
interface ReminderScheduler {
    /** Replaces every pending reminder with [reminders]. */
    fun sync(reminders: List<OsReminder>)
}

/** Routes from tapped notifications ("booking/CC-1234", "book/ac_deep", admins: "admin/order/CC-1234"). */
object DeepLinks {
    private val _links = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 4)
    val links: SharedFlow<String> = _links.asSharedFlow()
    fun emit(route: String) { _links.tryEmit(route) }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun consumed() { _links.resetReplayCache() }
}

class PhotoPicker(val gallery: () -> Unit, val camera: () -> Unit)

/** Gallery / camera pickers. [onPicked] receives paths of compressed JPEGs already saved in app storage. */
@Composable
expect fun rememberPhotoPicker(remaining: Int, onPicked: (List<String>) -> Unit): PhotoPicker

class NotificationPermission(val granted: Boolean, val request: () -> Unit)

/** Android 13+ POST_NOTIFICATIONS / iOS authorization. */
@Composable
expect fun rememberNotificationPermission(onResult: (Boolean) -> Unit = {}): NotificationPermission

/** Decodes a JPEG / PNG / WebP, downsampled so the long side is at least [minSide] px (thumbnails stay small in memory). */
expect fun decodeImage(bytes: ByteArray, minSide: Int = 400): ImageBitmap?

/** Small on-disk cache for downloaded images (Storage photos), kept in the OS cache folder. */
expect object ImageDiskCache {
    fun get(key: String): ByteArray?
    fun put(key: String, bytes: ByteArray)
    fun clear()
}

/** Firebase Storage needs the platform's byte container. */
expect fun ByteArray.toStorageData(): dev.gitlive.firebase.storage.Data
expect fun dev.gitlive.firebase.storage.Data.toByteArray(): ByteArray

/**
 * Phone sign-in, step 1: sends the SMS code. Returns the verification id for step 2, or null when Android
 * verified the number by itself (instant verification / SMS auto-retrieval) and the user is already signed in.
 * Step 2 (the code the user typed) is common code: [com.coolingcare.app.data.Cloud.verifyCode].
 */
expect object PhoneAuth {
    suspend fun sendCode(phoneE164: String, resend: Boolean): String?
}

/** How Firebase is reached. Debug builds can point at the local Emulator Suite. */
data class FirebaseEnv(val useEmulators: Boolean = false, val emulatorHost: String = "10.0.2.2")

expect val appVersion: String

/** True on Android (SMS codes fill in by themselves there). */
expect val isAndroid: Boolean

/** Android system back (iOS has none — swipe-back is handled by navigation). */
@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
