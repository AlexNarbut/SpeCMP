package ru.alarmtrade.specmp.core.files

/**
 * Порт выбора текстового файла (импорт): системный диалог, чтение и декодирование UTF-8 — вне main. Создаётся
 * `rememberTextFilePicker` в composable и передаётся во ViewModel.
 */
interface TextFilePicker {
    suspend fun pick(type: TextFileType): TextFilePickResult
}

/** Порт сохранения текста в файл, место выбирает пользователь (экспорт). */
interface TextFileSaver {
    suspend fun save(suggestedName: String, type: TextFileType, text: String): TextFileSaveResult
}

/**
 * [pickMimeTypes] шире [mimeType]: провайдеры Android часто отдают `.json` как `application/octet-stream`.
 * [uti] — тип для пикера iOS.
 */
data class TextFileType(
    val extension: String,
    val mimeType: String,
    val uti: String,
    val pickMimeTypes: List<String> = listOf(mimeType),
    val maxBytes: Long = DEFAULT_MAX_BYTES,
) {
    companion object {
        const val DEFAULT_MAX_BYTES: Long = 1024L * 1024L

        val Json = TextFileType(
            extension = "json",
            mimeType = "application/json",
            uti = "public.json",
            pickMimeTypes = listOf("application/json", "text/*", "application/octet-stream"),
        )
    }
}

/** Отмена — не ошибка и ничего не показывает. */
sealed interface TextFilePickResult {
    data class Picked(val name: String, val text: String) : TextFilePickResult

    data object Cancelled : TextFilePickResult

    data object TooLarge : TextFilePickResult

    data object Failed : TextFilePickResult
}

sealed interface TextFileSaveResult {
    data class Saved(val name: String) : TextFileSaveResult

    data object Cancelled : TextFileSaveResult

    data object Failed : TextFileSaveResult
}
