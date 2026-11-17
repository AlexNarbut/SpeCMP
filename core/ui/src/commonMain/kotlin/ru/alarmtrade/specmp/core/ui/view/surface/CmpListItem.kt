package ru.alarmtrade.specmp.core.ui.view.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.core.ui.view.text.toTextStyle

/** Точка статуса на иконке строки. */
enum class CmpListItemStatus { Ok, Warn, Error, Info }

private const val DISABLED_ALPHA = 0.4f
private val StatusDot = 12.dp
private val StatusDotRing = 2.dp
private val CompactInfoRow = 72.dp

/**
 * Строка списка (kit ListItem): от 80dp, ступень чернил 5% + рамка 1dp, иконка в круге 48, заголовок Headline2,
 * подзаголовок Paragraph3 60%, справа [trailing] (по умолчанию шеврон, если строка нажимается).
 */
@Composable
fun CmpListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: CmpIcons? = null,
    status: CmpListItemStatus? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.listItem)
    val clickable = onClick != null && enabled
    Row(
        modifier = modifier
            .heightIn(min = CmpSize.listItem)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .then(if (clickable) Modifier.cmpPressable(onClick = onClick, shape = shape) else Modifier)
            .background(colors.backgroundSurfaceUltraHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) ListItemIcon(icon, status)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = title, style = CmpTextStyle.Headline2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                CmpText(
                    text = subtitle,
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        when {
            trailing != null -> trailing()

            clickable -> CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.size(CmpSize.iconL),
            )
        }
    }
}

@Composable
private fun ListItemIcon(icon: CmpIcons, status: CmpListItemStatus?) {
    val colors = CmpTheme.colors
    Box(
        modifier = Modifier.size(CmpSize.touchMin).background(colors.backgroundSurfaceHighlight, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(CmpSize.iconM))
        if (status != null) {
            val dot = when (status) {
                CmpListItemStatus.Ok -> colors.successDefault
                CmpListItemStatus.Warn -> colors.warningDefault
                CmpListItemStatus.Error -> colors.errorDefault
                CmpListItemStatus.Info -> colors.brandPrimaryDefault
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(StatusDot)
                    .background(dot, CircleShape)
                    .border(StatusDotRing, colors.backgroundPagePrimary, CircleShape),
            )
        }
    }
}

/**
 * «Подпись над значением» (kit InfoRow): подпись Additional0 60%, значение Headline2 (моноширинное — [mono],
 * акцентное — [accent]), от 88dp ([compact] — 72), справа [trailing] (например, «копировать»); разделитель снизу, кроме [last].
 */
@Composable
fun CmpInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    mono: Boolean = false,
    accent: Boolean = false,
    last: Boolean = false,
    compact: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.heightIn(
                min = if (compact) CompactInfoRow else CmpSize.infoRow,
            ).padding(vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(text = label, style = CmpTextStyle.Additional0, color = colors.foregroundOnDarkSecondaryDefault)
                val valueStyle = CmpTextStyle.Headline2.toTextStyle()
                Text(
                    text = value,
                    style = if (mono) valueStyle.copy(fontFamily = FontFamily.Monospace) else valueStyle,
                    color = if (accent) colors.brandPrimaryDefault else colors.foregroundOnDarkPrimaryDefault,
                )
            }
            if (trailing != null) trailing()
        }
        if (!last) CmpDivider()
    }
}

/** Тон тега статуса. */
enum class CmpTagTone { Neutral, Brand, Success, Warning, Error, Info }

private val TagHeight = 32.dp

/** Тег статуса: 32dp, радиус `CmpRadius.tags`, рамка 1dp, заливка `*-highlight` тона, Additional0 капсом. */
@Composable
fun CmpTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: CmpTagTone = CmpTagTone.Neutral,
    icon: CmpIcons? = null,
) {
    val colors = CmpTheme.colors
    val (fill, content) = when (tone) {
        CmpTagTone.Neutral -> colors.backgroundSurfaceHighlight to colors.foregroundOnDarkPrimaryDefault
        CmpTagTone.Brand -> colors.brandPrimaryHighlight to colors.brandPrimaryDefault
        CmpTagTone.Success -> colors.successHighlight to colors.successDefault
        CmpTagTone.Warning -> colors.warningHighlight to colors.warningDefault
        CmpTagTone.Error -> colors.errorHighlight to colors.errorDefault
        CmpTagTone.Info -> colors.infoHighlight to colors.infoDefault
    }
    val shape = RoundedCornerShape(CmpRadius.tags)
    CompositionLocalProvider(LocalContentColor provides content) {
        Row(
            modifier = modifier
                .heightIn(min = TagHeight)
                .background(fill, shape)
                .border(CmpBorderWidth.tags, content.copy(alpha = TAG_BORDER_ALPHA), shape)
                .padding(horizontal = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(CmpSize.iconS))
            CmpText(text = text, style = CmpTextStyle.Additional0, uppercase = true, maxLines = 1)
        }
    }
}

private const val TAG_BORDER_ALPHA = 0.3f
