package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_a11y_copy
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductFirmware

internal val CardShape = RoundedCornerShape(20.dp)
internal val RowMinHeight = 56.dp

/** Карточка результата: фон `surfaceContainerLow`, тонкая рамка. */
@Composable
internal fun InfoCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    spacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier,
        shape = CardShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(spacing)) {
            content()
        }
    }
}

/** Заголовок карточки: плитка с иконкой, подпись, справа — пометка. */
@Composable
internal fun Kicker(icon: CmpIcons, text: String, trailing: @Composable RowScope.() -> Unit = {}) {
    val colors = cmpThemeColors()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(40.dp).background(colors.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(icon = icon, contentDescription = null, tint = colors.onPrimaryContainer)
        }
        CmpText(text = text, style = CmpTextStyle.Paragraph2, modifier = Modifier.weight(1f), color = colors.primary)
        trailing()
    }
}

@Composable
internal fun Badge(icon: CmpIcons, text: String, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp)
            .background(colors.secondaryContainer, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CmpIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.onSecondaryContainer,
        )
        CmpText(
            text = text,
            color = colors.onSecondaryContainer,
            style = CmpTextStyle.Paragraph2,
        )
    }
}

internal fun mono(text: String, weight: FontWeight = FontWeight.Medium): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = weight)) { append(text) }
}

/** Строка «подпись / значение»; [onCopy] — иконка копирования справа. */
@Composable
internal fun InfoRow(label: String, value: String, monospace: Boolean, divider: Boolean, onCopy: ((String) -> Unit)?) {
    val colors = cmpThemeColors()
    Column {
        if (divider) CmpDivider(color = colors.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = RowMinHeight).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(text = label, color = colors.onSurfaceVariant, style = CmpTextStyle.Paragraph2)
                if (monospace) {
                    CmpText(text = mono(value), style = CmpTextStyle.Paragraph1)
                } else {
                    CmpText(text = value, style = CmpTextStyle.Paragraph1)
                }
            }
            if (onCopy != null) CopyButton(value = value, onCopy = onCopy)
        }
    }
}

@Composable
internal fun CopyButton(value: String, onCopy: (String) -> Unit) {
    CmpIconButton(
        onClick = { onCopy(value) },
        icon = CmpIcons.ContentCopy,
        contentDescription = stringResource(Strings.string.productinfo_common_a11y_copy, value),
        tint = cmpThemeColors().onSurfaceVariant,
    )
}

/** Прошивки: модуль слева, версия и дата справа. */
@Composable
internal fun FirmwareRows(firmwares: List<ProductFirmware>, dense: Boolean = false) {
    val colors = cmpThemeColors()
    Column {
        firmwares.forEachIndexed { index, firmware ->
            if (index > 0) CmpDivider(color = colors.outlineVariant)
            // Длинная версия («v.3.12nrf (DX-90BT)») переносится на свою строку, название модуля не дробится.
            CmpFlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = if (dense) 40.dp else 48.dp)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = firmware.module,
                    modifier = Modifier.padding(end = 12.dp),
                    style = CmpTextStyle.Paragraph1,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    firmware.version?.let { version ->
                        CmpText(
                            text = mono(version, FontWeight.SemiBold),
                            color = colors.primary,
                            style = CmpTextStyle.Paragraph1,
                        )
                    }
                    firmware.date?.let { date ->
                        CmpText(
                            text = date.formatted(),
                            color = colors.onSurfaceVariant,
                            style = CmpTextStyle.Paragraph2,
                        )
                    }
                }
            }
        }
    }
}

/** Порядковый номер компонента в кружке. */
@Composable
internal fun IndexCircle(index: Int) {
    val colors = cmpThemeColors()
    Box(
        modifier = Modifier.size(32.dp).background(colors.surfaceContainerHighest, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CmpText(
            text = (index + 1).toString(),
            color = colors.onSurfaceVariant,
            style = CmpTextStyle.Headline3,
        )
    }
}

private const val DAY_MONTH_DIGITS = 2

internal fun LocalDate.formatted(): String = "${dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
    "${monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}.$year"
