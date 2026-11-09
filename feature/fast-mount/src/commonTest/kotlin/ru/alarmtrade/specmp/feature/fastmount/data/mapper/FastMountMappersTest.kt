package ru.alarmtrade.specmp.feature.fastmount.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ru.alarmtrade.devices.domain.appDevice.ConnectionType
import ru.alarmtrade.devices.domain.errors.CannotExecuteByCurrentConnectionType
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
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
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountFailure
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer

class FastMountMappersTest {

    @Test
    fun everyStateTitleIsMapped() {
        val shown = StateTitle.entries.associateWith { it.toStep() }
        assertEquals(FastMountStep.WRITING_SETTINGS, shown[StateTitle.INITIAL_STATE])
        assertEquals(FastMountStep.REBOOTING, shown[StateTitle.WAITING])
        assertEquals(FastMountStep.REBOOTING, shown[StateTitle.WAITING_REBOOT])
        assertEquals(FastMountStep.FINISH, shown[StateTitle.FINISH_STATE])
        // Служебные *_FINISHED в UI не попадают.
        assertEquals(
            setOf(
                StateTitle.MOUNT_FINISHED,
                StateTitle.KEY_BYPASS_LEARNING_FINISHED,
                StateTitle.CLONE_FINISHED,
                StateTitle.SLAVE_LEARNING_FINISHED,
                StateTitle.SYSTEM_CHECK_FINISHED,
            ),
            shown.filterValues { it == null }.keys,
        )
        // Каждый шаг мастера достижим из какого-то состояния SDK.
        assertEquals(FastMountStep.entries.toSet(), shown.values.filterNotNull().toSet())
    }

    @Test
    fun sdkErrorsMapToFailures() {
        assertEquals(FastMountFailure.SessionLost, FastMountSessionNotStartedException().toFastMountFailure())
        assertEquals(FastMountFailure.BypassNotFinished, BypassLearningWasNotFinishedException().toFastMountFailure())
        assertEquals(FastMountFailure.CloneNotFinished, CloneWasNotFinishedException().toFastMountFailure())
        assertEquals(FastMountFailure.ChecksNotDone, QuestionsWasNotAnsweredException().toFastMountFailure())
        assertEquals(FastMountFailure.FeedbackRequired, NoFeedbackException().toFastMountFailure())
        assertEquals(FastMountFailure.FeedbackRequired, MountWasNotRatedException().toFastMountFailure())
        assertEquals(FastMountFailure.TransportNotSupported, NotImplementedError().toFastMountFailure())
        assertEquals(
            FastMountFailure.TransportNotSupported,
            CannotExecuteByCurrentConnectionType("op", ConnectionType.USB).toFastMountFailure(),
        )
        assertNull(IllegalStateException().toFastMountFailure())
    }

    @Test
    fun deviceClassByMainSubsystem() {
        assertEquals(
            MountDeviceClass.ALARM,
            listOf(SubsystemType.BLUETOOTH_MODULE, SubsystemType.ALARM_MAIN).toDeviceClass(),
        )
        assertEquals(MountDeviceClass.MICROBOT, listOf(SubsystemType.MICROBOT_MAIN).toDeviceClass())
        assertEquals(MountDeviceClass.OTHER, listOf(SubsystemType.WATCH_MAIN).toDeviceClass())
        assertEquals(MountDeviceClass.OTHER, emptyList<SubsystemType>().toDeviceClass())
    }

    @Test
    fun questionStateAndAnswers() {
        val answers = listOf(ConfigurationQuestionAnswer("Да", 1), ConfigurationQuestionAnswer("Нет", 2))
        val question = ConfigurationQuestion(5, 1, "Двери", 0, "", "Откройте дверь", "Блок видит", true, answers)
        val state = FastMountQuestionTestState(FastMountQuestionTestStatus.IN_PROCESS, question).toDomain()
        val current = requireNotNull(state.current)
        assertFalse(state.completed)
        assertEquals("Двери", current.group)
        assertTrue(current.automatic)
        assertEquals(listOf(QuestionAnswer("Да", 1), QuestionAnswer("Нет", 2)), current.answers)
        // Обратный маппинг даёт равный объект SDK — SDK сверяет ответ через contains.
        assertEquals(answers[1], current.answers[1].toSdk())

        val empty = FastMountQuestionTestState(FastMountQuestionTestStatus.COMPLETED, ConfigurationQuestion.Empty)
            .toDomain()
        assertNull(empty.current)
        assertTrue(empty.completed)
    }
}
