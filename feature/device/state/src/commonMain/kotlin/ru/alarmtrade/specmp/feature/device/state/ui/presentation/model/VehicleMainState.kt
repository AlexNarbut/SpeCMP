package ru.alarmtrade.specmp.feature.device.state.ui.presentation.model

import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState

internal enum class VehicleMainState { PROGRAM, SERVICE, ALARM, ARMED, DISARMED }

internal fun VehicleState?.mainState(): VehicleMainState = when {
    this == null -> VehicleMainState.DISARMED
    programMode -> VehicleMainState.PROGRAM
    serviceMode -> VehicleMainState.SERVICE
    security == SecurityState.DISARMED -> VehicleMainState.DISARMED
    alarm -> VehicleMainState.ALARM
    else -> VehicleMainState.ARMED
}
