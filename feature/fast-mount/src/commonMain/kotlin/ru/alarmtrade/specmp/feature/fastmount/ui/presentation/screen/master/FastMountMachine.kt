package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.fastmount.domain.model.AutoTaskKind
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountBypass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountCheck
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountCheckResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResultConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountUnit
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountWorks
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTask
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState

internal fun AlarmDevice.toUnitInfo(): UnitInfo = UnitInfo(
    firmware = displayFirmwareVersion,
    canCode = can?.code,
    canVersion = can?.firmwareVersion,
    canFactory = can?.isFactory == true,
)

internal fun String.withPdfExtension(): String = if (endsWith(".pdf", ignoreCase = true)) this else "$this.pdf"

internal fun MasterState.withStep(step: FastMountStep): MasterState = copy(
    step = step,
    error = null,
    toast = if (this.step == FastMountStep.WRITING_SETTINGS && step != FastMountStep.WRITING_SETTINGS) {
        WizardToast.SETTINGS_WRITTEN
    } else {
        toast
    },
    cloneNeeded = if (step == FastMountStep.CLONE) true else cloneNeeded,
    mountMinutes = startedAt?.elapsedNow()?.inWholeMinutes?.toInt() ?: 0,
)

/** Курсор SDK сдвинулся: всё, что до него и не закрыто нами, ответил SDK по телеметрии — «пройдено». */
internal fun FastMountState.withQuestions(questions: QuestionsState): FastMountState {
    val all = configuration?.questions.orEmpty()
    val currentIndex = if (questions.completed) {
        all.size
    } else {
        all.indexOfFirst { it.id == questions.current?.id }.takeIf { it >= 0 } ?: all.size
    }
    val answeredBySdk = all.take(currentIndex)
        .filter { master.statuses[it.id] == null }
        .associate { it.id to MountTaskStatus.PASSED }
    return copy(
        master = master.copy(statuses = master.statuses + answeredBySdk, currentTask = all.getOrNull(currentIndex)?.id),
    ).withChecklist()
}

/** Статус задания проверки; `null` — снять (ответ не ушёл в SDK). */
internal fun FastMountState.withTaskStatus(taskId: Int, status: MountTaskStatus?): FastMountState {
    val statuses = if (status == null) master.statuses - taskId else master.statuses + (taskId to status)
    return copy(master = master.copy(statuses = statuses)).withChecklist()
}

internal fun FastMountState.withChecklist(): FastMountState {
    val tasks = configuration?.questions.orEmpty().map { question ->
        MountTask(
            id = question.id,
            text = question.task.ifBlank { question.expected },
            status = master.statuses[question.id] ?: MountTaskStatus.PENDING,
            active = question.id == master.currentTask && master.statuses[question.id] == null,
            auto = if (question.automatic) AutoTaskKind.of(question.id) else null,
            options = if (question.automatic) emptyList() else question.answers.map { it.text },
        )
    }
    return copy(master = master.copy(checklist = MountChecklist(tasks)))
}

/** Итог мастера на финале; до финала — `null`. Clone был — значит, обход его потребовал. */
internal fun FastMountState.fastMountResult(): FastMountResult? {
    if (master.step != FastMountStep.FINISH) return null
    val fixed = fixed ?: return null
    val planned = plan?.steps.orEmpty().map { it.step }
    val bypass = when {
        FastMountStep.KEY_BYPASS !in planned -> FastMountBypass.NONE
        master.cloneNeeded == true -> FastMountBypass.CLONE_NEEDED
        else -> FastMountBypass.TRAINED
    }
    return FastMountResult(
        unit = FastMountUnit(
            systemName = fixed.systemName,
            systemId = fixed.systemId,
            firmware = unit?.firmware.orEmpty(),
            canCode = unit?.canCode,
            canVersion = unit?.canVersion,
            canFactory = unit?.canFactory == true,
        ),
        car = car,
        configuration = configuration?.let { FastMountResultConfiguration(it.id, it.name, it.author) },
        works = FastMountWorks(
            settingsWritten = true,
            bypass = bypass,
            clone = bypass == FastMountBypass.CLONE_NEEDED,
            slave = FastMountStep.SLAVE in planned && master.slaveDone,
            durationMinutes = master.mountMinutes,
        ),
        systemCheck = if (FastMountStep.SYSTEM_CHECK in
            planned
        ) {
            master.checklist.tasks.map { it.toCheck() }
        } else {
            emptyList()
        },
        transport = fixed.transport,
    )
}

private fun MountTask.toCheck(): FastMountCheck = FastMountCheck(
    title = text,
    result = when (status) {
        MountTaskStatus.PASSED -> FastMountCheckResult.PASSED
        MountTaskStatus.FAILED -> FastMountCheckResult.FAILED
        MountTaskStatus.PENDING, MountTaskStatus.SKIPPED -> FastMountCheckResult.SKIPPED
    },
)
