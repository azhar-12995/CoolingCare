package com.coolingcare.app.navigation

import kotlinx.serialization.Serializable

@Serializable object SplashRoute
@Serializable data class LanguageRoute(val fromSettings: Boolean = false)
@Serializable object WelcomeRoute
@Serializable object LoginRoute
/** [local] = "03001234567". */
@Serializable data class VerifyRoute(val local: String, val verificationId: String)
@Serializable object YourDetailsRoute
/** Tabs: 0 Home · 1 Services · 2 Bookings · 3 Support. [filter] = services category ("all", "ac", "refrigeration", "commercial"). */
@Serializable data class MainRoute(val tab: Int = 0, val filter: String = "all")
@Serializable object NotificationsRoute
@Serializable data class ServiceRoute(val id: String)
@Serializable data class BookRoute(val serviceId: String? = null, val applianceId: String? = null, val token: Long = 0)
@Serializable object ReviewRoute
@Serializable data class OrderPlacedRoute(val id: String)
@Serializable data class StatusRoute(val id: String)
@Serializable object SettingsRoute
@Serializable object DetailsRoute
@Serializable object AboutRoute
@Serializable object PrivacyRoute
/** Admin tabs: 0 Dashboard · 1 Orders · 2 Services · 3 Settings. */
@Serializable data class AdminRoute(val tab: Int = 0)
@Serializable data class AdminOrderRoute(val id: String)
/** [id] null = new service. */
@Serializable data class AdminServiceRoute(val id: String? = null)
