package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_common_years_range
import specmp.feature.vehicle_ui.generated.resources.vehicle_common_years_single
import specmp.feature.vehicle_ui.generated.resources.vehicle_group_label_electric
import specmp.feature.vehicle_ui.generated.resources.vehicle_group_label_extended
import specmp.feature.vehicle_ui.generated.resources.vehicle_group_label_main
import specmp.feature.vehicle_ui.generated.resources.vehicle_group_label_trailer
import specmp.feature.vehicle_ui.generated.resources.vehicle_type_btn_alarm
import specmp.feature.vehicle_ui.generated.resources.vehicle_type_btn_microbot
import specmp.feature.vehicle_ui.generated.resources.vehicle_type_label_alarm
import specmp.feature.vehicle_ui.generated.resources.vehicle_type_label_microbot

/** Годы выпуска: «2017–2023» или «2018». */
@Composable
internal fun yearsText(years: IntRange): String = if (years.first == years.last) {
    stringResource(Res.string.vehicle_common_years_single, years.first)
} else {
    stringResource(Res.string.vehicle_common_years_range, years.first, years.last)
}

/** «Toyota Camry 2.5 AT»: марка, модель и комплектация без пустых частей. */
internal fun CarSummary.fullName(): String = listOf(brand, model, name).filter { it.isNotBlank() }.joinToString(" ")

@Composable
internal fun groupLabel(group: CarFeatureGroup): String = stringResource(
    when (group) {
        CarFeatureGroup.MAIN -> Res.string.vehicle_group_label_main
        CarFeatureGroup.EXTENDED -> Res.string.vehicle_group_label_extended
        CarFeatureGroup.ELECTRIC -> Res.string.vehicle_group_label_electric
        CarFeatureGroup.TRAILER -> Res.string.vehicle_group_label_trailer
    }
)

/** Полное название типа устройства (экран выбора типа, подзаголовок). */
@Composable
internal fun typeLabel(type: VehicleDeviceType): String = stringResource(
    when (type) {
        VehicleDeviceType.ALARM -> Res.string.vehicle_type_label_alarm
        VehicleDeviceType.MICROBOT -> Res.string.vehicle_type_label_microbot
    }
)

/** Короткое название типа (чип в шапке). */
@Composable
internal fun typeChipLabel(type: VehicleDeviceType): String = stringResource(
    when (type) {
        VehicleDeviceType.ALARM -> Res.string.vehicle_type_btn_alarm
        VehicleDeviceType.MICROBOT -> Res.string.vehicle_type_btn_microbot
    }
)
