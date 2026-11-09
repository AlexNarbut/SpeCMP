package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError

sealed interface SupportChatError {
    data class Common(val error: AppError) : SupportChatError

    data object HistoryUnavailable : SupportChatError

    companion object {
        fun from(error: Throwable): SupportChatError = error.appError?.let(::Common) ?: HistoryUnavailable
    }
}
