package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_format_gsm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_autostart
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_cabin
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_engine
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_fuel
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_gps
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_gsm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_network
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_outside
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_label_rpm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_value_no_gps
import ru.alarmtrade.specmp.core.localization.resources.devicestate_metric_value_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_pult_btn_all_commands
import ru.alarmtrade.specmp.core.localization.resources.devicestate_pult_format_hold
import ru.alarmtrade.specmp.core.localization.resources.devicestate_pult_msg_all_ok
import ru.alarmtrade.specmp.core.localization.resources.devicestate_pult_msg_hold_hint
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_celsius
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_kmh
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_percent
import ru.alarmtrade.specmp.core.localization.resources.devicestate_unit_format_volt
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_format_chip
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateState
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CommandUi
import specmp.feature.device.state.generated.resources.Res
import specmp.feature.device.state.generated.resources.devicestate_ic_autostart
import specmp.feature.device.state.generated.resources.devicestate_ic_cabin
import specmp.feature.device.state.generated.resources.devicestate_ic_engine
import specmp.feature.device.state.generated.resources.devicestate_ic_fuel
import specmp.feature.device.state.generated.resources.devicestate_ic_gps
import specmp.feature.device.state.generated.resources.devicestate_ic_network_0
import specmp.feature.device.state.generated.resources.devicestate_ic_network_1
import specmp.feature.device.state.generated.resources.devicestate_ic_network_2
import specmp.feature.device.state.generated.resources.devicestate_ic_network_3
import specmp.feature.device.state.generated.resources.devicestate_ic_network_off
import specmp.feature.device.state.generated.resources.devicestate_ic_out_temp
import specmp.feature.device.state.generated.resources.devicestate_ic_speed
import specmp.feature.device.state.generated.resources.devicestate_ic_voltage
import specmp.feature.device.state.generated.resources.devicestate_vehicle_alarm_off
import specmp.feature.device.state.generated.resources.devicestate_vehicle_alarm_on
import specmp.feature.device.state.generated.resources.devicestate_vehicle_angle_sensor
import specmp.feature.device.state.generated.resources.devicestate_vehicle_autostart_rotate_1
import specmp.feature.device.state.generated.resources.devicestate_vehicle_autostart_rotate_2
import specmp.feature.device.state.generated.resources.devicestate_vehicle_body
import specmp.feature.device.state.generated.resources.devicestate_vehicle_bottom_left_close
import specmp.feature.device.state.generated.resources.devicestate_vehicle_bottom_left_open
import specmp.feature.device.state.generated.resources.devicestate_vehicle_bottom_right_close
import specmp.feature.device.state.generated.resources.devicestate_vehicle_bottom_right_open
import specmp.feature.device.state.generated.resources.devicestate_vehicle_brake
import specmp.feature.device.state.generated.resources.devicestate_vehicle_ext_sensor_alert_zone
import specmp.feature.device.state.generated.resources.devicestate_vehicle_ext_sensor_alert_zone_off
import specmp.feature.device.state.generated.resources.devicestate_vehicle_ext_sensor_main_zone
import specmp.feature.device.state.generated.resources.devicestate_vehicle_ext_sensor_main_zone_off
import specmp.feature.device.state.generated.resources.devicestate_vehicle_heater
import specmp.feature.device.state.generated.resources.devicestate_vehicle_hood
import specmp.feature.device.state.generated.resources.devicestate_vehicle_ignition
import specmp.feature.device.state.generated.resources.devicestate_vehicle_light
import specmp.feature.device.state.generated.resources.devicestate_vehicle_moving_sens
import specmp.feature.device.state.generated.resources.devicestate_vehicle_parking
import specmp.feature.device.state.generated.resources.devicestate_vehicle_progm_mode
import specmp.feature.device.state.generated.resources.devicestate_vehicle_sensor_alert_zone
import specmp.feature.device.state.generated.resources.devicestate_vehicle_sensor_alert_zone_off
import specmp.feature.device.state.generated.resources.devicestate_vehicle_sensor_main_zone
import specmp.feature.device.state.generated.resources.devicestate_vehicle_sensor_main_zone_off
import specmp.feature.device.state.generated.resources.devicestate_vehicle_service_mode
import specmp.feature.device.state.generated.resources.devicestate_vehicle_top_left_close
import specmp.feature.device.state.generated.resources.devicestate_vehicle_top_left_open
import specmp.feature.device.state.generated.resources.devicestate_vehicle_top_right_close
import specmp.feature.device.state.generated.resources.devicestate_vehicle_top_right_open
import specmp.feature.device.state.generated.resources.devicestate_vehicle_trunk

private const val VEHICLE_RATIO = 522f / 1000f
private const val AUTOSTART_FRAME_MILLIS = 500
private const val LOADING_ALPHA = 0.3f
private const val SKELETON_METRICS = 4
private const val CHIPS_SKELETON_FRACTION = 0.7f
private val CommandsColumnWidth = 120.dp
private val MetricMinWidth = 88.dp
private val ChipsRowHeight = 40.dp
private val ProgressHeight = 6.dp

@Composable
internal fun PultCard(
    state: CheckStateState,
    carWidth: Dp,
    onFire: (CommandUi) -> Unit,
    onAllCommands: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vehicle = state.vehicle
    val hold = remember { HoldState() }
    val progress = holdProgress(hold, HOLD_MILLIS_PULT)
    SectionCard(modifier = modifier) {
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            if (vehicle == null) {
                CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    repeat(SKELETON_METRICS) {
                        CmpSkeleton(
                            modifier = Modifier.width(MetricMinWidth).height(CmpSize.iconXxl),
                            shape = RoundedCornerShape(CmpRadius.cardSmall),
                        )
                    }
                }
            } else {
                MetricStrip(vehicle)
            }
            CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                CmpBox(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    CmpBox(modifier = Modifier.width(carWidth)) {
                        VehicleSchematic(
                            vehicle = vehicle,
                            modifier = Modifier.fillMaxWidth().alpha(if (vehicle == null) LOADING_ALPHA else 1f),
                        )
                        HoldOverlay(hold = hold, progress = progress)
                    }
                }
                CmpColumn(
                    modifier = Modifier.width(CommandsColumnWidth),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    PrimaryCommands(
                        commands = state.commands,
                        status = { state.status(it.command) },
                        enabled = !state.loading && state.pending == null,
                        hold = hold,
                        onFire = onFire,
                    )
                    CmpButton(
                        text = stringResource(Strings.string.devicestate_pult_btn_all_commands),
                        onClick = onAllCommands,
                        type = CmpButtonType.Tertiary,
                        size = CmpButtonSize.S,
                        fullWidth = true,
                        maxLines = 2,
                        uppercase = false,
                    )
                }
            }
            if (vehicle == null) {
                CmpSkeleton(
                    modifier = Modifier.fillMaxWidth(CHIPS_SKELETON_FRACTION).height(CmpSize.tag),
                    shape = RoundedCornerShape(CmpRadius.tags),
                )
            } else {
                ActiveZones(vehicle = vehicle, changed = state.changedZones, tick = state.tick)
            }
        }
    }
}

@Composable
private fun BoxScope.HoldOverlay(hold: HoldState, progress: Float) {
    val command = hold.command
    if (command == null || (!hold.pressed && !hold.hint)) return
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = Modifier
            .matchParentSize()
            .background(
                colors.backgroundPagePrimary.copy(alpha = OVERLAY_ALPHA),
                RoundedCornerShape(CmpRadius.cardMedium),
            )
            .padding(CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpText(
            text = if (hold.pressed) {
                stringResource(Strings.string.devicestate_pult_format_hold, stringResource(command.label))
            } else {
                stringResource(Strings.string.devicestate_pult_msg_hold_hint)
            },
            style = CmpTextStyle.Headline3,
            color = colors.foregroundOnDarkPrimaryDefault,
            textAlign = TextAlign.Center,
        )
        if (hold.pressed) {
            CmpLinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(ProgressHeight))
        }
    }
}

private const val OVERLAY_ALPHA = 0.88f

private data class StripItem(
    val icon: DrawableResource,
    val value: String,
    val label: String,
    val highlighted: Boolean = false,
)

@Composable
private fun stripItems(vehicle: VehicleState): List<StripItem> {
    val engine = vehicle.engine
    val power = vehicle.power
    val link = vehicle.link
    val autostart = engine.autostart as? Autostart.Running
    return buildList {
        if (autostart != null) {
            add(
                StripItem(
                    icon = Res.drawable.devicestate_ic_autostart,
                    value = formatMinutesSeconds(autostart.secondsLeft),
                    label = stringResource(Strings.string.devicestate_metric_label_autostart),
                    highlighted = true,
                ),
            )
        }
        engine.rpm?.let {
            add(
                StripItem(
                    Res.drawable.devicestate_ic_speed,
                    it.toString(),
                    stringResource(Strings.string.devicestate_metric_label_rpm),
                ),
            )
        }
        add(
            StripItem(
                Res.drawable.devicestate_ic_voltage,
                stringResource(Strings.string.devicestate_unit_format_volt, formatOneDecimal(power.voltage)),
                stringResource(Strings.string.devicestate_metric_label_network),
            ),
        )
        add(
            StripItem(
                Res.drawable.devicestate_ic_engine,
                stringResource(Strings.string.devicestate_unit_format_celsius, engine.temperature),
                stringResource(Strings.string.devicestate_metric_label_engine),
            ),
        )
        add(
            StripItem(
                Res.drawable.devicestate_ic_cabin,
                stringResource(Strings.string.devicestate_unit_format_celsius, power.cabinTemp),
                stringResource(Strings.string.devicestate_metric_label_cabin),
            ),
        )
        add(
            StripItem(
                Res.drawable.devicestate_ic_out_temp,
                stringResource(Strings.string.devicestate_unit_format_celsius, power.outsideTemp),
                stringResource(Strings.string.devicestate_metric_label_outside),
            ),
        )
        power.fuelPercent?.let {
            add(
                StripItem(
                    Res.drawable.devicestate_ic_fuel,
                    stringResource(Strings.string.devicestate_unit_format_percent, it),
                    stringResource(Strings.string.devicestate_metric_label_fuel),
                ),
            )
        }
        val gsm = link.gsmLevel
        add(
            StripItem(
                icon = gsmIcon(gsm),
                value = if (gsm == null) {
                    stringResource(Strings.string.devicestate_metric_value_off)
                } else {
                    stringResource(Strings.string.devicestate_metric_format_gsm, gsm)
                },
                label = stringResource(Strings.string.devicestate_metric_label_gsm),
            ),
        )
        add(
            StripItem(
                icon = Res.drawable.devicestate_ic_gps,
                value = link.speedKmh?.let { stringResource(Strings.string.devicestate_unit_format_kmh, it) }
                    ?: stringResource(Strings.string.devicestate_metric_value_no_gps),
                label = stringResource(Strings.string.devicestate_metric_label_gps),
            ),
        )
    }
}

internal fun gsmIcon(level: Int?): DrawableResource = when (level) {
    null -> Res.drawable.devicestate_ic_network_off
    0 -> Res.drawable.devicestate_ic_network_0
    1 -> Res.drawable.devicestate_ic_network_1
    2 -> Res.drawable.devicestate_ic_network_2
    else -> Res.drawable.devicestate_ic_network_3
}

@Composable
internal fun MetricStrip(vehicle: VehicleState, wrap: Boolean = false) {
    val colors = CmpTheme.colors
    val row: @Composable (@Composable () -> Unit) -> Unit = { items ->
        if (wrap) {
            CmpFlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) { items() }
        } else {
            CmpRow(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) { items() }
        }
    }
    row {
        stripItems(vehicle).forEach { item ->
            val background = if (item.highlighted) colors.brandPrimaryHighlight else colors.backgroundSurfaceHighlight
            val content = colors.foregroundOnDarkPrimaryDefault
            CmpColumn(
                modifier = Modifier
                    .widthIn(min = MetricMinWidth)
                    .background(background, RoundedCornerShape(CmpRadius.cardSmall))
                    .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
            ) {
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    CmpIcon(
                        painter = painterResource(item.icon),
                        contentDescription = null,
                        tint = colors.brandPrimaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                    CmpText(
                        text = item.value,
                        style = CmpTextStyle.Headline2,
                        color = content,
                        maxLines = 1,
                    )
                }
                CmpText(
                    text = item.label,
                    style = CmpTextStyle.Additional1,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun ActiveZones(vehicle: VehicleState, changed: Set<Zone>, tick: Int) {
    val theme = CmpTheme.colors
    val active = Zone.entries.filter { vehicle.zone(it).tone != ZoneTone.REST || it in changed }
    CmpRow(
        modifier = Modifier.fillMaxWidth().height(ChipsRowHeight).horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        if (active.isEmpty()) {
            CmpIcon(
                icon = CmpIcons.CheckCircle,
                contentDescription = null,
                tint = theme.successDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
            CmpText(
                text = stringResource(Strings.string.devicestate_pult_msg_all_ok),
                style = CmpTextStyle.Paragraph2,
                maxLines = 1,
            )
        }
        active.forEach { zone ->
            val tone = vehicle.zone(zone).tone
            CmpBox {
                CmpTag(
                    text = stringResource(
                        Strings.string.devicestate_zones_format_chip,
                        stringResource(zone.label),
                        zoneValueText(zone, vehicle.zone(zone)).lowercase(),
                    ),
                    tone = tone.tagTone(),
                    icon = zone.icon,
                )
                ChangeFlash(
                    on = zone in changed,
                    key = tick,
                    color = if (tone == ZoneTone.ALARM) theme.errorDefault else theme.brandPrimaryDefault,
                    radius = CmpRadius.tags,
                )
            }
        }
    }
}

@Composable
internal fun VehicleSchematic(vehicle: VehicleState?, modifier: Modifier = Modifier) {
    val layers = vehicleLayers(vehicle)
    val running = vehicle?.engine?.autostart is Autostart.Running
    val frame = if (running) autostartFrame() else 0
    CmpBox(modifier = modifier.aspectRatio(VEHICLE_RATIO)) {
        layers.forEach { layer ->
            if (layer.frame == null || layer.frame == frame) {
                CmpIcon(
                    painter = painterResource(layer.drawable),
                    contentDescription = null,
                    tint = layer.color,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

@Composable
private fun autostartFrame(): Int {
    val transition = rememberInfiniteTransition(label = "autostart")
    val value by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            tween(AUTOSTART_FRAME_MILLIS * 2, easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "autostartFrame",
    )
    return value.toInt().coerceIn(0, 1)
}

private data class Layer(val drawable: DrawableResource, val color: Color, val frame: Int? = null)

@Composable
private fun layerColor(tone: ZoneTone): Color {
    val theme = CmpTheme.colors
    return when (tone) {
        ZoneTone.REST -> theme.foregroundOnDarkSecondaryDefault
        ZoneTone.ACTIVE -> theme.brandPrimaryDefault
        ZoneTone.ALARM -> theme.errorDefault
        ZoneTone.MUTED -> theme.foregroundOnDarkTertiary
    }
}

@Composable
private fun vehicleLayers(vehicle: VehicleState?): List<Layer> {
    val rest = layerColor(ZoneTone.REST)
    val doors = listOf(
        Zone.DOOR_FRONT_LEFT to DoorLayers.frontLeft,
        Zone.DOOR_FRONT_RIGHT to DoorLayers.frontRight,
        Zone.DOOR_REAR_LEFT to DoorLayers.rearLeft,
        Zone.DOOR_REAR_RIGHT to DoorLayers.rearRight,
    ).map { (zone, pair) ->
        val tone = vehicle?.zone(zone)?.tone ?: ZoneTone.REST
        Layer(if (tone == ZoneTone.REST) pair.first else pair.second, layerColor(tone))
    }
    val body = Layer(Res.drawable.devicestate_vehicle_body, rest)
    if (vehicle == null) return listOf(body) + doors + Layer(Res.drawable.devicestate_vehicle_alarm_off, rest)
    return listOf(body) + doors + zoneLayers(vehicle) + modeLayers(vehicle)
}

@Composable
private fun zoneLayers(vehicle: VehicleState): List<Layer> {
    val zones = ZoneLayers.mapNotNull { (zone, drawable) ->
        val tone = vehicle.zone(zone).tone
        if (tone == ZoneTone.REST) null else Layer(drawable, layerColor(tone))
    }
    val sensors = listOfNotNull(
        sensorLayer(vehicle.zone(Zone.SHOCK), extra = false),
        sensorLayer(vehicle.zone(Zone.EXTRA_SENSOR), extra = true),
    ).map { (drawable, tone) -> Layer(drawable, layerColor(tone)) }
    return zones + sensors
}

@Composable
private fun modeLayers(vehicle: VehicleState): List<Layer> {
    val theme = CmpTheme.colors
    val rest = layerColor(ZoneTone.REST)
    val active = layerColor(ZoneTone.ACTIVE)
    return buildList {
        if (vehicle.security == SecurityState.DISARMED) {
            add(Layer(Res.drawable.devicestate_vehicle_alarm_off, rest))
        } else {
            add(Layer(Res.drawable.devicestate_vehicle_alarm_on, if (vehicle.alarm) theme.errorDefault else active))
        }
        if (vehicle.serviceMode) add(Layer(Res.drawable.devicestate_vehicle_service_mode, theme.warningDefault))
        if (vehicle.programMode) add(Layer(Res.drawable.devicestate_vehicle_progm_mode, theme.warningDefault))
        if (vehicle.heater?.on == true) add(Layer(Res.drawable.devicestate_vehicle_heater, active))
        if (vehicle.engine.autostart is Autostart.Running) {
            add(Layer(Res.drawable.devicestate_vehicle_autostart_rotate_1, active, frame = 0))
            add(Layer(Res.drawable.devicestate_vehicle_autostart_rotate_2, active, frame = 1))
        }
    }
}

private val ZoneLayers = listOf(
    Zone.HOOD to Res.drawable.devicestate_vehicle_hood,
    Zone.TRUNK to Res.drawable.devicestate_vehicle_trunk,
    Zone.IGNITION to Res.drawable.devicestate_vehicle_ignition,
    Zone.BRAKE to Res.drawable.devicestate_vehicle_brake,
    Zone.NEUTRAL to Res.drawable.devicestate_vehicle_parking,
    Zone.LIGHTS to Res.drawable.devicestate_vehicle_light,
    Zone.MOTION to Res.drawable.devicestate_vehicle_moving_sens,
    Zone.TILT to Res.drawable.devicestate_vehicle_angle_sensor,
)

private fun sensorLayer(value: ZoneValue, extra: Boolean): Pair<DrawableResource, ZoneTone>? {
    val layers = if (extra) ExtraSensorLayers else ShockSensorLayers
    return when (value) {
        ZoneValue.WARNING -> layers.warning to ZoneTone.ACTIVE
        ZoneValue.MAIN -> layers.main to ZoneTone.ALARM
        ZoneValue.WARNING_OFF -> layers.warningOff to ZoneTone.MUTED
        ZoneValue.MAIN_OFF -> layers.mainOff to ZoneTone.MUTED
        else -> null
    }
}

private class SensorLayers(
    val warning: DrawableResource,
    val main: DrawableResource,
    val warningOff: DrawableResource,
    val mainOff: DrawableResource,
)

private val ShockSensorLayers = SensorLayers(
    warning = Res.drawable.devicestate_vehicle_sensor_alert_zone,
    main = Res.drawable.devicestate_vehicle_sensor_main_zone,
    warningOff = Res.drawable.devicestate_vehicle_sensor_alert_zone_off,
    mainOff = Res.drawable.devicestate_vehicle_sensor_main_zone_off,
)

private val ExtraSensorLayers = SensorLayers(
    warning = Res.drawable.devicestate_vehicle_ext_sensor_alert_zone,
    main = Res.drawable.devicestate_vehicle_ext_sensor_main_zone,
    warningOff = Res.drawable.devicestate_vehicle_ext_sensor_alert_zone_off,
    mainOff = Res.drawable.devicestate_vehicle_ext_sensor_main_zone_off,
)

private object DoorLayers {
    val frontLeft = Res.drawable.devicestate_vehicle_top_left_close to Res.drawable.devicestate_vehicle_top_left_open
    val frontRight = Res.drawable.devicestate_vehicle_top_right_close to Res.drawable.devicestate_vehicle_top_right_open
    val rearLeft =
        Res.drawable.devicestate_vehicle_bottom_left_close to Res.drawable.devicestate_vehicle_bottom_left_open
    val rearRight =
        Res.drawable.devicestate_vehicle_bottom_right_close to Res.drawable.devicestate_vehicle_bottom_right_open
}

/**
 * «Живое» состояние без команд: машина сверху, активные зоны, показатели. Левая панель раскладок «две панели» и
 * «три колонки»; [chips] = false — активные зоны показывает соседняя колонка.
 */
@Composable
internal fun LiveCard(state: CheckStateState, carWidth: Dp, modifier: Modifier = Modifier, chips: Boolean = true) {
    val vehicle = state.vehicle
    SectionCard(modifier = modifier) {
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            CmpBox(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                VehicleSchematic(
                    vehicle = vehicle,
                    modifier = Modifier.width(carWidth).alpha(if (vehicle == null) LOADING_ALPHA else 1f),
                )
            }
            if (vehicle == null) {
                CmpSkeleton(
                    modifier = Modifier.fillMaxWidth(CHIPS_SKELETON_FRACTION).height(CmpSize.tag),
                    shape = RoundedCornerShape(CmpRadius.tags),
                )
            } else {
                if (chips) ActiveZones(vehicle = vehicle, changed = state.changedZones, tick = state.tick)
                MetricStrip(vehicle, wrap = true)
            }
        }
    }
}

@Composable
internal fun HorizontalVehicle(vehicle: VehicleState?, height: Dp, modifier: Modifier = Modifier) {
    val width = height / VEHICLE_RATIO
    CmpBox(
        modifier = modifier
            .size(width = width, height = height)
            .layout { measurable, _ ->
                val placeable = measurable.measure(Constraints.fixed(height.roundToPx(), width.roundToPx()))
                layout(width.roundToPx(), height.roundToPx()) {
                    placeable.place(
                        (width.roundToPx() - placeable.width) / 2,
                        (height.roundToPx() - placeable.height) / 2,
                    )
                }
            },
    ) {
        VehicleSchematic(
            vehicle = vehicle,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                rotationZ = QUARTER_TURN
            },
        )
    }
}

private const val QUARTER_TURN = 90f
