package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Car
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_can_car

/** «Volkswagen Polo V, 2015–2020»: марка, модель и поколение без повторов, годы выпуска. */
@Composable
internal fun carLabel(car: Car): String = stringResource(
    Res.string.settings_can_car,
    listOf(car.brand, car.model, car.name).filter { it.isNotBlank() }.distinct().joinToString(" "),
    car.years.first,
    car.years.last
)
