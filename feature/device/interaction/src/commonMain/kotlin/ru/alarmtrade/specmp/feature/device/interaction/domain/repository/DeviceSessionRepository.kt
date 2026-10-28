package ru.alarmtrade.specmp.feature.device.interaction.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin

/**
 * Фасад над текущей сессией SDK (`bluetoothAlarmDeviceInteractor` обслуживает и BLE, и USB):
 * состояние связи и чтение блока. Тонкий: решения о повторах/переходах — в
 * [DeviceSession][ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession].
 */
interface DeviceSessionRepository {
    /** Поток состояния связи текущей сессии; ошибки SDK — как [DeviceLinkState.DISCONNECTED]. */
    fun linkState(): Flow<DeviceLinkState>

    /** BLE: прочитать Bluetooth-модуль (мануфактура + режим) — до чтения основной программы. */
    suspend fun readBluetoothModule(): Response<BluetoothModule>

    /** Прочитать информацию и возможности подключённого блока. */
    suspend fun readDevice(): Response<AlarmDevice>

    /**
     * Синхронизировать базу устройств (модели блоков и их возможности) с сервером — та же синхронизация, что на
     * главном экране. Нужна, когда модели подключённого блока нет в базе ([AlarmDevice.modelInDatabase]).
     */
    suspend fun syncDeviceDatabase(): Response<Unit>

    /** Прочитать IMEI GSM-модема блока. */
    suspend fun readImei(): Response<String>

    /** Повторно подключиться к блоку текущей сессии после обрыва (ошибка — например, выключен Bluetooth). */
    suspend fun reconnect(): Response<Unit>

    /** Разорвать соединение текущей сессии, не закрывая её (дальше — переподключение). */
    suspend fun disconnect(): Response<Unit>

    /**
     * Перевести [target] в режим загрузчика (для прошивки); блок переподключится уже в загрузчике.
     * BLE: команда идёт через режим программирования — SDK сам пробует сохранённый PIN или `1111`;
     * неверный PIN — [BootModeResult.WrongPin], не ошибка.
     */
    suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult>

    /**
     * Выйти из режима загрузчика командой по связи ([target] — что перезагрузить в рабочий режим): блок перезагрузится
     * и вернётся по обычному переподключению. Нужна после обновления прошивки, пока блок остаётся в загрузчике.
     */
    suspend fun exitBootMode(target: BootModeTarget): Response<Unit>

    /** Сохранить PIN программирования для текущего блока (его используют команды в режиме программирования). */
    suspend fun setServicePin(pin: ServicePin): Response<Unit>
}
