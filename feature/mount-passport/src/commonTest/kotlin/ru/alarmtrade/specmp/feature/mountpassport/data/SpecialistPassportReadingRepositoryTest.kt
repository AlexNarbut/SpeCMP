package ru.alarmtrade.specmp.feature.mountpassport.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.MountPassportStageNotReadException
import ru.alarmtrade.specialist.domain.errors.MountPassportUnitMismatchException
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadCommand
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadPhase
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadSource
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadState
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadStep
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStepState
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStepStatus
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStepSummary
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportUnavailableReason
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportUnitLookup
import ru.alarmtrade.specialist.domain.mountPassport.PassportReadStage as SdkReadStage
import ru.alarmtrade.specialist.domain.mountPassport.PassportTransport as SdkTransport
import ru.alarmtrade.specialist.domain.mountPassport.PassportUnit as SdkUnit
import ru.alarmtrade.specialist.domain.mountPassport.PassportWorksSource
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistPassportReadingRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportFastMountResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadCommand
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadPhase
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStep
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStageNotReadException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnavailableReason
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitLookup
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitMismatchException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle

class SpecialistPassportReadingRepositoryTest {

    private val source = FakePassportReadingDataSource()
    private val repository = SpecialistPassportReadingRepository(source)

    private fun sdkState(phase: MountPassportReadPhase = MountPassportReadPhase.Reading) = MountPassportReadState(
        steps = listOf(
            MountPassportStepState(
                MountPassportReadStep.BLOCK,
                MountPassportStepStatus.Done(MountPassportStepSummary.Block("Pandora DX-91", "1234567", "3.57")),
            ),
            MountPassportStepState(
                MountPassportReadStep.PARAMS,
                MountPassportStepStatus.NotRead(connectionLost = true),
            ),
            MountPassportStepState(
                MountPassportReadStep.DEVICES,
                MountPassportStepStatus.Done(MountPassportStepSummary.Devices(5, SdkTransport.USB)),
            ),
            MountPassportStepState(
                MountPassportReadStep.SETTINGS,
                MountPassportStepStatus.Unavailable(MountPassportUnavailableReason.FACTORY_CAN),
            ),
            MountPassportStepState(MountPassportReadStep.READY, MountPassportStepStatus.Reading),
        ),
        phase = phase,
        transport = SdkTransport.BLUETOOTH,
        unit = SdkUnit(systemName = "Pandora DX-91", systemId = "1234567"),
    )

    @Test
    fun readStateIsMapped() = runTest {
        source.states.emit(
            SpecialistResponse.Success(sdkState(MountPassportReadPhase.ConnectionLost(MountPassportReadStep.DEVICES))),
        )

        val state = assertIs<Response.Success<PassportReadState>>(
            repository.read(PassportReadSource.AdvancedMount, PassportInstaller()).first(),
        ).value

        assertEquals(
            PassportStepStatus.Done(PassportStepSummary.Block("Pandora DX-91", "1234567", "3.57")),
            state.status(PassportReadStep.BLOCK),
        )
        assertEquals(PassportStepStatus.NotRead(connectionLost = true), state.status(PassportReadStep.PARAMS))
        assertEquals(
            PassportStepStatus.Done(PassportStepSummary.Devices(5, PassportTransport.USB)),
            state.status(PassportReadStep.DEVICES),
        )
        assertEquals(
            PassportStepStatus.Unavailable(PassportUnavailableReason.FACTORY_CAN),
            state.status(PassportReadStep.SETTINGS),
        )
        assertEquals(PassportStepStatus.Reading, state.status(PassportReadStep.READY))
        assertEquals(PassportReadPhase.ConnectionLost(PassportReadStep.DEVICES), state.phase)
        assertEquals(PassportTransport.BLUETOOTH, state.transport)
        assertEquals("Pandora DX-91", state.unitName)
    }

    @Test
    fun donePhaseCarriesPassportId() = runTest {
        source.states.emit(SpecialistResponse.Success(sdkState(MountPassportReadPhase.Done(sdkPassport()))))

        val state = assertIs<Response.Success<PassportReadState>>(
            repository.read(PassportReadSource.AdvancedMount, PassportInstaller()).first(),
        ).value

        assertEquals(PassportReadPhase.Done("p1"), state.phase)
    }

    @Test
    fun readRequestCarriesSourceAndInstaller() = runTest {
        source.states.emit(SpecialistResponse.Success(sdkState()))

        repository.read(
            PassportReadSource.Complete("p7"),
            PassportInstaller(name = "Алексей", contact = "+7912"),
        ).first()

        val request = requireNotNull(source.readRequest)
        assertEquals(MountPassportReadSource.Complete("p7"), request.source)
        assertEquals("Алексей", request.installer.name)
        assertEquals("+7912", request.installer.contact)
    }

    @Test
    fun fastMountResultIsSentAsMasterData() = runTest {
        source.states.emit(SpecialistResponse.Success(sdkState()))
        val result = PassportFastMountResult(
            vehicle = PassportVehicle(brand = "Kia", model = "Sportage"),
            durationMinutes = 85,
            unit = PassportUnit("Pandora DX-91", "1234567", "3.57", null, null, null, null, manual = false),
        )

        repository.read(PassportReadSource.FastMount(result), PassportInstaller()).first()

        val sdk = assertIs<MountPassportReadSource.FastMount>(requireNotNull(source.readRequest).source).result
        assertEquals("Kia", sdk.vehicle.brand)
        assertEquals(PassportWorksSource.MASTER, sdk.works.source)
        assertEquals(85, sdk.durationMinutes)
        assertEquals("1234567", sdk.unit?.systemId)
    }

    @Test
    fun commandGoesToSdk() = runTest {
        assertIs<Response.Success<Unit>>(repository.send(PassportReadCommand.SKIP))

        assertEquals(MountPassportReadCommand.SKIP, source.command?.command)
    }

    @Test
    fun rereadSendsStageAndMapsPassport() = runTest {
        val passport = assertIs<Response.Success<MountPassport>>(repository.reread("p1", PassportReadStage.PERIPHERALS))

        assertEquals("p1", passport.value.id)
        assertEquals(SdkReadStage.PERIPHERALS, source.rereadRequest?.stage)
    }

    @Test
    fun rereadOtherUnitIsMismatch() = runTest {
        source.rereadResponse =
            SpecialistResponse.Error.General(MountPassportUnitMismatchException("1234567", "7654321"))

        val error = assertIs<Response.Error>(repository.reread("p1", PassportReadStage.SETTINGS))

        val mismatch = assertIs<PassportUnitMismatchException>(error.exception)
        assertEquals("1234567", mismatch.expectedSystemId)
        assertEquals("7654321", mismatch.actualSystemId)
    }

    @Test
    fun rereadNotReadIsStageError() = runTest {
        source.rereadResponse = SpecialistResponse.Error.General(MountPassportStageNotReadException("SETTINGS", true))

        val error = assertIs<Response.Error>(repository.reread("p1", PassportReadStage.SETTINGS))

        assertEquals(true, assertIs<PassportStageNotReadException>(error.exception).connectionLost)
    }

    @Test
    fun foundUnitIsMapped() = runTest {
        source.findResponse = SpecialistResponse.Success(MountPassportUnitLookup.Found("Pandora DX-91", "1234567"))

        val lookup = assertIs<Response.Success<PassportUnitLookup>>(repository.findUnit("1234567")).value

        assertEquals(PassportUnitLookup.Found("Pandora DX-91", "1234567"), lookup)
        assertEquals("1234567", source.findRequest?.systemId)
    }

    @Test
    fun notFoundLookupIsMapped() = runTest {
        source.findResponse = SpecialistResponse.Success(MountPassportUnitLookup.NotFound)

        assertEquals(
            PassportUnitLookup.NotFound,
            assertIs<Response.Success<PassportUnitLookup>>(repository.findUnit("999999999")).value,
        )
    }

    @Test
    fun offlineLookupIsMapped() = runTest {
        source.findResponse = SpecialistResponse.Success(MountPassportUnitLookup.Offline)

        assertEquals(
            PassportUnitLookup.Offline,
            assertIs<Response.Success<PassportUnitLookup>>(repository.findUnit("1")).value,
        )
    }

    @Test
    fun unitModelsWithoutInteractorIsError() = runTest {
        source.models = null

        assertIs<Response.Error>(repository.unitModels())
    }

    @Test
    fun unitModelsAreSortedWithoutBlanks() = runTest {
        source.models = SpecialistResponse.Success(listOf("Pandora DX-91", " ", "Pandora DX-4GS", "Pandora DX-91"))

        assertEquals(
            listOf("Pandora DX-4GS", "Pandora DX-91"),
            assertIs<Response.Success<List<String>>>(repository.unitModels()).value,
        )
    }

    @Test
    fun withoutUnitIsCreatedWithInstaller() = runTest {
        val created = repository.createWithoutUnit("Pandora DX-91", "1234567", PassportInstaller(name = "Алексей"))

        assertEquals("p1", assertIs<Response.Success<MountPassport>>(created).value.id)
        val request = requireNotNull(source.createRequest)
        assertEquals("Pandora DX-91", request.systemName)
        assertEquals("1234567", request.systemId)
        assertEquals("Алексей", request.installer.name)
    }
}
