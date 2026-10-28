package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError

/** Ошибка поиска устройств (оба транспорта ничего не нашли и хотя бы один упал). */
sealed interface ScanError {
    data class Common(val error: AppError) : ScanError

    data object ScanFailed : ScanError

    companion object {
        fun from(error: Throwable): ScanError = error.appError?.let(::Common) ?: ScanFailed
    }
}
