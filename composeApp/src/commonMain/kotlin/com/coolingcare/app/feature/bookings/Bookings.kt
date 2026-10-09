package com.coolingcare.app.feature.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.Messages
import com.coolingcare.app.domain.Planner
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.BookingStatus
import com.coolingcare.app.feature.status.messageShop
import com.coolingcare.app.ui.components.ServicePicture
import com.coolingcare.app.res.Res
import com.coolingcare.app.res.img_cat_split_ac
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.BtnStyle
import com.coolingcare.app.ui.components.HDivider
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.Img
import com.coolingcare.app.ui.components.ListRow
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.StatusBadge
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.TextBtn
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalLang


/** 50 / 51 / 52 · "Support & My Bookings" — one screen, two segments (the bottom nav picks the segment). */
@Composable
fun BookingsSupportTab(segment: Int, onSegment: (Int) -> Unit, onBooking: (String) -> Unit, onBookAgain: (Booking) -> Unit, onBook: () -> Unit, onSettings: () -> Unit, onArea: () -> Unit) {
    Screen(
        top = {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                T(S.supportAndBookings.s, type.titleL)
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CC.tile).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for ((i, label) in listOf(S.myBookings.s, S.support.s).withIndex()) {
                        val on = segment == i
                        Box(
                            Modifier.weight(1f).height(40.dp).then(if (on) Modifier.shadow(3.dp, RoundedCornerShape(11.dp)) else Modifier).clip(RoundedCornerShape(11.dp))
                                .background(if (on) CC.surface else CC.tile).tap { onSegment(i) },
                            contentAlignment = Alignment.Center,
                        ) { T(label, type.labelM, if (on) CC.primary else CC.text2) }
                    }
                }
            }
        },
        gap = 10.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
    ) {
        if (segment == 0) MyBookings(onBooking, onBookAgain, onBook) else Support(onSettings, onArea)
    }
}

@Composable
private fun MyBookings(onBooking: (String) -> Unit, onBookAgain: (Booking) -> Unit, onBook: () -> Unit) {
    val bookings by Graph.repo.bookings.collectAsState()
    val prefs by Graph.repo.prefs.collectAsState()
    val lang = LocalLang.current
    if (bookings.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(132.dp).clip(CircleShape).background(CC.tile), contentAlignment = Alignment.Center) { Img(Res.drawable.img_cat_split_ac, Modifier.size(104.dp, 68.dp), ContentScale.Fit) }
            T(S.noBookings.s, type.titleL, align = TextAlign.Center)
            T(S.noBookingsBody.s, type.bodyM, CC.text2, align = TextAlign.Center)
            Btn(S.bookService.s, onBook, arrow = true)
        }
        UrgentHelp()
        return
    }
    val upcoming = bookings.filter { it.status.active }.sortedBy { it.date + it.slotId }
    val done = bookings.filter { it.status == BookingStatus.COMPLETED }.sortedByDescending { it.completedAt ?: it.updatedAt }
    val cancelled = bookings.filter { it.status == BookingStatus.CANCELLED }.sortedByDescending { it.updatedAt }
    if (upcoming.isNotEmpty()) {
        T((if (upcoming.size > 1) S.upcomingMany else S.upcoming).s, type.titleS)
        upcoming.forEach { UpcomingCard(it) { onBooking(it.id) } }
    }
    if (done.isNotEmpty()) {
        T(S.completedServices.s, type.titleS)
        done.forEach { b ->
            Row(Modifier.fillMaxWidth().card(16.dp).tap { onBooking(b.id) }.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ServicePicture(b.serviceImage, b.serviceImagePath, Modifier.size(60.dp, 66.dp).clip(RoundedCornerShape(12.dp)))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    T(b.service.s, type.titleS); T(b.appliance.s, type.bodyS, CC.text2)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { Ic(CcIcons.Calendar, 14.dp, CC.text2); T(Dates.plain(Dates.parse(b.date)).s, type.caption) }
                    Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Btn(S.getInvoice.s, { messageShop(Messages.invoice(b, lang)) }, Modifier.weight(1f), BtnStyle.Secondary, small = true, height = 34.dp, px = 6.dp)
                        Btn(S.bookAgain.s, { onBookAgain(b) }, Modifier.weight(1f), small = true, height = 34.dp, px = 6.dp, flat = true)
                    }
                }
            }
        }
    }
    UrgentHelp()
    val due = Planner.maintenanceDue(bookings, prefs)
    if (due != null) Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CC.primaryTint).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(CcIcons.CalendarClock, CC.surface, CC.primary, 40.dp, 20.dp)
        Column(Modifier.weight(1f)) { T(S.maintenanceTitle.s, type.titleS); T(S.maintenanceBody.s, type.caption, CC.text2) }
        Btn(S.bookNow.s, { Graph.repo.bookings.value.firstOrNull { it.id == due.bookingId }?.let(onBookAgain) }, style = BtnStyle.Secondary, small = true, height = 34.dp, px = 12.dp)
    }
    if (cancelled.isNotEmpty()) {
        T(S.cancelledSection.s, type.titleS, CC.text2)
        cancelled.forEach { b ->
            ListRow(b.service.s, sub = Dates.plain(Dates.parse(b.date)).s + " · " + b.id, icon = CcIcons.CalendarX, iconBg = CC.surface2, iconTint = CC.text3, titleColor = CC.text2, chevron = true, onClick = { onBooking(b.id) })
        }
    }
}

@Composable
private fun UpcomingCard(b: Booking, onClick: () -> Unit) {
    val ap = Catalog.appliance(b.applianceId); val slot = Catalog.slot(b.slotId)
    val (label, bg, fg) = when (b.status) {
        BookingStatus.CONFIRMED -> Triple(S.stConfirmed, CC.greenTint, CC.greenText)
        BookingStatus.ON_THE_WAY -> Triple(S.stOnTheWay, CC.primaryTint, CC.primary)
        else -> Triple(S.stRequested, CC.warningTint, CC.warning)
    }
    Row(Modifier.fillMaxWidth().card(16.dp).tap(onClick = onClick).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (ap != null) Img(ap.image, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(CC.tile), ContentScale.Fit)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            T(b.service.s, type.titleS)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                T(b.appliance.s, type.bodyS, CC.text2)
                StatusBadge(label.s, bg, fg, if (b.status == BookingStatus.CONFIRMED) CcIcons.CircleCheck else null)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { Ic(CcIcons.Calendar, 14.dp, CC.text2); T(Dates.long(Dates.parse(b.date)).s + "  |  " + (slot?.label?.s ?: ""), type.caption, maxLines = 1) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { Ic(CcIcons.MapPin, 14.dp, CC.text2); T(b.address, type.caption, maxLines = 1) }
        }
        Ic(CcIcons.ChevronRight, 18.dp, CC.text3)
    }
}

@Composable
private fun UrgentHelp() {
    val lang = LocalLang.current
    val biz by Graph.cloud.business.collectAsState()
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(CC.orangeTint).border(1.dp, CC.orangeBorder, shape).padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Ic(CcIcons.TriangleAlert, 20.dp, CC.orange)
            Column { T(S.urgentHelp.s, type.titleS, CC.orangeText); T(S.urgentBody.s, type.bodyS, CC.text2) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn(S.callNow.s, { Graph.launcher.dial() }, Modifier.weight(1f), BtnStyle.Orange, lead = CcIcons.Phone, sub = biz.phoneDisplay, height = 52.dp, px = 10.dp, subLatin = true)
            Btn(S.whatsapp.s, { messageShop(Messages.support(null, lang)) }, Modifier.weight(1f), BtnStyle.Green, lead = CcIcons.Whatsapp, sub = S.chatWithUs.s, height = 52.dp, px = 10.dp)
        }
    }
}

@Composable
private fun Support(onSettings: () -> Unit, onArea: () -> Unit) {
    var open by rememberSaveable { mutableStateOf(0) }
    val biz by Graph.cloud.business.collectAsState()
    UrgentHelp()
    Column(Modifier.fillMaxWidth().card(16.dp, elevated = false).padding(horizontal = 14.dp, vertical = 4.dp)) {
        ListRow(S.workingHours.s, sub = biz.hours.s + "\n" + biz.hoursNote.s, icon = CcIcons.Clock)
        HDivider()
        ListRow(S.serviceAreaTitle.s, sub = S.serviceAreaSub.s, icon = CcIcons.MapPin, trailing = { TextBtn(S.change.s, onArea) }, onClick = onArea)
    }
    T(S.faqTitle.s, type.titleS)
    Column {
        Catalog.faqs.forEachIndexed { i, f ->
            Column(Modifier.fillMaxWidth().tap { open = if (open == i) -1 else i }.padding(vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { T(f.q.s, type.titleS, modifier = Modifier.weight(1f)); Ic(if (open == i) CcIcons.ChevronUp else CcIcons.ChevronDown, 18.dp, CC.text3) }
                if (open == i) T(f.a.s, type.bodyS, CC.text2)
            }
            HDivider()
        }
    }
    ListRow(S.settings.s, Modifier.clip(RoundedCornerShape(14.dp)).background(CC.bg2), sub = S.settingsSub.s, icon = CcIcons.Settings, chevron = true, onClick = onSettings, padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp))
}
