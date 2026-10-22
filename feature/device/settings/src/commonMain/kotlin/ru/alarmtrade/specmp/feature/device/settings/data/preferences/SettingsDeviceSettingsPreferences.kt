package ru.alarmtrade.specmp.feature.device.settings.data.preferences

import com.russhwolf.settings.Settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsPreferences

private const val KEY_TREE_WIDTH_DP = "tree_width_dp"

/** [DeviceSettingsPreferences] в именованном хранилище приложения (`AppSettingsFactory`). */
class SettingsDeviceSettingsPreferences(private val settings: Settings) : DeviceSettingsPreferences {

    override fun treeWidthDp(): Int? = settings.getIntOrNull(KEY_TREE_WIDTH_DP)

    override fun setTreeWidthDp(width: Int) = settings.putInt(KEY_TREE_WIDTH_DP, width)
}
