package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningError
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningStatus
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneCalculatingError
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureStatus
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneStatus
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage

class AutostartMappersTest {

    private fun bypass(
        status: BypassLearningStatus,
        error: BypassLearningError = BypassLearningError.NO_ERROR,
        code: Int = 0,
    ) = BypassLearningState(status, error, code).toEvent()

    private fun clone(
        status: CloneProcedureStatus,
        result: CloneStatus = CloneStatus.REQUIRED,
        error: CloneCalculatingError = CloneCalculatingError.NO_ERROR,
        tips: List<String> = emptyList(),
        written: Boolean = false,
    ) = CloneProcedureState(status, result, error, tips).toEvent(written)

    @Test
    fun everyBypassStatusMapsToStepOrResult() {
        BypassLearningStatus.entries.forEach { status ->
            val event = bypass(status)
            val finished = status.name.startsWith("FINISH_")
            assertEquals(finished, event is BypassEvent.Finished, "status $status")
        }
    }

    @Test
    fun requestAndSuccessOfActionDiffer() {
        val request = assertIs<BypassEvent.Step>(bypass(BypassLearningStatus.OPEN_HOOD_REQUEST)).step
        val success = assertIs<BypassEvent.Step>(bypass(BypassLearningStatus.OPEN_HOOD_SUCCESS)).step
        assertEquals(BypassStepKind.ACTION, request.kind)
        assertEquals(BypassStepKind.ACCEPTED, success.kind)
        assertEquals(BypassSubject.HOOD_OPEN, success.subject)
    }

    @Test
    fun dataExchangeThroughAppIsCritical() {
        listOf(
            BypassLearningStatus.LADA_CAN_IMMO_BYPASS,
            BypassLearningStatus.REQUEST_LADA_VESTA_CAN_PATCH,
            BypassLearningStatus.REQUEST_MED_17_5_25,
            BypassLearningStatus.REQUEST_MED_17_9_63,
            BypassLearningStatus.GTP_AUTHORIZATION_ALGORITHM,
        ).forEach { status ->
            assertEquals(
                BypassStepKind.CRITICAL,
                assertIs<BypassEvent.Step>(bypass(status)).step.kind,
                "status $status",
            )
        }
        val ecu = assertIs<BypassEvent.Step>(bypass(BypassLearningStatus.EXCHANGE_DATA_WITH_ECU_STARTED)).step
        assertEquals(BypassStepKind.WAIT, ecu.kind)
    }

    @Test
    fun instructionsAreInstructionSteps() {
        listOf(BypassLearningStatus.LADA_APS_IMMO_BYPASS, BypassLearningStatus.LEARN_TOYOTA_DAIHATSU).forEach {
            assertEquals(BypassStepKind.INSTRUCTION, assertIs<BypassEvent.Step>(bypass(it)).step.kind)
        }
    }

    @Test
    fun errorCodeKeptOnlyForVagEcuExchange() {
        val vag = bypass(BypassLearningStatus.FINISH_ERROR, BypassLearningError.VAG_BYPASS_ERROR, code = 7)
        assertEquals(BypassResult.Error(BypassError.VAG_ECU_EXCHANGE, 7), assertIs<BypassEvent.Finished>(vag).result)
        val kia = bypass(BypassLearningStatus.FINISH_ERROR, BypassLearningError.KIA_IMMOBILIZER_IS_OFF, code = 7)
        assertNull(assertIs<BypassResult.Error>(assertIs<BypassEvent.Finished>(kia).result).code)
    }

    @Test
    fun errorsWithoutTextAreGeneral() {
        listOf(
            BypassLearningError.DEFAULT,
            BypassLearningError.NO_ERROR,
            BypassLearningError.MED_TABLE_SEND,
            BypassLearningError.GTP_CALCULATE_NOT_VALID,
            BypassLearningError.GTP_SENDING_DATA,
            null,
        ).forEach { assertEquals(BypassError.GENERAL, it.toDomain(), "error $it") }
        assertEquals(BypassError.OPTION_NOT_SELECTED, BypassLearningError.BYPASS_OPTION_IS_NOT_SELECTED.toDomain())
    }

    @Test
    fun ladaVestaCanPatchHasOwnStepAndError() {
        val step = assertIs<BypassEvent.Step>(bypass(BypassLearningStatus.REQUEST_LADA_VESTA_CAN_PATCH)).step
        assertEquals(BypassSubject.LADA_VESTA_CAN, step.subject)
        val error = bypass(BypassLearningStatus.FINISH_ERROR, BypassLearningError.LADA_VESTA_CAN_PATCH_SEND)
        assertEquals(
            BypassResult.Error(BypassError.LADA_VESTA_CAN_PATCH_SEND),
            assertIs<BypassEvent.Finished>(error).result,
        )
    }

    @Test
    fun cloneStagesFollowProcedure() {
        assertEquals(
            CloneEvent.Stage(CloneStage.CALCULATING, inQueue = true),
            clone(CloneProcedureStatus.CALCULATING_NEW),
        )
        assertEquals(CloneEvent.Stage(CloneStage.CALCULATING), clone(CloneProcedureStatus.CALCULATING_WORK))
        val writing = clone(CloneProcedureStatus.CALCULATED)
        assertEquals(CloneEvent.Stage(CloneStage.WRITING), writing)
        assertTrue(writing.isWriting)
        assertFalse(clone(CloneProcedureStatus.READING_FROM_ALARM).isWriting)
    }

    @Test
    fun completedBeforeWritingMeansAlreadyDone() {
        val before = clone(CloneProcedureStatus.COMPLETED, CloneStatus.OK, written = false)
        assertEquals(CloneResult.Completed(already = true), assertIs<CloneEvent.Finished>(before).result)
        val after = clone(CloneProcedureStatus.COMPLETED, CloneStatus.COMPLETED, written = true)
        assertEquals(CloneResult.Completed(already = false), assertIs<CloneEvent.Finished>(after).result)
    }

    @Test
    fun calculatingErrorKeepsTipsAndRetryability() {
        val event = clone(
            CloneProcedureStatus.CALCULATING_ERROR,
            error = CloneCalculatingError.DEVICE_DENIED,
            tips = listOf("Проверьте CAN код", " "),
        )
        val failed = assertIs<CloneResult.Failed>(assertIs<CloneEvent.Finished>(event).result)
        assertEquals(CloneFailure.DEVICE_DENIED, failed.failure)
        assertEquals(listOf("Проверьте CAN код"), failed.tips)
        assertFalse(failed.failure.retryable)
        assertTrue(CloneFailure.CALC_ERROR.retryable)
    }

    @Test
    fun everyCloneStatusMaps() {
        CloneProcedureStatus.entries.forEach { clone(it) }
        CloneStatus.entries.forEach { clone(CloneProcedureStatus.COMPLETED, it) }
        CloneCalculatingError.entries.forEach { clone(CloneProcedureStatus.CALCULATING_ERROR, error = it) }
        assertEquals(
            CloneResult.NotSupported,
            assertIs<CloneEvent.Finished>(clone(CloneProcedureStatus.COMPLETED, CloneStatus.NOT_SUPPORTED)).result,
        )
    }
}
