package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_back_to_bypass
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_confirm
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_finish
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_btn_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_format_ticket
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_hint_processing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_hint_queue
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_how_lock
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_how_queue
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_how_server
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_label_attempts
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_label_ticket_origin
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_label_ticket_origin_fast
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_attempts_unknown
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_attempts_zero
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_confirm
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_confirm_unknown
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_interrupted_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_interrupted_kept
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_locked
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_ticket_no_attempts
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
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_ticket
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_msg_pin
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_title_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_format_clone
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartChatButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartDialog
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartIllustration
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartPrimaryButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartScaffold
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartSecondaryButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.ExitDialog
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.Headline
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.HowItWorks
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationFrame
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationMode
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StatusRow
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StepNextButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepChrome
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepNext
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.text

internal const val CLONE_START_TAG = "autostart_clone_start"
internal const val CLONE_STEP_NEXT_TAG = "autostart_clone_step_next"
internal const val CLONE_CONFIRM_TAG = "autostart_clone_confirm"
internal const val CLONE_RESULT_TAG = "autostart_clone_result"

@Immutable
internal data class CloneActions(
    val onBack: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
    val onExitConfirm: () -> Unit = {},
    val onOpenSupportChat: () -> Unit = {},
    val onOpenTechRequest: (origin: String, text: String) -> Unit = { _, _ -> },
)

/** [onOpenTechRequest] — «Заявка на техпомощь» при ошибке или без попыток: откуда открыто и текст для описания. */
@Composable
fun CloneEntry(
    fromBypass: Boolean,
    onBack: () -> Unit,
    onOpenSupportChat: () -> Unit,
    onOpenTechRequest: (origin: String, text: String) -> Unit,
) {
    val viewModel = koinViewModel<CloneViewModel> { parametersOf(fromBypass) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            CloneEffect.Close -> onBack()
        }
    }
    val actions = remember(viewModel, onBack, onOpenSupportChat, onOpenTechRequest) {
        CloneActions(
            onBack = viewModel::onBack,
            onStart = viewModel::onStartClick,
            onConfirm = viewModel::onConfirm,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onExitConfirm = {
                viewModel.onExitConfirm()
                onBack()
            },
            onOpenSupportChat = onOpenSupportChat,
            onOpenTechRequest = onOpenTechRequest,
        )
    }
    CloneContent(state = state, actions = actions, onClose = onBack)
}

/** @param key ключ VM: новый ключ — процедура с начала, тот же — прежний итог. */
@Composable
fun CloneStep(
    key: String,
    chrome: AutostartStepChrome,
    next: AutostartStepNext,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onOpenSupportChat: () -> Unit,
    onOpenTechRequest: (origin: String, text: String) -> Unit,
) {
    val viewModel = koinViewModel<CloneViewModel>(key = key) { parametersOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            CloneEffect.Close -> onBack()
        }
    }
    val actions = remember(viewModel, onBack, onOpenSupportChat, onOpenTechRequest) {
        CloneActions(
            onBack = viewModel::onBack,
            onStart = viewModel::onStartClick,
            onConfirm = viewModel::onConfirm,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onExitConfirm = viewModel::onExitConfirm,
            onOpenSupportChat = onOpenSupportChat,
            onOpenTechRequest = onOpenTechRequest,
        )
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = actions.onBack,
    )
    CloneContent(state = state, actions = actions, step = CloneStepMode(chrome, next, onNext))
}

internal class CloneStepMode(val chrome: AutostartStepChrome, val next: AutostartStepNext, val onNext: () -> Unit)

@Composable
internal fun CloneContent(
    state: CloneState,
    actions: CloneActions,
    onClose: () -> Unit = actions.onBack,
    step: CloneStepMode? = null,
) {
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
                    key = running.stage to running.inQueue,
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
                        modifier = Modifier.testTag(CLONE_RESULT_TAG),
                    )
                    ResultBody(state, phase.result)
                }
            }
        },
        side = if (phase is ClonePhase.Start) ({ HowItWorks(howItems()) }) else null,
        footer = footer(state, actions, onClose, step),
        chrome = step?.chrome,
    )
    CloneDialogHost(state, actions)
}

@Composable
private fun howItems() = listOf(
    CmpIcons.Cloud to stringResource(Strings.string.autostart_clone_item_how_server),
    CmpIcons.Schedule to stringResource(Strings.string.autostart_clone_item_how_queue),
    CmpIcons.Lock to stringResource(Strings.string.autostart_clone_item_how_lock),
)

@Composable
private fun StartBody(state: CloneState, phase: ClonePhase.Start) {
    phase.interruption?.let { interruption ->
        CmpBanner(
            icon = CmpIcons.LinkOff,
            tone = CmpBannerTone.Error,
            title = stringResource(Strings.string.autostart_common_title_interrupted),
            text = stringResource(
                if (interruption == CloneInterruption.ATTEMPT_KEPT) {
                    Strings.string.autostart_clone_msg_interrupted_kept
                } else {
                    Strings.string.autostart_clone_msg_interrupted_check
                },
            ),
        )
    }
    Headline(
        title = stringResource(Strings.string.autostart_clone_title_screen),
        subtitle = stringResource(Strings.string.autostart_clone_subtitle_start),
    )
    AttemptsBlock(state.attempts, large = true)
}

@Composable
private fun AttemptsBlock(attempts: CloneAttempts, large: Boolean) {
    val colors = CmpTheme.colors
    when (attempts) {
        CloneAttempts.Loading -> Unit

        CloneAttempts.Unknown -> CmpBanner(
            icon = CmpIcons.CloudOff,
            tone = CmpBannerTone.Info,
            text = stringResource(Strings.string.autostart_clone_msg_attempts_unknown),
        )

        is CloneAttempts.Known -> if (attempts.count == 0 && large) {
            CmpBanner(
                icon = CmpIcons.Block,
                tone = CmpBannerTone.Error,
                text = stringResource(Strings.string.autostart_clone_msg_attempts_zero),
            )
        } else {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (large) Modifier.cardFrame() else Modifier)
                    .padding(
                        horizontal = if (large) CmpSpacing.space16 else CmpSpacing.space4,
                        vertical = if (large) CmpSpacing.space12 else 0.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = stringResource(Strings.string.autostart_clone_label_attempts),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.weight(1f),
                )
                CmpText(
                    text = attempts.count.toString(),
                    style = if (large) CmpTextStyle.Title1 else CmpTextStyle.Headline1,
                    color = if (attempts.count == 1 && large) colors.errorDefault else colors.brandPrimaryDefault,
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
        },
    )
    if (phase.stage.locked) {
        CmpBanner(
            icon = CmpIcons.Lock,
            tone = CmpBannerTone.Warning,
            title = stringResource(Strings.string.autostart_clone_title_locked),
            text = stringResource(Strings.string.autostart_clone_msg_locked),
        )
    }
}

@Composable
private fun ColumnScope.ResultBody(state: CloneState, result: CloneResult) {
    when (result) {
        is CloneResult.Completed -> if (result.already) {
            Headline(
                title = stringResource(Strings.string.autostart_clone_title_already),
                subtitle = stringResource(Strings.string.autostart_clone_subtitle_already),
            )
        } else {
            Headline(
                title = stringResource(Strings.string.autostart_clone_title_success),
                subtitle = stringResource(Strings.string.autostart_clone_subtitle_success),
            )
        }

        CloneResult.NotSupported -> Headline(
            title = stringResource(Strings.string.autostart_clone_title_not_supported),
            subtitle = stringResource(Strings.string.autostart_clone_subtitle_not_supported),
        )

        is CloneResult.Failed -> {
            Headline(title = stringResource(Strings.string.autostart_clone_title_error))
            CmpBanner(icon = CmpIcons.ErrorOutline, tone = CmpBannerTone.Error, text = failureText(result.failure))
            if (result.tips.isNotEmpty()) Tips(result.tips)
            AttemptsBlock(state.attempts, large = false)
        }
    }
}

@Composable
private fun Modifier.cardFrame(): Modifier {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    return background(
        colors.backgroundSurfaceCard,
        shape,
    ).border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
}

@Composable
private fun Tips(tips: List<String>) {
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .cardFrame()
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpText(text = stringResource(Strings.string.autostart_clone_title_tips), style = CmpTextStyle.Headline3)
        tips.forEach { tip -> CmpText(text = "•  $tip", style = CmpTextStyle.Paragraph2) }
    }
}

/** Ошибка или нет попыток: повтор (если поможет) → заявка на техпомощь → закрыть → чат. */
@Composable
private fun ColumnScope.CloneHelpButtons(
    result: CloneResult,
    canStart: Boolean,
    actions: CloneActions,
    onClose: () -> Unit,
    step: CloneStepMode?,
) {
    val origin = cloneTicketOrigin(step)
    val ticket = if (result is CloneResult.Failed) {
        stringResource(Strings.string.autostart_clone_format_ticket, failureText(result.failure))
    } else {
        stringResource(Strings.string.autostart_clone_msg_ticket_no_attempts)
    }
    val onTicket = { actions.onOpenTechRequest(origin, ticket) }
    // Повтор — только там, где он может помочь, и пока есть попытки.
    if (result is CloneResult.Failed && result.failure.retryable && canStart) {
        AutostartPrimaryButton(Strings.string.autostart_common_btn_retry, actions.onStart, icon = CmpIcons.Refresh)
        AutostartSecondaryButton(Strings.string.autostart_common_btn_ticket, onTicket, icon = CmpIcons.Call)
    } else {
        AutostartPrimaryButton(Strings.string.autostart_common_btn_ticket, onTicket, icon = CmpIcons.Call)
        AutostartSecondaryButton(Strings.string.autostart_common_btn_close, onClose)
    }
    AutostartChatButton(actions.onOpenSupportChat)
}

@Composable
private fun cloneTicketOrigin(step: CloneStepMode?): String = stringResource(
    if (step != null) {
        Strings.string.autostart_clone_label_ticket_origin_fast
    } else {
        Strings.string.autostart_clone_label_ticket_origin
    },
)

@Composable
private fun failureText(failure: CloneFailure): String = failure.message().text()

private fun footer(
    state: CloneState,
    actions: CloneActions,
    onClose: () -> Unit,
    step: CloneStepMode?,
): (@Composable ColumnScope.() -> Unit)? = when (val phase = state.phase) {
    is ClonePhase.Start -> {
        {
            if (state.canStart) {
                AutostartPrimaryButton(
                    text = Strings.string.autostart_clone_btn_start,
                    onClick = actions.onStart,
                    modifier = Modifier.testTag(CLONE_START_TAG),
                    icon = CmpIcons.PlayArrow,
                )
            } else {
                val origin = cloneTicketOrigin(step)
                val ticket = stringResource(Strings.string.autostart_clone_msg_ticket_no_attempts)
                AutostartPrimaryButton(
                    Strings.string.autostart_common_btn_ticket,
                    { actions.onOpenTechRequest(origin, ticket) },
                    icon = CmpIcons.Call,
                )
                AutostartChatButton(actions.onOpenSupportChat)
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
                result is CloneResult.Completed && step != null -> StepNextButton(
                    next = step.next,
                    onClick = step.onNext,
                    modifier = Modifier.testTag(CLONE_STEP_NEXT_TAG),
                )

                result is CloneResult.Completed -> AutostartPrimaryButton(closeText, onClose)

                result is CloneResult.NotSupported -> AutostartPrimaryButton(
                    Strings.string.autostart_common_btn_close,
                    onClose,
                )

                else -> CloneHelpButtons(result, state.canStart, actions, onClose, step)
            }
        }
    }
}

@Composable
private fun CloneDialogHost(state: CloneState, actions: CloneActions) {
    when (val dialog = state.dialog) {
        CloneDialog.Confirm -> {
            val count = (state.attempts as? CloneAttempts.Known)?.count
            AutostartDialog(
                icon = CmpIcons.ContentCopy,
                title = stringResource(Strings.string.autostart_clone_title_confirm),
                text = if (count != null) {
                    stringResource(Strings.string.autostart_clone_msg_confirm, count)
                } else {
                    stringResource(Strings.string.autostart_clone_msg_confirm_unknown)
                },
                dismissText = stringResource(Strings.string.autostart_clone_btn_cancel),
                onDismissClick = actions.onDialogDismiss,
                confirmText = stringResource(Strings.string.autostart_clone_btn_confirm),
                onConfirmClick = actions.onConfirm,
                onDismissRequest = actions.onDialogDismiss,
                confirmModifier = Modifier.testTag(CLONE_CONFIRM_TAG),
            )
        }

        is CloneDialog.Pin -> ServicePinDialog(
            message = stringResource(Strings.string.autostart_common_msg_pin),
            state = dialog.pin,
            onDismiss = actions.onDialogDismiss,
            onSubmit = actions.onPinSubmit,
            testTagPrefix = "autostart_pin",
        )

        CloneDialog.Exit -> ExitDialog(onStay = actions.onDialogDismiss, onExit = actions.onExitConfirm)

        null -> Unit
    }
}
