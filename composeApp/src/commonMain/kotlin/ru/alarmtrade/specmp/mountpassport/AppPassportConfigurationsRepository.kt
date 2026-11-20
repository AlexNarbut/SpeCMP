package ru.alarmtrade.specmp.mountpassport

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportConfigurationsRepository

/** Паспорт не зависит от конфигуратора: снимок настроек уходит в «Мои конфигурации» через корень приложения. */
internal class AppPassportConfigurationsRepository(private val local: LocalConfigurationsRepository) :
    PassportConfigurationsRepository {

    override suspend fun saveSettings(name: String, deviceModel: String, settingsPath: String): Response<Unit> =
        local.create(
            name = name,
            description = "",
            deviceName = deviceModel,
            from = SettingsSource.File(path = settingsPath, deviceName = deviceModel, inPlace = false),
        ).map { }
}
