package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.common.Response

interface FirmwareArchivePreferences {
    val systemsOrder: StateFlow<Response<SystemsOrder>>

    suspend fun setSystemsOrder(order: SystemsOrder): Response<Unit>
}

enum class SystemsOrder { Added, Alphabet }
