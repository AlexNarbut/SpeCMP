package ru.alarmtrade.specmp.feature.fastmount.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult

/** Порт паспорта монтажа: адаптер — в корне приложения (фича от паспорта не зависит). */
interface FastMountPassportRepository {
    /** Паспорт по id; `null` — удалён. Эмитит заново при каждом изменении. */
    fun observe(passportId: String): Flow<Response<FastMountPassport?>>

    /** Заготовка паспорта из итога мастера без считывания блока; id паспорта. */
    suspend fun createBlank(result: FastMountResult): Response<String>
}
