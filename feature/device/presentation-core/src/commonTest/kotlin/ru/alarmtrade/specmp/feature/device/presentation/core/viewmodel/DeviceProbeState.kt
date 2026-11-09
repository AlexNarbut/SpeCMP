package ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice

internal data class DeviceProbeState(
    val events: List<String> = emptyList(),
    val launched: Boolean? = null,
    val current: AlarmDevice? = null,
)
