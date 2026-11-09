package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_autostart
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_cabin_temp
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_engine
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_engine_temp
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_fuel
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_outside_temp
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_rpm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_label_voltage
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_title_autostart_running
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_title_card
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_value_autostart_enabled
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_value_ready
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_value_running
import ru.alarmtrade.specmp.core.localization.resources.devicestate_engine_value_stopped
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_format_camera_recording
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_format_can_mileage
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_format_can_washer
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_format_ev
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_format_heater_errors
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_format_siren
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_format_windows_open
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_battery_range
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_battery_temp
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_charging
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_consumption
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_errors
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_hands_free
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_mileage
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_power
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_range
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_recorded
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_recording
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_siren
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_soc
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_soh
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_status
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_temperature
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_tire_front_left
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_tire_front_right
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_tire_rear_left
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_tire_rear_right
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_tire_spare
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_voltage
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_washer
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_window_front_left
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_window_front_right
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_window_rear_left
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_label_window_rear_right
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_msg_can
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_camera
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_can
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_ev
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_heater
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_section
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_siren
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_tires
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_title_windows_closed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_charging_connected
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_charging_fast
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_charging_none
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_charging_slow
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_has_errors
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_hf_arming
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_hf_both
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_hf_disarming
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_installed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_no
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_normal
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_recording
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_siren_signals
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_siren_warnings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_washer_low
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_window_closed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_window_open
import ru.alarmtrade.specmp.core.localization.resources.devicestate_extra_value_working
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_format_gps
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_format_gsm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_label_gps
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_label_gsm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_label_sms
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_title_card
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_value_allowed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_value_denied
import ru.alarmtrade.specmp.core.localization.resources.devicestate_link_value_no_fix
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_value_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_celsius
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_consumption
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_km
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_minutes
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_percent
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_volt
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_label_celsius
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_label_percent
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_label_rpm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_label_volt
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_off
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.CameraInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.EvCharging
import ru.alarmtrade.specmp.feature.device.state.domain.model.EvInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.HandsFreeMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.HeaterInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.SirenMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.TirePressure
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Window
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import specmp.feature.device.state.generated.resources.Res
import specmp.feature.device.state.generated.resources.devicestate_ic_engine

private const val EXPANDED_ROTATION = 180f

/** Автозапуск по умолчанию в SDK длится 10 минут — шкала отсчёта (точной длительности блок не сообщает). */
private const val AUTOSTART_SCALE_SECONDS = 600

@Composable
internal fun EngineCard(vehicle: VehicleState, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    val engine = vehicle.engine
    val power = vehicle.power
    val running = engine.status != EngineStatus.STOPPED
    SectionCard(modifier = modifier, title = stringResource(Strings.string.devicestate_engine_title_card)) {
        CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CmpBox(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (running) colors.primary else colors.surfaceContainerHighest, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        painter = painterResource(Res.drawable.devicestate_ic_engine),
                        contentDescription = null,
                        tint = if (running) colors.onPrimary else colors.onSurfaceVariant,
                        modifier = Modifier.size(26.dp),
                    )
                }
                CmpText(
                    text = stringResource(Strings.string.devicestate_engine_label_engine),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                ValuePill(
                    text = stringResource(
                        when (engine.status) {
                            EngineStatus.STOPPED -> Strings.string.devicestate_engine_value_stopped
                            EngineStatus.RUNNING -> Strings.string.devicestate_engine_value_running
                            EngineStatus.READY -> Strings.string.devicestate_engine_value_ready
                        },
                    ),
                    tone = if (running) ZoneTone.ACTIVE else ZoneTone.REST,
                )
            }
            AutostartBlock(engine.autostart)
            val metrics = buildList {
                engine.rpm?.let {
                    add(
                        Triple(
                            Strings.string.devicestate_engine_label_rpm,
                            it.toString(),
                            Strings.string.devicestate_unit_label_rpm,
                        ),
                    )
                }
                add(
                    Triple(
                        Strings.string.devicestate_engine_label_voltage,
                        formatOneDecimal(power.voltage),
                        Strings.string.devicestate_unit_label_volt,
                    ),
                )
                add(
                    Triple(
                        Strings.string.devicestate_engine_label_engine_temp,
                        engine.temperature.toString(),
                        Strings.string.devicestate_unit_label_celsius,
                    ),
                )
                add(
                    Triple(
                        Strings.string.devicestate_engine_label_cabin_temp,
                        power.cabinTemp.toString(),
                        Strings.string.devicestate_unit_label_celsius,
                    ),
                )
                add(
                    Triple(
                        Strings.string.devicestate_engine_label_outside_temp,
                        power.outsideTemp.toString(),
                        Strings.string.devicestate_unit_label_celsius,
                    ),
                )
                power.fuelPercent?.let {
                    add(
                        Triple(
                            Strings.string.devicestate_engine_label_fuel,
                            it.toString(),
                            Strings.string.devicestate_unit_label_percent,
                        ),
                    )
                }
            }
            metrics.chunked(2).forEach { row ->
                CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (label, value, unit) ->
                        MetricTile(
                            label = stringResource(label),
                            value = value,
                            unit = stringResource(unit),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) CmpBox(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AutostartBlock(autostart: Autostart) {
    val colors = cmpThemeColors()
    when (autostart) {
        is Autostart.Running -> CmpColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primaryContainer, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CmpIcon(
                    icon = CmpIcons.Autorenew,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
                CmpText(
                    text = stringResource(Strings.string.devicestate_engine_title_autostart_running),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
            }
            CmpText(
                text = formatMinutesSeconds(autostart.secondsLeft),
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onPrimaryContainer,
            )
            CmpLinearProgressIndicator(
                progress = (autostart.secondsLeft.toFloat() / AUTOSTART_SCALE_SECONDS).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
        }

        Autostart.Enabled -> KeyValueRow(
            label = stringResource(Strings.string.devicestate_engine_label_autostart),
            value = stringResource(Strings.string.devicestate_engine_value_autostart_enabled),
        )

        Autostart.Disabled -> KeyValueRow(
            label = stringResource(Strings.string.devicestate_engine_label_autostart),
            value = stringResource(Strings.string.devicestate_zone_value_off),
        )
    }
}

@Composable
internal fun LinkCard(vehicle: VehicleState, modifier: Modifier = Modifier) {
    val link = vehicle.link
    SectionCard(
        modifier = modifier,
        title = stringResource(Strings.string.devicestate_link_title_card),
        contentPadding = 8.dp,
    ) {
        val gsm = link.gsmLevel
        KeyValueRow(
            label = stringResource(Strings.string.devicestate_link_label_gsm),
            value = if (gsm == null) {
                stringResource(Strings.string.devicestate_metric_value_off)
            } else {
                stringResource(Strings.string.devicestate_link_format_gsm, gsm)
            },
            tone = if (gsm == null) ZoneTone.REST else ZoneTone.ACTIVE,
            trailing = gsm?.let { level -> { SignalBars(level) } },
        )
        KeyValueRow(
            label = stringResource(Strings.string.devicestate_link_label_gps),
            value = link.speedKmh?.let { stringResource(Strings.string.devicestate_link_format_gps, it) }
                ?: stringResource(Strings.string.devicestate_link_value_no_fix),
            tone = if (link.gpsFix) ZoneTone.ACTIVE else ZoneTone.REST,
        )
        KeyValueRow(
            label = stringResource(Strings.string.devicestate_link_label_sms),
            value = stringResource(
                if (link.sms || link.calls) {
                    Strings.string.devicestate_link_value_allowed
                } else {
                    Strings.string.devicestate_link_value_denied
                },
            ),
        )
    }
}

private const val SIGNAL_BARS = 3
private const val TIRE_COLUMNS = 3
private val BAR_WIDTH = 6.dp
private val BAR_BASE = 6.dp
private val BAR_STEP = 4.dp

@Composable
private fun SignalBars(level: Int) {
    val colors = cmpThemeColors()
    CmpRow(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (bar in 1..SIGNAL_BARS) {
            CmpBox(
                modifier = Modifier
                    .width(BAR_WIDTH)
                    .height(BAR_BASE + BAR_STEP * bar)
                    .background(if (bar <= level) colors.primary else colors.outlineVariant, RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
internal fun ExtraGroups(vehicle: VehicleState, modifier: Modifier = Modifier) {
    val can = vehicle.can
    val ev = vehicle.ev
    val heater = vehicle.heater
    val camera = vehicle.camera
    val sirenShown = vehicle.siren != SirenMode.OFF || vehicle.handsFree != HandsFreeMode.OFF
    if (listOf(can, ev, heater, camera).all { it == null } && !sirenShown) return
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpText(
            text = stringResource(Strings.string.devicestate_extra_title_section),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp),
        )
        if (can != null) CanGroup(can)
        if (ev != null) EvGroup(ev)
        if (heater != null) HeaterGroup(heater)
        if (camera != null) CameraGroup(camera)
        if (sirenShown) SirenGroup(vehicle)
    }
}

@Composable
private fun EvGroup(ev: EvInfo) {
    val charging = stringResource(ev.charging.text)
    ExtraGroup(
        key = "ev",
        icon = CmpIcons.EvStation,
        title = stringResource(Strings.string.devicestate_extra_title_ev),
        summary = stringResource(
            Strings.string.devicestate_extra_format_ev,
            ev.socPercent,
            charging.lowercase(),
        ),
    ) {
        KeyValueRow(
            label = stringResource(Strings.string.devicestate_extra_label_charging),
            value = charging,
            tone = if (ev.charging == EvCharging.NONE) ZoneTone.REST else ZoneTone.ACTIVE,
        )
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_soc),
            stringResource(Strings.string.devicestate_unit_format_percent, ev.socPercent),
        )
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_soh),
            stringResource(Strings.string.devicestate_unit_format_percent, ev.sohPercent),
        )
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_battery_temp),
            stringResource(Strings.string.devicestate_unit_format_celsius, ev.batteryTemp),
        )
        ev.rangeKm?.let {
            KeyValueRow(
                stringResource(Strings.string.devicestate_extra_label_battery_range),
                stringResource(Strings.string.devicestate_unit_format_km, it.toString()),
            )
        }
    }
}

@Composable
private fun HeaterGroup(heater: HeaterInfo) {
    val status = stringResource(
        if (heater.on) {
            Strings.string.devicestate_extra_value_working
        } else {
            Strings.string.devicestate_extra_value_off
        },
    )
    ExtraGroup(
        key = "heater",
        icon = CmpIcons.LocalFireDepartment,
        title = stringResource(Strings.string.devicestate_extra_title_heater),
        summary = if (heater.hasErrors) {
            stringResource(
                Strings.string.devicestate_extra_format_heater_errors,
                status,
            )
        } else {
            status
        },
    ) {
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_status),
            status,
            tone = if (heater.on) ZoneTone.ACTIVE else ZoneTone.REST,
        )
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_temperature),
            stringResource(Strings.string.devicestate_unit_format_celsius, heater.temperature),
        )
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_voltage),
            stringResource(Strings.string.devicestate_unit_format_volt, formatOneDecimal(heater.voltage)),
        )
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_power),
            stringResource(Strings.string.devicestate_unit_format_percent, heater.powerPercent),
        )
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_errors),
            stringResource(
                if (heater.hasErrors) {
                    Strings.string.devicestate_extra_value_has_errors
                } else {
                    Strings.string.devicestate_extra_value_no
                },
            ),
            tone = if (heater.hasErrors) ZoneTone.ALARM else ZoneTone.REST,
        )
    }
}

@Composable
private fun CameraGroup(camera: CameraInfo) {
    val minutes = camera.minutes
    ExtraGroup(
        key = "camera",
        icon = CmpIcons.Videocam,
        title = stringResource(Strings.string.devicestate_extra_title_camera),
        summary = if (camera.recording && minutes != null) {
            stringResource(Strings.string.devicestate_extra_format_camera_recording, minutes)
        } else {
            stringResource(Strings.string.devicestate_extra_value_installed)
        },
    ) {
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_recording),
            stringResource(
                if (camera.recording) {
                    Strings.string.devicestate_extra_value_recording
                } else {
                    Strings.string.devicestate_extra_value_no
                },
            ),
            tone = if (camera.recording) ZoneTone.ACTIVE else ZoneTone.REST,
        )
        minutes?.let {
            KeyValueRow(
                stringResource(Strings.string.devicestate_extra_label_recorded),
                stringResource(Strings.string.devicestate_unit_format_minutes, it),
            )
        }
    }
}

@Composable
private fun SirenGroup(vehicle: VehicleState) {
    val siren = stringResource(vehicle.siren.text)
    val handsFree = stringResource(vehicle.handsFree.text)
    ExtraGroup(
        key = "siren",
        icon = CmpIcons.VolumeUp,
        title = stringResource(Strings.string.devicestate_extra_title_siren),
        summary = stringResource(Strings.string.devicestate_extra_format_siren, siren, handsFree),
    ) {
        KeyValueRow(stringResource(Strings.string.devicestate_extra_label_siren), siren)
        KeyValueRow(stringResource(Strings.string.devicestate_extra_label_hands_free), handsFree)
    }
}

@Composable
private fun CanGroup(can: CanInfo) {
    val mileage =
        can.mileageKm?.let { stringResource(Strings.string.devicestate_extra_format_can_mileage, it.toString()) }
            ?: stringResource(Strings.string.devicestate_extra_msg_can)
    ExtraGroup(
        key = "can",
        icon = CmpIcons.Cable,
        title = stringResource(Strings.string.devicestate_extra_title_can),
        summary = if (can.washerLow) {
            stringResource(
                Strings.string.devicestate_extra_format_can_washer,
                mileage,
            )
        } else {
            mileage
        },
    ) {
        can.mileageKm?.let {
            KeyValueRow(
                stringResource(Strings.string.devicestate_extra_label_mileage),
                stringResource(Strings.string.devicestate_unit_format_km, it.toString()),
            )
        }
        can.rangeKm?.let {
            KeyValueRow(
                stringResource(Strings.string.devicestate_extra_label_range),
                stringResource(Strings.string.devicestate_unit_format_km, it.toString()),
            )
        }
        can.consumption?.let {
            KeyValueRow(
                stringResource(Strings.string.devicestate_extra_label_consumption),
                stringResource(Strings.string.devicestate_unit_format_consumption, it),
            )
        }
        KeyValueRow(
            stringResource(Strings.string.devicestate_extra_label_washer),
            stringResource(
                if (can.washerLow) {
                    Strings.string.devicestate_extra_value_washer_low
                } else {
                    Strings.string.devicestate_extra_value_normal
                },
            ),
            tone = if (can.washerLow) ZoneTone.ALARM else ZoneTone.REST,
        )
        WindowsBlock(can)
        can.tires?.let { TiresBlock(it) }
    }
}

@Composable
private fun WindowsBlock(can: CanInfo) {
    val colors = cmpThemeColors()
    CmpText(
        text = if (can.windowsOpen.isEmpty()) {
            stringResource(Strings.string.devicestate_extra_title_windows_closed)
        } else {
            stringResource(Strings.string.devicestate_extra_format_windows_open, can.windowsOpen.size)
        },
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
    )
    Window.entries.chunked(2).forEach { row ->
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { window ->
                val open = window in can.windowsOpen
                CmpColumn(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .background(
                            if (open) colors.primaryContainer else colors.surfaceContainerHigh,
                            RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    CmpText(
                        text = stringResource(window.label),
                        fontSize = 14.sp,
                        color = if (open) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    )
                    CmpText(
                        text = stringResource(
                            if (open) {
                                Strings.string.devicestate_extra_value_window_open
                            } else {
                                Strings.string.devicestate_extra_value_window_closed
                            },
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (open) colors.primary else colors.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun TiresBlock(tires: TirePressure) {
    CmpText(
        text = stringResource(Strings.string.devicestate_extra_title_tires),
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
    )
    val values = listOfNotNull(
        Strings.string.devicestate_extra_label_tire_front_left to tires.frontLeft,
        Strings.string.devicestate_extra_label_tire_front_right to tires.frontRight,
        Strings.string.devicestate_extra_label_tire_rear_left to tires.rearLeft,
        Strings.string.devicestate_extra_label_tire_rear_right to tires.rearRight,
        tires.spare?.let { Strings.string.devicestate_extra_label_tire_spare to it },
    )
    values.chunked(TIRE_COLUMNS).forEach { row ->
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { (label, value) ->
                MetricTile(
                    label = stringResource(label),
                    value = formatOneDecimal(value),
                    unit = null,
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(TIRE_COLUMNS - row.size) { CmpBox(modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun ExtraGroup(key: String, icon: CmpIcons, title: String, summary: String, content: @Composable () -> Unit) {
    val colors = cmpThemeColors()
    var open by rememberSaveable(key) { mutableStateOf(false) }
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 68.dp)
                    .clickable { open = !open }
                    .padding(start = 14.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CmpBox(
                    modifier = Modifier.size(44.dp).background(colors.secondaryContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        icon = icon,
                        contentDescription = null,
                        tint = colors.onSecondaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
                CmpColumn(modifier = Modifier.weight(1f)) {
                    CmpText(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    CmpText(
                        text = summary,
                        fontSize = 14.sp,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                CmpIcon(
                    icon = CmpIcons.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(28.dp).rotate(if (open) EXPANDED_ROTATION else 0f),
                )
            }
            if (open) {
                CmpColumn(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)) { content() }
            }
        }
    }
}

private val EvCharging.text
    get() = when (this) {
        EvCharging.NONE -> Strings.string.devicestate_extra_value_charging_none
        EvCharging.CONNECTED -> Strings.string.devicestate_extra_value_charging_connected
        EvCharging.SLOW -> Strings.string.devicestate_extra_value_charging_slow
        EvCharging.FAST -> Strings.string.devicestate_extra_value_charging_fast
    }

private val SirenMode.text
    get() = when (this) {
        SirenMode.OFF -> Strings.string.devicestate_extra_value_off
        SirenMode.SIGNALS -> Strings.string.devicestate_extra_value_siren_signals
        SirenMode.WARNINGS -> Strings.string.devicestate_extra_value_siren_warnings
    }

private val HandsFreeMode.text
    get() = when (this) {
        HandsFreeMode.OFF -> Strings.string.devicestate_extra_value_off
        HandsFreeMode.ARMING -> Strings.string.devicestate_extra_value_hf_arming
        HandsFreeMode.DISARMING -> Strings.string.devicestate_extra_value_hf_disarming
        HandsFreeMode.BOTH -> Strings.string.devicestate_extra_value_hf_both
    }

private val Window.label
    get() = when (this) {
        Window.FRONT_LEFT -> Strings.string.devicestate_extra_label_window_front_left
        Window.FRONT_RIGHT -> Strings.string.devicestate_extra_label_window_front_right
        Window.REAR_LEFT -> Strings.string.devicestate_extra_label_window_rear_left
        Window.REAR_RIGHT -> Strings.string.devicestate_extra_label_window_rear_right
    }
