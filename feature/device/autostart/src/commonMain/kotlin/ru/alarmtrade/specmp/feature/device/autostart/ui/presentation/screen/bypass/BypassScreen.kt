package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_btn_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_ecu_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_format_ticket
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_hint_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_hint_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_hint_wait
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_how_key
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_how_steps
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_how_watch
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_prepare
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_accepted
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_ticket_origin
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_ticket_origin_fast
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_wait
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_label_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_msg_can
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_msg_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_msg_reboot
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_msg_reboot_usb
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_subtitle_clone_next
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_subtitle_clone_required
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_subtitle_not_supported
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_subtitle_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_subtitle_success
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_subtitle_success_clone
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_can
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_clone_required
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_error
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_not_supported
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_reboot
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_screen
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_success
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_open_clone
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_ticket
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_msg_pin
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_title_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_instruction_label_fail
import ru.alarmtrade.specmp.core.localization.resources.autostart_instruction_label_forbidden
import ru.alarmtrade.specmp.core.localization.resources.autostart_instruction_label_key_black
import ru.alarmtrade.specmp.core.localization.resources.autostart_instruction_label_key_red
import ru.alarmtrade.specmp.core.localization.resources.autostart_instruction_label_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_instruction_label_wait
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_label_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_label_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_label_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_label_wait
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AccentBorder
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartChatButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartIllustration
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartLayout
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartPrimaryButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartScaffold
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartSecondaryButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.ExitDialog
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.Headline
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.HowItWorks
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationFrame
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationMode
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StatusRow
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StepAppear
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StepNextButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepChrome
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepNext
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog

private val txt = Strings.string

internal const val BYPASS_START_TAG = "autostart_bypass_start"
internal const val BYPASS_OPEN_CLONE_TAG = "autostart_bypass_open_clone"
internal const val BYPASS_RESULT_TAG = "autostart_bypass_result"
internal const val BYPASS_STEP_NEXT_TAG = "autostart_bypass_step_next"

@Immutable
internal data class BypassActions(
    val onBack: () -> Unit = {},
    val onClose: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
    val onExitConfirm: () -> Unit = {},
    val onOpenClone: () -> Unit = {},
    val onOpenSupportChat: () -> Unit = {},
    val onOpenTechRequest: (origin: String, text: String) -> Unit = { _, _ -> },
)

/** [onOpenTechRequest] — «Заявка на техпомощь» при ошибке: откуда открыто и текст ошибки для описания. */
@Composable
fun BypassLearningEntry(
    onBack: () -> Unit,
    onOpenClone: () -> Unit,
    onOpenSupportChat: () -> Unit,
    onOpenTechRequest: (origin: String, text: String) -> Unit,
) {
    val viewModel = koinViewModel<BypassLearningViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            BypassEffect.Close -> onBack()
        }
    }
    val actions = remember(viewModel, onBack, onOpenClone, onOpenSupportChat, onOpenTechRequest) {
        BypassActions(
            onBack = viewModel::onBack,
            onClose = onBack,
            onStart = viewModel::onStartClick,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onExitConfirm = {
                viewModel.onExitConfirm()
                onBack()
            },
            onOpenClone = onOpenClone,
            onOpenSupportChat = onOpenSupportChat,
            onOpenTechRequest = onOpenTechRequest,
        )
    }
    BypassContent(state = state, actions = actions)
}

/** @param key ключ VM: новый ключ — обучение с начала (мастер вернулся к монтажу), тот же — прежний итог. */
@Composable
fun BypassLearningStep(
    key: String,
    chrome: AutostartStepChrome,
    next: AutostartStepNext,
    onNext: (cloneRequired: Boolean) -> Unit,
    onBack: () -> Unit,
    onOpenSupportChat: () -> Unit,
    onOpenTechRequest: (origin: String, text: String) -> Unit,
) {
    val viewModel = koinViewModel<BypassLearningViewModel>(key = key)
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            BypassEffect.Close -> onBack()
        }
    }
    val actions = remember(viewModel, onBack, onOpenSupportChat, onOpenTechRequest) {
        BypassActions(
            onBack = viewModel::onBack,
            onClose = onBack,
            onStart = viewModel::onStartClick,
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
    BypassContent(
        state = state.copy(cloneAvailable = false),
        actions = actions,
        stepMode = BypassStepMode(chrome, next, onNext),
    )
}

internal class BypassStepMode(
    val chrome: AutostartStepChrome,
    val next: AutostartStepNext,
    val onNext: (cloneRequired: Boolean) -> Unit,
)

@Composable
internal fun BypassContent(state: BypassState, actions: BypassActions, stepMode: BypassStepMode? = null) {
    val phase = state.phase
    AutostartScaffold(
        title = stringResource(txt.autostart_bypass_title_screen),
        subtitle = state.deviceId?.let { id -> deviceTitle(state.deviceName.orEmpty(), id) },
        locked = false,
        onBack = actions.onBack,
        status = (phase as? BypassPhase.Running)?.let { running ->
            { StatusRow(text = stringResource(running.step.statusText()), key = running.step ?: Unit) }
        },
        main = { layout ->
            when (phase) {
                is BypassPhase.Start -> StartBody(state, phase, layout)

                is BypassPhase.Running -> StepAppear(phase.step) { step ->
                    val instruction = step?.subject?.instruction()
                    if (step != null && instruction != null) {
                        InstructionBody(step, instruction, layout)
                    } else {
                        StepCard(step, layout)
                    }
                }

                is BypassPhase.Finished -> ResultBody(state, phase.result, layout, inStep = stepMode != null)
            }
        },
        side = if (phase is BypassPhase.Start) ({ HowItWorks(howItems()) }) else null,
        footer = footer(state, actions, stepMode),
        chrome = stepMode?.chrome,
    )
    when (state.dialog) {
        is BypassDialog.Pin -> ServicePinDialog(
            message = stringResource(txt.autostart_common_msg_pin),
            state = state.dialog.pin,
            onDismiss = actions.onDialogDismiss,
            onSubmit = actions.onPinSubmit,
            testTagPrefix = "autostart_pin",
        )

        BypassDialog.Exit -> ExitDialog(onStay = actions.onDialogDismiss, onExit = actions.onExitConfirm)

        null -> Unit
    }
}

private fun BypassStep?.statusText(): StringResource = when (this?.kind) {
    BypassStepKind.ACTION -> txt.autostart_status_label_action
    BypassStepKind.INSTRUCTION -> txt.autostart_status_label_instruction
    BypassStepKind.CRITICAL -> txt.autostart_status_label_critical
    BypassStepKind.ACCEPTED, BypassStepKind.WAIT, null -> txt.autostart_status_label_wait
}

@Composable
private fun howItems() = listOf(
    CmpIcons.VpnKey to stringResource(txt.autostart_bypass_item_how_key),
    CmpIcons.DirectionsCar to stringResource(txt.autostart_bypass_item_how_steps),
    CmpIcons.Visibility to stringResource(txt.autostart_bypass_item_how_watch),
)

@Composable
private fun StartBody(state: BypassState, phase: BypassPhase.Start, layout: AutostartLayout) {
    IllustrationFrame(AutostartIllustration.IGNITION_OFF_REQUEST, layout)
    if (phase.interrupted) {
        CmpBanner(
            icon = CmpIcons.LinkOff,
            tone = CmpBannerTone.Error,
            title = stringResource(txt.autostart_common_title_interrupted),
            text = stringResource(txt.autostart_bypass_msg_interrupted),
        )
    }
    Headline(
        title = stringResource(txt.autostart_bypass_title_start),
        subtitle = stringResource(txt.autostart_bypass_subtitle_start),
    )
    state.canCode?.let { code ->
        CmpBanner(
            icon = CmpIcons.Memory,
            tone = CmpBannerTone.Info,
            title = stringResource(txt.autostart_bypass_title_can, code),
            text = stringResource(txt.autostart_bypass_msg_can),
        )
    }
    val reboot = stringResource(txt.autostart_bypass_msg_reboot)
    val rebootUsb = stringResource(txt.autostart_bypass_msg_reboot_usb)
    CmpBanner(
        icon = CmpIcons.PowerSettingsNew,
        tone = CmpBannerTone.Warning,
        title = stringResource(txt.autostart_bypass_title_reboot),
        text = if (state.usb) "$reboot $rebootUsb" else reboot,
    )
}

@Composable
private fun StepCard(step: BypassStep?, layout: AutostartLayout) {
    val kind = step?.kind ?: BypassStepKind.WAIT
    IllustrationFrame(
        illustration = step?.illustration() ?: AutostartIllustration.IGNITION_OFF_REQUEST,
        layout = layout,
        mode = kind.illustrationMode(),
    )
    val style = stepCardStyle(kind)
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.background, shape)
            .border(style.border, shape)
            .padding(CmpSpacing.space20),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        KindLabel(kind)
        StepCardText(step, kind, style)
    }
}

private class StepCardStyle(val background: Color, val content: Color, val hint: Color, val border: BorderStroke)

@Composable
private fun stepCardStyle(kind: BypassStepKind): StepCardStyle {
    val colors = CmpTheme.colors
    return when (kind) {
        BypassStepKind.ACTION -> StepCardStyle(
            colors.brandPrimaryHighlight,
            colors.foregroundOnDarkPrimaryDefault,
            colors.foregroundOnDarkPrimaryDefault,
            BorderStroke(AccentBorder, colors.brandPrimaryDefault),
        )

        BypassStepKind.CRITICAL -> StepCardStyle(
            colors.warningHighlight,
            colors.foregroundOnDarkPrimaryDefault,
            colors.foregroundOnDarkPrimaryDefault,
            BorderStroke(AccentBorder, colors.warningDefault),
        )

        else -> StepCardStyle(
            colors.backgroundSurfaceHighlight,
            colors.foregroundOnDarkPrimaryDefault,
            colors.foregroundOnDarkSecondaryDefault,
            BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight),
        )
    }
}

@Composable
private fun StepCardText(step: BypassStep?, kind: BypassStepKind, style: StepCardStyle) {
    val text = stringResource(step?.text() ?: txt.autostart_bypass_item_prepare)
    when (kind) {
        BypassStepKind.CRITICAL -> {
            CmpText(text = text, style = CmpTextStyle.Paragraph1, color = style.content)
            CmpText(
                text = stringResource(txt.autostart_bypass_title_critical),
                style = CmpTextStyle.Title1,
                color = style.content,
            )
        }

        BypassStepKind.ACTION ->
            CmpText(text = text, style = CmpTextStyle.Title1, color = style.content)

        else -> CmpText(text = text, style = CmpTextStyle.Headline1, color = style.content)
    }
    val hint = when (kind) {
        BypassStepKind.CRITICAL -> txt.autostart_bypass_hint_critical
        BypassStepKind.ACTION -> txt.autostart_bypass_hint_action
        else -> txt.autostart_bypass_hint_wait
    }
    CmpText(text = stringResource(hint), color = style.hint, style = CmpTextStyle.Paragraph2)
}

@Composable
private fun KindLabel(kind: BypassStepKind) {
    val (icon, text) = when (kind) {
        BypassStepKind.ACTION -> CmpIcons.TouchApp to txt.autostart_bypass_label_action
        BypassStepKind.ACCEPTED -> CmpIcons.CheckCircle to txt.autostart_bypass_label_accepted
        BypassStepKind.CRITICAL -> CmpIcons.SyncLock to txt.autostart_bypass_label_critical
        BypassStepKind.WAIT, BypassStepKind.INSTRUCTION -> CmpIcons.HourglassTop to txt.autostart_bypass_label_wait
    }
    val tone = when (kind) {
        BypassStepKind.ACTION -> CmpTagTone.Brand
        BypassStepKind.CRITICAL -> CmpTagTone.Warning
        else -> CmpTagTone.Neutral
    }
    CmpTag(text = stringResource(text), tone = tone, icon = icon)
}

@Composable
private fun InstructionBody(step: BypassStep, instruction: Instruction, layout: AutostartLayout) {
    IllustrationFrame(step.illustration(), layout, mode = IllustrationMode.Request)
    CmpBanner(
        icon = CmpIcons.MenuBook,
        tone = CmpBannerTone.Info,
        title = stringResource(instruction.title),
        text = stringResource(instruction.lead),
    )
    CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        instruction.items.forEachIndexed { index, item -> InstructionRow(index + 1, item) }
    }
}

@Composable
private fun InstructionRow(number: Int, item: InstructionItem) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpBox(
            modifier = Modifier
                .size(NumberSize)
                .background(
                    if (item.wait) colors.backgroundSurfaceHighlight else colors.brandPrimaryDefault,
                    RoundedCornerShape(CmpRadius.round),
                ),
            contentAlignment = Alignment.Center,
        ) {
            CmpText(
                text = number.toString(),
                style = CmpTextStyle.Headline2,
                color = if (item.wait) colors.foregroundOnDarkPrimaryDefault else colors.onBrand,
            )
        }
        CmpColumn(
            modifier = Modifier.weight(1f).padding(top = CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            item.action?.let { action ->
                InstructionAction(stringResource(action), item.count?.let { stringResource(it) })
            }
            item.forbidden?.let {
                LabeledLine(CmpIcons.Block, txt.autostart_instruction_label_forbidden, it, colors.errorDefault)
            }
            item.response?.let { response ->
                LabeledLine(
                    icon = if (item.wait) CmpIcons.HourglassTop else CmpIcons.Hearing,
                    label = if (item.wait) {
                        txt.autostart_instruction_label_wait
                    } else {
                        txt.autostart_instruction_label_response
                    },
                    text = response,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
            item.then?.let { CmpText(text = stringResource(it), style = CmpTextStyle.Headline2) }
            item.fail?.let { fail ->
                CmpBanner(
                    icon = CmpIcons.Replay,
                    tone = CmpBannerTone.Error,
                    title = stringResource(txt.autostart_instruction_label_fail),
                    text = stringResource(fail),
                )
            }
            item.key?.let { KeyTag(it) }
        }
    }
}

@Composable
private fun InstructionAction(text: String, count: String?) {
    if (count == null) {
        CmpText(text = text, style = CmpTextStyle.Headline2)
    } else {
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(text = text, style = CmpTextStyle.Headline2)
            CmpTag(text = count, tone = CmpTagTone.Brand)
        }
    }
}

@Composable
private fun LabeledLine(icon: CmpIcons, label: StringResource, text: StringResource, color: Color) {
    CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpIcon(icon = icon, contentDescription = null, tint = color, modifier = Modifier.size(CmpSize.iconM))
        CmpText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(stringResource(label)) }
                append(" ")
                append(stringResource(text))
            },
            color = color,
            style = CmpTextStyle.Paragraph2,
        )
    }
}

@Composable
private fun KeyTag(key: InstructionKey) {
    val red = key == InstructionKey.RED
    CmpTag(
        text = stringResource(
            if (red) txt.autostart_instruction_label_key_red else txt.autostart_instruction_label_key_black,
        ),
        tone = if (red) CmpTagTone.Error else CmpTagTone.Neutral,
        icon = CmpIcons.VpnKey,
    )
}

@Composable
private fun ResultBody(state: BypassState, result: BypassResult, layout: AutostartLayout, inStep: Boolean) {
    IllustrationFrame(
        if (result is BypassResult.Success) AutostartIllustration.RESULT_SUCCESS else AutostartIllustration.RESULT_FAIL,
        layout,
        modifier = Modifier.testTag(BYPASS_RESULT_TAG),
    )
    when (result) {
        BypassResult.Success -> Headline(
            title = stringResource(txt.autostart_bypass_title_success),
            subtitle = stringResource(
                if (state.cloneAvailable) {
                    txt.autostart_bypass_subtitle_success_clone
                } else {
                    txt.autostart_bypass_subtitle_success
                },
            ),
        )

        BypassResult.CloneRequired -> Headline(
            title = stringResource(txt.autostart_bypass_title_clone_required),
            subtitle = stringResource(
                if (inStep) txt.autostart_bypass_subtitle_clone_next else txt.autostart_bypass_subtitle_clone_required,
            ),
        )

        BypassResult.NotSupported -> Headline(
            title = stringResource(txt.autostart_bypass_title_not_supported),
            subtitle = stringResource(txt.autostart_bypass_subtitle_not_supported),
        )

        is BypassResult.Error -> {
            Headline(title = stringResource(txt.autostart_bypass_title_error))
            ErrorBlock(result)
        }
    }
}

@Composable
private fun ErrorBlock(error: BypassResult.Error) {
    val texts = error.error.texts()
    val what = error.whatText()
    CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        what?.let {
            CmpBanner(
                icon = CmpIcons.ErrorOutline,
                tone = CmpBannerTone.Error,
                title = stringResource(txt.autostart_bypass_label_what),
                text = it,
            )
        }
        texts.check?.let { CheckPart(stringResource(it)) }
    }
}

@Composable
private fun CheckPart(text: String) {
    CmpBanner(
        text = text,
        title = stringResource(txt.autostart_bypass_label_check),
        tone = CmpBannerTone.Neutral,
        icon = CmpIcons.FactCheck,
    )
}

/** «Что случилось» по ошибке обхода — и в карточке ошибки, и в описании заявки на техпомощь. */
@Composable
private fun BypassResult.Error.whatText(): String? = if (error == BypassError.VAG_ECU_EXCHANGE) {
    stringResource(txt.autostart_bypass_error_ecu_what, code ?: 0)
} else {
    error.texts().what?.let { stringResource(it) }
}

private fun footer(
    state: BypassState,
    actions: BypassActions,
    step: BypassStepMode?,
): (@Composable ColumnScope.() -> Unit)? = when (val phase = state.phase) {
    is BypassPhase.Start -> {
        {
            AutostartPrimaryButton(
                txt.autostart_bypass_btn_start,
                actions.onStart,
                Modifier.testTag(BYPASS_START_TAG),
                icon = CmpIcons.PlayArrow,
            )
        }
    }

    is BypassPhase.Running -> null

    is BypassPhase.Finished -> {
        {
            val stepDone = phase.result == BypassResult.Success || phase.result == BypassResult.CloneRequired
            if (step != null && stepDone) {
                StepNextButton(
                    next = step.next,
                    onClick = { step.onNext(phase.result == BypassResult.CloneRequired) },
                    modifier = Modifier.testTag(BYPASS_STEP_NEXT_TAG),
                )
            } else {
                when (phase.result) {
                    BypassResult.Success -> {
                        AutostartPrimaryButton(txt.autostart_common_btn_done, actions.onClose)
                        if (state.cloneAvailable) {
                            AutostartSecondaryButton(
                                txt.autostart_common_btn_open_clone,
                                actions.onOpenClone,
                                modifier = Modifier.testTag(BYPASS_OPEN_CLONE_TAG),
                                icon = CmpIcons.ContentCopy,
                            )
                        }
                    }

                    BypassResult.CloneRequired -> {
                        AutostartPrimaryButton(
                            txt.autostart_common_btn_open_clone,
                            actions.onOpenClone,
                            Modifier.testTag(BYPASS_OPEN_CLONE_TAG),
                            icon = CmpIcons.ContentCopy,
                        )
                        AutostartSecondaryButton(txt.autostart_common_btn_close, actions.onClose)
                    }

                    BypassResult.NotSupported -> AutostartPrimaryButton(txt.autostart_common_btn_close, actions.onClose)

                    is BypassResult.Error -> {
                        AutostartPrimaryButton(txt.autostart_common_btn_retry, actions.onStart, icon = CmpIcons.Refresh)
                        val origin = stringResource(
                            if (step != null) {
                                txt.autostart_bypass_label_ticket_origin_fast
                            } else {
                                txt.autostart_bypass_label_ticket_origin
                            },
                        )
                        val ticket = stringResource(
                            txt.autostart_bypass_format_ticket,
                            phase.result.whatText() ?: stringResource(txt.autostart_bypass_title_error),
                        )
                        AutostartSecondaryButton(
                            txt.autostart_common_btn_ticket,
                            { actions.onOpenTechRequest(origin, ticket) },
                            icon = CmpIcons.Call,
                        )
                        AutostartChatButton(actions.onOpenSupportChat)
                    }
                }
            }
        }
    }
}

private val NumberSize = 40.dp
