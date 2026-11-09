package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

import kotlinx.coroutines.flow.Flow

/**
 * Контракт: копирование — вне main (SDK ждёт путь, а не `content://`); размер — до копирования; на каждый [launch] —
 * ровно один результат в [results] с буфером до подписчика (процесс могут убить, пока открыт пикер).
 */
interface FirmwareFilePicker {
    val results: Flow<FirmwareFilePickResult>

    fun launch()
}

/** Отмена — не ошибка и ничего не показывает. */
sealed interface FirmwareFilePickResult {
    data class Picked(val name: String, val path: String) : FirmwareFilePickResult

    data object Cancelled : FirmwareFilePickResult

    data object TooLarge : FirmwareFilePickResult

    data object Failed : FirmwareFilePickResult
}

object FirmwareFileLimits {
    const val MAX_MEGABYTES = 16
    const val MAX_BYTES: Long = MAX_MEGABYTES * 1024L * 1024L
}

object NoFirmwareFilePicker : FirmwareFilePicker {
    override val results: Flow<FirmwareFilePickResult> = kotlinx.coroutines.flow.emptyFlow()

    override fun launch() = Unit
}
