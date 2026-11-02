package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_btn_other_device
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_error_failed
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_loading_device
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_unsupported_alarms
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_unsupported_database
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_unsupported_microbot
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_ready
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_unsupported
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_home
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_msg_stub
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_btn_disconnect
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_format_msg
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_title_screen
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

internal const val FAST_MOUNT_READY_TAG = "fastmount_check_ready"
internal const val FAST_MOUNT_UNSUPPORTED_TAG = "fastmount_check_unsupported"
internal const val FAST_MOUNT_WRONG_DEVICE_TAG = "fastmount_wrong_device"

/**
 * Мастер быстрого монтажа. [onExit] — выход из монтажа (сессия закрывается обёрткой), [onChooseAnother] — выход
 * с возвратом к поиску блока.
 */
@Composable
internal fun FastMountScreen(onExit: () -> Unit, onChooseAnother: () -> Unit) {
    val viewModel = koinViewModel<FastMountViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FastMountContent(state = state, onExit = onExit, onChooseAnother = onChooseAnother, onRetry = viewModel::onRetry)
}

@Composable
internal fun FastMountContent(
    state: FastMountState,
    onExit: () -> Unit,
    onChooseAnother: () -> Unit,
    onRetry: () -> Unit
) {
    val colors = cmpThemeColors()
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.fastmount_common_title_screen)) },
                subtitle = state.fixed?.let { fixed ->
                    { CmpText(text = fixed.title, fontSize = 14.sp, color = colors.onSurfaceVariant, maxLines = 1) }
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onExit,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.fastmount_common_a11y_back)
                    )
                }
            )
        }
    ) { padding ->
        CmpColumn(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val check = state.check) {
                DeviceCheck.Checking -> Checking()

                DeviceCheck.Ready -> CmpStateBox(
                    icon = CmpIcons.TaskAlt,
                    title = stringResource(Strings.string.fastmount_check_title_ready),
                    text = stringResource(Strings.string.fastmount_common_msg_stub),
                    modifier = Modifier.testTag(FAST_MOUNT_READY_TAG)
                )

                is DeviceCheck.Unsupported -> CmpStateBox(
                    icon = CmpIcons.Block,
                    title = stringResource(Strings.string.fastmount_check_title_unsupported),
                    text = stringResource(check.reason.message()),
                    tone = CmpStateTone.Error,
                    modifier = Modifier.testTag(FAST_MOUNT_UNSUPPORTED_TAG)
                ) {
                    Actions {
                        CmpButton(
                            text = stringResource(Strings.string.fastmount_check_btn_other_device),
                            onClick = onChooseAnother,
                            modifier = Modifier.fillMaxWidth()
                        )
                        CmpOutlinedButton(
                            text = stringResource(Strings.string.fastmount_common_btn_home),
                            onClick = onExit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is DeviceCheck.Failed -> CmpStateBox(
                    icon = CmpIcons.ErrorOutline,
                    title = stringResource(Strings.string.fastmount_check_error_failed),
                    text = (check.error as? FastMountError.Common)?.error?.message(),
                    tone = CmpStateTone.Error
                ) {
                    Actions {
                        CmpButton(
                            text = stringResource(Strings.string.fastmount_common_btn_retry),
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is DeviceCheck.WrongDevice -> CmpStateBox(
                    icon = CmpIcons.SyncProblem,
                    title = stringResource(Strings.string.fastmount_wrong_title_screen),
                    text = stringResource(
                        Strings.string.fastmount_wrong_format_msg,
                        state.fixed?.title.orEmpty(),
                        check.connectedTitle
                    ),
                    tone = CmpStateTone.Error,
                    modifier = Modifier.testTag(FAST_MOUNT_WRONG_DEVICE_TAG)
                ) {
                    Actions {
                        CmpOutlinedButton(
                            text = stringResource(Strings.string.fastmount_wrong_btn_disconnect),
                            onClick = onExit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Checking() {
    CmpColumn(
        modifier = Modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CmpCircularProgressIndicator()
        CmpText(
            text = stringResource(Strings.string.fastmount_check_loading_device),
            color = cmpThemeColors().onSurfaceVariant
        )
    }
}

@Composable
private fun Actions(content: @Composable () -> Unit) {
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

private fun UnsupportedReason.message() = when (this) {
    UnsupportedReason.NOT_ALARM -> Strings.string.fastmount_check_msg_unsupported_alarms
    UnsupportedReason.MICROBOT -> Strings.string.fastmount_check_msg_unsupported_microbot
    UnsupportedReason.NOT_IN_DATABASE -> Strings.string.fastmount_check_msg_unsupported_database
}
