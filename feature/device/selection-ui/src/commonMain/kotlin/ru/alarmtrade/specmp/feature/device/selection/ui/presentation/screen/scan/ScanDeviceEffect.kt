package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectedDevice

/** Тип конфликта сопряжения BLE (нужно снять bond перед подключением). */
enum class BondConflictKind {
    /** Устройство сопряжено с телефоном, но не с прибором — снять на телефоне. */
    REMOVE_FROM_PHONE,

    /** Устройство сопряжено с прибором, но не с телефоном — снять на приборе. */
    REMOVE_FROM_DEVICE,
}

/** One-shot события экрана поиска (см. docs/patterns.md — «One-shot события»). */
sealed interface ScanDeviceEffect {
    /** Сессия с устройством создана — дальше flow работы с устройством (решает навигация приложения). */
    data class SessionCreated(val device: ConnectedDevice) : ScanDeviceEffect

    /** Подключение не удалось. */
    data object ConnectionFailed : ScanDeviceEffect

    /** Выход из поиска. */
    data object Close : ScanDeviceEffect

    /** Обнаружен конфликт сопряжения — подключение невозможно без снятия bond. */
    data class BondConflict(val kind: BondConflictKind) : ScanDeviceEffect
}
