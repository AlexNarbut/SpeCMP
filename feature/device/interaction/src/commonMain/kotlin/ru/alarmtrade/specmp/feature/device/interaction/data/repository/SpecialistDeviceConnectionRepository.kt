package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import ru.alarmtrade.bluetoothControl.domain.bluetooth.ConnectingStatus
import ru.alarmtrade.bluetoothControl.domain.bluetooth.ConnectionParameters
import ru.alarmtrade.bluetoothControl.interactors.bluetoothConnector.requests.ConnectToBluetoothDeviceRequest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.serial.SerialPortConnectingStatus
import ru.alarmtrade.specialist.domain.usb.UsbConnectingState
import ru.alarmtrade.specialist.interactors.serialConnector.requests.ConnectToSerialPortRequest
import ru.alarmtrade.specialist.interactors.serialConnector.requests.SerialDeviceType
import ru.alarmtrade.specialist.interactors.usbConnector.requests.ConnectToUsbDeviceRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.DeviceConnectionDataSource
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository

/** Недоступный на платформе транспорт не роняет создание репозитория: подключение по нему — [Response.Error]. */
internal class SpecialistDeviceConnectionRepository(private val dataSource: DeviceConnectionDataSource) :
    DeviceConnectionRepository {

    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = flow {
        val request = ConnectToBluetoothDeviceRequest(
            deviceMacAddress = deviceId,
            connectionParameters = ConnectionParameters(timeout = timeoutMillis),
        )
        val connection = dataSource.connectBluetooth(request)
        if (connection == null) {
            emit(interactorUnavailable())
            return@flow
        }
        emitAll(
            connection.toConnectionStates { it.toDomain() }.onCompletion {
                // connect SDK внутри запускает свой поиск (manufacturer data) и не останавливает
                // его. Подписку обычно отменяют (первый финальный статус, отмена, таймаут).
                withContext(NonCancellable) { dataSource.stopBluetoothScan() }
            },
        )
    }

    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = flow {
        val connection = dataSource.connectUsb(ConnectToUsbDeviceRequest(deviceId))
        emitAll(connection?.toConnectionStates { it.toDomain() } ?: flowOf(interactorUnavailable()))
    }

    override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> = flow {
        val request = ConnectToSerialPortRequest(portId, SERIAL_BAUD_RATE, SerialDeviceType.Alarm)
        val connection = dataSource.connectSerial(request)
        emitAll(connection?.toConnectionStates { it.toDomain() } ?: flowOf(interactorUnavailable()))
    }

    override suspend fun closeSessions(): Response<Unit> {
        // Сессия в SDK одна на все транспорты: закрываем через каждый доступный интерактор.
        val ble = dataSource.closeBluetoothSession()?.toResponse()
        val usb = dataSource.closeUsbSession()?.toResponse()
        val serial = dataSource.closeSerialSession()?.toResponse()
        return listOfNotNull(ble, usb, serial).firstOrNull { it is Response.Error } ?: Response.Success(Unit)
    }

    private fun <T> Flow<SpecialistResponse<T>>.toConnectionStates(
        toDomain: (T) -> DeviceConnectionState,
    ): Flow<Response<DeviceConnectionState>> = flow {
        emitAll(safeFlowRun { this@toConnectionStates.map { response -> response.toResponse().map(toDomain) } })
    }

    private fun ConnectingStatus.toDomain(): DeviceConnectionState = when (this) {
        ConnectingStatus.CONNECTED -> DeviceConnectionState.CONNECTED

        ConnectingStatus.BONDING_FAIL -> DeviceConnectionState.PAIRING_REQUIRED

        ConnectingStatus.CONNECTING_FAIL,
        ConnectingStatus.FAIL,
        -> DeviceConnectionState.FAILED

        else -> DeviceConnectionState.CONNECTING
    }

    private fun UsbConnectingState.toDomain(): DeviceConnectionState = when (this) {
        UsbConnectingState.READY -> DeviceConnectionState.CONNECTED

        UsbConnectingState.FAILED -> DeviceConnectionState.FAILED

        UsbConnectingState.ASKING_PERMISSIONS,
        UsbConnectingState.INITIALIZING,
        -> DeviceConnectionState.CONNECTING
    }

    private fun SerialPortConnectingStatus.toDomain(): DeviceConnectionState = when (this) {
        SerialPortConnectingStatus.READY -> DeviceConnectionState.CONNECTED

        SerialPortConnectingStatus.FAILED -> DeviceConnectionState.FAILED

        SerialPortConnectingStatus.ASKING_PERMISSIONS,
        SerialPortConnectingStatus.INITIALIZING,
        -> DeviceConnectionState.CONNECTING
    }

    private companion object {
        const val SERIAL_BAUD_RATE = 115_200
    }
}
