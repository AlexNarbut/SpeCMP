package ru.alarmtrade.specmp.feature.device.firmware.domain.session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion

/**
 * Результат экрана «Архив прошивок»: выбранная версия возвращается экрану обновления, который лежит под ним в стеке
 * (его ViewModel жива и собирает [picks]). Нет повтора: версия, выбранная до появления подписчика, не нужна —
 * архив открывается только с экрана обновления.
 */
class FirmwareArchivePicker {

    private val _picks = MutableSharedFlow<FirmwareVersion>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val picks: SharedFlow<FirmwareVersion> = _picks.asSharedFlow()

    fun pick(version: FirmwareVersion) {
        _picks.tryEmit(version)
    }
}
