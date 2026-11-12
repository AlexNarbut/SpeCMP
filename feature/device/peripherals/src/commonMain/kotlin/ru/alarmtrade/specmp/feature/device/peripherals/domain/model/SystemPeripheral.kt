package ru.alarmtrade.specmp.feature.device.peripherals.domain.model

/**
 * Приживлённое устройство из опроса «Устройства системы». [number] — номер среди устройств своего типа (с 1),
 * [address] — hex-адрес устройства (по нему идут «Поморгать» и «Обновить»).
 */
data class SystemPeripheral(
    val type: PeripheralType,
    val number: Int,
    val address: String,
    val link: PeripheralLink,
    /** Уровень сигнала, dBm; `null` — устройство не на связи или тип его не сообщает. */
    val signalDbm: Int?,
    /** Напряжение питания, В; `null` — тип его не сообщает. */
    val voltage: Float?,
    val lowBattery: Boolean,
    val details: PeripheralDetails?,
)

enum class PeripheralLink {
    /** На связи и рядом с автомобилем (метки, брелоки, часы, браслеты). */
    NEAR,

    /** На связи, но вне зоны. */
    FAR,
    ONLINE,
    OFFLINE,

    /** Приживлено, связь тип не сообщает (датчики шин). */
    PAIRED,
}

/** Показатели своего типа. */
sealed interface PeripheralDetails {
    data class Tag(val pressed: Boolean, val blinking: Boolean) : PeripheralDetails

    data class Relay(val blocked: Boolean, val serviceMode: Boolean) : PeripheralDetails

    data class HoodLock(
        val locked: Boolean,
        val hoodOpened: Boolean,
        val serviceMode: Boolean,
        val temperature: Int,
        val triggerCount: Long,
    ) : PeripheralDetails

    data class Tpms(val pressureBar: Float, val temperature: Int) : PeripheralDetails

    data class FuelSensor(val liters: Int, val percent: Int, val temperature: Int) : PeripheralDetails

    data class Pir(val index: Int, val temperature: Int) : PeripheralDetails

    data class EngineTemperature(val celsius: Int) : PeripheralDetails

    data class Vs(val enabled: Boolean) : PeripheralDetails
}

/** Что умеет устройство этого типа на экране «Подробнее». */
val PeripheralType.canBlink: Boolean get() = this == PeripheralType.TAG || this == PeripheralType.DOOR_SENSOR

/** Датчики шин в режим прошивки не переводятся. */
val PeripheralType.canEnterFirmwareMode: Boolean get() = this != PeripheralType.TPMS

/** Один шаг опроса: тип [type] под номером [index] из [total]; [devices] `null` — тип не ответил. */
data class PeripheralPollStep(
    val type: PeripheralType,
    val index: Int,
    val total: Int,
    val devices: List<SystemPeripheral>?,
)
