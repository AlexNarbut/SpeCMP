package ru.alarmtrade.specmp.feature.mountpassport.presentation

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportConfigurationsRepository

internal class FakePassportConfigurationsRepository : PassportConfigurationsRepository {
    var result: Response<Unit> = Response.Success(Unit)
    val saves = mutableListOf<Triple<String, String, String>>()

    override suspend fun saveSettings(name: String, deviceModel: String, settingsPath: String): Response<Unit> {
        saves += Triple(name, deviceModel, settingsPath)
        return result
    }
}
