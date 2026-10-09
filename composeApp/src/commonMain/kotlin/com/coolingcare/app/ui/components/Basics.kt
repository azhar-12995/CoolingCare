package com.coolingcare.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.icons.CcIconsBold
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.CcType
import com.coolingcare.app.ui.theme.LocalType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

val type: CcType @Composable get() = LocalType.current
val isRtl: Boolean @Composable get() = LocalLayoutDirection.current == LayoutDirection.Rtl

/** Text with the design type scale. [latin] keeps English fonts for brand names, ids and phone numbers inside Urdu screens. */
@Composable
fun T(
    text: String, style: TextStyle = type.bodyM, color: Color = CC.text, modifier: Modifier = Modifier,
    align: TextAlign? = null, maxLines: Int = Int.MAX_VALUE, latin: Boolean = false,
) {
    // Latin runs (phone numbers, ids, brand, names typed in English) use the English fonts and LTR inside Urdu screens,
    // so digits never reorder and Latin letters are not drawn with the Nastaliq font's fallback glyphs.
    val useLatin = latin || (type.latin != null && isLatinText(text))
    val st = if (useLatin) (type.latin?.let { pickLatin(style, it) } ?: style).copy(textDirection = TextDirection.Ltr) else style
    Text(text, modifier, color = color, style = st, textAlign = align, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun pickLatin(style: TextStyle, l: CcType): TextStyle {
    val u = type
    return when (style) {
        u.displayL -> l.displayL; u.displayM -> l.displayM; u.titleL -> l.titleL; u.titleM -> l.titleM; u.titleS -> l.titleS
        u.bodyL -> l.bodyL; u.bodyS -> l.bodyS; u.button -> l.button; u.labelM -> l.labelM; u.labelS -> l.labelS
        u.caption -> l.caption; u.logoXL -> l.logoXL; u.logoM -> l.logoM; else -> l.bodyM
    }
}

/** True for text with letters/digits but no Arabic-script characters. */
fun isLatinText(s: String) = s.any { it.isLetterOrDigit() } && s.none { it in '\u0600'..'\u06FF' }

private val directional = setOf(CcIcons.ArrowLeft, CcIcons.ArrowRight, CcIcons.ChevronLeft, CcIcons.ChevronRight)

/** Icon that flips arrows / chevrons in right-to-left layouts. */
@Composable
fun Ic(icon: ImageVector, size: Dp = 20.dp, tint: Color = CC.primary, modifier: Modifier = Modifier, description: String? = null) {
    val flip = isRtl && icon in directional
    Icon(icon, description, modifier.size(size).then(if (flip) Modifier.scale(scaleX = -1f, scaleY = 1f) else Modifier), tint = tint)
}

@Composable
fun Img(res: DrawableResource, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Crop, alignment: Alignment = Alignment.Center, description: String? = null) =
    Image(painterResource(res), description, modifier, contentScale = scale, alignment = alignment)

fun Modifier.tap(enabled: Boolean = true, role: Role = Role.Button, onClick: () -> Unit): Modifier = this.clickable(enabled = enabled, role = role, onClick = onClick)

enum class BtnStyle(val fill: Color?, val stroke: Color?, val content: Color, val shadow: Boolean = false) {
    Primary(CC.primary, null, CC.onPrimary, true), Secondary(null, CC.primary, CC.primary), Soft(CC.primaryTint, null, CC.primary),
    Light(CC.surface, CC.border, CC.text), Ghost(null, null, CC.primary), Green(CC.green, null, CC.onPrimary),
    GreenOutline(CC.surface, CC.green, CC.green), Orange(CC.orange, null, CC.onPrimary), Danger(CC.danger, null, CC.onPrimary),
    DangerSoft(CC.dangerTint, null, CC.danger),
}

@Composable
fun Btn(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier, style: BtnStyle = BtnStyle.Primary,
    lead: ImageVector? = null, leadTint: Color? = null, arrow: Boolean = false, sub: String? = null,
    height: Dp = 50.dp, small: Boolean = false, enabled: Boolean = true, flat: Boolean = false, px: Dp = 20.dp, subLatin: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)
    val content = if (enabled) style.content else style.content.copy(alpha = 0.6f)
    Row(
        modifier
            .then(if (style.shadow && !flat && enabled) Modifier.shadow(10.dp, shape, ambientColor = CC.primary.copy(0.3f), spotColor = CC.primary.copy(0.35f)) else Modifier)
            .clip(shape)
            .background(style.fill?.let { if (enabled) it else it.copy(alpha = 0.55f) } ?: Color.Transparent)
            .then(if (style.stroke != null) Modifier.border(1.5.dp, style.stroke, shape) else Modifier)
            .tap(enabled, onClick = onClick)
            .defaultMinSize(minHeight = height)
            .padding(horizontal = px),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (lead != null) Ic(lead, if (small) 18.dp else 20.dp, leadTint ?: content)
        if (sub == null) T(label, if (small) type.labelM else type.button, content, maxLines = 1)
        else Column { T(label, if (small) type.labelM else type.button, content, maxLines = 1); T(sub, type.caption, content.copy(alpha = 0.92f), maxLines = 1, latin = subLatin) }
        if (arrow) Ic(CcIcons.ArrowRight, 18.dp, content)
    }
}

@Composable
fun TextBtn(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = CC.primary, style: TextStyle = type.labelM) =
    Box(modifier.clip(RoundedCornerShape(8.dp)).tap(onClick = onClick).padding(horizontal = 6.dp, vertical = 6.dp)) { T(label, style, color, maxLines = 1) }

@Composable
fun IconBtn(icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = CC.text, bg: Color? = null, size: Dp = 40.dp, iconSize: Dp = 22.dp, description: String? = null, badge: Boolean = false) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(Modifier.size(size).clip(CircleShape).background(bg ?: Color.Transparent).tap(onClick = onClick).semantics { if (description != null) contentDescription = description }, contentAlignment = Alignment.Center) {
            Ic(icon, iconSize, tint)
        }
        if (badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 9.dp).size(10.dp).clip(CircleShape).background(CC.surface).padding(1.5.dp).clip(CircleShape).background(CC.orange))
    }
}

@Composable
fun IconTile(icon: ImageVector, bg: Color = CC.primaryTint, tint: Color = CC.primary, size: Dp = 40.dp, iconSize: Dp = size * 0.5f, shape: Shape = RoundedCornerShape(12.dp), modifier: Modifier = Modifier) =
    Box(modifier.size(size).clip(shape).background(bg), contentAlignment = Alignment.Center) { Ic(icon, iconSize, tint) }

@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp = 36.dp) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier.then(if (selected) Modifier.shadow(6.dp, shape, ambientColor = CC.primary.copy(0.3f), spotColor = CC.primary.copy(0.3f)) else Modifier)
            .clip(shape).background(if (selected) CC.primary else CC.primaryTint).tap(onClick = onClick).height(height).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) { T(label, type.labelM, if (selected) CC.onPrimary else CC.text, maxLines = 1) }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, bg: Color = CC.primaryTint, color: Color = CC.primary, radius: Dp = 999.dp, padding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp), latin: Boolean = false) =
    Row(modifier.clip(RoundedCornerShape(radius)).background(bg).padding(padding), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) Ic(icon, 13.dp, color)
        T(text, type.labelS, color, maxLines = 1, latin = latin)
    }

@Composable
fun StatusBadge(text: String, bg: Color, color: Color, icon: ImageVector? = CcIcons.CircleCheck) =
    Pill(text, icon = icon, bg = bg, color = color, padding = PaddingValues(horizontal = 8.dp, vertical = 3.dp))

@Composable
fun RadioDot(selected: Boolean) = Box(
    Modifier.size(22.dp).clip(CircleShape).background(CC.surface).border(if (selected) 2.dp else 1.5.dp, if (selected) CC.primary else CC.placeholder, CircleShape),
    contentAlignment = Alignment.Center,
) { if (selected) Box(Modifier.size(11.dp).clip(CircleShape).background(CC.primary)) }

@Composable
fun Toggle(on: Boolean, onChange: (Boolean) -> Unit, description: String? = null) {
    val src = remember { MutableInteractionSource() }
    Box(
        Modifier.size(46.dp, 28.dp).clip(RoundedCornerShape(14.dp)).background(if (on) CC.primary else CC.border)
            .clickable(src, ripple(), role = Role.Switch) { onChange(!on) }.semantics { if (description != null) contentDescription = description }.padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(22.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(CC.surface)) }
}

@Composable
fun CheckCircle(size: Dp = 24.dp, bg: Color = CC.primary) =
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(CcIconsBold.Check, null, Modifier.size(size * 0.62f), tint = CC.onPrimary) }

@Composable
fun LogoMark(size: Dp = 56.dp, onDark: Boolean = false, shadow: Boolean = true) {
    val shape = RoundedCornerShape(size * 0.3f)
    Box(
        Modifier.size(size).then(if (shadow) Modifier.shadow(size * 0.16f, shape, ambientColor = CC.primary.copy(0.3f), spotColor = CC.primary.copy(0.4f)) else Modifier)
            .clip(shape).then(if (onDark) Modifier.background(CC.surface) else Modifier.background(CC.brand)),
        contentAlignment = Alignment.Center,
    ) { Icon(CcIcons.Snowflake, null, Modifier.size(size * 0.6f), tint = if (onDark) CC.primary else CC.onPrimary) }
}

@Composable
fun RowScope.Spacer1() = Box(Modifier.weight(1f))

fun Modifier.card(radius: Dp = 16.dp, fill: Color = CC.surface, stroke: Color? = CC.border, elevated: Boolean = true): Modifier {
    val shape = RoundedCornerShape(radius)
    return this.then(if (elevated) Modifier.shadow(6.dp, shape, ambientColor = Color(0x140B1F45), spotColor = Color(0x1F0B1F45)) else Modifier)
        .clip(shape).background(fill).then(if (stroke != null) Modifier.border(1.dp, stroke, shape) else Modifier)
}

fun Modifier.brush(b: Brush, radius: Dp = 0.dp) = this.clip(RoundedCornerShape(radius)).background(b)
