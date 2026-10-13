package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.CancellationException
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
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specialist.domain.usb.UsbConnectingState
import ru.alarmtrade.specialist.interactors.bluetoothConnector.BluetoothConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.UsbConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.requests.ConnectToUsbDeviceRequest

/**
 * Реализация [DeviceConnectionRepository]. Интеракторы транспортов резолвятся лениво
 * ([resolveOrNull]): недоступный на платформе транспорт не роняет создание репозитория;
 * попытка подключения по нему возвращает [Response.Error].
 */
class SpecialistDeviceConnectionRepository(
    private val bluetoothConnectorInteractor: () -> BluetoothConnectorInteractor,
    private val usbConnectorInteractor: () -> UsbConnectorInteractor,
) : DeviceConnectionRepository {

    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = flow {
        val interactor = bluetoothConnectorInteractor.resolveOrNull()
            ?: run {
                emit(transportUnavailable())
                return@flow
            }
        emitAll(
            safeFlowRun {
                interactor.openBluetoothAlarmDeviceSessionFlowableUseCase
                    .execute(
                        ConnectToBluetoothDeviceRequest(
                            deviceMacAddress = deviceId,
                            connectionParameters = ConnectionParameters(timeout = timeoutMillis),
                        ),
                    )
                    .map { response -> response.toStateResponse() }
            }.onCompletion {
                // connect SDK внутри запускает свой поиск (manufacturer data) и не останавливает
                // его. Подписку обычно отменяют (первый финальный статус, отмена, таймаут).
                withContext(NonCancellable) { interactor.stopBluetoothScanUseCase.execute() }
            },
        )
    }

    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = flow {
        val interactor = usbConnectorInteractor.resolveOrNull()
            ?: run {
                emit(transportUnavailable())
                return@flow
            }
        emitAll(
            safeFlowRun {
                interactor.openUsbDeviceSessionFlowableUseCase
                    .execute(ConnectToUsbDeviceRequest(deviceId))
                    .map { response -> response.toUsbStateResponse() }
            },
        )
    }

    override suspend fun closeSessions() {
        bluetoothConnectorInteractor.resolveOrNull()?.closeBluetoothDeviceSessionUseCase?.execute()
        usbConnectorInteractor.resolveOrNull()?.closeUsbDeviceSessionUseCase?.execute()
    }

    private fun transportUnavailable(): Response<DeviceConnectionState> =
        Response.Error(IllegalStateException("Transport is not available on this platform"))

    private fun SpecialistResponse<ConnectingStatus>.toStateResponse(): Response<DeviceConnectionState> =
        when (this) {
            is SpecialistResponse.Success -> Response.Success(value.toDomain())
            is SpecialistResponse.Error -> Response.Error(exception, message)
        }

    private fun SpecialistResponse<UsbConnectingState>.toUsbStateResponse(): Response<DeviceConnectionState> =
        when (this) {
            is SpecialistResponse.Success -> Response.Success(value.toDomain())
            is SpecialistResponse.Error -> Response.Error(exception, message)
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
}

private fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    null
}
