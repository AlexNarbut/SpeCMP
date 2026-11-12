package ru.alarmtrade.specmp.feature.device.peripherals.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.connection.DeviceType
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.PeripheralDeviceInformation
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.PeripheralDeviceStatus
import ru.alarmtrade.devices.domain.errors.PeripheralInformationTimeoutException
import ru.alarmtrade.devices.domain.errors.PeripheralProgramModeException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralException
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralPollStep
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType

class SpecialistSystemPeripheralsRepositoryTest {
    private val dataSource = FakePeripheralsDataSource()
    private val repository = SpecialistSystemPeripheralsRepository(dataSource, Duration.ZERO, Duration.ZERO)

    @Test
    fun `poll steps through types in order and repeats the pass`() = runTest {
        dataSource.byType[DeviceType.TAG_760] = SpecialistResponse.Success(listOf(tag(id = 1, bound = true)))

        val steps = repository.poll(listOf(PeripheralType.TAG, PeripheralType.RELAY)).take(3).toList()
            .map { assertIs<Response.Success<PeripheralPollStep>>(it).value }

        assertEquals(listOf(PeripheralType.TAG, PeripheralType.RELAY, PeripheralType.TAG), steps.map { it.type })
        assertEquals(listOf(0, 1, 0), steps.map { it.index })
        val device = steps[0].devices.orEmpty().single()
        assertEquals(2, device.number)
        assertEquals("A1B2C3D4E5F6", device.address)
        assertEquals(PeripheralLink.NEAR, device.link)
        assertEquals(-67, device.signalDbm)
        assertEquals(emptyList(), steps[1].devices)
    }

    @Test
    fun `unbound positions are hidden and offline devices have no signal`() = runTest {
        dataSource.byType[DeviceType.TAG_760] = SpecialistResponse.Success(
            listOf(tag(id = 0, bound = false), tag(id = 1, bound = true, connected = false)),
        )

        val step =
            assertIs<Response.Success<PeripheralPollStep>>(
                repository.poll(listOf(PeripheralType.TAG)).take(1).toList().single(),
            )

        val device = step.value.devices.orEmpty().single()
        assertEquals(PeripheralLink.OFFLINE, device.link)
        assertNull(device.signalDbm)
    }

    @Test
    fun `type without answer gives a step without devices`() = runTest {
        dataSource.byType[DeviceType.TPMS] =
            SpecialistResponse.Error.General(PeripheralInformationTimeoutException(DeviceType.TPMS))

        val step =
            assertIs<Response.Success<PeripheralPollStep>>(
                repository.poll(listOf(PeripheralType.TPMS)).take(1).toList().single(),
            )

        assertNull(step.value.devices)
    }

    @Test
    fun `nav group reads both gps and nav-09 types`() = runTest {
        repository.poll(listOf(PeripheralType.NAV)).take(1).toList()

        assertEquals(listOf(DeviceType.GPS_NAV_03, DeviceType.NAV_09_10), dataSource.reads)
    }

    @Test
    fun `same device answered for two sdk types of a group is shown once`() = runTest {
        val rhm = PeripheralDeviceInformation.Rhm(
            id = 0,
            status = PeripheralDeviceStatus.Default(isBound = true, isConnected = true),
            addressByteArray = byteArrayOf(0xEA.toByte(), 0x52, 0xA1.toByte(), 0xB0.toByte(), 0x35, 0xD4.toByte()),
            signalLevel = -60,
            engineTemperature = 20,
        )
        dataSource.byType[DeviceType.RHM] = SpecialistResponse.Success(listOf(rhm))
        dataSource.byType[DeviceType.SIREN] = SpecialistResponse.Success(listOf(rhm))

        val step =
            assertIs<Response.Success<PeripheralPollStep>>(
                repository.poll(listOf(PeripheralType.RHM)).take(1).toList().single(),
            )

        assertEquals(1, step.value.devices.orEmpty().size)
    }

    @Test
    fun `other read error ends the poll`() = runTest {
        val failure = IllegalStateException("no link")
        dataSource.byType[DeviceType.TAG_760] = SpecialistResponse.Error.General(failure)

        val values = repository.poll(listOf(PeripheralType.TAG, PeripheralType.RELAY)).toList()

        assertIs<Response.Error>(values.single())
    }

    @Test
    fun `firmware mode not confirmed is a typed failure`() = runTest {
        dataSource.command = SpecialistResponse.Error.General(PeripheralProgramModeException("A1"))

        val error = assertIs<Response.Error>(repository.enterFirmwareMode("A1"))

        assertEquals(PeripheralFailure.NOT_CONFIRMED, assertIs<PeripheralException>(error.exception).failure)
        assertEquals(listOf("firmware A1"), dataSource.calls)
    }

    private fun tag(id: Int, bound: Boolean, connected: Boolean = true) = PeripheralDeviceInformation.Tag(
        id = id,
        status = PeripheralDeviceStatus.Tag(
            isBound = bound,
            isConnected = connected,
            inZone = true,
            pressed = false,
            isBlinking = false,
            lowBatteryLevel = false,
        ),
        addressByteArray = byteArrayOf(
            0xA1.toByte(),
            0xB2.toByte(),
            0xC3.toByte(),
            0xD4.toByte(),
            0xE5.toByte(),
            0xF6.toByte(),
        ),
        signalLevel = -67,
        voltage = 2.95f,
    )
}
