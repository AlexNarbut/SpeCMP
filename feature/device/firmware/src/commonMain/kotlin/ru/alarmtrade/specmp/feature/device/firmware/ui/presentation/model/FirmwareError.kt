package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.ui.error.message

sealed interface FirmwareError {
    data class Common(val error: AppError) : FirmwareError

    data object LoadFailed : FirmwareError

    companion object {
        fun from(error: Throwable): FirmwareError = error.appError?.let(::Common) ?: LoadFailed
    }
}

@Composable
internal fun FirmwareError.text(loadFailed: StringResource): String = when (this) {
    is FirmwareError.Common -> error.message()
    FirmwareError.LoadFailed -> stringResource(loadFailed)
}
