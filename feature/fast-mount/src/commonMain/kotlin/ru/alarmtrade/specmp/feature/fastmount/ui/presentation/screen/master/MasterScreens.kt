package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_back
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_home
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_next
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_msg_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_title_step
import ru.alarmtrade.specmp.core.localization.resources.fastmount_reboot_format_next
import ru.alarmtrade.specmp.core.localization.resources.fastmount_reboot_msg_hint
import ru.alarmtrade.specmp.core.localization.resources.fastmount_reboot_msg_power
import ru.alarmtrade.specmp.core.localization.resources.fastmount_reboot_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_reboot_title_step
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_btn_finish
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_btn_sending
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_format_checks
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_format_minutes
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_format_star
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_hint_feedback
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_label_checks
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_label_feedback
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_label_time
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_msg_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_msg_required
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_title_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_review_title_summary
import ru.alarmtrade.specmp.core.localization.resources.fastmount_slave_label_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_slave_msg_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_slave_msg_step1
import ru.alarmtrade.specmp.core.localization.resources.fastmount_slave_msg_step2
import ru.alarmtrade.specmp.core.localization.resources.fastmount_slave_msg_step3
import ru.alarmtrade.specmp.core.localization.resources.fastmount_slave_title_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_start_error_title
import ru.alarmtrade.specmp.core.localization.resources.fastmount_start_msg_error
import ru.alarmtrade.specmp.core.localization.resources.fastmount_start_msg_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_start_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_error_bypass_not_finished
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_error_checks_not_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_error_clone_not_finished
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_error_feedback_required
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_error_session_lost
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_error_transport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_error
import ru.alarmtrade.specmp.core.localization.resources.fastmount_write_error_title
import ru.alarmtrade.specmp.core.localization.resources.fastmount_write_msg_error
import ru.alarmtrade.specmp.core.localization.resources.fastmount_write_msg_interrupted
import ru.alarmtrade.specmp.core.localization.resources.fastmount_write_msg_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_write_title_interrupted
import ru.alarmtrade.specmp.core.localization.resources.fastmount_write_title_screen
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountFailure
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Banner
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.BusyArt
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.ContentColumn
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Dock
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Hero
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.IndeterminateBar
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.InfoCard
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.KeyValue
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.LocalWizardWide
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.MainButton
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RailKey
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RoundArt
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Tag
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardNav
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardScaffold
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.formatElapsed
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.railKey
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.title

internal const val FAST_MOUNT_WRITE_RETRY_TAG = "fastmount_write_retry"
internal const val FAST_MOUNT_REBOOT_TAG = "fastmount_reboot"
internal const val FAST_MOUNT_SLAVE_NEXT_TAG = "fastmount_slave_next"
internal const val FAST_MOUNT_REVIEW_SEND_TAG = "fastmount_review_send"
internal const val FAST_MOUNT_DONE_TAG = "fastmount_done"

private const val TICK_MILLIS = 1_000L
private const val SLAVE_TEXT_WEIGHT = 1.15f
private const val REVIEW_FORM_WEIGHT = 1.3f

/** Действия шагов мастера. */
internal class MasterActions(
    val onBack: () -> Unit = {},
    val onStartRetry: () -> Unit = {},
    val onWriteRetry: () -> Unit = {},
    val onMountDone: () -> Unit = {},
    val onMapOpen: () -> Unit = {},
    val onMapRetry: () -> Unit = {},
    val onSlaveDoneChange: (Boolean) -> Unit = {},
    val onSlaveNext: () -> Unit = {},
    val onRatingChange: (Int) -> Unit = {},
    val onFeedbackChange: (String) -> Unit = {},
    val onFeedbackSend: () -> Unit = {},
    val onRetryStep: () -> Unit = {},
    val onHome: () -> Unit = {},
)

/** «Готовим монтаж…» (F6) и ошибка старта. */
@Composable
internal fun StartingScreen(state: FastMountState, actions: MasterActions) {
    val error = state.master.startError
    WizardScaffold(
        title = stringResource(RailKey.WRITE.title()),
        subtitle = state.fixed?.title,
        nav = if (error == null) WizardNav.LOCK else WizardNav.BACK,
        onBack = actions.onBack,
        centered = true,
        dock = error?.let {
            {
                Dock(
                    buttons = listOf(
                        {
                            MainButton(
                                text = stringResource(Strings.string.fastmount_common_btn_retry),
                                icon = CmpIcons.Refresh,
                                onClick = actions.onStartRetry,
                            )
                        },
                        {
                            MainButton(
                                text = stringResource(Strings.string.fastmount_common_btn_back),
                                outlined = true,
                                onClick = actions.onBack,
                            )
                        },
                    ),
                )
            }
        },
    ) {
        if (error == null) {
            Hero(
                title = stringResource(Strings.string.fastmount_start_title_screen),
                text = stringResource(Strings.string.fastmount_start_msg_screen),
                art = { BusyArt(icon = CmpIcons.CloudSync) },
            )
        } else {
            Hero(
                title = stringResource(Strings.string.fastmount_start_error_title),
                text = listOfNotNull(error.text(), stringResource(Strings.string.fastmount_start_msg_error))
                    .joinToString(" "),
                art = { RoundArt(icon = CmpIcons.CloudOff, tone = BannerTone.ERROR) },
            )
        }
    }
}

/** Запись настроек в блок (F7): процесс без процентов и ошибка с «Повторить». */
@Composable
internal fun WritingScreen(state: FastMountState, actions: MasterActions) {
    val error = state.master.error.takeIf { !state.master.busy }
    WizardScaffold(
        title = stringResource(RailKey.WRITE.title()),
        subtitle = state.fixed?.title,
        nav = if (error == null) WizardNav.LOCK else WizardNav.BACK,
        onBack = actions.onBack,
        centered = true,
        dock = error?.let {
            {
                Dock(
                    buttons = listOf {
                        MainButton(
                            text = stringResource(Strings.string.fastmount_common_btn_retry),
                            icon = CmpIcons.Refresh,
                            onClick = actions.onWriteRetry,
                            modifier = Modifier.testTag(FAST_MOUNT_WRITE_RETRY_TAG),
                        )
                    },
                )
            }
        },
    ) {
        if (error == null) {
            Hero(
                title = stringResource(Strings.string.fastmount_write_title_screen),
                text = stringResource(Strings.string.fastmount_write_msg_screen),
                art = { BusyArt(icon = CmpIcons.Memory) },
            ) {
                IndeterminateBar()
            }
        } else {
            ContentColumn(maxWidth = 640.dp) {
                Banner(
                    tone = BannerTone.ERROR,
                    title = stringResource(Strings.string.fastmount_write_error_title),
                    text = error.text() ?: stringResource(Strings.string.fastmount_write_msg_error),
                )
                Hero(
                    title = stringResource(Strings.string.fastmount_write_title_interrupted),
                    text = stringResource(Strings.string.fastmount_write_msg_interrupted),
                    art = { RoundArt(icon = CmpIcons.Memory) },
                )
            }
        }
    }
}

/** Перезагрузка блока (F9): таймер, «Не отключайте питание», «Далее: …». 60 с — потерю связи покажет обёртка. */
@Composable
internal fun RebootScreen(state: FastMountState, next: RailKey?, seconds: Int) {
    val colors = cmpThemeColors()
    WizardScaffold(
        title = stringResource(Strings.string.fastmount_reboot_title_step),
        subtitle = state.fixed?.title,
        nav = WizardNav.LOCK,
        onBack = {},
        centered = true,
        modifier = Modifier.testTag(FAST_MOUNT_REBOOT_TAG),
    ) {
        Hero(
            title = stringResource(Strings.string.fastmount_reboot_title_screen),
            text = stringResource(Strings.string.fastmount_reboot_msg_power),
            art = { BusyArt(icon = CmpIcons.RestartAlt, spin = true) },
        ) {
            CmpText(
                text = formatElapsed(seconds),
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
            )
            CmpText(
                text = stringResource(Strings.string.fastmount_reboot_msg_hint),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
            )
            Tag(
                text = stringResource(
                    Strings.string.fastmount_reboot_format_next,
                    stringResource(next?.title() ?: Strings.string.fastmount_done_title_step),
                ),
                icon = CmpIcons.ArrowForward,
            )
        }
    }
}

/** Секунды с момента, как экран перезагрузки появился (таймер экрана и колонки). */
@Composable
internal fun rememberRebootSeconds(active: Boolean): Int {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(active) {
        seconds = 0
        while (active) {
            delay(TICK_MILLIS)
            seconds += 1
        }
    }
    return seconds
}

/** Ошибка перехода на текущем шаге (F17): причина и «Повторить». */
@Composable
internal fun StepErrorBanner(state: FastMountState, actions: MasterActions) {
    val error = state.master.error ?: return
    if (state.master.busy) return
    Banner(
        tone = BannerTone.ERROR,
        title = stringResource(Strings.string.fastmount_step_title_error),
        text = error.text(),
        action = stringResource(Strings.string.fastmount_common_btn_retry),
        onAction = actions.onRetryStep,
    )
}

/** Обучение Slave (F11): анимация из старого приложения, 3 шага, «Обучение выполнено» в нижней панели. */
@Composable
internal fun SlaveScreen(state: FastMountState, actions: MasterActions) {
    val wide = LocalWizardWide.current
    val done = state.master.slaveDone
    val doneRow: @Composable () -> Unit = { SlaveDoneRow(done, actions.onSlaveDoneChange) }
    val nextButton: @Composable () -> Unit = {
        MainButton(
            text = stringResource(Strings.string.fastmount_common_btn_next),
            icon = CmpIcons.ArrowForward,
            enabled = done,
            busy = state.master.busy,
            onClick = actions.onSlaveNext,
            modifier = Modifier.testTag(FAST_MOUNT_SLAVE_NEXT_TAG),
        )
    }
    WizardScaffold(
        title = stringResource(RailKey.SLAVE.title()),
        subtitle = state.fixed?.title,
        nav = WizardNav.BACK,
        onBack = actions.onBack,
        dock = { Dock(buttons = if (wide) listOf(nextButton, doneRow) else listOf(doneRow, nextButton)) },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val two = maxWidth >= 860.dp
            ContentColumn(maxWidth = 1040.dp) {
                if (two) {
                    CmpRow(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SlaveAnimation(modifier = Modifier.weight(1f))
                        CmpColumn(
                            modifier = Modifier.weight(SLAVE_TEXT_WEIGHT),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            SlaveInstructions()
                        }
                    }
                } else {
                    SlaveAnimation(modifier = Modifier.fillMaxWidth())
                    SlaveInstructions()
                }
                StepErrorBanner(state, actions)
            }
        }
    }
}

@Composable
private fun SlaveInstructions() {
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CmpText(text = stringResource(Strings.string.fastmount_slave_title_lead), fontSize = 24.sp)
        CmpText(
            text = stringResource(Strings.string.fastmount_slave_msg_lead),
            style = CmpTextStyle.BodyLarge,
            color = colors.onSurfaceVariant,
        )
    }
    listOf(
        Strings.string.fastmount_slave_msg_step1,
        Strings.string.fastmount_slave_msg_step2,
        Strings.string.fastmount_slave_msg_step3,
    ).forEachIndexed { index, text ->
        CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            CmpBox(
                modifier = Modifier.size(36.dp).background(colors.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpText(text = "${index + 1}", fontWeight = FontWeight.Bold, color = colors.onSecondaryContainer)
            }
            CmpText(
                text = stringResource(text),
                style = CmpTextStyle.BodyLarge,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun SlaveDoneRow(done: Boolean, onChange: (Boolean) -> Unit) {
    val colors = cmpThemeColors()
    val shape = RoundedCornerShape(28.dp)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(if (done) colors.secondaryContainer else colors.surfaceContainerLow, shape)
            .border(1.5.dp, if (done) colors.primary else colors.outlineVariant, shape)
            .toggleable(value = done, role = Role.Checkbox, onValueChange = onChange)
            .padding(start = 6.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CmpCheckbox(checked = done, onCheckedChange = null)
        CmpText(
            text = stringResource(Strings.string.fastmount_slave_label_done),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Отзыв (F13): оценка и текст обязательны (правило SDK), итоги монтажа; ошибка — плашка (F17). */
@Composable
internal fun FeedbackScreen(state: FastMountState, actions: MasterActions) {
    val master = state.master
    val ready = master.rating > 0 && master.feedback.isNotBlank()
    WizardScaffold(
        title = stringResource(RailKey.REVIEW.title()),
        subtitle = state.fixed?.title,
        nav = if (master.busy) WizardNav.LOCK else WizardNav.BACK,
        onBack = actions.onBack,
        dock = {
            Dock(
                note = if (!ready &&
                    !master.busy
                ) {
                    stringResource(Strings.string.fastmount_review_msg_required)
                } else {
                    null
                },
                buttons = listOf {
                    MainButton(
                        text = stringResource(Strings.string.fastmount_review_btn_finish),
                        busyText = stringResource(Strings.string.fastmount_review_btn_sending),
                        icon = CmpIcons.DoneAll,
                        enabled = ready,
                        busy = master.busy,
                        onClick = actions.onFeedbackSend,
                        modifier = Modifier.testTag(FAST_MOUNT_REVIEW_SEND_TAG),
                    )
                },
            )
        },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val two = maxWidth >= 860.dp
            ContentColumn(maxWidth = 1040.dp) {
                if (two) {
                    CmpRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        CmpColumn(
                            modifier = Modifier.weight(REVIEW_FORM_WEIGHT),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            FeedbackForm(state, actions)
                        }
                        MountSummary(state, Modifier.weight(1f))
                    }
                } else {
                    FeedbackForm(state, actions)
                    MountSummary(state, Modifier)
                }
            }
        }
    }
}

@Composable
private fun FeedbackForm(state: FastMountState, actions: MasterActions) {
    val colors = cmpThemeColors()
    val master = state.master
    StepErrorBanner(state, actions)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CmpText(text = stringResource(Strings.string.fastmount_review_title_lead), fontSize = 24.sp)
        CmpText(
            text = stringResource(Strings.string.fastmount_review_msg_lead),
            style = CmpTextStyle.BodyLarge,
            color = colors.onSurfaceVariant,
        )
    }
    state.configuration?.let { configuration ->
        InfoCard {
            CmpText(text = configuration.author, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
            CmpText(text = configuration.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Stars(rating = master.rating, enabled = !master.busy, onChange = actions.onRatingChange)
        }
    }
    CmpTextField(
        value = master.feedback,
        onValueChange = actions.onFeedbackChange,
        label = stringResource(Strings.string.fastmount_review_label_feedback),
        placeholder = stringResource(Strings.string.fastmount_review_hint_feedback),
        enabled = !master.busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
    )
}

@Composable
private fun Stars(rating: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    val colors = cmpThemeColors()
    val star = cmpDomainColors().rating
    CmpRow(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (value in 1..MAX_RATING) {
            val a11y = stringResource(Strings.string.fastmount_review_format_star, value, MAX_RATING)
            CmpBox(
                modifier = Modifier
                    .size(56.dp)
                    .clickable(enabled = enabled, role = Role.RadioButton, onClickLabel = a11y) { onChange(value) },
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = if (value <= rating) CmpIcons.Star else CmpIcons.StarBorder,
                    contentDescription = a11y,
                    tint = if (value <= rating) star else colors.onSurfaceVariant,
                    modifier = Modifier.size(42.dp),
                )
            }
        }
    }
}

private const val MAX_RATING = 5

@Composable
private fun MountSummary(state: FastMountState, modifier: Modifier) {
    val checklist = state.master.checklist
    InfoCard(title = stringResource(Strings.string.fastmount_review_title_summary), modifier = modifier) {
        KeyValue(
            stringResource(Strings.string.fastmount_review_label_time),
            stringResource(Strings.string.fastmount_review_format_minutes, state.master.mountMinutes),
        )
        if (!state.usb && checklist.tasks.isNotEmpty()) {
            KeyValue(
                stringResource(Strings.string.fastmount_review_label_checks),
                stringResource(
                    Strings.string.fastmount_review_format_checks,
                    checklist.tasks.size - checklist.skipped - checklist.failed,
                    checklist.tasks.size,
                    checklist.skipped,
                ),
            )
        }
        val passed = state.plan?.steps.orEmpty()
            .filter { it.step != FastMountStep.FEEDBACK && (!it.conditional || state.master.cloneNeeded == true) }
            .mapNotNull { it.step.railKey() }
        CmpFlowRow(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            passed.forEach { key -> Tag(text = stringResource(key.title()), icon = CmpIcons.Check) }
        }
    }
}

/** «Готово» (F14): анимация успеха, «На главную». */
@Composable
internal fun DoneScreen(state: FastMountState, actions: MasterActions) {
    WizardScaffold(
        title = stringResource(Strings.string.fastmount_done_title_step),
        subtitle = state.fixed?.title,
        nav = WizardNav.NONE,
        onBack = {},
        centered = true,
        modifier = Modifier.testTag(FAST_MOUNT_DONE_TAG),
        dock = {
            Dock(
                buttons = listOf {
                    MainButton(
                        text = stringResource(Strings.string.fastmount_common_btn_home),
                        icon = CmpIcons.Home,
                        onClick = actions.onHome,
                    )
                },
            )
        },
    ) {
        Hero(
            title = stringResource(Strings.string.fastmount_done_title_screen),
            text = stringResource(Strings.string.fastmount_done_msg_screen),
            art = { SuccessArt() },
        )
    }
}

/** Успех: машина в круге и всплывающий значок (макет `SuccessArt`, плейсхолдер художника — иконки). */
@Composable
private fun SuccessArt() {
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    CmpBox(modifier = Modifier.size(180.dp)) {
        RoundArt(icon = CmpIcons.DirectionsCar, size = 160.dp, modifier = Modifier.align(Alignment.Center))
        CmpBox(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(72.dp)
                .scale(pop.value)
                .background(cmpDomainColors().success, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp),
            )
        }
    }
}

internal fun FastMountFailure.message(): StringResource = when (this) {
    FastMountFailure.SessionLost -> Strings.string.fastmount_step_error_session_lost
    FastMountFailure.BypassNotFinished -> Strings.string.fastmount_step_error_bypass_not_finished
    FastMountFailure.CloneNotFinished -> Strings.string.fastmount_step_error_clone_not_finished
    FastMountFailure.ChecksNotDone -> Strings.string.fastmount_step_error_checks_not_done
    FastMountFailure.FeedbackRequired -> Strings.string.fastmount_step_error_feedback_required
    FastMountFailure.TransportNotSupported -> Strings.string.fastmount_step_error_transport
}
