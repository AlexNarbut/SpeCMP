package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhotoLimitException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageFullException

internal sealed interface PassportError {
    data class Common(val error: AppError) : PassportError

    data object PhotoLimit : PassportError

    data object StorageFull : PassportError

    data object Unknown : PassportError

    companion object {
        fun from(error: Throwable): PassportError = when (error) {
            is PassportPhotoLimitException -> PhotoLimit
            is PassportStorageFullException -> StorageFull
            else -> error.appError?.let(::Common) ?: Unknown
        }
    }
}
