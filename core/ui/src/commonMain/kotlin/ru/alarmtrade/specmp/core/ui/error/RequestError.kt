package ru.alarmtrade.specmp.core.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.NetworkFailure
import ru.alarmtrade.specmp.core.common.networkFailure

/**
 * Ошибка запроса для UI-состояния: сбой связи (общий текст приложения) или отказ (текст экрана).
 * VM кладёт в состояние `RequestError.from(response.exception)`, экран — `error.text(Res.string.<фраза экрана>)`.
 */
@Immutable
sealed interface RequestError {
    data object Failed : RequestError

    data class Network(val failure: NetworkFailure) : RequestError

    companion object {
        fun from(error: Throwable): RequestError = error.networkFailure?.let(::Network) ?: Failed
    }
}

/** Текст ошибки; [failed] — фраза экрана для отказа, не связанного со связью. */
@Composable
fun RequestError.text(failed: StringResource): String = when (this) {
    RequestError.Failed -> stringResource(failed)
    is RequestError.Network -> failure.message()
}
