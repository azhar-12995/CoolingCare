package com.coolingcare.app.feature.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.data.Live
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.feature.settings.AccountDialog
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Business
import com.coolingcare.app.model.Service
import com.coolingcare.app.model.Txt
import com.coolingcare.app.platform.rememberPhotoPicker
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.BtnStyle
import com.coolingcare.app.ui.components.CcDialog
import com.coolingcare.app.ui.components.Chip
import com.coolingcare.app.ui.components.Field
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.HDivider
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconBtn
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.Img
import com.coolingcare.app.ui.components.ListRow
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.Pill
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.ServicePicture
import com.coolingcare.app.ui.components.Sheet
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.TextBtn
import com.coolingcare.app.ui.components.Toasts
import com.coolingcare.app.ui.components.Toggle
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.rememberLocalImage
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalBothTypes
import kotlinx.coroutines.launch

// ------------------------------------------------------------------ 74 services manager

@Composable
fun ServicesManager(onService: (String?) -> Unit) {
    val services by Graph.cloud.services.collectAsState()
    var filter by rememberSaveable { mutableStateOf("all") }
    val scope = rememberCoroutineScope()
    val list = services.filter { filter == "all" || it.category == filter }
    Screen(
        top = {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    T(S.services.s, type.titleL, modifier = Modifier.weight(1f))
                    Btn(S.add.s, { onService(null) }, lead = CcIcons.Plus, small = true, height = 38.dp, px = 14.dp)
                }
                T(S.openClosed(services.count { it.active }, services.count { !it.active }).s, type.bodyS, CC.text2)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(S.all.s, filter == "all", { filter = "all" }, height = 34.dp)
                for (c in Catalog.categories) Chip(c.name.s, filter == c.id, { filter = c.id }, height = 34.dp)
            }
        },
        gap = 0.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 10.dp),
    ) {
        for (sv in list) {
            val off = !sv.active
            Row(Modifier.fillMaxWidth().tap { onService(sv.id) }.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).alpha(if (off) 0.6f else 1f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ServicePicture(sv.image, sv.imagePath, Modifier.size(56.dp, 48.dp).clip(RoundedCornerShape(10.dp)))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        T(sv.list.s, type.titleS)
                        T((Catalog.categories.firstOrNull { it.id == sv.category }?.name?.s ?: "") + " · " + sv.duration.s, type.caption, CC.text2, maxLines = 1)
                        if (off) Pill(S.closedHidden.s, icon = CcIcons.EyeOff, bg = CC.surface2, color = CC.text2, padding = PaddingValues(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Toggle(sv.active, { on -> scope.launch { runCatching { Graph.cloud.setServiceOpen(sv.id, on) }.onFailure { Toasts.error(it) } } }, sv.list.s)
                    T(if (off) S.closed.s else S.openLbl.s, type.caption, if (off) CC.text3 else CC.greenText)
                }
            }
            HDivider()
        }
    }
}

// ------------------------------------------------------------------ 75 add / edit service

/** Bilingual service form. [id] null = new service. */
@Composable
fun AdminServiceScreen(id: String?, onBack: () -> Unit) {
    val original = remember(id) { id?.let { Graph.cloud.service(it) } }
    val sortNext = remember { (Graph.cloud.services.value.maxOfOrNull { it.sort } ?: 0) + 10 }
    var category by rememberSaveable { mutableStateOf(original?.category ?: "ac") }
    var title by remember { mutableStateOf(original?.title ?: Txt()) }
    var short by remember { mutableStateOf(original?.short ?: Txt()) }
    var about by remember { mutableStateOf(original?.about ?: Txt()) }
    var duration by remember { mutableStateOf(original?.duration ?: Txt()) }
    val included = remember { mutableStateListOf<Txt>().apply { addAll(original?.included ?: listOf(Txt())) } }
    var active by rememberSaveable { mutableStateOf(original?.active ?: true) }
    var imageKey by rememberSaveable { mutableStateOf(original?.image ?: "svc_general") }
    var imagePath by rememberSaveable { mutableStateOf(original?.imagePath ?: "") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var tried by remember { mutableStateOf(false) }
    var pictureSheet by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val (busy, act) = rememberAction()

    fun Txt.ok(max: Int) = en.isNotBlank() && ur.isNotBlank() && en.length <= max && ur.length <= max
    val valid = title.ok(80) && short.ok(200)
    fun save() {
        tried = true
        if (!valid) return
        val items = included.map { Txt(it.en.trim(), it.ur.trim()) }.filterNot { it.isBlank }.map { if (it.en.isBlank()) it.copy(en = it.ur) else if (it.ur.isBlank()) it.copy(ur = it.en) else it }.take(12)
        val base = original ?: Service(id = Graph.cloud.newServiceId(), sort = sortNext)
        val titleChanged = original == null || original.title != title
        val s = base.copy(
            category = category, active = active, title = title.trimmed(), listTitle = if (titleChanged) null else base.listTitle, short = short.trimmed(), about = about.trimmed(),
            included = items, duration = duration.trimmed(), image = imageKey, header = if (original == null || original.image != imageKey) imageKey else base.header, imagePath = imagePath,
        )
        act({ Graph.cloud.saveService(s, photo, original?.imagePath.orEmpty()) }) { photo?.let { Graph.photos.delete(it) }; Toasts.show(S.saved); onBack() }
    }

    Screen(
        top = { AppBar((if (original == null) S.newService else S.editService).s, onBack) { if (original != null) IconBtn(CcIcons.Trash2, { confirmDelete = true }, tint = CC.danger, description = S.delete.s) } },
        footer = { Footer { Btn(if (busy) S.saving.s else S.saveService.s, ::save, Modifier.fillMaxWidth(), enabled = !busy) } },
        gap = 16.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val local = photo?.let { rememberLocalImage(it, 600) }
            val pic = Modifier.size(110.dp, 84.dp).clip(RoundedCornerShape(14.dp))
            if (local != null) Image(local, null, pic, contentScale = ContentScale.Crop) else ServicePicture(imageKey, imagePath, pic)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                T(S.servicePhoto.s, type.labelM)
                Btn(S.changePhoto.s, { pictureSheet = true }, style = BtnStyle.Soft, lead = CcIcons.ImageUp, small = true, height = 38.dp, px = 12.dp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            T(S.category.s, type.labelM)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { for (c in Catalog.categories) Chip(c.name.s, category == c.id, { category = c.id }, height = 34.dp) }
        }
        PairField(S.titleLbl.s, title, { title = it }, error = tried && !title.ok(80))
        PairField(S.shortDesc.s, short, { short = it }, multiline = true, error = tried && !short.ok(200))
        PairField(S.fullDesc.s, about, { about = it }, multiline = true, optional = true)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            T(S.includedLbl.s, type.labelM)
            included.forEachIndexed { i, item ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, CC.border, RoundedCornerShape(12.dp)).padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ic(CcIcons.Check, 16.dp, CC.green)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LangInput(item.en, { included[i] = item.copy(en = it) }, urdu = false, compact = true)
                        HDivider()
                        LangInput(item.ur, { included[i] = item.copy(ur = it) }, urdu = true, compact = true)
                    }
                    IconBtn(CcIcons.X, { included.removeAt(i) }, size = 32.dp, iconSize = 16.dp, tint = CC.text3, description = S.delete.s)
                }
            }
            if (included.size < 12) TextBtn(S.addItem.s, { included.add(Txt()) })
        }
        PairField(S.durationLbl.s, duration, { duration = it }, optional = true)
        ListRow(S.openForBooking.s, Modifier.clip(RoundedCornerShape(14.dp)).background(CC.bg2), sub = S.openForBookingSub.s, icon = CcIcons.Eye,
            trailing = { Toggle(active, { active = it }, S.openForBooking.s) }, padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp))
    }

    if (pictureSheet) PictureSheet(onDismiss = { pictureSheet = false }, onKey = { k -> imageKey = k; imagePath = ""; photo = null; pictureSheet = false }, onPhoto = { p -> photo = p; pictureSheet = false })
    if (confirmDelete && original != null) CcDialog({ confirmDelete = false }) {
        IconTile(CcIcons.Trash2, CC.dangerTint, CC.danger, 60.dp, 28.dp, RoundedCornerShape(18.dp))
        T(S.deleteServiceTitle.s, type.titleM, align = TextAlign.Center)
        T(S.deleteServiceBody.s, type.bodyM, CC.text2, align = TextAlign.Center)
        Btn(S.delete.s, { act({ Graph.cloud.deleteService(original) }) { confirmDelete = false; onBack() } }, Modifier.fillMaxWidth(), BtnStyle.Danger, enabled = !busy)
        Btn(S.cancel.s, { confirmDelete = false }, Modifier.fillMaxWidth(), BtnStyle.Light)
    }
}

private fun Txt.trimmed() = Txt(en.trim(), ur.trim())

/** English + Urdu pair of inputs (customers see their language). */
@Composable
private fun PairField(label: String, value: Txt, onChange: (Txt) -> Unit, multiline: Boolean = false, optional: Boolean = false, error: Boolean = false) =
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) { T(label, type.labelM); if (optional) T(S.optional.s, type.caption, CC.text3) }
        val shape = RoundedCornerShape(12.dp)
        for (urdu in listOf(false, true)) {
            val v = if (urdu) value.ur else value.en
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
            Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = if (multiline) 62.dp else 46.dp).clip(shape).background(CC.surface)
                .border(if (error && v.isBlank()) 1.5.dp else 1.dp, if (error && v.isBlank()) CC.danger else CC.border, shape).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically) {
                // EN tag on the left, اردو tag on the right (where each language starts), whatever the app language is.
                if (!urdu) Pill("EN", bg = CC.surface2, color = CC.text2, padding = PaddingValues(horizontal = 6.dp, vertical = 2.dp))
                Box(Modifier.weight(1f)) { LangInput(v, { onChange(if (urdu) value.copy(ur = it) else value.copy(en = it)) }, urdu, multiline = multiline) }
                if (urdu) Pill("اردو", bg = CC.surface2, color = CC.text2, padding = PaddingValues(horizontal = 6.dp, vertical = 2.dp))
            }
            }
        }
        if (error) Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) { Ic(CcIcons.CircleAlert, 14.dp, CC.danger); T(S.errBothLanguages.s, type.caption, CC.danger) }
    }

/** One text input in a fixed language: Urdu uses the Nastaliq font and right-to-left text whatever the app language is. */
@Composable
private fun LangInput(value: String, onChange: (String) -> Unit, urdu: Boolean, multiline: Boolean = false, compact: Boolean = false) {
    val both = LocalBothTypes.current
    val base = if (urdu) (if (compact) both.ur.bodyS else both.ur.bodyM) else (if (compact) both.en.bodyS else both.en.bodyM)
    val style = base.copy(color = CC.text, textDirection = if (urdu) TextDirection.Rtl else TextDirection.Ltr, textAlign = if (urdu) TextAlign.Right else TextAlign.Left)
    BasicTextField(value, onChange, Modifier.fillMaxWidth().padding(vertical = if (compact) 2.dp else 4.dp), textStyle = style, singleLine = !multiline, minLines = 1, maxLines = if (multiline) 5 else 1,
        cursorBrush = SolidColor(CC.primary), keyboardOptions = KeyboardOptions(capitalization = if (urdu) KeyboardCapitalization.None else KeyboardCapitalization.Sentences))
}

/** Pick one of the app's pictures or upload a photo from the phone. */
@Composable
private fun PictureSheet(onDismiss: () -> Unit, onKey: (String) -> Unit, onPhoto: (String) -> Unit) {
    val picker = rememberPhotoPicker(1) { paths -> paths.firstOrNull()?.let(onPhoto) }
    Sheet(S.choosePicture.s, onDismiss) {
        Btn(S.uploadPhoto.s, picker.gallery, Modifier.fillMaxWidth(), BtnStyle.Soft, lead = CcIcons.ImageUp)
        T(S.appPictures.s, type.labelM, CC.text2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in Catalog.images.keys.toList().chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (k in row) Img(Catalog.images.getValue(k), Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(12.dp)).background(CC.tile).tap { onKey(k) },
                    if (k in Catalog.fitImages) ContentScale.Fit else ContentScale.Crop)
                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

// ------------------------------------------------------------------ 76 admin settings

@Composable
fun AdminSettings(onLanguage: () -> Unit, onCustomerView: () -> Unit) {
    val biz by Graph.cloud.business.collectAsState()
    val admins by Graph.cloud.adminPhones.collectAsState()
    val profile by Graph.cloud.profile.collectAsState()
    val alerts = (profile as? Live.Value)?.value?.adminAlerts ?: true
    var edit by remember { mutableStateOf<String?>(null) }
    var logout by remember { mutableStateOf(false) }
    var loggingOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Screen(
        top = { Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)) { T(S.settings.s, type.titleL) } },
        gap = 14.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
    ) {
        Group(S.business.s) {
            ListRow(S.phoneForCalls.s, icon = CcIcons.Phone, titleStyle = type.bodyL, trailing = { T(biz.phoneDisplay, type.labelM, CC.text2, latin = true) }, chevron = true, onClick = { edit = "phone" })
            HDivider()
            ListRow(S.whatsappNumber.s, icon = CcIcons.Whatsapp, titleStyle = type.bodyL, trailing = { T(com.coolingcare.app.feature.auth.prettyE164(biz.whatsapp), type.labelM, CC.text2, latin = true) }, chevron = true, onClick = { edit = "whatsapp" })
            HDivider()
            ListRow(S.workingHours.s, sub = biz.hours.s, icon = CcIcons.Clock, titleStyle = type.bodyL, chevron = true, onClick = { edit = "hours" })
        }
        T(S.shownToCustomers.s, type.caption, CC.text3)
        Group(S.admins.s) {
            ListRow(S.adminNumbers.s, sub = admins.joinToString("\n") { com.coolingcare.app.feature.auth.prettyE164(it) }, icon = CcIcons.ShieldUser, titleStyle = type.bodyL,
                trailing = { Pill(S.nAdmins(admins.size).s, bg = CC.surface2, color = CC.text2) })
            Note(CcIcons.Info, S.adminsNote.s, Modifier.padding(bottom = 10.dp), bg = CC.bg2, tint = CC.text2)
        }
        Group(S.app.s) {
            ListRow(S.language.s, icon = CcIcons.Languages, titleStyle = type.bodyL, trailing = { T(S.langName.s, type.labelM, CC.primary) }, chevron = true, onClick = onLanguage)
            HDivider()
            ListRow(S.switchCustomer.s, icon = CcIcons.Smartphone, titleStyle = type.bodyL, chevron = true, onClick = onCustomerView)
            HDivider()
            ListRow(S.newOrderAlerts.s, icon = CcIcons.BellRing, titleStyle = type.bodyL, trailing = { Toggle(alerts, { Graph.cloud.setAdminAlerts(it) }, S.newOrderAlerts.s) })
        }
        ListRow(S.logOut.s, Modifier.card(16.dp, elevated = false), icon = CcIcons.LogOut, iconBg = CC.dangerTint, iconTint = CC.danger, titleColor = CC.danger,
            onClick = { logout = true }, padding = PaddingValues(horizontal = 14.dp, vertical = 10.dp))
    }
    when (edit) {
        "phone" -> NumberDialog(S.phoneForCalls.s, biz.phoneDisplay, { edit = null }) { n -> biz.copy(phoneDisplay = Phone.display(n), phoneDial = Phone.e164(n)!!) }
        "whatsapp" -> NumberDialog(S.whatsappNumber.s, biz.whatsapp, { edit = null }) { n -> biz.copy(whatsapp = Phone.waDigits(n)!!) }
        "hours" -> HoursDialog(biz) { edit = null }
    }
    if (logout) AccountDialog("logout", loggingOut, { logout = false }) {
        loggingOut = true
        scope.launch { runCatching { Graph.cloud.signOut() }.onFailure { Toasts.error(it) }; loggingOut = false; logout = false }
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    T(title.uppercase(), type.labelM, CC.text3)
    Column(Modifier.fillMaxWidth().card(16.dp, elevated = false).padding(horizontal = 14.dp, vertical = 2.dp)) { content() }
}

/** Edits one shop number; [apply] builds the new business doc from a valid Pakistani mobile. */
@Composable
private fun NumberDialog(title: String, current: String, onDismiss: () -> Unit, apply: (String) -> Business) {
    var v by remember { mutableStateOf(Phone.display(current)) }
    var tried by remember { mutableStateOf(false) }
    val (busy, act) = rememberAction()
    CcDialog(onDismiss) {
        T(title, type.titleM, align = TextAlign.Center)
        Field(v, { v = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(16) }, placeholder = "03XX XXXXXXX", icon = CcIcons.Phone, latin = true,
            keyboard = androidx.compose.ui.text.input.KeyboardType.Phone, error = if (tried && !Phone.isValid(v)) S.errPhone.s else null)
        Btn(S.save.s, { tried = true; if (Phone.isValid(v)) act({ Graph.cloud.saveBusiness(apply(v)) }) { Toasts.show(S.saved); onDismiss() } }, Modifier.fillMaxWidth(), enabled = !busy)
        Btn(S.cancel.s, onDismiss, Modifier.fillMaxWidth(), BtnStyle.Light)
    }
}

@Composable
private fun HoursDialog(biz: Business, onDismiss: () -> Unit) {
    var hours by remember { mutableStateOf(biz.hours) }
    var note by remember { mutableStateOf(biz.hoursNote) }
    val (busy, act) = rememberAction()
    CcDialog(onDismiss) {
        T(S.workingHours.s, type.titleM, align = TextAlign.Center)
        PairField(S.workingHours.s, hours, { hours = it })
        PairField(S.hoursNoteLbl.s, note, { note = it }, optional = true)
        Btn(S.save.s, { act({ Graph.cloud.saveBusiness(biz.copy(hours = hours.trimmed(), hoursNote = note.trimmed())) }) { Toasts.show(S.saved); onDismiss() } }, Modifier.fillMaxWidth(), enabled = !busy && !hours.isBlank)
        Btn(S.cancel.s, onDismiss, Modifier.fillMaxWidth(), BtnStyle.Light)
    }
}
