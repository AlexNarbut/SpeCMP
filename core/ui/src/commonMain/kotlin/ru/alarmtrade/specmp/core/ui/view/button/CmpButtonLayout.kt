package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.offset
import ru.alarmtrade.specmp.core.ui.view.text.atAutoSizeMin

internal fun adaptivePadding(available: Int, content: Int, min: Int, full: Int): Int {
    if (available == Constraints.Infinity) return full
    return ((available - content) / 2).coerceIn(min.coerceAtMost(full), full)
}

internal fun Modifier.adaptiveHorizontalPadding(full: Dp, min: Dp): Modifier =
    this then AdaptivePaddingElement(full, min)

private data class AdaptivePaddingElement(val full: Dp, val min: Dp) : ModifierNodeElement<AdaptivePaddingNode>() {
    override fun create() = AdaptivePaddingNode(full, min)

    override fun update(node: AdaptivePaddingNode) {
        node.full = full
        node.min = min
    }
}

private class AdaptivePaddingNode(var full: Dp, var min: Dp) :
    Modifier.Node(),
    LayoutModifierNode {

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val content = if (constraints.hasBoundedWidth) measurable.maxIntrinsicWidth(constraints.maxHeight) else 0
        val padding = adaptivePadding(constraints.maxWidth, content, min.roundToPx(), full.roundToPx())
        val placeable = measurable.measure(constraints.offset(horizontal = -2 * padding))
        val width = constraints.constrainWidth(placeable.width + 2 * padding)
        return layout(width, constraints.constrainHeight(placeable.height)) { placeable.place(padding, 0) }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
        measurable.minIntrinsicWidth(height) + 2 * minOf(min, full).roundToPx()

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
        measurable.maxIntrinsicWidth(height) + 2 * full.roundToPx()

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
        measurable.minIntrinsicHeight(innerWidth(width))

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
        measurable.maxIntrinsicHeight(innerWidth(width))

    private fun IntrinsicMeasureScope.innerWidth(width: Int): Int =
        if (width == Constraints.Infinity) width else (width - 2 * full.roundToPx()).coerceAtLeast(0)
}

/**
 * Иконка и подпись кнопки: подпись не влезает и на минимуме авторазмера рядом с иконкой — иконка скрывается
 * ([hideLeading]). Ширины подписи берутся из [TextMeasurer]: интринсики `Text` с autoSize зависят от прошлой раскладки.
 */
@Composable
internal fun ButtonLabelLayout(
    label: String,
    gap: Dp,
    hideLeading: Boolean,
    leading: (@Composable () -> Unit)?,
    text: @Composable () -> Unit,
) {
    val style = LocalTextStyle.current
    val measurer = rememberTextMeasurer(cacheSize = 2)
    val policy = remember(label, style, gap, hideLeading, measurer) {
        ButtonLabelPolicy(LabelWidths(measurer, label, style), gap, hideLeading)
    }
    Layout(
        content = {
            leading?.invoke()
            text()
        },
        measurePolicy = policy,
    )
}

private class LabelWidths(private val measurer: TextMeasurer, private val label: String, private val style: TextStyle) {
    fun IntrinsicMeasureScope.full(): Int = width(style)

    fun IntrinsicMeasureScope.min(): Int = width(style.atAutoSizeMin())

    private fun IntrinsicMeasureScope.width(at: TextStyle): Int = measurer.measure(
        text = label,
        style = at,
        maxLines = 1,
        softWrap = false,
        layoutDirection = layoutDirection,
        density = this,
    ).size.width
}

private class ButtonLabelPolicy(
    private val widths: LabelWidths,
    private val gap: Dp,
    private val hideLeading: Boolean,
) : MeasurePolicy {

    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val text = measurables.last()
        val gapPx = gap.roundToPx()
        val leading = measurables.takeIf { it.size > 1 }?.first()?.measure(Constraints())
            ?.takeIf { !hideLeading || fits(constraints, it.width + gapPx) }
        val textStart = leading?.let { it.width + gapPx } ?: 0
        val maxWidth = if (constraints.hasBoundedWidth) {
            (constraints.maxWidth - textStart).coerceAtLeast(0)
        } else {
            Constraints.Infinity
        }
        val placeable = text.measure(constraints.copy(minWidth = 0, maxWidth = maxWidth))
        val height = maxOf(placeable.height, leading?.height ?: 0)
        val width = constraints.constrainWidth(textStart + placeable.width)
        return layout(width, constraints.constrainHeight(height)) {
            leading?.place(0, (height - leading.height) / 2)
            placeable.place(textStart, (height - placeable.height) / 2)
        }
    }

    private fun MeasureScope.fits(constraints: Constraints, leadingSpace: Int): Boolean =
        !constraints.hasBoundedWidth || leadingSpace + with(widths) { min() } <= constraints.maxWidth

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        leadingSpace(measurables, height) + with(widths) { full() }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        (if (hideLeading) 0 else leadingSpace(measurables, height)) + with(widths) { min() }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        measurables.maxOf { it.minIntrinsicHeight(width) }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        measurables.maxOf { it.maxIntrinsicHeight(width) }

    private fun IntrinsicMeasureScope.leadingSpace(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        if (measurables.size > 1) measurables.first().maxIntrinsicWidth(height) + gap.roundToPx() else 0
}
