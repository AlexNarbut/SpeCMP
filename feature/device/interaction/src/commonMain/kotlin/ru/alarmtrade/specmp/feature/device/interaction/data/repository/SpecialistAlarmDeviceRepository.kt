package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.CancellationException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDeviceInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootData
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.AlarmDeviceRepository
import ru.alarmtrade.specialist.domain.alarmDevice.information.AlarmDeviceInformation
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor

class SpecialistAlarmDeviceRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor,
) : AlarmDeviceRepository {

    override suspend fun getAlarmDeviceInformation(): Response<AlarmDeviceInfo> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull() ?: return unavailable()
        return when (val response = interactor.getAlarmDeviceInformationUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(response.value.toDomain())
            is SpecialistResponse.Error -> Response.Error(response.exception, response.message)
        }
    }

    override suspend fun getBootData(): Response<BootData> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull() ?: return unavailable()
        return when (val response = interactor.getBluetoothManufacturerDataUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(
                BootData(
                    deviceName = response.value.deviceName,
                    firmwareName = response.value.firmwareName,
                    firmwareVersion = response.value.mainFirmwareVersion,
                ),
            )
            is SpecialistResponse.Error -> Response.Error(response.exception, response.message)
        }
    }

    override suspend fun disconnect() {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull() ?: return
        safeRun { interactor.disconnectUseCase.execute() }
    }

    private fun <T> unavailable(): Response<T> =
        Response.Error(IllegalStateException("Alarm device interactor is not available on this platform"))

    private fun AlarmDeviceInformation.toDomain(): AlarmDeviceInfo = AlarmDeviceInfo(
        systemId = systemId.toString(),
        systemName = systemName,
        firmwareVersion = firmwareVersion,
    )
}

private fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    null
}
