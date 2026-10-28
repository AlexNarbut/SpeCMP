package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_can_format_car
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

/** «Volkswagen Polo V, 2015–2020»: марка, модель и поколение без повторов, годы выпуска. */
@Composable
internal fun carLabel(car: CarSummary): String = stringResource(
    Strings.string.settings_can_format_car,
    listOf(car.brand, car.model, car.name).filter { it.isNotBlank() }.distinct().joinToString(" "),
    car.years.first,
    car.years.last
)
