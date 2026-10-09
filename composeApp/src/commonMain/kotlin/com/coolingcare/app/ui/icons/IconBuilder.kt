package com.coolingcare.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** One SVG path of a 24×24 Lucide-style icon: stroked (S) or filled (F). */
internal class IconPath(val d: String, val filled: Boolean)

internal fun S(d: String) = IconPath(d, false)
internal fun F(d: String) = IconPath(d, true)

/** Builds a 24dp icon; tint is applied by `Icon(tint = …)` so the base colour does not matter. */
internal fun icon(name: String, vararg paths: IconPath): ImageVector = iconW(name, 2f, *paths)

internal fun iconW(name: String, strokeWidth: Float, vararg paths: IconPath): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        for (p in paths) {
            if (p.filled) addPath(pathData = addPathNodes(p.d), fill = SolidColor(Color.Black))
            else addPath(
                pathData = addPathNodes(p.d), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

/** Heavier variants used inside small filled circles (step dots, ticks, close badges). */
object CcIconsBold {
    val Check: ImageVector by lazy { iconW("check-bold", 3f, S("M20 6 9 17l-5-5")) }
    val X: ImageVector by lazy { iconW("x-bold", 2.8f, S("M18 6 6 18"), S("m6 6 12 12")) }
    private const val STAR = "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"
    val StarFilled: ImageVector by lazy { iconW("star-filled", 2f, F(STAR), S(STAR)) }
}
