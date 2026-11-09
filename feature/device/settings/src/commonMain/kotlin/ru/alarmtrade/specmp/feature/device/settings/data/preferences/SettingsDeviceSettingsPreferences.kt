package ru.alarmtrade.specmp.feature.device.settings.data.preferences

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsPreferences

private const val KEY_TREE_WIDTH_DP = "tree_width_dp"

internal class SettingsDeviceSettingsPreferences(private val settings: Settings) : DeviceSettingsPreferences {

    private val stored = MutableStateFlow(safeRun { settings.getIntOrNull(KEY_TREE_WIDTH_DP) })
    override val treeWidthDp: StateFlow<Response<Int?>> = stored.asStateFlow()

    override suspend fun setTreeWidthDp(width: Int): Response<Unit> = safeRun {
        settings.putInt(KEY_TREE_WIDTH_DP, width)
        stored.update { Response.Success(width) }
    }
}
