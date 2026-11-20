package ru.alarmtrade.specmp.feature.mountpassport.domain.repository

import ru.alarmtrade.specmp.core.common.Response

/** «Мои конфигурации»: фича от конфигуратора не зависит, адаптер — в корне приложения. */
interface PassportConfigurationsRepository {
    suspend fun saveSettings(name: String, deviceModel: String, settingsPath: String): Response<Unit>
}
