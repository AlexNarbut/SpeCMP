package ru.alarmtrade.specmp.feature.mountpassport.domain.model

import kotlinx.datetime.Instant

/** Строка списка и поиска (индекс SDK). */
data class MountPassportSummary(
    val id: String,
    val number: String,
    val status: PassportStatus,
    val createdAt: Instant,
    val systemId: String,
    val unitModel: String,
    val carTitle: String?,
    val plate: String?,
    val vin: String?,
    val clientName: String?,
    val clientPhone: String?,
    val withoutUnit: Boolean,
    val hasSettings: Boolean,
)

data class PassportStorageUsage(val passports: Int, val bytes: Long)

/** Ручные поля предотчёта: заменяют разделы паспорта целиком. */
data class PassportEdit(
    val vehicle: PassportVehicle,
    val configuration: PassportConfiguration?,
    val works: PassportWorks,
    val remarks: String?,
    val client: PassportClient,
    val installer: PassportInstaller,
)

class PassportPhotoLimitException(val limit: Int, cause: Throwable? = null) :
    Exception("Mount passport already has $limit photos", cause)

/** На устройстве не хватило места для файла паспорта. */
class PassportStorageFullException(cause: Throwable? = null) : Exception("No space left for mount passport", cause)
