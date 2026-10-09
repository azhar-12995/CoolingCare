package com.coolingcare.app.feature.status

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.data.Live
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.Messages
import com.coolingcare.app.domain.Planner
import com.coolingcare.app.feature.book.DateTimeSheet
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.BookingStatus
import com.coolingcare.app.platform.Sent
import com.coolingcare.app.platform.rememberNotificationPermission
import com.coolingcare.app.res.Res
import com.coolingcare.app.res.img_tech_portrait
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.BtnStyle
import com.coolingcare.app.ui.components.CcDialog
import com.coolingcare.app.ui.components.CheckCircle
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.HDivider
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconBtn
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.Img
import com.coolingcare.app.ui.components.InfoLine
import com.coolingcare.app.ui.components.LoadingBox
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.RadioDot
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.ServicePicture
import com.coolingcare.app.ui.components.Sheet
import com.coolingcare.app.ui.components.Stepper
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.TextBtn
import com.coolingcare.app.ui.components.Toasts
import com.coolingcare.app.ui.components.Toggle
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.icons.CcIconsBold
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalLang
import kotlinx.coroutines.launch

/** WhatsApp chat with the shop, SMS if WhatsApp is missing. */
fun messageShop(text: String) { if (Graph.launcher.whatsApp(text) == Sent.NO_APP) Graph.launcher.sms(text) }

/** 40 / 41 / 43 · Booking status, live from Firestore (Requested · Confirmed · On the way · Completed · Cancelled). */
@Composable
fun StatusScreen(id: String, onBack: () -> Unit, onBookAgain: (Booking) -> Unit) {
    val live by remember(id) { Graph.cloud.order(id) }.collectAsState(Graph.repo.booking(id)?.let { Live.Value(it) } ?: Live.Loading)
    val b = (live as? Live.Value)?.value
    if (b == null) {
        Screen(top = { AppBar(S.bookingStatus.s, onBack) }) {
            if (live is Live.Missing) Note(CcIcons.CircleAlert, S.orderMissing.s, bg = CC.dangerTint, tint = CC.danger) else LoadingBox(Modifier.fillMaxWidth())
        }
        return
    }
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var manage by rememberSaveable { mutableStateOf(false) }
    var cancel by rememberSaveable { mutableStateOf(false) }
    var reschedule by rememberSaveable { mutableStateOf(false) }
    val perm = rememberNotificationPermission()

    if (b.status == BookingStatus.COMPLETED) { CompletedScreen(b, onBack, onBookAgain); return }
    val steps = listOf(S.stRequested.s, S.stConfirmed.s, S.stOnTheWay.s, S.stCompleted.s)
    Screen(
        top = { AppBar(S.bookingStatus.s, onBack) { if (b.status.active) IconBtn(CcIcons.EllipsisVertical, { manage = true }, description = S.manageBooking.s) } },
        gap = 14.dp,
    ) {
        when (b.status) {
            BookingStatus.REQUESTED -> Banner(CcIcons.Send, CC.primaryTint, CC.primary, CC.primary, S.waitingConfirm.s, S.waitingBody.s)
            BookingStatus.CONFIRMED -> Banner(CcIcons.Check, CC.greenTint, CC.green, CC.greenText, S.visitConfirmed.s, S.visitConfirmedBody.s)
            BookingStatus.ON_THE_WAY -> Banner(CcIcons.Truck, CC.primaryTint, CC.primary, CC.primary, S.onTheWayTitle.s, S.onTheWayBody.s)
            else -> {
                val byShop = b.history.lastOrNull()?.by == "admin"
                Banner(CcIcons.Ban, CC.dangerTint, CC.danger, CC.danger, S.cancelledTitle.s, if (byShop) S.cancelledByShop(b.cancelReason).s else S.cancelledBody.s)
            }
        }
        BookingCard(b, team = b.status == BookingStatus.CONFIRMED || b.status == BookingStatus.ON_THE_WAY)
        if (b.status != BookingStatus.CANCELLED) Box(Modifier.padding(top = 6.dp, bottom = 2.dp)) {
            Stepper(steps, when (b.status) { BookingStatus.REQUESTED -> 1; BookingStatus.CONFIRMED -> 2; else -> 3 })
        }
        if (b.status == BookingStatus.REQUESTED) Note(CcIcons.BellRing, S.usuallyConfirmed.s)
        if (b.status != BookingStatus.CANCELLED) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn(S.callUs.s, { Graph.launcher.dial() }, Modifier.weight(1f), BtnStyle.Green, lead = CcIcons.Phone, height = 48.dp)
            Btn(S.whatsappSupport.s, { messageShop(Messages.support(b, lang)) }, Modifier.weight(1f), BtnStyle.GreenOutline, lead = CcIcons.Whatsapp, height = 48.dp, small = true, px = 8.dp)
        } else Btn(S.bookAgain.s, { onBookAgain(b) }, Modifier.fillMaxWidth(), arrow = true)
        if (b.status == BookingStatus.CONFIRMED || b.status == BookingStatus.ON_THE_WAY)
            Note(CcIcons.Info, S.techContact.s, trailing = { if (b.status.customerCanChange) TextBtn(S.update.s, { manage = true }) })
        if (!perm.granted && b.status.active) Note(CcIcons.BellRing, S.notifOffNote.s, bg = CC.warningTint, tint = CC.warning, trailing = { TextBtn(S.turnOn.s, perm.request, color = CC.warning) })
    }
    if (manage) ManageSheet(b, onDismiss = { manage = false }, onReschedule = { manage = false; reschedule = true }, onCancel = { manage = false; cancel = true })
    if (cancel) CancelDialog(b, onDismiss = { cancel = false }, onReschedule = { cancel = false; reschedule = true })
    if (reschedule) DateTimeSheet(b.date, b.slotId, { reschedule = false }, title = S.changeDateTime.s, cta = S.sendRequest.s) { date, slot ->
        reschedule = false
        scope.launch { runCatching { Graph.cloud.rescheduleByCustomer(b.id, date, slot) }.onFailure { Toasts.error(it) } }
    }
}

@Composable
private fun Banner(icon: ImageVector, bg: Color, iconBg: Color, titleColor: Color, title: String, body: String) =
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, iconBg, CC.onPrimary, 40.dp, 21.dp, CircleShape)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) { T(title, type.titleS, titleColor); T(body, type.bodyS, CC.text2) }
    }

@Composable
fun BookingCard(b: Booking, team: Boolean) {
    val ap = Catalog.appliance(b.applianceId); val slot = Catalog.slot(b.slotId)
    @Suppress("DEPRECATION") val clipboard = LocalClipboardManager.current
    Column(Modifier.fillMaxWidth().card(18.dp).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (ap != null) Img(ap.image, Modifier.size(64.dp, 46.dp).clip(RoundedCornerShape(10.dp)).background(CC.tile), ContentScale.Fit)
                    else ServicePicture(b.serviceImage, b.serviceImagePath, Modifier.size(64.dp, 46.dp).clip(RoundedCornerShape(10.dp)))
                    Column(Modifier.weight(1f)) { T(b.service.s, type.titleS); T(b.appliance.s, type.bodyS, CC.text2) }
                }
                InfoLine(CcIcons.Calendar, Dates.long(Dates.parse(b.date)).s)
                InfoLine(CcIcons.Clock, slot?.label?.s ?: b.slot.s)
                InfoLine(CcIcons.MapPin, b.address)
            }
            if (team) Column(Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Img(Res.drawable.img_tech_portrait, Modifier.size(96.dp, 104.dp).clip(RoundedCornerShape(14.dp)), ContentScale.Crop, Alignment.TopCenter)
                T(S.coolingCareUr.s, type.labelM, align = TextAlign.Center); T(S.serviceTechnician.s, type.caption, CC.text2, align = TextAlign.Center)
            }
        }
        HDivider()
        Row(Modifier.fillMaxWidth().tap { clipboard.setText(AnnotatedString(b.id)) }, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            T(S.bookingId.s, type.caption, CC.text3); T(b.id, type.labelM, latin = true); Box(Modifier.weight(1f)); Ic(CcIcons.Copy, 15.dp, CC.text3)
        }
    }
}

/** 42 · Manage booking: another time / message the shop / cancel (until the technician leaves). */
@Composable
private fun ManageSheet(b: Booking, onDismiss: () -> Unit, onReschedule: () -> Unit, onCancel: () -> Unit) {
    val lang = LocalLang.current
    Sheet(S.manageBooking.s + " " + b.id, onDismiss) {
        val shape = RoundedCornerShape(14.dp)
        @Composable fun Option(icon: ImageVector, bg: Color, fg: Color, title: String, sub: String, titleColor: Color = CC.text, onClick: () -> Unit) =
            Row(Modifier.fillMaxWidth().clip(shape).border(1.dp, CC.border, shape).tap(onClick = onClick).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconTile(icon, bg, fg, 40.dp, 20.dp, RoundedCornerShape(12.dp))
                Column(Modifier.weight(1f)) { T(title, type.titleS, titleColor); T(sub, type.bodyS, CC.text2) }
                Ic(CcIcons.ChevronRight, 18.dp, CC.text3)
            }
        if (b.status.customerCanChange) Option(CcIcons.CalendarSync, CC.primaryTint, CC.primary, S.changeDateTime.s, S.changeDateTimeSub.s, onClick = onReschedule)
        Option(CcIcons.Whatsapp, CC.greenTint, CC.green, S.messageCC.s, S.messageCCSub.s) { onDismiss(); messageShop(Messages.support(b, lang)) }
        if (b.status.customerCanChange) Option(CcIcons.Ban, CC.dangerTint, CC.danger, S.cancelBooking.s, S.cancelBookingSub.s, CC.danger, onCancel)
        else Note(CcIcons.Truck, S.lockedOnTheWay.s, Modifier.tap { Graph.launcher.dial() }, bg = CC.warningTint, tint = CC.warning)
    }
}

/** 44 · Cancel booking dialog. */
@Composable
private fun CancelDialog(b: Booking, onDismiss: () -> Unit, onReschedule: () -> Unit) {
    var reason by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    CcDialog(onDismiss) {
        IconTile(CcIcons.CalendarX, CC.dangerTint, CC.danger, 60.dp, 28.dp, RoundedCornerShape(18.dp))
        T(S.cancelThis.s, type.titleM, align = TextAlign.Center)
        T(S.cancelThisBody.s, type.bodyM, CC.text2, align = TextAlign.Center)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, CC.border, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 2.dp)) {
            S.reasons.forEachIndexed { i, (_, r) ->
                Row(Modifier.fillMaxWidth().tap { reason = i }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    T(r.s, if (reason == i) type.titleS else type.bodyM, modifier = Modifier.weight(1f)); RadioDot(reason == i)
                }
                if (i < S.reasons.lastIndex) HDivider()
            }
        }
        Btn(S.cancelBookingCta.s, {
            busy = true
            scope.launch {
                runCatching { Graph.cloud.cancelByCustomer(b.id, S.reasons[reason].first) }.onSuccess { onDismiss() }.onFailure { Toasts.error(it) }
                busy = false
            }
        }, Modifier.fillMaxWidth(), BtnStyle.Danger, enabled = !busy)
        Btn(S.keepBooking.s, onDismiss, Modifier.fillMaxWidth(), BtnStyle.Light)
        TextBtn(S.rescheduleInstead.s, onReschedule)
    }
}

/** 43 · Service completed: rating (saved on the order for the shop), invoice, next maintenance reminder. */
@Composable
private fun CompletedScreen(b: Booking, onBack: () -> Unit, onBookAgain: (Booking) -> Unit) {
    val prefs by Graph.repo.prefs.collectAsState()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var stars by remember(b.rating) { mutableIntStateOf(b.rating ?: 0) }
    val reminderOn = prefs.maintenanceReminders && b.id !in prefs.dismissedMaintenance
    val due = Dates.dateOf(Planner.maintenanceAt(b.completedAt ?: b.updatedAt))
    Screen(
        top = { AppBar(S.bookingStatus.s, onBack) },
        footer = {
            Footer {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Btn(S.getInvoice.s, { messageShop(Messages.invoice(b, lang)) }, Modifier.weight(1f), BtnStyle.Secondary, lead = CcIcons.Receipt, px = 10.dp)
                    Btn(S.bookAgain.s, { onBookAgain(b) }, Modifier.weight(1f))
                }
            }
        },
        gap = 14.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(76.dp).clip(CircleShape).background(CC.greenTint), contentAlignment = Alignment.Center) { CheckCircle(54.dp, CC.green) }
            T(S.serviceCompleted.s, type.titleL, align = TextAlign.Center)
            T(b.service.s + " · " + Dates.long(Dates.parse(b.date)).s, type.bodyS, CC.text2, align = TextAlign.Center)
        }
        Stepper(listOf(S.stRequested.s, S.stConfirmed.s, S.stOnTheWay.s, S.stCompleted.s), 4, CC.green)
        Column(Modifier.fillMaxWidth().card(16.dp, elevated = false).padding(horizontal = 16.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            T(S.howWasService.s, type.titleS)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..5) Icon(if (i <= stars) CcIconsBold.StarFilled else CcIcons.Star, null, Modifier.size(34.dp).clip(CircleShape).tap {
                    stars = i
                    scope.launch { runCatching { Graph.cloud.rate(b.id, i) }.onFailure { Toasts.error(it); stars = b.rating ?: 0 } }
                }.padding(2.dp), tint = if (i <= stars) CC.star else CC.border)
            }
            if (b.rating != null) T(S.ratingThanks.s, type.caption, CC.greenText, align = TextAlign.Center)
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CC.primaryTint).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(CcIcons.BellRing, CC.surface, CC.primary, 40.dp, 20.dp)
            Column(Modifier.weight(1f)) { T(S.nextReminder.s, type.titleS); T(if (reminderOn) S.remindOn(Dates.plain(due)).s else S.reminderOff.s, type.bodyS, CC.text2) }
            Toggle(reminderOn, { on ->
                if (on) Graph.repo.updatePrefs { it.copy(maintenanceReminders = true, dismissedMaintenance = it.dismissedMaintenance - b.id) } else Graph.repo.dismissMaintenance(b.id)
            }, S.nextReminder.s)
        }
    }
}

