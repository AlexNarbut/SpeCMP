package ru.alarmtrade.specmp.feature.device.peripherals.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.connection.DeviceType
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.DynamicPeripheralDevice
import ru.alarmtrade.devices.domain.errors.IncorrectServicePinException
import ru.alarmtrade.devices.domain.errors.SetProgramModeException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralException
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType

class SpecialistDynamicPeripheralsRepositoryTest {
    private val dataSource = FakePeripheralsDataSource()
    private val repository = SpecialistDynamicPeripheralsRepository(dataSource)

    @Test
    fun `slots are numbered within a type by slot order`() = runTest {
        dataSource.dynamic = flowOf(
            SpecialistResponse.Success(
                listOf(
                    device(id = 3, DeviceType.TAG_760, paired = false),
                    device(id = 0, DeviceType.TAG_760, paired = true),
                    device(id = 1, DeviceType.RELAY_BTR_101, paired = true),
                    device(id = 2, DeviceType.PIR_SENSOR, paired = true, temperature = 5),
                ),
            ),
        )

        val slots = assertIs<Response.Success<List<DynamicPeripheral>>>(repository.slots().toList().single()).value

        assertEquals(listOf(0, 1, 2, 3), slots.map { it.slot })
        assertEquals(listOf(1, 1, 1, 2), slots.map { it.number })
        assertEquals(PeripheralType.RELAY, slots[1].type)
        assertEquals(5, slots[2].pirAddress)
        assertEquals(3.0f, slots[0].voltage)
    }

    @Test
    fun `wrong pin in the stream is a typed failure`() = runTest {
        dataSource.dynamic = flowOf(SpecialistResponse.Error.General(IncorrectServicePinException()))

        val error = assertIs<Response.Error>(repository.slots().toList().single())

        assertEquals(PeripheralFailure.WRONG_PIN, assertIs<PeripheralException>(error.exception).failure)
    }

    @Test
    fun `refused program mode on a command is a typed failure`() = runTest {
        dataSource.command = SpecialistResponse.Error.General(SetProgramModeException())

        val error = assertIs<Response.Error>(repository.delete(4))

        assertEquals(PeripheralFailure.PROGRAM_MODE_REFUSED, assertIs<PeripheralException>(error.exception).failure)
        assertEquals(listOf("delete 4"), dataSource.calls)
    }

    @Test
    fun `pair passes slot and address`() = runTest {
        assertIs<Response.Success<Unit>>(repository.pair(2, "A1B2C3D4"))
        repository.update(5)

        assertEquals(listOf("pair 2 A1B2C3D4", "update 5"), dataSource.calls)
    }

    private fun device(id: Int, type: DeviceType, paired: Boolean, temperature: Short = 0) = DynamicPeripheralDevice(
        id = id,
        type = type,
        name = "",
        isPaired = paired,
        isConnected = paired,
        voltage = 3.0f,
        temperature = temperature,
    )
}
