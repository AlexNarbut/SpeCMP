package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_back_to_bypass
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_confirm
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_finish
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_algorithm
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_attempts
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_calc
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_code
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_debug
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_denied
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_device_id
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_reading
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_request
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_required
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_unknown
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_unsupported
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_verifying
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_writing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_hint_processing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_hint_queue
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_how_lock
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_how_queue
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_how_server
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_preparing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_processing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_queue
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_reading
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_verifying
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_writing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_label_attempts
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_attempts_unknown
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_attempts_zero
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_confirm
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_confirm_unknown
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_interrupted_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_interrupted_kept
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_locked
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_subtitle_already
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_subtitle_not_supported
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_subtitle_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_subtitle_success
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_already
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_confirm
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_error
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_locked
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_not_supported
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_screen
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_success
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_tips
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_chat
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_msg_pin
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_title_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_format_clone
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartButtonHeight
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartIllustration
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartScaffold
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.ExitDialog
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.Headline
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.HowItWorks
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationFrame
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationMode
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StatusRow
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialog

internal const val CLONE_START_TAG = "autostart_clone_start"
internal const val CLONE_CONFIRM_TAG = "autostart_clone_confirm"
internal const val CLONE_RESULT_TAG = "autostart_clone_result"

/** Действия экрана; один экземпляр на экран. */
@Immutable
internal data class CloneActions(
    val onBack: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
    val onExitConfirm: () -> Unit = {},
    val onOpenSupportChat: () -> Unit = {}
)

/**
 * Экран «Процедура Clone» (раздел расширенного монтажа; отдельный экран — не вкладка обхода).
 *
 * @param fromBypass открыт из бесключевого обхода: итог закрывается «Вернуться к обходу».
 * @param onBack выход с экрана (в обход или на корень монтажа).
 */
@Composable
fun CloneEntry(fromBypass: Boolean, onBack: () -> Unit, onOpenSupportChat: () -> Unit) {
    val viewModel = koinViewModel<CloneViewModel> { parametersOf(fromBypass) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack, onOpenSupportChat) {
        CloneActions(
            onBack = { if (viewModel.onBackRequest()) onBack() },
            onStart = viewModel::onStartClick,
            onConfirm = viewModel::onConfirm,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onExitConfirm = {
                viewModel.onExitConfirm()
                onBack()
            },
            onOpenSupportChat = onOpenSupportChat
        )
    }
    CloneContent(state = state, actions = actions, onClose = onBack)
}

/** Stateless-контент: фаза → тело и кнопки; диалоги поверх. */
@Composable
internal fun CloneContent(state: CloneState, actions: CloneActions, onClose: () -> Unit = actions.onBack) {
    val phase = state.phase
    val subtitle = state.deviceId?.let { id -> deviceTitle(state.deviceName.orEmpty(), id) }
    AutostartScaffold(
        title = stringResource(Strings.string.autostart_clone_title_screen),
        subtitle = subtitle,
        locked = state.locked,
        onBack = actions.onBack,
        status = (phase as? ClonePhase.Running)?.let { running ->
            {
                val stage = running.stage.number
                StatusRow(
                    text = stringResource(Strings.string.autostart_status_format_clone, stage, CloneStage.COUNT),
                    key = running.stage to running.inQueue
                )
            }
        },
        main = { layout ->
            when (phase) {
                is ClonePhase.Start -> {
                    IllustrationFrame(AutostartIllustration.CLONE_START, layout)
                    StartBody(state, phase)
                }

                is ClonePhase.Running -> {
                    IllustrationFrame(AutostartIllustration.CLONE_RUN, layout, mode = IllustrationMode.Wait)
                    RunningBody(phase)
                }

                is ClonePhase.Finished -> {
                    val success = phase.result is CloneResult.Completed
                    IllustrationFrame(
                        if (success) AutostartIllustration.RESULT_SUCCESS else AutostartIllustration.RESULT_FAIL,
                        layout,
                        modifier = Modifier.testTag(CLONE_RESULT_TAG)
                    )
                    ResultBody(state, phase.result)
                }
            }
        },
        side = if (phase is ClonePhase.Start) ({ HowItWorks(howItems()) }) else null,
        footer = footer(state, actions, onClose)
    )
    CloneDialogHost(state, actions)
}

@Composable
private fun howItems() = listOf(
    CmpIcons.Cloud to stringResource(Strings.string.autostart_clone_item_how_server),
    CmpIcons.Schedule to stringResource(Strings.string.autostart_clone_item_how_queue),
    CmpIcons.Lock to stringResource(Strings.string.autostart_clone_item_how_lock)
)

@Composable
private fun StartBody(state: CloneState, phase: ClonePhase.Start) {
    phase.interruption?.let { interruption ->
        InfoBanner(
            icon = CmpIcons.LinkOff,
            tone = BannerTone.Error,
            title = stringResource(Strings.string.autostart_common_title_interrupted),
            text = stringResource(
                if (interruption == CloneInterruption.ATTEMPT_KEPT) {
                    Strings.string.autostart_clone_msg_interrupted_kept
                } else {
                    Strings.string.autostart_clone_msg_interrupted_check
                }
            )
        )
    }
    Headline(
        title = stringResource(Strings.string.autostart_clone_title_screen),
        subtitle = stringResource(Strings.string.autostart_clone_subtitle_start)
    )
    AttemptsBlock(state.attempts, large = true)
}

@Composable
private fun AttemptsBlock(attempts: CloneAttempts, large: Boolean) {
    val colors = cmpThemeColors()
    when (attempts) {
        CloneAttempts.Loading -> Unit

        CloneAttempts.Unknown -> InfoBanner(
            icon = CmpIcons.CloudOff,
            tone = BannerTone.Info,
            text = stringResource(Strings.string.autostart_clone_msg_attempts_unknown)
        )

        is CloneAttempts.Known -> if (attempts.count == 0 && large) {
            InfoBanner(
                icon = CmpIcons.Block,
                tone = BannerTone.Error,
                text = stringResource(Strings.string.autostart_clone_msg_attempts_zero)
            )
        } else {
            val shape = RoundedCornerShape(18.dp)
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (large) {
                            Modifier
                                .background(colors.surfaceContainerLow, shape)
                                .border(1.dp, colors.outlineVariant, shape)
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = if (large) 18.dp else 4.dp, vertical = if (large) 14.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpText(
                    text = stringResource(Strings.string.autostart_clone_label_attempts),
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                CmpText(
                    text = attempts.count.toString(),
                    fontSize = if (large) 32.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (attempts.count == 1 && large) colors.error else colors.primary
                )
            }
        }
    }
}

@Composable
private fun RunningBody(phase: ClonePhase.Running) {
    val calculating = phase.stage == CloneStage.CALCULATING
    Headline(
        title = stringResource(phase.stageTitle()),
        subtitle = if (calculating) {
            val hint = if (phase.inQueue) {
                Strings.string.autostart_clone_hint_queue
            } else {
                Strings.string.autostart_clone_hint_processing
            }
            stringResource(hint)
        } else {
            null
        }
    )
    if (phase.stage.locked) {
        InfoBanner(
            icon = CmpIcons.Lock,
            tone = BannerTone.Warn,
            title = stringResource(Strings.string.autostart_clone_title_locked),
            text = stringResource(Strings.string.autostart_clone_msg_locked)
        )
    }
}

private fun ClonePhase.Running.stageTitle(): StringResource = when (stage) {
    CloneStage.PREPARING -> Strings.string.autostart_clone_item_stage_preparing

    CloneStage.READING -> Strings.string.autostart_clone_item_stage_reading

    CloneStage.CALCULATING -> if (inQueue) {
        Strings.string.autostart_clone_item_stage_queue
    } else {
        Strings.string.autostart_clone_item_stage_processing
    }

    CloneStage.WRITING -> Strings.string.autostart_clone_item_stage_writing

    CloneStage.VERIFYING -> Strings.string.autostart_clone_item_stage_verifying
}

@Composable
private fun ColumnScope.ResultBody(state: CloneState, result: CloneResult) {
    when (result) {
        is CloneResult.Completed -> if (result.already) {
            Headline(
                title = stringResource(Strings.string.autostart_clone_title_already),
                subtitle = stringResource(Strings.string.autostart_clone_subtitle_already)
            )
        } else {
            Headline(
                title = stringResource(Strings.string.autostart_clone_title_success),
                subtitle = stringResource(Strings.string.autostart_clone_subtitle_success)
            )
        }

        CloneResult.NotSupported -> Headline(
            title = stringResource(Strings.string.autostart_clone_title_not_supported),
            subtitle = stringResource(Strings.string.autostart_clone_subtitle_not_supported)
        )

        is CloneResult.Failed -> {
            Headline(title = stringResource(Strings.string.autostart_clone_title_error))
            InfoBanner(icon = CmpIcons.ErrorOutline, tone = BannerTone.Error, text = failureText(result.failure))
            if (result.tips.isNotEmpty()) Tips(result.tips)
            AttemptsBlock(state.attempts, large = false)
        }
    }
}

@Composable
private fun Tips(tips: List<String>) {
    val colors = cmpThemeColors()
    val shape = RoundedCornerShape(18.dp)
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, shape)
            .border(1.dp, colors.outlineVariant, shape)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CmpText(text = stringResource(Strings.string.autostart_clone_title_tips), fontWeight = FontWeight.SemiBold)
        tips.forEach { tip -> CmpText(text = "•  $tip", style = CmpTextStyle.Body) }
    }
}

@Composable
private fun failureText(failure: CloneFailure): String = when (failure) {
    CloneFailure.DATA -> stringResource(Strings.string.autostart_clone_error_code, ERROR_CODE_DATA)
    CloneFailure.ENCRYPT -> stringResource(Strings.string.autostart_clone_error_code, ERROR_CODE_ENCRYPT)
    CloneFailure.ALGORITHM -> stringResource(Strings.string.autostart_clone_error_code, ERROR_CODE_ALGORITHM)
    else -> stringResource(failure.text())
}

private fun CloneFailure.text(): StringResource = when (this) {
    CloneFailure.READING -> Strings.string.autostart_clone_error_reading

    CloneFailure.WRITING -> Strings.string.autostart_clone_error_writing

    CloneFailure.VERIFYING -> Strings.string.autostart_clone_error_verifying

    CloneFailure.REQUIRED -> Strings.string.autostart_clone_error_required

    CloneFailure.CALC_ERROR -> Strings.string.autostart_clone_error_calc

    CloneFailure.ALGORITHM_ERROR -> Strings.string.autostart_clone_error_algorithm

    CloneFailure.WRONG_REQUEST -> Strings.string.autostart_clone_error_request

    CloneFailure.DEBUG -> Strings.string.autostart_clone_error_debug

    CloneFailure.UNSUPPORTED -> Strings.string.autostart_clone_error_unsupported

    CloneFailure.WRONG_DEVICE_ID -> Strings.string.autostart_clone_error_device_id

    CloneFailure.ATTEMPTS_EXCEEDED -> Strings.string.autostart_clone_error_attempts

    CloneFailure.DEVICE_DENIED -> Strings.string.autostart_clone_error_denied

    CloneFailure.UNKNOWN, CloneFailure.DATA, CloneFailure.ENCRYPT, CloneFailure.ALGORITHM ->
        Strings.string.autostart_clone_error_unknown
}

/** Кнопки по фазе (макет): старт / итог; во время процесса кнопок нет. */
private fun footer(
    state: CloneState,
    actions: CloneActions,
    onClose: () -> Unit
): (@Composable ColumnScope.() -> Unit)? = when (val phase = state.phase) {
    is ClonePhase.Start -> {
        {
            if (state.canStart) {
                PrimaryButton(
                    text = Strings.string.autostart_clone_btn_start,
                    onClick = actions.onStart,
                    modifier = Modifier.testTag(CLONE_START_TAG)
                )
            } else {
                PrimaryButton(Strings.string.autostart_common_btn_chat, actions.onOpenSupportChat)
                CmpOutlinedButton(
                    text = stringResource(Strings.string.autostart_clone_btn_start),
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(AutostartButtonHeight)
                )
            }
        }
    }

    is ClonePhase.Running -> null

    is ClonePhase.Finished -> {
        {
            val result = phase.result
            val closeText = if (state.fromBypass) {
                Strings.string.autostart_clone_btn_back_to_bypass
            } else {
                Strings.string.autostart_clone_btn_finish
            }
            when {
                result is CloneResult.Completed -> PrimaryButton(closeText, onClose)

                result is CloneResult.NotSupported -> PrimaryButton(Strings.string.autostart_common_btn_close, onClose)

                // D3: повтор — только там, где он может помочь, и пока есть попытки.
                result is CloneResult.Failed && result.failure.retryable && state.canStart -> {
                    PrimaryButton(Strings.string.autostart_common_btn_retry, actions.onStart)
                    SecondaryButton(Strings.string.autostart_common_btn_chat, actions.onOpenSupportChat)
                }

                else -> {
                    PrimaryButton(Strings.string.autostart_common_btn_chat, actions.onOpenSupportChat)
                    SecondaryButton(Strings.string.autostart_common_btn_close, onClose)
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: StringResource, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(
        text = stringResource(text),
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(AutostartButtonHeight)
    )
}

@Composable
private fun SecondaryButton(text: StringResource, onClick: () -> Unit) {
    CmpOutlinedButton(
        text = stringResource(text),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(AutostartButtonHeight)
    )
}

@Composable
private fun CloneDialogHost(state: CloneState, actions: CloneActions) {
    when (val dialog = state.dialog) {
        CloneDialog.Confirm -> {
            val count = (state.attempts as? CloneAttempts.Known)?.count
            CmpDialog(
                onDismissRequest = actions.onDialogDismiss,
                icon = CmpIcons.ContentCopy.imageVector(),
                title = { CmpText(text = stringResource(Strings.string.autostart_clone_title_confirm)) },
                text = {
                    CmpText(
                        text = if (count != null) {
                            stringResource(Strings.string.autostart_clone_msg_confirm, count)
                        } else {
                            stringResource(Strings.string.autostart_clone_msg_confirm_unknown)
                        },
                        style = CmpTextStyle.Body
                    )
                },
                dismissButton = {
                    CmpTextButton(
                        text = stringResource(Strings.string.autostart_clone_btn_cancel),
                        onClick = actions.onDialogDismiss,
                        modifier = Modifier.height(48.dp)
                    )
                },
                confirmButton = {
                    CmpButton(
                        text = stringResource(Strings.string.autostart_clone_btn_confirm),
                        onClick = actions.onConfirm,
                        modifier = Modifier.height(48.dp).testTag(CLONE_CONFIRM_TAG)
                    )
                }
            )
        }

        is CloneDialog.Pin -> ServicePinDialog(
            message = stringResource(Strings.string.autostart_common_msg_pin),
            state = dialog.pin,
            onDismiss = actions.onDialogDismiss,
            onSubmit = actions.onPinSubmit,
            testTagPrefix = "autostart_pin"
        )

        CloneDialog.Exit -> ExitDialog(onStay = actions.onDialogDismiss, onExit = actions.onExitConfirm)

        null -> Unit
    }
}

private const val ERROR_CODE_DATA = 4
private const val ERROR_CODE_ENCRYPT = 5
private const val ERROR_CODE_ALGORITHM = 6
