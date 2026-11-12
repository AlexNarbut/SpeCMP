package ru.alarmtrade.specmp.feature.device.settings.data.service

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsWrites
import ru.alarmtrade.specmp.feature.device.settings.domain.service.SettingsWrite

internal class DeviceSettingsWritesImpl : DeviceSettingsWrites {
    // Без подписчиков запись никому не нужна; с подписчиком не теряется и не задерживает записавшего.
    private val events = MutableSharedFlow<SettingsWrite>(
        extraBufferCapacity = BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun writes(): Flow<Response<SettingsWrite>> = events.map { Response.Success(it) }

    override fun record(write: SettingsWrite) {
        events.tryEmit(write)
    }

    private companion object {
        const val BUFFER = 16
    }
}
