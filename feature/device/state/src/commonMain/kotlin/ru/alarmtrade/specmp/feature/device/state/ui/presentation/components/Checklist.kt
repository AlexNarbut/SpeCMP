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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateChecklist
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistStatus
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistTask

internal const val CHECKLIST_TASK_TAG_PREFIX = "devicestate_task_"
private const val PASSED_ALPHA = 0.14f
private const val ALL_DONE_ALPHA = 0.2f

@Composable
internal fun ChecklistPanel(
    checklist: CheckStateChecklist,
    vehicle: VehicleState?,
    onSkip: (String) -> Unit,
    onAnswer: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    onlyCurrent: Boolean = false,
) {
    val colors = cmpThemeColors()
    val success = cmpDomainColors().success
    val total = checklist.tasks.size
    val allDone = checklist.allClosed
    val order = if (checklist.sequential) {
        checklist.tasks
    } else {
        checklist.tasks.sortedBy { if (it.status == ChecklistStatus.PENDING) 0 else 1 }
    }
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CmpColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (allDone) success.copy(alpha = ALL_DONE_ALPHA) else colors.surfaceContainerHigh,
                    RoundedCornerShape(16.dp),
                )
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (allDone) {
                    CmpIcon(
                        icon = CmpIcons.TaskAlt,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
                CmpText(
                    text = if (allDone) {
                        stringResource(Strings.string.devicestate_fast_title_all_done)
                    } else {
                        stringResource(Strings.string.devicestate_fast_format_progress, checklist.closed, total)
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            CmpLinearProgressIndicator(
                progress = if (total == 0) 1f else checklist.closed.toFloat() / total,
                modifier = Modifier.fillMaxWidth().height(8.dp),
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
                fontSize = 14.sp,
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
    val colors = cmpThemeColors()
    val pending = task.status == ChecklistStatus.PENDING
    val failed = task.status == ChecklistStatus.FAILED
    // Строгий порядок: текущее задание выделено, остальные незакрытые — «Ожидает» (без кнопок).
    val current = pending && sequential && task.active
    val waiting = pending && sequential && !task.active
    val background = taskBackground(task.status, current)
    val shape = RoundedCornerShape(16.dp)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape)
            .then(if (current) Modifier.border(2.dp, colors.primary, shape) else Modifier)
            .padding(horizontal = 10.dp, vertical = 12.dp)
            .testTag(CHECKLIST_TASK_TAG_PREFIX + task.id),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TaskBadge(task.status, number, current)
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CurrentLabel(visible = current)
            CmpText(
                text = task.text,
                fontSize = 16.sp,
                fontWeight = if (pending) FontWeight.SemiBold else FontWeight.Medium,
                color = taskTextColor(failed, waiting),
                modifier = Modifier.padding(top = if (current) 0.dp else 6.dp),
            )
            if (waiting) WaitingLabel() else TaskDetails(task, vehicle, onAnswer)
        }
        SkipButton(visible = pending && !task.manual && !waiting, onClick = { onSkip(task.id) })
    }
}

@Composable
private fun CurrentLabel(visible: Boolean) {
    if (!visible) return
    CmpText(
        text = stringResource(Strings.string.devicestate_fast_label_current),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = cmpThemeColors().primary,
    )
}

@Composable
private fun SkipButton(visible: Boolean, onClick: () -> Unit) {
    if (!visible) return
    CmpTextButton(text = stringResource(Strings.string.devicestate_fast_btn_skip), onClick = onClick)
}

@Composable
private fun taskBackground(status: ChecklistStatus, current: Boolean): Color = when (status) {
    ChecklistStatus.PASSED -> cmpDomainColors().success.copy(alpha = PASSED_ALPHA)
    ChecklistStatus.FAILED -> cmpThemeColors().errorContainer
    else -> if (current) cmpThemeColors().secondaryContainer else Color.Transparent
}

@Composable
private fun taskTextColor(failed: Boolean, waiting: Boolean) = when {
    failed -> cmpThemeColors().onErrorContainer
    waiting -> cmpThemeColors().onSurfaceVariant
    else -> cmpThemeColors().onSurface
}

@Composable
private fun WaitingLabel() {
    val colors = cmpThemeColors()
    CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CmpIcon(
            icon = CmpIcons.Schedule,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        CmpText(
            text = stringResource(Strings.string.devicestate_fast_msg_waiting),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun TaskDetails(task: ChecklistTask, vehicle: VehicleState?, onAnswer: (String, Int) -> Unit) {
    val colors = cmpThemeColors()
    when {
        task.status == ChecklistStatus.PENDING && !task.manual -> LiveValue(task, vehicle)

        task.status == ChecklistStatus.PENDING -> CmpRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            task.options.forEachIndexed { index, option ->
                CmpOutlinedButton(
                    text = option,
                    onClick = { onAnswer(task.id, index) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                )
            }
        }

        else -> CmpText(
            text = stringResource(task.resultText),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (task.status == ChecklistStatus.FAILED) colors.onErrorContainer else colors.onSurfaceVariant,
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
    val colors = cmpThemeColors()
    val success = cmpDomainColors().success
    val modifier = Modifier.size(36.dp)
    when (status) {
        ChecklistStatus.PASSED -> CmpBox(
            modifier.background(success, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Check,
                contentDescription = null,
                tint = colors.surface,
                modifier = Modifier.size(22.dp),
            )
        }

        ChecklistStatus.FAILED -> CmpBox(
            modifier.background(colors.error, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Close,
                contentDescription = null,
                tint = colors.onError,
                modifier = Modifier.size(22.dp),
            )
        }

        ChecklistStatus.SKIPPED -> CmpBox(
            modifier.border(2.dp, colors.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Redo,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        ChecklistStatus.PENDING -> CmpBox(
            modifier.border(2.dp, if (current) colors.primary else colors.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpText(
                text = number.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (current) colors.primary else colors.onSurface,
            )
        }
    }
}

@Composable
private fun LiveValue(task: ChecklistTask, vehicle: VehicleState?) {
    val colors = cmpThemeColors()
    val zone = task.zone
    val live: Pair<String, ZoneTone>? = when {
        vehicle == null -> null

        task.guard -> stringResource(vehicle.security.title) to
            (if (vehicle.security == SecurityState.DISARMED) ZoneTone.REST else ZoneTone.ACTIVE)

        zone != null -> zoneValueText(zone, vehicle.zone(zone)) to vehicle.zone(zone).tone

        else -> null
    }
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(Strings.string.devicestate_fast_label_now),
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
        )
        if (live != null) ValuePill(text = live.first, tone = live.second)
        CmpText(
            text = stringResource(Strings.string.devicestate_fast_msg_auto),
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
        )
    }
}
