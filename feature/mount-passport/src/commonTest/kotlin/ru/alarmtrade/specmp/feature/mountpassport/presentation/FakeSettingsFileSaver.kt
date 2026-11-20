package ru.alarmtrade.specmp.feature.mountpassport.presentation

import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver

internal class FakeSettingsFileSaver : SettingsFileSaver {
    var result: SettingsFileSaveResult = SettingsFileSaveResult.Saved("settings.dxs")
    val saves = mutableListOf<Pair<String, String>>()

    override suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult {
        saves += sourcePath to fileName
        return result
    }
}
