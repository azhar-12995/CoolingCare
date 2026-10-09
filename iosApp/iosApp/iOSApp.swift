import SwiftUI
import UIKit
import FirebaseCore
import FirebaseAuth
import FirebaseMessaging
import ComposeApp

/// Firebase + push setup. The shared Kotlin code (GitLive Firebase SDK) uses the same Firebase app.
final class AppDelegate: NSObject, UIApplicationDelegate, MessagingDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseApp.configure()
        // Creates the app graph, the notification delegate and syncs reminders. true = local Firebase Emulator Suite (simulator only).
        MainViewControllerKt.startApp(useEmulators: false)
        Messaging.messaging().delegate = self
        application.registerForRemoteNotifications()
        return true
    }

    // APNs token → Firebase Auth (silent phone verification) and FCM (order pushes).
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Auth.auth().setAPNSToken(deviceToken, type: .unknown)
        Messaging.messaging().apnsToken = deviceToken
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        if Auth.auth().canHandleNotification(userInfo) { completionHandler(.noData); return }
        completionHandler(.newData)
    }

    // Every new FCM token is saved on the signed-in account (users/{uid}.tokens).
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let token = fcmToken { MainViewControllerKt.onPushToken(token: token) }
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                // reCAPTCHA fallback of phone sign-in returns through the app's URL scheme.
                .onOpenURL { url in _ = Auth.auth().canHandle(url) }
        }
    }
}
