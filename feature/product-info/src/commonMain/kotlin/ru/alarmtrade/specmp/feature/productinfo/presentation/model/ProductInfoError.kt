package ru.alarmtrade.specmp.feature.productinfo.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductNotFoundException

internal sealed interface ProductInfoError {
    data class Common(val error: AppError) : ProductInfoError

    data class NotFound(val id: Long) : ProductInfoError

    data object Unknown : ProductInfoError

    companion object {
        fun from(error: Throwable): ProductInfoError = when (error) {
            is ProductNotFoundException -> NotFound(error.id)
            else -> error.appError?.let(::Common) ?: Unknown
        }
    }
}
