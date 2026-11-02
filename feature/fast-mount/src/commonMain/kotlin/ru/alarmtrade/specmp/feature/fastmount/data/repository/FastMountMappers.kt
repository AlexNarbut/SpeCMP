package ru.alarmtrade.specmp.feature.fastmount.data.repository

import ru.alarmtrade.devices.domain.alarmDevice.settings.AlarmDeviceSettings
import ru.alarmtrade.devices.domain.alarmDevice.settings.isKeyBypassEnabled
import ru.alarmtrade.devices.domain.alarmDevice.settings.isSlaveEnabled
import ru.alarmtrade.devices.domain.errors.CannotExecuteByCurrentConnectionType
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.domain.configuration.CarConfiguration
import ru.alarmtrade.specialist.domain.configuration.ConfigurationQuestion
import ru.alarmtrade.specialist.domain.configuration.ConfigurationQuestionAnswer
import ru.alarmtrade.specialist.domain.errors.BypassLearningWasNotFinishedException
import ru.alarmtrade.specialist.domain.errors.CloneWasNotFinishedException
import ru.alarmtrade.specialist.domain.errors.FastMountSessionNotStartedException
import ru.alarmtrade.specialist.domain.errors.MountWasNotRatedException
import ru.alarmtrade.specialist.domain.errors.NoFeedbackException
import ru.alarmtrade.specialist.domain.errors.QuestionsWasNotAnsweredException
import ru.alarmtrade.specialist.domain.fastMount.StateTitle
import ru.alarmtrade.specialist.domain.fastMount.questions.FastMountQuestionTestState
import ru.alarmtrade.specialist.domain.fastMount.questions.FastMountQuestionTestStatus
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountFailure
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountFile
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountImage
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountQuestion
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState

/**
 * Шаг мастера по состоянию SDK; `null` — служебное состояние для серверного лога (`*_FINISHED`), в UI не
 * приходит (SDK сразу переходит дальше).
 */
internal fun StateTitle.toStep(): FastMountStep? = when (this) {
    StateTitle.INITIAL_STATE -> FastMountStep.WRITING_SETTINGS

    StateTitle.MOUNT_START -> FastMountStep.MOUNT

    StateTitle.WAITING, StateTitle.WAITING_REBOOT -> FastMountStep.REBOOTING

    StateTitle.KEY_BYPASS_LEARNING_STARTED -> FastMountStep.KEY_BYPASS

    StateTitle.CLONE_STARTED -> FastMountStep.CLONE

    StateTitle.SLAVE_LEARNING_STARTED -> FastMountStep.SLAVE

    StateTitle.SYSTEM_CHECK_STARTED -> FastMountStep.SYSTEM_CHECK

    StateTitle.FEEDBACK -> FastMountStep.FEEDBACK

    StateTitle.FINISH_STATE -> FastMountStep.FINISH

    StateTitle.MOUNT_FINISHED,
    StateTitle.KEY_BYPASS_LEARNING_FINISHED,
    StateTitle.CLONE_FINISHED,
    StateTitle.SLAVE_LEARNING_FINISHED,
    StateTitle.SYSTEM_CHECK_FINISHED -> null
}

/** Предметная ошибка SDK быстрого монтажа; прочие (сеть, связь, неизвестные) — `null`, остаются как есть. */
internal fun Throwable.toFastMountFailure(): FastMountFailure? = when (this) {
    is FastMountSessionNotStartedException -> FastMountFailure.SessionLost

    is BypassLearningWasNotFinishedException -> FastMountFailure.BypassNotFinished

    is CloneWasNotFinishedException -> FastMountFailure.CloneNotFinished

    is QuestionsWasNotAnsweredException -> FastMountFailure.ChecksNotDone

    is NoFeedbackException, is MountWasNotRatedException -> FastMountFailure.FeedbackRequired

    // USB-маршрут SDK бросает NotImplementedError на Slave / проверке системы.
    is CannotExecuteByCurrentConnectionType, is NotImplementedError -> FastMountFailure.TransportNotSupported

    else -> null
}

/** Класс блока по типам подсистем модели (основная подсистема). */
internal fun List<SubsystemType>.toDeviceClass(): MountDeviceClass = when {
    SubsystemType.ALARM_MAIN in this -> MountDeviceClass.ALARM
    SubsystemType.MICROBOT_MAIN in this -> MountDeviceClass.MICROBOT
    else -> MountDeviceClass.OTHER
}

internal fun CarConfiguration.toDomain(): FastMountConfiguration {
    val settings = AlarmDeviceSettings(info.settings)
    val map = info.mountingManualFile
    return FastMountConfiguration(
        id = info.id,
        name = info.name,
        shortDescription = info.shortDescription,
        fullDescription = info.fullDescription,
        deviceName = info.alarmDeviceName,
        author = info.author.fullName.ifBlank { info.author.login },
        rating = info.rating,
        mountingMap = map.url.takeIf { it.isNotBlank() }?.let { MountFile(name = map.name, url = it) },
        images = images.map { image ->
            MountImage(
                title = image.shortDescription,
                description = image.fullDescription,
                previewUrl = image.previewUrl.takeIf { it.isNotBlank() },
                url = image.url
            )
        },
        keyBypass = settings.hasByte { isKeyBypassEnabled() },
        slave = settings.hasByte { isSlaveEnabled() },
        questionCount = questions.sumOf { it.questions.size }
    )
}

/** Флаг из байтов настроек; короткий массив (битая конфигурация) — `false`, как будто опции нет. */
private inline fun AlarmDeviceSettings.hasByte(flag: AlarmDeviceSettings.() -> Boolean): Boolean = try {
    flag()
} catch (ignored: IndexOutOfBoundsException) {
    false
}

internal fun ConfigurationQuestion.toDomain(): MountQuestion = MountQuestion(
    id = id,
    group = listOf(groupName, subGroupName).filter { it.isNotBlank() }.joinToString(" · "),
    task = algorithmDescription,
    expected = resultDescription,
    automatic = isAutomatic,
    answers = answers.map { it.toDomain() }
)

internal fun ConfigurationQuestionAnswer.toDomain(): QuestionAnswer = QuestionAnswer(text = answer, value = value)

internal fun QuestionAnswer.toSdk(): ConfigurationQuestionAnswer =
    ConfigurationQuestionAnswer(answer = text, value = value)

/** Пустой тест SDK отдаёт `COMPLETED` с `ConfigurationQuestion.Empty` — у нас это `current = null`. */
internal fun FastMountQuestionTestState.toDomain(): QuestionsState {
    val question = currentQuestion.takeIf { it != ConfigurationQuestion.Empty }?.toDomain()
    return QuestionsState(current = question, completed = status == FastMountQuestionTestStatus.COMPLETED)
}
