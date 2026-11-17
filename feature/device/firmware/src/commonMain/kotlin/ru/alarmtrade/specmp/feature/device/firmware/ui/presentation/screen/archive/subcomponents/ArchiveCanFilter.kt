package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_a11y_can_clear
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_a11y_can_vehicle
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_can_reset
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_format_can_tag
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_hint_can
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can_device
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can_field
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_cleared
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_device_auto
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_device_auto_plain
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_factory
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_factory_short
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_partial
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_pick_vehicle
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_status_can_same
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.CAN_CODE_DIGITS
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.CanFilterUi

internal const val ARCHIVE_CAN_INPUT_TAG = "firmware_archive_can_input"
internal const val ARCHIVE_CAN_VEHICLE_TAG = "firmware_archive_can_vehicle"
internal const val ARCHIVE_CAN_RESET_TAG = "firmware_archive_can_reset"

@Composable
internal fun ArchiveCanFilter(
    filter: CanFilterUi,
    onInput: (String) -> Unit,
    onResetToDevice: () -> Unit,
    onPickVehicle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val auto = filter.isDeviceCode
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpText(
            text = stringResource(Strings.string.firmware_archive_label_can_field),
            style = CmpTextStyle.Headline3,
            color = colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.padding(horizontal = CmpSpacing.space4),
        )
        CmpTextField(
            value = filter.input,
            onValueChange = { onInput(it.filter(Char::isDigit).take(CAN_CODE_DIGITS)) },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (auto) colors.brandPrimaryHighlight else Color.Transparent,
                    RoundedCornerShape(CmpRadius.inputs),
                )
                .testTag(ARCHIVE_CAN_INPUT_TAG),
            placeholder = stringResource(Strings.string.firmware_archive_hint_can),
            singleLine = true,
            isError = filter.isPartial,
            valueStyle = CmpTextStyle.Headline1,
            leadingIcon = {
                CmpIcon(
                    icon = if (auto) CmpIcons.Memory else CmpIcons.Tag,
                    contentDescription = null,
                    tint = if (auto) colors.brandPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
                )
            },
            trailingIcon = {
                CmpRow {
                    if (filter.input.isNotEmpty()) {
                        CmpIconButton(
                            onClick = { onInput("") },
                            icon = CmpIcons.Close,
                            contentDescription = stringResource(Strings.string.firmware_archive_a11y_can_clear),
                        )
                    }
                    if (filter.canPickVehicle) {
                        CmpIconButton(
                            onClick = onPickVehicle,
                            icon = CmpIcons.DirectionsCar,
                            contentDescription = stringResource(Strings.string.firmware_archive_a11y_can_vehicle),
                            tint = colors.brandPrimaryDefault,
                            modifier = Modifier.testTag(ARCHIVE_CAN_VEHICLE_TAG),
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        CanHelp(filter)
        val deviceCode = filter.deviceCode
        if (filter.canResetToDevice && deviceCode != null) DeviceCodePlate(deviceCode, onResetToDevice)
    }
}

@Composable
private fun CanHelp(filter: CanFilterUi) {
    val colors = CmpTheme.colors
    val carTitle = filter.carTitle?.takeIf { filter.input.length == CAN_CODE_DIGITS }
    when {
        filter.isPartial -> HelpLine(
            CmpIcons.ErrorOutline,
            stringResource(Strings.string.firmware_archive_msg_can_partial),
            colors.errorDefault,
        )

        filter.isDeviceCode -> HelpLine(
            CmpIcons.Memory,
            filter.deviceProfile?.let { stringResource(Strings.string.firmware_archive_msg_can_device_auto, it) }
                ?: stringResource(Strings.string.firmware_archive_msg_can_device_auto_plain),
            colors.brandPrimaryDefault,
        )

        filter.noUnit || (filter.deviceCode == null && filter.deviceFactory) -> InfoHelp(filter, carTitle)

        else -> {
            carTitle?.let { CarLine(it) }
            if (filter.input.isEmpty() && filter.deviceCode != null) {
                HelpLine(
                    CmpIcons.FilterAltOff,
                    stringResource(Strings.string.firmware_archive_msg_can_cleared),
                    colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
    }
}

@Composable
private fun InfoHelp(filter: CanFilterUi, carTitle: String?) {
    carTitle?.let { CarLine(it) }
    val text = when {
        filter.noUnit -> if (filter.input.isEmpty()) Strings.string.firmware_archive_msg_can_pick_vehicle else null
        filter.input.isEmpty() -> Strings.string.firmware_archive_msg_can_factory
        else -> Strings.string.firmware_archive_msg_can_factory_short
    }
    text?.let { HelpLine(CmpIcons.Info, stringResource(it), CmpTheme.colors.foregroundOnDarkSecondaryDefault) }
}

@Composable
private fun CarLine(carTitle: String) {
    HelpLine(
        CmpIcons.DirectionsCar,
        carTitle,
        CmpTheme.colors.brandPrimaryDefault,
        textColor = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
    )
}

@Composable
private fun HelpLine(icon: CmpIcons, text: String, color: Color, textColor: Color = color) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = color, modifier = Modifier.size(CmpSize.iconM))
        CmpText(
            text = text,
            style = if (textColor != color) CmpTextStyle.Headline3 else CmpTextStyle.Paragraph2,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DeviceCodePlate(deviceCode: String, onResetToDevice: () -> Unit) {
    val colors = CmpTheme.colors
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        color = colors.backgroundSurfaceHighlight,
    ) {
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = CmpSpacing.space12, end = CmpSpacing.space4)
                .heightIn(min = CmpSize.touchMin),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Подпись сжимается и переносится по словам, код блока — никогда.
            CmpRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(
                    icon = CmpIcons.Memory,
                    contentDescription = null,
                    tint = colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )
                CmpText(
                    text = stringResource(Strings.string.firmware_archive_label_can_device),
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier.weight(1f, fill = false),
                )
                CmpText(
                    text = deviceCode,
                    style = CmpTextStyle.Headline3,
                    color = colors.brandPrimaryDefault,
                    maxLines = 1,
                )
            }
            CmpButton(
                text = stringResource(Strings.string.firmware_archive_btn_can_reset),
                onClick = onResetToDevice,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                maxLines = 2,
                modifier = Modifier.testTag(ARCHIVE_CAN_RESET_TAG),
            )
        }
    }
}

@Composable
internal fun ArchiveRowCan(row: ArchiveRowUi, modifier: Modifier = Modifier) {
    val code = row.canCode ?: return
    val mine = row.canMatchesDevice
    val label = row.canVersion?.let { stringResource(Strings.string.firmware_archive_format_can_tag, code, it) }
        ?: stringResource(Strings.string.firmware_archive_label_can, code)
    CmpTag(
        text = if (mine) "$label · " + stringResource(Strings.string.firmware_archive_status_can_same) else label,
        modifier = modifier,
        tone = if (mine) CmpTagTone.Brand else CmpTagTone.Outline,
        icon = if (mine) CmpIcons.Link else null,
        maxLines = 2,
    )
}
