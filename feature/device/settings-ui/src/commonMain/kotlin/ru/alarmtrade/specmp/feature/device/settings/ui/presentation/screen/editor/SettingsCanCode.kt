package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

/*
 * Окно CAN-кода: как результаты поиска и выбора автомобиля меняют state (чистые функции; запросы — во VM).
 */

/**
 * Ответ поиска автомобилей по коду [code]. Устарел (в окне уже другой код или окно закрыто) — state без изменений;
 * найден ровно один — сразу привязывается к коду.
 */
internal fun DeviceSettingsState.withCarSearch(
    code: String,
    response: Response<List<CarSummary>>
): DeviceSettingsState {
    val current = editing ?: return this
    if ((current.draft as? SettingValue.Can)?.value?.code != code) return this
    return when (response) {
        is Response.Error -> copy(
            editing = current.copy(carSearch = LCEState.Error(SettingsError.from(response.exception)))
        )

        is Response.Success -> {
            val found = response.value
            val draft = found.singleOrNull()?.let { SettingValue.Can(CanCode(code, it.id)) } ?: current.draft
            copy(
                editing = current.copy(draft = draft, carSearch = LCEState.Content(found)),
                cars = cars + found.associateBy { it.id }
            )
        }
    }
}

/** Выбран автомобиль [carId] из нескольких подходящих к коду. */
internal fun DeviceSettingsState.withCarPicked(carId: Int): DeviceSettingsState {
    val current = editing ?: return this
    val draft = current.draft as? SettingValue.Can ?: return this
    return copy(editing = current.copy(draft = SettingValue.Can(draft.value.copy(carId = carId))))
}

/** Автомобиль выбран в каталоге: его CAN-код и `carId` — в черновик (применяется по «Готово»); каталог закрыт. */
internal fun DeviceSettingsState.withCatalogCar(car: CarSummary): DeviceSettingsState {
    val current = editing
    if (current == null || current.draft !is SettingValue.Can) return copy(vehiclePicker = false)
    return copy(
        vehiclePicker = false,
        editing = current.copy(draft = SettingValue.Can(CanCode(car.canCode, car.id)), carSearch = LCEState.None),
        cars = cars + (car.id to car)
    )
}
