package com.coolingcare.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.data.Live
import com.coolingcare.app.data.Session
import com.coolingcare.app.data.problem
import com.coolingcare.app.domain.Phone
import com.coolingcare.app.feature.home.AreaSheet
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.platform.isAndroid
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.Field
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconTile
import com.coolingcare.app.ui.components.LogoMark
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.SelectField
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.TextBtn
import com.coolingcare.app.ui.components.Toasts
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.text
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** "+92 300 1234567" */
fun prettyE164(local: String): String = Phone.normalize(local)?.let { "+92 " + it.substring(1, 4) + " " + it.substring(4) } ?: local

/** 04 · Log in / sign up with the mobile number (Firebase Phone Auth). */
@Composable
fun LoginScreen(onBack: (() -> Unit)?, onCodeSent: (local: String, verificationId: String) -> Unit, onSignedIn: () -> Unit, onPrivacy: () -> Unit) {
    var number by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val lang = com.coolingcare.app.ui.theme.LocalLang.current
    fun send() {
        error = null
        if (!Phone.isValid(number)) { error = S.errInvalidNumber.get(lang); return }
        busy = true
        scope.launch {
            try {
                val id = Graph.cloud.sendCode(number)
                if (id == null) onSignedIn() else onCodeSent(Phone.normalize(number)!!, id)
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.problem().text().get(lang)
            } finally { busy = false }
        }
    }
    Screen(
        top = { AppBar("", onBack) },
        footer = {
            Footer {
                Btn(if (busy) S.sending.s else S.sendCode.s, ::send, Modifier.fillMaxWidth(), arrow = !busy, enabled = !busy)
                T(S.agreePrivacy.s, type.caption, CC.text3, Modifier.fillMaxWidth().tap(onClick = onPrivacy), align = TextAlign.Center)
            }
        },
        gap = 18.dp, bodyPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 8.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LogoMark(64.dp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                T(S.loginTitle.s, type.displayL)
                T(S.loginSub.s, type.bodyM, CC.text2)
            }
        }
        PhoneInput(number, { number = it; error = null }, error, onDone = ::send)
        Note(CcIcons.ShieldCheck, S.numberPrivacy.s)
    }
}

/** "+92 | 300 1234567" field. Accepts 300…, 0300… or +92300… (pasted). */
@Composable
private fun PhoneInput(value: String, onChange: (String) -> Unit, error: String?, onDone: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val focused by src.collectIsFocusedAsState()
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        T(S.mobileNumber.s, type.labelM)
        // Numbers are always written left-to-right, also on Urdu screens.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp)).background(CC.surface)
                    .border(if (focused || error != null) 1.5.dp else 1.dp, if (error != null) CC.danger else if (focused) CC.primary else CC.border, RoundedCornerShape(14.dp))
                    .padding(start = 6.dp, end = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.height(42.dp).clip(RoundedCornerShape(10.dp)).background(CC.primaryTint).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    T("+92", type.titleS, CC.primary, latin = true)
                }
                val style = (type.latin?.titleM ?: type.titleM).copy(color = CC.text, textDirection = TextDirection.Ltr)
                BasicTextField(
                    value, { v -> onChange(v.filter { it.isDigit() || it == ' ' || it == '+' }.take(16)) }, Modifier.weight(1f).focusRequester(focus),
                    textStyle = style, singleLine = true, cursorBrush = SolidColor(CC.primary), interactionSource = src,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onDone() }, onGo = { onDone() }),
                    decorationBox = { inner -> Box { if (value.isEmpty()) T("300 1234567", type.titleM, CC.placeholder, latin = true); inner() } },
                )
            }
        }
        if (error != null) Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) { Ic(CcIcons.CircleAlert, 14.dp, CC.danger); T(error, type.caption, CC.danger) }
        else T(S.codeHelp.s, type.caption, CC.text3)
    }
}

/** 05 · Enter the 6-digit SMS code. Android may finish by itself (auto-retrieval) — the session flips to signed in. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun VerifyScreen(local: String, firstVerificationId: String, onChangeNumber: () -> Unit, onVerified: () -> Unit) {
    var verificationId by rememberSaveable { mutableStateOf(firstVerificationId) }
    var code by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var left by rememberSaveable { mutableIntStateOf(60) }
    val scope = rememberCoroutineScope()
    val lang = com.coolingcare.app.ui.theme.LocalLang.current
    val session by Graph.cloud.session.collectAsState()

    LaunchedEffect(session) {
        if (session is Session.SignedIn) {
            busy = true
            withTimeoutOrNull(6000) { Graph.cloud.ready.first { it } }
            onVerified()
        }
    }
    LaunchedEffect(verificationId) { while (left > 0) { delay(1000); left-- } }

    fun verify() {
        if (code.length < 6 || busy) return
        error = null; busy = true
        scope.launch {
            try { Graph.cloud.verifyCode(verificationId, code) }
            catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.problem().text().get(lang); busy = false
            }
        }
    }
    fun resend() {
        error = null; code = ""
        scope.launch {
            try {
                val id = Graph.cloud.sendCode(local, resend = true)
                if (id != null) { verificationId = id; left = 60; Toasts.show(S.codeResent) }
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.problem().text().get(lang)
            }
        }
    }

    Screen(
        top = { AppBar("", onChangeNumber) },
        footer = { Footer { Btn(if (busy) S.verifying.s else S.verifyContinue.s, ::verify, Modifier.fillMaxWidth(), arrow = !busy, enabled = code.length == 6 && !busy) } },
        gap = 22.dp, bodyPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 8.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            IconTile(CcIcons.MessageSquareText, size = 56.dp, iconSize = 28.dp, shape = RoundedCornerShape(16.dp))
            T(S.verifyTitle.s, type.displayL)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.Center) {
                T(S.codeSentTo.s, type.bodyM, CC.text2, Modifier.align(Alignment.CenterVertically))
                T(prettyE164(local), type.titleS, latin = true, modifier = Modifier.align(Alignment.CenterVertically))
                TextBtn(S.change.s, onChangeNumber, Modifier.align(Alignment.CenterVertically))
            }
        }
        CodeBoxes(code, { c -> code = c; error = null; if (c.length == 6) verify() }, error != null)
        if (error != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Ic(CcIcons.CircleAlert, 16.dp, CC.danger); T(error!!, type.labelM, CC.danger)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            if (left > 0) { Ic(CcIcons.Clock, 16.dp, CC.text3); T(S.resendIn("0:" + left.toString().padStart(2, '0')).s, type.labelM, CC.text3) }
            else TextBtn(S.resendCode.s, ::resend)
        }
        Note(CcIcons.Smartphone, (if (isAndroid) S.autoFillAndroid else S.autoFillIos).s, bg = CC.bg2, tint = CC.text2)
    }
}

/** Six boxes drawn over one hidden text field (so the keyboard, paste and SMS autofill work normally). */
@Composable
private fun CodeBoxes(code: String, onChange: (String) -> Unit, error: Boolean) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BasicTextField(
            code, { v -> onChange(v.filter { it.isDigit() }.take(6)) }, Modifier.fillMaxWidth().focusRequester(focus),
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), cursorBrush = SolidColor(CC.surface),
            decorationBox = { _ ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                    for (i in 0 until 6) {
                        val d = code.getOrNull(i)?.toString(); val on = i == code.length
                        val stroke = when { error -> CC.danger; on -> CC.primary; d != null -> CC.text3; else -> CC.border }
                        Box(
                            Modifier.width(46.dp).height(56.dp).clip(RoundedCornerShape(12.dp)).background(if (on) CC.primaryTint else CC.surface)
                                .border(if (on || error) 2.dp else 1.dp, stroke, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (d != null) T(d, type.titleL, latin = true)
                            else if (on) Box(Modifier.size(2.dp, 24.dp).background(CC.primary))
                        }
                    }
                }
            },
        )
    }
}

/** 06 · Your details — only for new accounts (or when the profile was never completed). */
@Composable
fun YourDetailsScreen(onDone: () -> Unit) {
    val session by Graph.cloud.session.collectAsState()
    val existing = (Graph.cloud.profile.value as? Live.Value)?.value ?: Graph.repo.profile.value
    var name by rememberSaveable { mutableStateOf(existing.name) }
    var area by rememberSaveable { mutableIntStateOf(existing.area) }
    var house by rememberSaveable { mutableStateOf(existing.house) }
    var landmark by rememberSaveable { mutableStateOf(existing.landmark) }
    var tried by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var areaSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val nameOk = name.trim().length >= 2; val houseOk = house.trim().length >= 3
    val phone = (session as? Session.SignedIn)?.phone.orEmpty()
    Screen(
        top = { AppBar("", null) },
        footer = {
            Footer {
                Btn(if (busy) S.saving.s else S.saveContinue.s, {
                    tried = true
                    if (nameOk && houseOk && !busy) {
                        busy = true
                        scope.launch {
                            try { Graph.cloud.saveProfile(existing.copy(name = name, area = area, house = house, landmark = landmark)); onDone() }
                            catch (e: Throwable) { if (e is kotlinx.coroutines.CancellationException) throw e; Toasts.error(e) }
                            finally { busy = false }
                        }
                    }
                }, Modifier.fillMaxWidth(), arrow = !busy, enabled = !busy)
            }
        },
        gap = 14.dp, bodyPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 8.dp),
    ) {
        Column(Modifier.padding(bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            T(S.almostDone.s, type.displayL)
            T(S.almostDoneSub.s, type.bodyM, CC.text2)
        }
        if (phone.isNotBlank()) Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CC.greenTint).padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Ic(CcIcons.BadgeCheck, 20.dp, CC.green); T(S.numberVerified(prettyE164(phone)).s, type.labelM, CC.greenText)
        }
        Field(name, { name = it }, label = S.fullName.s, placeholder = S.fullName.s, icon = CcIcons.User, capitalization = KeyboardCapitalization.Words,
            error = if (tried && !nameOk) S.errName.s else null)
        SelectField(Catalog.areas.getOrNull(area)?.s, S.area.s, { areaSheet = true }, label = S.area.s, icon = CcIcons.MapPin)
        Field(house, { house = it }, label = S.houseStreet.s, placeholder = S.housePh.s, icon = CcIcons.House, error = if (tried && !houseOk) S.errAddress.s else null)
        Field(landmark, { landmark = it }, label = S.landmark.s, optional = S.optional.s, placeholder = S.landmarkPh.s, icon = CcIcons.Navigation)
    }
    if (areaSheet) AreaSheet(area, { areaSheet = false }) { area = it; areaSheet = false }
}
