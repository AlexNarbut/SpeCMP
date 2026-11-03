package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

/**
 * Файл прошивки из архива без блока: сохранить себе, отдать в другое приложение, открыть папку.
 *
 * Реализация — в платформенном слое `composeApp`: Android — «Загрузки/Pandora Spec» (MediaStore) и системное
 * «Поделиться» (`ACTION_SEND` через FileProvider); iOS — экспорт через «Файлы» и `UIActivityViewController`;
 * desktop — `~/Downloads/Pandora Spec` и «Открыть папку» (поделиться нечем).
 */
interface FirmwareFileExporter {
    /** Куда попадает сохранённый файл — от этого текст диалога «Прошивка скачана». */
    val target: FirmwareSaveTarget

    /** Системное «Поделиться» есть на платформе. */
    val canShare: Boolean

    /** Открыть папку с сохранённым файлом умеет платформа (desktop). */
    val canOpenFolder: Boolean

    /** Сохраняет [bytes] под именем [fileName]; отмена пользователем — не ошибка. */
    suspend fun save(fileName: String, bytes: ByteArray): FirmwareSaveResult

    /** Системное меню «Поделиться» с файлом; `false` — не открылось. */
    suspend fun share(fileName: String, bytes: ByteArray): Boolean

    /** Папка с файлом [path] (результат [save]) в файловом менеджере; `false` — не открылась. */
    suspend fun openFolder(path: String): Boolean
}

/**
 * Куда сохраняет платформа: «Файлы» → «Загрузки» (Android), папка на диске (desktop), место выбрал пользователь
 * (iOS).
 */
enum class FirmwareSaveTarget { Downloads, Folder, Chosen }

sealed interface FirmwareSaveResult {
    /** [path] — где лежит файл (полный путь или путь в «Загрузках»), [folder] — папка для показа и «Открыть папку». */
    data class Saved(val path: String, val folder: String?) : FirmwareSaveResult

    data object Cancelled : FirmwareSaveResult

    /** Android 7–9: не дано разрешение на запись в «Загрузки». */
    data object PermissionDenied : FirmwareSaveResult

    data object Failed : FirmwareSaveResult
}
