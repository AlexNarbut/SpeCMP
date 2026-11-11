package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.LocalThemeIsDark
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/** Фон карточек ленты: светлая тема — самый светлый контейнер, тёмная — приподнятый. */
@Composable
internal fun assistantCardColor(): Color {
    val colors = cmpThemeColors()
    return if (LocalThemeIsDark.current.value) colors.surfaceContainerHigh else colors.surfaceContainerLowest
}

internal enum class PillTone { OK, WARN, ERROR, NEUTRAL, INFO }

@Composable
internal fun AssistantPill(text: String, tone: PillTone = PillTone.NEUTRAL, icon: CmpIcons? = null) {
    val colors = cmpThemeColors()
    val success = cmpDomainColors().success
    val (background, content) = when (tone) {
        PillTone.OK -> success.copy(alpha = OK_TINT) to colors.onSurface
        PillTone.WARN -> colors.tertiaryContainer to colors.onTertiaryContainer
        PillTone.ERROR -> colors.errorContainer to colors.onErrorContainer
        PillTone.NEUTRAL -> colors.surfaceContainerHighest to colors.onSurface
        PillTone.INFO -> colors.secondaryContainer to colors.onSecondaryContainer
    }
    CmpSurface(shape = RoundedCornerShape(8.dp), color = background, contentColor = content) {
        CmpRow(
            modifier = Modifier.heightIn(min = 32.dp).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                CmpIcon(
                    icon = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (tone == PillTone.OK) success else content,
                )
            }
            CmpText(
                text = text,
                style = CmpTextStyle.Body,
                fontWeight = FontWeight.Medium,
                color = content,
                maxLines = 1,
            )
        }
    }
}

private const val OK_TINT = 0.22f

/** Квадрат с иконкой: карточки, плитки, источники. */
@Composable
internal fun IconBox(icon: CmpIcons, background: Color, tint: Color, size: Dp = 40.dp, iconSize: Dp = 22.dp) {
    CmpSurface(modifier = Modifier.size(size), shape = RoundedCornerShape(12.dp), color = background) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(iconSize), tint = tint)
        }
    }
}

/** Метка ассистента — молния в круге primary. */
@Composable
internal fun AssistantMark(size: Dp = 32.dp) {
    val colors = cmpThemeColors()
    CmpSurface(modifier = Modifier.size(size), shape = CircleShape, color = colors.primary) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = CmpIcons.Bolt,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colors.onPrimary,
            )
        }
    }
}

/** Оболочка карточки результата; [heavy] — выделенная (контур primary). */
@Composable
internal fun CardShell(
    modifier: Modifier = Modifier,
    heavy: Boolean = false,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = assistantCardColor(),
        contentColor = colors.onSurface,
        border = if (heavy) {
            BorderStroke(2.dp, borderColor ?: colors.primary)
        } else {
            BorderStroke(1.dp, colors.outlineVariant)
        },
        shadowElevation = if (heavy) 2.dp else 0.dp,
    ) {
        CmpColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
internal fun CardHeader(icon: CmpIcons, title: String, error: Boolean = false, badge: @Composable () -> Unit = {}) {
    val colors = cmpThemeColors()
    CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBox(
            icon = if (error) CmpIcons.ErrorOutline else icon,
            background = if (error) colors.errorContainer else colors.primaryContainer,
            tint = if (error) colors.onErrorContainer else colors.onPrimaryContainer,
        )
        CmpText(
            text = title,
            modifier = Modifier.weight(1f),
            style = CmpTextStyle.BodyLarge,
            fontWeight = FontWeight.Medium,
        )
        badge()
    }
}

@Composable
internal fun SkeletonRows(rows: Int) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(rows) { index ->
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpBox(modifier = Modifier.weight(1f)) {
                    CmpSkeleton(
                        modifier = Modifier.fillMaxWidth(fraction = 1f - index * SKELETON_STEP).heightIn(min = 20.dp),
                    )
                }
                CmpSkeleton(modifier = Modifier.size(width = 72.dp, height = 28.dp))
            }
        }
    }
}

private const val SKELETON_STEP = 0.14f

/** Строка «название — значение-плашка», [sub] — моноширинная подпись (версии). */
@Composable
internal fun KeyValueRow(label: String, value: String, tone: PillTone, icon: CmpIcons? = null, sub: String? = null) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(text = label, style = CmpTextStyle.BodyLarge)
            if (sub != null) CmpText(text = sub, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
        }
        AssistantPill(text = value, tone = tone, icon = icon)
    }
}

/** Текст с подсвеченными совпадениями поиска; диапазоны вне текста пропускаются. */
@Composable
internal fun highlighted(text: String, ranges: List<IntRange>): AnnotatedString {
    val colors = cmpThemeColors()
    return buildAnnotatedString {
        append(text)
        val style = SpanStyle(background = colors.primaryContainer, color = colors.onPrimaryContainer)
        ranges.filter { it.first >= 0 && it.last < text.length }.forEach { addStyle(style, it.first, it.last + 1) }
    }
}
