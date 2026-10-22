package ru.alarmtrade.specmp.feature.device.settings.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Car

/** Автомобили из базы (сервер) для CAN-кода. Тонкий: без кэша и повторов. */
interface CarsRepository {
    /** Автомобили с CAN-кодом [canCode] (пусто — не найдено). */
    suspend fun findByCanCode(canCode: String): Response<List<Car>>

    /** Автомобиль по id (подпись к CAN-коду). */
    suspend fun car(id: Int): Response<Car>
}
