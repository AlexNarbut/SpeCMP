package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

/** One-shot события экрана поиска (см. docs/patterns.md — «One-shot события»). */
sealed interface ScanDeviceEffect {
    /** Сессия с устройством создана — дальше flow работы с устройством (решает навигация приложения). */
    data object SessionCreated : ScanDeviceEffect

    /** Подключение не удалось. */
    data object ConnectionFailed : ScanDeviceEffect

    /** Выход из поиска. */
    data object Close : ScanDeviceEffect

    /** BLE-блок привязан к другому телефону: с этого телефона не исправить — сбросить сопряжение на приборе. */
    data object BoundToAnotherPhone : ScanDeviceEffect

    /** BLE: нужен режим сопряжения блока (старое сопряжение на телефоне уже снято). */
    data object PairingRequired : ScanDeviceEffect
}
