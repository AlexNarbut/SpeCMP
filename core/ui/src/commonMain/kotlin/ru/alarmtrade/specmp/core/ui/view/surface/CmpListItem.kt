package ru.alarmtrade.specmp.core.ui.view.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
private val StatusDotSize = 12.dp
private val StatusDotRing = 2.dp
private val CompactInfoRow = 72.dp

/**
 * Строка списка (kit ListItem): от 80dp, ступень чернил 5% + рамка 1dp, иконка в круге 48, заголовок Headline2,
 * подзаголовок Paragraph3 60%, справа [trailing] (по умолчанию шеврон, если строка нажимается).
 * [leading] — свой элемент слева вместо [icon] (фото, логотип), [below] — содержимое под подзаголовком (кнопка).
 * [selected] — выбранная строка (master-detail), [titleMaxLines] — перенос заголовка.
 * [flat] — пункт меню: без фона и рамки, иконка 24 без круга, от 56dp, заголовок Paragraph1.
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
    flat: Boolean = false,
    selected: Boolean = false,
    titleMaxLines: Int = 1,
    leading: (@Composable () -> Unit)? = null,
    below: (@Composable ColumnScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.listItem)
    val clickable = onClick != null && enabled
    val frame = listItemFrame(selected = selected, flat = flat, shape = shape)
    Row(
        modifier = modifier
            .heightIn(min = if (flat) CmpSize.touchMin + CmpSpacing.space8 else CmpSize.listItem)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .then(if (clickable) Modifier.cmpPressable(onClick = onClick, shape = shape) else Modifier)
            .then(frame)
            .padding(horizontal = CmpSpacing.space16, vertical = if (flat) CmpSpacing.space8 else CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ListItemLeading(leading = leading, icon = icon, flat = flat, status = status)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(
                text = title,
                style = if (flat) CmpTextStyle.Paragraph1 else CmpTextStyle.Headline2,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                CmpText(
                    text = subtitle,
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            below?.invoke(this)
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
        if (status != null) StatusDot(status, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun StatusDot(status: CmpListItemStatus, modifier: Modifier) {
    val colors = CmpTheme.colors
    val dot = when (status) {
        CmpListItemStatus.Ok -> colors.successDefault
        CmpListItemStatus.Warn -> colors.warningDefault
        CmpListItemStatus.Error -> colors.errorDefault
        CmpListItemStatus.Info -> colors.brandPrimaryDefault
    }
    Box(
        modifier = modifier
            .size(StatusDotSize)
            .background(dot, CircleShape)
            .border(StatusDotRing, colors.backgroundPagePrimary, CircleShape),
    )
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
    CmpInfoRow(label = label, modifier = modifier, last = last, compact = compact, trailing = trailing) {
        val valueStyle = CmpTextStyle.Headline2.toTextStyle()
        Text(
            text = value,
            style = if (mono) valueStyle.copy(fontFamily = FontFamily.Monospace) else valueStyle,
            color = if (accent) colors.brandPrimaryDefault else colors.foregroundOnDarkPrimaryDefault,
        )
    }
}

/** [CmpInfoRow] со своим значением (индикатор сигнала, батареи, теги) под подписью. */
@Composable
fun CmpInfoRow(
    label: String,
    modifier: Modifier = Modifier,
    last: Boolean = false,
    compact: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    value: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.heightIn(
                min = if (compact) CompactInfoRow else CmpSize.infoRow,
            ).padding(vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(
                    text = label,
                    style = CmpTextStyle.Additional0,
                    color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                )
                value()
            }
            if (trailing != null) trailing()
        }
        if (!last) CmpDivider()
    }
}

/** Тон тега статуса. */
enum class CmpTagTone { Neutral, Outline, Brand, Success, Warning, Error, Info }

/**
 * Тег статуса: 32dp, радиус `CmpRadius.tags`, рамка 1dp, заливка `*-highlight` тона, Additional0 капсом;
 * [loading] — спиннер вместо иконки.
 */
@Composable
fun CmpTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: CmpTagTone = CmpTagTone.Neutral,
    icon: CmpIcons? = null,
    maxLines: Int = 1,
    loading: Boolean = false,
) {
    val colors = CmpTheme.colors
    val (fill, content) = when (tone) {
        CmpTagTone.Neutral -> colors.backgroundSurfaceHighlight to colors.foregroundOnDarkPrimaryDefault
        CmpTagTone.Outline -> Color.Transparent to colors.foregroundOnDarkSecondaryDefault
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
                .heightIn(min = CmpSize.tag)
                .background(fill, shape)
                .border(CmpBorderWidth.tags, content.copy(alpha = TAG_BORDER_ALPHA), shape)
                .padding(horizontal = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(CmpSize.iconS),
                    color = content,
                    strokeWidth = TagSpinnerStroke,
                )
            } else if (icon != null) {
                CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(CmpSize.iconS))
            }
            CmpText(
                text = text,
                style = CmpTextStyle.Additional0,
                uppercase = true,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val TAG_BORDER_ALPHA = 0.3f
private val TagSpinnerStroke = 2.dp

@Composable
private fun listItemFrame(selected: Boolean, flat: Boolean, shape: Shape): Modifier {
    val colors = CmpTheme.colors
    return when {
        selected ->
            Modifier
                .background(colors.brandPrimaryHighlight, shape)
                .border(CmpBorderWidth.listItem, colors.brandPrimaryDefault, shape)

        flat -> Modifier

        else ->
            Modifier
                .background(colors.backgroundSurfaceUltraHighlight, shape)
                .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
    }
}

@Composable
private fun ListItemLeading(
    leading: (@Composable () -> Unit)?,
    icon: CmpIcons?,
    flat: Boolean,
    status: CmpListItemStatus?,
) {
    when {
        leading != null -> Box {
            leading()
            if (status != null) StatusDot(status, Modifier.align(Alignment.BottomEnd))
        }

        icon == null -> Unit

        flat -> CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.size(CmpSize.iconM),
        )

        else -> ListItemIcon(icon, status)
    }
}
