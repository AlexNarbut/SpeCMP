package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_can_format_car
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.fullName

/** «Volkswagen Polo V, 2015–2020»: марка, модель и поколение без повторов, годы выпуска. */
@Composable
internal fun carLabel(car: CarSummary): String = stringResource(
    Strings.string.settings_can_format_car,
    car.fullName(),
    car.years.first,
    car.years.last,
)
