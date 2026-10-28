package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_common_format_years_range
import ru.alarmtrade.specmp.core.localization.resources.vehicle_common_format_years_single
import ru.alarmtrade.specmp.core.localization.resources.vehicle_group_label_electric
import ru.alarmtrade.specmp.core.localization.resources.vehicle_group_label_extended
import ru.alarmtrade.specmp.core.localization.resources.vehicle_group_label_main
import ru.alarmtrade.specmp.core.localization.resources.vehicle_group_label_trailer
import ru.alarmtrade.specmp.core.localization.resources.vehicle_type_btn_alarm
import ru.alarmtrade.specmp.core.localization.resources.vehicle_type_btn_microbot
import ru.alarmtrade.specmp.core.localization.resources.vehicle_type_label_alarm
import ru.alarmtrade.specmp.core.localization.resources.vehicle_type_label_microbot
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType

/** Годы выпуска: «2017–2023» или «2018». */
@Composable
internal fun yearsText(years: IntRange): String = if (years.first == years.last) {
    stringResource(Strings.string.vehicle_common_format_years_single, years.first)
} else {
    stringResource(Strings.string.vehicle_common_format_years_range, years.first, years.last)
}

/** «Toyota Camry 2.5 AT»: марка, модель и комплектация без пустых частей. */
internal fun CarSummary.fullName(): String = listOf(brand, model, name).filter { it.isNotBlank() }.joinToString(" ")

@Composable
internal fun groupLabel(group: CarFeatureGroup): String = stringResource(
    when (group) {
        CarFeatureGroup.MAIN -> Strings.string.vehicle_group_label_main
        CarFeatureGroup.EXTENDED -> Strings.string.vehicle_group_label_extended
        CarFeatureGroup.ELECTRIC -> Strings.string.vehicle_group_label_electric
        CarFeatureGroup.TRAILER -> Strings.string.vehicle_group_label_trailer
    }
)

/** Полное название типа устройства (экран выбора типа, подзаголовок). */
@Composable
internal fun typeLabel(type: VehicleDeviceType): String = stringResource(
    when (type) {
        VehicleDeviceType.ALARM -> Strings.string.vehicle_type_label_alarm
        VehicleDeviceType.MICROBOT -> Strings.string.vehicle_type_label_microbot
    }
)

/** Короткое название типа (чип в шапке). */
@Composable
internal fun typeChipLabel(type: VehicleDeviceType): String = stringResource(
    when (type) {
        VehicleDeviceType.ALARM -> Strings.string.vehicle_type_btn_alarm
        VehicleDeviceType.MICROBOT -> Strings.string.vehicle_type_btn_microbot
    }
)
