package ru.alarmtrade.specmp.feature.device.settings.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.common.Response

interface DeviceSettingsPreferences {
    val treeWidthDp: StateFlow<Response<Int?>>

    suspend fun setTreeWidthDp(width: Int): Response<Unit>
}
