package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_btn_skip
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_format_failed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_format_progress
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_label_current
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_label_now
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_msg_all_done
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_msg_auto
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_msg_in_progress
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_msg_waiting
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_status_no
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_status_passed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_status_skipped
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_status_yes
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_title_all_done
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateChecklist
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistStatus
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistTask

internal const val CHECKLIST_TASK_TAG_PREFIX = "devicestate_task_"
private const val TONE_BORDER_ALPHA = 0.3f
private val BadgeSize = 36.dp
private val BadgeRing = 2.dp
private val ProgressHeight = 8.dp

@Composable
internal fun ChecklistPanel(
    checklist: CheckStateChecklist,
    vehicle: VehicleState?,
    onSkip: (String) -> Unit,
    onAnswer: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    onlyCurrent: Boolean = false,
) {
    val colors = CmpTheme.colors
    val total = checklist.tasks.size
    val allDone = checklist.allClosed
    val order = if (checklist.sequential) {
        checklist.tasks
    } else {
        checklist.tasks.sortedBy { if (it.status == ChecklistStatus.PENDING) 0 else 1 }
    }
    val headerShape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (allDone) colors.successHighlight else colors.backgroundSurfaceHighlight, headerShape)
                .border(
                    CmpBorderWidth.listItem,
                    if (allDone) colors.successDefault.copy(alpha = TONE_BORDER_ALPHA) else colors.borderHighlight,
                    headerShape,
                )
                .padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                if (allDone) {
                    CmpIcon(
                        icon = CmpIcons.TaskAlt,
                        contentDescription = null,
                        tint = colors.successDefault,
                        modifier = Modifier.size(CmpSize.iconL),
                    )
                }
                CmpText(
                    text = if (allDone) {
                        stringResource(Strings.string.devicestate_fast_title_all_done)
                    } else {
                        stringResource(Strings.string.devicestate_fast_format_progress, checklist.closed, total)
                    },
                    style = CmpTextStyle.Headline1,
                )
            }
            CmpLinearProgressIndicator(
                progress = if (total == 0) 1f else checklist.closed.toFloat() / total,
                color = if (allDone) colors.successDefault else colors.brandPrimaryDefault,
                modifier = Modifier.fillMaxWidth().height(ProgressHeight),
            )
            CmpText(
                text = when {
                    !allDone -> stringResource(Strings.string.devicestate_fast_msg_in_progress)

                    checklist.failed > 0 -> stringResource(
                        Strings.string.devicestate_fast_format_failed,
                        checklist.failed,
                    )

                    else -> stringResource(Strings.string.devicestate_fast_msg_all_done)
                },
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
        val shown = if (onlyCurrent) {
            listOfNotNull(order.firstOrNull { it.status == ChecklistStatus.PENDING })
        } else {
            order
        }
        shown.forEach { task ->
            TaskRow(
                task = task,
                sequential = checklist.sequential,
                number = checklist.tasks.indexOf(task) + 1,
                vehicle = vehicle,
                onSkip = onSkip,
                onAnswer = onAnswer,
            )
        }
    }
}

@Composable
private fun TaskRow(
    task: ChecklistTask,
    sequential: Boolean,
    number: Int,
    vehicle: VehicleState?,
    onSkip: (String) -> Unit,
    onAnswer: (String, Int) -> Unit,
) {
    val pending = task.status == ChecklistStatus.PENDING
    // Строгий порядок: текущее задание выделено, остальные незакрытые — «Ожидает» (без кнопок).
    val current = pending && sequential && task.active
    val waiting = pending && sequential && !task.active
    val (background, border) = taskFrame(task.status, current)
    val shape = RoundedCornerShape(CmpRadius.listItem)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.listItem)
            .background(background, shape)
            .border(CmpBorderWidth.listItem, border, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12)
            .testTag(CHECKLIST_TASK_TAG_PREFIX + task.id),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        TaskBadge(task.status, number, current)
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            CurrentLabel(visible = current)
            CmpText(
                text = task.text,
                style = if (pending) CmpTextStyle.Headline2 else CmpTextStyle.Paragraph2,
                color = taskTextColor(waiting),
                modifier = Modifier.padding(top = if (current) 0.dp else CmpSpacing.space8),
            )
            if (waiting) WaitingLabel() else TaskDetails(task, vehicle, onAnswer)
            SkipButton(visible = pending && !task.manual && !waiting, onClick = { onSkip(task.id) })
        }
    }
}

@Composable
private fun CurrentLabel(visible: Boolean) {
    if (!visible) return
    CmpText(
        text = stringResource(Strings.string.devicestate_fast_label_current),
        style = CmpTextStyle.Additional0,
        color = CmpTheme.colors.brandPrimaryDefault,
        uppercase = true,
    )
}

@Composable
private fun SkipButton(visible: Boolean, onClick: () -> Unit) {
    if (!visible) return
    CmpButton(
        text = stringResource(Strings.string.devicestate_fast_btn_skip),
        onClick = onClick,
        type = CmpButtonType.Tertiary,
        size = CmpButtonSize.S,
    )
}

/** Рамка строки: текущее задание — как выбранная строка списка, пройдено/не работает — статусные ступени. */
@Composable
private fun taskFrame(status: ChecklistStatus, current: Boolean): Pair<Color, Color> {
    val colors = CmpTheme.colors
    return when {
        status == ChecklistStatus.PASSED ->
            colors.successHighlight to
                colors.successDefault.copy(alpha = TONE_BORDER_ALPHA)

        status == ChecklistStatus.FAILED -> colors.errorHighlight to colors.errorDefault.copy(alpha = TONE_BORDER_ALPHA)

        current -> colors.brandPrimaryHighlight to colors.brandPrimaryDefault

        else -> colors.backgroundSurfaceUltraHighlight to colors.borderHighlight
    }
}

@Composable
private fun taskTextColor(waiting: Boolean) =
    if (waiting) CmpTheme.colors.foregroundOnDarkSecondaryDefault else CmpTheme.colors.foregroundOnDarkPrimaryDefault

@Composable
private fun WaitingLabel() {
    val colors = CmpTheme.colors
    CmpRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpIcon(
            icon = CmpIcons.Schedule,
            contentDescription = null,
            tint = colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.size(CmpSize.iconM),
        )
        CmpText(
            text = stringResource(Strings.string.devicestate_fast_msg_waiting),
            style = CmpTextStyle.Additional0,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
private fun TaskDetails(task: ChecklistTask, vehicle: VehicleState?, onAnswer: (String, Int) -> Unit) {
    val colors = CmpTheme.colors
    when {
        task.status == ChecklistStatus.PENDING && !task.manual -> LiveValue(task, vehicle)

        task.status == ChecklistStatus.PENDING -> CmpRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            task.options.forEachIndexed { index, option ->
                CmpButton(
                    text = option,
                    onClick = { onAnswer(task.id, index) },
                    type = CmpButtonType.Secondary,
                    size = CmpButtonSize.S,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        else -> CmpText(
            text = stringResource(task.resultText),
            style = CmpTextStyle.Additional0,
            color = if (task.status == ChecklistStatus.FAILED) {
                colors.errorDefault
            } else {
                colors.foregroundOnDarkSecondaryDefault
            },
        )
    }
}

private val ChecklistTask.resultText: StringResource
    get() = when (status) {
        ChecklistStatus.PASSED -> if (manual) {
            Strings.string.devicestate_fast_status_yes
        } else {
            Strings.string.devicestate_fast_status_passed
        }

        ChecklistStatus.FAILED -> Strings.string.devicestate_fast_status_no

        else -> Strings.string.devicestate_fast_status_skipped
    }

@Composable
private fun TaskBadge(status: ChecklistStatus, number: Int, current: Boolean = false) {
    val colors = CmpTheme.colors
    val modifier = Modifier.size(BadgeSize)
    when (status) {
        ChecklistStatus.PASSED -> CmpBox(
            modifier.background(colors.successDefault, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Check,
                contentDescription = null,
                tint = colors.staticWhite,
                modifier = Modifier.size(CmpSize.iconM),
            )
        }

        ChecklistStatus.FAILED -> CmpBox(
            modifier.background(colors.errorDefault, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Close,
                contentDescription = null,
                tint = colors.staticWhite,
                modifier = Modifier.size(CmpSize.iconM),
            )
        }

        ChecklistStatus.SKIPPED -> CmpBox(
            modifier.border(BadgeRing, colors.borderLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Redo,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
        }

        ChecklistStatus.PENDING -> CmpBox(
            modifier.border(BadgeRing, if (current) colors.brandPrimaryDefault else colors.borderLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpText(
                text = number.toString(),
                style = CmpTextStyle.Headline3,
                color = if (current) colors.brandPrimaryDefault else colors.foregroundOnDarkPrimaryDefault,
            )
        }
    }
}

@Composable
private fun LiveValue(task: ChecklistTask, vehicle: VehicleState?) {
    val colors = CmpTheme.colors
    val zone = task.zone
    val live: Pair<String, ZoneTone>? = when {
        vehicle == null -> null

        task.guard -> stringResource(vehicle.security.title) to
            (if (vehicle.security == SecurityState.DISARMED) ZoneTone.REST else ZoneTone.ACTIVE)

        zone != null -> zoneValueText(zone, vehicle.zone(zone)) to vehicle.zone(zone).tone

        else -> null
    }
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(Strings.string.devicestate_fast_label_now),
            style = CmpTextStyle.Paragraph3,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
        if (live != null) ValuePill(text = live.first, tone = live.second)
        CmpText(
            text = stringResource(Strings.string.devicestate_fast_msg_auto),
            style = CmpTextStyle.Paragraph3,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
}
