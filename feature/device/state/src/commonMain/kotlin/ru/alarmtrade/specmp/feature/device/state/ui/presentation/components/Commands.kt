package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_status_hold
import ru.alarmtrade.specmp.core.localization.resources.devicestate_commands_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.devicestate_commands_title_sheet
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_btn_hold_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_btn_hold_start
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_hint_hold
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_msg_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_msg_start
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_title_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_title_start
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CommandOutcome
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CommandStatus
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CommandUi
import specmp.feature.device.state.generated.resources.Res
import specmp.feature.device.state.generated.resources.devicestate_cmd_alarm
import specmp.feature.device.state.generated.resources.devicestate_cmd_beaper
import specmp.feature.device.state.generated.resources.devicestate_cmd_disalarm
import specmp.feature.device.state.generated.resources.devicestate_cmd_engine_prolong
import specmp.feature.device.state.generated.resources.devicestate_cmd_engine_start
import specmp.feature.device.state.generated.resources.devicestate_cmd_engine_stop
import specmp.feature.device.state.generated.resources.devicestate_cmd_heater
import specmp.feature.device.state.generated.resources.devicestate_cmd_heater_stop
import specmp.feature.device.state.generated.resources.devicestate_cmd_light_way
import specmp.feature.device.state.generated.resources.devicestate_cmd_open_trunk
import specmp.feature.device.state.generated.resources.devicestate_cmd_program_mode_disable
import specmp.feature.device.state.generated.resources.devicestate_cmd_program_mode_enable
import specmp.feature.device.state.generated.resources.devicestate_cmd_service_mode_disable
import specmp.feature.device.state.generated.resources.devicestate_cmd_service_mode_enable

internal const val COMMAND_TAG_PREFIX = "devicestate_cmd_"
internal const val HOLD_MILLIS_PULT = 1_000L
internal const val CONFIRM_HOLD_TAG = "devicestate_confirm_hold"
private const val HOLD_MILLIS = 1_000L
private const val CONFIRM_HOLD_MILLIS = 2_000L
private const val HINT_MILLIS = 1_800L

@Stable
internal class HoldState {
    var pressed by mutableStateOf(false)
    var hint by mutableStateOf(false)

    var command by mutableStateOf<StateCommand?>(null)
}

/** Отправка удержанием [millis] — защита от случайного нажатия; для TalkBack — обычное действие «нажать». */
@Composable
internal fun Modifier.holdToFire(
    enabled: Boolean,
    millis: Long,
    label: String,
    state: HoldState,
    owner: StateCommand? = null,
    onFire: () -> Unit,
): Modifier {
    val fire by rememberUpdatedState(onFire)
    return this
        .semantics {
            role = Role.Button
            if (enabled) {
                onClick(label) {
                    fire()
                    true
                }
            } else {
                disabled()
            }
        }
        .pointerInput(enabled, millis, owner) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown()
                state.command = owner
                state.hint = false
                state.pressed = true
                val released = withTimeoutOrNull(millis) {
                    waitForUpOrCancellation()
                    true
                }
                state.pressed = false
                if (released == null) {
                    fire()
                    waitForUpOrCancellation()
                } else {
                    state.hint = true
                }
            }
        }
}

@Composable
internal fun holdProgress(state: HoldState, millis: Long): Float {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(state.pressed) {
        if (state.pressed) progress.animateTo(1f, tween(millis.toInt(), easing = LinearEasing)) else progress.snapTo(0f)
    }
    LaunchedEffect(state.hint) {
        if (state.hint) {
            delay(HINT_MILLIS)
            state.hint = false
        }
    }
    return progress.value
}

@Composable
internal fun commandPainter(command: StateCommand): Painter = painterResource(
    when (command) {
        StateCommand.ARM -> Res.drawable.devicestate_cmd_alarm

        StateCommand.DISARM -> Res.drawable.devicestate_cmd_disalarm

        StateCommand.START_ENGINE -> Res.drawable.devicestate_cmd_engine_start

        StateCommand.EXTEND_AUTOSTART -> Res.drawable.devicestate_cmd_engine_prolong

        StateCommand.STOP_ENGINE -> Res.drawable.devicestate_cmd_engine_stop

        StateCommand.OPEN_TRUNK -> Res.drawable.devicestate_cmd_open_trunk

        StateCommand.HEATER_ON -> Res.drawable.devicestate_cmd_heater

        StateCommand.HEATER_OFF -> Res.drawable.devicestate_cmd_heater_stop

        StateCommand.SERVICE_MODE_ON,
        StateCommand.SERVICE_MODE_OFF,
        StateCommand.PROGRAM_MODE_ON,
        StateCommand.PROGRAM_MODE_OFF,
        -> modeIcon(command)

        StateCommand.PANIC -> Res.drawable.devicestate_cmd_beaper

        StateCommand.LIGHT_WAY -> Res.drawable.devicestate_cmd_light_way
    },
)

private fun modeIcon(command: StateCommand): DrawableResource = when (command) {
    StateCommand.SERVICE_MODE_OFF -> Res.drawable.devicestate_cmd_service_mode_disable
    StateCommand.PROGRAM_MODE_ON -> Res.drawable.devicestate_cmd_program_mode_enable
    StateCommand.PROGRAM_MODE_OFF -> Res.drawable.devicestate_cmd_program_mode_disable
    else -> Res.drawable.devicestate_cmd_service_mode_enable
}

@Composable
private fun RingIcon(command: StateCommand, progress: Float, sending: Boolean, tint: Color, size: Int, iconSize: Int) {
    CmpBox(modifier = Modifier.size(size.dp), contentAlignment = Alignment.Center) {
        when {
            sending -> CmpCircularProgressIndicator(modifier = Modifier.size(size.dp), strokeWidth = 3.dp, color = tint)

            progress > 0f -> CmpCircularProgressIndicator(
                modifier = Modifier.size(size.dp),
                progress = progress,
                strokeWidth = 3.dp,
                color = tint,
            )
        }
        CmpIcon(
            painter = commandPainter(command),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize.dp),
        )
    }
}

private data class CommandLook(val background: Color, val content: Color, val icon: Color)

@Composable
private fun commandLook(command: CommandUi, status: CommandStatus): CommandLook {
    val colors = cmpThemeColors()
    return when ((status as? CommandStatus.Finished)?.outcome) {
        CommandOutcome.DONE -> CommandLook(
            colors.primaryContainer,
            colors.onPrimaryContainer,
            colors.onPrimaryContainer,
        )

        null -> CommandLook(
            background = colors.surfaceContainerHigh,
            content = colors.onSurface,
            icon = if (command.danger) colors.error else colors.primary,
        )

        else -> CommandLook(colors.errorContainer, colors.onErrorContainer, colors.onErrorContainer)
    }
}

/** [sharedHold] — удержание общее для колонки пульта: прогресс рисует силуэт машины, а не кольцо у значка. */
@Composable
internal fun CommandButton(
    command: CommandUi,
    status: CommandStatus,
    enabled: Boolean,
    onFire: (CommandUi) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    sharedHold: HoldState? = null,
) {
    val ownHold = remember(command.command) { HoldState() }
    val hold = sharedHold ?: ownHold
    val progress = if (sharedHold == null) holdProgress(ownHold, HOLD_MILLIS) else 0f
    val sending = status == CommandStatus.Sending
    val look = commandLook(command, status)
    val label = stringResource(if (compact) command.command.shortLabel else command.command.label)
    val result = (status as? CommandStatus.Finished)?.outcome?.let { stringResource(it.status) }
    val active = enabled && !sending
    val gesture = if (command.danger) {
        Modifier.dangerTap(active, label) { onFire(command) }
    } else {
        Modifier.holdToFire(active, HOLD_MILLIS, label, hold, owner = command.command) { onFire(command) }
    }
    CmpColumn(
        modifier = modifier
            .height(if (compact) CompactHeight else TileHeight)
            .background(look.background, RoundedCornerShape(16.dp))
            .then(gesture)
            .semantics { if (result != null) stateDescription = result }
            .testTag(COMMAND_TAG_PREFIX + command.command.name.lowercase())
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        RingIcon(
            command = command.command,
            progress = progress,
            sending = sending,
            tint = if (active) look.icon else look.icon.copy(alpha = DISABLED_ALPHA),
            size = if (compact) 44 else 52,
            iconSize = if (compact) 38 else 44,
        )
        CmpText(
            text = label,
            fontSize = if (compact) 14.sp else 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = look.content,
            textAlign = TextAlign.Center,
            maxLines = if (compact) 1 else 2,
        )
    }
}

private val CompactHeight = 84.dp
private val TileHeight = 104.dp
private const val DISABLED_ALPHA = 0.38f

private fun Modifier.dangerTap(enabled: Boolean, label: String, onClick: () -> Unit): Modifier = this
    .semantics {
        role = Role.Button
        if (enabled) {
            onClick(label) {
                onClick()
                true
            }
        } else {
            disabled()
        }
    }
    .pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            awaitFirstDown()
            if (waitForUpOrCancellation() != null) onClick()
        }
    }

@Composable
internal fun CommandsGrid(
    commands: List<CommandUi>,
    status: (CommandUi) -> CommandStatus,
    enabled: Boolean,
    columns: Int,
    onFire: (CommandUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        commands.chunked(columns).forEach { row ->
            CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { command ->
                    CommandButton(
                        command = command,
                        status = status(command),
                        enabled = enabled,
                        onFire = onFire,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) { CmpBox(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
internal fun AllCommandsSheet(
    commands: List<CommandUi>,
    status: (CommandUi) -> CommandStatus,
    enabled: Boolean,
    columns: Int,
    onFire: (CommandUi) -> Unit,
    onDismiss: () -> Unit,
) {
    CmpBottomSheet(onDismissRequest = onDismiss) {
        CmpColumn(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 20.dp)) {
            CmpRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CmpText(
                    text = stringResource(Strings.string.devicestate_commands_title_sheet),
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f),
                )
                CmpIconButton(
                    onClick = onDismiss,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.devicestate_commands_a11y_close),
                )
            }
            CommandsGrid(
                commands = commands,
                status = status,
                enabled = enabled,
                columns = columns,
                onFire = onFire,
                modifier = Modifier.padding(end = 8.dp, top = 4.dp),
            )
        }
    }
}

@Composable
internal fun DangerSheet(command: StateCommand, vehicle: VehicleState?, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = cmpThemeColors()
    val start = command == StateCommand.START_ENGINE
    CmpBottomSheet(onDismissRequest = onDismiss) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CmpBox(
                    modifier = Modifier.size(56.dp).background(colors.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        painter = commandPainter(command),
                        contentDescription = null,
                        tint = colors.onErrorContainer,
                        modifier = Modifier.size(42.dp),
                    )
                }
                CmpText(
                    text = stringResource(
                        if (start) {
                            Strings.string.devicestate_danger_title_start
                        } else {
                            Strings.string.devicestate_danger_title_panic
                        },
                    ),
                    fontSize = 22.sp,
                )
            }
            CmpText(
                text = stringResource(
                    if (start) {
                        Strings.string.devicestate_danger_msg_start
                    } else {
                        Strings.string.devicestate_danger_msg_panic
                    },
                ),
                fontSize = 16.sp,
            )
            if (start && vehicle != null) StartChecks(vehicle)
            HoldBar(
                text = stringResource(
                    if (start) {
                        Strings.string.devicestate_danger_btn_hold_start
                    } else {
                        Strings.string.devicestate_danger_btn_hold_panic
                    },
                ),
                onDone = onConfirm,
            )
            CmpOutlinedButton(
                text = stringResource(Strings.string.devicestate_danger_btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            )
        }
    }
}

@Composable
private fun StartChecks(vehicle: VehicleState) {
    val colors = cmpThemeColors()
    val checks = listOf(
        Zone.NEUTRAL to (vehicle.zone(Zone.NEUTRAL) == ZoneValue.ON),
        Zone.HANDBRAKE to (vehicle.zone(Zone.HANDBRAKE) == ZoneValue.ON),
        Zone.HOOD to (vehicle.zone(Zone.HOOD) == ZoneValue.OFF),
    )
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        checks.forEach { (zone, ok) ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CmpIcon(
                    icon = if (ok) CmpIcons.CheckCircle else CmpIcons.ErrorOutline,
                    contentDescription = null,
                    tint = if (ok) colors.primary else colors.error,
                    modifier = Modifier.size(24.dp),
                )
                CmpText(text = stringResource(zone.label), fontSize = 16.sp, modifier = Modifier.weight(1f))
                CmpText(
                    text = zoneValueText(zone, vehicle.zone(zone)),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ok) colors.onSurface else colors.error,
                )
            }
        }
    }
}

@Composable
private fun HoldBar(text: String, onDone: () -> Unit) {
    val colors = cmpThemeColors()
    val hold = remember { HoldState() }
    val progress = holdProgress(hold, CONFIRM_HOLD_MILLIS)
    val shape = RoundedCornerShape(32.dp)
    val label = when {
        progress > 0f -> stringResource(Strings.string.devicestate_cmd_status_hold)
        hold.hint -> stringResource(Strings.string.devicestate_danger_hint_hold)
        else -> text
    }
    CmpBox(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(colors.errorContainer, shape)
            .holdToFire(enabled = true, millis = CONFIRM_HOLD_MILLIS, label = text, state = hold, onFire = onDone)
            .testTag(CONFIRM_HOLD_TAG),
        contentAlignment = Alignment.Center,
    ) {
        CmpBox(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(progress)
                .heightIn(min = 64.dp)
                .background(colors.error, shape),
        )
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val content = if (progress > HALF) colors.onError else colors.onErrorContainer
            CmpIcon(
                icon = CmpIcons.TouchApp,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(24.dp),
            )
            CmpText(text = label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = content)
        }
    }
}

private const val HALF = 0.5f

@Composable
internal fun PrimaryCommands(
    commands: List<CommandUi>,
    status: (CommandUi) -> CommandStatus,
    enabled: Boolean,
    hold: HoldState,
    onFire: (CommandUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        commands.filter { it.primary }.forEach { command ->
            CommandButton(
                command = command,
                status = status(command),
                enabled = enabled,
                onFire = onFire,
                compact = true,
                sharedHold = hold,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

internal fun ZoneTone.isOn(): Boolean = this == ZoneTone.ACTIVE || this == ZoneTone.ALARM
