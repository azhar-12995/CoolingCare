package com.coolingcare.app.domain

import com.coolingcare.app.data.Catalog
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.Lang
import com.coolingcare.app.model.Profile

/** Pakistani mobile numbers: 03XXXXXXXXX, 3XXXXXXXXX, 923XXXXXXXXX or +923XXXXXXXXX (spaces / dashes allowed). */
object Phone {
    /** Returns "03XXXXXXXXX" or null. */
    fun normalize(input: String): String? {
        val d = input.filter { it.isDigit() || it == '+' }.removePrefix("+")
        val local = when {
            d.length == 11 && d.startsWith("03") -> d
            d.length == 12 && d.startsWith("923") -> "0" + d.drop(2)
            d.length == 10 && d.startsWith("3") -> "0$d"
            else -> return null
        }
        return if (local.all { it.isDigit() }) local else null
    }

    fun isValid(input: String) = normalize(input) != null

    /** "0300 1234567" */
    fun display(input: String): String = normalize(input)?.let { it.take(4) + " " + it.drop(4) } ?: input.trim()

    /** For Urdu text: digits only, so a right-to-left paragraph cannot swap the two groups. */
    fun compact(input: String): String = normalize(input) ?: input.trim()

    fun forLang(input: String, lang: Lang) = if (lang == Lang.UR) compact(input) else display(input)

    /** "+923001234567" (what Firebase Phone Auth and the login token use) or null. */
    fun e164(input: String): String? = normalize(input)?.let { "+92" + it.drop(1) }

    /** "923001234567" for WhatsApp links. */
    fun waDigits(input: String): String? = normalize(input)?.let { "92" + it.drop(1) }
}

/** Wraps a left-to-right run (phone number, id) so it keeps its order inside Urdu text. */
fun ltr(s: String) = "\u2066$s\u2069"

/** Builds the full address line used in the booking and the WhatsApp message. */
fun Profile.addressLine(lang: Lang): String {
    val area = Catalog.areas.getOrNull(area)
    val near = landmark.trim().removePrefix("near ").removePrefix("Near ")
    val first = house.trim() + if (near.isBlank()) "" else if (lang == Lang.UR) " ($near کے قریب)" else " (near $near)"
    val parts = mutableListOf(first)
    if (area != null && !area.en.startsWith("Other")) parts += area.get(lang)
    val city = Catalog.business.city.get(lang)
    if (parts.none { it.contains(city, ignoreCase = true) }) parts += city
    return parts.filter { it.isNotBlank() }.joinToString(if (lang == Lang.UR) "، " else ", ")
}

/** Texts that open in WhatsApp (support chats, invoice requests, the shop writing to a customer). */
object Messages {
    fun invoice(b: Booking, lang: Lang): String {
        val s = b.service.get(lang); val d = Dates.plain(Dates.parse(b.date)).get(lang)
        return if (lang == Lang.UR) "براہِ کرم آرڈر ${b.id} ($s، $d) کی انوائس بھیج دیں۔" else "Please send the invoice for order ${b.id} ($s, $d)."
    }

    fun support(b: Booking?, lang: Lang) = when {
        b != null && lang == Lang.UR -> "السلام علیکم کولنگ کیئر، مجھے آرڈر ${b.id} کے بارے میں مدد چاہیے۔"
        b != null -> "Hi Cooling Care, I need help with order ${b.id}."
        lang == Lang.UR -> "السلام علیکم کولنگ کیئر، مجھے اے سی / فریج سروس کے بارے میں معلومات چاہییں۔"
        else -> "Hi Cooling Care, I'd like to ask about an AC / fridge service."
    }

    /** Admin → customer, in the language the customer booked in. */
    fun toCustomer(b: Booking): String = if (b.lang == "ur") "السلام علیکم ${b.name}، کولنگ کیئر سے بات ہو رہی ہے — آپ کے آرڈر ${b.id} (${b.service.ur}) کے بارے میں۔"
        else "Hello ${b.name}, this is Cooling Care about your order ${b.id} (${b.service.en})."
}

data class FormErrors(val date: Boolean = false, val slot: Boolean = false, val slotPassed: Boolean = false, val name: Boolean = false, val phone: Boolean = false, val address: Boolean = false) {
    val count get() = listOf(date, slot || slotPassed, name, phone, address).count { it }
    val ok get() = count == 0
}

object Validate {
    fun form(date: String?, slotId: String?, name: String, phone: String, house: String, nowMs: Long = Dates.nowMs()): FormErrors {
        val slot = slotId?.let { Catalog.slot(it) }
        val passed = date != null && slot != null && !Dates.slotOpen(Dates.parse(date), slot, nowMs)
        return FormErrors(
            date = date == null, slot = date != null && slot == null, slotPassed = passed,
            name = name.trim().length < 2, phone = !Phone.isValid(phone), address = house.trim().length < 3,
        )
    }
}
