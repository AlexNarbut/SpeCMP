package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Платформенный выбор файла настроек `.dxs`. Реализация — в `composeApp` (диалог выбора файла на desktop, SAF на
 * Android, document picker на iOS); фича получает путь к читаемому файлу.
 *
 * Контракт: на Android/iOS документ копируется во временный файл (SDK ждёт путь) вне main —
 * [SettingsFilePickResult.Picked] с `inPlace = false`; на desktop — путь к самому файлу (`inPlace = true`,
 * «Сохранить» его перезапишет). На каждый [launch] — ровно один результат в [results], доставка на main, буфер
 * до появления подписчика.
 */
interface SettingsFilePicker {
    val results: Flow<SettingsFilePickResult>

    fun launch()
}

sealed interface SettingsFilePickResult {
    /** [name] — исходное имя для показа, [path] — абсолютный путь читаемого файла. */
    data class Picked(val name: String, val path: String, val inPlace: Boolean) : SettingsFilePickResult

    data object Cancelled : SettingsFilePickResult

    data object Failed : SettingsFilePickResult
}

/**
 * Платформенное сохранение файла настроек: системный диалог сохранения (desktop, Android SAF) или экспорт документа
 * (iOS). Файл [sourcePath] — готовый `.dxs` во временном каталоге, [fileName] — имя по умолчанию.
 */
interface SettingsFileSaver {
    suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult
}

sealed interface SettingsFileSaveResult {
    /** [name] — имя сохранённого файла для показа. */
    data class Saved(val name: String) : SettingsFileSaveResult

    data object Cancelled : SettingsFileSaveResult

    data object Failed : SettingsFileSaveResult
}

/** Заглушка для галерей состояний и превью: ничего не выбирает. */
object NoSettingsFilePicker : SettingsFilePicker {
    override val results: Flow<SettingsFilePickResult> = emptyFlow()

    override fun launch() = Unit
}

/** Заглушка для галерей состояний и превью: ничего не сохраняет. */
object NoSettingsFileSaver : SettingsFileSaver {
    override suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult =
        SettingsFileSaveResult.Cancelled
}

/** Имя файла по умолчанию: `<модель> <название>.dxs` без символов, запрещённых в именах файлов. */
fun settingsFileName(deviceName: String, title: String?): String {
    val base = listOf(deviceName, title.orEmpty()).filter { it.isNotBlank() }.joinToString(" ")
        .map { if (it in FORBIDDEN_FILE_CHARS || it.code < SPACE_CODE) '_' else it }
        .joinToString("")
        .trim()
        .ifEmpty { DEFAULT_FILE_NAME }
    return if (base.endsWith(DXS_EXTENSION, ignoreCase = true)) base else base + DXS_EXTENSION
}

private const val DXS_EXTENSION = ".dxs"
private const val DEFAULT_FILE_NAME = "settings"
private const val FORBIDDEN_FILE_CHARS = "/\\:*?\"<>|"
private const val SPACE_CODE = 0x20
