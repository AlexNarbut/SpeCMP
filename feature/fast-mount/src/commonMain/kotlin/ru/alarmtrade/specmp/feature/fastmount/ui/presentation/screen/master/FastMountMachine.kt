package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.fastmount.domain.model.AutoTaskKind
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
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
