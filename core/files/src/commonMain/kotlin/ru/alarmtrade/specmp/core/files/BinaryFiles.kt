package ru.alarmtrade.specmp.core.files

/**
 * Готовый файл из байтов (PDF, архив): сохранить в выбранное пользователем место, отправить системным «Поделиться»,
 * открыть во внешнем приложении. Создаётся `rememberBinaryFileExporter` в composable и передаётся во ViewModel.
 */
interface BinaryFileExporter {
    val canShare: Boolean

    suspend fun save(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileSaveResult

    suspend fun share(fileName: String, type: BinaryFileType, bytes: ByteArray): Boolean

    suspend fun open(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileOpenResult
}

data class BinaryFileType(val extension: String, val mimeType: String, val uti: String) {
    companion object {
        val Pdf = BinaryFileType(extension = "pdf", mimeType = "application/pdf", uti = "com.adobe.pdf")
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

internal fun BinaryFileSaveResult.toTextResult(): TextFileSaveResult = when (this) {
    is BinaryFileSaveResult.Saved -> TextFileSaveResult.Saved(name)
    BinaryFileSaveResult.Cancelled -> TextFileSaveResult.Cancelled
    BinaryFileSaveResult.Failed -> TextFileSaveResult.Failed
}
