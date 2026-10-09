package com.coolingcare.app

import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.CloudException
import com.coolingcare.app.data.Problem
import com.coolingcare.app.data.problem
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.EventKind
import com.coolingcare.app.domain.Messages
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.domain.Planner
import com.coolingcare.app.domain.Validate
import com.coolingcare.app.domain.addressLine
import com.coolingcare.app.i18n.S
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.BookingStatus
import com.coolingcare.app.model.HistoryEntry
import com.coolingcare.app.model.Lang
import com.coolingcare.app.model.Prefs
import com.coolingcare.app.model.Profile
import com.coolingcare.app.model.Txt
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class DomainTest {
    private val pk = TimeZone.of("Asia/Karachi")
    private class FixedClock(var ms: Long) : Clock { override fun now() = Instant.fromEpochMilliseconds(ms) }
    // Sat 03 Oct 2026, 09:30 in Pakistan
    private val clock = FixedClock(0)

    @BeforeTest fun setUp() { Dates.zone = pk; Dates.clock = clock; clock.ms = Dates.at(LocalDate(2026, 10, 3), 9, 30) }
    @AfterTest fun tearDown() { Dates.clock = Clock.System; Dates.zone = TimeZone.currentSystemDefault() }

    private fun booking(
        date: String = "2026-10-08", slot: String = "10-12", status: BookingStatus = BookingStatus.REQUESTED, created: Long = clock.ms,
        completedAt: Long? = null, id: String = "CC-1004", history: List<HistoryEntry>? = null,
    ) = Booking(
        id = id, uid = "u1", serviceId = "ac_deep", service = Txt("AC Deep Cleaning", "اے سی ڈیپ کلیننگ"), serviceImage = "svc_deep",
        applianceId = "split_ac", appliance = Txt("Split AC", "اسپلٹ اے سی"), date = date, slotId = slot, issue = "Not cooling",
        name = "Ali Raza", phone = "03001234567", address = "House No. 12, Main Street, Pasrur", status = status, createdAt = created, completedAt = completedAt,
        history = history ?: listOf(HistoryEntry(BookingStatus.REQUESTED, created, "customer", date = date, slotId = slot)),
    )

    @Test fun phoneNormalisation() {
        assertEquals("03001234567", Phone.normalize("0300 1234567"))
        assertEquals("03001234567", Phone.normalize("+92 300 1234567"))
        assertEquals("03001234567", Phone.normalize("923001234567"))
        assertEquals("03001234567", Phone.normalize("300-1234567"))
        assertNull(Phone.normalize("0300 12345"))
        assertNull(Phone.normalize("04212345678"))
        assertEquals("0300 1234567", Phone.display("+923001234567"))
    }

    @Test fun phoneFormatsForFirebaseAndWhatsApp() {
        assertEquals("+923001234567", Phone.e164("0300 1234567"))
        assertEquals("+923001234567", Phone.e164("300 1234567"))
        assertEquals("923001234567", Phone.waDigits("+92 300 1234567"))
        assertNull(Phone.e164("12345"))
    }

    @Test fun slotsNeedAnHourOfNoticeAndSundaysAreClosed() {
        val today = LocalDate(2026, 10, 3)
        assertFalse(Dates.slotOpen(today, Catalog.slot("10-12")!!)) // starts in 30 min
        assertTrue(Dates.slotOpen(today, Catalog.slot("12-14")!!))
        assertTrue(Dates.isClosed(LocalDate(2026, 10, 4)))
        assertFalse(Dates.dayOpen(LocalDate(2026, 10, 4)))
        val first = Dates.firstOpen()!!
        assertEquals(today, first.first); assertEquals("12-14", first.second.id)
        assertEquals(Dates.BOOKING_DAYS, Dates.bookableDays().size)
    }

    @Test fun formValidation() {
        val ok = Validate.form("2026-10-08", "10-12", "Ali Raza", "0300 1234567", "House 12")
        assertTrue(ok.ok)
        val bad = Validate.form(null, null, "A", "123", "")
        assertEquals(4, bad.count)
        assertTrue(Validate.form("2026-10-03", "10-12", "Ali", "03001234567", "House 12").slotPassed)
    }

    @Test fun dateFormatsInBothLanguages() {
        val d = LocalDate(2026, 10, 8)
        assertEquals("Thu, 08 Oct 2026", Dates.long(d).en)
        assertEquals("جمعرات، 08 اکتوبر 2026", Dates.long(d).ur)
    }

    @Test fun whatsAppTextsCarryTheOrderNumber() {
        val b = booking()
        assertTrue("CC-1004" in Messages.support(b, Lang.EN) && "CC-1004" in Messages.support(b, Lang.UR))
        assertTrue("AC Deep Cleaning" in Messages.invoice(b, Lang.EN))
        assertTrue("Ali Raza" in Messages.toCustomer(b) && "CC-1004" in Messages.toCustomer(b))
        assertTrue("اے سی ڈیپ کلیننگ" in Messages.toCustomer(b.copy(lang = "ur")))
    }

    @Test fun addressLineAddsAreaAndCity() {
        val p = Profile(house = "House No. 12, Main Street", landmark = "Jamia Masjid", area = 1)
        assertEquals("House No. 12, Main Street (near Jamia Masjid), Chawinda, Pasrur", p.addressLine(Lang.EN))
        assertEquals("House 5, Pasrur", Profile(house = "House 5", area = 6).addressLine(Lang.EN))
    }

    /** The order document must use the field names / values the Firestore rules and Cloud Functions expect. */
    @Test fun orderDocumentMatchesTheRules() {
        val json = Json { encodeDefaults = true }.encodeToJsonElement(Booking.serializer(), booking()).jsonObject
        assertEquals("CC-1004", json["code"]!!.jsonPrimitive.content)
        assertEquals("requested", json["status"]!!.jsonPrimitive.content)
        for (k in listOf("uid", "name", "phone", "address", "issue", "photos", "history", "service", "dateLabel", "slot", "date", "slotId")) assertTrue(k in json, "missing $k")
        val onTheWay = Json.encodeToJsonElement(BookingStatus.serializer(), BookingStatus.ON_THE_WAY).jsonPrimitive.content
        assertEquals("on_the_way", onTheWay)
    }

    @Test fun visitRemindersAreScheduledInTheFuture() {
        val ev = Planner.scheduled(listOf(booking()), Prefs())
        assertEquals(listOf(EventKind.DAY_BEFORE, EventKind.HOUR_BEFORE), ev.map { it.kind })
        assertEquals(Dates.at(LocalDate(2026, 10, 7), 19), ev[0].at)
        assertEquals(Dates.at(LocalDate(2026, 10, 8), 9), ev[1].at)
        assertTrue(Planner.scheduled(listOf(booking()), Prefs(visitReminders = false)).none { it.kind == EventKind.DAY_BEFORE })
        assertTrue(Planner.scheduled(listOf(booking(status = BookingStatus.CANCELLED)), Prefs()).isEmpty())
    }

    @Test fun rescheduledVisitGetsNewReminderIds() {
        val a = Planner.scheduled(listOf(booking()), Prefs()).map { it.notificationId }
        val b = Planner.scheduled(listOf(booking(date = "2026-10-09")), Prefs()).map { it.notificationId }
        assertTrue(a.none { it in b })
    }

    @Test fun completedVisitSchedulesNoVisitReminders() {
        val done = booking(status = BookingStatus.COMPLETED, completedAt = clock.ms)
        assertTrue(Planner.scheduled(listOf(done), Prefs()).none { it.kind == EventKind.DAY_BEFORE || it.kind == EventKind.HOUR_BEFORE })
        assertTrue(Planner.scheduled(listOf(done), Prefs()).any { it.kind == EventKind.MAINTENANCE })
    }

    @Test fun maintenanceReminderAfterNinetyDaysUntilRebooked() {
        val done = booking(date = "2026-07-01", status = BookingStatus.COMPLETED, created = Dates.at(LocalDate(2026, 6, 28), 10), completedAt = Dates.at(LocalDate(2026, 7, 1), 13))
        val due = Planner.maintenanceDue(listOf(done), Prefs())
        assertEquals(Dates.at(LocalDate(2026, 9, 29), 10), due?.at)
        val rebooked = booking(id = "CC-1111")
        assertNull(Planner.maintenanceDue(listOf(done, rebooked), Prefs()))
        assertNull(Planner.maintenanceDue(listOf(done), Prefs(dismissedMaintenance = listOf(done.id))))
        assertEquals("book/ac_deep", due?.route)
    }

    @Test fun inboxFollowsTheOrderHistory() {
        val placed = clock.ms - 3_600_000
        val confirmedAt = clock.ms - 600_000
        val b = booking(created = placed, status = BookingStatus.CONFIRMED, history = listOf(
            HistoryEntry(BookingStatus.REQUESTED, placed, "customer", date = "2026-10-08", slotId = "10-12"),
            HistoryEntry(BookingStatus.CONFIRMED, confirmedAt, "admin", date = "2026-10-08", slotId = "10-12"),
        ))
        val inbox = Planner.inbox(listOf(b), Prefs())
        assertEquals(listOf(EventKind.CONFIRMED, EventKind.PLACED), inbox.map { it.kind })
        assertTrue("CC-1004" in inbox.first().title.en)
        assertTrue("Thu, 08 Oct, 10 AM – 12 PM" in inbox.first().body.en)
        clock.ms = Dates.at(LocalDate(2026, 10, 8), 9, 15)
        assertEquals(listOf(EventKind.HOUR_BEFORE, EventKind.DAY_BEFORE, EventKind.CONFIRMED, EventKind.PLACED), Planner.inbox(listOf(b), Prefs()).map { it.kind })
        assertNotEquals(0, Planner.inbox(listOf(b), Prefs()).first().notificationId)
    }

    @Test fun shopCancellationShowsTheReason() {
        val b = booking(status = BookingStatus.CANCELLED, history = listOf(
            HistoryEntry(BookingStatus.REQUESTED, clock.ms - 7_200_000, "customer"),
            HistoryEntry(BookingStatus.CANCELLED, clock.ms - 60_000, "admin", note = "No technician that day"),
        ))
        val ev = Planner.inbox(listOf(b), Prefs()).first()
        assertEquals(EventKind.CANCELLED, ev.kind)
        assertTrue("No technician that day" in ev.body.en)
        assertEquals("Not needed anymore", S.reason("not_needed").en)
    }

    @Test fun problemsAreClassified() {
        assertEquals(Problem.SERVICE_CLOSED, CloudException(Problem.SERVICE_CLOSED).problem())
        assertEquals(Problem.OFFLINE, IllegalStateException("Failed to get document because the client is offline.").problem())
        assertEquals(Problem.INVALID_NUMBER, Exception("INVALID_PHONE_NUMBER").problem())
        assertEquals(Problem.OTHER, Exception("boom").problem())
    }

    @Test fun catalogIsConsistent() {
        assertEquals(10, Catalog.services.size)
        for (s in Catalog.services) {
            assertTrue(s.category in Catalog.categories.map { it.id }); assertTrue(s.included.isNotEmpty())
            assertTrue(s.image in Catalog.images, "picture ${s.image}"); assertTrue(s.header in Catalog.images, "header ${s.header}")
        }
        for (a in Catalog.appliances) assertTrue(Catalog.services.any { it.category == a.category })
        assertTrue(Catalog.business.whatsapp.all { it.isDigit() })
    }
}
