package ru.alarmtrade.specmp.feature.device.state.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.commands.ArmCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.ControlCommandResponse
import ru.alarmtrade.devices.domain.alarmDevice.commands.StartEngineCommand
import ru.alarmtrade.devices.domain.alarmDevice.state.DeviceState
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState

class SpecialistDeviceStateRepositoryTest {
    private val dataSource = FakeDeviceStateDataSource()
    private val repository = SpecialistDeviceStateRepository(dataSource)

    @Test
    fun `states start polling, skip empty snapshots and stop polling at the end`() = runTest {
        val failure = IllegalStateException("timeout")
        dataSource.states = flowOf(
            SpecialistResponse.Success(DeviceState.Empty),
            SpecialistResponse.Success(DeviceState.getDefault(77).copy(isValid = true)),
            SpecialistResponse.Error.General(failure),
        )

        val values = repository.states().toList()

        assertEquals(2, values.size)
        assertIs<VehicleState>(assertIs<Response.Success<VehicleState>>(values[0]).value)
        assertSame(failure, assertIs<Response.Error>(values[1]).exception)
        assertEquals(listOf("start", "stop"), dataSource.calls)
    }

    @Test
    fun `cancelled collection stops polling`() = runTest {
        dataSource.states = flowOf(
            SpecialistResponse.Success(DeviceState.getDefault(77).copy(isValid = true)),
            SpecialistResponse.Success(DeviceState.getDefault(77).copy(isValid = true)),
        )

        repository.states().first()

        assertEquals(listOf("start", "stop"), dataSource.calls)
    }

    @Test
    fun `failed start does not poll`() = runTest {
        val failure = IllegalStateException("no link")
        dataSource.start = SpecialistResponse.Error.General(failure)

        assertSame(failure, assertIs<Response.Error>(repository.states().toList().single()).exception)
        assertEquals(listOf("start"), dataSource.calls)

        dataSource.start = null
        assertIs<Response.Error>(repository.states().toList().single())
    }

    @Test
    fun `broken telemetry ends with an error and stops polling`() = runTest {
        val failure = IllegalStateException("parse")
        dataSource.states = flow { throw failure }

        assertSame(failure, assertIs<Response.Error>(repository.states().toList().single()).exception)
        assertEquals(listOf("start", "stop"), dataSource.calls)
    }

    @Test
    fun `command reply maps to done or rejected`() = runTest {
        dataSource.reply = SpecialistResponse.Success(reply(isSuccess = true))
        assertEquals(Response.Success(CommandReply.DONE), repository.send(StateCommand.ARM))

        dataSource.reply = SpecialistResponse.Success(reply(isSuccess = false))
        assertEquals(Response.Success(CommandReply.REJECTED), repository.send(StateCommand.EXTEND_AUTOSTART))

        assertIs<ArmCommand>(dataSource.commands[0].command)
        assertIs<StartEngineCommand>(dataSource.commands[1].command)
    }

    @Test
    fun `program mode is not a control command and unavailable interactor is an error`() = runTest {
        assertIs<Response.Error>(repository.send(StateCommand.PROGRAM_MODE_ON))
        assertEquals(emptyList(), dataSource.commands)

        assertIs<Response.Error>(repository.send(StateCommand.ARM))
    }

    private fun reply(isSuccess: Boolean) = ControlCommandResponse(
        replyId = 1,
        deviceCommand = ArmCommand(),
        isSuccess = isSuccess,
        code = 0,
    )
}
