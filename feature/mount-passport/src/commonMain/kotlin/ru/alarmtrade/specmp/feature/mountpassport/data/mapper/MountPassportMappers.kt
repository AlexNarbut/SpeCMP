package ru.alarmtrade.specmp.feature.mountpassport.data.mapper

import kotlinx.datetime.Instant
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportEdit
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportListItem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportOrigin
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStatus
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStorageUsage
import ru.alarmtrade.specialist.domain.mountPassport.PassportBypass as SdkBypass
import ru.alarmtrade.specialist.domain.mountPassport.PassportCheckResult as SdkCheckResult
import ru.alarmtrade.specialist.domain.mountPassport.PassportClient as SdkClient
import ru.alarmtrade.specialist.domain.mountPassport.PassportConfiguration as SdkConfiguration
import ru.alarmtrade.specialist.domain.mountPassport.PassportReadStage as SdkReadStage
import ru.alarmtrade.specialist.domain.mountPassport.PassportReadStatus as SdkReadStatus
import ru.alarmtrade.specialist.domain.mountPassport.PassportTransport as SdkTransport
import ru.alarmtrade.specialist.domain.mountPassport.PassportUnit as SdkUnit
import ru.alarmtrade.specialist.domain.mountPassport.PassportUnitOrigin
import ru.alarmtrade.specialist.domain.mountPassport.PassportVehicle as SdkVehicle
import ru.alarmtrade.specialist.domain.mountPassport.PassportWorks as SdkWorks
import ru.alarmtrade.specialist.domain.mountPassport.PassportWorksSource
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
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportEdit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPeripherals
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhoto
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportSettingsSnapshot
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUsbPeripheral
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportWorks

/** Пути файлов в SDK — относительно папки паспорта ([MountPassportFiles.directory]). */
internal fun SdkMountPassport.toDomain(files: MountPassportFiles): MountPassport = MountPassport(
    id = id,
    number = number,
    status = status.toDomain(),
    origin = when (origin) {
        MountPassportOrigin.FAST_MOUNT -> PassportOrigin.FAST_MOUNT
        MountPassportOrigin.ADVANCED_MOUNT -> PassportOrigin.ADVANCED_MOUNT
        MountPassportOrigin.ARCHIVE -> PassportOrigin.ARCHIVE
    },
    createdAt = Instant.fromEpochMilliseconds(createdAtMs),
    updatedAt = Instant.fromEpochMilliseconds(updatedAtMs),
    vehicle = vehicle.toDomain(),
    unit = unit.toDomain(),
    configuration = configuration?.let { PassportConfiguration(it.name, it.author, it.cloudId) },
    works = works.toDomain(),
    systemCheck = systemCheck.map { PassportCheckTask(it.title, it.result.toDomain()) },
    peripherals = PassportPeripherals(
        bluetooth = peripherals.bluetooth.map {
            PassportBluetoothPeripheral(it.type, it.title, it.number, it.address, it.online)
        },
        usb = peripherals.usb.map { PassportUsbPeripheral(it.type, it.name, it.slot, it.paired, it.online) },
    ),
    photos = photos.map {
        PassportPhoto(
            id = it.id,
            path = files.inDirectory(it.file),
            thumbnailPath = files.inDirectory(it.thumb),
            caption = it.caption,
        )
    },
    remarks = remarks,
    client = PassportClient(client.name, client.phone),
    installer = PassportInstaller(installer.name, installer.contact, installer.organization, installer.address),
    settingsSnapshot = settingsSnapshot?.let {
        PassportSettingsSnapshot(it.deviceModel, Instant.fromEpochMilliseconds(it.takenAtMs), it.canFactory)
    },
    readStages = readStages.entries.associate { (stage, status) -> stage.toDomain() to status.toDomain() },
)

internal fun MountPassportListItem.toDomain(): MountPassportSummary = MountPassportSummary(
    id = id,
    number = number,
    status = status.toDomain(),
    createdAt = Instant.fromEpochMilliseconds(createdAtMs),
    updatedAt = Instant.fromEpochMilliseconds(updatedAtMs),
    systemId = systemId,
    unitModel = unitModel,
    carTitle = carTitle,
    plate = plate,
    vin = vin,
    clientName = clientName,
    clientPhone = clientPhone,
    withoutUnit = withoutUnit,
    hasSettings = hasSettings,
)

internal fun MountPassportStorageUsage.toDomain(): PassportStorageUsage = PassportStorageUsage(passports, bytes)

internal fun PassportEdit.toSdk(): MountPassportEdit = MountPassportEdit(
    vehicle = vehicle.toSdk(),
    configuration = configuration?.toSdk(),
    works = works.toSdk(),
    remarks = remarks,
    client = SdkClient(client.name, client.phone),
    installer = installer.toSdk(),
)

internal fun PassportVehicle.toSdk(): SdkVehicle = SdkVehicle(
    brand = brand,
    model = model,
    years = years,
    catalogCarId = catalogCarId,
    customTitle = customTitle,
    vin = vin,
    plate = plate,
    mileageKm = mileageKm,
)

internal fun PassportConfiguration.toSdk(): SdkConfiguration = SdkConfiguration(name, author, cloudId)

internal fun PassportWorks.toSdk(): SdkWorks = SdkWorks(
    source = if (fromMaster) PassportWorksSource.MASTER else PassportWorksSource.MANUAL,
    settingsWritten = settingsWritten,
    bypass = bypass?.toSdk(),
    clone = clone,
    slave = slave,
    durationMinutes = durationMinutes,
)

private fun MountPassportFiles.inDirectory(relative: String): String = "${directory.trimEnd('/')}/$relative"

private fun MountPassportStatus.toDomain(): PassportStatus = when (this) {
    MountPassportStatus.BLANK -> PassportStatus.BLANK
    MountPassportStatus.DRAFT -> PassportStatus.DRAFT
    MountPassportStatus.ISSUED -> PassportStatus.ISSUED
}

private fun SdkVehicle.toDomain(): PassportVehicle =
    PassportVehicle(brand, model, years, catalogCarId, customTitle, vin, plate, mileageKm)

private fun SdkUnit.toDomain(): PassportUnit = PassportUnit(
    systemName = systemName,
    systemId = systemId,
    firmwareVersion = firmwareVersion,
    can = can?.let { PassportCan(it.version, it.code, it.factory) },
    imei = imei,
    bluetoothModule = bluetoothModule?.let { PassportBluetoothModule(it.firmwareVersion, it.bootloaderVersion) },
    transport = when (transport) {
        SdkTransport.BLUETOOTH -> PassportTransport.BLUETOOTH
        SdkTransport.USB -> PassportTransport.USB
        null -> null
    },
    manual = origin == PassportUnitOrigin.MANUAL,
)

private fun SdkWorks.toDomain(): PassportWorks = PassportWorks(
    fromMaster = source == PassportWorksSource.MASTER,
    settingsWritten = settingsWritten,
    bypass = bypass?.toDomain(),
    clone = clone,
    slave = slave,
    durationMinutes = durationMinutes,
)

private fun SdkBypass.toDomain(): PassportBypass = when (this) {
    SdkBypass.TRAINED -> PassportBypass.TRAINED
    SdkBypass.CLONE_NEEDED -> PassportBypass.CLONE_NEEDED
    SdkBypass.NONE -> PassportBypass.NONE
}

private fun PassportBypass.toSdk(): SdkBypass = when (this) {
    PassportBypass.TRAINED -> SdkBypass.TRAINED
    PassportBypass.CLONE_NEEDED -> SdkBypass.CLONE_NEEDED
    PassportBypass.NONE -> SdkBypass.NONE
}

private fun SdkCheckResult.toDomain(): PassportCheckResult = when (this) {
    SdkCheckResult.PASSED -> PassportCheckResult.PASSED
    SdkCheckResult.FAILED -> PassportCheckResult.FAILED
    SdkCheckResult.SKIPPED -> PassportCheckResult.SKIPPED
}

private fun SdkReadStage.toDomain(): PassportReadStage = when (this) {
    SdkReadStage.UNIT -> PassportReadStage.UNIT
    SdkReadStage.PARAMETERS -> PassportReadStage.PARAMETERS
    SdkReadStage.PERIPHERALS -> PassportReadStage.PERIPHERALS
    SdkReadStage.SETTINGS -> PassportReadStage.SETTINGS
}

private fun SdkReadStatus.toDomain(): PassportReadStatus = when (this) {
    SdkReadStatus.READ -> PassportReadStatus.READ
    SdkReadStatus.NOT_READ -> PassportReadStatus.NOT_READ
    SdkReadStatus.UNAVAILABLE -> PassportReadStatus.UNAVAILABLE
    SdkReadStatus.SKIPPED -> PassportReadStatus.SKIPPED
}
