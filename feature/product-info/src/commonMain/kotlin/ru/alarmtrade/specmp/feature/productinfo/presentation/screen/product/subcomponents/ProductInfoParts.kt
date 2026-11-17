package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_a11y_copy
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.badge.CmpIconTile
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpInfoRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductFirmware

internal val IconTileSize = 40.dp
internal val IndexSize = 32.dp
private val FirmwareRowDense = 40.dp

@Composable
internal fun InfoCard(
    modifier: Modifier = Modifier,
    padding: Dp = CmpSpacing.space16,
    spacing: Dp = CmpSpacing.space12,
    content: @Composable ColumnScope.() -> Unit,
) {
    CmpCard(modifier = modifier) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(spacing)) {
            content()
        }
    }
}

/** Заголовок карточки: плитка с иконкой, подпись, справа — пометка. */
@Composable
internal fun Kicker(icon: CmpIcons, text: String, trailing: @Composable RowScope.() -> Unit = {}) {
    val colors = CmpTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIconTile(icon = rememberVectorPainter(icon.imageVector()), size = IconTileSize)
        CmpText(
            text = text,
            style = CmpTextStyle.Headline3,
            modifier = Modifier.weight(1f),
            color = colors.brandPrimaryDefault,
        )
        trailing()
    }
}

internal fun mono(text: String): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(text) }
}

internal class InfoLine(val label: String, val value: String, val monospace: Boolean, val copyable: Boolean)

/** Строки «подпись / значение» с разделителями между ними; [lastDivider] — разделитель и под последней. */
@Composable
internal fun InfoLines(lines: List<InfoLine>, onCopy: (String) -> Unit, lastDivider: Boolean = false) {
    Column {
        lines.forEachIndexed { index, line ->
            CmpInfoRow(
                label = line.label,
                value = line.value,
                mono = line.monospace,
                compact = true,
                last = index == lines.lastIndex && !lastDivider,
                trailing = if (line.copyable) {
                    { CopyButton(value = line.value, onCopy = onCopy) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
internal fun CopyButton(value: String, onCopy: (String) -> Unit) {
    CmpIconButton(
        onClick = { onCopy(value) },
        icon = CmpIcons.ContentCopy,
        contentDescription = stringResource(Strings.string.productinfo_common_a11y_copy, value),
        tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
    )
}

/** Прошивки: модуль слева, версия и дата справа. */
@Composable
internal fun FirmwareRows(firmwares: List<ProductFirmware>, dense: Boolean = false) {
    val colors = CmpTheme.colors
    Column {
        firmwares.forEachIndexed { index, firmware ->
            if (index > 0) CmpDivider()
            // Длинная версия («v.3.12nrf (DX-90BT)») переносится на свою строку, название модуля не дробится.
            CmpFlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = if (dense) FirmwareRowDense else CmpSize.touchMin)
                    .padding(vertical = CmpSpacing.space4),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = firmware.module,
                    modifier = Modifier.padding(end = CmpSpacing.space12),
                    style = CmpTextStyle.Paragraph2,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    firmware.version?.let { version ->
                        CmpText(
                            text = mono(version),
                            color = colors.brandPrimaryDefault,
                            style = CmpTextStyle.Headline2,
                        )
                    }
                    firmware.date?.let { date ->
                        CmpText(
                            text = date.formatted(),
                            color = colors.foregroundOnDarkSecondaryDefault,
                            style = CmpTextStyle.Paragraph3,
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
    val colors = CmpTheme.colors
    Box(
        modifier = Modifier.size(IndexSize).background(colors.backgroundSurfaceHighlight, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CmpText(
            text = (index + 1).toString(),
            color = colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Additional0,
        )
    }
}

private const val DAY_MONTH_DIGITS = 2

internal fun LocalDate.formatted(): String = "${dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
    "${monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}.$year"
