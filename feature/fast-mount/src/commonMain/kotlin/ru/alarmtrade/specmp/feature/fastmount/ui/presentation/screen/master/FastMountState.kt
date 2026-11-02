package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

/**
 * Состояние мастера быстрого монтажа.
 *
 * @property fixed блок, с которым идёт монтаж: фиксируется на первом подключении и не меняется.
 */
@Immutable
data class FastMountState(val fixed: FixedDevice? = null, val check: DeviceCheck = DeviceCheck.Checking)

/** Блок монтажа: [title] — «Модель  ID» для шапки и сообщений. */
@Immutable
data class FixedDevice(val systemId: Long, val systemName: String, val title: String)

/** Проверка подключённого блока перед мастером. */
@Immutable
sealed interface DeviceCheck {
    data object Checking : DeviceCheck

    /** Блок подходит — дальше шаги мастера. */
    data object Ready : DeviceCheck

    data class Unsupported(val reason: UnsupportedReason) : DeviceCheck

    data class Failed(val error: FastMountError) : DeviceCheck

    /** После переподключения на связи другой блок: шаги остановлены, пока не вернётся [FastMountState.fixed]. */
    data class WrongDevice(val connectedTitle: String) : DeviceCheck
}

enum class UnsupportedReason {
    /** Не сигнализация (метка, реле, модуль…). */
    NOT_ALARM,

    MICROBOT,

    /** Модели нет в базе устройств — класс не определить. */
    NOT_IN_DATABASE
}
