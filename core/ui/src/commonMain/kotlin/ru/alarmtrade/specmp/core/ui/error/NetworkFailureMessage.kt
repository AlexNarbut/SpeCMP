package ru.alarmtrade.specmp.core.ui.error

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.NetworkFailure
import specmp.core.ui.generated.resources.Res
import specmp.core.ui.generated.resources.coreui_common_error_no_connection
import specmp.core.ui.generated.resources.coreui_common_error_server
import specmp.core.ui.generated.resources.coreui_common_error_timeout

/** Общий текст сбоя связи: одинаков на всех экранах, экран показывает его вместо своей общей фразы. */
@Composable
fun NetworkFailure.message(): String = stringResource(
    when (this) {
        NetworkFailure.NoConnection -> Res.string.coreui_common_error_no_connection
        NetworkFailure.Timeout -> Res.string.coreui_common_error_timeout
        NetworkFailure.ServerError -> Res.string.coreui_common_error_server
    }
)
