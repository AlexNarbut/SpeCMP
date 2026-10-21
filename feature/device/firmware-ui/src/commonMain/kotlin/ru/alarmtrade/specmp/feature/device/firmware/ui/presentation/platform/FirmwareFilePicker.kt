package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

import kotlinx.coroutines.flow.Flow

/**
 * Платформенный выбор файла прошивки в файловом менеджере. Реализация живёт в `composeApp` (SAF на Android,
 * диалог выбора файла на desktop, document picker на iOS); фича получает только путь к готовому файлу.
 *
 * Контракт реализации:
 * - копирование файла (Android/iOS: выбранный документ → временный файл, SDK ждёт путь, а не `content://`) — вне main;
 * - размер проверяется **до** копирования: больше [FirmwareFileLimits.MAX_BYTES] → [FirmwareFilePickResult.TooLarge];
 * - на каждый [launch] — ровно один результат в [results], доставка на main, буфер до появления подписчика
 *   (пока открыт системный пикер, процесс могут убить — результат приходит уже в новый).
 */
interface FirmwareFilePicker {
    val results: Flow<FirmwareFilePickResult>

    fun launch()
}

/** Результат выбора. Отмена — не ошибка и ничего не показывает; [TooLarge] и [Failed] — баннер на выборе источника. */
sealed interface FirmwareFilePickResult {
    /** [path] — абсолютный путь читаемого файла; [name] — исходное имя для показа. */
    data class Picked(val name: String, val path: String) : FirmwareFilePickResult

    data object Cancelled : FirmwareFilePickResult

    data object TooLarge : FirmwareFilePickResult

    data object Failed : FirmwareFilePickResult
}

object FirmwareFileLimits {
    const val MAX_MEGABYTES = 16
    const val MAX_BYTES: Long = MAX_MEGABYTES * 1024L * 1024L
}

/** Заглушка для галерей состояний и превью: ничего не выбирает. */
object NoFirmwareFilePicker : FirmwareFilePicker {
    override val results: Flow<FirmwareFilePickResult> = kotlinx.coroutines.flow.emptyFlow()

    override fun launch() = Unit
}
