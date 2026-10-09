package com.coolingcare.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.Event
import com.coolingcare.app.domain.EventKind
import com.coolingcare.app.domain.Planner
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.Lang
import com.coolingcare.app.res.Res
import com.coolingcare.app.res.img_hero_technician
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.EmptyIcon
import com.coolingcare.app.ui.components.HDivider
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconBtn
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.Img
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.RadioDot
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.SearchBox
import com.coolingcare.app.ui.components.SectionHeader
import com.coolingcare.app.ui.components.Sheet
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.TextBtn
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.isRtl
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalLang
import kotlinx.coroutines.launch

/** 10 · Home (tab content; the bottom nav is drawn by the tab host). */
@Composable
fun HomeTab(onBook: () -> Unit, onCategory: (applianceId: String) -> Unit, onNotifications: () -> Unit, onBooking: (String) -> Unit) {
    val prefs by Graph.repo.prefs.collectAsState()
    val profile by Graph.repo.profile.collectAsState()
    val bookings by Graph.repo.bookings.collectAsState()
    val biz by Graph.cloud.business.collectAsState()
    var areaSheet by remember { mutableStateOf(false) }
    val unread = Planner.inbox(bookings, prefs).any { it.at > prefs.notifSeenAt }
    val next = bookings.filter { it.status.active }.minByOrNull { it.date + it.slotId }
    val area = Catalog.areas.getOrNull(profile.area) ?: Catalog.areas.first()

    Screen(
        top = {
            Row(Modifier.fillMaxWidth().height(50.dp).padding(start = 20.dp, end = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.clip(RoundedCornerShape(10.dp)).tap { areaSheet = true }.padding(vertical = 6.dp, horizontal = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ic(CcIcons.MapPin, 19.dp); T(if (profile.area == 0 || area.en.startsWith("Other")) biz.city.s else area.s, type.titleS); Ic(CcIcons.ChevronDown, 17.dp, CC.text2)
                }
                Box(Modifier.weight(1f))
                IconBtn(CcIcons.Bell, onNotifications, badge = unread, description = S.notifications.s)
            }
        },
        gap = 14.dp, bodyPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(if (isRtl) 0.dp else 3.dp)) {
            val h = Dates.now().hour
            T((if (h < 12) S.goodMorning else if (h < 17) S.goodAfternoon else S.goodEvening).s, type.titleL)
            T(S.homeSub.s, type.bodyS, CC.text2)
        }
        HeroCard(onBook)
        if (next != null) NextVisit(next) { onBooking(next.id) }
        SectionHeader(S.ourServices.s, S.seeAll.s) { onCategory("") }
        val ap = Catalog.appliances
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (row in ap.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (a in row) Column(
                    Modifier.weight(1f).height(88.dp).clip(RoundedCornerShape(14.dp)).background(CC.tile).tap { onCategory(a.id) }.padding(horizontal = 4.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Img(a.image, Modifier.width(90.dp).height(46.dp), ContentScale.Fit)
                    T(a.name.s, type.labelS, align = TextAlign.Center, maxLines = 2)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            ContactCard(CcIcons.Phone, S.callNow.s, biz.phoneDisplay, true, Modifier.weight(1f)) { Graph.launcher.dial() }
            ContactCard(CcIcons.Whatsapp, S.whatsapp.s, S.chatWithUs.s, false, Modifier.weight(1f)) { Graph.launcher.whatsApp(com.coolingcare.app.domain.Messages.support(null, prefs.lang ?: Lang.EN)) }
        }
    }
    if (areaSheet) AreaSheet(profile.area, onDismiss = { areaSheet = false }) { a -> saveArea(a); areaSheet = false }
}

/** Saves the chosen area on the account (queued when offline). */
fun saveArea(area: Int) {
    val p = Graph.repo.profile.value
    if (p.area == area) return
    kotlinx.coroutines.MainScope().launch { runCatching { Graph.cloud.saveProfile(p.copy(area = area)) } }
}

@Composable
private fun HeroCard(onBook: () -> Unit) {
    val rtl = isRtl
    val fade = Color(0xFFE5F1FD)
    Box(Modifier.fillMaxWidth().height(238.dp).card(20.dp, stroke = null).background(CC.hero(rtl))) {
        Box(Modifier.align(Alignment.TopEnd).width(184.dp).height(188.dp)) {
            Img(Res.drawable.img_hero_technician, Modifier.fillMaxSize(), ContentScale.Crop, Alignment.CenterEnd)
            Box(Modifier.align(Alignment.CenterStart).width(70.dp).fillMaxHeightBox().background(Brush.horizontalGradient(if (rtl) listOf(Color.Transparent, fade) else listOf(fade, Color.Transparent))))
        }
        Column(Modifier.padding(start = 18.dp, top = 18.dp).width(170.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            T(S.heroTitle.s, type.displayM, CC.navy)
            T(S.heroSub.s, type.bodyS, CC.text2, Modifier.width(150.dp))
        }
        Btn(S.bookService.s, onBook, Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 14.dp).fillMaxWidth(), arrow = true, height = 46.dp)
    }
}

private fun Modifier.fillMaxHeightBox() = this.then(Modifier.height(188.dp))

@Composable
private fun NextVisit(b: Booking, onClick: () -> Unit) {
    val slot = Catalog.slot(b.slotId)
    Row(Modifier.fillMaxWidth().card(16.dp).tap(onClick = onClick).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(CcIcons.CalendarClock, size = 42.dp, iconSize = 21.dp)
        Column(Modifier.weight(1f)) {
            T(S.nextVisit.s, type.caption, CC.text3)
            T(b.service.s, type.titleS)
            T(Dates.short(Dates.parse(b.date)).s + " · " + (slot?.label?.s ?: ""), type.bodyS, CC.text2)
        }
        Ic(CcIcons.ChevronRight, 18.dp, CC.text3)
    }
}

@Composable
private fun ContactCard(icon: ImageVector, title: String, sub: String, subLatin: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(modifier.clip(shape).background(CC.greenTint).border(1.dp, CC.greenBorder, shape).tap(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, CC.green, CC.onPrimary, 38.dp, 20.dp, CircleShape)
        Column { T(title, type.titleS, CC.greenText, maxLines = 1); T(sub, type.caption, CC.text2, maxLines = 1, latin = subLatin) }
    }
}

/** 11 · Select Area sheet. */
@Composable
fun AreaSheet(selected: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var sel by remember { mutableStateOf(selected) }
    var q by remember { mutableStateOf("") }
    Sheet(S.selectArea.s, onDismiss) {
        T(S.areaSub.s, type.bodyS, CC.text2)
        SearchBox(q, { q = it }, S.searchArea.s)
        val lang = LocalLang.current
        Column {
            Catalog.areas.forEachIndexed { i, a ->
                if (q.isNotBlank() && !a.get(lang).contains(q.trim(), true) && !a.en.contains(q.trim(), true)) return@forEachIndexed
                Row(Modifier.fillMaxWidth().tap { sel = i }.padding(vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(if (i == Catalog.areas.lastIndex) CcIcons.MapPinned else CcIcons.MapPin, if (sel == i) CC.primaryTint else CC.surface2, if (sel == i) CC.primary else CC.text2, 34.dp, 17.dp, RoundedCornerShape(10.dp))
                    T(a.s, if (sel == i) type.titleS else type.bodyL, modifier = Modifier.weight(1f))
                    RadioDot(sel == i)
                }
                if (i < Catalog.areas.lastIndex) HDivider()
            }
        }
        Note(CcIcons.Phone, S.areaNotListed.s, Modifier.tap { Graph.launcher.dial() })
        Btn(S.confirmArea.s, { onConfirm(sel) }, Modifier.fillMaxWidth())
    }
}

/** 12 · Notifications — order updates (from the order history) and reminders. */
@Composable
fun NotificationsScreen(onBack: () -> Unit, onRoute: (String) -> Unit) {
    val prefs by Graph.repo.prefs.collectAsState()
    val bookings by Graph.repo.bookings.collectAsState()
    val seenAt = remember { prefs.notifSeenAt }
    val items = Planner.inbox(bookings, prefs)
    DisposableEffect(Unit) { onDispose { Graph.repo.markNotificationsSeen() } }
    var allRead by remember { mutableStateOf(false) }
    Screen(top = { AppBar(S.notifications.s, onBack, center = false) { if (items.isNotEmpty()) TextBtn(S.markAllRead.s, { allRead = true; Graph.repo.markNotificationsSeen() }) } }, gap = 12.dp) {
        if (items.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(top = 80.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EmptyIcon(CcIcons.Bell)
                T(S.noNotifications.s, type.titleL, align = TextAlign.Center)
                T(S.noNotificationsBody.s, type.bodyM, CC.text2, align = TextAlign.Center)
            }
            return@Screen
        }
        val startOfToday = Dates.at(Dates.today(), 0)
        val (today, earlier) = items.partition { it.at >= startOfToday }
        if (today.isNotEmpty()) { T(S.today.s.uppercase(), type.labelM, CC.text3); today.forEach { NotifItem(it, !allRead && it.at > seenAt) { onRoute(it.route) } } }
        if (earlier.isNotEmpty()) { T(S.earlier.s.uppercase(), type.labelM, CC.text3); earlier.forEach { NotifItem(it, !allRead && it.at > seenAt) { onRoute(it.route) } } }
    }
}

@Composable
private fun NotifItem(e: Event, unread: Boolean, onClick: () -> Unit) {
    val (icon, bg, fg) = when (e.kind) {
        EventKind.PLACED -> Triple(CcIcons.Send, CC.primaryTint, CC.primary)
        EventKind.CONFIRMED -> Triple(CcIcons.CalendarCheck, CC.greenTint, CC.green)
        EventKind.ON_THE_WAY -> Triple(CcIcons.Truck, CC.primaryTint, CC.primary)
        EventKind.NEW_TIME -> Triple(CcIcons.CalendarSync, CC.primaryTint, CC.primary)
        EventKind.DAY_BEFORE, EventKind.HOUR_BEFORE -> Triple(CcIcons.CalendarClock, CC.primaryTint, CC.primary)
        EventKind.COMPLETED -> Triple(CcIcons.CircleCheck, CC.greenTint, CC.green)
        EventKind.CANCELLED -> Triple(CcIcons.CalendarX, CC.dangerTint, CC.danger)
        EventKind.MAINTENANCE -> Triple(CcIcons.Snowflake, CC.warningTint, CC.warning)
    }
    Row(
        Modifier.fillMaxWidth().then(if (unread) Modifier.clip(RoundedCornerShape(16.dp)).background(CC.primaryTint) else Modifier.card(16.dp, elevated = false)).tap(onClick = onClick).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(icon, if (unread) CC.surface else bg, fg, 42.dp, 21.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(e.title.s, type.titleS, modifier = Modifier.weight(1f))
                if (unread) Box(Modifier.size(8.dp).clip(CircleShape).background(CC.primary))
            }
            T(e.body.s, type.bodyS, CC.text2)
            T(ago(e.at, LocalLang.current), type.caption, CC.text3)
        }
    }
}

fun ago(at: Long, lang: Lang): String {
    val mins = ((Dates.nowMs() - at) / 60_000).coerceAtLeast(0)
    return when {
        mins < 1 -> if (lang == Lang.UR) "ابھی" else "now"
        mins < 60 -> if (lang == Lang.UR) "$mins منٹ پہلے" else "$mins min ago"
        mins < 24 * 60 -> if (lang == Lang.UR) "${mins / 60} گھنٹے پہلے" else "${mins / 60} h ago"
        else -> Dates.dayMonth(Dates.dateOf(at)).get(lang)
    }
}
