package ru.alarmtrade.specmp.feature.device.peripherals.data.mapper

import ru.alarmtrade.alarmCpu.pcbaseProtocol.exception.PcBaseNotSupportedException
import ru.alarmtrade.devices.domain.alarmDevice.connection.DeviceType
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.DynamicPeripheralDevice
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.PeripheralDeviceInformation
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.PeripheralDeviceStatus
import ru.alarmtrade.devices.domain.errors.IncorrectServicePinException
import ru.alarmtrade.devices.domain.errors.PeripheralProgramModeException
import ru.alarmtrade.devices.domain.errors.SetProgramModeException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralDetails
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralException
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral

// Таблицы, а не `when`: на каждый тип SDK по ветке — detekt CyclomaticComplexMethod; полноту проверяет тест.
private val TypeBySdk: Map<DeviceType, PeripheralType> = mapOf(
    DeviceType.TAG_760 to PeripheralType.TAG,
    DeviceType.TAG_760_V3 to PeripheralType.TAG,
    DeviceType.REMOTE_CONTROL to PeripheralType.KEYFOB,
    DeviceType.REMOTE_CONTROL_D030 to PeripheralType.KEYFOB,
    DeviceType.REMOTE_CONTROL_D_035 to PeripheralType.KEYFOB,
    DeviceType.WATCH to PeripheralType.WATCH,
    DeviceType.BAND to PeripheralType.BRACELET,
    DeviceType.RELAY_BTR_101 to PeripheralType.RELAY,
    DeviceType.RELAY_BTR_101_V3 to PeripheralType.RELAY,
    DeviceType.HOOD_LOCK to PeripheralType.HOOD_LOCK,
    DeviceType.RHM to PeripheralType.RHM,
    DeviceType.RHM_V2 to PeripheralType.RHM,
    DeviceType.SIREN to PeripheralType.RHM,
    DeviceType.DOOR_MODULE_DMS to PeripheralType.DOOR_SENSOR,
    DeviceType.DOOR_MODULE_DMS_V4 to PeripheralType.DOOR_SENSOR,
    DeviceType.TPMS to PeripheralType.TPMS,
    DeviceType.FUEL_SENSOR to PeripheralType.FUEL_SENSOR,
    DeviceType.PIR_SENSOR to PeripheralType.PIR_SENSOR,
    DeviceType.VS_MODULE to PeripheralType.VS,
    DeviceType.GPS_NAV_03 to PeripheralType.NAV,
    DeviceType.NAV_08 to PeripheralType.NAV,
    DeviceType.NAV_09_10 to PeripheralType.NAV,
    DeviceType.NAV_11 to PeripheralType.NAV,
    DeviceType.NAV_12 to PeripheralType.NAV,
    DeviceType.NAV_13 to PeripheralType.NAV,
    DeviceType.DI_04 to PeripheralType.DI,
    DeviceType.RELAY_BT_02 to PeripheralType.BT02,
    DeviceType.RF_MODULE to PeripheralType.RF_MODULE,
    DeviceType.IMMOBILIZER to PeripheralType.IMMOBILIZER,
    DeviceType.NFC_READER to PeripheralType.NFC_READER,
    DeviceType.FB to PeripheralType.FUNCTION_BUTTON,
    DeviceType.ALT_420 to PeripheralType.ALT_420,
    DeviceType.MOBILE to PeripheralType.PHONE,
    DeviceType.ALARM to PeripheralType.BASE,
)

internal fun DeviceType.toPeripheralType(): PeripheralType = TypeBySdk[this] ?: PeripheralType.UNKNOWN

/** Какие типы SDK спрашивать у блока для типа каталога «Устройств системы». */
private val PollTypes: Map<PeripheralType, List<DeviceType>> = mapOf(
    PeripheralType.TAG to listOf(DeviceType.TAG_760),
    PeripheralType.KEYFOB to listOf(DeviceType.REMOTE_CONTROL),
    PeripheralType.WATCH to listOf(DeviceType.WATCH),
    PeripheralType.BRACELET to listOf(DeviceType.BAND),
    PeripheralType.RELAY to listOf(DeviceType.RELAY_BTR_101),
    PeripheralType.HOOD_LOCK to listOf(DeviceType.HOOD_LOCK),
    PeripheralType.RHM to listOf(DeviceType.RHM, DeviceType.SIREN),
    PeripheralType.DOOR_SENSOR to listOf(DeviceType.DOOR_MODULE_DMS),
    PeripheralType.TPMS to listOf(DeviceType.TPMS),
    PeripheralType.FUEL_SENSOR to listOf(DeviceType.FUEL_SENSOR),
    PeripheralType.PIR_SENSOR to listOf(DeviceType.PIR_SENSOR),
    PeripheralType.VS to listOf(DeviceType.VS_MODULE),
    PeripheralType.NAV to listOf(DeviceType.GPS_NAV_03, DeviceType.NAV_09_10),
    PeripheralType.DI to listOf(DeviceType.DI_04),
    PeripheralType.BT02 to listOf(DeviceType.RELAY_BT_02),
    PeripheralType.RF_MODULE to listOf(DeviceType.RF_MODULE),
)

internal fun PeripheralType.pollTypes(): List<DeviceType> = PollTypes[this].orEmpty()

/** Блок отдаёт и неприживлённые позиции — их не показываем. */
internal fun PeripheralDeviceInformation.toSystemPeripheral(type: PeripheralType): SystemPeripheral? {
    val status = status
    if (!status.isBound) return null
    val link = status.toLink()
    val online = link != PeripheralLink.OFFLINE && link != PeripheralLink.PAIRED
    return SystemPeripheral(
        type = type,
        number = id + 1,
        address = addressHex.uppercase(),
        link = link,
        signalDbm = signalLevel.takeIf { online },
        voltage = voltage(),
        lowBattery = (status as? PeripheralDeviceStatus.Tag)?.lowBatteryLevel == true,
        details = details(),
    )
}

private fun PeripheralDeviceStatus.toLink(): PeripheralLink = when (this) {
    is PeripheralDeviceStatus.Tpms -> PeripheralLink.PAIRED
    is PeripheralDeviceStatus.Tag -> zoneLink(isConnected, inZone)
    is PeripheralDeviceStatus.WithZone -> zoneLink(isConnected, inZone)
    else -> if (isConnected) PeripheralLink.ONLINE else PeripheralLink.OFFLINE
}

private fun zoneLink(connected: Boolean, inZone: Boolean): PeripheralLink = when {
    !connected -> PeripheralLink.OFFLINE
    inZone -> PeripheralLink.NEAR
    else -> PeripheralLink.FAR
}

private fun PeripheralDeviceInformation.voltage(): Float? = when (this) {
    is PeripheralDeviceInformation.Tag -> voltage
    is PeripheralDeviceInformation.Relay -> voltage
    is PeripheralDeviceInformation.PirSensor -> voltage
    is PeripheralDeviceInformation.DoorModule -> voltage
    is PeripheralDeviceInformation.FuelSensor -> voltage
    is PeripheralDeviceInformation.Tpms -> voltage
    else -> null
}?.takeIf { it > 0f }

private fun PeripheralDeviceInformation.details(): PeripheralDetails? = when (this) {
    is PeripheralDeviceInformation.Tag,
    is PeripheralDeviceInformation.Relay,
    is PeripheralDeviceInformation.HoodLock,
    is PeripheralDeviceInformation.VsModule,
    -> statusDetails()

    is PeripheralDeviceInformation.Tpms -> PeripheralDetails.Tpms(pressureBar = pressure, temperature = temperature)

    is PeripheralDeviceInformation.FuelSensor ->
        PeripheralDetails.FuelSensor(liters = fuelLevelLiters, percent = fuelLevelPercents, temperature = temperature)

    is PeripheralDeviceInformation.PirSensor -> PeripheralDetails.Pir(index = index, temperature = temperature)

    is PeripheralDeviceInformation.Rhm -> PeripheralDetails.EngineTemperature(engineTemperature)

    is PeripheralDeviceInformation.Siren -> PeripheralDetails.EngineTemperature(engineTemperature)

    else -> null
}

private fun PeripheralDeviceInformation.statusDetails(): PeripheralDetails? = when (val status = status) {
    is PeripheralDeviceStatus.Tag -> PeripheralDetails.Tag(pressed = status.pressed, blinking = status.isBlinking)

    is PeripheralDeviceStatus.Relay -> PeripheralDetails.Relay(
        blocked = status.isBlocked,
        serviceMode = status.inServiceMode,
    )

    is PeripheralDeviceStatus.VsModule -> PeripheralDetails.Vs(enabled = status.isEnabled)

    is PeripheralDeviceStatus.HoodLock -> (this as? PeripheralDeviceInformation.HoodLock)?.let {
        PeripheralDetails.HoodLock(
            locked = status.isLocked,
            hoodOpened = status.isHoodOpened,
            serviceMode = status.isServiceMode,
            temperature = it.temperature,
            triggerCount = it.triggerCount.toLong(),
        )
    }

    else -> null
}

/** Номер внутри типа — по порядку слотов. */
internal fun List<DynamicPeripheralDevice>.toDynamicPeripherals(): List<DynamicPeripheral> {
    val counters = mutableMapOf<PeripheralType, Int>()
    return sortedBy { it.id }.map { device ->
        val type = device.type.toPeripheralType()
        val number = counters.getOrElse(type) { 0 } + 1
        counters[type] = number
        DynamicPeripheral(
            slot = device.id,
            type = type,
            number = number,
            paired = device.isPaired,
            connected = device.isConnected,
            voltage = device.voltage.takeIf { device.isConnected && it > 0f },
            pirAddress = device.temperature.toInt().takeIf { type == PeripheralType.PIR_SENSOR && device.isConnected },
        )
    }
}

internal fun Throwable.sdkFailure(): PeripheralFailure = when (this) {
    is IncorrectServicePinException -> PeripheralFailure.WRONG_PIN
    is SetProgramModeException -> PeripheralFailure.PROGRAM_MODE_REFUSED
    is PcBaseNotSupportedException -> PeripheralFailure.UNSUPPORTED
    is PeripheralProgramModeException -> PeripheralFailure.NOT_CONFIRMED
    else -> PeripheralFailure.UNKNOWN
}

internal fun <T> Response<T>.withPeripheralFailure(): Response<T> = when (this) {
    is Response.Success -> this
    is Response.Error -> Response.Error(PeripheralException(exception.sdkFailure(), exception))
}
