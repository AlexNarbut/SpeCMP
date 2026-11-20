package ru.alarmtrade.specmp.feature.mountpassport.data.mapper

import kotlinx.datetime.Instant
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportExport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportDecision
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportFailure
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportInspection
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportItem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportProblem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportDecision
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportFailure
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportInspection
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportItem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportProblem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult

internal fun MountPassportExport.toDomain() = PassportArchive(path, fileName, sizeBytes, passports)

internal fun MountPassportImportInspection.toDomain(): PassportImportInspection = when (this) {
    is MountPassportImportInspection.Ready ->
        PassportImportInspection.Ready(passports.map { it.toDomain() }, problems.map { it.toDomain() })

    MountPassportImportInspection.NotPassportArchive -> PassportImportInspection.NotArchive

    is MountPassportImportInspection.NothingToImport ->
        PassportImportInspection.NothingToImport(problems.map { it.toDomain() })
}

internal fun MountPassportImportItem.toDomain() = PassportImportItem(
    key = key,
    number = number,
    carTitle = carTitle,
    plate = plate,
    createdAt = Instant.fromEpochMilliseconds(createdAtMs),
    systemId = systemId,
    unitModel = unitModel,
    hasSettings = hasSettings,
    photos = photos,
    exists = exists,
)

internal fun MountPassportImportProblem.toDomain() = PassportImportProblem(
    key = key,
    number = number,
    reason = when (reason) {
        MountPassportImportFailure.UNSUPPORTED_SCHEMA -> PassportImportFailure.UNSUPPORTED_SCHEMA
        MountPassportImportFailure.UNREADABLE -> PassportImportFailure.UNREADABLE
        MountPassportImportFailure.WRITE_FAILED -> PassportImportFailure.WRITE_FAILED
    },
)

internal fun MountPassportImportResult.toDomain() =
    PassportImportResult(imported.size, replaced.size, skipped, problems.map { it.toDomain() })

internal fun PassportImportDecision.toSdk(): MountPassportImportDecision = when (this) {
    PassportImportDecision.SKIP -> MountPassportImportDecision.SKIP
    PassportImportDecision.REPLACE -> MountPassportImportDecision.REPLACE
}
