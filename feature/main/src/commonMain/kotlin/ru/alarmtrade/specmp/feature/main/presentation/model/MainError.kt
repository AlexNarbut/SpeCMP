package ru.alarmtrade.specmp.feature.main.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError

/** Ошибка главного экрана: общая ошибка приложения или отказ операции (свой текст у каждого места). */
sealed interface MainError {
    data class Common(val error: AppError) : MainError

    data object Failed : MainError

    companion object {
        fun from(error: Throwable): MainError = error.appError?.let(::Common) ?: Failed
    }
}
