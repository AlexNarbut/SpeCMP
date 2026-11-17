package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_format_counter
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_format_reboot
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_bypass
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_check
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_clone
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_mount
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_prep
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_review
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_slave
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_hint_write
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_item_configuration
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_item_summary
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_item_unit
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_item_vehicle
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_label_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_label_step
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_status_next
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_status_optional
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_status_skipped
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_bypass
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_check
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_clone
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_mount
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_prep
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_review
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_slave
import ru.alarmtrade.specmp.core.localization.resources.fastmount_step_title_write
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal const val STEP_RAIL_TAG = "fastmount_rail"

private val MarkerSize = 36.dp
private val SmallMarkerSize = 28.dp
private val MarkerRing = 2.dp
private val MarkerHalo = 4.dp
private val LineWidth = 3.dp
private val RunWidth = 4.dp
private val CompactRebootHeight = 40.dp
private val SubRowHeight = 28.dp
private val SubIconSize = 20.dp
private val SubDotSize = 12.dp
private val DashOn = 4.dp
private val DashOff = 3.dp
private val TooltipWidth = 280.dp
private const val PULSE_MILLIS = 1_600
private const val RUN_MILLIS = 600
private const val SPIN_MILLIS = 2_000
private const val FULL_TURN = 360f
private const val PULSE_MIN_ALPHA = 0.45f
private const val HALO_ALPHA = 0.25f
private const val DOTTED_ALPHA = 0.5f

internal fun railWidth(windowWidth: Dp): Dp = when {
    windowWidth >= 1100.dp -> 300.dp
    windowWidth >= 900.dp -> 264.dp
    else -> CompactRailWidth
}

internal val CompactRailWidth = 88.dp

internal data class RailCar(val title: String, val configuration: String, val usb: Boolean)

@Composable
internal fun StepRail(
    model: RailModel,
    position: RailPosition,
    width: Dp,
    rebootSeconds: Int,
    modifier: Modifier = Modifier,
    dense: Boolean = false,
    car: RailCar? = null,
) {
    val colors = CmpTheme.colors
    val compact = width <= CompactRailWidth
    var help by remember { mutableStateOf<Pair<RailKey, Int>?>(null) }
    var railTop by remember { mutableIntStateOf(0) }
    CmpBox(
        modifier = modifier
            .onGloballyPositioned { railTop = it.positionInWindow().y.toInt() }
            .width(width)
            .fillMaxHeight()
            .background(colors.backgroundSurfaceCard)
            .testTag(STEP_RAIL_TAG),
    ) {
        CmpColumn(modifier = Modifier.fillMaxHeight()) {
            RailHeader(model, compact, dense)
            CmpColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = if (compact) CmpSpacing.space8 else CmpSpacing.space12,
                        vertical = CmpSpacing.space4,
                    ),
            ) {
                model.rows.forEachIndexed { index, row ->
                    var top by remember { mutableIntStateOf(0) }
                    RailStepRow(
                        row = row,
                        position = position,
                        compact = compact,
                        onClick = { help = if (help?.first == row.key) null else row.key to top - railTop },
                        modifier = Modifier.onGloballyPositioned { top = it.positionInWindow().y.toInt() },
                    )
                    val next = model.rows.getOrNull(index + 1) ?: return@forEachIndexed
                    if (position.rebootFrom == row.key) {
                        RebootLine(seconds = rebootSeconds, compact = compact)
                    } else {
                        Connector(row = row, next = next, compact = compact, dense = dense)
                    }
                }
            }
            if (car != null && !compact && !dense) RailCarCard(car)
        }
        CmpBox(
            Modifier
                .align(Alignment.CenterEnd)
                .width(CmpBorderWidth.inputs)
                .fillMaxHeight()
                .background(colors.borderHighlight),
        )
        help?.let { (key, top) ->
            val status = model.rows.firstOrNull { it.key == key }?.status
            StepHelp(
                key = key,
                status = status,
                x = width - if (compact) CmpSpacing.space8 else CmpSpacing.space16,
                y = top,
            ) { help = null }
        }
    }
}

@Composable
private fun RailHeader(model: RailModel, compact: Boolean, dense: Boolean) {
    val secondary = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    val counter = if (model.done) {
        stringResource(Strings.string.fastmount_rail_label_done)
    } else {
        stringResource(Strings.string.fastmount_rail_format_counter, model.currentNumber, model.total)
    }
    if (compact) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(top = CmpSpacing.space16, bottom = CmpSpacing.space8),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CmpText(
                text = if (model.done) "✓" else "${model.currentNumber}/${model.total}",
                style = CmpTextStyle.Headline3,
            )
            CmpText(
                text = stringResource(Strings.string.fastmount_rail_label_step),
                style = CmpTextStyle.Additional1,
                color = secondary,
            )
        }
    } else {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(
                start = CmpSpacing.space20,
                end = CmpSpacing.space20,
                top = if (dense) CmpSpacing.space12 else CmpSpacing.space16,
                bottom = if (dense) CmpSpacing.space8 else CmpSpacing.space12,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpText(
                text = stringResource(Strings.string.fastmount_common_title_screen),
                style = CmpTextStyle.Headline1,
                modifier = Modifier.weight(1f),
            )
            CmpText(text = counter, style = CmpTextStyle.Headline3, color = secondary)
        }
    }
}

@Composable
private fun RailStepRow(
    row: RailRow,
    position: RailPosition,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val current = row.status == RailStatus.CURRENT
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.touchMin)
            .background(if (current) colors.backgroundSurfaceHighlight else Color.Transparent, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 0.dp else CmpSpacing.space8, vertical = CmpSpacing.space4)
            .animateContentSize(),
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top,
    ) {
        CmpBox(modifier = Modifier.width(MarkerSize), contentAlignment = Alignment.Center) {
            Marker(row.status, row.number)
        }
        if (!compact) {
            CmpColumn(
                modifier = Modifier.weight(1f).padding(top = CmpSpacing.space8),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            ) {
                CmpText(
                    text = stringResource(row.key.title()),
                    style = if (current) CmpTextStyle.Headline3 else CmpTextStyle.Paragraph2,
                    color = titleColor(row.status),
                )
                statusText(row.status)?.let { text ->
                    CmpTag(text = stringResource(text), tone = row.status.tagTone(), maxLines = 2)
                }
                if (row.key == RailKey.PREP && current) PrepSubSteps(position.prepSub)
            }
        }
    }
}

@Composable
private fun titleColor(status: RailStatus): Color = when (status) {
    RailStatus.OPTIONAL, RailStatus.SKIPPED, RailStatus.AHEAD -> CmpTheme.colors.foregroundOnDarkSecondaryDefault
    RailStatus.CURRENT, RailStatus.DONE, RailStatus.NEXT -> CmpTheme.colors.foregroundOnDarkPrimaryDefault
}

private fun RailStatus.tagTone(): CmpTagTone = when (this) {
    RailStatus.NEXT -> CmpTagTone.Brand
    RailStatus.SKIPPED -> CmpTagTone.Neutral
    RailStatus.OPTIONAL, RailStatus.DONE, RailStatus.CURRENT, RailStatus.AHEAD -> CmpTagTone.Outline
}

@Composable
private fun PrepSubSteps(current: PrepSub) {
    val colors = CmpTheme.colors
    CmpColumn(modifier = Modifier.padding(bottom = CmpSpacing.space4)) {
        PrepSub.entries.forEach { sub ->
            val done = sub.ordinal < current.ordinal
            val active = sub == current
            CmpRow(
                modifier = Modifier.heightIn(min = SubRowHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpBox(modifier = Modifier.width(SubIconSize), contentAlignment = Alignment.Center) {
                    when {
                        done -> CmpIcon(
                            icon = CmpIcons.Check,
                            contentDescription = null,
                            tint = colors.successDefault,
                            modifier = Modifier.size(SubIconSize),
                        )

                        active -> CmpIcon(
                            icon = CmpIcons.ChevronRight,
                            contentDescription = null,
                            tint = colors.brandPrimaryDefault,
                            modifier = Modifier.size(SubIconSize),
                        )

                        else -> CmpBox(
                            Modifier.size(SubDotSize).border(CmpBorderWidth.inputs, colors.borderLight, CircleShape),
                        )
                    }
                }
                CmpText(
                    text = stringResource(sub.title()),
                    style = if (active) CmpTextStyle.Headline3 else CmpTextStyle.Paragraph2,
                    color = if (active ||
                        done
                    ) {
                        colors.foregroundOnDarkPrimaryDefault
                    } else {
                        colors.foregroundOnDarkSecondaryDefault
                    },
                )
            }
        }
    }
}

@Composable
private fun Marker(status: RailStatus, number: Int?) {
    val colors = CmpTheme.colors
    val label = number?.toString().orEmpty()
    when (status) {
        RailStatus.DONE -> MarkerCircle(colors.successDefault) {
            CmpIcon(
                CmpIcons.Check,
                contentDescription = null,
                tint = colors.onBrand,
                modifier = Modifier.size(SubIconSize),
            )
        }

        RailStatus.CURRENT -> CmpBox(
            modifier = Modifier
                .size(MarkerSize + MarkerHalo * 2)
                .background(colors.brandPrimaryDefault.copy(alpha = HALO_ALPHA), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            MarkerCircle(colors.brandPrimaryDefault) {
                CmpText(text = label, style = CmpTextStyle.Headline3, color = colors.onBrand)
            }
        }

        RailStatus.NEXT -> {
            val pulse = rememberInfiniteTransition(label = "next").animateFloat(
                initialValue = 1f,
                targetValue = PULSE_MIN_ALPHA,
                animationSpec = infiniteRepeatable(tween(PULSE_MILLIS / 2), RepeatMode.Reverse),
                label = "pulse",
            )
            MarkerCircle(
                fill = colors.backgroundPagePrimary,
                modifier = Modifier.alpha(pulse.value).dashedCircle(colors.brandPrimaryDefault),
            ) {
                CmpText(text = label, style = CmpTextStyle.Headline3, color = colors.brandPrimaryDefault)
            }
        }

        RailStatus.OPTIONAL -> MarkerCircle(
            fill = colors.backgroundPagePrimary,
            modifier = Modifier.dashedCircle(colors.borderLight),
            size = SmallMarkerSize,
        ) {
            CmpIcon(
                CmpIcons.Add,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.size(CmpSize.iconS),
            )
        }

        RailStatus.SKIPPED -> MarkerCircle(
            fill = colors.backgroundSurfaceHighlight,
            modifier = Modifier.dashedCircle(colors.borderHighlight),
            size = SmallMarkerSize,
        ) {
            CmpIcon(
                CmpIcons.Remove,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.size(CmpSize.iconS),
            )
        }

        RailStatus.AHEAD -> MarkerCircle(
            fill = colors.backgroundPagePrimary,
            modifier = Modifier.border(MarkerRing, colors.borderLight, CircleShape),
        ) {
            CmpText(text = label, style = CmpTextStyle.Headline3, color = colors.foregroundOnDarkSecondaryDefault)
        }
    }
}

@Composable
private fun MarkerCircle(
    fill: Color,
    modifier: Modifier = Modifier,
    size: Dp = MarkerSize,
    content: @Composable () -> Unit,
) {
    CmpBox(
        modifier = Modifier.size(size).background(fill, CircleShape).then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

internal fun Modifier.dashedCircle(color: Color): Modifier = drawBehind {
    val stroke = MarkerRing.toPx()
    drawCircle(
        color = color,
        radius = (size.minDimension - stroke) / 2,
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashOn.toPx(), DashOff.toPx())),
        ),
    )
}

@Composable
private fun Connector(row: RailRow, next: RailRow, compact: Boolean, dense: Boolean) {
    val colors = CmpTheme.colors
    val optional = { r: RailRow -> r.status == RailStatus.OPTIONAL || r.status == RailStatus.SKIPPED }
    val dotted = (next.key == RailKey.CLONE && optional(next)) || (row.key == RailKey.CLONE && optional(row))
    val color = when {
        dotted -> colors.borderHighlight
        next.status == RailStatus.CURRENT -> colors.brandPrimaryDefault
        next.status == RailStatus.DONE -> colors.successDefault
        else -> colors.borderHighlight
    }
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (dense) CmpSpacing.space4 else CmpSpacing.space8)
            .padding(horizontal = if (compact) 0.dp else CmpSpacing.space8),
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.Start,
    ) {
        CmpBox(modifier = Modifier.width(MarkerSize).fillMaxHeight(), contentAlignment = Alignment.Center) {
            CmpBox(
                Modifier
                    .width(LineWidth)
                    .fillMaxHeight()
                    .alpha(if (dotted) DOTTED_ALPHA else 1f)
                    .background(color),
            )
        }
    }
}

@Composable
private fun RebootLine(seconds: Int, compact: Boolean) {
    val colors = CmpTheme.colors
    val transition = rememberInfiniteTransition(label = "reboot")
    val run by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(RUN_MILLIS, easing = LinearEasing), RepeatMode.Reverse),
        label = "run",
    )
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec = infiniteRepeatable(tween(SPIN_MILLIS, easing = LinearEasing)),
        label = "spin",
    )
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) CompactRebootHeight else CmpSize.touchMin)
            .padding(horizontal = if (compact) 0.dp else CmpSpacing.space8),
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpBox(
            modifier = Modifier.width(MarkerSize).height(CompactRebootHeight),
            contentAlignment = Alignment.Center,
        ) {
            CmpBox(
                Modifier
                    .width(RunWidth)
                    .fillMaxHeight()
                    .alpha(run)
                    .background(colors.brandPrimaryDefault, RoundedCornerShape(CmpRadius.round)),
            )
        }
        if (!compact) {
            CmpRow(
                modifier = Modifier
                    .background(colors.brandPrimaryHighlight, RoundedCornerShape(CmpRadius.cardSmall))
                    .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpIcon(
                    icon = CmpIcons.Autorenew,
                    contentDescription = null,
                    tint = colors.brandPrimaryDefault,
                    modifier = Modifier.size(SubIconSize).rotate(angle),
                )
                CmpText(
                    text = stringResource(Strings.string.fastmount_rail_format_reboot, formatElapsed(seconds)),
                    style = CmpTextStyle.Headline3,
                    color = colors.brandPrimaryDefault,
                )
            }
        }
    }
}

@Composable
private fun RailCarCard(car: RailCar) {
    val secondary = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    CmpCard(
        modifier = Modifier
            .padding(start = CmpSpacing.space12, end = CmpSpacing.space12, bottom = CmpSpacing.space12)
            .fillMaxWidth(),
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpIcon(
                icon = if (car.usb) CmpIcons.Usb else CmpIcons.Bluetooth,
                contentDescription = null,
                tint = secondary,
                modifier = Modifier.size(CmpSize.iconM),
            )
            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(
                    text = car.title,
                    style = CmpTextStyle.Headline3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                CmpText(
                    text = car.configuration,
                    style = CmpTextStyle.Paragraph3,
                    color = secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StepHelp(key: RailKey, status: RailStatus?, x: Dp, y: Int, onClose: () -> Unit) {
    val colors = CmpTheme.colors
    val ink = colors.foregroundOnLightPrimaryDefault
    val density = LocalDensity.current
    val offset = with(density) { IntOffset(x.roundToPx(), y) }
    Popup(offset = offset, onDismissRequest = onClose, properties = PopupProperties(focusable = true)) {
        CmpColumn(
            modifier = Modifier
                .width(TooltipWidth)
                .background(colors.backgroundSurfacePrimary, RoundedCornerShape(CmpRadius.cardMedium))
                .clickable(onClick = onClose)
                .padding(
                    start = CmpSpacing.space16,
                    end = CmpSpacing.space4,
                    top = CmpSpacing.space4,
                    bottom = CmpSpacing.space16,
                ),
        ) {
            CmpRow(verticalAlignment = Alignment.CenterVertically) {
                val suffix = status?.let(::statusText)?.let { " · " + stringResource(it).lowercase() }.orEmpty()
                CmpText(
                    text = stringResource(key.title()) + suffix,
                    style = CmpTextStyle.Headline3,
                    color = ink,
                    modifier = Modifier.weight(1f),
                )
                CmpBox(modifier = Modifier.size(CmpSize.touchMin), contentAlignment = Alignment.Center) {
                    CmpIcon(
                        icon = CmpIcons.Close,
                        contentDescription = stringResource(Strings.string.fastmount_rail_a11y_close),
                        tint = ink,
                        modifier = Modifier.size(SubIconSize),
                    )
                }
            }
            CmpText(
                text = stringResource(key.help()),
                style = CmpTextStyle.Paragraph2,
                color = ink,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(end = CmpSpacing.space8),
            )
        }
    }
}

internal fun formatElapsed(seconds: Int): String =
    "${seconds / SECONDS_IN_MINUTE}:${(seconds % SECONDS_IN_MINUTE).toString().padStart(2, '0')}"

private const val SECONDS_IN_MINUTE = 60

internal fun RailKey.title(): StringResource = when (this) {
    RailKey.PREP -> Strings.string.fastmount_step_title_prep
    RailKey.WRITE -> Strings.string.fastmount_step_title_write
    RailKey.MOUNT -> Strings.string.fastmount_step_title_mount
    RailKey.BYPASS -> Strings.string.fastmount_step_title_bypass
    RailKey.CLONE -> Strings.string.fastmount_step_title_clone
    RailKey.SLAVE -> Strings.string.fastmount_step_title_slave
    RailKey.CHECK -> Strings.string.fastmount_step_title_check
    RailKey.REVIEW -> Strings.string.fastmount_step_title_review
}

private fun RailKey.help(): StringResource = when (this) {
    RailKey.PREP -> Strings.string.fastmount_rail_hint_prep
    RailKey.WRITE -> Strings.string.fastmount_rail_hint_write
    RailKey.MOUNT -> Strings.string.fastmount_rail_hint_mount
    RailKey.BYPASS -> Strings.string.fastmount_rail_hint_bypass
    RailKey.CLONE -> Strings.string.fastmount_rail_hint_clone
    RailKey.SLAVE -> Strings.string.fastmount_rail_hint_slave
    RailKey.CHECK -> Strings.string.fastmount_rail_hint_check
    RailKey.REVIEW -> Strings.string.fastmount_rail_hint_review
}

private fun PrepSub.title(): StringResource = when (this) {
    PrepSub.UNIT -> Strings.string.fastmount_rail_item_unit
    PrepSub.VEHICLE -> Strings.string.fastmount_rail_item_vehicle
    PrepSub.CONFIGURATION -> Strings.string.fastmount_rail_item_configuration
    PrepSub.SUMMARY -> Strings.string.fastmount_rail_item_summary
}

private fun statusText(status: RailStatus): StringResource? = when (status) {
    RailStatus.OPTIONAL -> Strings.string.fastmount_rail_status_optional
    RailStatus.SKIPPED -> Strings.string.fastmount_rail_status_skipped
    RailStatus.NEXT -> Strings.string.fastmount_rail_status_next
    RailStatus.DONE, RailStatus.CURRENT, RailStatus.AHEAD -> null
}
