package ru.alarmtrade.specmp.core.specialist

/**
 * Типы соединений, доступные для поиска/подключения устройств на текущей платформе.
 * Источник правды для ветвления сканирования (какие транспорты опрашивать) и подсказок.
 *
 * - Android — BLE + USB + COM (OTG)
 * - iOS — только BLE (COM в SDK не реализован)
 * - Desktop — USB + COM
 */
expect fun supportedConnectionTypes(): Set<DeviceConnectionType>
