package com.coolingcare.app.feature.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import com.coolingcare.app.ui.theme.CcType
import com.coolingcare.app.ui.theme.LocalBothTypes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Graph
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Lang
import com.coolingcare.app.res.Res
import com.coolingcare.app.res.img_welcome_scene
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.Centered
import com.coolingcare.app.ui.components.FeatureLine
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.GUTTER
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.Img
import com.coolingcare.app.ui.components.LogoMark
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.RadioDot
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.card
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** 01 · Splash — brand moment while the login, profile and admin role load (max ~4 s offline), then routes. */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        if (Graph.repo.prefs.value.onboarded) withTimeoutOrNull(4000) { Graph.cloud.ready.first { it } }
        onDone()
    }
    Box(Modifier.fillMaxSize().background(CC.splash)) {
        for ((x, y, sz, a) in listOf(Quad(-70, 520, 280, 0.07f), Quad(250, 70, 190, 0.07f), Quad(270, 640, 120, 0.09f)))
            Icon(CcIcons.Snowflake, null, Modifier.offset(x.dp, y.dp).size(sz.dp).alpha(a), tint = Color.White)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
                LogoMark(104.dp, onDark = true)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T(S.appName.s, type.logoXL, CC.onPrimary, align = TextAlign.Center, latin = true)
                    T(S.tagline.s, type.bodyL, CC.onPrimary.copy(alpha = 0.88f), align = TextAlign.Center)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { for (a in listOf(1f, 0.6f, 0.3f)) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White.copy(alpha = a))) }
            Spacer(Modifier.height(14.dp))
            T(S.servingArea.s, type.caption, CC.onPrimary.copy(alpha = 0.8f), align = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
        }
    }
}

private data class Quad(val a: Int, val b: Int, val c: Int, val d: Float)

/** 02 · Choose Language — bilingual on purpose; switches strings + layout direction instantly. */
@Composable
fun LanguageScreen(onContinue: () -> Unit) {
    val prefs by Graph.repo.prefs.collectAsState()
    val lang = prefs.lang ?: Lang.EN
    val both = LocalBothTypes.current
    Column(Modifier.fillMaxSize().background(CC.bg).statusBarsPadding()) {
        Centered(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = GUTTER.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(top = 44.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    LogoMark(72.dp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(S.chooseLanguageEn, color = CC.text, style = both.en.titleL, textAlign = TextAlign.Center)
                        Text(S.chooseLanguageUr, color = CC.text2, style = both.ur.titleM, textAlign = TextAlign.Center)
                    }
                }
                LangOption("Aa", "English", "Continue in English", lang == Lang.EN, both.en) { Graph.repo.setLang(Lang.EN) }
                LangOption("اُ", "اردو", "اردو میں جاری رکھیں", lang == Lang.UR, both.ur) { Graph.repo.setLang(Lang.UR) }
                Note(CcIcons.Languages, S.langNote.s, bg = CC.bg2, tint = CC.text2)
            }
        }
        Footer { Btn(S.cont.s, { if (prefs.lang == null) Graph.repo.setLang(Lang.EN); onContinue() }, Modifier.fillMaxWidth(), arrow = true) }
    }
}

@Composable
private fun LangOption(badge: String, title: String, sub: String, selected: Boolean, t: CcType, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().then(if (selected) Modifier.clip(shape).background(CC.primaryTint).border(2.dp, CC.primary, shape) else Modifier.card(18.dp))
            .tap(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(if (selected) CC.primary else CC.surface2), contentAlignment = Alignment.Center) {
            Text(badge, color = if (selected) CC.onPrimary else CC.text, style = t.titleL)
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = CC.text, style = t.titleM)
            Text(sub, color = CC.text2, style = t.bodyS)
        }
        RadioDot(selected)
    }
}

/** 03 · Welcome — brand intro from the client poster. */
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().background(CC.bg)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(380.dp)) {
                Img(Res.drawable.img_welcome_scene, Modifier.fillMaxSize(), ContentScale.Crop, Alignment.TopCenter)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 0.62f to Color.Transparent, 1f to Color.White)))
            }
            Centered {
                Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        T(S.appName.s, type.displayL, CC.navy, latin = true)
                        T(S.tagline.s, type.titleM, CC.text2)
                        Box(Modifier.size(44.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(CC.primaryBright))
                    }
                    FeatureLine(CcIcons.Snowflake, S.feat1.s); FeatureLine(CcIcons.ShieldCheck, S.feat2.s)
                    FeatureLine(CcIcons.Settings, S.feat3.s); FeatureLine(CcIcons.MapPin, S.feat4.s)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        Footer { Btn(S.getStarted.s, onStart, Modifier.fillMaxWidth(), arrow = true) }
    }
}

@Composable
fun Dot(on: Boolean) = Box(Modifier.width(if (on) 20.dp else 8.dp).height(8.dp).clip(CircleShape).background(if (on) CC.primary else CC.border))

@Composable
fun MiniIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) = Ic(icon, 18.dp, CC.primary)
