package com.coolingcare.app.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.coolingcare.app.model.Lang
import com.coolingcare.app.res.Res
import com.coolingcare.app.res.inter_bold
import com.coolingcare.app.res.inter_medium
import com.coolingcare.app.res.inter_regular
import com.coolingcare.app.res.inter_semibold
import com.coolingcare.app.res.nastaliq_bold
import com.coolingcare.app.res.nastaliq_regular
import com.coolingcare.app.res.nastaliq_semibold
import com.coolingcare.app.res.poppins_bold
import com.coolingcare.app.res.poppins_medium
import com.coolingcare.app.res.poppins_regular
import com.coolingcare.app.res.poppins_semibold
import org.jetbrains.compose.resources.Font

/** Colour tokens — same names and values as the Figma "Color" variables. */
object CC {
    val primary = Color(0xFF0A66C8)
    val primaryDark = Color(0xFF0852A3)
    val primaryBright = Color(0xFF1E88E5)
    val primaryTint = Color(0xFFEAF3FC)
    val primarySoft = Color(0xFFD3E5F8)
    val navy = Color(0xFF0B2A5B)
    val text = Color(0xFF0B1F45)
    val text2 = Color(0xFF4E5566)
    val text3 = Color(0xFF6B7280)
    val placeholder = Color(0xFF8A919C)
    val border = Color(0xFFE2E8F0)
    val divider = Color(0xFFEEF2F6)
    val bg = Color(0xFFFFFFFF)
    val bg2 = Color(0xFFF4F8FC)
    val surface = Color(0xFFFFFFFF)
    val surface2 = Color(0xFFF1F5F9)
    val tile = Color(0xFFEEF5FB)
    val onPrimary = Color(0xFFFFFFFF)
    val green = Color(0xFF0C8446)
    val greenDark = Color(0xFF0A6B39)
    val greenTint = Color(0xFFE2F4E9)
    val greenText = Color(0xFF1E6B43)
    val greenBorder = Color(0xFFBFE3CC)
    val orange = Color(0xFFC9470C)
    val orangeTint = Color(0xFFFDEDDF)
    val orangeText = Color(0xFFB4441C)
    val orangeBorder = Color(0xFFF8D2B4)
    val danger = Color(0xFFC62828)
    val dangerTint = Color(0xFFFDECEC)
    val warning = Color(0xFFB45309)
    val warningTint = Color(0xFFFEF3C7)
    val star = Color(0xFFF5A524)
    val scrim = Color(0x730B1F45)

    val brand get() = Brush.linearGradient(listOf(Color(0xFF0A66C8), Color(0xFF0B3D91)))
    val splash get() = Brush.verticalGradient(0f to Color(0xFF1E88E5), 0.45f to Color(0xFF0A66C8), 1f to Color(0xFF0B2A5B))
    fun hero(rtl: Boolean) = Brush.horizontalGradient(if (rtl) listOf(Color(0xFFF6FAFF), Color(0xFFE3F0FD)) else listOf(Color(0xFFE3F0FD), Color(0xFFF6FAFF)))
}

/** Type scale from Figma. English: Poppins (display/titles) + Inter (UI). Urdu: Noto Nastaliq Urdu with tall line heights. */
@Immutable
data class CcType(
    val displayL: TextStyle, val displayM: TextStyle, val titleL: TextStyle, val titleM: TextStyle, val titleS: TextStyle,
    val bodyL: TextStyle, val bodyM: TextStyle, val bodyS: TextStyle, val button: TextStyle, val labelM: TextStyle,
    val labelS: TextStyle, val caption: TextStyle, val logoXL: TextStyle, val logoM: TextStyle,
    /** Latin-only text (brand name, booking ids, phone numbers) keeps the English fonts in Urdu screens. */
    val latin: CcType? = null,
)

val LocalType = staticCompositionLocalOf<CcType> { error("No CcType") }
val LocalLang = staticCompositionLocalOf { Lang.EN }

/** Both scales, for the bilingual language picker. */
class BothTypes(val en: CcType, val ur: CcType)
val LocalBothTypes = staticCompositionLocalOf<BothTypes> { error("No types") }

private val lhs = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)

private fun st(f: FontFamily, w: FontWeight, size: Int, lh: Number, ls: TextUnit = 0.sp) =
    TextStyle(fontFamily = f, fontWeight = w, fontSize = size.sp, lineHeight = lh.toFloat().sp, letterSpacing = ls, lineHeightStyle = lhs)

private fun stF(f: FontFamily, w: FontWeight, size: Float, lh: Number) =
    TextStyle(fontFamily = f, fontWeight = w, fontSize = size.sp, lineHeight = lh.toFloat().sp, lineHeightStyle = lhs)

@Composable
private fun englishType(): CcType {
    val poppins = FontFamily(
        Font(Res.font.poppins_regular, FontWeight.Normal), Font(Res.font.poppins_medium, FontWeight.Medium),
        Font(Res.font.poppins_semibold, FontWeight.SemiBold), Font(Res.font.poppins_bold, FontWeight.Bold),
    )
    val inter = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal), Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold), Font(Res.font.inter_bold, FontWeight.Bold),
    )
    return CcType(
        displayL = st(poppins, FontWeight.Bold, 26, 34, (-0.01).em), displayM = st(poppins, FontWeight.Bold, 22, 29, (-0.01).em),
        titleL = st(poppins, FontWeight.SemiBold, 20, 28), titleM = st(poppins, FontWeight.SemiBold, 17, 24),
        titleS = st(inter, FontWeight.SemiBold, 15, 20), bodyL = st(inter, FontWeight.Normal, 15, 22),
        bodyM = st(inter, FontWeight.Normal, 14, 20), bodyS = st(inter, FontWeight.Normal, 13, 18),
        button = st(inter, FontWeight.SemiBold, 15, 20), labelM = st(inter, FontWeight.SemiBold, 13, 18),
        labelS = st(inter, FontWeight.SemiBold, 11, 14), caption = st(inter, FontWeight.Medium, 11, 14),
        logoXL = st(poppins, FontWeight.Bold, 34, 40, (-0.01).em), logoM = st(poppins, FontWeight.Bold, 20, 26),
    )
}

@Composable
private fun urduType(latin: CcType): CcType {
    val n = FontFamily(
        Font(Res.font.nastaliq_regular, FontWeight.Normal), Font(Res.font.nastaliq_semibold, FontWeight.SemiBold),
        Font(Res.font.nastaliq_bold, FontWeight.Bold),
    )
    return CcType(
        displayL = stF(n, FontWeight.Bold, 22f, 46), displayM = stF(n, FontWeight.Bold, 19f, 40), titleL = stF(n, FontWeight.Bold, 17f, 36),
        titleM = stF(n, FontWeight.SemiBold, 15f, 32), titleS = stF(n, FontWeight.SemiBold, 13.5f, 29), bodyL = stF(n, FontWeight.Normal, 13.5f, 29),
        bodyM = stF(n, FontWeight.Normal, 12.5f, 27), bodyS = stF(n, FontWeight.Normal, 11.5f, 25), button = stF(n, FontWeight.SemiBold, 13.5f, 28),
        labelM = stF(n, FontWeight.SemiBold, 12f, 25), labelS = stF(n, FontWeight.SemiBold, 10.5f, 22), caption = stF(n, FontWeight.Normal, 10.5f, 22),
        logoXL = stF(n, FontWeight.Bold, 28f, 56), logoM = stF(n, FontWeight.Bold, 17f, 36), latin = latin,
    )
}

@Composable
fun CcTheme(lang: Lang, content: @Composable () -> Unit) {
    val en = englishType()
    val ur = urduType(en)
    val type = if (lang == Lang.UR) ur else en
    val scheme = lightColorScheme(
        primary = CC.primary, onPrimary = CC.onPrimary, primaryContainer = CC.primaryTint, onPrimaryContainer = CC.primary,
        secondary = CC.green, onSecondary = CC.onPrimary, background = CC.bg, onBackground = CC.text, surface = CC.surface,
        onSurface = CC.text, onSurfaceVariant = CC.text2, outline = CC.border, outlineVariant = CC.divider, error = CC.danger,
        surfaceContainerLow = CC.surface, surfaceContainer = CC.surface, surfaceContainerHigh = CC.surface,
    )
    CompositionLocalProvider(
        LocalType provides type, LocalLang provides lang, LocalBothTypes provides BothTypes(en, ur),
        LocalLayoutDirection provides if (lang == Lang.UR) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalTextSelectionColors provides TextSelectionColors(CC.primary, CC.primarySoft),
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
