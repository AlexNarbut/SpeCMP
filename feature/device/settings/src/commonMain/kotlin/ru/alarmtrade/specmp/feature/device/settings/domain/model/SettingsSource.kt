package ru.alarmtrade.specmp.feature.device.settings.domain.model

/**
 * Откуда редактор берёт настройки. Все операции репозитория принимают источник, поэтому редактор
 * не знает, работает он с подключённым блоком, файлом или конфигурацией.
 */
sealed interface SettingsSource {
    /** Подключённый блок (текущая сессия). */
    data object Device : SettingsSource

    /**
     * Файл настроек `.dxs` по локальному пути; модели в файле нет — [deviceName] задаёт пользователь.
     * [name] — имя для показа; [inPlace] — путь ведёт к самому файлу пользователя (desktop) и «Сохранить»
     * перезаписывает его. На Android/iOS выбранный документ копируется во временный файл — сохранить можно только
     * «как…».
     */
    data class File(
        val path: String,
        val deviceName: String,
        val name: String = path.substringAfterLast('/'),
        val inPlace: Boolean = true
    ) : SettingsSource

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
    val followsDeviceConnection: Boolean,
    /** Только просмотр (облако, заводские): правки сохраняются только «как…» в другой источник. */
    val isReadOnly: Boolean = false
)

val SettingsSource.capabilities: SettingsSourceCapabilities
    get() = when (this) {
        SettingsSource.Device -> SettingsSourceCapabilities(
            canSave = true,
            canResetToFactory = true,
            followsDeviceConnection = true
        )

        is SettingsSource.File -> SettingsSourceCapabilities(
            canSave = inPlace,
            canResetToFactory = false,
            followsDeviceConnection = false
        )

        is SettingsSource.LocalConfiguration -> SettingsSourceCapabilities(
            canSave = true,
            canResetToFactory = false,
            followsDeviceConnection = false
        )

        is SettingsSource.RemoteConfiguration, is SettingsSource.Defaults -> SettingsSourceCapabilities(
            canSave = false,
            canResetToFactory = false,
            followsDeviceConnection = false,
            isReadOnly = true
        )
    }

/** Модель блока источника; у подключённого блока — `null` (модель известна из сессии устройства). */
val SettingsSource.deviceNameOrNull: String?
    get() = when (this) {
        SettingsSource.Device -> null
        is SettingsSource.File -> deviceName
        is SettingsSource.LocalConfiguration -> deviceName
        is SettingsSource.RemoteConfiguration -> deviceName
        is SettingsSource.Defaults -> deviceName
    }
