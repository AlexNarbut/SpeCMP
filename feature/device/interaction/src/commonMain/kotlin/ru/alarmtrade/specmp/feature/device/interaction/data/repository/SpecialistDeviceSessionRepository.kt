package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specialist.domain.alarmDevice.information.AlarmDeviceOperationMode
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemFeature
import ru.alarmtrade.specialist.domain.bluetooth.ConnectionState
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor

/**
 * Реализация [DeviceSessionRepository] над `bluetoothAlarmDeviceInteractor` — он работает с текущей
 * сессией SDK независимо от транспорта (BLE/USB). Интерактор резолвится лениво, как в соседних репозиториях.
 */
class SpecialistDeviceSessionRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor,
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
            },
        )
    }.catch { e ->
        if (e is CancellationException) throw e
        emit(DeviceLinkState.DISCONNECTED)
    }

    override suspend fun readDevice(): Response<AlarmDevice> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
            ?: return Response.Error(IllegalStateException("Alarm device interactor is not available on this platform"))
        val information = when (val response = interactor.getAlarmDeviceInformationUseCase.execute()) {
            is SpecialistResponse.Success -> response.value
            is SpecialistResponse.Error -> return Response.Error(response.exception, response.message)
        }
        // Возможности — со справочника по имени системы; недоступны — меню по умолчанию (как в старом приложении).
        val features = when (val response = interactor.getAlarmDeviceAppFeaturesUseCase.execute()) {
            is SpecialistResponse.Success -> response.value.mapNotNullTo(mutableSetOf()) { it.toDomain() }
            is SpecialistResponse.Error -> emptySet()
        }
        return Response.Success(
            AlarmDevice(
                systemId = information.systemId,
                systemName = information.systemName,
                firmwareVersion = information.firmwareVersion,
                mode = if (information.operationMode.isBoot()) DeviceOperationMode.BOOT else DeviceOperationMode.MAIN,
                features = features,
            ),
        )
    }

    private fun ConnectionState.toDomain(): DeviceLinkState = when (this) {
        ConnectionState.READY -> DeviceLinkState.READY
        ConnectionState.CONNECTING, ConnectionState.CONNECTED -> DeviceLinkState.CONNECTING
        ConnectionState.DISCONNECTED, ConnectionState.UNKNOWN -> DeviceLinkState.DISCONNECTED
    }

    private fun AlarmDeviceOperationMode.isBoot(): Boolean =
        this == AlarmDeviceOperationMode.BOOT_OLD ||
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

private fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    null
}
