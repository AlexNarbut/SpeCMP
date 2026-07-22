package ru.alarmtrade.specmp.feature.device.interaction.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDeviceInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootData

/**
 * Фасад над bluetoothAlarmDevice-интерактором: чтение информации об
 * уже подключённом устройстве и завершение работы с ним.
 */
interface AlarmDeviceRepository {
    /** Прочитать информацию об устройстве в рабочем режиме. */
    suspend fun getAlarmDeviceInformation(): Response<AlarmDeviceInfo>

    /** Прочитать данные устройства в режиме загрузчика (BOOT). */
    suspend fun getBootData(): Response<BootData>

    /** Отключиться от устройства. */
    suspend fun disconnect()
}
