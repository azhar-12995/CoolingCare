package com.coolingcare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.icons.CcIconsBold
import com.coolingcare.app.ui.theme.CC

const val GUTTER = 20

/** Phone-width column centred on tablets / landscape so layouts stay as designed. */
@Composable
fun Centered(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) { Column(Modifier.widthIn(max = 600.dp).fillMaxWidth(), content = content) }

/** Standard screen: status-bar safe area, optional app bar, scrolling body, optional footer pinned to the bottom. */
@Composable
fun Screen(
    modifier: Modifier = Modifier, bg: Color = CC.bg, top: (@Composable () -> Unit)? = null, footer: (@Composable () -> Unit)? = null,
    scroll: Boolean = true, bodyPadding: PaddingValues = PaddingValues(start = GUTTER.dp, end = GUTTER.dp, top = 6.dp, bottom = 16.dp), gap: Dp = 16.dp,
    body: @Composable ColumnScope.() -> Unit,
) {
    // imePadding: with edge-to-edge the window no longer resizes for the keyboard, so the body shrinks instead
    Column(modifier.fillMaxSize().background(bg).statusBarsPadding().imePadding()) {
        if (top != null) Centered { top() }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Centered(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier.fillMaxSize()) {
                Column(Modifier.padding(bodyPadding), verticalArrangement = Arrangement.spacedBy(gap), content = body)
            }
        }
        if (footer != null) footer()
    }
}

@Composable
fun AppBar(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier, center: Boolean = true, backIcon: ImageVector = CcIcons.ArrowLeft, actions: @Composable () -> Unit = {}) {
    Box(modifier.fillMaxWidth().height(54.dp).padding(horizontal = 8.dp)) {
        Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconBtn(backIcon, onBack, description = S.back.s) else Spacer(Modifier.width(12.dp))
            if (!center) T(title, type.titleM, CC.text, Modifier.padding(start = 6.dp), maxLines = 1)
        }
        if (center) T(title, type.titleM, CC.text, Modifier.align(Alignment.Center).padding(horizontal = 56.dp), maxLines = 1, align = TextAlign.Center)
        Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) { actions() }
    }
}

/** White footer with a soft top shadow; pads for the gesture bar. */
@Composable
fun Footer(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().shadow(14.dp, ambientColor = Color(0x120B1F45), spotColor = Color(0x120B1F45)).background(CC.surface).navigationBarsPadding()) {
        Centered { Column(Modifier.padding(horizontal = GUTTER.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
    }
}

enum class Tab { HOME, SERVICES, BOOKINGS, SUPPORT }

@Composable
fun BottomNav(active: Tab, onSelect: (Tab) -> Unit) {
    val items = listOf(Triple(Tab.HOME, CcIcons.House, S.home), Triple(Tab.SERVICES, CcIcons.LayoutGrid, S.services), Triple(Tab.BOOKINGS, CcIcons.CalendarDays, S.bookings), Triple(Tab.SUPPORT, CcIcons.Headset, S.support))
    Box(Modifier.fillMaxWidth().shadow(14.dp, ambientColor = Color(0x120B1F45), spotColor = Color(0x140B1F45)).background(CC.surface).navigationBarsPadding()) {
        Centered {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                for ((tab, icon, label) in items) {
                    val on = tab == active
                    Column(Modifier.width(76.dp).clip(RoundedCornerShape(12.dp)).tap { onSelect(tab) }.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(Modifier.size(52.dp, 28.dp).clip(RoundedCornerShape(14.dp)).background(if (on) CC.primaryTint else Color.Transparent), contentAlignment = Alignment.Center) { Ic(icon, 21.dp, if (on) CC.primary else CC.text3) }
                        T(label.s, if (on) type.labelS else type.caption, if (on) CC.primary else CC.text3, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) =
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        T(title, type.titleM, CC.text, Modifier.weight(1f))
        if (action != null) TextBtn(action, onAction)
    }

@Composable
fun HDivider(modifier: Modifier = Modifier) = Box(modifier.fillMaxWidth().height(1.dp).background(CC.divider))

@Composable
fun Note(icon: ImageVector, text: String, modifier: Modifier = Modifier, bg: Color = CC.primaryTint, tint: Color = CC.primary, textColor: Color = CC.text, title: String? = null, titleColor: Color = CC.text, trailing: (@Composable () -> Unit)? = null) =
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Ic(icon, 20.dp, tint)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (title != null) T(title, type.titleS, titleColor)
            T(text, type.bodyS, if (title != null) CC.text2 else textColor)
        }
        trailing?.invoke()
    }

@Composable
fun InfoLine(icon: ImageVector, text: String, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = type.bodyS, iconSize: Dp = 18.dp, latin: Boolean = false, maxLines: Int = Int.MAX_VALUE) =
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { Ic(icon, iconSize, CC.text2); T(text, style, CC.text, Modifier.weight(1f, fill = false), latin = latin, maxLines = maxLines) }

@Composable
fun ListRow(
    title: String, modifier: Modifier = Modifier, sub: String? = null, icon: ImageVector? = null, iconBg: Color = CC.primaryTint, iconTint: Color = CC.primary,
    titleColor: Color = CC.text, titleStyle: androidx.compose.ui.text.TextStyle? = null, chevron: Boolean = false, trailing: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(vertical = 11.dp),
) = Row(
    modifier.fillMaxWidth().then(if (onClick != null) Modifier.tap(onClick = onClick) else Modifier).padding(padding),
    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
) {
    if (icon != null) IconTile(icon, iconBg, iconTint, 36.dp, 18.dp, RoundedCornerShape(10.dp))
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        T(title, titleStyle ?: type.titleS, titleColor)
        if (sub != null) T(sub, type.bodyS, CC.text2)
    }
    trailing?.invoke()
    if (chevron) Ic(CcIcons.ChevronRight, 18.dp, CC.text3)
}

/** Requested → Confirmed → On the way → Completed. [done] = number of finished steps. */
@Composable
fun Stepper(labels: List<String>, done: Int, color: Color = CC.primary) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val seg = maxWidth / labels.size
        for (i in 0 until labels.size - 1) {
            Box(Modifier.offset(x = seg * (i + 0.5f) + 17.dp, y = 11.5.dp).width(seg - 34.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (i + 1 < done) color else CC.border))
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, l ->
                val ok = i < done
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ok) CheckCircle(26.dp, color)
                    else Box(Modifier.size(26.dp).clip(CircleShape).background(CC.surface).border(2.dp, CC.border, CircleShape), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(if (i == done) color else CC.border))
                    }
                    T(l, if (ok) type.labelS else type.caption, if (ok) CC.text else CC.text3, align = TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
}

/** Bottom sheet with the design's grabber, title row and close button. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = state, containerColor = CC.surface, scrimColor = CC.scrim,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(40.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(CC.border)) },
        contentWindowInsets = { BottomSheetDefaults.windowInsets },
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = GUTTER.dp, end = GUTTER.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                T(title, type.titleM, CC.text, Modifier.weight(1f))
                IconBtn(CcIconsBold.X, onDismiss, bg = CC.surface2, size = 34.dp, iconSize = 16.dp, description = S.close.s)
            }
            content()
        }
    }
}

/** Centred dialog card (design 37 / 44). */
@Composable
fun CcDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.padding(horizontal = 24.dp).widthIn(max = 420.dp).fillMaxWidth().shadow(24.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(CC.surface)
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), content = content,
        )
    }
}

@Composable
fun FeatureLine(icon: ImageVector, text: String, tileSize: Dp = 40.dp) =
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, size = tileSize, iconSize = tileSize * 0.55f); T(text, type.bodyL, CC.text)
    }

@Composable
fun Grabber() = Box(Modifier.size(40.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(CC.border))

@Composable
fun EmptyIcon(icon: ImageVector) = Box(Modifier.size(96.dp).clip(CircleShape).background(CC.tile), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(44.dp), tint = CC.primary) }
