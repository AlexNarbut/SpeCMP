package ru.alarmtrade.specmp.feature.login.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_required_fields
import ru.alarmtrade.specmp.core.ui.error.message

sealed interface AuthFormError {
    data object RequiredFields : AuthFormError

    data object Rejected : AuthFormError

    data class Common(val error: AppError) : AuthFormError

    /** Виноват ввод — поля формы подсвечиваются; при общей ошибке поля не виноваты. */
    val blamesInput: Boolean get() = this !is Common

    companion object {
        fun from(error: Throwable): AuthFormError = error.appError?.let(::Common) ?: Rejected
    }
}

/** [rejected] — фраза экрана для отказа сервера. */
@Composable
fun AuthFormError.text(rejected: StringResource): String = when (this) {
    AuthFormError.RequiredFields -> stringResource(Strings.string.login_common_error_required_fields)
    AuthFormError.Rejected -> stringResource(rejected)
    is AuthFormError.Common -> error.message()
}
