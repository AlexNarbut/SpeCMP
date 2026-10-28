package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.CanFilterUi
import specmp.feature.device.firmware.generated.resources.Res
import specmp.feature.device.firmware.generated.resources.firmware_archive_can_a11y_clear
import specmp.feature.device.firmware.generated.resources.firmware_archive_can_a11y_vehicle
import specmp.feature.device.firmware.generated.resources.firmware_archive_can_hint
import specmp.feature.device.firmware.generated.resources.firmware_archive_can_msg_partial
import specmp.feature.device.firmware.generated.resources.firmware_archive_row_can
import specmp.feature.device.firmware.generated.resources.firmware_archive_row_can_version

internal const val ARCHIVE_CAN_INPUT_TAG = "firmware_archive_can_input"
internal const val ARCHIVE_CAN_VEHICLE_TAG = "firmware_archive_can_vehicle"

private const val CAN_CODE_LENGTH = 4

/**
 * Поле CAN-фильтра над списком версий: цифры кода, справа — очистка и выбор автомобиля из каталога (если подключён).
 * Под полем — автомобиль, из которого взят код, или подсказка, пока код не набран полностью.
 */
@Composable
internal fun ArchiveCanFilter(
    filter: CanFilterUi,
    onInput: (String) -> Unit,
    onPickVehicle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supporting = when {
        filter.carTitle != null -> filter.carTitle

        filter.input.isNotEmpty() && filter.input.length < CAN_CODE_LENGTH ->
            stringResource(Res.string.firmware_archive_can_msg_partial)

        else -> null
    }
    CmpTextField(
        value = filter.input,
        onValueChange = { onInput(it.filter(Char::isDigit).take(CAN_CODE_LENGTH)) },
        modifier = modifier.fillMaxWidth().testTag(ARCHIVE_CAN_INPUT_TAG),
        placeholder = stringResource(Res.string.firmware_archive_can_hint),
        singleLine = true,
        leadingIcon = { CmpIcon(icon = CmpIcons.Search, contentDescription = null) },
        trailingIcon = {
            CmpRow {
                if (filter.input.isNotEmpty()) {
                    CmpIconButton(
                        onClick = { onInput("") },
                        icon = CmpIcons.Close,
                        contentDescription = stringResource(Res.string.firmware_archive_can_a11y_clear)
                    )
                }
                if (filter.canPickVehicle) {
                    CmpIconButton(
                        onClick = onPickVehicle,
                        icon = CmpIcons.DirectionsCar,
                        contentDescription = stringResource(Res.string.firmware_archive_can_a11y_vehicle),
                        tint = cmpThemeColors().primary,
                        modifier = Modifier.testTag(ARCHIVE_CAN_VEHICLE_TAG)
                    )
                }
            }
        },
        supportingText = supporting?.let { text -> { CmpText(text = text) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

/** «CAN 1234 · <версия профиля>» у версии, собранной под CAN-код; без CAN — ничего. */
@Composable
internal fun ArchiveRowCan(row: ArchiveRowUi, modifier: Modifier = Modifier) {
    val code = row.canCode ?: return
    CmpText(
        text = row.canVersion?.let { stringResource(Res.string.firmware_archive_row_can_version, code, it) }
            ?: stringResource(Res.string.firmware_archive_row_can, code),
        fontSize = 13.sp,
        color = cmpThemeColors().onSurfaceVariant,
        modifier = modifier
    )
}
