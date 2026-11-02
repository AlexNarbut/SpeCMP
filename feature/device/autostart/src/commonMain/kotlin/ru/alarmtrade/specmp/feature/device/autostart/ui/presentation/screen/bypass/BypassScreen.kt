package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_chat
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_open_clone
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_retry
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartIllustration
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartLayout
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartPrimaryButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartScaffold
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartSecondaryButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.ExitDialog
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.Headline
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.HowItWorks
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationFrame
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationMode
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StatusRow
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StepAppear
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.StepNextButton
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepChrome
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepNext
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialog

/** Короткий доступ к строкам фичи (ключи длинные). */
private val txt = Strings.string

internal const val BYPASS_START_TAG = "autostart_bypass_start"
internal const val BYPASS_OPEN_CLONE_TAG = "autostart_bypass_open_clone"
internal const val BYPASS_RESULT_TAG = "autostart_bypass_result"
internal const val BYPASS_STEP_NEXT_TAG = "autostart_bypass_step_next"

/** Действия экрана; один экземпляр на экран. */
@Immutable
internal data class BypassActions(
    val onBack: () -> Unit = {},
    val onClose: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
    val onExitConfirm: () -> Unit = {},
    val onOpenClone: () -> Unit = {},
    val onOpenSupportChat: () -> Unit = {}
)

/**
 * Экран «Бесключевой обход» (раздел расширенного монтажа; Clone — отдельный экран, открывается поверх через
 * [onOpenClone]).
 */
@Composable
fun BypassLearningEntry(onBack: () -> Unit, onOpenClone: () -> Unit, onOpenSupportChat: () -> Unit) {
    val viewModel = koinViewModel<BypassLearningViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack, onOpenClone, onOpenSupportChat) {
        BypassActions(
            onBack = { if (viewModel.onBackRequest()) onBack() },
            onClose = onBack,
            onStart = viewModel::onStartClick,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onExitConfirm = {
                viewModel.onExitConfirm()
                onBack()
            },
            onOpenClone = onOpenClone,
            onOpenSupportChat = onOpenSupportChat
        )
    }
    BypassContent(state = state, actions = actions)
}

/**
 * Обход как шаг мастера быстрого монтажа: шапка и нижняя панель — мастера ([chrome]), тело, статус, ошибки и диалоги —
 * этого экрана. Без перехода в Clone (его ведёт мастер): на успехе и «нужен Clone» — «Далее» → [onNext].
 *
 * @param key ключ VM: новый ключ — обучение с начала (мастер вернулся к монтажу), тот же — прежний итог.
 */
@Composable
fun BypassLearningStep(
    key: String,
    chrome: AutostartStepChrome,
    next: AutostartStepNext,
    onNext: (cloneRequired: Boolean) -> Unit,
    onBack: () -> Unit,
    onOpenSupportChat: () -> Unit
) {
    val viewModel = koinViewModel<BypassLearningViewModel>(key = key)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack, onOpenSupportChat) {
        BypassActions(
            onBack = { if (viewModel.onBackRequest()) onBack() },
            onClose = onBack,
            onStart = viewModel::onStartClick,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onExitConfirm = viewModel::onExitConfirm,
            onOpenSupportChat = onOpenSupportChat
        )
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = actions.onBack
    )
    BypassContent(
        state = state.copy(cloneAvailable = false),
        actions = actions,
        stepMode = BypassStepMode(chrome, next, onNext)
    )
}

/** Режим шага мастера (см. [BypassLearningStep]). */
internal class BypassStepMode(
    val chrome: AutostartStepChrome,
    val next: AutostartStepNext,
    val onNext: (cloneRequired: Boolean) -> Unit
)

/** Stateless-контент: фаза → тело и кнопки; диалоги поверх. [stepMode] — шаг мастера быстрого монтажа. */
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

                // Каждый новый шаг появляется (макет `bcIn`); ключ — сам шаг.
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
        chrome = stepMode?.chrome
    )
    when (state.dialog) {
        is BypassDialog.Pin -> ServicePinDialog(
            message = stringResource(txt.autostart_common_msg_pin),
            state = state.dialog.pin,
            onDismiss = actions.onDialogDismiss,
            onSubmit = actions.onPinSubmit,
            testTagPrefix = "autostart_pin"
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
    CmpIcons.Visibility to stringResource(txt.autostart_bypass_item_how_watch)
)

@Composable
private fun StartBody(state: BypassState, phase: BypassPhase.Start, layout: AutostartLayout) {
    IllustrationFrame(AutostartIllustration.IGNITION_OFF_REQUEST, layout)
    if (phase.interrupted) {
        InfoBanner(
            icon = CmpIcons.LinkOff,
            tone = BannerTone.Error,
            title = stringResource(txt.autostart_common_title_interrupted),
            text = stringResource(txt.autostart_bypass_msg_interrupted)
        )
    }
    Headline(
        title = stringResource(txt.autostart_bypass_title_start),
        subtitle = stringResource(txt.autostart_bypass_subtitle_start)
    )
    state.canCode?.let { code ->
        InfoBanner(
            icon = CmpIcons.Memory,
            tone = BannerTone.Info,
            title = stringResource(txt.autostart_bypass_title_can, code),
            text = stringResource(txt.autostart_bypass_msg_can)
        )
    }
    val reboot = stringResource(txt.autostart_bypass_msg_reboot)
    val rebootUsb = stringResource(txt.autostart_bypass_msg_reboot_usb)
    InfoBanner(
        icon = CmpIcons.PowerSettingsNew,
        tone = BannerTone.Warn,
        title = stringResource(txt.autostart_bypass_title_reboot),
        text = if (state.usb) "$reboot $rebootUsb" else reboot
    )
}

/** Карточка-команда под иллюстрацией (макет `StepCard`, вариант A). */
@Composable
private fun StepCard(step: BypassStep?, layout: AutostartLayout) {
    val kind = step?.kind ?: BypassStepKind.WAIT
    IllustrationFrame(
        illustration = step?.illustration() ?: AutostartIllustration.IGNITION_OFF_REQUEST,
        layout = layout,
        mode = kind.illustrationMode()
    )
    val style = stepCardStyle(kind)
    val shape = RoundedCornerShape(24.dp)
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.background, shape)
            .border(style.border, shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        KindLabel(kind)
        StepCardText(step, kind, style)
    }
}

/** Цвета карточки: действие — primary, критичный обмен — tertiary, остальное — нейтральная. */
private class StepCardStyle(val background: Color, val content: Color, val hint: Color, val border: BorderStroke)

@Composable
private fun stepCardStyle(kind: BypassStepKind): StepCardStyle {
    val colors = cmpThemeColors()
    return when (kind) {
        BypassStepKind.ACTION -> StepCardStyle(
            colors.primaryContainer,
            colors.onPrimaryContainer,
            colors.onPrimaryContainer,
            BorderStroke(3.dp, colors.primary)
        )

        BypassStepKind.CRITICAL -> StepCardStyle(
            colors.tertiaryContainer,
            colors.onTertiaryContainer,
            colors.onTertiaryContainer,
            BorderStroke(3.dp, colors.tertiary)
        )

        else -> StepCardStyle(
            colors.surfaceContainerHigh,
            colors.onSurface,
            colors.onSurfaceVariant,
            BorderStroke(1.dp, colors.outlineVariant)
        )
    }
}

@Composable
private fun StepCardText(step: BypassStep?, kind: BypassStepKind, style: StepCardStyle) {
    val text = stringResource(step?.text() ?: txt.autostart_bypass_item_prepare)
    when (kind) {
        BypassStepKind.CRITICAL -> {
            CmpText(text = text, color = style.content, fontSize = 19.sp)
            CmpText(
                text = stringResource(txt.autostart_bypass_title_critical),
                color = style.content,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        BypassStepKind.ACTION ->
            CmpText(text = text, color = style.content, fontSize = 30.sp, fontWeight = FontWeight.Bold)

        else -> CmpText(text = text, color = style.content, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    }
    val hint = when (kind) {
        BypassStepKind.CRITICAL -> txt.autostart_bypass_hint_critical
        BypassStepKind.ACTION -> txt.autostart_bypass_hint_action
        else -> txt.autostart_bypass_hint_wait
    }
    CmpText(text = stringResource(hint), color = style.hint, style = CmpTextStyle.Body)
}

@Composable
private fun KindLabel(kind: BypassStepKind) {
    val colors = cmpThemeColors()
    val (icon, text) = when (kind) {
        BypassStepKind.ACTION -> CmpIcons.TouchApp to txt.autostart_bypass_label_action
        BypassStepKind.ACCEPTED -> CmpIcons.CheckCircle to txt.autostart_bypass_label_accepted
        BypassStepKind.CRITICAL -> CmpIcons.SyncLock to txt.autostart_bypass_label_critical
        BypassStepKind.WAIT, BypassStepKind.INSTRUCTION -> CmpIcons.HourglassTop to txt.autostart_bypass_label_wait
    }
    val (bg, fg) = when (kind) {
        BypassStepKind.ACTION -> colors.primary to colors.onPrimary
        BypassStepKind.CRITICAL -> colors.tertiary to colors.onTertiary
        else -> colors.surfaceContainerHighest to colors.onSurface
    }
    CmpRow(
        modifier = Modifier.height(36.dp).background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        CmpText(text = stringResource(text), color = fg, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

/** Инструкция: монтажник выполняет пункты сам, блок ждёт (макет `InstrView`). */
@Composable
private fun InstructionBody(step: BypassStep, instruction: Instruction, layout: AutostartLayout) {
    IllustrationFrame(step.illustration(), layout, mode = IllustrationMode.Request)
    InfoBanner(
        icon = CmpIcons.MenuBook,
        tone = BannerTone.Info,
        title = stringResource(instruction.title),
        text = stringResource(instruction.lead)
    )
    CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        instruction.items.forEachIndexed { index, item -> InstructionRow(index + 1, item) }
    }
}

@Composable
private fun InstructionRow(number: Int, item: InstructionItem) {
    val colors = cmpThemeColors()
    val shape = RoundedCornerShape(18.dp)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, shape)
            .border(1.dp, colors.outlineVariant, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CmpBox(
            modifier = Modifier
                .size(40.dp)
                .background(if (item.wait) colors.surfaceContainerHighest else colors.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CmpText(
                text = number.toString(),
                color = if (item.wait) colors.onSurface else colors.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        CmpColumn(
            modifier = Modifier.weight(1f).padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item.action?.let { action ->
                val count = item.count?.let { stringResource(it) }
                val text = stringResource(action)
                CmpText(
                    text = if (count != null) "$text — $count" else text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item.forbidden?.let {
                LabeledLine(CmpIcons.Block, txt.autostart_instruction_label_forbidden, it, colors.error)
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
                    color = colors.onSurfaceVariant
                )
            }
            item.then?.let { CmpText(text = stringResource(it), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
            item.fail?.let { fail ->
                CmpBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.errorContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    LabeledLine(CmpIcons.Replay, txt.autostart_instruction_label_fail, fail, colors.onErrorContainer)
                }
            }
            item.key?.let { KeyTag(it) }
        }
    }
}

@Composable
private fun LabeledLine(icon: CmpIcons, label: StringResource, text: StringResource, color: Color) {
    CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpIcon(icon = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        CmpText(text = "${stringResource(label)} ${stringResource(text)}", color = color, style = CmpTextStyle.Body)
    }
}

/** Метка ключа: цвет — сама суть (красный обучающий / чёрный рабочий), поэтому фиксированные цвета. */
@Composable
private fun KeyTag(key: InstructionKey) {
    val red = key == InstructionKey.RED
    CmpRow(
        modifier = Modifier
            .height(30.dp)
            .background(if (red) KeyRed else KeyBlack, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CmpIcon(icon = CmpIcons.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        CmpText(
            text = stringResource(
                if (red) txt.autostart_instruction_label_key_red else txt.autostart_instruction_label_key_black
            ),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ResultBody(state: BypassState, result: BypassResult, layout: AutostartLayout, inStep: Boolean) {
    IllustrationFrame(
        if (result is BypassResult.Success) AutostartIllustration.RESULT_SUCCESS else AutostartIllustration.RESULT_FAIL,
        layout,
        modifier = Modifier.testTag(BYPASS_RESULT_TAG)
    )
    when (result) {
        BypassResult.Success -> Headline(
            title = stringResource(txt.autostart_bypass_title_success),
            subtitle = stringResource(
                if (state.cloneAvailable) {
                    txt.autostart_bypass_subtitle_success_clone
                } else {
                    txt.autostart_bypass_subtitle_success
                }
            )
        )

        BypassResult.CloneRequired -> Headline(
            title = stringResource(txt.autostart_bypass_title_clone_required),
            subtitle = stringResource(
                if (inStep) txt.autostart_bypass_subtitle_clone_next else txt.autostart_bypass_subtitle_clone_required
            )
        )

        BypassResult.NotSupported -> Headline(
            title = stringResource(txt.autostart_bypass_title_not_supported),
            subtitle = stringResource(txt.autostart_bypass_subtitle_not_supported)
        )

        is BypassResult.Error -> {
            Headline(title = stringResource(txt.autostart_bypass_title_error))
            ErrorBlock(result)
        }
    }
}

/** «Что случилось» / «Что проверить» (D4): две строки ресурса на ошибку, любая может отсутствовать. */
@Composable
private fun ErrorBlock(error: BypassResult.Error) {
    val colors = cmpThemeColors()
    val texts = error.error.texts()
    val what = if (error.error == BypassError.VAG_ECU_EXCHANGE) {
        stringResource(txt.autostart_bypass_error_ecu_what, error.code ?: 0)
    } else {
        texts.what?.let { stringResource(it) }
    }
    CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        what?.let {
            ErrorPart(
                CmpIcons.ErrorOutline,
                txt.autostart_bypass_label_what,
                it,
                colors.errorContainer,
                colors.onErrorContainer
            )
        }
        texts.check?.let {
            ErrorPart(
                CmpIcons.FactCheck,
                txt.autostart_bypass_label_check,
                stringResource(it),
                colors.surfaceContainerHigh,
                colors.onSurface
            )
        }
    }
}

@Composable
private fun ErrorPart(icon: CmpIcons, title: StringResource, text: String, bg: Color, fg: Color) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().background(
            bg,
            RoundedCornerShape(18.dp)
        ).padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = fg, modifier = Modifier.size(26.dp))
        CmpColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpText(text = stringResource(title), color = fg, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            CmpText(text = text, color = fg, fontSize = 18.sp)
        }
    }
}

/** Кнопки по фазе (макет): старт / итог; во время обучения кнопок нет. */
private fun footer(
    state: BypassState,
    actions: BypassActions,
    step: BypassStepMode?
): (@Composable ColumnScope.() -> Unit)? = when (val phase = state.phase) {
    is BypassPhase.Start -> {
        {
            AutostartPrimaryButton(
                txt.autostart_bypass_btn_start,
                actions.onStart,
                Modifier.testTag(BYPASS_START_TAG)
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
                    modifier = Modifier.testTag(BYPASS_STEP_NEXT_TAG)
                )
            } else {
                when (phase.result) {
                    BypassResult.Success -> {
                        AutostartPrimaryButton(txt.autostart_common_btn_done, actions.onClose)
                        if (state.cloneAvailable) {
                            AutostartSecondaryButton(
                                txt.autostart_common_btn_open_clone,
                                actions.onOpenClone,
                                modifier = Modifier.testTag(BYPASS_OPEN_CLONE_TAG)
                            )
                        }
                    }

                    BypassResult.CloneRequired -> {
                        AutostartPrimaryButton(
                            txt.autostart_common_btn_open_clone,
                            actions.onOpenClone,
                            Modifier.testTag(BYPASS_OPEN_CLONE_TAG)
                        )
                        AutostartSecondaryButton(txt.autostart_common_btn_close, actions.onClose)
                    }

                    BypassResult.NotSupported -> AutostartPrimaryButton(txt.autostart_common_btn_close, actions.onClose)

                    is BypassResult.Error -> {
                        AutostartPrimaryButton(txt.autostart_common_btn_retry, actions.onStart)
                        AutostartSecondaryButton(txt.autostart_common_btn_chat, actions.onOpenSupportChat)
                    }
                }
            }
        }
    }
}

private val KeyRed = Color(0xFFC62828)
private val KeyBlack = Color(0xFF1F2324)
