package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_label_firmware
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_label_firmwares
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_label_id
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_label_serial
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_btn_collapse_all
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_btn_expand_all
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_format_firmware
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_format_id
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_format_serial
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_label_firmware_count
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_label_name
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_msg_no_firmware
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_msg_no_serial
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_title
import ru.alarmtrade.specmp.core.localization.resources.productinfo_components_value_none
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductUnit
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoTags

private const val HALF_TURN = 180f
private const val NAME_WEIGHT = 1.3f
private const val SERIAL_WEIGHT = 1f
private const val FIRMWARE_WEIGHT = 1.15f
private val ZeroDp = 0.dp
private val TableHeaderHeight = 52.dp
private val DetailsInset = CmpSpacing.space16 + IndexSize + CmpSpacing.space12

/** «Компоненты · N» и «Раскрыть все» (кнопка — только у строк с раскрытием). */
@Composable
internal fun ComponentsHeader(count: Int, allExpanded: Boolean, onToggleAll: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(Strings.string.productinfo_components_title, count),
            modifier = Modifier.weight(1f),
            style = CmpTextStyle.Headline1,
        )
        if (onToggleAll != null) {
            CmpButton(
                text = stringResource(
                    if (allExpanded) {
                        Strings.string.productinfo_components_btn_collapse_all
                    } else {
                        Strings.string.productinfo_components_btn_expand_all
                    },
                ),
                onClick = onToggleAll,
                modifier = Modifier.testTag(ProductInfoTags.EXPAND_ALL),
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        }
    }
}

/** Телефон и планшет-портрет: серийник виден сразу, прошивки и копирование — по нажатию. */
@Composable
internal fun ComponentRows(
    components: ImmutableList<ProductUnit>,
    expanded: PersistentSet<Int>,
    onToggle: (Int) -> Unit,
    onCopy: (String) -> Unit,
) {
    val colors = CmpTheme.colors
    InfoCard(padding = ZeroDp, spacing = ZeroDp) {
        components.forEachIndexed { index, component ->
            val open = index in expanded
            if (index > 0) CmpDivider()
            Column(
                modifier = Modifier.background(if (open) colors.backgroundSurfaceUltraHighlight else Color.Transparent),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onToggle(index) }
                        .testTag(ProductInfoTags.component(index))
                        .defaultMinSize(minHeight = CmpSize.listItem)
                        .padding(
                            start = CmpSpacing.space16,
                            end = CmpSpacing.space8,
                            top = CmpSpacing.space12,
                            bottom = CmpSpacing.space12,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                ) {
                    IndexCircle(index)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                    ) {
                        CmpText(text = component.name, style = CmpTextStyle.Headline3)
                        ComponentSummary(component)
                    }
                    CmpIcon(
                        icon = CmpIcons.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconL).rotate(if (open) HALF_TURN else 0f),
                        tint = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                AnimatedVisibility(visible = open) {
                    Column(
                        modifier = Modifier.padding(
                            start = DetailsInset,
                            end = CmpSpacing.space16,
                            bottom = CmpSpacing.space8,
                        ),
                    ) {
                        ComponentDetails(component, onCopy)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentSummary(component: ProductUnit) {
    val colors = CmpTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val serial = component.serial
        if (serial != null) {
            CmpText(
                text = mono(stringResource(Strings.string.productinfo_components_format_serial, serial)),
                style = CmpTextStyle.Paragraph2,
            )
        } else {
            CmpText(
                text = stringResource(Strings.string.productinfo_components_msg_no_serial),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
            )
        }
        val firmwares = component.firmwares
        val summary = when {
            firmwares.size == 1 && firmwares.first().version != null ->
                stringResource(
                    Strings.string.productinfo_components_format_firmware,
                    firmwares.first().version.orEmpty(),
                )

            firmwares.isNotEmpty() ->
                pluralStringResource(
                    Strings.plurals.productinfo_components_label_firmware_count,
                    firmwares.size,
                    firmwares.size,
                )

            else -> null
        }
        summary?.let {
            CmpText(text = it, color = colors.foregroundOnDarkSecondaryDefault, style = CmpTextStyle.Paragraph2)
        }
    }
}

@Composable
private fun ComponentDetails(component: ProductUnit, onCopy: (String) -> Unit) {
    val colors = CmpTheme.colors
    val lines = listOfNotNull(
        component.serial?.let {
            InfoLine(stringResource(Strings.string.productinfo_common_label_serial), it, true, true)
        },
        component.id?.let {
            InfoLine(stringResource(Strings.string.productinfo_common_label_id), it.toString(), true, true)
        },
    )
    InfoLines(lines, onCopy, lastDivider = true)
    if (component.firmwares.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = CmpSpacing.space8)) {
            CmpText(
                text = stringResource(
                    if (component.firmwares.size > 1) {
                        Strings.string.productinfo_common_label_firmwares
                    } else {
                        Strings.string.productinfo_common_label_firmware
                    },
                ),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Additional0,
            )
            FirmwareRows(component.firmwares, dense = true)
        }
    } else {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = CmpSize.touchMin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(
                text = stringResource(Strings.string.productinfo_components_msg_no_firmware),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
            )
        }
    }
}

/** Широкое окно: таблица «Название | Серийный номер | Прошивки». */
@Composable
internal fun ComponentTable(components: ImmutableList<ProductUnit>, onCopy: (String) -> Unit) {
    val colors = CmpTheme.colors
    InfoCard(padding = ZeroDp, spacing = ZeroDp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.backgroundSurfaceUltraHighlight)
                .defaultMinSize(minHeight = TableHeaderHeight)
                .padding(start = CmpSpacing.space24, end = CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            HeaderCell(Strings.string.productinfo_components_label_name, NAME_WEIGHT)
            HeaderCell(Strings.string.productinfo_common_label_serial, SERIAL_WEIGHT)
            HeaderCell(Strings.string.productinfo_common_label_firmwares, FIRMWARE_WEIGHT)
        }
        components.forEachIndexed { index, component ->
            CmpDivider()
            TableRow(index, component, onCopy)
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: StringResource, weight: Float) {
    CmpText(
        text = stringResource(text),
        modifier = Modifier.weight(weight),
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        style = CmpTextStyle.Additional0,
    )
}

@Composable
private fun TableRow(index: Int, component: ProductUnit, onCopy: (String) -> Unit) {
    val colors = CmpTheme.colors
    val none = stringResource(Strings.string.productinfo_components_value_none)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(ProductInfoTags.component(index))
            .defaultMinSize(minHeight = CmpSize.listItem)
            .padding(
                start = CmpSpacing.space24,
                end = CmpSpacing.space16,
                top = CmpSpacing.space8,
                bottom = CmpSpacing.space8,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        Row(
            modifier = Modifier.weight(NAME_WEIGHT),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            IndexCircle(index)
            Column {
                CmpText(text = component.name, style = CmpTextStyle.Headline3)
                component.id?.let { id ->
                    CmpText(
                        text = mono(stringResource(Strings.string.productinfo_components_format_id, id.toString())),
                        color = colors.foregroundOnDarkSecondaryDefault,
                        style = CmpTextStyle.Paragraph3,
                    )
                }
            }
        }
        Row(modifier = Modifier.weight(SERIAL_WEIGHT), verticalAlignment = Alignment.CenterVertically) {
            val serial = component.serial
            if (serial != null) {
                CmpText(text = mono(serial), modifier = Modifier.weight(1f), style = CmpTextStyle.Paragraph2)
                CopyButton(value = serial, onCopy = onCopy)
            } else {
                CmpText(text = none, color = colors.foregroundOnDarkSecondaryDefault, style = CmpTextStyle.Paragraph2)
            }
        }
        Column(
            modifier = Modifier.weight(FIRMWARE_WEIGHT),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            if (component.firmwares.isEmpty()) {
                CmpText(text = none, color = colors.foregroundOnDarkSecondaryDefault, style = CmpTextStyle.Paragraph2)
            }
            component.firmwares.forEach { firmware ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = firmware.module,
                        color = colors.foregroundOnDarkSecondaryDefault,
                        style = CmpTextStyle.Paragraph3,
                    )
                    firmware.version?.let {
                        CmpText(text = mono(it), color = colors.brandPrimaryDefault, style = CmpTextStyle.Headline3)
                    }
                    firmware.date?.let {
                        CmpText(
                            text = it.formatted(),
                            color = colors.foregroundOnDarkSecondaryDefault,
                            style = CmpTextStyle.Paragraph3,
                        )
                    }
                }
            }
        }
    }
}
