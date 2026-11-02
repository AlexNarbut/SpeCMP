package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

/**
 * Мастер быстрого монтажа (одна VM на мастер). Сейчас — фиксация блока и проверка, что это сигнализация.
 *
 * Блок фиксируется на первом подключении. На каждом следующем — сверка `systemId`: другой блок останавливает мастер
 * экраном «Подключён другой блок», возврат нужного — продолжение с того же места. Класс блока проверяется один раз.
 */
class FastMountViewModel(
    deviceSession: DeviceSession,
    private val repository: FastMountRepository,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(FastMountState())
    val state: StateFlow<FastMountState> = _state.asStateFlow()

    init {
        onEachDeviceReady(::onDeviceReady)
    }

    fun onRetry() {
        val device = currentDevice ?: return
        launchOnDevice { checkClass(device) }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val fixed = _state.value.fixed ?: FixedDevice(device.systemId, device.systemName, device.title)
            .also { fixed -> _state.update { it.copy(fixed = fixed) } }
        if (device.systemId != fixed.systemId) {
            logger.d { "Another unit connected: ${device.systemId}, expected ${fixed.systemId}" }
            _state.update { it.copy(check = DeviceCheck.WrongDevice(device.title)) }
            return
        }
        when (_state.value.check) {
            // Тот же блок вернулся — продолжаем, где были; класс уже проверен.
            is DeviceCheck.WrongDevice -> _state.update { it.copy(check = DeviceCheck.Ready) }

            DeviceCheck.Ready, is DeviceCheck.Unsupported -> Unit

            DeviceCheck.Checking, is DeviceCheck.Failed -> checkClass(device)
        }
    }

    private suspend fun checkClass(device: AlarmDevice) {
        _state.update { it.copy(check = DeviceCheck.Checking) }
        // Без модели в базе класс не определить (DeviceSession уже пробовал синхронизировать базу).
        if (!device.modelInDatabase) {
            _state.update { it.copy(check = DeviceCheck.Unsupported(UnsupportedReason.NOT_IN_DATABASE)) }
            return
        }
        val check = when (val response = withContext(dispatchers.io) { repository.deviceClass(device.systemName) }) {
            is Response.Success -> when (response.value) {
                MountDeviceClass.ALARM -> DeviceCheck.Ready
                MountDeviceClass.MICROBOT -> DeviceCheck.Unsupported(UnsupportedReason.MICROBOT)
                MountDeviceClass.OTHER -> DeviceCheck.Unsupported(UnsupportedReason.NOT_ALARM)
            }

            is Response.Error -> DeviceCheck.Failed(FastMountError.from(response.exception))
        }
        logger.d { "Unit ${device.systemName} check: $check" }
        _state.update { it.copy(check = check) }
    }
}
