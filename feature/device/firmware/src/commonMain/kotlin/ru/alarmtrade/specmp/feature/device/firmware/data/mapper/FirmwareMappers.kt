package ru.alarmtrade.specmp.feature.device.firmware.data.mapper

import ru.alarmtrade.devices.domain.alarmDevice.DataWritingError
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateState
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateStatus
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmwareType
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.domain.appDevice.ConnectionType
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateException
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.WriteFailureReason
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

internal fun FirmwareChannel.toSdk(): SubsystemFirmwareType = when (this) {
    FirmwareChannel.RELEASE -> SubsystemFirmwareType.RELEASE
    FirmwareChannel.BETA -> SubsystemFirmwareType.BETA
    FirmwareChannel.ALPHA -> SubsystemFirmwareType.ALPHA
}

internal fun SubsystemInformation.toModule(transport: ConnectionTransport): FirmwareModule = FirmwareModule(
    id = id,
    kind = type.toKind(),
    systemName = systemName,
    title = title,
    bootTarget = type.bootTarget(transport),
)

/**
 * Обновить можно, когда это разрешает SDK (`isBootSupported`) и приложение умеет войти в загрузчик этого типа;
 * иначе `null`.
 */
internal fun SubsystemType.bootTarget(transport: ConnectionTransport): BootModeTarget? {
    val connectionType = when (transport) {
        ConnectionTransport.BLE -> ConnectionType.BLUETOOTH

        ConnectionTransport.USB -> ConnectionType.USB

        // COM: загрузчик .frm SDK входит сам внутри обновления по COM (`SerialFirmwareRepository`), не через этот путь.
        ConnectionTransport.SERIAL -> return null
    }
    if (!isBootSupported(connectionType)) return null
    return when (this) {
        SubsystemType.ALARM_MAIN,
        SubsystemType.WATCH_MAIN,
        SubsystemType.GPS_TRACKER_MAIN,
        SubsystemType.BMW_BYPASS_MAIN,
        SubsystemType.REMOTE_CONTROL_MAIN,
        -> BootModeTarget.MainProgram

        SubsystemType.BLUETOOTH_MODULE -> BootModeTarget.BluetoothModule

        SubsystemType.BATTERY_CHARGER_MAIN,
        SubsystemType.WATCH_INTERFACE,
        SubsystemType.WATCH_BLUETOOTH,
        SubsystemType.FUEL_SENSOR,
        SubsystemType.CHARGING_STATION_TESTER,
        SubsystemType.CAN_STM32H563IIK,
        SubsystemType.MICROBOT_MAIN,
        SubsystemType.UNKNOWN,
        -> null
    }
}

// Таблица соответствия типов: сложность — число веток, делить нечего
@Suppress("CyclomaticComplexMethod")
internal fun SubsystemType.toKind(): FirmwareModuleKind = when (this) {
    SubsystemType.ALARM_MAIN -> FirmwareModuleKind.AlarmMain
    SubsystemType.BLUETOOTH_MODULE -> FirmwareModuleKind.BluetoothModule
    SubsystemType.BATTERY_CHARGER_MAIN -> FirmwareModuleKind.BatteryCharger
    SubsystemType.WATCH_MAIN -> FirmwareModuleKind.WatchMain
    SubsystemType.WATCH_INTERFACE -> FirmwareModuleKind.WatchInterface
    SubsystemType.REMOTE_CONTROL_MAIN -> FirmwareModuleKind.RemoteControlMain
    SubsystemType.GPS_TRACKER_MAIN -> FirmwareModuleKind.GpsTracker
    SubsystemType.WATCH_BLUETOOTH -> FirmwareModuleKind.WatchBluetooth
    SubsystemType.FUEL_SENSOR -> FirmwareModuleKind.FuelSensor
    SubsystemType.BMW_BYPASS_MAIN -> FirmwareModuleKind.BmwBypass
    SubsystemType.CHARGING_STATION_TESTER -> FirmwareModuleKind.ChargingStationTester
    SubsystemType.CAN_STM32H563IIK -> FirmwareModuleKind.CanModule
    SubsystemType.MICROBOT_MAIN -> FirmwareModuleKind.Microbot
    SubsystemType.UNKNOWN -> FirmwareModuleKind.Unknown
}

internal fun SubsystemFirmware.toDomain(): FirmwareVersion = FirmwareVersion(
    id = id,
    version = version,
    // Сервер отдаёт локальное время без зоны: дата берётся как есть (см. KDoc FirmwareVersion).
    createdOn = creationTime.date,
    description = description,
    url = url,
    canCode = canCode?.takeIf { it.isNotBlank() },
    canVersion = canVersionString?.takeIf { it.isNotBlank() },
    canVersionCode = canVersion,
)

internal fun FirmwareUpdateState.toProgress(): Response<FirmwareUpdateProgress> = when (status) {
    FirmwareUpdateStatus.SENDING_DATA -> Response.Success(FirmwareUpdateProgress.Sending(stateProgress))

    FirmwareUpdateStatus.CHECKING_CRC -> Response.Success(FirmwareUpdateProgress.Verifying)

    FirmwareUpdateStatus.APPLYING -> Response.Success(FirmwareUpdateProgress.Applying)

    FirmwareUpdateStatus.FINISHED_SUCCESS -> Response.Success(FirmwareUpdateProgress.Finished)

    FirmwareUpdateStatus.FINISHED_ERROR ->
        Response.Error(FirmwareUpdateException(FirmwareFailure.Write(error.toReason())))
}

private fun DataWritingError?.toReason(): WriteFailureReason = when (this) {
    DataWritingError.CRC -> WriteFailureReason.Crc
    DataWritingError.TIMEOUT -> WriteFailureReason.Timeout
    DataWritingError.LOST_PACKET -> WriteFailureReason.LostPacket
    DataWritingError.WRONG_PACKET_LENGTH -> WriteFailureReason.WrongPacketLength
    DataWritingError.DEVICE_NOT_READY -> WriteFailureReason.DeviceNotReady
    DataWritingError.APPLYING_ERROR -> WriteFailureReason.ApplyingError
    DataWritingError.UNKNOWN, null -> WriteFailureReason.Unknown
}

internal fun DeviceInformation.toSystem(): FirmwareSystem = FirmwareSystem(
    id = id,
    name = name,
    subsystems = subsystems.map { FirmwareSubsystem(it.id, it.title, it.systemName, it.type.toKind()) },
)
