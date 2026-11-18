package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.signallog_actions_btn_clear
import ru.alarmtrade.specmp.core.localization.resources.signallog_actions_btn_clear_log
import ru.alarmtrade.specmp.core.localization.resources.signallog_actions_btn_share
import ru.alarmtrade.specmp.core.localization.resources.signallog_clear_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.signallog_clear_btn_confirm
import ru.alarmtrade.specmp.core.localization.resources.signallog_clear_msg_count
import ru.alarmtrade.specmp.core.localization.resources.signallog_clear_title
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_btn_save
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_btn_send
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_error_problem
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_format_date_time
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_label_problem
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_label_summary
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_loading_save
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_loading_send
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_msg_failed
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_msg_intro
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_placeholder_problem
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_title
import ru.alarmtrade.specmp.core.localization.resources.signallog_share_title_failed
import ru.alarmtrade.specmp.core.ui.format.rememberCmpDateLabeler
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.time.currentTimeZone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogActions
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogLayout
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogShareAction
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogState

internal const val SIGNAL_LOG_CLEAR_TAG = "signallog_clear"
internal const val SIGNAL_LOG_SHARE_TAG = "signallog_share"
internal const val SIGNAL_LOG_PROBLEM_TAG = "signallog_problem"
internal const val SIGNAL_LOG_SEND_TAG = "signallog_send"
internal const val SIGNAL_LOG_SAVE_TAG = "signallog_save"
internal const val SIGNAL_LOG_CLEAR_CONFIRM_TAG = "signallog_clear_confirm"

private val ShareButtonMaxWidth = 240.dp
private const val PROBLEM_MIN_LINES = 4

private val ClockFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}
private val SendLabels = Strings.string.signallog_share_btn_send to Strings.string.signallog_share_btn_retry

@Composable
internal fun SignalLogActionsBar(state: SignalLogState, actions: SignalLogActions, layout: SignalLogLayout) {
    val colors = CmpTheme.colors
    CmpColumn(modifier = Modifier.fillMaxWidth().background(colors.backgroundPagePrimary).navigationBarsPadding()) {
        CmpDivider(color = colors.borderHighlight)
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = layout.paddingH, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpButton(
                text = stringResource(Strings.string.signallog_actions_btn_clear),
                onClick = actions.onClearRequest,
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.XL,
                icon = CmpIcons.Delete,
                enabled = state.hasLines,
                uppercase = false,
                modifier = Modifier.testTag(SIGNAL_LOG_CLEAR_TAG),
            )
            CmpRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                CmpButton(
                    text = stringResource(Strings.string.signallog_actions_btn_share),
                    onClick = actions.onShareOpen,
                    size = CmpButtonSize.XL,
                    icon = CmpIcons.Share,
                    enabled = state.hasLines,
                    fullWidth = true,
                    uppercase = false,
                    modifier = Modifier.widthIn(max = ShareButtonMaxWidth).testTag(SIGNAL_LOG_SHARE_TAG),
                )
            }
        }
    }
}

/** Телефон и планшет до 1100: лист снизу (на планшете — шириной до 640, предел листа Material). */
@Composable
internal fun SignalLogShareSheet(state: SignalLogState, actions: SignalLogActions, layout: SignalLogLayout) {
    CmpBottomSheet(onDismissRequest = actions.onShareDismiss) {
        CmpColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = if (layout.compact) CmpSpacing.space16 else CmpSpacing.space32)
                .padding(bottom = CmpSpacing.space24),
        ) {
            ShareForm(state = state, actions = actions, panel = false)
        }
    }
}

@Composable
internal fun SignalLogSidePanel(state: SignalLogState, actions: SignalLogActions, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpRow(modifier = modifier) {
        CmpVerticalDivider(color = colors.borderHighlight)
        CmpColumn(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(CmpSpacing.space24),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            ShareForm(state = state, actions = actions, panel = true)
            CmpDivider(color = colors.borderHighlight)
            CmpButton(
                text = stringResource(Strings.string.signallog_actions_btn_clear_log),
                onClick = actions.onClearRequest,
                type = CmpButtonType.TertiaryDanger,
                size = CmpButtonSize.XL,
                icon = CmpIcons.Delete,
                enabled = state.hasLines,
                fullWidth = true,
                uppercase = false,
                modifier = Modifier.testTag(SIGNAL_LOG_CLEAR_TAG),
            )
        }
    }
}

/** Отправка в техподдержку показывается всегда: признака «у аккаунта нет техподдержки» в приложении нет. */
@Composable
private fun ShareForm(state: SignalLogState, actions: SignalLogActions, panel: Boolean) {
    val colors = CmpTheme.colors
    val form = state.share
    val busy = form.busy
    val problemTitle = stringResource(Strings.string.signallog_share_label_problem)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        CmpRow(verticalAlignment = Alignment.CenterVertically) {
            CmpText(
                text = stringResource(Strings.string.signallog_share_title),
                style = CmpTextStyle.Headline1,
                modifier = Modifier.weight(1f),
            )
            if (!panel) {
                CmpIconButton(
                    onClick = actions.onShareDismiss,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.signallog_share_a11y_close),
                )
            }
        }
        CmpText(
            text = stringResource(Strings.string.signallog_share_msg_intro),
            style = CmpTextStyle.Paragraph2,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            CmpText(text = problemTitle, style = CmpTextStyle.Headline3)
            CmpTextField(
                value = form.problem,
                onValueChange = actions.onProblemChange,
                placeholder = stringResource(Strings.string.signallog_share_placeholder_problem),
                enabled = busy == null && state.hasLines,
                isError = form.problemError,
                minLines = PROBLEM_MIN_LINES,
                valueStyle = CmpTextStyle.Paragraph1,
                modifier = Modifier.fillMaxWidth().testTag(SIGNAL_LOG_PROBLEM_TAG),
            )
            if (form.problemError) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpIcon(CmpIcons.AlertCircle, contentDescription = null, tint = colors.errorDefault)
                    CmpText(
                        text = stringResource(Strings.string.signallog_share_error_problem),
                        style = CmpTextStyle.Paragraph3,
                        color = colors.errorDefault,
                    )
                }
            }
        }
        InfoPlate(icon = CmpIcons.Description) {
            CmpText(text = summaryText(state.summary), style = CmpTextStyle.Paragraph2, modifier = Modifier.weight(1f))
        }
        if (form.sendFailed) {
            InfoPlate(icon = CmpIcons.AlertCircle, error = true) {
                CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    CmpText(
                        text = stringResource(Strings.string.signallog_share_title_failed),
                        style = CmpTextStyle.Headline3,
                    )
                    CmpText(
                        text = stringResource(Strings.string.signallog_share_msg_failed),
                        style = CmpTextStyle.Paragraph3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
        }
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            CmpButton(
                text = stringResource(if (form.sendFailed) SendLabels.second else SendLabels.first),
                onClick = { actions.onSend(problemTitle) },
                size = CmpButtonSize.XL,
                icon = if (form.sendFailed) CmpIcons.Refresh else CmpIcons.Send,
                enabled = busy == null && state.hasLines,
                loading = busy == SignalLogShareAction.SEND,
                loadingText = stringResource(Strings.string.signallog_share_loading_send),
                fullWidth = true,
                uppercase = false,
                modifier = Modifier.testTag(SIGNAL_LOG_SEND_TAG),
            )
            CmpButton(
                text = stringResource(Strings.string.signallog_share_btn_save),
                onClick = { actions.onSave(problemTitle) },
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.XL,
                icon = CmpIcons.Download,
                enabled = busy == null && state.hasLines,
                loading = busy == SignalLogShareAction.SAVE,
                loadingText = stringResource(Strings.string.signallog_share_loading_save),
                fullWidth = true,
                uppercase = false,
                modifier = Modifier.testTag(SIGNAL_LOG_SAVE_TAG),
            )
        }
    }
}

@Composable
private fun InfoPlate(
    icon: CmpIcons,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (error) colors.errorHighlight else colors.backgroundSurfaceHighlight,
                RoundedCornerShape(CmpRadius.cardSmall),
            )
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(
            icon,
            contentDescription = null,
            tint = if (error) colors.errorDefault else colors.foregroundOnDarkSecondaryDefault,
        )
        content()
    }
}

/** Сводка по всему логу блока: в пределах суток — только время, иначе — день и время. */
@Composable
private fun summaryText(summary: SignalLogSummary): String {
    val first = summary.firstTimeMs
    val last = summary.lastTimeMs
    if (summary.count == 0 || first == null || last == null) {
        return stringResource(Strings.string.signallog_share_msg_empty)
    }
    val timeZone = currentTimeZone()
    val labeler = rememberCmpDateLabeler()
    val from = first.local(timeZone)
    val to = last.local(timeZone)
    val sameDay = from.date == to.date
    val dateTime = Strings.string.signallog_share_format_date_time
    val fromText = ClockFormat.format(from.time).let { time ->
        if (sameDay) time else stringResource(dateTime, labeler(from.date), time)
    }
    val toText = ClockFormat.format(to.time).let { time ->
        if (sameDay) time else stringResource(dateTime, labeler(to.date), time)
    }
    val count = summary.count
    return pluralStringResource(Strings.plurals.signallog_share_label_summary, count, count, fromText, toText)
}

private fun Long.local(timeZone: TimeZone): LocalDateTime =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)

@Composable
internal fun SignalLogClearDialog(count: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        icon = CmpIcons.Delete.imageVector(),
        title = {
            CmpText(text = stringResource(Strings.string.signallog_clear_title), style = CmpTextStyle.Headline1)
        },
        text = {
            CmpText(
                text = pluralStringResource(Strings.plurals.signallog_clear_msg_count, count, count),
                style = CmpTextStyle.Paragraph2,
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.signallog_clear_btn_cancel),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.signallog_clear_btn_confirm),
                onClick = onConfirm,
                type = CmpButtonType.Danger,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag(SIGNAL_LOG_CLEAR_CONFIRM_TAG),
            )
        },
    )
}
