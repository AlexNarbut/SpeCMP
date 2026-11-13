package ru.alarmtrade.specmp.feature.techrequests.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestException
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestFailure

internal sealed interface TechRequestsError {
    data class Common(val error: AppError) : TechRequestsError

    data object Limit : TechRequestsError

    data object NotFound : TechRequestsError

    data object DeviceRead : TechRequestsError

    data object Unknown : TechRequestsError

    companion object {
        fun from(error: Throwable): TechRequestsError = when (error) {
            is TechRequestException -> when (error.failure) {
                TechRequestFailure.Limit -> Limit
                TechRequestFailure.NotFound -> NotFound
                TechRequestFailure.DeviceRead -> DeviceRead
            }

            else -> error.appError?.let(::Common) ?: Unknown
        }
    }
}
