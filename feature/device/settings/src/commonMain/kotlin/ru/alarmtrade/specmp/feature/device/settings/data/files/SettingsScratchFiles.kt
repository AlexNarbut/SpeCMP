package ru.alarmtrade.specmp.feature.device.settings.data.files

// SDK читает и пишет файлы настроек только по пути.

internal expect fun settingsScratchDirectory(): String

internal expect fun deleteSettingsScratchFile(path: String)
