package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import ru.alarmtrade.bluetoothControl.domain.bluetooth.ConnectingStatus
import ru.alarmtrade.bluetoothControl.domain.bluetooth.ConnectionParameters
import ru.alarmtrade.bluetoothControl.interactors.bluetoothConnector.requests.ConnectToBluetoothDeviceRequest
import ru.alarmtrade.specialist.domain.serial.SerialPortConnectingStatus
import ru.alarmtrade.specialist.domain.usb.UsbConnectingState
import ru.alarmtrade.specialist.interactors.bluetoothConnector.BluetoothConnectorInteractor
import ru.alarmtrade.specialist.interactors.serialConnector.SerialConnectorInteractor
import ru.alarmtrade.specialist.interactors.serialConnector.requests.ConnectToSerialPortRequest
import ru.alarmtrade.specialist.interactors.serialConnector.requests.SerialDeviceType
import ru.alarmtrade.specialist.interactors.usbConnector.UsbConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.requests.ConnectToUsbDeviceRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository

/**
 * Реализация [DeviceConnectionRepository]. Интеракторы транспортов резолвятся лениво
 * ([resolveOrNull]): недоступный на платформе транспорт не роняет создание репозитория;
 * попытка подключения по нему возвращает [Response.Error].
 */
class SpecialistDeviceConnectionRepository(
    private val bluetoothConnectorInteractor: () -> BluetoothConnectorInteractor,
    private val usbConnectorInteractor: () -> UsbConnectorInteractor,
    private val serialConnectorInteractor: () -> SerialConnectorInteractor
) : DeviceConnectionRepository {

    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = flow {
        val interactor = bluetoothConnectorInteractor.resolveOrNull()
            ?: run {
                emit(interactorUnavailable())
                return@flow
            }
        emitAll(
            safeFlowRun {
                interactor.openBluetoothAlarmDeviceSessionFlowableUseCase
                    .execute(
                        ConnectToBluetoothDeviceRequest(
                            deviceMacAddress = deviceId,
                            connectionParameters = ConnectionParameters(timeout = timeoutMillis)
                        )
                    )
                    .map { response -> response.toResponse().map { it.toDomain() } }
            }.onCompletion {
                // connect SDK внутри запускает свой поиск (manufacturer data) и не останавливает
                // его. Подписку обычно отменяют (первый финальный статус, отмена, таймаут).
                withContext(NonCancellable) { interactor.stopBluetoothScanUseCase.execute() }
            }
        )
    }

    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = flow {
        val interactor = usbConnectorInteractor.resolveOrNull()
            ?: run {
                emit(interactorUnavailable())
                return@flow
            }
        emitAll(
            safeFlowRun {
                interactor.openUsbDeviceSessionFlowableUseCase
                    .execute(ConnectToUsbDeviceRequest(deviceId))
                    .map { response -> response.toResponse().map { it.toDomain() } }
            }
        )
    }

    override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> = flow {
        val interactor = serialConnectorInteractor.resolveOrNull()
            ?: run {
                emit(interactorUnavailable())
                return@flow
            }
        emitAll(
            safeFlowRun {
                interactor.openSerialPortSessionFlowableUseCase
                    .execute(ConnectToSerialPortRequest(portId, SERIAL_BAUD_RATE, SerialDeviceType.Alarm))
                    .map { response -> response.toResponse().map { it.toDomain() } }
            }
        )
    }

    override suspend fun closeSessions(): Response<Unit> {
        // Сессия в SDK одна на все транспорты: закрываем через каждый доступный интерактор.
        val ble = bluetoothConnectorInteractor.resolveOrNull()
            ?.closeBluetoothDeviceSessionUseCase
            ?.execute()
            ?.toResponse()
        val usb = usbConnectorInteractor.resolveOrNull()?.closeUsbDeviceSessionUseCase?.execute()?.toResponse()
        val serial = serialConnectorInteractor.resolveOrNull()?.closeSerialDeviceSessionUseCase?.execute()?.toResponse()
        return listOfNotNull(ble, usb, serial).firstOrNull { it is Response.Error } ?: Response.Success(Unit)
    }

    private fun ConnectingStatus.toDomain(): DeviceConnectionState = when (this) {
        ConnectingStatus.CONNECTED -> DeviceConnectionState.CONNECTED

        ConnectingStatus.BONDING_FAIL -> DeviceConnectionState.PAIRING_REQUIRED

        ConnectingStatus.CONNECTING_FAIL,
        ConnectingStatus.FAIL
        -> DeviceConnectionState.FAILED

        else -> DeviceConnectionState.CONNECTING
    }

    private fun UsbConnectingState.toDomain(): DeviceConnectionState = when (this) {
        UsbConnectingState.READY -> DeviceConnectionState.CONNECTED

        UsbConnectingState.FAILED -> DeviceConnectionState.FAILED

        UsbConnectingState.ASKING_PERMISSIONS,
        UsbConnectingState.INITIALIZING
        -> DeviceConnectionState.CONNECTING
    }

    private fun SerialPortConnectingStatus.toDomain(): DeviceConnectionState = when (this) {
        SerialPortConnectingStatus.READY -> DeviceConnectionState.CONNECTED

        SerialPortConnectingStatus.FAILED -> DeviceConnectionState.FAILED

        SerialPortConnectingStatus.ASKING_PERMISSIONS,
        SerialPortConnectingStatus.INITIALIZING
        -> DeviceConnectionState.CONNECTING
    }

    private companion object {
        /** Скорость PcBase по COM-порту (`DEFAULT_PCBASE_BAUD_RATE` SDK). */
        const val SERIAL_BAUD_RATE = 115_200
    }
}
