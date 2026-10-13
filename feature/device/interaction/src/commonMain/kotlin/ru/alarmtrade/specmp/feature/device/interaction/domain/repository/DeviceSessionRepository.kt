package ru.alarmtrade.specmp.feature.device.interaction.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState

/**
 * Фасад над текущей сессией SDK (`bluetoothAlarmDeviceInteractor` обслуживает и BLE, и USB):
 * состояние связи и чтение блока. Тонкий: решения о повторах/переходах — в
 * [DeviceSession][ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession].
 */
interface DeviceSessionRepository {
    /** Поток состояния связи текущей сессии; ошибки SDK — как [DeviceLinkState.DISCONNECTED]. */
    fun linkState(): Flow<DeviceLinkState>

    /** Прочитать информацию и возможности подключённого блока. */
    suspend fun readDevice(): Response<AlarmDevice>

    /** Прочитать IMEI GSM-модема блока. */
    suspend fun readImei(): Response<String>

    /** Перевести блок в режим загрузчика (для прошивки); блок переподключится уже в загрузчике. */
    suspend fun setBootMode(): Response<Unit>
}
