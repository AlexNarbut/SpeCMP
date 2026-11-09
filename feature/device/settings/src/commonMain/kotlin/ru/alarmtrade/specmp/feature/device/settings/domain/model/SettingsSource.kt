package ru.alarmtrade.specmp.feature.device.settings.domain.model

sealed interface SettingsSource {
    data object Device : SettingsSource

    /** [inPlace] — путь к самому файлу пользователя (desktop): «Сохранить» его перезаписывает; иначе — «как…». */
    data class File(
        val path: String,
        val deviceName: String,
        val name: String = path.substringAfterLast('/'),
        val inPlace: Boolean = true,
    ) : SettingsSource

    data class LocalConfiguration(val id: Long, val deviceName: String, val title: String) : SettingsSource

    data class RemoteConfiguration(val id: Int, val deviceName: String, val title: String) : SettingsSource

    data class Defaults(val deviceName: String) : SettingsSource
}

data class SettingsSourceCapabilities(
    val canSave: Boolean,
    val canResetToFactory: Boolean,
    val followsDeviceConnection: Boolean,
    val isReadOnly: Boolean = false,
)

val SettingsSource.capabilities: SettingsSourceCapabilities
    get() = when (this) {
        SettingsSource.Device -> SettingsSourceCapabilities(
            canSave = true,
            canResetToFactory = true,
            followsDeviceConnection = true,
        )

        is SettingsSource.File -> SettingsSourceCapabilities(
            canSave = inPlace,
            canResetToFactory = false,
            followsDeviceConnection = false,
        )

        is SettingsSource.LocalConfiguration -> SettingsSourceCapabilities(
            canSave = true,
            canResetToFactory = false,
            followsDeviceConnection = false,
        )

        is SettingsSource.RemoteConfiguration, is SettingsSource.Defaults -> SettingsSourceCapabilities(
            canSave = false,
            canResetToFactory = false,
            followsDeviceConnection = false,
            isReadOnly = true,
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
