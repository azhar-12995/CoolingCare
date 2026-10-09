package com.coolingcare.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource

@Serializable
enum class Lang { EN, UR;
    /** "en" / "ur" — stored on the user so pushes arrive in the right language. */
    val code get() = name.lowercase()
}

/** A piece of copy in both languages. Use `.s` in composables, `.get(lang)` elsewhere. Stored in Firestore as {en, ur}. */
@Serializable
data class Txt(val en: String = "", val ur: String = "") {
    fun get(lang: Lang) = if (lang == Lang.UR) ur.ifBlank { en } else en.ifBlank { ur }
    val isBlank get() = en.isBlank() && ur.isBlank()
}

/** Shop contact details. Firestore config/business (editable by admins); [com.coolingcare.app.data.Catalog.business] is the offline default. */
@Serializable
data class Business(
    val name: String = "Cooling Care",
    val tagline: Txt = Txt(),
    val city: Txt = Txt(),
    /** "0300 1234567" */
    val phoneDisplay: String = "",
    /** E.164, used for tel: and SMS. */
    val phoneDial: String = "",
    /** Digits only (country code, no +), used for wa.me. */
    val whatsapp: String = "",
    val hours: Txt = Txt(),
    val hoursNote: Txt = Txt(),
    val updatedAt: Long = 0,
)

data class Category(val id: String, val name: Txt)
data class Appliance(val id: String, val name: Txt, val image: DrawableResource, val category: String)

/** A bookable service. Firestore services/{id}; customers only see `active` ones. */
@Serializable
data class Service(
    @Transient val id: String = "",
    val category: String = "ac",
    val sort: Int = 0,
    val active: Boolean = true,
    val title: Txt = Txt(),
    /** Shorter title for the services list (falls back to [title]). */
    val listTitle: Txt? = null,
    val short: Txt = Txt(),
    val about: Txt = Txt(),
    val included: List<Txt> = emptyList(),
    val duration: Txt = Txt(),
    /** Bundled picture key (svc_deep, big_fridge …) — see Catalog.images. */
    val image: String = "",
    val header: String = "",
    /** Storage path of a photo uploaded by an admin; shown instead of [image]. */
    val imagePath: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    val list get() = listTitle?.takeUnless { it.isBlank } ?: title
}

data class TimeSlot(val id: String, val startHour: Int, val endHour: Int, val label: Txt)
data class Faq(val q: Txt, val a: Txt)

@Serializable
enum class BookingStatus {
    @SerialName("requested") REQUESTED,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("on_the_way") ON_THE_WAY,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED;

    val active get() = this == REQUESTED || this == CONFIRMED || this == ON_THE_WAY
    /** Customers may cancel or ask for another time until the technician leaves (enforced by the Firestore rules). */
    val customerCanChange get() = this == REQUESTED || this == CONFIRMED
}

/** One step in an order's life. [by] = "customer" or "admin" (the Cloud Function uses it to decide who gets the push). */
@Serializable
data class HistoryEntry(
    val status: BookingStatus,
    val at: Long,
    val by: String,
    val note: String = "",
    /** Set when the visit time changed in this step. */
    val date: String = "",
    val slotId: String = "",
)

/** An order (Firestore orders/{code}). The document id is the order number, e.g. CC-1004. */
@Serializable
data class Booking(
    @SerialName("code") val id: String,
    val uid: String = "",
    val serviceId: String,
    val service: Txt,
    val serviceImage: String = "",
    val serviceImagePath: String = "",
    val applianceId: String,
    val appliance: Txt,
    /** ISO date, e.g. 2026-10-08. */
    val date: String,
    /** "Thu, 08 Oct" in both languages — used by the push texts. */
    val dateLabel: Txt = Txt(),
    val slotId: String,
    val slot: Txt = Txt(),
    val issue: String = "",
    /** Storage paths: orders/{uid}/{folder}/{n}.jpg */
    val photos: List<String> = emptyList(),
    val name: String,
    val phone: String,
    val address: String,
    val status: BookingStatus = BookingStatus.REQUESTED,
    val history: List<HistoryEntry> = emptyList(),
    /** Language the customer booked in. */
    val lang: String = "en",
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val completedAt: Long? = null,
    val rating: Int? = null,
    val feedback: String = "",
    /** Reason code (not_needed, other_time, fixed, other) or the shop's own words. */
    val cancelReason: String = "",
    /** Internal note for the technician (admin only). */
    val adminNote: String = "",
    /** Random id of the form that created the order: a retry after a lost answer finds the order instead of placing it twice. */
    val clientRef: String = "",
)

/** users/{uid}. [phone] is the verified login number in E.164 (+923001234567); the rules require it. */
@Serializable
data class Profile(
    val phone: String = "",
    val name: String = "",
    /** Index into Catalog.areas. */
    val area: Int = 0,
    val house: String = "",
    val landmark: String = "",
    val lang: String = "en",
    /** FCM tokens of this user's phones (max 10). */
    val tokens: List<String> = emptyList(),
    /** Admins only: get a push for every new order. */
    val adminAlerts: Boolean = true,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    val complete get() = name.isNotBlank() && house.isNotBlank()
}

@Serializable
data class Prefs(
    /** null until the user picks a language on first launch. */
    val lang: Lang? = null,
    val onboarded: Boolean = false,
    val visitReminders: Boolean = true,
    val maintenanceReminders: Boolean = true,
    /** Notifications older than this are shown as read. */
    val notifSeenAt: Long = 0,
    /** Maintenance reminders the user already dismissed (booking ids). */
    val dismissedMaintenance: List<String> = emptyList(),
    /** Notification permission is asked once, after the first order. */
    val askedNotifications: Boolean = false,
    /** Admin chose "Switch to customer view". */
    val customerView: Boolean = false,
)
