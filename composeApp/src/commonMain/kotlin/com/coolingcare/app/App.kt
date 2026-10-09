package com.coolingcare.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.data.Live
import com.coolingcare.app.data.Session
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.feature.admin.AdminHost
import com.coolingcare.app.feature.admin.AdminOrderScreen
import com.coolingcare.app.feature.admin.AdminServiceScreen
import com.coolingcare.app.feature.auth.LoginScreen
import com.coolingcare.app.feature.auth.VerifyScreen
import com.coolingcare.app.feature.auth.YourDetailsScreen
import com.coolingcare.app.feature.book.BookScreen
import com.coolingcare.app.feature.book.OrderPlacedScreen
import com.coolingcare.app.feature.book.ReviewScreen
import com.coolingcare.app.feature.bookings.BookingsSupportTab
import com.coolingcare.app.feature.home.AreaSheet
import com.coolingcare.app.feature.home.HomeTab
import com.coolingcare.app.feature.home.NotificationsScreen
import com.coolingcare.app.feature.home.saveArea
import com.coolingcare.app.feature.services.ServiceDetailScreen
import com.coolingcare.app.feature.services.ServicesTab
import com.coolingcare.app.feature.settings.AboutScreen
import com.coolingcare.app.feature.settings.DetailsScreen
import com.coolingcare.app.feature.settings.PrivacyScreen
import com.coolingcare.app.feature.settings.SettingsScreen
import com.coolingcare.app.feature.start.LanguageScreen
import com.coolingcare.app.feature.start.SplashScreen
import com.coolingcare.app.feature.start.WelcomeScreen
import com.coolingcare.app.feature.status.StatusScreen
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.Lang
import com.coolingcare.app.navigation.AboutRoute
import com.coolingcare.app.navigation.AdminOrderRoute
import com.coolingcare.app.navigation.AdminRoute
import com.coolingcare.app.navigation.AdminServiceRoute
import com.coolingcare.app.navigation.BookRoute
import com.coolingcare.app.navigation.DetailsRoute
import com.coolingcare.app.navigation.LanguageRoute
import com.coolingcare.app.navigation.LoginRoute
import com.coolingcare.app.navigation.MainRoute
import com.coolingcare.app.navigation.NotificationsRoute
import com.coolingcare.app.navigation.OrderPlacedRoute
import com.coolingcare.app.navigation.PrivacyRoute
import com.coolingcare.app.navigation.ReviewRoute
import com.coolingcare.app.navigation.ServiceRoute
import com.coolingcare.app.navigation.SettingsRoute
import com.coolingcare.app.navigation.SplashRoute
import com.coolingcare.app.navigation.StatusRoute
import com.coolingcare.app.navigation.VerifyRoute
import com.coolingcare.app.navigation.WelcomeRoute
import com.coolingcare.app.navigation.YourDetailsRoute
import com.coolingcare.app.platform.DeepLinks
import com.coolingcare.app.platform.SystemBackHandler
import com.coolingcare.app.ui.components.BottomNav
import com.coolingcare.app.ui.components.Tab
import com.coolingcare.app.ui.components.ToastHost
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.CcTheme

@Composable
fun App() {
    val prefs by Graph.repo.prefs.collectAsState()
    val session by Graph.cloud.session.collectAsState()
    val ready by Graph.cloud.ready.collectAsState()
    CcTheme(prefs.lang ?: Lang.EN) {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val dest = entry?.destination
        var started by rememberSaveable { mutableStateOf(false) }
        val inAuth = dest == null || dest.hasRoute<SplashRoute>() || dest.hasRoute<LanguageRoute>() || dest.hasRoute<WelcomeRoute>() ||
            dest.hasRoute<LoginRoute>() || dest.hasRoute<VerifyRoute>() || dest.hasRoute<YourDetailsRoute>() || dest.hasRoute<PrivacyRoute>()

        // Pushes arrive in the customer's language: keep users/{uid}.lang in step with the app.
        LaunchedEffect(prefs.lang, session) { prefs.lang?.let { Graph.cloud.syncLanguage(it) } }
        // Logged out inside the app (log out, account deleted, login revoked) → back to Log in.
        LaunchedEffect(session, started) {
            if (started && session is Session.SignedOut && !inAuth) nav.navigate(LoginRoute) { popUpTo(nav.graph.id) { inclusive = true } }
        }
        // Notification taps: only once signed in and past the start screens, otherwise the jump would be undone.
        val linksOk = started && session is Session.SignedIn && ready && !inAuth
        LaunchedEffect(linksOk) {
            if (!linksOk) return@LaunchedEffect
            DeepLinks.links.collect { route -> openRoute(nav, route); DeepLinks.consumed() }
        }

        Box(Modifier.fillMaxSize().background(CC.bg)) {
            NavHost(
                nav, startDestination = SplashRoute, modifier = Modifier.fillMaxSize(),
                enterTransition = { slideInHorizontally(tween(260)) { it / 6 } + fadeIn(tween(260)) },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { slideOutHorizontally(tween(240)) { it / 6 } + fadeOut(tween(200)) },
            ) {
                composable<SplashRoute>(enterTransition = { EnterTransition.None }, exitTransition = { fadeOut(tween(300)) }) {
                    SplashScreen { goHome(nav); started = true }
                }
                composable<LanguageRoute> { e ->
                    val r = e.toRoute<LanguageRoute>()
                    LanguageScreen { if (r.fromSettings) nav.popBackStack() else nav.navigate(WelcomeRoute) { popUpTo<LanguageRoute> { inclusive = true } } }
                }
                composable<WelcomeRoute> { WelcomeScreen { Graph.repo.updatePrefs { it.copy(onboarded = true) }; goHome(nav) } }

                // ---------- log in (04 – 06)
                composable<LoginRoute> {
                    LoginScreen(
                        onBack = if (nav.previousBackStackEntry != null) ({ nav.popBackStack() }) else null,
                        onCodeSent = { local, id -> nav.navigate(VerifyRoute(local, id)) },
                        onSignedIn = { goHome(nav) },
                        onPrivacy = { nav.navigate(PrivacyRoute) },
                    )
                }
                composable<VerifyRoute> { e ->
                    val r = e.toRoute<VerifyRoute>()
                    VerifyScreen(r.local, r.verificationId, onChangeNumber = { nav.popBackStack() }, onVerified = { goHome(nav) })
                }
                composable<YourDetailsRoute> { YourDetailsScreen { goHome(nav) } }

                // ---------- customer
                composable<MainRoute>(enterTransition = { fadeIn(tween(250)) }) { e -> MainHost(nav, e.toRoute()) }
                composable<NotificationsRoute> { NotificationsScreen({ nav.popBackStack() }) { openRoute(nav, it) } }
                composable<ServiceRoute> { e ->
                    val id = e.toRoute<ServiceRoute>().id
                    ServiceDetailScreen(id, { nav.popBackStack() }) { startBooking(nav, id) }
                }
                composable<BookRoute> { BookScreen({ nav.popBackStack() }) { nav.navigate(ReviewRoute) } }
                composable<ReviewRoute> {
                    ReviewScreen({ nav.popBackStack() }) { id -> nav.navigate(OrderPlacedRoute(id)) { popUpTo<MainRoute> { inclusive = false } } }
                }
                composable<OrderPlacedRoute> { e ->
                    val id = e.toRoute<OrderPlacedRoute>().id
                    OrderPlacedScreen(id, onTrack = { nav.navigate(StatusRoute(id)) { popUpTo<OrderPlacedRoute> { inclusive = true } } }, onHome = { nav.popBackStack() })
                }
                composable<StatusRoute> { e ->
                    StatusScreen(e.toRoute<StatusRoute>().id, { if (!nav.popBackStack()) goHome(nav) }) { b -> rebook(nav, b) }
                }
                composable<SettingsRoute> {
                    SettingsScreen({ nav.popBackStack() }, { nav.navigate(DetailsRoute) }, { nav.navigate(LanguageRoute(fromSettings = true)) }, { nav.navigate(AboutRoute) }, { nav.navigate(PrivacyRoute) }) {
                        Graph.repo.updatePrefs { it.copy(customerView = false) }
                        nav.navigate(AdminRoute()) { popUpTo(nav.graph.id) { inclusive = true } }
                    }
                }
                composable<DetailsRoute> { DetailsScreen { nav.popBackStack() } }
                composable<AboutRoute> { AboutScreen { nav.popBackStack() } }
                composable<PrivacyRoute> { PrivacyScreen { nav.popBackStack() } }

                // ---------- admin (70 – 76)
                composable<AdminRoute>(enterTransition = { fadeIn(tween(250)) }) { e ->
                    AdminHost(
                        e.toRoute<AdminRoute>().tab,
                        onOrder = { nav.navigate(AdminOrderRoute(it)) },
                        onService = { nav.navigate(AdminServiceRoute(it)) },
                        onLanguage = { nav.navigate(LanguageRoute(fromSettings = true)) },
                        onCustomerView = {
                            Graph.repo.updatePrefs { it.copy(customerView = true) }
                            nav.navigate(MainRoute()) { popUpTo(nav.graph.id) { inclusive = true } }
                        },
                    )
                }
                composable<AdminOrderRoute> { e -> AdminOrderScreen(e.toRoute<AdminOrderRoute>().id) { if (!nav.popBackStack()) goHome(nav) } }
                composable<AdminServiceRoute> { e -> AdminServiceScreen(e.toRoute<AdminServiceRoute>().id) { nav.popBackStack() } }
            }
            ToastHost()
        }
    }
}

/** Where the app starts for the current state: language → welcome → log in → (details) → customer home or admin dashboard. */
private fun homeRoute(): Any {
    val c = Graph.cloud; val p = Graph.repo.prefs.value
    return when {
        p.lang == null -> LanguageRoute()
        !p.onboarded -> WelcomeRoute
        c.session.value !is Session.SignedIn -> LoginRoute
        // Admins skip "where should the technician go" — they only need an account record for the push token.
        c.isAdmin.value && !p.customerView -> { c.ensureAdminProfile(); AdminRoute() }
        c.profile.value is Live.Missing -> YourDetailsRoute
        else -> MainRoute()
    }
}

private fun goHome(nav: NavHostController) = nav.navigate(homeRoute()) { popUpTo(nav.graph.id) { inclusive = true } }

/** Tab host: Home · Services · Bookings · Support (Bookings & Support are two segments of one design screen). */
@Composable
private fun MainHost(nav: NavHostController, route: MainRoute) {
    var tab by rememberSaveable(route.tab) { mutableIntStateOf(route.tab) }
    var filter by rememberSaveable(route.filter) { mutableStateOf(route.filter) }
    var areaSheet by rememberSaveable { mutableStateOf(false) }
    // System back on another tab returns to Home first instead of closing the app.
    SystemBackHandler(enabled = tab != 0) { tab = 0 }
    Column(Modifier.fillMaxSize().background(CC.bg)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> HomeTab(
                    onBook = { startBooking(nav, null) },
                    onCategory = { ap ->
                        when (ap) {
                            "" -> { filter = "all"; tab = 1 }
                            "installation" -> nav.navigate(ServiceRoute("ac_install"))
                            else -> { filter = Catalog.appliance(ap)?.category ?: "all"; tab = 1 }
                        }
                    },
                    onNotifications = { nav.navigate(NotificationsRoute) },
                    onBooking = { nav.navigate(StatusRoute(it)) },
                )
                1 -> ServicesTab(filter, { filter = it }) { nav.navigate(ServiceRoute(it)) }
                else -> BookingsSupportTab(
                    segment = if (tab == 3) 1 else 0, onSegment = { tab = if (it == 1) 3 else 2 },
                    onBooking = { nav.navigate(StatusRoute(it)) }, onBookAgain = { rebook(nav, it) }, onBook = { startBooking(nav, null) },
                    onSettings = { nav.navigate(SettingsRoute) }, onArea = { areaSheet = true },
                )
            }
        }
        BottomNav(Tab.entries[tab]) { tab = it.ordinal }
    }
    if (areaSheet) AreaSheet(Graph.repo.profile.value.area, { areaSheet = false }) { saveArea(it); areaSheet = false }
}

private fun startBooking(nav: NavHostController, serviceId: String?, applianceId: String? = null) {
    val repo = Graph.repo
    Graph.draft.start(repo.profile.value, repo.prefs.value.lang ?: Lang.EN, serviceId, applianceId)
    nav.navigate(BookRoute(serviceId, applianceId, Dates.nowMs()))
}

private fun rebook(nav: NavHostController, b: Booking) {
    startBooking(nav, b.serviceId, b.applianceId)
    Graph.draft.address = b.address.ifBlank { Graph.draft.address }
}

/** Notification / deep-link routes: "booking/<id>", "admin/order/<id>", "book/<serviceId>". */
private fun openRoute(nav: NavHostController, route: String) {
    val parts = route.split("/")
    when (parts.firstOrNull()) {
        "booking", "update" -> parts.getOrNull(1)?.takeIf { it.isNotBlank() }?.let { nav.navigate(StatusRoute(it)) }
        "admin" -> parts.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { if (Graph.cloud.isAdmin.value) nav.navigate(AdminOrderRoute(it)) else nav.navigate(StatusRoute(it)) }
        "book" -> startBooking(nav, parts.getOrNull(1)?.ifBlank { null })
    }
}
