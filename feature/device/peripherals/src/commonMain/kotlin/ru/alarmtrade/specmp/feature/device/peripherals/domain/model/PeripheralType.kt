package ru.alarmtrade.specmp.feature.device.peripherals.domain.model

/** Типы периферии в порядке каталога: в этом порядке «Устройства системы» опрашивают блок и показывают группы. */
enum class PeripheralType {
    TAG,
    KEYFOB,
    WATCH,
    BRACELET,
    RELAY,
    HOOD_LOCK,
    RHM,
    DOOR_SENSOR,
    TPMS,
    FUEL_SENSOR,
    PIR_SENSOR,
    VS,
    NAV,
    DI,
    BT02,
    RF_MODULE,
    IMMOBILIZER,
    NFC_READER,
    FUNCTION_BUTTON,
    ALT_420,
    PHONE,
    BASE,
    UNKNOWN,
}

/** Типы, которые опрашивают «Устройства системы»; остальные бывают только в динамической таблице. */
val SystemPeripheralTypes: List<PeripheralType> = PeripheralType.entries.take(PeripheralType.RF_MODULE.ordinal + 1)

/**
 * Типы для опроса по версии BLE-модуля (`4.35`, `VER4.35`): старые модули о новых типах не знают и на запрос
 * молчат до таймаута. Пороги — из старого приложения (линейка 4.x); версия неизвестна или из другой линейки —
 * опрашиваем все.
 */
fun systemPeripheralTypesFor(bluetoothVersion: String?): List<PeripheralType> {
    val version = bluetoothVersion?.let(::parseVersion)?.takeIf { it / MINOR_DIGITS == GATED_MAJOR }
        ?: return SystemPeripheralTypes
    return SystemPeripheralTypes.filter { type -> MinBluetoothVersion[type]?.let { version >= it } ?: true }
}

private val MinBluetoothVersion = mapOf(
    PeripheralType.VS to 410,
    PeripheralType.TPMS to 412,
    PeripheralType.FUEL_SENSOR to 413,
    PeripheralType.PIR_SENSOR to 422,
)

private const val MINOR_DIGITS = 100
private const val GATED_MAJOR = 4

private fun parseVersion(raw: String): Int? {
    val match = Regex("""(\d+)\.(\d{1,2})""").find(raw) ?: return null
    val (major, minor) = match.destructured
    return major.toInt() * MINOR_DIGITS + minor.toInt()
}
