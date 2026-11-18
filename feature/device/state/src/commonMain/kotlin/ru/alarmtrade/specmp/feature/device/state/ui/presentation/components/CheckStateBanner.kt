package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private const val BORDER_ALPHA = 0.3f
private val SpinnerStroke = 2.dp

/** Плашка экрана: иконка | заголовок и текст | действие справа. [neutral] — тон Info. */
@Composable
internal fun CheckStateBanner(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    neutral: Boolean = false,
    busy: Boolean = false,
    action: String? = null,
    actionEnabled: Boolean = true,
    onAction: () -> Unit = {},
) {
    val colors = CmpTheme.colors
    val fill = if (neutral) colors.infoHighlight else colors.warningHighlight
    val accent = if (neutral) colors.infoDefault else colors.warningDefault
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    Layout(
        modifier = modifier
            .fillMaxWidth()
            .background(fill, shape)
            .border(CmpBorderWidth.inputs, accent.copy(alpha = BORDER_ALPHA), shape)
            .padding(CmpSpacing.space12),
        content = {
            if (busy) {
                CmpCircularProgressIndicator(
                    modifier = Modifier.size(CmpSize.iconM),
                    strokeWidth = SpinnerStroke,
                    color = accent,
                    trackColor = Color.Transparent,
                )
            } else {
                CmpIcon(
                    icon = icon ?: if (neutral) CmpIcons.Info else CmpIcons.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(CmpSize.iconM),
                )
            }
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(text = title, style = CmpTextStyle.Headline3)
                CmpText(text = text, style = CmpTextStyle.Paragraph3, color = colors.foregroundOnDarkSecondaryDefault)
            }
            if (action != null) {
                CmpButton(
                    onClick = onAction,
                    enabled = actionEnabled,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                ) {
                    CmpText(text = action, style = CmpTextStyle.Headline3, maxLines = 1, uppercase = true)
                }
            }
        },
        measurePolicy = BannerPolicy,
    )
}

private class BannerGaps(val lead: Dp, val action: Dp)

private val WideGaps = BannerGaps(lead = CmpSpacing.space12, action = CmpSpacing.space12)

private val TightGaps = BannerGaps(lead = CmpSpacing.space8, action = 0.dp)

// Слова текста и подпись кнопки не рвутся и не сжимаются: сначала уже отступы (у кнопки свой внутренний),
// не влезло и так — кнопка под текстом справа.
private val BannerPolicy = MeasurePolicy { measurables, constraints ->
    val width = constraints.maxWidth
    val leading = measurables[0].measure(Constraints())
    val body = measurables[1]
    val button = measurables.getOrNull(2)
    val textMin = body.minIntrinsicWidth(constraints.maxHeight)
    val buttonMin = button?.minIntrinsicWidth(constraints.maxHeight) ?: 0
    val inRow = listOf(WideGaps, TightGaps).firstOrNull {
        button == null || leading.width + it.lead.roundToPx() + textMin + it.action.roundToPx() + buttonMin <= width
    }
    val afterLeading = (width - leading.width - (inRow ?: WideGaps).lead.roundToPx()).coerceAtLeast(0)
    val actionGap = inRow?.action?.roundToPx()
    val action = button?.let {
        val room = if (actionGap != null) afterLeading - actionGap - textMin else width
        val natural = it.maxIntrinsicWidth(constraints.maxHeight)
        it.measure(Constraints(maxWidth = room.coerceIn(buttonMin, maxOf(buttonMin, natural)).coerceAtMost(width)))
    }
    val beside = if (action != null && actionGap != null) action.width + actionGap else 0
    val content = body.measure(Constraints.fixedWidth((afterLeading - beside).coerceAtLeast(0)))
    val top = maxOf(leading.height, content.height, if (actionGap != null) action?.height ?: 0 else 0)
    val belowY = top + CmpSpacing.space8.roundToPx()
    val below = action != null && actionGap == null
    val height = (if (below) belowY + action.height else top).coerceIn(constraints.minHeight, constraints.maxHeight)
    layout(width, height) {
        leading.placeRelative(0, (top - leading.height) / 2)
        content.placeRelative(width - afterLeading, (top - content.height) / 2)
        action?.placeRelative(width - action.width, if (below) belowY else (top - action.height) / 2)
    }
}
