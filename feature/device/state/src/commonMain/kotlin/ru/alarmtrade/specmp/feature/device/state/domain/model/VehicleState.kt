package ru.alarmtrade.specmp.feature.device.state.domain.model

/**
 * Живое состояние блока и автомобиля — один снимок телеметрии (блок присылает раз в 3 с, только BLE).
 * Группы, данных по которым блок не прислал, — `null` (на экране скрываются, нули не рисуем).
 */
data class VehicleState(
    val security: SecurityState,
    /** Блок в тревоге (сработала зона при охране). */
    val alarm: Boolean,
    val serviceMode: Boolean,
    val programMode: Boolean,
    val zones: Map<Zone, ZoneValue>,
    val engine: EngineInfo,
    val power: PowerInfo,
    val link: LinkInfo,
    val event: DeviceEventKind?,
    val can: CanInfo?,
    val ev: EvInfo?,
    val heater: HeaterInfo?,
    val camera: CameraInfo?,
    val siren: SirenMode,
    val handsFree: HandsFreeMode,
) {
    fun zone(zone: Zone): ZoneValue = zones[zone] ?: ZoneValue.OFF
}

enum class SecurityState { DISARMED, ARMED, ACTIVE_ARMED }

/** Группа зон (порядок — порядок на экране). */
enum class ZoneGroup { BODY, CONTROLS, SENSORS }

/** Зона, которую монтажник проверяет руками. Порядок — порядок на экране. */
enum class Zone(val group: ZoneGroup, val kind: ZoneKind) {
    DOOR_FRONT_LEFT(ZoneGroup.BODY, ZoneKind.DOOR),
    DOOR_FRONT_RIGHT(ZoneGroup.BODY, ZoneKind.DOOR),
    DOOR_REAR_LEFT(ZoneGroup.BODY, ZoneKind.DOOR),
    DOOR_REAR_RIGHT(ZoneGroup.BODY, ZoneKind.DOOR),
    HOOD(ZoneGroup.BODY, ZoneKind.LID),
    TRUNK(ZoneGroup.BODY, ZoneKind.LID),
    IGNITION(ZoneGroup.CONTROLS, ZoneKind.SWITCH),
    BRAKE(ZoneGroup.CONTROLS, ZoneKind.PEDAL),
    HANDBRAKE(ZoneGroup.CONTROLS, ZoneKind.SWITCH),
    NEUTRAL(ZoneGroup.CONTROLS, ZoneKind.SWITCH),
    LIGHTS(ZoneGroup.CONTROLS, ZoneKind.SWITCH),
    SHOCK(ZoneGroup.SENSORS, ZoneKind.ZONED_SENSOR),
    EXTRA_SENSOR(ZoneGroup.SENSORS, ZoneKind.ZONED_SENSOR),
    MOTION(ZoneGroup.SENSORS, ZoneKind.SENSOR),
    TILT(ZoneGroup.SENSORS, ZoneKind.SENSOR),
}

/** Тип зоны — от него зависят подписи значений («Открыта» / «Нажат» / «Сработал»). */
enum class ZoneKind { DOOR, LID, SWITCH, PEDAL, SENSOR, ZONED_SENSOR }

/** Значение зоны. Датчики удара и доп. датчик — пять состояний ([WARNING] … [MAIN_OFF]). */
enum class ZoneValue {
    /** Закрыта / выкл / норма. */
    OFF,

    /** Открыта / вкл / сработал. */
    ON,
    ALARM,

    /** Сработала зона предупреждения. */
    WARNING,

    /** Сработала основная зона. */
    MAIN,
    WARNING_OFF,
    MAIN_OFF,
    ;

    val tone: ZoneTone
        get() = when (this) {
            ALARM, MAIN -> ZoneTone.ALARM
            ON, WARNING -> ZoneTone.ACTIVE
            WARNING_OFF, MAIN_OFF -> ZoneTone.MUTED
            OFF -> ZoneTone.REST
        }
}

/** Тон зоны на экране: покой, активно, тревога, зона датчика отключена. */
enum class ZoneTone { REST, ACTIVE, ALARM, MUTED }

data class EngineInfo(
    val status: EngineStatus,
    /** Температура двигателя, °C. */
    val temperature: Int,
    /** Обороты; `null` — двигатель не запущен или блок их не знает. */
    val rpm: Int?,
    val autostart: Autostart,
)

enum class EngineStatus { STOPPED, READY, RUNNING }

sealed interface Autostart {
    data object Disabled : Autostart

    /** Включён в настройках, двигатель не запущен. */
    data object Enabled : Autostart

    /** Двигатель запущен автозапуском; до остановки [secondsLeft]. */
    data class Running(val secondsLeft: Int) : Autostart
}

data class PowerInfo(
    /** Напряжение бортовой сети, В. */
    val voltage: Float,
    val cabinTemp: Int,
    val outsideTemp: Int,
    /** Топливо, %; `null` — нет данных. */
    val fuelPercent: Int?,
)

data class LinkInfo(
    /** Уровень GSM 0–3; `null` — GSM выключен. */
    val gsmLevel: Int?,
    val gpsFix: Boolean,
    /** Скорость по GPS, км/ч; `null` — нет фиксации. */
    val speedKmh: Int?,
    val sms: Boolean,
    val calls: Boolean,
)

data class CanInfo(
    val mileageKm: Long?,
    val rangeKm: Int?,
    val consumption: Int?,
    val washerLow: Boolean,
    val windowsOpen: Set<Window>,
    val tires: TirePressure?,
)

enum class Window { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT }

/** Давление в шинах, бар. */
data class TirePressure(
    val frontLeft: Float,
    val frontRight: Float,
    val rearLeft: Float,
    val rearRight: Float,
    val spare: Float?,
)

data class EvInfo(
    val charging: EvCharging,
    val socPercent: Int,
    val sohPercent: Int,
    val batteryTemp: Int,
    val rangeKm: Int?,
)

enum class EvCharging { NONE, CONNECTED, SLOW, FAST }

data class HeaterInfo(
    val on: Boolean,
    val temperature: Int,
    val voltage: Float,
    val powerPercent: Int,
    val hasErrors: Boolean,
)

data class CameraInfo(val recording: Boolean, val minutes: Int?)

enum class SirenMode { OFF, SIGNALS, WARNINGS }

enum class HandsFreeMode { OFF, ARMING, DISARMING, BOTH }

/** Последнее событие блока (основные типы; прочие — `null`, строка события не показывается). */
enum class DeviceEventKind {
    ARMING,
    DISARMING,
    ALARM,
    ENGINE_START,
    ENGINE_STOP,
    ENGINE_START_FAILED,
    AUTOSTART_EXTENDED,
    SERVICE_MODE_ON,
    SERVICE_MODE_OFF,
    PROGRAM_MODE_ON,
    TRUNK_OPEN,
    HEATER_ON,
    HEATER_OFF,
    LOW_VOLTAGE,
    POWER_OFF,
    SETTINGS_CHANGED,
    CAR_STATE,
}
