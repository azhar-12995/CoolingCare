package com.coolingcare.app

import androidx.compose.ui.window.ComposeUIViewController
import com.coolingcare.app.data.Graph
import com.coolingcare.app.platform.DefaultsStore
import com.coolingcare.app.platform.FirebaseEnv
import com.coolingcare.app.platform.IosLauncher
import com.coolingcare.app.platform.IosPhotoFiles
import com.coolingcare.app.platform.IosReminderScheduler
import com.coolingcare.app.platform.NotificationDelegate
import platform.UIKit.UIViewController
import platform.UserNotifications.UNUserNotificationCenter

private var delegate: NotificationDelegate? = null

/**
 * Called once from Swift (`MainViewControllerKt.startApp(useEmulators:)`) after `FirebaseApp.configure()`.
 * [useEmulators] = talk to the Firebase Emulator Suite on this Mac (simulator only).
 */
fun startApp(useEmulators: Boolean = false) {
    if (Graph.ready) return
    Graph.init(DefaultsStore(), IosLauncher(), IosPhotoFiles(), IosReminderScheduler(), FirebaseEnv(useEmulators, "127.0.0.1"))
    delegate = NotificationDelegate().also { UNUserNotificationCenter.currentNotificationCenter().delegate = it }
}

/** Swift's MessagingDelegate hands every new FCM token to the account. */
fun onPushToken(token: String) { if (Graph.ready) Graph.cloud.onNewToken(token) }

fun MainViewController(): UIViewController {
    startApp()
    return ComposeUIViewController { App() }
}
