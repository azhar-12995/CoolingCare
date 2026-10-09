package com.coolingcare.app

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.coolingcare.app.data.Graph
import com.coolingcare.app.platform.AndroidApp
import com.coolingcare.app.platform.AndroidLauncher
import com.coolingcare.app.platform.AndroidPhotoFiles
import com.coolingcare.app.platform.AndroidReminderScheduler
import com.coolingcare.app.platform.Channels
import com.coolingcare.app.platform.DeepLinks
import com.coolingcare.app.platform.EXTRA_ROUTE
import com.coolingcare.app.platform.FirebaseEnv
import com.coolingcare.app.platform.PrefsStore
import com.google.firebase.auth.FirebaseAuth
import java.lang.ref.WeakReference

class CoolingCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidApp.ctx = this
        // Debug builds made with -Pcc.useEmulators=true talk to the local Firebase Emulator Suite (10.0.2.2 = the PC from the Android emulator).
        val env = FirebaseEnv(useEmulators = BuildConfig.DEBUG && BuildConfig.USE_EMULATORS, emulatorHost = "10.0.2.2")
        Graph.init(PrefsStore(this), AndroidLauncher(this), AndroidPhotoFiles(this), AndroidReminderScheduler(this), env)
        if (env.useEmulators) FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        Channels.ensure(this)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Dark status-bar icons on the light screens (the splash draws its own gradient behind them).
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(0, 0), navigationBarStyle = SystemBarStyle.light(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()))
        super.onCreate(savedInstanceState)
        AndroidApp.activity = WeakReference(this)
        if (savedInstanceState == null) handle(intent)
        setContent { App() }
    }

    override fun onResume() {
        super.onResume()
        AndroidApp.activity = WeakReference(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** Notification taps: our own reminders and FCM pushes both carry the in-app route as the "route" extra. */
    private fun handle(intent: Intent?) {
        intent?.getStringExtra(EXTRA_ROUTE)?.takeIf { it.isNotBlank() }?.let { DeepLinks.emit(it) }
    }
}
