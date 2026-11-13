package ru.alarmtrade.specmp.feature.techrequests.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_error_not_found
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_error_unknown
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_status_canceled_by_user
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_status_finished
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_status_finished_by_user
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_status_in_process
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_status_open
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_status_suspended
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_error_device_read
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_error_limit
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_error_send
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.error.messageText
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus

/** [unknown] — текст для непредвиденной ошибки: у отправки он свой («Не удалось отправить заявку»). */
@Composable
internal fun TechRequestsError.text(
    unknown: StringResource = Strings.string.techrequests_common_error_unknown,
): String = when (this) {
    is TechRequestsError.Common -> error.message()
    TechRequestsError.Limit -> stringResource(Strings.string.techrequests_form_error_limit)
    TechRequestsError.NotFound -> stringResource(Strings.string.techrequests_common_error_not_found)
    TechRequestsError.DeviceRead -> stringResource(Strings.string.techrequests_form_error_device_read)
    TechRequestsError.Unknown -> stringResource(unknown)
}

/** Для снекбара из обработчика эффектов (вне composable). */
internal suspend fun TechRequestsError.textOf(): String = when (this) {
    is TechRequestsError.Common -> error.messageText()
    TechRequestsError.Limit -> getString(Strings.string.techrequests_form_error_limit)
    TechRequestsError.NotFound -> getString(Strings.string.techrequests_common_error_not_found)
    TechRequestsError.DeviceRead -> getString(Strings.string.techrequests_form_error_device_read)
    TechRequestsError.Unknown -> getString(Strings.string.techrequests_common_error_unknown)
}

@Composable
internal fun TechRequestsError.submitText(): String = text(unknown = Strings.string.techrequests_form_error_send)

internal val TechRequestsError.icon: CmpIcons
    get() = when (this) {
        is TechRequestsError.Common -> if (error is ApiError) CmpIcons.WifiOff else CmpIcons.BluetoothDisabled
        TechRequestsError.Limit -> CmpIcons.HourglassTop
        TechRequestsError.DeviceRead -> CmpIcons.BluetoothDisabled
        TechRequestsError.NotFound, TechRequestsError.Unknown -> CmpIcons.ErrorOutline
    }

internal val TechRequestStatus.label: StringResource
    get() = when (this) {
        TechRequestStatus.OPEN -> Strings.string.techrequests_common_status_open
        TechRequestStatus.IN_PROCESS -> Strings.string.techrequests_common_status_in_process
        TechRequestStatus.SUSPENDED -> Strings.string.techrequests_common_status_suspended
        TechRequestStatus.FINISHED -> Strings.string.techrequests_common_status_finished
        TechRequestStatus.FINISHED_BY_USER -> Strings.string.techrequests_common_status_finished_by_user
        TechRequestStatus.CANCELED_BY_USER -> Strings.string.techrequests_common_status_canceled_by_user
    }

internal val TechRequestStatus.icon: CmpIcons
    get() = when (this) {
        TechRequestStatus.OPEN -> CmpIcons.Schedule
        TechRequestStatus.IN_PROCESS -> CmpIcons.SupportAgent
        TechRequestStatus.SUSPENDED -> CmpIcons.PauseCircle
        TechRequestStatus.FINISHED -> CmpIcons.CheckCircle
        TechRequestStatus.FINISHED_BY_USER -> CmpIcons.Done
        TechRequestStatus.CANCELED_BY_USER -> CmpIcons.Block
    }
