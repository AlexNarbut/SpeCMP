package ru.alarmtrade.specmp.feature.device.settings.domain.model

/**
 * Откуда редактор берёт настройки. Все операции репозитория принимают источник, поэтому редактор
 * не знает, работает он с подключённым блоком, файлом или конфигурацией.
 */
sealed interface SettingsSource {
    /** Подключённый блок (текущая сессия). */
    data object Device : SettingsSource

    /** Файл настроек `.dxs` по локальному пути. */
    data class File(val path: String, val deviceName: String) : SettingsSource

    /** Конфигурация из локального хранилища. */
    data class LocalConfiguration(val id: Long, val deviceName: String, val title: String) : SettingsSource

    /** Конфигурация с сервера (конфигуратор) — только просмотр. */
    data class RemoteConfiguration(val id: Int, val deviceName: String, val title: String) : SettingsSource

    /** Заводские настройки модели — только просмотр. */
    data class Defaults(val deviceName: String) : SettingsSource
}

/** Что можно делать с настройками источника. */
data class SettingsSourceCapabilities(
    /** Правки можно сохранить в источник (записать в блок, файл, хранилище). */
    val canSave: Boolean,
    /** Доступен сброс к заводским настройкам. */
    val canResetToFactory: Boolean,
    /** Источник — подключённый блок: при переподключении меню перечитывается из блока. */
    val followsDeviceConnection: Boolean
)

val SettingsSource.capabilities: SettingsSourceCapabilities
    get() = when (this) {
        SettingsSource.Device -> SettingsSourceCapabilities(
            canSave = true,
            canResetToFactory = true,
            followsDeviceConnection = true
        )

        is SettingsSource.File, is SettingsSource.LocalConfiguration -> SettingsSourceCapabilities(
            canSave = true,
            canResetToFactory = false,
            followsDeviceConnection = false
        )

        is SettingsSource.RemoteConfiguration, is SettingsSource.Defaults -> SettingsSourceCapabilities(
            canSave = false,
            canResetToFactory = false,
            followsDeviceConnection = false
        )
    }
