package ru.alarmtrade.specmp.core.specialist

/**
 * Типы соединений, доступные для поиска/подключения устройств на текущей платформе.
 * Источник правды для ветвления сканирования (какие транспорты опрашивать) и подсказок.
 *
 * - Android — BLE + USB
 * - iOS — только BLE
 * - Desktop — только USB
 */
expect fun supportedConnectionTypes(): Set<DeviceConnectionType>
