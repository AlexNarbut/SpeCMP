package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_mileage
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_parameters
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_vin_chars
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_vin_length
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_format_can
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_bootloader
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_brand_model
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_can
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_car
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_device_id
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_imei
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_mileage
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_model
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_plate
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_source_master
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_source_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_source_usb
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_vin
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_years
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_manual_unit
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.timeLabel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportField
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.VinError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.needsReread
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.vehicleFromCatalog

private const val MISSING = "—"
private val BodyGap = 16.dp

@Composable
internal fun SectionColumn(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(BodyGap), modifier = Modifier.fillMaxWidth()) { content() }
}

@Composable
internal fun VehicleBody(
    passport: MountPassport,
    state: PassportEditorState,
    onFieldChange: (PassportField, String) -> Unit,
) {
    val form = state.form
    SectionColumn {
        if (passport.vehicleFromCatalog()) {
            AutoCard(source = stringResource(Strings.string.mountpassport_editor_label_source_master)) {
                val vehicle = passport.vehicle
                val model = listOfNotNull(vehicle.brand, vehicle.model).joinToString(" ")
                val modelLabel = stringResource(Strings.string.mountpassport_editor_label_brand_model)
                val yearsLabel = stringResource(Strings.string.mountpassport_editor_label_years)
                KeyValue(modelLabel, model, last = vehicle.years == null)
                vehicle.years?.let { KeyValue(yearsLabel, it, last = true) }
            }
        } else {
            OptionalField(
                label = stringResource(Strings.string.mountpassport_editor_label_car),
                value = form.carTitle,
                onValueChange = { onFieldChange(PassportField.CAR, it) },
            )
        }
        OptionalField(
            label = stringResource(Strings.string.mountpassport_editor_label_plate),
            value = form.plate,
            onValueChange = { onFieldChange(PassportField.PLATE, it) },
        )
        VinField(form.vin, state.vinError, onValueChange = { onFieldChange(PassportField.VIN, it) })
        OptionalField(
            label = stringResource(Strings.string.mountpassport_editor_label_mileage),
            value = form.mileage,
            onValueChange = { onFieldChange(PassportField.MILEAGE, it) },
            error = if (state.mileageInvalid) {
                stringResource(
                    Strings.string.mountpassport_editor_error_mileage,
                )
            } else {
                null
            },
            keyboardType = KeyboardType.Number,
        )
    }
}

/** Ошибка длины — только когда поле не в фокусе: пока VIN набирается, он и не должен быть полным. */
@Composable
private fun VinField(vin: String, error: VinError?, onValueChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shown = when (error) {
        VinError.INVALID_CHARS -> stringResource(Strings.string.mountpassport_editor_error_vin_chars)
        VinError.LENGTH -> if (focused) null else stringResource(Strings.string.mountpassport_editor_error_vin_length)
        null -> null
    }
    OptionalField(
        label = stringResource(Strings.string.mountpassport_editor_label_vin),
        value = vin,
        onValueChange = onValueChange,
        error = shown,
        modifier = Modifier.onFocusChanged { focused = it.isFocused },
    )
}

@Composable
internal fun EquipmentBody(
    passport: MountPassport,
    rereadVisible: Boolean,
    rereading: PassportReadStage?,
    onReread: (PassportReadStage) -> Unit,
) {
    val unit = passport.unit
    SectionColumn {
        if (passport.withoutUnit) {
            AutoCard(source = null) {
                Line(stringResource(Strings.string.mountpassport_editor_label_model), unit.systemName)
                Line(stringResource(Strings.string.mountpassport_editor_label_device_id), unit.systemId, last = true)
            }
            Note(CmpIcons.Edit, stringResource(Strings.string.mountpassport_editor_msg_manual_unit))
        } else {
            val missing = passport.needsReread(PassportReadStage.PARAMETERS)
            val time = passport.createdAt.timeLabel()
            val source = if (unit.transport == PassportTransport.USB) {
                stringResource(Strings.string.mountpassport_editor_label_source_usb, time)
            } else {
                stringResource(Strings.string.mountpassport_editor_label_source_unit, time)
            }
            AutoCard(source = source) { EquipmentLines(passport, missing) }
            if (missing) {
                RereadRow(
                    text = stringResource(Strings.string.mountpassport_editor_error_parameters),
                    stage = PassportReadStage.PARAMETERS,
                    rereadVisible = rereadVisible,
                    onReread = onReread,
                    rereading = rereading,
                )
            }
        }
    }
}

@Composable
private fun EquipmentLines(passport: MountPassport, missing: Boolean) {
    val unit = passport.unit
    val bluetooth = unit.transport == PassportTransport.BLUETOOTH
    val lines = buildList {
        add(stringResource(Strings.string.mountpassport_editor_label_model) to unit.systemName)
        add(stringResource(Strings.string.mountpassport_editor_label_device_id) to unit.systemId)
        unit.firmwareVersion?.let { add(stringResource(Strings.string.mountpassport_editor_label_firmware) to it) }
        val bootloader = unit.bluetoothModule?.bootloaderVersion?.takeIf { bluetooth }
        add(stringResource(Strings.string.mountpassport_editor_label_bootloader) to bootloader)
        unit.can?.let { can ->
            val value = can.code?.let { code ->
                stringResource(Strings.string.mountpassport_editor_format_can, can.version.orEmpty(), code)
            } ?: can.version
            add(stringResource(Strings.string.mountpassport_editor_label_can) to value)
        }
        val bluetoothFirmware = unit.bluetoothModule?.firmwareVersion
        add(stringResource(Strings.string.mountpassport_editor_label_bluetooth) to bluetoothFirmware)
        add(stringResource(Strings.string.mountpassport_editor_label_imei) to unit.imei)
    }.filter { (_, value) -> value != null || missing }
    lines.forEachIndexed { index, (label, value) ->
        Line(label, value ?: MISSING, last = index == lines.lastIndex, missing = value == null)
    }
}

@Composable
internal fun SourceRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp).padding(vertical = CmpSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) { content() }
}
