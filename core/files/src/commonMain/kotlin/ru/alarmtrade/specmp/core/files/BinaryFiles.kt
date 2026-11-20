package ru.alarmtrade.specmp.core.files

import ru.alarmtrade.specmp.core.common.file.safeFileName

/**
 * Готовый файл (PDF, архив): сохранить в выбранное пользователем место, отправить системным «Поделиться», открыть во
 * внешнем приложении. Байты — для небольших файлов, `*File` — по пути к готовому файлу без чтения его в память.
 * Создаётся `rememberBinaryFileExporter` в composable и передаётся во ViewModel.
 */
interface BinaryFileExporter {
    val canShare: Boolean

    suspend fun save(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileSaveResult

    suspend fun share(fileName: String, type: BinaryFileType, bytes: ByteArray): Boolean

    suspend fun open(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileOpenResult

    suspend fun saveFile(fileName: String, type: BinaryFileType, path: String): BinaryFileSaveResult

    suspend fun shareFile(fileName: String, type: BinaryFileType, path: String): Boolean
}

/**
 * Выбор файла (импорт архива и т. п.) без лимита размера: файл копируется во временную папку приложения (её
 * содержимое живёт до следующего выбора), результат — путь к копии. Создаётся `rememberBinaryFilePicker`.
 */
interface BinaryFilePicker {
    suspend fun pick(type: BinaryFileType): BinaryFilePickResult
}

/** [pickMimeTypes] шире [mimeType]: провайдеры Android отдают ZIP и как `application/x-zip-compressed`, и как поток. */
data class BinaryFileType(
    val extension: String,
    val mimeType: String,
    val uti: String,
    val pickMimeTypes: List<String> = listOf(mimeType),
) {
    companion object {
        val Pdf = BinaryFileType(extension = "pdf", mimeType = "application/pdf", uti = "com.adobe.pdf")

        val Zip = BinaryFileType(
            extension = "zip",
            mimeType = "application/zip",
            uti = "public.zip-archive",
            pickMimeTypes = listOf("application/zip", "application/x-zip-compressed", "application/octet-stream"),
        )
    }
}

sealed interface BinaryFileSaveResult {
    data class Saved(val name: String) : BinaryFileSaveResult

    data object Cancelled : BinaryFileSaveResult

    data object Failed : BinaryFileSaveResult
}

sealed interface BinaryFileOpenResult {
    data object Opened : BinaryFileOpenResult

    data object NoApp : BinaryFileOpenResult

    data object Failed : BinaryFileOpenResult
}

/** Отмена — не ошибка и ничего не показывает. [path] — копия файла во временной папке приложения. */
sealed interface BinaryFilePickResult {
    data class Picked(val name: String, val path: String) : BinaryFilePickResult

    data object Cancelled : BinaryFilePickResult

    data object Failed : BinaryFilePickResult
}

internal fun BinaryFileSaveResult.toTextResult(): TextFileSaveResult = when (this) {
    is BinaryFileSaveResult.Saved -> TextFileSaveResult.Saved(name)
    BinaryFileSaveResult.Cancelled -> TextFileSaveResult.Cancelled
    BinaryFileSaveResult.Failed -> TextFileSaveResult.Failed
}

internal fun pickedFileName(name: String?, type: BinaryFileType): String =
    safeFileName(name.orEmpty(), fallback = fileNameWithExtension("", type.extension))
