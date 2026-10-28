package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.ui.error.message

/** Ошибка загрузки данных настроек: меню блока, поиск автомобиля по CAN. */
internal sealed interface SettingsError {
    data class Common(val error: AppError) : SettingsError

    /** Данные не получены (своя фраза у каждого места). */
    data object LoadFailed : SettingsError

    companion object {
        fun from(error: Throwable): SettingsError = error.appError?.let(::Common) ?: LoadFailed
    }
}

/** Текст ошибки; [loadFailed] — фраза места для своего отказа. */
@Composable
internal fun SettingsError.text(loadFailed: StringResource): String = when (this) {
    is SettingsError.Common -> error.message()
    SettingsError.LoadFailed -> stringResource(loadFailed)
}
