package ru.alarmtrade.specmp.feature.push.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.error.messageText

internal sealed interface PushRulesError {
    data class Common(val error: AppError) : PushRulesError

    data object Failed : PushRulesError

    companion object {
        fun from(error: Throwable): PushRulesError = error.appError?.let(::Common) ?: Failed
    }
}

@Composable
internal fun PushRulesError.message(failed: StringResource): String = when (this) {
    is PushRulesError.Common -> error.message()
    PushRulesError.Failed -> stringResource(failed)
}

internal suspend fun PushRulesError.messageText(failed: StringResource): String = when (this) {
    is PushRulesError.Common -> error.messageText()
    PushRulesError.Failed -> getString(failed)
}
