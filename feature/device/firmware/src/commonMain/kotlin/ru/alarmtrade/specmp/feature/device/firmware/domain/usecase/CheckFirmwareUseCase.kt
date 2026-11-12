package ru.alarmtrade.specmp.feature.device.firmware.domain.usecase

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleStatus
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.InstalledVersionSource
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.canFilter
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.installedVersionSource
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toFirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/**
 * Установленные версии модулей блока против последних на сервере (канал релизов). Ошибка запроса основного модуля —
 * ошибка проверки; у остальных модулей — нет версии.
 */
class CheckFirmwareUseCase(private val repository: FirmwareRepository, private val deviceSession: DeviceSession) {

    suspend operator fun invoke(device: AlarmDevice): Response<List<FirmwareModuleStatus>> {
        val transport = deviceSession.transport.value
            ?: return Response.Error(IllegalStateException("Device transport is unknown"))
        return repository.modules(device.systemName, transport).mapIfSuccess { modules ->
            val statuses = mutableListOf<FirmwareModuleStatus>()
            for (module in modules) {
                val installed = installedVersion(device, module.kind)
                if (!module.isBootSupported) {
                    statuses += FirmwareModuleStatus(module, installed, latest = null)
                    continue
                }
                val can = module.kind.canFilter(device.can.toFirmwareCan())
                val latest = repository.latest(module.id, FirmwareChannel.RELEASE, can)
                if (latest is Response.Error && module.kind == FirmwareModuleKind.AlarmMain) return@mapIfSuccess latest
                statuses += FirmwareModuleStatus(module, installed, latest.getValueIfSuccess())
            }
            Response.Success(statuses)
        }
    }

    private suspend fun installedVersion(device: AlarmDevice, kind: FirmwareModuleKind): String? =
        when (kind.installedVersionSource) {
            InstalledVersionSource.DEVICE -> device.displayFirmwareVersion

            InstalledVersionSource.BLUETOOTH_MODULE ->
                deviceSession.readBluetoothModule().getValueIfSuccess()?.manufacturer?.mainFirmwareVersion

            InstalledVersionSource.NONE -> null
        }
}
