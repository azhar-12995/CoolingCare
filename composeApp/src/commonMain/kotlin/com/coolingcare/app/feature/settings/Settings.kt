package com.coolingcare.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.domain.addressLine
import com.coolingcare.app.feature.home.AreaSheet
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.platform.appVersion
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.BtnStyle
import com.coolingcare.app.ui.components.CcDialog
import com.coolingcare.app.ui.components.Field
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.HDivider
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.InfoLine
import com.coolingcare.app.ui.components.ListRow
import com.coolingcare.app.ui.components.LogoMark
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.SelectField
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.Toggle
import com.coolingcare.app.ui.components.Toasts
import com.coolingcare.app.ui.components.Pill
import com.coolingcare.app.data.Session
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalLang

@Composable
private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    T(title.uppercase(), type.labelM, CC.text3)
    Column(Modifier.fillMaxWidth().card(16.dp, elevated = false).padding(horizontal = 14.dp, vertical = 2.dp), content = content)
}

/** 53 · Settings. Admins also get a way back to the dashboard. */
@Composable
fun SettingsScreen(onBack: () -> Unit, onDetails: () -> Unit, onLanguage: () -> Unit, onAbout: () -> Unit, onPrivacy: () -> Unit, onAdmin: () -> Unit) {
    val prefs by Graph.repo.prefs.collectAsState()
    val profile by Graph.repo.profile.collectAsState()
    val session by Graph.cloud.session.collectAsState()
    val isAdmin by Graph.cloud.isAdmin.collectAsState()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var confirm by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val phone = (session as? Session.SignedIn)?.phone ?: profile.phone
    Screen(top = { AppBar(S.settings.s, onBack) }, gap = 14.dp) {
        if (isAdmin) ListRow(S.adminDashboard.s, Modifier.clip(RoundedCornerShape(16.dp)).background(CC.navy), sub = S.adminDashboardSub.s, icon = CcIcons.LayoutDashboard,
            iconBg = CC.surface, titleColor = CC.onPrimary, chevron = true, onClick = onAdmin, padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CC.primaryTint).tap(onClick = onDetails).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val initials = profile.name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
            Box(Modifier.size(50.dp).clip(CircleShape).background(CC.primary), contentAlignment = Alignment.Center) {
                if (initials.isBlank()) Ic(CcIcons.User, 24.dp, CC.onPrimary) else T(initials, type.titleM, CC.onPrimary)
            }
            Column(Modifier.weight(1f)) {
                if (profile.name.isBlank()) { T(S.addYourDetails.s, type.titleS); T(S.addYourDetailsSub.s, type.bodyS, CC.text2) }
                else { T(profile.name, type.titleS); T(Phone.display(phone), type.bodyS, CC.text2, latin = true); if (profile.house.isNotBlank()) T(profile.addressLine(lang), type.caption, CC.text3, maxLines = 1) }
            }
            Ic(CcIcons.Pencil, 18.dp)
        }
        Group(S.preferences.s) {
            ListRow(S.language.s, icon = CcIcons.Languages, titleStyle = type.bodyL, trailing = { T(S.langName.s, type.labelM, CC.primary) }, chevron = true, onClick = onLanguage)
            HDivider()
            ListRow(S.visitReminders.s, icon = CcIcons.CalendarClock, titleStyle = type.bodyL, trailing = { Toggle(prefs.visitReminders, { v -> Graph.repo.updatePrefs { it.copy(visitReminders = v) } }, S.visitReminders.s) })
            HDivider()
            ListRow(S.maintenanceReminders.s, icon = CcIcons.BellRing, titleStyle = type.bodyL, trailing = { Toggle(prefs.maintenanceReminders, { v -> Graph.repo.updatePrefs { it.copy(maintenanceReminders = v) } }, S.maintenanceReminders.s) })
        }
        Group(S.about.s) {
            ListRow(S.aboutCC.s, icon = CcIcons.Info, titleStyle = type.bodyL, chevron = true, onClick = onAbout); HDivider()
            ListRow(S.privacy.s, icon = CcIcons.ShieldCheck, titleStyle = type.bodyL, chevron = true, onClick = onPrivacy); HDivider()
            ListRow(S.shareApp.s, icon = CcIcons.Share2, titleStyle = type.bodyL, chevron = true, onClick = { Graph.launcher.share(S.shareAppText(Graph.launcher.storeLink).get(lang)) }); HDivider()
            ListRow(S.rateApp.s, icon = CcIcons.Star, titleStyle = type.bodyL, chevron = true, onClick = { Graph.launcher.openStore() })
        }
        Column(Modifier.fillMaxWidth().card(16.dp, elevated = false).padding(horizontal = 14.dp, vertical = 2.dp)) {
            ListRow(S.logOut.s, icon = CcIcons.LogOut, iconBg = CC.surface2, iconTint = CC.text2, onClick = { confirm = "logout" }, padding = PaddingValues(vertical = 10.dp))
            HDivider()
            ListRow(S.deleteAccount.s, icon = CcIcons.UserX, iconBg = CC.dangerTint, iconTint = CC.danger, titleColor = CC.danger, onClick = { confirm = "delete" }, padding = PaddingValues(vertical = 10.dp))
        }
        T(S.version(appVersion).s, type.caption, CC.text3, Modifier.fillMaxWidth(), align = TextAlign.Center)
    }
    if (confirm != null) AccountDialog(confirm!!, busy, onDismiss = { confirm = null }) {
        busy = true
        scope.launch {
            try { if (confirm == "logout") Graph.cloud.signOut() else Graph.cloud.deleteAccount(); confirm = null }
            catch (e: Throwable) { if (e is kotlinx.coroutines.CancellationException) throw e; Toasts.error(e) }
            finally { busy = false }
        }
    }
}

/** Log out / delete account confirmation. After either, the app returns to Log in by itself (session listener). */
@Composable
fun AccountDialog(kind: String, busy: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) = CcDialog(onDismiss) {
    val delete = kind == "delete"
    IconTile(if (delete) CcIcons.UserX else CcIcons.LogOut, if (delete) CC.dangerTint else CC.surface2, if (delete) CC.danger else CC.text2, 60.dp, 28.dp, RoundedCornerShape(18.dp))
    T((if (delete) S.deleteTitle else S.logOutTitle).s, type.titleM, align = TextAlign.Center)
    T((if (delete) S.deleteBody else S.logOutBody).s, type.bodyM, CC.text2, align = TextAlign.Center)
    Btn(if (busy) S.loading.s else (if (delete) S.delete else S.logOut).s, onConfirm, Modifier.fillMaxWidth(), if (delete) BtnStyle.Danger else BtnStyle.Primary, enabled = !busy)
    Btn(S.cancel.s, onDismiss, Modifier.fillMaxWidth(), BtnStyle.Light)
}

/** 54 · My details — saved on the account and pre-filled in every booking. The login number cannot be changed here. */
@Composable
fun DetailsScreen(onBack: () -> Unit) {
    val p = Graph.repo.profile.value
    val session by Graph.cloud.session.collectAsState()
    var name by remember { mutableStateOf(p.name) }
    var area by remember { mutableStateOf(p.area) }
    var house by remember { mutableStateOf(p.house) }
    var landmark by remember { mutableStateOf(p.landmark) }
    var tried by remember { mutableStateOf(false) }
    var areaSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val phone = (session as? Session.SignedIn)?.phone ?: p.phone
    val ok = name.trim().length >= 2 && house.trim().length >= 3
    Screen(
        top = { AppBar(S.myDetails.s, onBack) },
        footer = { Footer { Btn(S.save.s, {
            tried = true
            if (ok) scope.launch {
                runCatching { Graph.cloud.saveProfile(Graph.repo.profile.value.copy(name = name, area = area, house = house, landmark = landmark)) }
                    .onSuccess { Toasts.show(S.saved); onBack() }.onFailure { Toasts.error(it) }
            }
        }, Modifier.fillMaxWidth()) } },
        gap = 14.dp,
    ) {
        Note(CcIcons.Lock, S.detailsPrivate.s)
        Field(name, { name = it }, label = S.fullName.s, placeholder = S.fullName.s, icon = CcIcons.User, capitalization = KeyboardCapitalization.Words,
            error = if (tried && name.trim().length < 2) S.errName.s else null)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            T(S.mobileNumber.s, type.labelM)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CC.bg2).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Ic(CcIcons.Phone, 18.dp, CC.text2)
                T(com.coolingcare.app.feature.auth.prettyE164(phone), type.bodyM, modifier = Modifier.weight(1f), latin = true)
                Pill(S.verifiedPill.s, icon = CcIcons.BadgeCheck, bg = CC.greenTint, color = CC.greenText)
            }
            T(S.loginNumberHelp.s, type.caption, CC.text3)
        }
        SelectField(Catalog.areas.getOrNull(area)?.s, S.area.s, { areaSheet = true }, label = S.area.s, icon = CcIcons.MapPin)
        Field(house, { house = it }, label = S.houseStreet.s, placeholder = S.housePh.s, icon = CcIcons.House, error = if (tried && house.trim().length < 3) S.errAddress.s else null)
        Field(landmark, { landmark = it }, label = S.landmark.s, optional = S.optional.s, placeholder = S.landmarkPh.s, icon = CcIcons.Navigation)
    }
    if (areaSheet) AreaSheet(area, { areaSheet = false }) { area = it; areaSheet = false }
}

/** 55 · About. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Screen(top = { AppBar(S.about.s, onBack) }, gap = 14.dp) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LogoMark(72.dp)
            T(S.appName.s, type.titleL, CC.navy, latin = true)
            T(S.tagline.s, type.bodyS, CC.text2)
        }
        T(S.aboutBody.s, type.bodyM, CC.text2, Modifier.fillMaxWidth(), align = TextAlign.Center)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CC.bg2).padding(horizontal = 14.dp, vertical = 6.dp)) {
            for ((icon, t) in listOf(CcIcons.Snowflake to S.feat1, CcIcons.ShieldCheck to S.feat2, CcIcons.Settings to S.feat3, CcIcons.MapPin to S.feat4))
                Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { Ic(icon, 18.dp); T(t.s, type.bodyM) }
        }
        T(S.howItWorks.s, type.titleS)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(CcIcons.Search, CcIcons.CalendarDays, CcIcons.CircleCheck, CcIcons.Receipt).forEachIndexed { i, icon ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconTile(icon, size = 44.dp, iconSize = 21.dp, shape = CircleShape)
                    T("${i + 1}", type.caption, CC.text3, latin = true)
                    T(S.steps[i].s, type.labelS, align = TextAlign.Center)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn(S.call.s, { Graph.launcher.dial() }, Modifier.weight(1f), BtnStyle.Light, lead = CcIcons.Phone, leadTint = CC.green, height = 46.dp)
            val lang = LocalLang.current
            Btn(S.whatsapp.s, { com.coolingcare.app.feature.status.messageShop(com.coolingcare.app.domain.Messages.support(null, lang)) }, Modifier.weight(1f), BtnStyle.Light, lead = CcIcons.Whatsapp, leadTint = CC.green, height = 46.dp)
        }
    }
}

/** 56 · Privacy policy (also host this text as a web page for the store listings). */
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Screen(top = { AppBar(S.privacyTitle.s, onBack) }, gap = 16.dp) {
        T(S.lastUpdated.s, type.caption, CC.text3)
        val icons = listOf(CcIcons.Smartphone, CcIcons.ClipboardList, CcIcons.Camera, CcIcons.Bell, CcIcons.Trash2)
        S.privacySections.forEachIndexed { i, (t, b) ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconTile(icons[i], size = 36.dp, iconSize = 18.dp, shape = RoundedCornerShape(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { T(t.s, type.titleS); T(b.s, type.bodyS, CC.text2) }
            }
        }
    }
}
