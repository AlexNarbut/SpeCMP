package ru.alarmtrade.specmp.feature.advancedmount.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError

/** Ошибка проверки блока на сервере. */
sealed interface MountError {
    data class Common(val error: AppError) : MountError

    /** Сервер не подтвердил блок. */
    data object VerifyFailed : MountError

    companion object {
        fun from(error: Throwable): MountError = error.appError?.let(::Common) ?: VerifyFailed
    }
}
