package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_format_done
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_format_no_link
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_format_no_reply
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_format_rejected
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_arm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_disarm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_extend
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_heater_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_heater_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_light
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_program_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_program_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_service_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_service_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_start
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_stop
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_item_trunk
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_status_done
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_status_no_link
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_status_no_reply
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_status_rejected
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_value_short_arm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_value_short_disarm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_value_short_extend
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_value_short_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_value_short_start
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_value_short_stop
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_value_short_trunk
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_alarm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_arming
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_autostart_extended
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_car_state
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_disarming
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_engine_start
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_engine_start_failed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_engine_stop
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_heater_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_heater_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_low_voltage
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_power_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_program_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_service_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_service_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_settings_changed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_event_value_trunk_open
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_status_active_armed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_status_armed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_status_disarmed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_brake
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_door_front_left
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_door_front_right
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_door_rear_left
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_door_rear_right
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_extra
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_handbrake
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_hood
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_ignition
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_lights
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_motion
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_neutral
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_shock
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_tilt
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_trunk
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_alarm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_closed_f
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_closed_m
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_main
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_main_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_normal
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_open_f
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_open_m
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_pressed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_released
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_triggered
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_warning
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_warning_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_title_body
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_title_controls
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_title_sensors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.device.state.domain.model.DeviceEventKind
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneGroup
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneKind
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CommandOutcome

internal val Zone.label: StringResource
    get() = when (this) {
        Zone.DOOR_FRONT_LEFT -> Strings.string.devicestate_zone_label_door_front_left
        Zone.DOOR_FRONT_RIGHT -> Strings.string.devicestate_zone_label_door_front_right
        Zone.DOOR_REAR_LEFT -> Strings.string.devicestate_zone_label_door_rear_left
        Zone.DOOR_REAR_RIGHT -> Strings.string.devicestate_zone_label_door_rear_right
        Zone.HOOD -> Strings.string.devicestate_zone_label_hood
        Zone.TRUNK -> Strings.string.devicestate_zone_label_trunk
        Zone.IGNITION -> Strings.string.devicestate_zone_label_ignition
        Zone.BRAKE -> Strings.string.devicestate_zone_label_brake
        Zone.HANDBRAKE -> Strings.string.devicestate_zone_label_handbrake
        Zone.NEUTRAL -> Strings.string.devicestate_zone_label_neutral
        Zone.LIGHTS -> Strings.string.devicestate_zone_label_lights
        Zone.SHOCK -> Strings.string.devicestate_zone_label_shock
        Zone.EXTRA_SENSOR -> Strings.string.devicestate_zone_label_extra
        Zone.MOTION -> Strings.string.devicestate_zone_label_motion
        Zone.TILT -> Strings.string.devicestate_zone_label_tilt
    }

internal val Zone.icon: CmpIcons
    get() = when (this) {
        Zone.DOOR_FRONT_LEFT, Zone.DOOR_FRONT_RIGHT, Zone.DOOR_REAR_LEFT, Zone.DOOR_REAR_RIGHT -> CmpIcons.SensorDoor
        Zone.HOOD -> CmpIcons.DirectionsCar
        Zone.TRUNK -> CmpIcons.Luggage
        Zone.IGNITION -> CmpIcons.Key
        Zone.BRAKE -> CmpIcons.PauseCircle
        Zone.HANDBRAKE -> CmpIcons.PanTool
        Zone.NEUTRAL -> CmpIcons.LocalParking
        Zone.LIGHTS -> CmpIcons.Highlight
        Zone.SHOCK -> CmpIcons.Vibration
        Zone.EXTRA_SENSOR -> CmpIcons.Sensors
        Zone.MOTION -> CmpIcons.DirectionsWalk
        Zone.TILT -> CmpIcons.ScreenRotation
    }

@Suppress("CyclomaticComplexMethod")
internal fun zoneValueRes(kind: ZoneKind, value: ZoneValue): StringResource = when (value) {
    ZoneValue.ALARM -> Strings.string.devicestate_zone_value_alarm

    ZoneValue.WARNING -> Strings.string.devicestate_zone_value_warning

    ZoneValue.MAIN -> Strings.string.devicestate_zone_value_main

    ZoneValue.WARNING_OFF -> Strings.string.devicestate_zone_value_warning_off

    ZoneValue.MAIN_OFF -> Strings.string.devicestate_zone_value_main_off

    ZoneValue.OFF -> when (kind) {
        ZoneKind.DOOR -> Strings.string.devicestate_zone_value_closed_f
        ZoneKind.LID -> Strings.string.devicestate_zone_value_closed_m
        ZoneKind.SWITCH -> Strings.string.devicestate_zone_value_off
        ZoneKind.PEDAL -> Strings.string.devicestate_zone_value_released
        ZoneKind.SENSOR, ZoneKind.ZONED_SENSOR -> Strings.string.devicestate_zone_value_normal
    }

    ZoneValue.ON -> when (kind) {
        ZoneKind.DOOR -> Strings.string.devicestate_zone_value_open_f
        ZoneKind.LID -> Strings.string.devicestate_zone_value_open_m
        ZoneKind.SWITCH -> Strings.string.devicestate_zone_value_on
        ZoneKind.PEDAL -> Strings.string.devicestate_zone_value_pressed
        ZoneKind.SENSOR, ZoneKind.ZONED_SENSOR -> Strings.string.devicestate_zone_value_triggered
    }
}

@Composable
internal fun zoneValueText(zone: Zone, value: ZoneValue): String = stringResource(zoneValueRes(zone.kind, value))

internal val ZoneGroup.title: StringResource
    get() = when (this) {
        ZoneGroup.BODY -> Strings.string.devicestate_zones_title_body
        ZoneGroup.CONTROLS -> Strings.string.devicestate_zones_title_controls
        ZoneGroup.SENSORS -> Strings.string.devicestate_zones_title_sensors
    }

internal val SecurityState.title: StringResource
    get() = when (this) {
        SecurityState.DISARMED -> Strings.string.devicestate_hero_status_disarmed
        SecurityState.ARMED -> Strings.string.devicestate_hero_status_armed
        SecurityState.ACTIVE_ARMED -> Strings.string.devicestate_hero_status_active_armed
    }

internal val StateCommand.label: StringResource
    get() = when (this) {
        StateCommand.ARM -> Strings.string.devicestate_cmd_item_arm
        StateCommand.DISARM -> Strings.string.devicestate_cmd_item_disarm
        StateCommand.START_ENGINE -> Strings.string.devicestate_cmd_item_start
        StateCommand.EXTEND_AUTOSTART -> Strings.string.devicestate_cmd_item_extend
        StateCommand.STOP_ENGINE -> Strings.string.devicestate_cmd_item_stop
        StateCommand.OPEN_TRUNK -> Strings.string.devicestate_cmd_item_trunk
        StateCommand.HEATER_ON -> Strings.string.devicestate_cmd_item_heater_on
        StateCommand.HEATER_OFF -> Strings.string.devicestate_cmd_item_heater_off
        StateCommand.SERVICE_MODE_ON -> Strings.string.devicestate_cmd_item_service_on
        StateCommand.SERVICE_MODE_OFF -> Strings.string.devicestate_cmd_item_service_off
        StateCommand.PROGRAM_MODE_ON -> Strings.string.devicestate_cmd_item_program_on
        StateCommand.PROGRAM_MODE_OFF -> Strings.string.devicestate_cmd_item_program_off
        StateCommand.PANIC -> Strings.string.devicestate_cmd_item_panic
        StateCommand.LIGHT_WAY -> Strings.string.devicestate_cmd_item_light
    }

internal val StateCommand.shortLabel: StringResource
    get() = when (this) {
        StateCommand.ARM -> Strings.string.devicestate_cmd_value_short_arm
        StateCommand.DISARM -> Strings.string.devicestate_cmd_value_short_disarm
        StateCommand.START_ENGINE -> Strings.string.devicestate_cmd_value_short_start
        StateCommand.EXTEND_AUTOSTART -> Strings.string.devicestate_cmd_value_short_extend
        StateCommand.STOP_ENGINE -> Strings.string.devicestate_cmd_value_short_stop
        StateCommand.OPEN_TRUNK -> Strings.string.devicestate_cmd_value_short_trunk
        StateCommand.PANIC -> Strings.string.devicestate_cmd_value_short_panic
        else -> label
    }

internal val CommandOutcome.status: StringResource
    get() = when (this) {
        CommandOutcome.DONE -> Strings.string.devicestate_cmd_status_done
        CommandOutcome.REJECTED -> Strings.string.devicestate_cmd_status_rejected
        CommandOutcome.NO_REPLY -> Strings.string.devicestate_cmd_status_no_reply
        CommandOutcome.NO_LINK -> Strings.string.devicestate_cmd_status_no_link
    }

internal val CommandOutcome.snackbarFormat: StringResource
    get() = when (this) {
        CommandOutcome.DONE -> Strings.string.devicestate_cmd_format_done
        CommandOutcome.REJECTED -> Strings.string.devicestate_cmd_format_rejected
        CommandOutcome.NO_REPLY -> Strings.string.devicestate_cmd_format_no_reply
        CommandOutcome.NO_LINK -> Strings.string.devicestate_cmd_format_no_link
    }

internal val DeviceEventKind.text: StringResource
    get() = when (this) {
        DeviceEventKind.ARMING -> Strings.string.devicestate_event_value_arming
        DeviceEventKind.DISARMING -> Strings.string.devicestate_event_value_disarming
        DeviceEventKind.ALARM -> Strings.string.devicestate_event_value_alarm
        DeviceEventKind.ENGINE_START -> Strings.string.devicestate_event_value_engine_start
        DeviceEventKind.ENGINE_STOP -> Strings.string.devicestate_event_value_engine_stop
        DeviceEventKind.ENGINE_START_FAILED -> Strings.string.devicestate_event_value_engine_start_failed
        DeviceEventKind.AUTOSTART_EXTENDED -> Strings.string.devicestate_event_value_autostart_extended
        DeviceEventKind.SERVICE_MODE_ON -> Strings.string.devicestate_event_value_service_on
        DeviceEventKind.SERVICE_MODE_OFF -> Strings.string.devicestate_event_value_service_off
        DeviceEventKind.PROGRAM_MODE_ON -> Strings.string.devicestate_event_value_program_on
        DeviceEventKind.TRUNK_OPEN -> Strings.string.devicestate_event_value_trunk_open
        DeviceEventKind.HEATER_ON -> Strings.string.devicestate_event_value_heater_on
        DeviceEventKind.HEATER_OFF -> Strings.string.devicestate_event_value_heater_off
        DeviceEventKind.LOW_VOLTAGE -> Strings.string.devicestate_event_value_low_voltage
        DeviceEventKind.POWER_OFF -> Strings.string.devicestate_event_value_power_off
        DeviceEventKind.SETTINGS_CHANGED -> Strings.string.devicestate_event_value_settings_changed
        DeviceEventKind.CAR_STATE -> Strings.string.devicestate_event_value_car_state
    }

internal fun formatMinutesSeconds(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val minutes = (safe / SECONDS_IN_MINUTE).toString().padStart(2, '0')
    val rest = (safe % SECONDS_IN_MINUTE).toString().padStart(2, '0')
    return "$minutes:$rest"
}

internal fun formatOneDecimal(value: Float): String {
    val tenths = kotlin.math.round(value * TENTHS).toInt()
    val sign = if (tenths < 0) "-" else ""
    val abs = kotlin.math.abs(tenths)
    return "$sign${abs / TENTHS.toInt()}.${abs % TENTHS.toInt()}"
}

private const val SECONDS_IN_MINUTE = 60
private const val TENTHS = 10f
