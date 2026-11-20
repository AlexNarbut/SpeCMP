package ru.alarmtrade.specmp.feature.mountpassport.gallery

import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportBluetoothModule
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportBluetoothPeripheral
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportBypass
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCan
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckTask
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportClient
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportConfiguration
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPeripherals
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhoto
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportSettingsSnapshot
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportWorks

internal val AT: Instant = Instant.fromEpochMilliseconds(1_763_550_300_000L)

internal fun row(
    id: String,
    car: String?,
    plate: String?,
    status: PassportStatus,
    systemId: String = "1234567",
    model: String = "Pandora DX-91",
    withoutUnit: Boolean = false,
    settings: Boolean = true,
    client: String? = null,
) = MountPassportSummary(
    id = id,
    number = "20261119-$systemId-1",
    status = status,
    createdAt = AT,
    systemId = systemId,
    unitModel = model,
    carTitle = car,
    plate = plate,
    vin = "XWEPH81ADN0012345",
    clientName = client,
    clientPhone = "+79035551240",
    withoutUnit = withoutUnit,
    hasSettings = settings,
)

internal val rows = listOf(
    row("p1", "Kia Sportage", "А 123 ВС 777", PassportStatus.DRAFT, client = "Ирина Ковалёва"),
    row(
        "p2",
        "Volkswagen Polo",
        null,
        PassportStatus.BLANK,
        systemId = "107131",
        model = "Pandora DX-4GS",
        settings = false,
    ),
    row("p3", "Toyota RAV4", "О 456 КМ 199", PassportStatus.ISSUED, systemId = "1180042"),
    row(
        "p4",
        null,
        null,
        PassportStatus.ISSUED,
        systemId = "107131",
        model = "Pandora DX-4GS",
        withoutUnit = true,
        settings = false,
    ),
    row(
        "p5",
        "Lada Vesta",
        "Е 205 ТО 750",
        PassportStatus.ISSUED,
        systemId = "3100772",
        model = "Pandora VX-4G",
        settings = false,
    ),
)

internal fun fastPassport(): MountPassport = MountPassport(
    id = "p1",
    number = "20261119-234567-1",
    status = PassportStatus.DRAFT,
    origin = PassportOrigin.FAST_MOUNT,
    createdAt = AT,
    updatedAt = AT,
    vehicle = PassportVehicle(brand = "Kia", model = "Sportage", years = "2021–2024", plate = "А 123 ВС 777"),
    unit = PassportUnit(
        systemName = "Pandora DX-91",
        systemId = "1234567",
        firmwareVersion = "3.57",
        can = PassportCan("12.05", "1234", factory = false),
        imei = "861234050118734",
        bluetoothModule = PassportBluetoothModule("2.8", "1.12"),
        transport = PassportTransport.BLUETOOTH,
        manual = false,
    ),
    configuration = PassportConfiguration("Kia Sportage 2022 · автозапуск", "Pandora", "42"),
    works = PassportWorks(
        fromMaster = true,
        settingsWritten = true,
        bypass = PassportBypass.CLONE_NEEDED,
        clone = true,
        slave = true,
        durationMinutes = 85,
    ),
    systemCheck = listOf(
        PassportCheckTask("Постановка на охрану с брелока", PassportCheckResult.PASSED),
        PassportCheckTask("Открытие дверей", PassportCheckResult.PASSED),
        PassportCheckTask("Датчик удара", PassportCheckResult.SKIPPED),
        PassportCheckTask("Автозапуск двигателя", PassportCheckResult.FAILED),
    ),
    peripherals = PassportPeripherals(
        bluetooth = listOf(
            PassportBluetoothPeripheral("TAG", "Метка BT-760", 1, "4C:11:AE:9F:20:01", true),
            PassportBluetoothPeripheral("KEYCHAIN", "Брелок D-030", 1, "4C:11:AE:12:7B:44", true),
        ),
    ),
    photos = listOf(
        PassportPhoto("ph1", "/none/1.jpg", "/none/t1.jpg", "Блок под панелью"),
        PassportPhoto("ph2", "/none/2.jpg", "/none/t2.jpg", null),
    ),
    remarks = null,
    client = PassportClient(name = "Ирина", phone = "+79035551240"),
    installer = PassportInstaller(name = "Алексей Смирнов", contact = "+7 912 345-67-89", organization = "Автозвук"),
    settingsSnapshot = PassportSettingsSnapshot("Pandora DX-91", AT, canFactory = false),
    readStages = emptyMap(),
)

internal fun partialPassport(): MountPassport = fastPassport().copy(
    origin = PassportOrigin.ADVANCED_MOUNT,
    vehicle = PassportVehicle(customTitle = "Toyota RAV4"),
    configuration = null,
    works = PassportWorks(settingsWritten = true),
    systemCheck = emptyList(),
    settingsSnapshot = null,
    readStages = mapOf(
        PassportReadStage.PARAMETERS to PassportReadStatus.NOT_READ,
        PassportReadStage.PERIPHERALS to PassportReadStatus.NOT_READ,
        PassportReadStage.SETTINGS to PassportReadStatus.NOT_READ,
    ),
)

internal fun noUnitPassport(): MountPassport = fastPassport().copy(
    origin = PassportOrigin.ARCHIVE,
    vehicle = PassportVehicle(),
    unit = fastPassport().unit.copy(manual = true, can = null, imei = null, bluetoothModule = null, transport = null),
    configuration = null,
    works = PassportWorks(),
    systemCheck = emptyList(),
    peripherals = PassportPeripherals(),
    photos = emptyList(),
    settingsSnapshot = null,
)
