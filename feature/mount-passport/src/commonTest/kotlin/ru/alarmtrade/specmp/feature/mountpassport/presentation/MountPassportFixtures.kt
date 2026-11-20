package ru.alarmtrade.specmp.feature.mountpassport.presentation

import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportClient
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPeripherals
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportWorks

internal val CREATED: Instant = Instant.fromEpochMilliseconds(1_763_550_300_000L)

internal const val UNIT_ID = 1234567L

internal fun testDevice(systemId: Long = UNIT_ID): AlarmDevice = AlarmDevice(
    systemId = systemId,
    systemName = "Pandora DX-91",
    firmwareVersion = "VER3.57",
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = null,
)

internal fun summary(
    id: String = "p1",
    status: PassportStatus = PassportStatus.DRAFT,
    systemId: String = UNIT_ID.toString(),
    carTitle: String? = "Kia Sportage",
) = MountPassportSummary(
    id = id,
    number = "20261119-234567-1",
    status = status,
    createdAt = CREATED,
    systemId = systemId,
    unitModel = "Pandora DX-91",
    carTitle = carTitle,
    plate = "А 123 ВС 777",
    vin = "XWEPH81ADN0012345",
    clientName = "Ирина Ковалёва",
    clientPhone = "+79035551240",
    withoutUnit = false,
    hasSettings = true,
)

internal fun testPassport(
    origin: PassportOrigin = PassportOrigin.ADVANCED_MOUNT,
    manual: Boolean = false,
    systemId: String = UNIT_ID.toString(),
) = MountPassport(
    id = "p1",
    number = "20261119-234567-1",
    status = PassportStatus.DRAFT,
    origin = origin,
    createdAt = CREATED,
    updatedAt = CREATED,
    vehicle = PassportVehicle(customTitle = "Toyota RAV4", plate = "О 456 КМ 199"),
    unit = PassportUnit(
        systemName = "Pandora DX-91",
        systemId = systemId,
        firmwareVersion = "3.57",
        can = null,
        imei = null,
        bluetoothModule = null,
        transport = null,
        manual = manual,
    ),
    configuration = null,
    works = PassportWorks(fromMaster = origin == PassportOrigin.FAST_MOUNT, settingsWritten = true),
    systemCheck = emptyList(),
    peripherals = PassportPeripherals(),
    photos = emptyList(),
    remarks = null,
    client = PassportClient(),
    installer = PassportInstaller(name = "Алексей Смирнов", organization = "Автозвук"),
    settingsSnapshot = null,
    readStages = emptyMap(),
)

internal fun linkState(connected: Boolean): DeviceLinkState =
    if (connected) DeviceLinkState.READY else DeviceLinkState.DISCONNECTED
