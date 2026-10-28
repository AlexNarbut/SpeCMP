package ru.alarmtrade.specmp.feature.login.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.error.RequestError
import ru.alarmtrade.specmp.core.ui.error.text
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_error_required_fields

/**
 * Типизированная ошибка submit-флоу auth-форм.
 * UI сам маппит значение в stringResource конкретного экрана (см. docs/patterns.md).
 */
sealed interface AuthFormError {
    data object RequiredFields : AuthFormError

    /** Запрос не прошёл: отказ сервера или сбой связи (см. [RequestError]). */
    data class Request(val error: RequestError) : AuthFormError

    /** Сбой связи: поля формы не подсвечиваются — они не виноваты. */
    val isNetwork: Boolean get() = this is Request && error is RequestError.Network

    companion object {
        /** Сервер отклонил запрос — не сбой связи. */
        val Rejected = Request(RequestError.Failed)

        fun from(error: Throwable): AuthFormError = Request(RequestError.from(error))
    }
}

/** Текст ошибки; [requestFailed] — фраза экрана для отказа сервера. */
@Composable
fun AuthFormError.text(requestFailed: StringResource): String = when (this) {
    AuthFormError.RequiredFields -> stringResource(Res.string.login_common_error_required_fields)
    is AuthFormError.Request -> error.text(requestFailed)
}
