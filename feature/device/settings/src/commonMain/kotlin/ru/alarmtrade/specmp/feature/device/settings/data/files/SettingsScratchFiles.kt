package ru.alarmtrade.specmp.feature.device.settings.data.files

// Временный каталог настроек `.dxs` в кэше приложения: снимки для сравнения «Что изменится», файлы перед отдачей
// пользователю. SDK читает и пишет файлы настроек только по пути.

/** Каталог (создаётся при обращении). */
internal expect fun settingsScratchDirectory(): String

/** Удалить файл; нет файла — ничего. */
internal expect fun deleteSettingsScratchFile(path: String)
