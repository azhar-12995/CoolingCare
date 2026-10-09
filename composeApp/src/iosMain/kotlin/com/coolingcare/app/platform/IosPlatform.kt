@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.coolingcare.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.coolingcare.app.data.Graph
import com.coolingcare.app.domain.Phone
import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRPhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSError
import platform.Foundation.dataWithBytes
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.writeToFile
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import org.jetbrains.skia.Image as SkiaImage

class DefaultsStore : KeyValueStore {
    private val d = NSUserDefaults.standardUserDefaults
    override fun get(key: String): String? = d.stringForKey("cc.$key")
    override fun put(key: String, value: String) = d.setObject(value, forKey = "cc.$key")
    override fun clear() { for (k in listOf("prefs", "profile", "orders")) d.removeObjectForKey("cc.$k") }
}

private fun topController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}

private fun String.urlEncoded(): String = (NSString.create(string = this).stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: this)
    .replace("&", "%26").replace("+", "%2B").replace("#", "%23")

private fun open(url: String): Boolean {
    val u = NSURL.URLWithString(url) ?: return false
    if (!UIApplication.sharedApplication.canOpenURL(u)) return false
    UIApplication.sharedApplication.openURL(u, emptyMap<Any?, Any?>(), null)
    return true
}

actual val isAndroid: Boolean = false

class IosLauncher : Launcher {
    private val biz get() = Graph.cloud.business.value
    /** Replace with the real App Store link once the app is published. */
    override val storeLink = "https://apps.apple.com/app/cooling-care"

    override fun whatsApp(text: String, toDigits: String?): Sent =
        if (open("whatsapp://send?phone=${toDigits ?: biz.whatsapp}&text=${text.urlEncoded()}")) Sent.OPENED else Sent.NO_APP

    override fun sms(text: String, toDial: String?) = open("sms:${toDial ?: biz.phoneDial}&body=${text.urlEncoded()}")
    override fun dial(number: String?) { open("tel:${number?.let { Phone.e164(it) ?: it } ?: biz.phoneDial}") }
    override fun share(text: String) { topController()?.presentViewController(UIActivityViewController(listOf(text), null), animated = true, completion = null) }
    override fun openStore() { open(storeLink) }
}

class IosPhotoFiles : PhotoFiles {
    val dir: String get() {
        val docs = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
        val p = docs.path + "/photos"
        NSFileManager.defaultManager.createDirectoryAtPath(p, withIntermediateDirectories = true, attributes = null, error = null)
        return p
    }
    override fun read(path: String): ByteArray? {
        val data = NSData.dataWithContentsOfFile(path) ?: return null
        val size = data.length.toInt(); if (size == 0) return null
        return ByteArray(size).apply { usePinned { memcpy(it.addressOf(0), data.bytes, data.length) } }
    }
    override fun delete(path: String) { NSFileManager.defaultManager.removeItemAtPath(path, null) }
    override fun deleteAll() { NSFileManager.defaultManager.removeItemAtPath(dir, null) }

    /** Downsizes to 1600 px on the long side and stores JPEG 82%. */
    fun save(image: UIImage): String? {
        val (w, h) = image.size.useContents { width to height }
        val k = minOf(1.0, 1600.0 / maxOf(w, h))
        val size = CGSizeMake(w * k, h * k)
        val scaled = UIGraphicsImageRenderer(size).imageWithActions { image.drawInRect(CGRectMake(0.0, 0.0, w * k, h * k)) }
        val data = UIImageJPEGRepresentation(scaled, 0.82) ?: return null
        val path = dir + "/" + NSUUID().UUIDString + ".jpg"
        return if (data.writeToFile(path, atomically = true)) path else null
    }
}

// Picker delegates must outlive the presented controller.
private var galleryDelegate: NSObject? = null
private var cameraDelegate: NSObject? = null

@Composable
actual fun rememberPhotoPicker(remaining: Int, onPicked: (List<String>) -> Unit): PhotoPicker {
    val files = com.coolingcare.app.data.Graph.photos as IosPhotoFiles
    return remember(remaining) {
        PhotoPicker(
            gallery = gallery@{
                if (remaining <= 0) return@gallery
                val config = PHPickerConfiguration().apply { selectionLimit = remaining.toLong(); filter = PHPickerFilter.imagesFilter }
                val picker = PHPickerViewController(config)
                val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
                    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                        picker.dismissViewControllerAnimated(true, null)
                        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
                        val out = mutableListOf<String>(); var left = results.size
                        if (left == 0) return
                        for (r in results) r.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                            val path = data?.let { UIImage.imageWithData(it) }?.let { files.save(it) }
                            dispatch_async(dispatch_get_main_queue()) { if (path != null) out += path; left--; if (left == 0) onPicked(out) }
                        }
                    }
                }
                galleryDelegate = delegate; picker.delegate = delegate
                topController()?.presentViewController(picker, animated = true, completion = null)
            },
            camera = camera@{
                if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) return@camera
                val picker = UIImagePickerController().apply { sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera }
                val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
                    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
                        picker.dismissViewControllerAnimated(true, null)
                        (didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)?.let { files.save(it) }?.let { onPicked(listOf(it)) }
                    }
                    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) { picker.dismissViewControllerAnimated(true, null) }
                }
                cameraDelegate = delegate; picker.delegate = delegate
                topController()?.presentViewController(picker, animated = true, completion = null)
            },
        )
    }
}

actual fun decodeImage(bytes: ByteArray, minSide: Int): ImageBitmap? = runCatching { SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()

private fun NSData.bytesCopy(): ByteArray {
    val size = length.toInt(); if (size == 0) return ByteArray(0)
    return ByteArray(size).apply { usePinned { memcpy(it.addressOf(0), bytes, length) } }
}

private fun ByteArray.nsData(): NSData = if (isEmpty()) NSData() else usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }

actual object ImageDiskCache {
    private val dir: String get() {
        val caches = NSFileManager.defaultManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).first() as NSURL
        val p = caches.path + "/img"
        NSFileManager.defaultManager.createDirectoryAtPath(p, withIntermediateDirectories = true, attributes = null, error = null)
        return p
    }
    private fun file(key: String) = dir + "/" + key.replace(Regex("[^A-Za-z0-9._-]"), "_")
    actual fun get(key: String): ByteArray? = NSData.dataWithContentsOfFile(file(key))?.bytesCopy()
    actual fun put(key: String, bytes: ByteArray) { bytes.nsData().writeToFile(file(key), atomically = true) }
    actual fun clear() { NSFileManager.defaultManager.removeItemAtPath(dir, null) }
}

actual fun ByteArray.toStorageData(): dev.gitlive.firebase.storage.Data = dev.gitlive.firebase.storage.Data(nsData())
actual fun dev.gitlive.firebase.storage.Data.toByteArray(): ByteArray = data.bytesCopy()

/** Firebase phone auth on iOS: silent APNs verification, or a reCAPTCHA page when push is not available. */
actual object PhoneAuth {
    actual suspend fun sendCode(phoneE164: String, resend: Boolean): String? = suspendCancellableCoroutine { cont ->
        FIRPhoneAuthProvider.providerWithAuth(FIRAuth.auth()).verifyPhoneNumber(phoneE164, null) { id: String?, error: NSError? ->
            if (!cont.isActive) return@verifyPhoneNumber
            if (error != null || id == null) cont.resumeWithException(PhoneAuthError(error?.code ?: 0L, error?.localizedDescription ?: "verification failed"))
            else cont.resume(id)
        }
    }
}

/** Maps Firebase Auth's iOS error codes to the messages the common code understands (see Cloud.problem). */
class PhoneAuthError(code: Long, description: String) : Exception(
    when (code) {
        17042L, 17041L -> "INVALID_PHONE_NUMBER"
        17010L -> "TOO_MANY_REQUESTS"
        17020L -> "network error $description"
        else -> description
    },
)

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {}

actual val appVersion: String
    get() = (platform.Foundation.NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "1.0.0"

private val center get() = UNUserNotificationCenter.currentNotificationCenter()

@Composable
actual fun rememberNotificationPermission(onResult: (Boolean) -> Unit): NotificationPermission {
    var granted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        center.getNotificationSettingsWithCompletionHandler { s ->
            val ok = s?.authorizationStatus == UNAuthorizationStatusAuthorized || s?.authorizationStatus == UNAuthorizationStatusProvisional
            MainScope().launch { granted = ok }
        }
    }
    return NotificationPermission(granted) {
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { ok, _ ->
            MainScope().launch { granted = ok; onResult(ok) }
        }
    }
}

/** One-shot calendar triggers; iOS keeps them across restarts and fires them with the app closed. */
class IosReminderScheduler : ReminderScheduler {
    override fun sync(reminders: List<OsReminder>) {
        center.removeAllPendingNotificationRequests()
        val cal = NSCalendar.currentCalendar
        for (r in reminders.take(60)) { // iOS allows 64 pending requests per app
            val date = NSDate.dateWithTimeIntervalSince1970(r.at / 1000.0)
            val comps = cal.components(NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute, fromDate = date)
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(comps, repeats = false)
            val content = UNMutableNotificationContent().apply {
                setTitle(r.title); setBody(r.body); setSound(UNNotificationSound.defaultSound()); setUserInfo(mapOf<Any?, Any?>("route" to r.route))
            }
            center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier("cc.${r.id}", content, trigger), withCompletionHandler = null)
        }
    }
}

/** Shows reminders as banners while the app is open and routes taps into the app. */
class NotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(center: UNUserNotificationCenter, willPresentNotification: UNNotification, withCompletionHandler: (UNNotificationPresentationOptions) -> Unit) {
        withCompletionHandler(UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionList or UNNotificationPresentationOptionSound)
    }
    override fun userNotificationCenter(center: UNUserNotificationCenter, didReceiveNotificationResponse: UNNotificationResponse, withCompletionHandler: () -> Unit) {
        (didReceiveNotificationResponse.notification.request.content.userInfo["route"] as? String)?.takeIf { it.isNotBlank() }?.let { DeepLinks.emit(it) }
        withCompletionHandler()
    }
}
