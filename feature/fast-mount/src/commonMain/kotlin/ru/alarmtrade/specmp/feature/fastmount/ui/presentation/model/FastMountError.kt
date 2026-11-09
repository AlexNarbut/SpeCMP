package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountException
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountFailure

@Immutable
sealed interface FastMountError {
    data class Step(val failure: FastMountFailure) : FastMountError

    data class Common(val error: AppError) : FastMountError

    data object Unknown : FastMountError

    companion object {
        fun from(error: Throwable): FastMountError = (error as? FastMountException)?.failure?.let(::Step)
            ?: error.appError?.let(::Common)
            ?: Unknown
    }
}
