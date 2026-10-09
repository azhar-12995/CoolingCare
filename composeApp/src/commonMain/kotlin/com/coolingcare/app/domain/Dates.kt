package com.coolingcare.app.domain

import com.coolingcare.app.data.Catalog
import com.coolingcare.app.model.Lang
import com.coolingcare.app.model.TimeSlot
import com.coolingcare.app.model.Txt
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** Time helpers. All business rules use the phone's local time zone (Pakistan). */
object Dates {
    var clock: Clock = Clock.System
    var zone: TimeZone = TimeZone.currentSystemDefault()

    fun nowMs(): Long = clock.now().toEpochMilliseconds()
    fun now(): LocalDateTime = clock.now().toLocalDateTime(zone)
    fun today(): LocalDate = now().date
    fun parse(iso: String): LocalDate = LocalDate.parse(iso)
    fun at(date: LocalDate, hour: Int, minute: Int = 0): Long = date.atTime(hour, minute).toInstant(zone).toEpochMilliseconds()
    fun dateOf(ms: Long): LocalDate = Instant.fromEpochMilliseconds(ms).toLocalDateTime(zone).date
    fun timeOf(ms: Long): LocalDateTime = Instant.fromEpochMilliseconds(ms).toLocalDateTime(zone)

    private val wdEn = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val wdUr = listOf("پیر", "منگل", "بدھ", "جمعرات", "جمعہ", "ہفتہ", "اتوار")
    private val wdLongEn = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val monEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val monLongEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    private val monUr = listOf("جنوری", "فروری", "مارچ", "اپریل", "مئی", "جون", "جولائی", "اگست", "ستمبر", "اکتوبر", "نومبر", "دسمبر")

    private fun idx(d: LocalDate) = d.dayOfWeek.ordinal // Monday = 0
    private fun dd(d: LocalDate) = d.day.toString().padStart(2, '0')
    private fun mi(d: LocalDate) = d.month.ordinal

    /** "Thu, 08 Oct 2026" / "جمعرات، 08 اکتوبر 2026" */
    fun long(d: LocalDate) = Txt("${wdEn[idx(d)]}, ${dd(d)} ${monEn[mi(d)]} ${d.year}", "${wdUr[idx(d)]}، ${dd(d)} ${monUr[mi(d)]} ${d.year}")
    /** "Thu, 08 Oct" */
    fun short(d: LocalDate) = Txt("${wdEn[idx(d)]}, ${dd(d)} ${monEn[mi(d)]}", "${wdUr[idx(d)]}، ${dd(d)} ${monUr[mi(d)]}")
    /** "12 Sep 2026" */
    fun plain(d: LocalDate) = Txt("${dd(d)} ${monEn[mi(d)]} ${d.year}", "${dd(d)} ${monUr[mi(d)]} ${d.year}")
    /** "12 Sep" */
    fun dayMonth(d: LocalDate) = Txt("${dd(d)} ${monEn[mi(d)]}", "${dd(d)} ${monUr[mi(d)]}")
    fun weekday(d: LocalDate) = Txt(wdEn[idx(d)], wdUr[idx(d)])
    fun weekdayLong(d: LocalDate) = Txt("${wdLongEn[idx(d)]}, ${d.day} ${monLongEn[mi(d)]}", "${wdUr[idx(d)]}، ${d.day} ${monUr[mi(d)]}")
    fun monthYear(d: LocalDate) = Txt("${monLongEn[mi(d)]} ${d.year}", "${monUr[mi(d)]} ${d.year}")
    fun clock(ms: Long, lang: Lang): String {
        val t = timeOf(ms); val h = if (t.hour % 12 == 0) 12 else t.hour % 12
        val m = t.minute.toString().padStart(2, '0')
        return if (lang == Lang.UR) "$h:$m ${if (t.hour < 12) "صبح" else "شام"}" else "$h:$m ${if (t.hour < 12) "AM" else "PM"}"
    }

    /** How many days ahead customers can book. */
    const val BOOKING_DAYS = 14

    fun isClosed(d: LocalDate) = d.dayOfWeek == DayOfWeek.SUNDAY

    /** A slot can be picked if it starts at least one hour from now. */
    fun slotOpen(d: LocalDate, slot: TimeSlot, nowMs: Long = nowMs()): Boolean = !isClosed(d) && at(d, slot.startHour) - nowMs >= 60 * 60_000L

    fun bookableDays(from: LocalDate = today()): List<LocalDate> = (0 until BOOKING_DAYS).map { from.plus(DatePeriod(days = it)) }

    fun dayOpen(d: LocalDate, nowMs: Long = nowMs()) = Catalog.timeSlots.any { slotOpen(d, it, nowMs) }

    /** First day + slot a new booking defaults to: the next open slot. */
    fun firstOpen(nowMs: Long = nowMs()): Pair<LocalDate, TimeSlot>? {
        for (d in bookableDays(dateOf(nowMs))) for (s in Catalog.timeSlots) if (slotOpen(d, s, nowMs)) return d to s
        return null
    }

    fun plusDays(d: LocalDate, n: Int) = d.plus(DatePeriod(days = n))
}
