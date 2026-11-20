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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountFailure
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
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
    val onPassportStart: () -> Unit = {},
    val onPassportOpen: () -> Unit = {},
    val onPassportShare: () -> Unit = {},
)

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
                art = { RoundArt(icon = CmpIcons.CloudOff, tone = CmpBannerTone.Error) },
            )
        }
    }
}

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
                CmpBanner(
                    tone = CmpBannerTone.Error,
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

@Composable
internal fun RebootScreen(state: FastMountState, next: RailKey?, seconds: Int) {
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
            CmpText(text = formatElapsed(seconds), style = CmpTextStyle.Accent3)
            CmpText(
                text = stringResource(Strings.string.fastmount_reboot_msg_hint),
                style = CmpTextStyle.Paragraph2,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
            CmpTag(
                text = stringResource(
                    Strings.string.fastmount_reboot_format_next,
                    stringResource(next?.title() ?: Strings.string.fastmount_done_title_step),
                ),
                tone = CmpTagTone.Brand,
                icon = CmpIcons.ArrowForward,
            )
        }
    }
}

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

@Composable
internal fun StepErrorBanner(state: FastMountState, actions: MasterActions) {
    val error = state.master.error ?: return
    if (state.master.busy) return
    val title = stringResource(Strings.string.fastmount_step_title_error)
    val message = error.text()
    CmpBanner(
        tone = CmpBannerTone.Error,
        title = title.takeIf { message != null },
        text = message ?: title,
        actionLabel = stringResource(Strings.string.fastmount_common_btn_retry),
        onAction = actions.onRetryStep,
    )
}

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
                        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SlaveAnimation(modifier = Modifier.weight(1f))
                        CmpColumn(
                            modifier = Modifier.weight(SLAVE_TEXT_WEIGHT),
                            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
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
    val colors = CmpTheme.colors
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(text = stringResource(Strings.string.fastmount_slave_title_lead), style = CmpTextStyle.Headline1)
        CmpText(
            text = stringResource(Strings.string.fastmount_slave_msg_lead),
            style = CmpTextStyle.Paragraph1,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
    listOf(
        Strings.string.fastmount_slave_msg_step1,
        Strings.string.fastmount_slave_msg_step2,
        Strings.string.fastmount_slave_msg_step3,
    ).forEachIndexed { index, text ->
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            modifier = Modifier.padding(vertical = CmpSpacing.space4),
        ) {
            CmpBox(
                modifier = Modifier.size(StepNumberSize).background(colors.backgroundSurfaceHighlight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpText(text = "${index + 1}", style = CmpTextStyle.Headline3)
            }
            CmpText(
                text = stringResource(text),
                style = CmpTextStyle.Paragraph1,
                modifier = Modifier.padding(top = CmpSpacing.space4),
            )
        }
    }
}

private val StepNumberSize = 36.dp

@Composable
private fun SlaveDoneRow(done: Boolean, onChange: (Boolean) -> Unit) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.round)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.input)
            .background(if (done) colors.brandPrimaryHighlight else colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.inputs, if (done) colors.brandPrimaryDefault else colors.borderHighlight, shape)
            .toggleable(value = done, role = Role.Checkbox, onValueChange = onChange)
            .padding(start = CmpSpacing.space4, end = CmpSpacing.space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpCheckbox(checked = done, onCheckedChange = null)
        CmpText(
            text = stringResource(Strings.string.fastmount_slave_label_done),
            style = CmpTextStyle.Headline3,
        )
    }
}

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
                        icon = CmpIcons.DoneAll,
                        enabled = ready,
                        busy = master.busy,
                        busyText = stringResource(Strings.string.fastmount_review_btn_sending),
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
                    CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24)) {
                        CmpColumn(
                            modifier = Modifier.weight(REVIEW_FORM_WEIGHT),
                            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
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
    val secondary = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    val master = state.master
    StepErrorBanner(state, actions)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(text = stringResource(Strings.string.fastmount_review_title_lead), style = CmpTextStyle.Headline1)
        CmpText(
            text = stringResource(Strings.string.fastmount_review_msg_lead),
            style = CmpTextStyle.Paragraph1,
            color = secondary,
        )
    }
    state.configuration?.let { configuration ->
        InfoCard {
            CmpText(text = configuration.author, style = CmpTextStyle.Paragraph3, color = secondary)
            CmpText(text = configuration.name, style = CmpTextStyle.Headline3)
            Stars(rating = master.rating, enabled = !master.busy, onChange = actions.onRatingChange)
        }
    }
    CmpTextField(
        value = master.feedback,
        onValueChange = actions.onFeedbackChange,
        label = stringResource(Strings.string.fastmount_review_label_feedback),
        placeholder = stringResource(Strings.string.fastmount_review_hint_feedback),
        enabled = !master.busy,
        valueStyle = CmpTextStyle.Paragraph1,
        modifier = Modifier.fillMaxWidth().heightIn(min = FeedbackMinHeight),
    )
}

private val FeedbackMinHeight = 132.dp

@Composable
private fun Stars(rating: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    val secondary = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    val star = cmpDomainColors().rating
    CmpRow(
        modifier = Modifier.padding(top = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        for (value in 1..MAX_RATING) {
            val a11y = stringResource(Strings.string.fastmount_review_format_star, value, MAX_RATING)
            CmpBox(
                modifier = Modifier
                    .size(CmpSize.iconXxl)
                    .clip(CircleShape)
                    .clickable(enabled = enabled, role = Role.RadioButton, onClickLabel = a11y) { onChange(value) },
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = if (value <= rating) CmpIcons.Star else CmpIcons.StarBorder,
                    contentDescription = a11y,
                    tint = if (value <= rating) star else secondary,
                    modifier = Modifier.size(CmpSize.iconXl),
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
            modifier = Modifier.padding(top = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            passed.forEach { key ->
                CmpTag(text = stringResource(key.title()), tone = CmpTagTone.Success, icon = CmpIcons.Check)
            }
        }
    }
}

@Composable
internal fun DoneScreen(state: FastMountState, actions: MasterActions, passportShare: Boolean) {
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
        ) {
            DonePassportBlock(passport = state.master.passport.passport, canShare = passportShare, actions = actions)
        }
    }
}

@Composable
private fun SuccessArt() {
    val colors = CmpTheme.colors
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    CmpBox(modifier = Modifier.size(SuccessArtSize)) {
        RoundArt(icon = CmpIcons.DirectionsCar, size = SuccessCarSize, modifier = Modifier.align(Alignment.Center))
        CmpBox(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(SuccessBadgeSize)
                .scale(pop.value)
                .background(colors.successDefault, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Check,
                contentDescription = null,
                tint = colors.onBrand,
                modifier = Modifier.size(CmpSize.iconXl),
            )
        }
    }
}

private val SuccessArtSize = 180.dp
private val SuccessCarSize = 160.dp
private val SuccessBadgeSize = 72.dp

internal fun FastMountFailure.message(): StringResource = when (this) {
    FastMountFailure.SessionLost -> Strings.string.fastmount_step_error_session_lost
    FastMountFailure.BypassNotFinished -> Strings.string.fastmount_step_error_bypass_not_finished
    FastMountFailure.CloneNotFinished -> Strings.string.fastmount_step_error_clone_not_finished
    FastMountFailure.ChecksNotDone -> Strings.string.fastmount_step_error_checks_not_done
    FastMountFailure.FeedbackRequired -> Strings.string.fastmount_step_error_feedback_required
    FastMountFailure.TransportNotSupported -> Strings.string.fastmount_step_error_transport
}
