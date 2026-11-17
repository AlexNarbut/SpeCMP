package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/** Круг с иконкой: карточки, плитки, источники. */
@Composable
internal fun IconBox(
    icon: CmpIcons,
    background: Color,
    tint: Color,
    size: Dp = CmpSize.touchMin,
    iconSize: Dp = CmpSize.iconM,
) {
    CmpBox(modifier = Modifier.size(size).background(background, CircleShape), contentAlignment = Alignment.Center) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(iconSize), tint = tint)
    }
}

/** Метка ассистента — молния в круге бренда. */
@Composable
internal fun AssistantMark(size: Dp = CmpSize.iconL) {
    val colors = CmpTheme.colors
    IconBox(
        icon = CmpIcons.Bolt,
        background = colors.brandPrimaryDefault,
        tint = colors.onBrand,
        size = size,
        iconSize = CmpSize.iconS,
    )
}

/** Оболочка карточки результата; [heavy] — выделенная (рамка бренда или [borderColor]). */
@Composable
internal fun CardShell(
    modifier: Modifier = Modifier,
    heavy: Boolean = false,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    CmpCard(
        modifier = modifier.fillMaxWidth(),
        border = if (heavy) {
            BorderStroke(CmpBorderWidth.listItem, borderColor ?: CmpTheme.colors.brandPrimaryDefault)
        } else {
            null
        },
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            content = content,
        )
    }
}

@Composable
internal fun CardHeader(icon: CmpIcons, title: String, error: Boolean = false, badge: @Composable () -> Unit = {}) {
    val colors = CmpTheme.colors
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBox(
            icon = if (error) CmpIcons.ErrorOutline else icon,
            background = if (error) colors.errorHighlight else colors.brandPrimaryHighlight,
            tint = if (error) colors.errorDefault else colors.brandPrimaryDefault,
        )
        CmpFlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(text = title, style = CmpTextStyle.Headline2)
            badge()
        }
    }
}

private val SkeletonLine = 20.dp
private val SkeletonValueWidth = 72.dp
private val SkeletonValueHeight = 28.dp

@Composable
internal fun SkeletonRows(rows: Int) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        repeat(rows) { index ->
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpBox(modifier = Modifier.weight(1f)) {
                    CmpSkeleton(
                        modifier = Modifier.fillMaxWidth(fraction = 1f - index * SKELETON_STEP)
                            .heightIn(min = SkeletonLine),
                    )
                }
                CmpSkeleton(modifier = Modifier.size(width = SkeletonValueWidth, height = SkeletonValueHeight))
            }
        }
    }
}

private const val SKELETON_STEP = 0.14f

/** Строка «название — тег значения», [sub] — подпись (версии). */
@Composable
internal fun KeyValueRow(label: String, value: String, tone: CmpTagTone, icon: CmpIcons? = null, sub: String? = null) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(text = label, style = CmpTextStyle.Paragraph1)
            if (sub != null) {
                CmpText(
                    text = sub,
                    style = CmpTextStyle.Paragraph2,
                    color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
        CmpTag(text = value, tone = tone, icon = icon)
    }
}

/** Текст с подсвеченными совпадениями поиска; диапазоны вне текста пропускаются. */
@Composable
internal fun highlighted(text: String, ranges: List<IntRange>): AnnotatedString {
    val colors = CmpTheme.colors
    return buildAnnotatedString {
        append(text)
        val style = SpanStyle(background = colors.brandPrimaryHighlight, color = colors.brandPrimaryDefault)
        ranges.filter { it.first >= 0 && it.last < text.length }.forEach { addStyle(style, it.first, it.last + 1) }
    }
}
