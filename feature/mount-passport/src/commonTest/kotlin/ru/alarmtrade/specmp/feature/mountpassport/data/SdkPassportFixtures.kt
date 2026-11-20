package ru.alarmtrade.specmp.feature.mountpassport.data

import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportListItem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportOrigin
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStatus
import ru.alarmtrade.specialist.domain.mountPassport.PassportCan
import ru.alarmtrade.specialist.domain.mountPassport.PassportClient
import ru.alarmtrade.specialist.domain.mountPassport.PassportPhoto
import ru.alarmtrade.specialist.domain.mountPassport.PassportReadStage
import ru.alarmtrade.specialist.domain.mountPassport.PassportReadStatus
import ru.alarmtrade.specialist.domain.mountPassport.PassportSettingsSnapshot
import ru.alarmtrade.specialist.domain.mountPassport.PassportUnit
import ru.alarmtrade.specialist.domain.mountPassport.PassportUnitOrigin
import ru.alarmtrade.specialist.domain.mountPassport.PassportVehicle
import ru.alarmtrade.specialist.domain.mountPassport.PassportWorks
import ru.alarmtrade.specialist.domain.mountPassport.PassportWorksSource

internal const val CREATED_MS = 1_763_550_300_000L

internal fun sdkPassport(): SdkMountPassport = SdkMountPassport(
    id = "p1",
    number = "20261119-234567-1",
    status = MountPassportStatus.DRAFT,
    origin = MountPassportOrigin.FAST_MOUNT,
    createdAtMs = CREATED_MS,
    updatedAtMs = CREATED_MS + 1000,
    vehicle = PassportVehicle(brand = "Kia", model = "Sportage", years = "2021–2024", plate = "А123ВС777"),
    unit = PassportUnit(
        systemName = "Pandora DX-91",
        systemId = "1234567",
        firmwareVersion = "3.57",
        can = PassportCan(version = "12.05", code = "1234", factory = false),
        origin = PassportUnitOrigin.READ,
    ),
    works = PassportWorks(source = PassportWorksSource.MASTER, settingsWritten = true, durationMinutes = 85),
    photos = listOf(PassportPhoto(id = "ph1", file = "photos/ph1.jpg", thumb = "thumbs/ph1.jpg", addedAtMs = 1)),
    client = PassportClient(name = "Ирина", phone = "+79035551240"),
    settingsSnapshot = PassportSettingsSnapshot(
        deviceModel = "Pandora DX-91",
        takenAtMs = CREATED_MS,
        canFactory = true,
        sizeBytes = 10,
        file = "settings.dxs",
    ),
    readStages = mapOf(PassportReadStage.PERIPHERALS to PassportReadStatus.NOT_READ),
)

internal fun sdkFiles(): MountPassportFiles = MountPassportFiles(
    directory = "/data/passports/p1",
    passportJson = "/data/passports/p1/passport.json",
    settingsDxs = "/data/passports/p1/settings.dxs",
    documentHtml = "/data/passports/p1/document.html",
    photosDirectory = "/data/passports/p1/photos",
    thumbsDirectory = "/data/passports/p1/thumbs",
)

internal fun sdkItem(id: String = "p1", status: MountPassportStatus = MountPassportStatus.DRAFT) =
    MountPassportListItem(
        id = id,
        number = "20261119-234567-1",
        status = status,
        createdAtMs = CREATED_MS,
        updatedAtMs = CREATED_MS,
        systemId = "1234567",
        unitModel = "Pandora DX-91",
        carTitle = "Kia Sportage",
        plate = "А123ВС777",
        vin = null,
        clientName = "Ирина",
        clientPhone = null,
        withoutUnit = false,
        hasSettings = true,
    )
