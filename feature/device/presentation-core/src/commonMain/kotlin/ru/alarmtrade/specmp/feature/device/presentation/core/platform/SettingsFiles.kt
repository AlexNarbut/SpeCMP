package ru.alarmtrade.specmp.feature.device.presentation.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Контракт: Android/iOS копируют документ во временный файл вне main (`inPlace = false`); desktop отдаёт сам файл
 * (`inPlace = true`, «Сохранить» его перезапишет). На каждый [launch] — один результат в [results], буфер до
 * подписчика.
 */
interface SettingsFilePicker {
    val results: Flow<SettingsFilePickResult>

    fun launch()
}

sealed interface SettingsFilePickResult {
    data class Picked(val name: String, val path: String, val inPlace: Boolean) : SettingsFilePickResult

    data object Cancelled : SettingsFilePickResult

    data object Failed : SettingsFilePickResult
}

/** [sourcePath] — готовый `.dxs` во временном каталоге, [fileName] — имя по умолчанию. */
interface SettingsFileSaver {
    suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult
}

sealed interface SettingsFileSaveResult {
    data class Saved(val name: String) : SettingsFileSaveResult

    data object Cancelled : SettingsFileSaveResult

    data object Failed : SettingsFileSaveResult
}

object NoSettingsFilePicker : SettingsFilePicker {
    override val results: Flow<SettingsFilePickResult> = emptyFlow()

    override fun launch() = Unit
}

object NoSettingsFileSaver : SettingsFileSaver {
    override suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult =
        SettingsFileSaveResult.Cancelled
}

fun String.withoutSettingsExtension(): String =
    if (endsWith(DXS_EXTENSION, ignoreCase = true)) dropLast(DXS_EXTENSION.length) else this

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
