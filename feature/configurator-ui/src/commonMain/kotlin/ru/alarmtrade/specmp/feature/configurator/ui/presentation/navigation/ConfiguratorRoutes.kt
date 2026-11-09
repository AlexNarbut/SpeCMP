package ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

@Serializable
@SerialName("configurator-ui.SourceKey")
internal sealed interface SourceKey {
    @Serializable
    @SerialName("configurator-ui.SourceKey.Mine")
    data class Mine(val id: Long, val deviceName: String, val title: String) : SourceKey

    @Serializable
    @SerialName("configurator-ui.SourceKey.File")
    data class File(val path: String, val deviceName: String, val name: String, val inPlace: Boolean) : SourceKey

    @Serializable
    @SerialName("configurator-ui.SourceKey.Cloud")
    data class Cloud(val id: Int, val deviceName: String, val title: String) : SourceKey

    @Serializable
    @SerialName("configurator-ui.SourceKey.Defaults")
    data class Defaults(val deviceName: String) : SourceKey
}

internal fun SourceKey.toSource(): SettingsSource = when (this) {
    is SourceKey.Mine -> SettingsSource.LocalConfiguration(id, deviceName, title)
    is SourceKey.File -> SettingsSource.File(path, deviceName, name, inPlace)
    is SourceKey.Cloud -> SettingsSource.RemoteConfiguration(id, deviceName, title)
    is SourceKey.Defaults -> SettingsSource.Defaults(deviceName)
}

/** Подключённый блок ключом не бывает — `null`. */
internal fun SettingsSource.toKey(): SourceKey? = when (this) {
    SettingsSource.Device -> null
    is SettingsSource.LocalConfiguration -> SourceKey.Mine(id, deviceName, title)
    is SettingsSource.File -> SourceKey.File(path, deviceName, name, inPlace)
    is SettingsSource.RemoteConfiguration -> SourceKey.Cloud(id, deviceName, title)
    is SettingsSource.Defaults -> SourceKey.Defaults(deviceName)
}

@Serializable
@SerialName("configurator-ui.MineListRoute")
internal data object MineListRoute : NavKey

/** `file == null` — «С нуля». */
@Serializable
@SerialName("configurator-ui.ModelsRoute")
internal data class ModelsRoute(val file: SourceKey.File? = null) : NavKey

@Serializable
@SerialName("configurator-ui.EditorRoute")
internal data class EditorRoute(val source: SourceKey) : NavKey

@Serializable
@SerialName("configurator-ui.CloudCarsRoute")
internal data object CloudCarsRoute : NavKey

@Serializable
@SerialName("configurator-ui.CloudListRoute")
internal data class CloudListRoute(val carId: Int, val carTitle: String) : NavKey

@Serializable
@SerialName("configurator-ui.CloudCardRoute")
internal data class CloudCardRoute(val id: Int, val title: String) : NavKey

@Serializable
@SerialName("configurator-ui.DeviceWriteRoute")
internal data class DeviceWriteRoute(val source: SourceKey) : NavKey
