package ru.alarmtrade.specmp.feature.device.state.ui.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.ui.vehicle

class VehicleMainStateTest {

    @Test
    fun programModeWins() {
        assertEquals(VehicleMainState.PROGRAM, vehicle(programMode = true).mainState())
    }

    @Test
    fun programWinsOverServiceAndArmed() {
        val state = vehicle(security = SecurityState.ARMED, serviceMode = true, programMode = true)
        assertEquals(VehicleMainState.PROGRAM, state.copy(alarm = true).mainState())
    }

    @Test
    fun serviceModeWinsOverArmed() {
        assertEquals(
            VehicleMainState.SERVICE,
            vehicle(security = SecurityState.ACTIVE_ARMED, serviceMode = true).mainState(),
        )
    }

    @Test
    fun anyArmedStateIsArmed() {
        assertEquals(VehicleMainState.ARMED, vehicle(security = SecurityState.ARMED).mainState())
        assertEquals(VehicleMainState.ARMED, vehicle(security = SecurityState.ACTIVE_ARMED).mainState())
    }

    @Test
    fun alarmWhileArmed() {
        assertEquals(VehicleMainState.ALARM, vehicle(security = SecurityState.ARMED).copy(alarm = true).mainState())
    }

    @Test
    fun disarmed() {
        assertEquals(VehicleMainState.DISARMED, vehicle().mainState())
        assertEquals(VehicleMainState.DISARMED, vehicle().copy(alarm = true).mainState())
    }

    @Test
    fun noDataIsDisarmed() {
        val state: VehicleState? = null
        assertEquals(VehicleMainState.DISARMED, state.mainState())
    }
}
