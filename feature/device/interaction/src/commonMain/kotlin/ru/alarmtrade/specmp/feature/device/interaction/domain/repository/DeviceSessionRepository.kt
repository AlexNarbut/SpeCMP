package ru.alarmtrade.specmp.feature.device.interaction.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin

interface DeviceSessionRepository {
    /** Поток состояния связи текущей сессии; ошибку SDK владелец сессии считает [DeviceLinkState.DISCONNECTED]. */
    fun linkState(): Flow<Response<DeviceLinkState>>

    /** BLE: прочитать Bluetooth-модуль (мануфактура + режим) — до чтения основной программы. */
    suspend fun readBluetoothModule(): Response<BluetoothModule>

    suspend fun readDevice(): Response<AlarmDevice>

    /** Нужна, когда модели подключённого блока нет в базе ([AlarmDevice.modelInDatabase]). */
    suspend fun syncDeviceDatabase(): Response<Unit>

    suspend fun readImei(): Response<String>

    suspend fun reconnect(): Response<Unit>

    /** Разорвать соединение текущей сессии, не закрывая её (дальше — переподключение). */
    suspend fun disconnect(): Response<Unit>

    /** BLE: SDK сам пробует сохранённый PIN или `1111`; неверный — [BootModeResult.WrongPin], не ошибка. */
    suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult>

    /** Блок перезагрузится и вернётся по обычному переподключению. */
    suspend fun exitBootMode(target: BootModeTarget): Response<Unit>

    suspend fun setServicePin(pin: ServicePin): Response<Unit>
}
