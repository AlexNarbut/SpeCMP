package ru.alarmtrade.specmp.feature.configurator.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.ui.error.message

internal sealed interface ConfiguratorError {
    data class Common(val error: AppError) : ConfiguratorError

    data object LoadFailed : ConfiguratorError

    companion object {
        fun from(error: Throwable): ConfiguratorError = error.appError?.let(::Common) ?: LoadFailed
    }
}

/** [loadFailed] — фраза экрана для своего отказа. */
@Composable
internal fun ConfiguratorError.text(loadFailed: StringResource): String = when (this) {
    is ConfiguratorError.Common -> error.message()
    ConfiguratorError.LoadFailed -> stringResource(loadFailed)
}
