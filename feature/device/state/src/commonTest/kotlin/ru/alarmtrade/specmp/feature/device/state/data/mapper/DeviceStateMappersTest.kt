package ru.alarmtrade.specmp.feature.device.state.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import ru.alarmtrade.devices.domain.alarmDevice.event.DeviceMainEventType
import ru.alarmtrade.devices.domain.alarmDevice.state.DeviceState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.AlarmZoneStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.CanState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.CarState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.EvState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.HeaterState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.HeaterType
import ru.alarmtrade.devices.domain.alarmDevice.state.status.SensorStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.ServiceZoneStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.DeviceEventKind
import ru.alarmtrade.specmp.feature.device.state.domain.model.EvCharging
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue

class DeviceStateMappersTest {

    @Test
    fun `empty sdk snapshot is skipped`() {
        assertNull(DeviceState.Empty.toVehicleState())
        assertNull(DeviceState.getDefault(77).toVehicleState())
    }

    @Test
    fun `valid snapshot is mapped`() {
        val state = DeviceState.getDefault(77).copy(isValid = true).toVehicleState()
        assertNotNull(state)
        assertEquals(ZoneValue.OFF, state.zones.values.distinct().single())
    }

    @Test
    fun `alarm zone statuses map to zone values`() {
        assertEquals(ZoneValue.ON, AlarmZoneStatus.OPEN.toZone())
        assertEquals(ZoneValue.OFF, AlarmZoneStatus.CLOSE.toZone())
        assertEquals(ZoneValue.ALARM, AlarmZoneStatus.ALARM.toZone())
    }

    @Test
    fun `shock sensor maps five states, disabled is normal`() {
        assertEquals(ZoneValue.OFF, SensorStatus.DISABLED.toZone())
        assertEquals(ZoneValue.MAIN, SensorStatus.ALARM_TRIGGERED.toZone())
        assertEquals(ZoneValue.MAIN_OFF, SensorStatus.ALARM_ZONE_OFF.toZone())
        assertEquals(ZoneValue.WARNING, SensorStatus.WARNING_TRIGGERED.toZone())
        assertEquals(ZoneValue.WARNING_OFF, SensorStatus.WARNING_ZONE_OFF.toZone())
    }

    @Test
    fun `groups without data are hidden`() {
        val car = CarState.Empty
        assertNull(car.canState.toCan())
        assertNull(EvState.Undef.toEv())
        assertNull(HeaterState.Undef.toHeater())
    }

    @Test
    fun `can group appears with mileage`() {
        val can = CarState.Empty.canState.copy(totalMileage = 84_210, isFrontLeftWindowOpen = true).toCan()
        assertNotNull(can)
        assertEquals(84_210L, can.mileageKm)
        assertEquals(1, can.windowsOpen.size)
        assertNull(can.tires)
    }

    @Test
    fun `undefined can state is hidden`() {
        assertNull(CanState.Undef.toCan())
    }

    @Test
    fun `ev group appears when charging or battery is known`() {
        val ev = EvState.Undef.copy(
            chargingState = EvState.ChargingState.SLOW_CHARGING,
            batteryStateOfCharge = 78f,
        ).toEv()
        assertNotNull(ev)
        assertEquals(EvCharging.SLOW, ev.charging)
        assertEquals(78, ev.socPercent)
    }

    @Test
    fun `heater appears when it reports anything`() {
        val heater = HeaterState.Undef.copy(
            heaterType = HeaterType.WEBASTO,
            heaterStatus = ServiceZoneStatus.OPEN,
        ).toHeater()
        assertNotNull(heater)
        assertEquals(true, heater.on)
    }

    @Test
    fun `main events map, the rest are hidden`() {
        assertEquals(DeviceEventKind.ARMING, DeviceMainEventType.ARMING.toEventKind())
        assertEquals(DeviceEventKind.TRUNK_OPEN, DeviceMainEventType.TRUNK_OPENING.toEventKind())
        assertNull(DeviceMainEventType.POTHOLE_ON_ROAD.toEventKind())
    }
}
