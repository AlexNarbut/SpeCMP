package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothManufacturerData
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothOperationMode
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.information.AlarmDeviceOperationMode
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemFeature
import ru.alarmtrade.specialist.domain.bluetooth.ConnectionState
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

/**
 * Реализация [DeviceSessionRepository] над `bluetoothAlarmDeviceInteractor` — он работает с текущей
 * сессией SDK независимо от транспорта (BLE/USB). Интерактор резолвится лениво, как в соседних репозиториях.
 */
class SpecialistDeviceSessionRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor
) : DeviceSessionRepository {

    override fun linkState(): Flow<DeviceLinkState> = flow {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
        if (interactor == null) {
            emit(DeviceLinkState.DISCONNECTED)
            return@flow
        }
        emitAll(
            interactor.getDeviceConnectionStateFlowUseCase.execute().map { response ->
                when (response) {
                    is SpecialistResponse.Success -> response.value.toDomain()
                    is SpecialistResponse.Error -> DeviceLinkState.DISCONNECTED
                }
            }
        )
    }.catch { e ->
        if (e is CancellationException) throw e
        emit(DeviceLinkState.DISCONNECTED)
    }

    override suspend fun readBluetoothModule(): Response<BluetoothModule> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        // Мануфактура — обмен с модулем (заодно проверка, что он отвечает). Режима в ней нет: SDK определяет его
        // при подключении и отдаёт из кэша, к блоку этот вызов не ходит.
        val manufacturer = when (val response = interactor.getBluetoothManufacturerDataUseCase.execute().toResponse()) {
            is Response.Success -> response.value.toDomain()
            is Response.Error -> return response
        }
        return interactor.getBluetoothOperationModeUseCase.execute().toResponse().mapIfSuccess { mode ->
            when (mode) {
                BluetoothOperationMode.BLE_MAIN_MODE -> Response.Success(
                    BluetoothModule(BluetoothModuleMode.MAIN, manufacturer)
                )

                BluetoothOperationMode.BLE_BOOT_MODE -> Response.Success(
                    BluetoothModule(BluetoothModuleMode.BOOT, manufacturer)
                )

                BluetoothOperationMode.ERROR -> Response.Error(
                    IllegalStateException("Bluetooth module mode is unknown")
                )
            }
        }
    }

    override suspend fun readDevice(): Response<AlarmDevice> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        val information = when (val response = interactor.getAlarmDeviceInformationUseCase.execute().toResponse()) {
            is Response.Success -> response.value
            is Response.Error -> return response
        }
        // Возможности — со справочника по имени системы; не прочитаны — `null` (блок не поддерживается).
        val sdkFeatures = interactor.getAlarmDeviceAppFeaturesUseCase.execute().toResponse().getValueIfSuccess()
        val can = information.canInformation
        return Response.Success(
            AlarmDevice(
                systemId = information.systemId,
                systemName = information.systemName,
                firmwareVersion = information.firmwareVersion,
                mode = if (information.operationMode.isBoot()) DeviceOperationMode.BOOT else DeviceOperationMode.MAIN,
                features = sdkFeatures?.mapNotNullTo(mutableSetOf()) { it.toDomain() },
                can = if (sdkFeatures?.contains(SubsystemFeature.SINGLE_CAN_FIRMWARE) == true) {
                    CanInfo(
                        firmwareVersion = can.canVersionString,
                        code = can.canCode,
                        isFactory = can.isFactoryCanSettings
                    )
                } else {
                    null
                }
            )
        )
    }

    override suspend fun readImei(): Response<String> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        return interactor.getModemInformationUseCase.execute().toResponse().map { it.imei }
    }

    override suspend fun reconnect(): Response<Unit> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        return interactor.connectUseCase.execute().toResponse().map { }
    }

    override suspend fun disconnect(): Response<Unit> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        return interactor.disconnectUseCase.execute().toResponse().map { }
    }

    override suspend fun setBootMode(): Response<Unit> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        return interactor.setAlarmDeviceBootModeUseCase.execute().toResponse().map { }
    }

    private fun interactorUnavailable(): Response.Error =
        Response.Error(IllegalStateException("Alarm device interactor is not available on this platform"))

    private fun <T> SpecialistResponse<T>.toResponse(): Response<T> = when (this) {
        is SpecialistResponse.Success -> Response.Success(value)
        is SpecialistResponse.Error -> Response.Error(exception, message)
    }

    private fun BluetoothManufacturerData.toDomain(): BluetoothManufacturer = BluetoothManufacturer(
        serialNumber = serialNumber,
        firmwareName = firmwareName,
        deviceName = deviceName,
        bootFirmwareVersion = bootFirmwareVersion,
        mainFirmwareVersion = mainFirmwareVersion
    )

    private fun ConnectionState.toDomain(): DeviceLinkState = when (this) {
        ConnectionState.READY -> DeviceLinkState.READY
        ConnectionState.CONNECTING, ConnectionState.CONNECTED -> DeviceLinkState.CONNECTING
        ConnectionState.DISCONNECTED, ConnectionState.UNKNOWN -> DeviceLinkState.DISCONNECTED
    }

    private fun AlarmDeviceOperationMode.isBoot(): Boolean = this == AlarmDeviceOperationMode.BOOT_OLD ||
        this == AlarmDeviceOperationMode.BOOT_MLD ||
        this == AlarmDeviceOperationMode.BOOT_FLD

    private fun SubsystemFeature.toDomain(): DeviceFeature? = when (this) {
        SubsystemFeature.FIRMWARE_UPDATE -> DeviceFeature.FIRMWARE_UPDATE
        SubsystemFeature.VOICE_FIRMWARE_BASE -> DeviceFeature.VOICE_FIRMWARE
        SubsystemFeature.DEVICE_SETTINGS -> DeviceFeature.SETTINGS
        SubsystemFeature.BYPASS_LEARNING -> DeviceFeature.BYPASS_LEARNING
        SubsystemFeature.CLONE -> DeviceFeature.CLONE
        SubsystemFeature.DYNAMIC_PERIPHERAL_DEVICES -> DeviceFeature.DYNAMIC_DEVICES
        else -> null
    }
}
