package com.coolingcare.app.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.data.Live
import com.coolingcare.app.domain.Dates
import com.coolingcare.app.domain.Messages
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.domain.ltr
import com.coolingcare.app.feature.book.DateTimeSheet
import com.coolingcare.app.feature.home.ago
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Booking
import com.coolingcare.app.model.BookingStatus
import com.coolingcare.app.model.HistoryEntry
import com.coolingcare.app.model.Txt
import com.coolingcare.app.platform.Sent
import com.coolingcare.app.platform.SystemBackHandler
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.BtnStyle
import com.coolingcare.app.ui.components.Centered
import com.coolingcare.app.ui.components.Chip
import com.coolingcare.app.ui.components.Field
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconBtn
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.InfoLine
import com.coolingcare.app.ui.components.LoadingBox
import com.coolingcare.app.ui.components.LogoMark
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.Pill
import com.coolingcare.app.ui.components.RadioDot
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.SearchBox
import com.coolingcare.app.ui.components.SectionHeader
import com.coolingcare.app.ui.components.ServicePicture
import com.coolingcare.app.ui.components.Sheet
import com.coolingcare.app.ui.components.StorageImage
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.TextBtn
import com.coolingcare.app.ui.components.Toasts
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.icons.CcIconsBold
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalLang
import kotlinx.coroutines.launch

// ------------------------------------------------------------------ host & nav

/** Admin area: Dashboard · Orders · Services · Settings (70 / 71 / 74 / 76). */
@Composable
fun AdminHost(startTab: Int, onOrder: (String) -> Unit, onService: (String?) -> Unit, onLanguage: () -> Unit, onCustomerView: () -> Unit) {
    var tab by rememberSaveable(startTab) { mutableIntStateOf(startTab) }
    var filter by rememberSaveable { mutableStateOf("new") }
    val all by Graph.cloud.allOrders.collectAsState()
    val newCount = (all as? Live.Value)?.value?.count { it.status == BookingStatus.REQUESTED } ?: 0
    SystemBackHandler(enabled = tab != 0) { tab = 0 }
    Column(Modifier.fillMaxSize().background(CC.bg)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> AdminDashboard(onOrder) { f -> filter = f; tab = 1 }
                1 -> AdminOrders(filter, { filter = it }, onOrder)
                2 -> ServicesManager(onService)
                else -> AdminSettings(onLanguage, onCustomerView)
            }
        }
        AdminNav(tab, newCount) { tab = it }
    }
}

@Composable
private fun AdminNav(active: Int, badge: Int, onSelect: (Int) -> Unit) {
    val items = listOf(CcIcons.LayoutDashboard to S.dashboard, CcIcons.ClipboardList to S.orders, CcIcons.Wrench to S.services, CcIcons.Settings to S.settings)
    Box(Modifier.fillMaxWidth().shadow(14.dp, ambientColor = Color(0x120B1F45), spotColor = Color(0x140B1F45)).background(CC.surface).navigationBarsPadding()) {
        Centered {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                items.forEachIndexed { i, (icon, label) ->
                    val on = i == active
                    Column(Modifier.width(76.dp).clip(RoundedCornerShape(12.dp)).tap { onSelect(i) }.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(Modifier.size(52.dp, 28.dp).clip(RoundedCornerShape(14.dp)).background(if (on) CC.primaryTint else Color.Transparent), contentAlignment = Alignment.Center) {
                            Ic(icon, 21.dp, if (on) CC.primary else CC.text3)
                            if (i == 1 && badge > 0) Box(Modifier.align(Alignment.TopEnd).padding(end = 8.dp, top = 2.dp).size(8.dp).clip(CircleShape).background(CC.danger))
                        }
                        T(label.s, if (on) type.labelS else type.caption, if (on) CC.primary else CC.text3, maxLines = 1)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ shared bits

fun statusLabel(s: BookingStatus): Txt = when (s) {
    BookingStatus.REQUESTED -> S.stNew; BookingStatus.CONFIRMED -> S.stConfirmed; BookingStatus.ON_THE_WAY -> S.stOnTheWay
    BookingStatus.COMPLETED -> S.stCompleted; BookingStatus.CANCELLED -> S.stCancelled
}

private fun statusColors(s: BookingStatus): Pair<Color, Color> = when (s) {
    BookingStatus.REQUESTED -> CC.primaryTint to CC.primary; BookingStatus.CONFIRMED -> CC.greenTint to CC.greenText
    BookingStatus.ON_THE_WAY -> CC.warningTint to CC.warning; BookingStatus.COMPLETED -> CC.surface2 to CC.text2
    BookingStatus.CANCELLED -> CC.dangerTint to CC.danger
}

@Composable
fun StatusPill(s: BookingStatus) { val (bg, fg) = statusColors(s); Pill(statusLabel(s).s, bg = bg, color = fg, padding = PaddingValues(horizontal = 9.dp, vertical = 3.dp)) }

@Composable
fun OrderCard(o: Booking, onClick: () -> Unit, actions: (@Composable RowScope.() -> Unit)? = null) {
    val lang = LocalLang.current
    val slot = Catalog.slot(o.slotId)?.label ?: o.slot
    Column(Modifier.fillMaxWidth().card(16.dp).tap(onClick = onClick).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            T(o.id, type.titleS, latin = true); StatusPill(o.status); Box(Modifier.weight(1f)); T(ago(o.createdAt, lang), type.caption, CC.text3)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ServicePicture(o.serviceImage, o.serviceImagePath, Modifier.size(56.dp, 48.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                T(o.service.s, type.titleS, maxLines = 1)
                T(o.name + " · " + ltr(Phone.display(o.phone)), type.bodyS, CC.text2, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ic(CcIcons.Calendar, 14.dp, CC.text3); T(Dates.short(Dates.parse(o.date)).s + " | " + slot.s, type.caption, CC.text2, maxLines = 1)
                }
            }
        }
        if (actions != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

/** Runs an admin action with a busy flag; errors become a toast. */
@Composable
fun rememberAction(): Pair<Boolean, (suspend () -> Unit, (() -> Unit)?) -> Unit> {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    return busy to { block, done ->
        if (!busy) {
            busy = true
            scope.launch {
                try { block(); done?.invoke() } catch (e: Throwable) { if (e is kotlinx.coroutines.CancellationException) throw e; Toasts.error(e) } finally { busy = false }
            }
        }
    }
}

// ------------------------------------------------------------------ 70 dashboard

@Composable
private fun AdminDashboard(onOrder: (String) -> Unit, onFilter: (String) -> Unit) {
    val live by Graph.cloud.allOrders.collectAsState()
    val (busy, act) = rememberAction()
    Screen(
        top = {
            Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 20.dp, end = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                LogoMark(36.dp, shadow = false)
                Column(Modifier.weight(1f)) {
                    T(S.appName.s, type.titleS, latin = true)
                    Pill(S.adminBadge.s, icon = CcIcons.ShieldUser, bg = CC.primary, color = CC.onPrimary, padding = PaddingValues(horizontal = 8.dp, vertical = 2.dp))
                }
                val news = (live as? Live.Value)?.value?.any { it.status == BookingStatus.REQUESTED } == true
                IconBtn(CcIcons.Bell, { onFilter("new") }, badge = news, description = S.statNew.s)
            }
        },
        gap = 14.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val h = Dates.now().hour
            T((if (h < 12) S.goodMorningAdmin else if (h < 17) S.goodAfternoonAdmin else S.goodEveningAdmin).s, type.titleL)
            T(Dates.weekdayLong(Dates.today()).s, type.bodyS, CC.text2)
        }
        val orders = (live as? Live.Value)?.value
        if (orders == null) { if (live is Live.Missing) Note(CcIcons.CircleAlert, S.errGeneric.s, bg = CC.dangerTint, tint = CC.danger) else LoadingBox(Modifier.fillMaxWidth()); return@Screen }
        val today = Dates.today().toString(); val weekAgo = Dates.nowMs() - 7 * 86_400_000L
        val new = orders.filter { it.status == BookingStatus.REQUESTED }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(CcIcons.Inbox, CC.primaryTint, CC.primary, new.size, S.statNew.s, Modifier.weight(1f)) { onFilter("new") }
                Stat(CcIcons.CalendarCheck, CC.greenTint, CC.green, orders.count { it.date == today && (it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.ON_THE_WAY) }, S.statToday.s, Modifier.weight(1f)) { onFilter("confirmed") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(CcIcons.Truck, CC.warningTint, CC.warning, orders.count { it.status == BookingStatus.ON_THE_WAY }, S.statOnTheWay.s, Modifier.weight(1f)) { onFilter("on_the_way") }
                Stat(CcIcons.CircleCheck, CC.surface2, CC.text2, orders.count { it.status == BookingStatus.COMPLETED && (it.completedAt ?: it.updatedAt) >= weekAgo }, S.statDoneWeek.s, Modifier.weight(1f)) { onFilter("done") }
            }
        }
        SectionHeader(S.statNew.s, S.seeAll.s) { onFilter("new") }
        if (new.isEmpty()) Note(CcIcons.Inbox, S.noNewOrders.s, bg = CC.bg2, tint = CC.text2)
        for (o in new.take(4)) OrderCard(o, { onOrder(o.id) }) {
            Btn(S.view.s, { onOrder(o.id) }, Modifier.weight(1f), BtnStyle.Light, height = 38.dp, small = true)
            Btn(S.confirm.s, { act({ Graph.cloud.setStatus(o.id, BookingStatus.CONFIRMED) }) { Toasts.show(S.savedNotified) } }, Modifier.weight(1f), BtnStyle.Green, lead = CcIcons.Check, height = 38.dp, small = true, enabled = !busy)
        }
    }
}

@Composable
private fun Stat(icon: ImageVector, bg: Color, fg: Color, n: Int, label: String, modifier: Modifier, onClick: () -> Unit) =
    Column(modifier.card(16.dp).tap(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IconTile(icon, bg, fg, 36.dp, 18.dp, RoundedCornerShape(10.dp))
        T(n.toString(), type.displayM, latin = true)
        T(label, type.labelM, CC.text2, maxLines = 1)
    }

// ------------------------------------------------------------------ 71 orders

@Composable
private fun AdminOrders(filter: String, onFilter: (String) -> Unit, onOrder: (String) -> Unit) {
    val live by Graph.cloud.allOrders.collectAsState()
    var searching by rememberSaveable { mutableStateOf(false) }
    var q by rememberSaveable { mutableStateOf("") }
    val orders = (live as? Live.Value)?.value.orEmpty()
    fun count(st: BookingStatus) = orders.count { it.status == st }
    Screen(
        top = {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T(S.orders.s, type.titleL, modifier = Modifier.weight(1f))
                    IconBtn(if (searching) CcIcons.X else CcIcons.Search, { searching = !searching; if (!searching) q = "" }, tint = CC.primary, bg = CC.primaryTint, size = 38.dp, iconSize = 19.dp, description = S.searchOrders.s)
                }
                if (searching) SearchBox(q, { q = it }, S.searchOrders.s)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(S.stNew.s + " · " + count(BookingStatus.REQUESTED), filter == "new", { onFilter("new") }, height = 34.dp)
                Chip(S.stConfirmed.s + " · " + count(BookingStatus.CONFIRMED), filter == "confirmed", { onFilter("confirmed") }, height = 34.dp)
                Chip(S.stOnTheWay.s + " · " + count(BookingStatus.ON_THE_WAY), filter == "on_the_way", { onFilter("on_the_way") }, height = 34.dp)
                Chip(S.doneFilter.s, filter == "done", { onFilter("done") }, height = 34.dp)
                Chip(S.stCancelled.s, filter == "cancelled", { onFilter("cancelled") }, height = 34.dp)
                Chip(S.all.s, filter == "all", { onFilter("all") }, height = 34.dp)
            }
        },
        gap = 10.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
    ) {
        if (live !is Live.Value) { LoadingBox(Modifier.fillMaxWidth()); return@Screen }
        val needle = q.trim(); val digits = needle.filter { it.isDigit() }
        val list = orders.filter {
            when (filter) {
                "new" -> it.status == BookingStatus.REQUESTED; "confirmed" -> it.status == BookingStatus.CONFIRMED; "on_the_way" -> it.status == BookingStatus.ON_THE_WAY
                "done" -> it.status == BookingStatus.COMPLETED; "cancelled" -> it.status == BookingStatus.CANCELLED; else -> true
            }
        }.filter { needle.isBlank() || it.id.contains(needle, true) || it.name.contains(needle, true) || (digits.length >= 3 && it.phone.filter(Char::isDigit).contains(digits.removePrefix("92").removePrefix("0"))) }
            .let { l -> if (filter == "confirmed" || filter == "on_the_way") l.sortedBy { it.date + it.slotId } else l }
        if (list.isEmpty()) Note(CcIcons.Inbox, S.noOrders.s, bg = CC.bg2, tint = CC.text2)
        for (o in list) OrderCard(o, { onOrder(o.id) })
    }
}

// ------------------------------------------------------------------ 72 order detail + 73 update sheet

@Composable
fun AdminOrderScreen(id: String, onBack: () -> Unit) {
    val cached = (Graph.cloud.allOrders.value as? Live.Value)?.value?.firstOrNull { it.id == id }
    val live by remember(id) { Graph.cloud.order(id) }.collectAsState(cached?.let { Live.Value(it) } ?: Live.Loading)
    val o = (live as? Live.Value)?.value
    if (o == null) {
        Screen(top = { AppBar(S.orderTitle(id).s, onBack) }) {
            if (live is Live.Missing) Note(CcIcons.CircleAlert, S.orderMissing.s, bg = CC.dangerTint, tint = CC.danger) else LoadingBox(Modifier.fillMaxWidth())
        }
        return
    }
    val lang = LocalLang.current
    val (busy, act) = rememberAction()
    var sheet by rememberSaveable { mutableStateOf<String?>(null) }
    var viewer by remember { mutableStateOf<String?>(null) }
    @Suppress("DEPRECATION") val clipboard = LocalClipboardManager.current
    val slot = Catalog.slot(o.slotId)?.label ?: o.slot
    Screen(
        top = { AppBar(S.orderTitle(o.id).s, onBack) { IconBtn(CcIcons.EllipsisVertical, { sheet = "status" }, description = S.updateOrder(o.id).s) } },
        footer = if (o.status.active) ({
            Footer {
                when (o.status) {
                    BookingStatus.REQUESTED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Btn(S.cancel.s, { sheet = "cancel" }, Modifier.weight(1f), BtnStyle.DangerSoft, lead = CcIcons.Ban, height = 48.dp, small = true)
                            Btn(S.confirmVisit.s, { act({ Graph.cloud.setStatus(o.id, BookingStatus.CONFIRMED) }) { Toasts.show(S.savedNotified) } }, Modifier.weight(1f), BtnStyle.Green, lead = CcIcons.Check, height = 48.dp, enabled = !busy)
                        }
                        TextBtn(S.proposeTime.s, { sheet = "time" }, Modifier.align(Alignment.CenterHorizontally))
                    }
                    BookingStatus.CONFIRMED -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Btn(S.changeTime.s, { sheet = "time" }, Modifier.weight(1f), BtnStyle.Light, lead = CcIcons.CalendarSync, height = 48.dp, small = true)
                        Btn(S.markOnTheWay.s, { act({ Graph.cloud.setStatus(o.id, BookingStatus.ON_THE_WAY) }) { Toasts.show(S.savedNotified) } }, Modifier.weight(1f), lead = CcIcons.Truck, height = 48.dp, enabled = !busy)
                    }
                    else -> Btn(S.markCompleted.s, { act({ Graph.cloud.setStatus(o.id, BookingStatus.COMPLETED) }) { Toasts.show(S.savedNotified) } }, Modifier.fillMaxWidth(), BtnStyle.Green, lead = CcIcons.CircleCheck, height = 48.dp, enabled = !busy)
                }
            }
        }) else null,
        gap = 12.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 10.dp),
    ) {
        OrderBanner(o)
        Section(S.secCustomer.s) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val initials = o.name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
                Box(Modifier.size(44.dp).clip(CircleShape).background(CC.primary), contentAlignment = Alignment.Center) { T(initials.ifBlank { "?" }, type.titleS, CC.onPrimary) }
                Column(Modifier.weight(1f)) { T(o.name, type.titleS); T(Phone.display(o.phone), type.bodyS, CC.text2, latin = true) }
                IconBtn(CcIcons.Phone, { Graph.launcher.dial(o.phone) }, tint = CC.onPrimary, bg = CC.green, size = 40.dp, iconSize = 19.dp, description = S.call.s)
                IconBtn(CcIcons.Whatsapp, {
                    val text = Messages.toCustomer(o)
                    if (Graph.launcher.whatsApp(text, Phone.waDigits(o.phone)) == Sent.NO_APP) Graph.launcher.sms(text, Phone.e164(o.phone))
                }, tint = CC.green, bg = CC.greenTint, size = 40.dp, iconSize = 19.dp, description = S.whatsapp.s)
            }
        }
        Section(S.secService.s) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ServicePicture(o.serviceImage, o.serviceImagePath, Modifier.size(60.dp, 50.dp).clip(RoundedCornerShape(10.dp)))
                Column(Modifier.weight(1f)) { T(o.service.s, type.titleS); T(o.appliance.s, type.bodyS, CC.text2) }
            }
            InfoLine(CcIcons.Calendar, Dates.long(Dates.parse(o.date)).s + "  ·  " + slot.s)
            Row(Modifier.fillMaxWidth().tap { clipboard.setText(AnnotatedString(o.address)); Toasts.show(S.copied) }, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Ic(CcIcons.MapPin, 18.dp, CC.text2); T(o.address, type.bodyS, modifier = Modifier.weight(1f)); Ic(CcIcons.Copy, 16.dp)
            }
        }
        Section(S.secIssue.s) {
            T(o.issue.ifBlank { S.noIssue.s }, type.bodyM, if (o.issue.isBlank()) CC.text3 else CC.text)
            if (o.photos.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (p in o.photos) StorageImage(p, Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).tap { viewer = p })
            }
        }
        if (o.rating != null) Section(S.rated.s) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { for (i in 1..5) Icon(if (i <= o.rating) CcIconsBold.StarFilled else CcIcons.Star, null, Modifier.size(24.dp), tint = if (i <= o.rating) CC.star else CC.border) }
        }
        Section(S.secHistory.s) { o.history.forEachIndexed { i, h -> HistoryLine(h, i == 0, lang) } }
        NoteField(o)
    }
    when (sheet) {
        "status" -> UpdateStatusSheet(o, null, { sheet = null })
        "cancel" -> UpdateStatusSheet(o, BookingStatus.CANCELLED, { sheet = null })
        "time" -> DateTimeSheet(o.date, o.slotId, { sheet = null }, title = S.proposeTime.s, cta = S.confirmThisTime.s, note = S.willNotify(o.name).s) { date, slotId ->
            sheet = null
            act({ Graph.cloud.confirmAtTime(o.id, date, slotId) }) { Toasts.show(S.savedNotified) }
        }
    }
    viewer?.let { PhotoViewer(it) { viewer = null } }
}

@Composable
private fun OrderBanner(o: Booking) {
    val lang = LocalLang.current
    val slot = Catalog.slot(o.slotId)?.label ?: o.slot
    val whenText = Dates.short(Dates.parse(o.date)).s + " · " + slot.s
    val (icon, bg, fg, title) = when (o.status) {
        BookingStatus.REQUESTED -> Quad(CcIcons.Inbox, CC.primaryTint, CC.primary, S.bannerNew)
        BookingStatus.CONFIRMED -> Quad(CcIcons.CalendarCheck, CC.greenTint, CC.green, S.bannerConfirmed)
        BookingStatus.ON_THE_WAY -> Quad(CcIcons.Truck, CC.warningTint, CC.warning, S.bannerOnTheWay)
        BookingStatus.COMPLETED -> Quad(CcIcons.CircleCheck, CC.surface2, CC.text2, S.bannerCompleted)
        BookingStatus.CANCELLED -> Quad(CcIcons.Ban, CC.dangerTint, CC.danger, S.bannerCancelled)
    }
    val sub = when (o.status) {
        BookingStatus.REQUESTED -> S.placedAgo(ago(o.createdAt, lang), Dates.short(Dates.dateOf(o.createdAt)).get(lang)).s
        BookingStatus.CANCELLED -> o.cancelReason.takeIf { it.isNotBlank() }?.let { S.reason(it).s } ?: whenText
        else -> whenText
    }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, fg, CC.onPrimary, 40.dp, 20.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) { T(title.s, type.titleS, if (o.status == BookingStatus.COMPLETED) CC.text else fg); T(sub, type.bodyS, CC.text2) }
    }
}

private data class Quad(val icon: ImageVector, val bg: Color, val fg: Color, val title: Txt)

@Composable
private fun Section(title: String, content: @Composable () -> Unit) =
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, CC.border, RoundedCornerShape(16.dp)).background(CC.surface).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        T(title.uppercase(), type.labelM, CC.text3)
        content()
    }

@Composable
private fun HistoryLine(h: HistoryEntry, isFirst: Boolean, lang: com.coolingcare.app.model.Lang) {
    val (icon, text) = when {
        h.status == BookingStatus.REQUESTED && h.by == "customer" && isFirst -> CcIcons.Send to S.hRequested
        h.status == BookingStatus.REQUESTED && h.by == "customer" -> CcIcons.CalendarSync to S.hNewTime
        h.status == BookingStatus.REQUESTED -> CcIcons.RotateCcw to S.hBackToNew
        h.status == BookingStatus.CONFIRMED -> CcIcons.CalendarCheck to S.hConfirmed
        h.status == BookingStatus.ON_THE_WAY -> CcIcons.Truck to S.hOnTheWay
        h.status == BookingStatus.COMPLETED -> CcIcons.CircleCheck to S.hCompleted
        h.by == "customer" -> CcIcons.Ban to S.hCancelledCustomer
        else -> CcIcons.Ban to S.hCancelledShop
    }
    val whenText = Dates.clock(h.at, lang) + " · " + Dates.dayMonth(Dates.dateOf(h.at)).get(lang)
    val detail = buildString {
        if (h.date.isNotBlank() && h.status != BookingStatus.CANCELLED) {
            val slot = Catalog.slot(h.slotId)?.label?.get(lang)
            append(Dates.short(Dates.parse(h.date)).get(lang)); if (slot != null) append(", ").append(slot)
        }
        if (h.note.isNotBlank()) { if (isNotEmpty()) append(" — "); append(if (h.status == BookingStatus.CANCELLED && h.by == "customer") S.reason(h.note).get(lang) else h.note) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Ic(icon, 18.dp, CC.primary, Modifier.padding(top = 1.dp))
        Column(Modifier.weight(1f)) {
            T(text.s + " · " + whenText, type.bodyS)
            if (detail.isNotBlank()) T(detail, type.caption, CC.text2)
        }
    }
}

@Composable
private fun NoteField(o: Booking) {
    var note by rememberSaveable(o.id) { mutableStateOf(o.adminNote) }
    val (busy, act) = rememberAction()
    Field(note, { note = it.take(500) }, label = S.techNote.s, optional = S.optional.s, placeholder = S.techNotePh.s, icon = CcIcons.NotebookPen, multiline = true,
        trailing = if (note.trim() != o.adminNote) ({ TextBtn(S.save.s, { act({ Graph.cloud.saveAdminNote(o.id, note) }) { Toasts.show(S.noteSaved) } }) }) else null)
}

/** 73 · Update status (admin). Cancelling needs a reason the customer will see. */
@Composable
private fun UpdateStatusSheet(o: Booking, preset: BookingStatus?, onDismiss: () -> Unit) {
    var sel by remember { mutableStateOf(preset ?: o.status) }
    var reason by remember { mutableStateOf("") }
    val (busy, act) = rememberAction()
    Sheet(S.updateOrder(o.id).s, onDismiss) {
        val opts = listOf(
            Triple(BookingStatus.REQUESTED, CcIcons.Inbox, S.stNew to S.optNewSub),
            Triple(BookingStatus.CONFIRMED, CcIcons.CalendarCheck, S.stConfirmed to S.optConfirmedSub),
            Triple(BookingStatus.ON_THE_WAY, CcIcons.Truck, S.stOnTheWay to S.optOnTheWaySub),
            Triple(BookingStatus.COMPLETED, CcIcons.CircleCheck, S.stCompleted to S.optCompletedSub),
            Triple(BookingStatus.CANCELLED, CcIcons.Ban, S.stCancelled to S.optCancelledSub),
        )
        for ((st, icon, txt) in opts) {
            val on = sel == st; val shape = RoundedCornerShape(14.dp)
            Row(
                Modifier.fillMaxWidth().clip(shape).background(if (on) CC.primaryTint else CC.surface).border(if (on) 1.5.dp else 1.dp, if (on) CC.primary else CC.border, shape).tap { sel = st }.padding(horizontal = 12.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(icon, if (on) CC.surface else CC.surface2, if (on) CC.primary else CC.text2, 36.dp, 18.dp, RoundedCornerShape(10.dp))
                Column(Modifier.weight(1f)) { T(txt.first.s, type.titleS); T(txt.second.s, type.caption, CC.text2) }
                RadioDot(on)
            }
        }
        if (sel == BookingStatus.CANCELLED) Field(reason, { reason = it.take(200) }, label = S.cancelReasonLabel.s, placeholder = S.cancelReasonPh.s, multiline = true)
        Note(CcIcons.BellRing, S.willNotify(o.name).s, bg = CC.greenTint, tint = CC.green)
        Btn(S.saveNotify.s, { act({ Graph.cloud.setStatus(o.id, sel, if (sel == BookingStatus.CANCELLED) reason else "") }) { Toasts.show(S.savedNotified); onDismiss() } }, Modifier.fillMaxWidth(),
            enabled = !busy && sel != o.status && (sel != BookingStatus.CANCELLED || reason.isNotBlank()))
    }
}

/** Full-screen look at an order photo. */
@Composable
private fun PhotoViewer(path: String, onDismiss: () -> Unit) = Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Box(Modifier.fillMaxSize().background(Color(0xF0000000)).tap(onClick = onDismiss), contentAlignment = Alignment.Center) {
        StorageImage(path, Modifier.fillMaxWidth().widthIn(max = 700.dp).height(520.dp), ContentScale.Fit, minSide = 1400)
        IconBtn(CcIconsBold.X, onDismiss, Modifier.align(Alignment.TopEnd).padding(16.dp), tint = CC.onPrimary, bg = Color(0x33FFFFFF), description = S.close.s)
    }
}

