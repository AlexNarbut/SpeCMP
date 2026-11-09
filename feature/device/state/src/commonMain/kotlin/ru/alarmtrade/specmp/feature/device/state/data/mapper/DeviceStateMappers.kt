package ru.alarmtrade.specmp.feature.device.state.data.mapper

import ru.alarmtrade.devices.domain.alarmDevice.event.DeviceMainEventType
import ru.alarmtrade.devices.domain.alarmDevice.state.DeviceState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.AlarmDeviceState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.AlarmZoneStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.AutomaticStartState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.CanState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.EngineStatus as SdkEngineStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.EvState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.ExternalCameraState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.HandsFreeStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.HeaterState
import ru.alarmtrade.devices.domain.alarmDevice.state.status.HeaterType
import ru.alarmtrade.devices.domain.alarmDevice.state.status.SecurityStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.SensorStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.ServiceZoneStatus
import ru.alarmtrade.devices.domain.alarmDevice.state.status.SirenSignalStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.CameraInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.DeviceEventKind
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.EvCharging
import ru.alarmtrade.specmp.feature.device.state.domain.model.EvInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.HandsFreeMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.HeaterInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.LinkInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.PowerInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.SirenMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.TirePressure
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Window
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue

private const val GSM_LEVEL_MAX = 3

/**
 * SDK не отдаёт `null`, а заполняет отсутствующее нулями (`-1`, `UNKNOWN`): группа считается присланной, если в ней
 * есть ненулевое значение. Пустой стартовый снимок (`deviceId == -1`) — `null`.
 */
internal fun DeviceState.toVehicleState(): VehicleState? {
    if (deviceId < 0 || !isValid) return null
    val car = carState
    val alarmDevice = alarmDeviceState
    return VehicleState(
        security = alarmDevice.securityStatus.toSecurity(),
        alarm = alarmDevice.isAlarm,
        serviceMode = alarmDevice.serviceMode == ServiceZoneStatus.OPEN,
        programMode = alarmDevice.programMode == ServiceZoneStatus.OPEN,
        zones = mapOf(
            Zone.DOOR_FRONT_LEFT to car.frontLeftDoor.toZone(),
            Zone.DOOR_FRONT_RIGHT to car.frontRightDoor.toZone(),
            Zone.DOOR_REAR_LEFT to car.rearLeftDoor.toZone(),
            Zone.DOOR_REAR_RIGHT to car.rearRightDoor.toZone(),
            Zone.HOOD to car.hood.toZone(),
            Zone.TRUNK to car.trunk.toZone(),
            Zone.IGNITION to car.ignition.toZone(),
            Zone.BRAKE to car.brake.toZone(),
            Zone.HANDBRAKE to car.handBrake.toZone(),
            Zone.NEUTRAL to car.parking.toZone(),
            Zone.LIGHTS to if (car.light) ZoneValue.ON else ZoneValue.OFF,
            Zone.SHOCK to car.shockSensor.toZone(),
            Zone.EXTRA_SENSOR to car.additionalSensor.toZone(),
            Zone.MOTION to car.movementSensor.toZone(),
            Zone.TILT to car.tiltSensor.toZone(),
        ),
        engine = EngineInfo(
            status = car.engineState.engineStatus.toEngine(),
            temperature = car.engineState.temperature,
            rpm = car.engineState.rpm.takeIf { it > 0 && car.engineState.engineStatus.isStarted() },
            autostart = car.automaticStart.toAutostart(),
        ),
        power = PowerInfo(
            voltage = car.voltage,
            cabinTemp = car.cabinTemp,
            outsideTemp = car.outsideTemp,
            fuelPercent = car.fuel.fuelInPercents.takeIf { it > 0 },
        ),
        link = alarmDevice.toLink(),
        event = lastEvent.event.eventType.toEventKind(),
        can = car.canState.toCan(),
        ev = car.evState.toEv(),
        heater = car.heaterState.toHeater(),
        camera = alarmDevice.cameraState.toCamera(),
        siren = alarmDevice.sirenSignalStatus.toSiren(),
        handsFree = alarmDevice.handsFreeStatus.toHandsFree(),
    )
}

internal fun SecurityStatus.toSecurity(): SecurityState = when (this) {
    SecurityStatus.ARMED -> SecurityState.ARMED
    SecurityStatus.ACTIVE_ARMED -> SecurityState.ACTIVE_ARMED
    SecurityStatus.DISARMED -> SecurityState.DISARMED
}

internal fun AlarmZoneStatus.toZone(): ZoneValue = when (this) {
    AlarmZoneStatus.OPEN -> ZoneValue.ON
    AlarmZoneStatus.CLOSE -> ZoneValue.OFF
    AlarmZoneStatus.ALARM -> ZoneValue.ALARM
}

/** Датчик удара / доп. датчик: выключен — «Норма» (решение дизайна). */
internal fun SensorStatus.toZone(): ZoneValue = when (this) {
    SensorStatus.DISABLED -> ZoneValue.OFF
    SensorStatus.ALARM_TRIGGERED -> ZoneValue.MAIN
    SensorStatus.ALARM_ZONE_OFF -> ZoneValue.MAIN_OFF
    SensorStatus.WARNING_TRIGGERED -> ZoneValue.WARNING
    SensorStatus.WARNING_ZONE_OFF -> ZoneValue.WARNING_OFF
}

private fun SdkEngineStatus.toEngine(): EngineStatus = when (this) {
    SdkEngineStatus.STOP -> EngineStatus.STOPPED
    SdkEngineStatus.READY -> EngineStatus.READY
    SdkEngineStatus.START, SdkEngineStatus.READY_AND_START -> EngineStatus.RUNNING
}

private fun AutomaticStartState.toAutostart(): Autostart = when (automaticStartStatus) {
    AutomaticStartState.AutomaticStartStatus.ENGINE_STARTED -> Autostart.Running(timeToStopInSeconds.coerceAtLeast(0))
    AutomaticStartState.AutomaticStartStatus.SETTING_ENABLED -> Autostart.Enabled
    AutomaticStartState.AutomaticStartStatus.DISABLED -> Autostart.Disabled
}

private fun AlarmDeviceState.toLink(): LinkInfo {
    val gpsFix = gps.coordinates.lat != 0f && gps.coordinates.long != 0f
    return LinkInfo(
        gsmLevel = gsm.gsmLevel.coerceIn(0, GSM_LEVEL_MAX).takeIf { gsm.gsmStatus == ServiceZoneStatus.OPEN },
        gpsFix = gpsFix,
        speedKmh = gps.speed.toInt().takeIf { gpsFix },
        sms = gsm.smsEnabled,
        calls = gsm.callsEnabled,
    )
}

internal fun CanState.toCan(): CanInfo? {
    val windows = buildSet {
        if (isFrontLeftWindowOpen) add(Window.FRONT_LEFT)
        if (isFrontRightWindowOpen) add(Window.FRONT_RIGHT)
        if (isRearLeftWindowOpen) add(Window.REAR_LEFT)
        if (isRearRightWindowOpen) add(Window.REAR_RIGHT)
    }
    val tires = listOf(frontLeftTpms, frontRightTpms, rearLeftTpms, rearRightTpms).takeIf { all -> all.any { it > 0f } }
        ?.let { TirePressure(it[0], it[1], it[2], it[3], reserveTpms.takeIf { spare -> spare > 0f }) }
    val can = CanInfo(
        mileageKm = totalMileage.takeIf { it > 0 },
        rangeKm = distanceToEmpty.takeIf { it > 0 },
        consumption = fuelConsumption.takeIf { it > 0 },
        washerLow = lowWasherLiquid,
        windowsOpen = windows,
        tires = tires,
    )
    val empty = can.mileageKm == null && can.rangeKm == null && can.consumption == null && !can.washerLow &&
        windows.isEmpty() && tires == null
    return can.takeUnless { empty }
}

internal fun EvState.toEv(): EvInfo? {
    val charging = when (chargingState) {
        EvState.ChargingState.UNKNOWN -> null
        EvState.ChargingState.DISCONNECTED -> EvCharging.NONE
        EvState.ChargingState.CONNECTED -> EvCharging.CONNECTED
        EvState.ChargingState.SLOW_CHARGING -> EvCharging.SLOW
        EvState.ChargingState.FAST_CHARGING -> EvCharging.FAST
    }
    val noBattery = batteryStateOfCharge <= 0f && batteryStateOfHealth <= 0f
    if (noBattery && (charging == null || charging == EvCharging.NONE)) return null
    return EvInfo(
        charging = charging ?: EvCharging.NONE,
        socPercent = batteryStateOfCharge.toInt(),
        sohPercent = batteryStateOfHealth.toInt(),
        batteryTemp = batteryTemperature,
        rangeKm = distanceToEmpty.takeIf { it > 0 },
    )
}

internal fun HeaterState.toHeater(): HeaterInfo? {
    val on = heaterStatus == ServiceZoneStatus.OPEN
    val silent = heaterType == HeaterType.UNKNOWN && !on && temp == 0 && voltage <= 0f && power == 0 && errors.isEmpty()
    if (silent) return null
    return HeaterInfo(
        on = on,
        temperature = temp,
        voltage = voltage,
        powerPercent = power,
        hasErrors = errors.isNotEmpty(),
    )
}

private fun ExternalCameraState.toCamera(): CameraInfo? =
    if (isInstalled) CameraInfo(recording = isRecording, minutes = recordingTimeInMinutes.takeIf { it >= 0 }) else null

private fun SirenSignalStatus.toSiren(): SirenMode = when (this) {
    SirenSignalStatus.OFF -> SirenMode.OFF
    SirenSignalStatus.ONLY_SIGNALS -> SirenMode.SIGNALS
    SirenSignalStatus.ONLY_WARNING_SIGNALS -> SirenMode.WARNINGS
}

private fun HandsFreeStatus.toHandsFree(): HandsFreeMode = when (this) {
    HandsFreeStatus.OFF -> HandsFreeMode.OFF
    HandsFreeStatus.ONLY_ARMING -> HandsFreeMode.ARMING
    HandsFreeStatus.ONLY_DISARMING -> HandsFreeMode.DISARMING
    HandsFreeStatus.ARMING_AND_DISARMING -> HandsFreeMode.BOTH
}

@Suppress("CyclomaticComplexMethod")
internal fun DeviceMainEventType.toEventKind(): DeviceEventKind? = when (this) {
    DeviceMainEventType.ARMING, DeviceMainEventType.ACTIVE_SECURITY_ARMING -> DeviceEventKind.ARMING
    DeviceMainEventType.DISARMING, DeviceMainEventType.ACTIVE_SECURITY_DISARMING -> DeviceEventKind.DISARMING
    DeviceMainEventType.ALARM, DeviceMainEventType.ACTIVE_SECURITY_ALARM -> DeviceEventKind.ALARM
    DeviceMainEventType.REMOTE_ENGINE_START -> DeviceEventKind.ENGINE_START
    DeviceMainEventType.ENGINE_STOP -> DeviceEventKind.ENGINE_STOP
    DeviceMainEventType.UNSUCCESSFUL_ENGINE_START -> DeviceEventKind.ENGINE_START_FAILED
    DeviceMainEventType.PROLONG_ENGINE_RUNNING -> DeviceEventKind.AUTOSTART_EXTENDED
    DeviceMainEventType.SERVICE_MODE_ACTIVATION -> DeviceEventKind.SERVICE_MODE_ON
    DeviceMainEventType.SERVICE_MODE_DEACTIVATION -> DeviceEventKind.SERVICE_MODE_OFF
    DeviceMainEventType.ENTER_PROGRAMING_MODE -> DeviceEventKind.PROGRAM_MODE_ON
    DeviceMainEventType.TRUNK_OPENING -> DeviceEventKind.TRUNK_OPEN
    DeviceMainEventType.ENGINE_HEATER_ACTIVATING -> DeviceEventKind.HEATER_ON
    DeviceMainEventType.ENGINE_HEATER_DEACTIVATING -> DeviceEventKind.HEATER_OFF
    DeviceMainEventType.LOW_VOLTAGE -> DeviceEventKind.LOW_VOLTAGE
    DeviceMainEventType.POWER_OFF -> DeviceEventKind.POWER_OFF
    DeviceMainEventType.SETTINGS_CHANGED -> DeviceEventKind.SETTINGS_CHANGED
    DeviceMainEventType.CAR_STATE -> DeviceEventKind.CAR_STATE
    else -> null
}
