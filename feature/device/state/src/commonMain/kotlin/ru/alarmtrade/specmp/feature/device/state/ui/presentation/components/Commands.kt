package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_status_done
import ru.alarmtrade.specmp.core.localization.resources.devicestate_commands_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.devicestate_commands_title_sheet
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_btn_hold_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_btn_hold_start
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_msg_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_msg_start
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_title_panic
import ru.alarmtrade.specmp.core.localization.resources.devicestate_danger_title_start
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpMotion
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpHoldToConfirmButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
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
private const val CONFIRM_HOLD_MILLIS = 2_000
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
private fun RingIcon(command: StateCommand, progress: Float, sending: Boolean, tint: Color, size: Dp, iconSize: Dp) {
    CmpBox(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        when {
            sending -> CmpCircularProgressIndicator(
                modifier = Modifier.size(size),
                strokeWidth = RingStroke,
                color = tint,
            )

            progress > 0f -> CmpCircularProgressIndicator(
                modifier = Modifier.size(size),
                progress = progress,
                strokeWidth = RingStroke,
                color = tint,
            )
        }
        CmpIcon(
            painter = commandPainter(command),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

private data class CommandLook(val background: Color, val border: Color, val content: Color, val icon: Color)

@Composable
private fun commandLook(command: CommandUi, status: CommandStatus): CommandLook {
    val colors = CmpTheme.colors
    val ink = colors.foregroundOnDarkPrimaryDefault
    return when ((status as? CommandStatus.Finished)?.outcome) {
        CommandOutcome.DONE -> CommandLook(
            background = colors.successHighlight,
            border = colors.successDefault.copy(alpha = TONE_BORDER_ALPHA),
            content = ink,
            icon = colors.successDefault,
        )

        null -> CommandLook(
            background = colors.backgroundSurfaceHighlight,
            border = colors.borderHighlight,
            content = ink,
            icon = if (command.danger) colors.errorDefault else colors.brandPrimaryDefault,
        )

        else -> CommandLook(
            background = colors.errorHighlight,
            border = colors.errorDefault.copy(alpha = TONE_BORDER_ALPHA),
            content = ink,
            icon = colors.errorDefault,
        )
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
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    val size = if (compact) CommandSize.Square else CommandSize.Tile
    CmpColumn(
        modifier = modifier
            .height(size.height)
            .commandGesture(command, active, label, hold, shape) { onFire(command) }
            .background(look.background, shape)
            .border(CmpBorderWidth.listItem, look.border, shape)
            .semantics { if (result != null) stateDescription = result }
            .testTag(COMMAND_TAG_PREFIX + command.command.name.lowercase())
            .padding(horizontal = CmpSpacing.space8, vertical = size.padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4, Alignment.CenterVertically),
    ) {
        RingIcon(
            command = command.command,
            progress = progress,
            sending = sending,
            tint = if (active) look.icon else look.icon.copy(alpha = DISABLED_ALPHA),
            size = size.ring,
            iconSize = size.icon,
        )
        CmpText(
            text = label,
            style = size.label,
            color = look.content,
            textAlign = TextAlign.Center,
            maxLines = size.maxLines,
        )
    }
}

/** Квадрат 72 — пульт и низкое окно; плитка — сетка всех команд. */
private enum class CommandSize(
    val height: Dp,
    val ring: Dp,
    val icon: Dp,
    val padding: Dp,
    val label: CmpTextStyle,
    val maxLines: Int,
) {
    Square(72.dp, 40.dp, 34.dp, CmpSpacing.space4, CmpTextStyle.Additional0, 1),
    Tile(104.dp, 52.dp, CmpSize.iconXl, CmpSpacing.space8, CmpTextStyle.Headline3, 2),
}

@Composable
private fun Modifier.commandGesture(
    command: CommandUi,
    active: Boolean,
    label: String,
    hold: HoldState,
    shape: RoundedCornerShape,
    onFire: () -> Unit,
): Modifier = if (command.danger) {
    cmpPressable(onClick = onFire, enabled = active, shape = shape)
} else {
    holdPressScale(pressed = hold.pressed && hold.command == command.command)
        .holdToFire(active, HOLD_MILLIS, label, hold, owner = command.command, onFire = onFire)
}

private val RingStroke = 3.dp
private const val DISABLED_ALPHA = 0.38f
private const val TONE_BORDER_ALPHA = 0.3f

/** Удержание идёт своим жестом (не clickable), поэтому сжатие `cmpPressable` повторено по флагу нажатия. */
@Composable
private fun Modifier.holdPressScale(pressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) CmpMotion.PRESS_SCALE else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "holdPress",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
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
    compact: Boolean = false,
) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        commands.chunked(columns).forEach { row ->
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                row.forEach { command ->
                    CommandButton(
                        command = command,
                        status = status(command),
                        enabled = enabled,
                        onFire = onFire,
                        compact = compact,
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
    PanelSheet(title = stringResource(Strings.string.devicestate_commands_title_sheet), onDismiss = onDismiss) {
        CommandsGrid(
            commands = commands,
            status = status,
            enabled = enabled,
            columns = columns,
            onFire = onFire,
            modifier = Modifier.padding(top = CmpSpacing.space4),
        )
    }
}

/** Лист снизу с заголовком и «Закрыть»: все команды, «Подробно», весь чек-лист в низком окне. */
@Composable
internal fun PanelSheet(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    CmpBottomSheet(onDismissRequest = onDismiss) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(
                start = CmpSpacing.screenPaddingH,
                end = CmpSpacing.space12,
                bottom = CmpSpacing.space24,
            ),
        ) {
            CmpRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CmpText(text = title, style = CmpTextStyle.Headline1, modifier = Modifier.weight(1f))
                CmpIconButton(
                    onClick = onDismiss,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.devicestate_commands_a11y_close),
                )
            }
            CmpColumn(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(end = CmpSpacing.space12),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                content = content,
            )
        }
    }
}

@Composable
internal fun DangerSheet(command: StateCommand, vehicle: VehicleState?, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = CmpTheme.colors
    val start = command == StateCommand.START_ENGINE
    CmpBottomSheet(onDismissRequest = onDismiss) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(
                start = CmpSpacing.screenPaddingH,
                end = CmpSpacing.screenPaddingH,
                bottom = CmpSpacing.space24,
            ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            ) {
                CmpBox(
                    modifier = Modifier.size(CmpSize.iconXxl).background(colors.errorHighlight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        painter = commandPainter(command),
                        contentDescription = null,
                        tint = colors.errorDefault,
                        modifier = Modifier.size(CmpSize.iconXl),
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
                    style = CmpTextStyle.Headline1,
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
                style = CmpTextStyle.Paragraph2,
            )
            if (start && vehicle != null) StartChecks(vehicle)
            CmpHoldToConfirmButton(
                text = stringResource(
                    if (start) {
                        Strings.string.devicestate_danger_btn_hold_start
                    } else {
                        Strings.string.devicestate_danger_btn_hold_panic
                    },
                ),
                doneText = stringResource(Strings.string.devicestate_cmd_status_done),
                onConfirm = onConfirm,
                durationMs = CONFIRM_HOLD_MILLIS,
                modifier = Modifier.testTag(CONFIRM_HOLD_TAG),
            )
            CmpButton(
                text = stringResource(Strings.string.devicestate_danger_btn_cancel),
                onClick = onDismiss,
                type = CmpButtonType.Secondary,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun StartChecks(vehicle: VehicleState) {
    val colors = CmpTheme.colors
    val checks = listOf(
        Zone.NEUTRAL to (vehicle.zone(Zone.NEUTRAL) == ZoneValue.ON),
        Zone.HANDBRAKE to (vehicle.zone(Zone.HANDBRAKE) == ZoneValue.ON),
        Zone.HOOD to (vehicle.zone(Zone.HOOD) == ZoneValue.OFF),
    )
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.cardMedium))
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space4),
    ) {
        checks.forEach { (zone, ok) ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpIcon(
                    icon = if (ok) CmpIcons.CheckCircle else CmpIcons.ErrorOutline,
                    contentDescription = null,
                    tint = if (ok) colors.successDefault else colors.errorDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )
                CmpText(
                    text = stringResource(zone.label),
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier.weight(1f),
                )
                CmpText(
                    text = zoneValueText(zone, vehicle.zone(zone)),
                    style = CmpTextStyle.Headline3,
                    color = if (ok) colors.foregroundOnDarkPrimaryDefault else colors.errorDefault,
                )
            }
        }
    }
}

@Composable
internal fun PrimaryCommands(
    commands: List<CommandUi>,
    status: (CommandUi) -> CommandStatus,
    enabled: Boolean,
    hold: HoldState,
    onFire: (CommandUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
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
