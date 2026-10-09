package com.coolingcare.app.feature.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.data.Problem
import com.coolingcare.app.data.problem
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.domain.Validate
import com.coolingcare.app.domain.ltr
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.platform.rememberNotificationPermission
import com.coolingcare.app.platform.rememberPhotoPicker
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.BtnStyle
import com.coolingcare.app.ui.components.CcDialog
import com.coolingcare.app.ui.components.Field
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.HDivider
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.Img
import com.coolingcare.app.ui.components.InfoLine
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.Pill
import com.coolingcare.app.ui.components.RadioDot
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.SelectField
import com.coolingcare.app.ui.components.ServiceThumb
import com.coolingcare.app.ui.components.Sheet
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.TextBtn
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.rememberLocalImage
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.text
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.icons.CcIconsBold
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalLang
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

const val MAX_PHOTOS = 3

fun Modifier.dashedBorder(color: Color, radius: Dp, width: Dp = 1.2.dp) = this.drawBehind {
    drawRoundRect(color, style = Stroke(width.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))), cornerRadius = CornerRadius(radius.toPx()))
}

/** 30 / 34 · Book a Visit form. Validation errors appear after the first Request Booking tap. */
@Composable
fun BookScreen(onBack: () -> Unit, onReview: () -> Unit) {
    val d = Graph.draft
    val services by Graph.cloud.services.collectAsState()
    var sheet by remember { mutableStateOf<String?>(null) }
    val err = Validate.form(d.date, d.slotId, d.name, d.phone, d.address)
    val show = d.showErrors
    val svc = services.firstOrNull { it.id == d.serviceId && it.active }; val ap = Catalog.appliance(d.applianceId)
    Screen(
        top = { AppBar(S.bookServiceVisit.s, onBack) },
        footer = { Footer { Btn(S.requestBooking.s, { if (svc == null) sheet = "appliance" else if (err.ok) onReview() else d.showErrors = true }, Modifier.fillMaxWidth(), arrow = true) } },
        gap = 11.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
    ) {
        if (show && !err.ok) Note(CcIcons.CircleAlert, S.fixFields(err.count).s, bg = CC.dangerTint, tint = CC.danger, textColor = CC.danger)
        SelectField(ap?.name?.s, S.selectAppliance.s, { sheet = "appliance" }, label = S.selectAppliance.s)
        SelectField(svc?.title?.s, S.serviceType.s, { sheet = "appliance" }, label = S.serviceType.s, error = if (show && svc == null) S.serviceClosedNote.s else null)
        Field(d.issue, { d.issue = it.take(1000) }, label = S.describeIssue.s, optional = S.optional.s, placeholder = S.issuePh.s, multiline = true)
        PhotosBox(d.photos) { sheet = "photos" }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectField(d.date?.let { Dates.short(Dates.parse(it)).s }, S.selectDate.s, { sheet = "date" }, Modifier.weight(1f), S.preferredDate.s, CcIcons.Calendar, null,
                error = if (show && err.date) S.errDate.s else null)
            SelectField(d.slotId?.let { Catalog.slot(it)?.label?.s }, S.selectTime.s, { sheet = "date" }, Modifier.weight(1f), S.timeSlot.s, CcIcons.Clock, null,
                error = if (show && (err.slot || err.slotPassed)) (if (err.slotPassed) S.errSlotPassed.s else S.errSlot.s) else null)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(d.name, { d.name = it.take(60) }, Modifier.weight(1f), S.yourName.s, S.fullName.s, CcIcons.User, error = if (show && err.name) S.errName.s else null, capitalization = KeyboardCapitalization.Words)
            Field(d.phone, { d.phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(16) }, Modifier.weight(1f), S.contactNumber.s, "03XX XXXXXXX", CcIcons.Phone,
                error = if (show && err.phone) S.errPhone.s else null, keyboard = KeyboardType.Phone, latin = true)
        }
        Field(d.address, { d.address = it.take(300) }, label = S.serviceAddress.s, placeholder = S.addressPh.s, icon = CcIcons.MapPin, error = if (show && err.address) S.errAddress.s else null,
            trailing = { Ic(CcIcons.Pencil, 18.dp, CC.primary) })
    }
    when (sheet) {
        "appliance" -> ApplianceServiceSheet(d.applianceId, d.serviceId, { sheet = null }) { a, s -> d.applianceId = a; d.serviceId = s; sheet = null }
        "date" -> DateTimeSheet(d.date, d.slotId, { sheet = null }) { date, slot -> d.date = date; d.slotId = slot; sheet = null }
        "photos" -> PhotosSheet({ sheet = null })
    }
}

@Composable
private fun PhotosBox(photos: List<String>, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) { T(S.addPhotos.s, type.labelM); T(S.optional.s, type.caption, CC.text3) }
        Row(
            Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(12.dp)).background(CC.bg2).dashedBorder(CC.primary.copy(alpha = 0.45f), 12.dp).tap(onClick = onClick).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
        ) {
            if (photos.isEmpty()) {
                IconTile(CcIcons.Camera, size = 36.dp, iconSize = 19.dp, shape = RoundedCornerShape(10.dp))
                Column { T(S.addPhotos.s, type.titleS, CC.primary); T(S.addPhotosSub.s, type.caption, CC.text3) }
            } else {
                for (p in photos) Thumb(p, 40.dp)
                T(S.photosCount(photos.size).s, type.labelM, CC.primary)
            }
        }
    }
}

/** Thumbnail of a photo picked on this phone. */
@Composable
fun Thumb(path: String, size: Dp) {
    val bmp = rememberLocalImage(path)
    Box(Modifier.size(size).clip(RoundedCornerShape(10.dp)).background(CC.surface2)) {
        if (bmp != null) Image(bmp, null, Modifier.size(size), contentScale = ContentScale.Crop)
    }
}

/** 31 · Appliance & service sheet (one sheet for both dropdowns). Lists only the services that are open. */
@Composable
fun ApplianceServiceSheet(appliance: String, service: String, onDismiss: () -> Unit, onDone: (String, String) -> Unit) {
    val all by Graph.cloud.services.collectAsState()
    var a by remember { mutableStateOf(appliance) }
    var s by remember { mutableStateOf(service) }
    val cat = Catalog.appliance(a)?.category ?: "ac"
    val list = all.filter { it.active && it.category == cat }
    LaunchedEffect(a, list.size) { if (list.none { it.id == s }) s = (if (a == "installation") list.firstOrNull { it.id == "ac_install" } else null)?.id ?: list.firstOrNull()?.id ?: "" }
    Sheet(S.applianceAndService.s, onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in Catalog.appliances.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (ap in row) {
                    val on = ap.id == a; val shape = RoundedCornerShape(14.dp)
                    Column(
                        Modifier.weight(1f).height(82.dp).clip(shape).background(if (on) CC.primaryTint else CC.tile).then(if (on) Modifier.border(2.dp, CC.primary, shape) else Modifier)
                            .tap { a = ap.id }.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween,
                    ) { Img(ap.image, Modifier.width(84.dp).height(42.dp), ContentScale.Fit); T(ap.name.s, type.labelS, if (on) CC.primary else CC.text, maxLines = 1, align = TextAlign.Center) }
                }
            }
        }
        T(S.serviceTypeLower.s, type.labelM, CC.text2)
        if (list.isEmpty()) Note(CcIcons.Phone, S.noServicesForAppliance.s, Modifier.tap { Graph.launcher.dial() }, bg = CC.warningTint, tint = CC.warning)
        Column {
            list.forEachIndexed { i, sv ->
                Row(Modifier.fillMaxWidth().tap { s = sv.id }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { T(sv.title.s, if (sv.id == s) type.titleS else type.bodyL); T(sv.duration.s, type.bodyS, CC.text2) }
                    RadioDot(sv.id == s)
                }
                if (i < list.lastIndex) HDivider()
            }
        }
        Btn(S.done.s, { onDone(a, s) }, Modifier.fillMaxWidth(), enabled = list.any { it.id == s })
    }
}

/** 32 · Date & time sheet (also used to change the time of an order). */
@Composable
fun DateTimeSheet(date: String?, slotId: String?, onDismiss: () -> Unit, title: String? = null, cta: String? = null, note: String? = null, onConfirm: (String, String) -> Unit) {
    val now = Dates.nowMs()
    val days = Dates.bookableDays()
    var day by remember { mutableStateOf(date?.let { Dates.parse(it) }?.takeIf { it in days && Dates.dayOpen(it, now) } ?: days.firstOrNull { Dates.dayOpen(it, now) } ?: days.first()) }
    var slot by remember { mutableStateOf(slotId) }
    val slotValid = slot?.let { id -> Catalog.slot(id)?.let { Dates.slotOpen(day, it, now) } } == true
    Sheet(title ?: S.chooseDateTime.s, onDismiss) {
        T(Dates.monthYear(day).s, type.labelM, CC.text2)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            for (d in days) DayCell(d, d == day, !Dates.dayOpen(d, now), d == Dates.today()) { day = d; if (slot != null && Catalog.slot(slot!!)?.let { Dates.slotOpen(d, it, now) } != true) slot = null }
        }
        if (Dates.isClosed(day)) Note(CcIcons.Phone, S.sundayNote.s, bg = CC.warningTint, tint = CC.warning, modifier = Modifier.tap { Graph.launcher.dial() })
        T(S.timeSlotLower.s, type.labelM, CC.text2)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (row in Catalog.timeSlots.chunked(2)) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (ts in row) {
                    val open = Dates.slotOpen(day, ts, now); val on = slot == ts.id && open; val shape = RoundedCornerShape(12.dp)
                    Row(
                        Modifier.weight(1f).height(46.dp).clip(shape).background(if (on) CC.primaryTint else if (!open) CC.surface2 else CC.surface)
                            .border(if (on) 2.dp else 1.dp, if (on) CC.primary else if (!open) Color.Transparent else CC.border, shape).tap(open) { slot = ts.id },
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
                    ) { if (on) Ic(CcIcons.CircleCheck, 16.dp); T(ts.label.s, type.labelM, if (on) CC.primary else if (!open) CC.placeholder else CC.text) }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
        Note(CcIcons.Info, note ?: S.preferredTimeNote.s)
        Btn(cta ?: S.confirm.s, { onConfirm(day.toString(), slot!!) }, Modifier.fillMaxWidth(), enabled = slotValid)
    }
}

@Composable
private fun DayCell(d: LocalDate, selected: Boolean, disabled: Boolean, today: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.width(50.dp).height(76.dp).clip(shape).background(if (selected) CC.primary else if (disabled) CC.surface2 else CC.surface)
            .then(if (!selected && !disabled) Modifier.border(1.dp, CC.border, shape) else Modifier).tap(!disabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        val c = if (selected) CC.onPrimary else if (disabled) CC.placeholder else CC.text
        T(Dates.weekday(d).s, type.caption, if (selected) CC.onPrimary else if (disabled) CC.placeholder else CC.text2, maxLines = 1)
        T(d.day.toString().padStart(2, '0'), type.titleM, c, latin = true)
        if (today) T(S.today.s, type.labelS, if (selected) CC.onPrimary else CC.primary, maxLines = 1)
        else if (Dates.isClosed(d)) T(S.closed.s, type.labelS, CC.placeholder, maxLines = 1)
    }
}

/** 33 · Add photos sheet (camera / gallery, up to 3). */
@Composable
fun PhotosSheet(onDismiss: () -> Unit) {
    val d = Graph.draft
    val picker = rememberPhotoPicker(MAX_PHOTOS - d.photos.size) { paths -> d.photos.addAll(paths.take(MAX_PHOTOS - d.photos.size)) }
    Sheet(S.addPhotosTitle.s, onDismiss) {
        val full = d.photos.size >= MAX_PHOTOS
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for ((icon, label, action) in listOf(Triple(CcIcons.Camera, S.takePhoto.s, picker.camera), Triple(CcIcons.Images, S.fromGallery.s, picker.gallery))) {
                Column(
                    Modifier.weight(1f).height(92.dp).clip(RoundedCornerShape(16.dp)).background(if (full) CC.surface2 else CC.primaryTint).tap(!full) { action() },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ) { Ic(icon, 26.dp, if (full) CC.placeholder else CC.primary); T(label, type.titleS, if (full) CC.placeholder else CC.primary) }
            }
        }
        if (d.photos.isNotEmpty()) {
            T(S.selectedPhotos(d.photos.size, MAX_PHOTOS).s, type.labelM, CC.text2)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                for (p in d.photos.toList()) Box {
                    Thumb(p, 96.dp)
                    Box(Modifier.align(Alignment.TopEnd).offset(6.dp, (-6).dp).size(24.dp).clip(CircleShape).background(CC.text).tap { d.photos.remove(p); Graph.photos.delete(p) }, contentAlignment = Alignment.Center) {
                        Ic(CcIconsBold.X, 13.dp, CC.onPrimary)
                    }
                }
            }
        }
        Note(CcIcons.Lock, S.photosNote.s)
        Btn(S.done.s, onDismiss, Modifier.fillMaxWidth())
    }
}

/** 35 · Review & Place Order. The order goes to Firestore (number from a transaction) and every admin gets a push. */
@Composable
fun ReviewScreen(onBack: () -> Unit, onPlaced: (orderId: String) -> Unit) {
    val d = Graph.draft
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf<Problem?>(null) }
    if (d.date == null || d.slotId == null) { LaunchedEffect(Unit) { onBack() }; return }
    val svc = Graph.cloud.service(d.serviceId); val ap = Catalog.appliance(d.applianceId); val slot = Catalog.slot(d.slotId!!)
    fun place() {
        if (busy) return
        busy = true; failed = null
        scope.launch {
            try {
                val id = Graph.cloud.placeOrder(d, lang)
                val photos = d.photos.toList(); d.photos.clear(); photos.forEach { Graph.photos.delete(it) }
                onPlaced(id)
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                failed = e.problem()
            } finally { busy = false }
        }
    }
    Screen(
        top = { AppBar(S.reviewConfirm.s, onBack) },
        footer = { Footer { Btn(if (busy) S.placing.s else S.placeOrder.s, ::place, Modifier.fillMaxWidth(), lead = if (busy) null else CcIcons.Check, enabled = !busy) } },
        gap = 12.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth().card(16.dp).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (svc != null) ServiceThumb(svc, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)))
            Column(Modifier.weight(1f)) { T(svc?.title?.s ?: "", type.titleS); T(ap?.name?.s ?: "", type.bodyS, CC.text2) }
            TextBtn(S.edit.s, onBack)
        }
        Column(Modifier.fillMaxWidth().card(16.dp, elevated = false).padding(horizontal = 14.dp, vertical = 4.dp)) {
            InfoLine(CcIcons.Calendar, Dates.long(Dates.parse(d.date!!)).s + "  ·  " + (slot?.label?.s ?: ""), Modifier.padding(vertical = 9.dp), type.bodyM)
            HDivider(); InfoLine(CcIcons.MapPin, d.address.trim(), Modifier.padding(vertical = 9.dp), type.bodyM)
            HDivider(); InfoLine(CcIcons.User, d.name.trim() + "  ·  " + ltr(Phone.display(d.phone)), Modifier.padding(vertical = 9.dp), type.bodyM)
            if (d.issue.isNotBlank()) { HDivider(); InfoLine(CcIcons.MessageSquareText, d.issue.trim(), Modifier.padding(vertical = 9.dp), type.bodyM, maxLines = 3) }
            if (d.photos.isNotEmpty()) { HDivider(); InfoLine(CcIcons.Camera, S.photosCount(d.photos.size).s, Modifier.padding(vertical = 9.dp), type.bodyM) }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CC.primaryTint).padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            T(S.whatNext.s, type.titleS)
            for ((icon, line) in listOf(CcIcons.BellRing to S.next1, CcIcons.CalendarCheck to S.next2, CcIcons.Tag to S.next3))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { Ic(icon, 18.dp); T(line.s, type.bodyS, modifier = Modifier.weight(1f)) }
        }
    }
    failed?.let { p -> OrderFailedDialog(p, onDismiss = { failed = null }, onRetry = { failed = null; place() }, onCall = { failed = null; Graph.launcher.dial() }) }
}

/** 37 · No connection (or another error) while placing the order. The form is kept. */
@Composable
fun OrderFailedDialog(problem: Problem, onDismiss: () -> Unit, onRetry: () -> Unit, onCall: () -> Unit) = CcDialog(onDismiss) {
    val offline = problem == Problem.OFFLINE
    IconTile(if (offline) CcIcons.Wifi else CcIcons.CircleAlert, CC.surface2, CC.text2, 64.dp, 32.dp, RoundedCornerShape(20.dp))
    T((if (offline) S.noInternet else S.orderFailed).s, type.titleM, align = TextAlign.Center)
    T((if (offline) S.noInternetBody else problem.text()).s, type.bodyM, CC.text2, align = TextAlign.Center)
    if (problem != Problem.SERVICE_CLOSED) Btn(S.tryAgain.s, onRetry, Modifier.fillMaxWidth(), lead = CcIcons.RefreshCw)
    Btn(S.callNumber(ltr(Graph.cloud.business.value.phoneDisplay)).s, onCall, Modifier.fillMaxWidth(), BtnStyle.Light, lead = CcIcons.Phone, leadTint = CC.green)
}

/** 36 · Order placed. Asks for notification permission once (first order). */
@Composable
fun OrderPlacedScreen(id: String, onTrack: () -> Unit, onHome: () -> Unit) {
    val prefs by Graph.repo.prefs.collectAsState()
    val perm = rememberNotificationPermission()
    LaunchedEffect(Unit) { if (!prefs.askedNotifications) { Graph.repo.updatePrefs { it.copy(askedNotifications = true) }; if (!perm.granted) perm.request() } }
    Screen(
        footer = { Footer { Btn(S.trackOrder.s, onTrack, Modifier.fillMaxWidth(), arrow = true); TextBtn(S.backHome.s, onHome, Modifier.align(Alignment.CenterHorizontally)) } },
        gap = 20.dp, bodyPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 8.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 72.dp, start = 8.dp, end = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(120.dp).clip(CircleShape).background(CC.greenTint), contentAlignment = Alignment.Center) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(CC.green), contentAlignment = Alignment.Center) { Ic(CcIconsBold.Check, 46.dp, CC.onPrimary) }
            }
            T(S.orderPlaced.s, type.displayL, align = TextAlign.Center)
            T(S.orderNumber.s, type.bodyS, CC.text2, align = TextAlign.Center)
            Pill(id, bg = CC.primaryTint, color = CC.primary, padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), latin = true)
            T(S.orderPlacedBody.s, type.bodyM, CC.text2, align = TextAlign.Center)
        }
        if (!perm.granted) Note(CcIcons.BellRing, S.allowNotif.s, bg = CC.warningTint, tint = CC.warning, trailing = { TextBtn(S.turnOn.s, perm.request, color = CC.warning) })
    }
}
