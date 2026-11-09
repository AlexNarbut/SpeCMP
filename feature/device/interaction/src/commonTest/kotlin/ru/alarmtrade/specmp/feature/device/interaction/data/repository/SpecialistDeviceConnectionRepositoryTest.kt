package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.bluetoothControl.domain.bluetooth.ConnectingStatus
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.serial.SerialPortConnectingStatus
import ru.alarmtrade.specialist.domain.usb.UsbConnectingState
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState

class SpecialistDeviceConnectionRepositoryTest {
    private val dataSource = FakeDeviceConnectionDataSource()
    private val repository = SpecialistDeviceConnectionRepository(dataSource)

    @Test
    fun `ble connection maps statuses and stops the SDK scan`() = runTest {
        dataSource.bluetooth = flowOf(
            SpecialistResponse.Success(ConnectingStatus.BONDING),
            SpecialistResponse.Success(ConnectingStatus.BONDING_FAIL),
            SpecialistResponse.Success(ConnectingStatus.CONNECTING_FAIL),
            SpecialistResponse.Success(ConnectingStatus.CONNECTED),
        )

        val states = repository.connectBle(deviceId = "AA:BB", timeoutMillis = 15_000).toList()

        assertEquals(
            listOf(
                DeviceConnectionState.CONNECTING,
                DeviceConnectionState.PAIRING_REQUIRED,
                DeviceConnectionState.FAILED,
                DeviceConnectionState.CONNECTED,
            ).map { Response.Success(it) },
            states,
        )
        val request = dataSource.bluetoothRequests.single()
        assertEquals("AA:BB", request.deviceMacAddress)
        assertEquals(15_000, request.connectionParameters.timeout)
        assertEquals(1, dataSource.scanStops)
    }

    @Test
    fun `cancelled ble connection still stops the scan`() = runTest {
        dataSource.bluetooth = flowOf(
            SpecialistResponse.Success(ConnectingStatus.CONNECTED),
            SpecialistResponse.Success(ConnectingStatus.CONNECTING),
        )

        repository.connectBle("AA:BB", 1_000).first()

        assertEquals(1, dataSource.scanStops)
    }

    @Test
    fun `unavailable transport ends with an error without scanning`() = runTest {
        val ble = repository.connectBle("AA:BB", 1_000).toList()
        val usb = repository.connectUsb("usb-1").toList()
        val serial = repository.connectSerial("COM3").toList()

        listOf(ble, usb, serial).forEach { assertIs<Response.Error>(it.single()) }
        assertEquals(0, dataSource.scanStops)
    }

    @Test
    fun `usb and serial states are mapped and serial goes at PcBase speed`() = runTest {
        dataSource.usb = flowOf(
            SpecialistResponse.Success(UsbConnectingState.ASKING_PERMISSIONS),
            SpecialistResponse.Success(UsbConnectingState.READY),
        )
        dataSource.serial = flowOf(
            SpecialistResponse.Success(SerialPortConnectingStatus.INITIALIZING),
            SpecialistResponse.Success(SerialPortConnectingStatus.FAILED),
        )

        assertEquals(
            listOf(DeviceConnectionState.CONNECTING, DeviceConnectionState.CONNECTED).map { Response.Success(it) },
            repository.connectUsb("usb-1").toList(),
        )
        assertEquals(
            listOf(DeviceConnectionState.CONNECTING, DeviceConnectionState.FAILED).map { Response.Success(it) },
            repository.connectSerial("COM3").toList(),
        )
        val request = dataSource.serialRequests.single()
        assertEquals("COM3", request.deviceId)
        assertEquals(115_200, request.baudRate)
    }

    @Test
    fun `failed connection flow ends with its error`() = runTest {
        val failure = IllegalStateException("adapter off")
        dataSource.usb = flow {
            emit(SpecialistResponse.Success(UsbConnectingState.INITIALIZING))
            throw failure
        }

        val states = repository.connectUsb("usb-1").toList()

        assertEquals(Response.Success(DeviceConnectionState.CONNECTING), states.first())
        assertSame(failure, assertIs<Response.Error>(states.last()).exception)
    }

    @Test
    fun `closing sessions reports the first transport error and skips unavailable ones`() = runTest {
        dataSource.closeUsb = null
        assertEquals(Response.Success(Unit), repository.closeSessions())

        val failure = IllegalStateException("port busy")
        dataSource.closeSerial = SpecialistResponse.Error.General(failure)
        assertSame(failure, assertIs<Response.Error>(repository.closeSessions()).exception)
    }
}
