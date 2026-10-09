package com.coolingcare.app.domain

import com.coolingcare.app.data.Catalog
import com.coolingcare.app.i18n.S
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.BookingStatus
import com.coolingcare.app.model.Prefs
import com.coolingcare.app.model.Txt

enum class EventKind { PLACED, CONFIRMED, ON_THE_WAY, NEW_TIME, COMPLETED, CANCELLED, DAY_BEFORE, HOUR_BEFORE, MAINTENANCE }

/**
 * One inbox entry / reminder. Order updates come from the order's history (the shop's changes also arrive as pushes);
 * reminders in the future are scheduled with the OS so they fire with the app closed.
 * Route: "booking/<id>" or "book/<serviceId>".
 */
data class Event(val key: String, val kind: EventKind, val bookingId: String, val at: Long, val title: Txt, val body: Txt, val route: String) {
    /** Stable small int for OS notification / alarm ids. */
    val notificationId get() = key.hashCode() and 0x7fffffff
}

object Planner {
    const val MAINTENANCE_DAYS = 90
    const val DAY_BEFORE_HOUR = 19

    fun maintenanceAt(completedAt: Long): Long = Dates.at(Dates.plusDays(Dates.dateOf(completedAt), MAINTENANCE_DAYS), 10)

    /** A completed booking stops nagging once a newer booking exists for the same appliance. */
    private fun superseded(b: Booking, all: List<Booking>) =
        all.any { it.id != b.id && it.applianceId == b.applianceId && it.createdAt > (b.completedAt ?: b.updatedAt) && it.status != BookingStatus.CANCELLED }

    private fun whenText(date: String, slotId: String): Txt? {
        val slot = Catalog.slot(slotId) ?: return null
        val d = runCatching { Dates.short(Dates.parse(date)) }.getOrNull() ?: return null
        return Txt("${d.en}, ${slot.label.en}", "${d.ur}، ${slot.label.ur}")
    }

    fun all(bookings: List<Booking>, prefs: Prefs): List<Event> = buildList {
        for (b in bookings) {
            val svc = b.service
            val slot = Catalog.slot(b.slotId) ?: continue
            val day = runCatching { Dates.parse(b.date) }.getOrNull() ?: continue
            val route = "booking/${b.id}"
            b.history.forEachIndexed { i, h ->
                val key = "${b.id}.h$i"
                val at = whenText(h.date.ifBlank { b.date }, h.slotId.ifBlank { b.slotId }) ?: Txt()
                when {
                    i == 0 -> add(Event(key, EventKind.PLACED, b.id, h.at, S.nPlaced(b.id), S.nPlacedBody(svc), route))
                    h.status == BookingStatus.CONFIRMED -> add(Event(key, EventKind.CONFIRMED, b.id, h.at, S.nConfirmed(b.id), S.nConfirmedBody(svc, at), route))
                    h.status == BookingStatus.ON_THE_WAY -> add(Event(key, EventKind.ON_THE_WAY, b.id, h.at, S.nOnTheWay, S.nOnTheWayBody(svc), route))
                    h.status == BookingStatus.COMPLETED -> add(Event(key, EventKind.COMPLETED, b.id, h.at, S.nCompleted, S.nCompletedBody(svc), route))
                    h.status == BookingStatus.CANCELLED -> add(Event(key, EventKind.CANCELLED, b.id, h.at, S.nCancelled,
                        if (h.by == "admin") S.nCancelledByShop(b.id, h.note) else S.nCancelledBody(b.id), route))
                    h.status == BookingStatus.REQUESTED && h.by == "customer" -> add(Event(key, EventKind.NEW_TIME, b.id, h.at, S.nNewTimeRequested, S.nNewTimeRequestedBody(at), route))
                }
            }
            if ((b.status.active || b.status == BookingStatus.COMPLETED) && prefs.visitReminders) {
                // A completed visit keeps the reminders it already fired (inbox history) but schedules nothing new.
                val until = if (b.status == BookingStatus.COMPLETED) (b.completedAt ?: b.updatedAt) else Long.MAX_VALUE
                val dayBefore = Dates.at(Dates.plusDays(day, -1), DAY_BEFORE_HOUR)
                if (dayBefore > b.createdAt && dayBefore < until) add(Event("${b.id}.d1.${b.date}.${b.slotId}", EventKind.DAY_BEFORE, b.id, dayBefore, S.nVisitTomorrow(slot.label), S.nVisitBody(svc), route))
                val hour = Dates.at(day, slot.startHour) - 60 * 60_000L
                if (hour > b.createdAt && hour < until) add(Event("${b.id}.h1.${b.date}.${b.slotId}", EventKind.HOUR_BEFORE, b.id, hour, S.nVisitToday(slot.label), S.nVisitBody(svc), route))
            }
            if (b.status == BookingStatus.COMPLETED && b.completedAt != null && prefs.maintenanceReminders && !superseded(b, bookings) && b.id !in prefs.dismissedMaintenance)
                add(Event("${b.id}.mnt", EventKind.MAINTENANCE, b.id, maintenanceAt(b.completedAt), S.nMaintenance, S.nMaintenanceBody(svc), "book/${b.serviceId}"))
        }
    }

    private val reminderKinds = setOf(EventKind.DAY_BEFORE, EventKind.HOUR_BEFORE, EventKind.MAINTENANCE)

    /** Reminders the OS should fire later. */
    fun scheduled(bookings: List<Booking>, prefs: Prefs, now: Long = Dates.nowMs()): List<Event> =
        all(bookings, prefs).filter { it.at > now && it.kind in reminderKinds }.sortedBy { it.at }

    /** In-app Notifications list: everything that already happened, newest first. */
    fun inbox(bookings: List<Booking>, prefs: Prefs, now: Long = Dates.nowMs()): List<Event> =
        all(bookings, prefs).filter { it.at <= now }.sortedByDescending { it.at }

    fun maintenanceDue(bookings: List<Booking>, prefs: Prefs, now: Long = Dates.nowMs()): Event? =
        all(bookings, prefs).filter { it.kind == EventKind.MAINTENANCE && it.at <= now }.maxByOrNull { it.at }

    fun nextMaintenance(bookings: List<Booking>, prefs: Prefs, now: Long = Dates.nowMs()): Event? =
        all(bookings, prefs).filter { it.kind == EventKind.MAINTENANCE && it.at > now }.minByOrNull { it.at }
}
