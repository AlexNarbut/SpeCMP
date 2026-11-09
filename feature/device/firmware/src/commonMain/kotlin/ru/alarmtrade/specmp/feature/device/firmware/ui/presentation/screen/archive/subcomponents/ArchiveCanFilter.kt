package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
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
    val colors = cmpThemeColors()
    val auto = filter.isDeviceCode
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpText(
            text = stringResource(Strings.string.firmware_archive_label_can_field),
            fontWeight = FontWeight.Medium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        CmpTextField(
            value = filter.input,
            onValueChange = { onInput(it.filter(Char::isDigit).take(CAN_CODE_DIGITS)) },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (auto) colors.secondaryContainer else Color.Transparent,
                    RoundedCornerShape(4.dp),
                )
                .testTag(ARCHIVE_CAN_INPUT_TAG),
            placeholder = stringResource(Strings.string.firmware_archive_hint_can),
            singleLine = true,
            isError = filter.isPartial,
            leadingIcon = {
                CmpIcon(
                    icon = if (auto) CmpIcons.Memory else CmpIcons.Tag,
                    contentDescription = null,
                    tint = if (auto) colors.primary else colors.onSurfaceVariant,
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
                            tint = colors.primary,
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
    val colors = cmpThemeColors()
    val carTitle = filter.carTitle?.takeIf { filter.input.length == CAN_CODE_DIGITS }
    when {
        filter.isPartial -> HelpLine(
            CmpIcons.ErrorOutline,
            stringResource(Strings.string.firmware_archive_msg_can_partial),
            colors.error,
        )

        filter.isDeviceCode -> HelpLine(
            CmpIcons.Memory,
            filter.deviceProfile?.let { stringResource(Strings.string.firmware_archive_msg_can_device_auto, it) }
                ?: stringResource(Strings.string.firmware_archive_msg_can_device_auto_plain),
            colors.primary,
        )

        filter.noUnit || (filter.deviceCode == null && filter.deviceFactory) -> InfoHelp(filter, carTitle)

        else -> {
            carTitle?.let { CarLine(it) }
            if (filter.input.isEmpty() && filter.deviceCode != null) {
                HelpLine(
                    CmpIcons.FilterAltOff,
                    stringResource(Strings.string.firmware_archive_msg_can_cleared),
                    colors.onSurfaceVariant,
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
    text?.let { HelpLine(CmpIcons.Info, stringResource(it), cmpThemeColors().onSurfaceVariant) }
}

@Composable
private fun CarLine(carTitle: String) {
    HelpLine(CmpIcons.DirectionsCar, carTitle, cmpThemeColors().primary, textColor = cmpThemeColors().onSurface)
}

@Composable
private fun HelpLine(icon: CmpIcons, text: String, color: Color, textColor: Color = color) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        CmpText(
            text = text,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = if (textColor != color) FontWeight.SemiBold else null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DeviceCodePlate(deviceCode: String, onResetToDevice: () -> Unit) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerHigh,
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp).heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Подпись сжимается и переносится по словам, код блока — никогда.
            CmpRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(
                    icon = CmpIcons.Memory,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                CmpText(
                    text = stringResource(Strings.string.firmware_archive_label_can_device),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f, fill = false),
                )
                CmpText(
                    text = deviceCode,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    maxLines = 1,
                )
            }
            CmpTextButton(
                text = stringResource(Strings.string.firmware_archive_btn_can_reset),
                onClick = onResetToDevice,
                modifier = Modifier.testTag(ARCHIVE_CAN_RESET_TAG),
            )
        }
    }
}

@Composable
internal fun ArchiveRowCan(row: ArchiveRowUi, modifier: Modifier = Modifier) {
    val code = row.canCode ?: return
    val colors = cmpThemeColors()
    val mine = row.canMatchesDevice
    val content = if (mine) colors.onSecondaryContainer else colors.onSurfaceVariant
    CmpSurface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (mine) colors.secondaryContainer else Color.Transparent,
        border = if (mine) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (mine) {
                CmpIcon(
                    icon = CmpIcons.Link,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(18.dp),
                )
            }
            CmpText(
                text = row.canVersion?.let { stringResource(Strings.string.firmware_archive_format_can_tag, code, it) }
                    ?: stringResource(Strings.string.firmware_archive_label_can, code),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = content,
            )
            if (mine) {
                CmpText(
                    text = "· " + stringResource(Strings.string.firmware_archive_status_can_same),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = content,
                )
            }
        }
    }
}
