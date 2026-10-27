package ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/** Источник настроек в ключе маршрута (сериализуется — переживает пересоздание back stack). */
@Serializable
internal sealed interface SourceKey {
    @Serializable
    data class Mine(val id: Long, val deviceName: String, val title: String) : SourceKey

    @Serializable
    data class File(val path: String, val deviceName: String, val name: String, val inPlace: Boolean) : SourceKey

    @Serializable
    data class Cloud(val id: Int, val deviceName: String, val title: String) : SourceKey

    @Serializable
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

/** K1 «Мои конфигурации». */
@Serializable
internal data object MineListRoute : NavKey

/** K2 выбор модели: «С нуля» (`file == null`) или модель для файла `.dxs` (K3). */
@Serializable
internal data class ModelsRoute(val file: SourceKey.File? = null) : NavKey

/** K5 редактор конфигурации. */
@Serializable
internal data class EditorRoute(val source: SourceKey) : NavKey

/** K4 шаг 1: каталог автомобилей с конфигурациями. */
@Serializable
internal data object CloudCarsRoute : NavKey

/** K4 шаг 2: конфигурации автомобиля. */
@Serializable
internal data class CloudListRoute(val carId: Int, val carTitle: String) : NavKey

/** K4 шаг 3: карточка облачной конфигурации. */
@Serializable
internal data class CloudCardRoute(val id: Int, val title: String) : NavKey

/** «Записать в блок»: редактор подключённого блока с загрузкой конфигурации. */
@Serializable
internal data class DeviceWriteRoute(val source: SourceKey) : NavKey
