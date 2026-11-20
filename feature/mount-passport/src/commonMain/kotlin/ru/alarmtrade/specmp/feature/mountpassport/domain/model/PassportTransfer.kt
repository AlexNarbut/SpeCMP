package ru.alarmtrade.specmp.feature.mountpassport.domain.model

import kotlinx.datetime.Instant

/** Состав архива: PDF и данные паспорта — всегда; снимок настроек — по умолчанию да, фото 2048 px — нет. */
data class PassportExportOptions(val includeSettings: Boolean = true, val includePhotos: Boolean = false)

/** Готовый ZIP во временной папке SDK: [path] — сам файл, [fileName] — имя для «Поделиться» / «Сохранить». */
data class PassportArchive(val path: String, val fileName: String, val sizeBytes: Long, val passports: Int)

/** PDF документа паспорта для архива — делает приложение. */
fun interface PassportPdfSource {
    suspend fun pdf(passportId: String): ByteArray
}

/** Миниатюра фото из JPEG оригинала — для архивов без миниатюр. */
fun interface PassportThumbnailer {
    suspend fun thumbnail(jpeg: ByteArray): ByteArray
}

sealed interface PassportImportInspection {
    data class Ready(val items: List<PassportImportItem>, val problems: List<PassportImportProblem>) :
        PassportImportInspection

    /** Не ZIP, повреждён или без данных паспорта. */
    data object NotArchive : PassportImportInspection

    /** Архив паспортов, но ни один паспорт не читается. */
    data class NothingToImport(val problems: List<PassportImportProblem>) : PassportImportInspection
}

/** Паспорт в архиве; [key] — ключ решения о дубликате, [exists] — такой паспорт уже есть на устройстве. */
data class PassportImportItem(
    val key: String,
    val number: String,
    val carTitle: String?,
    val plate: String?,
    val createdAt: Instant,
    val systemId: String,
    val unitModel: String,
    val hasSettings: Boolean,
    val photos: Int,
    val exists: Boolean,
)

data class PassportImportProblem(val key: String, val number: String?, val reason: PassportImportFailure)

/** `UNSUPPORTED_SCHEMA` — из более новой версии приложения, `UNREADABLE` — повреждён, `WRITE_FAILED` — не записался. */
enum class PassportImportFailure { UNSUPPORTED_SCHEMA, UNREADABLE, WRITE_FAILED }

enum class PassportImportDecision { SKIP, REPLACE }

data class PassportImportResult(
    val imported: Int,
    val replaced: Int,
    val skipped: Int,
    val problems: List<PassportImportProblem>,
)

/** Файл импорта — не архив паспортов. */
class PassportNotArchiveException(cause: Throwable? = null) : Exception("Not a mount passport archive", cause)
