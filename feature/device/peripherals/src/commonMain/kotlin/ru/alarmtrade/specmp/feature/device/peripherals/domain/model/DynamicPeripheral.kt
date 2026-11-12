package ru.alarmtrade.specmp.feature.device.peripherals.domain.model

/**
 * Слот динамической таблицы блока. [slot] — номер слота в таблице (идентификатор для команд), [number] — номер
 * среди слотов своего типа (с 1, по порядку слотов).
 */
data class DynamicPeripheral(
    val slot: Int,
    val type: PeripheralType,
    val number: Int,
    val paired: Boolean,
    val connected: Boolean,
    /** Напряжение, В; `null` — неизвестно (устройство не на связи или тип его не сообщает). */
    val voltage: Float?,
    /** Адрес PIR-датчика на связи (блок отдаёт его вместо температуры). */
    val pirAddress: Int?,
)
