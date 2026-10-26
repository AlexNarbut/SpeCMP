package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothManufacturerData
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothOperationMode
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.SetPcBasePinRequest
import ru.alarmtrade.specialist.domain.alarmDevice.information.AlarmDeviceOperationMode
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemFeature
import ru.alarmtrade.specialist.domain.bluetooth.ConnectionState
import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specialist.domain.errors.alarmDevice.AlarmDeviceNotFoundException
import ru.alarmtrade.specialist.interactors.appUpdates.AppUpdatesInteractor
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.common.safeResponseRun
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

/**
 * Реализация [DeviceSessionRepository] над `bluetoothAlarmDeviceInteractor` — он работает с текущей
 * сессией SDK независимо от транспорта (BLE/USB). Интерактор резолвится лениво, как в соседних репозиториях.
 */
class SpecialistDeviceSessionRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor,
    private val appUpdatesInteractor: () -> AppUpdatesInteractor
) : DeviceSessionRepository {

    override fun linkState(): Flow<DeviceLinkState> = flow {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
        if (interactor == null) {
            emit(DeviceLinkState.DISCONNECTED)
            return@flow
        }
        // Поток текущей сессии SDK: сам переключается на новую сессию после закрытия прежней (поток конкретной
        // сессии после её закрытия молчит, и долгоживущий подписчик не увидел бы новое подключение).
        emitAll(
            interactor.observeCurrentSessionConnectionStateUseCase.execute().map { response ->
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
        // Возможности — из базы устройств SDK по имени системы; не прочитаны — `null`. Модели нет в базе
        // (AlarmDeviceNotFoundException) — отдельно: базу нужно синхронизировать, блок при этом может поддерживаться.
        val featuresResponse = interactor.getAlarmDeviceAppFeaturesUseCase.execute().toResponse()
        val sdkFeatures = featuresResponse.getValueIfSuccess()
        val modelMissing = (featuresResponse as? Response.Error)?.exception is AlarmDeviceNotFoundException
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
                },
                modelInDatabase = !modelMissing
            )
        )
    }

    override suspend fun syncDeviceDatabase(): Response<Unit> {
        val interactor = appUpdatesInteractor.resolveOrNull() ?: return interactorUnavailable()
        // Поток SDK может бросить мимо Response (ошибка встроенной базы) — safeResponseRun превращает это в ошибку.
        return safeResponseRun {
            when (val last = interactor.synchronizeSettingsVersionUseCase.execute().lastOrNull()) {
                is SpecialistResponse.Success -> Response.Success(Unit)
                is SpecialistResponse.Error -> Response.Error(last.exception, last.message)
                null -> Response.Error(IllegalStateException("Device database sync produced no result"))
            }
        }
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

    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        val response = when (target) {
            BootModeTarget.MainProgram -> interactor.setAlarmDeviceBootModeUseCase.execute().toResponse()
            BootModeTarget.BluetoothModule -> interactor.setBluetoothModemBootModeUseCase.execute().toResponse()
        }
        return when (response) {
            is Response.Success -> Response.Success(BootModeResult.Entered)

            // Исход, а не сбой: экран просит PIN. Остальные ошибки (в т.ч. «блок не ответил») — Response.Error.
            is Response.Error ->
                if (response.exception is IncorrectServicePinException) {
                    Response.Success(BootModeResult.WrongPin)
                } else {
                    response
                }
        }
    }

    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        return when (target) {
            BootModeTarget.MainProgram -> interactor.exitAlarmDeviceBootModeUseCase.execute().toResponse().map { }

            BootModeTarget.BluetoothModule ->
                interactor.exitBluetoothModemBootModeUseCase.execute().toResponse().map { }
        }
    }

    override suspend fun setServicePin(pin: ServicePin): Response<Unit> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return interactorUnavailable()
        return interactor.setPcBasePinUseCase.execute(SetPcBasePinRequest(pin.value)).toResponse().map { }
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
