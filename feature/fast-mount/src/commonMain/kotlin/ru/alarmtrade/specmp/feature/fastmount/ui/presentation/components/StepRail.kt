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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal const val STEP_RAIL_TAG = "fastmount_rail"

private val MarkerSize = 36.dp
private val SmallMarkerSize = 28.dp
private const val PULSE_MILLIS = 1_600
private const val RUN_MILLIS = 600
private const val SPIN_MILLIS = 2_000
private const val FULL_TURN = 360f
private const val PULSE_MIN_ALPHA = 0.45f
private val TooltipWidth = 280.dp

/** Ширина колонки шагов по ширине окна (макет: ≥1100 — 300, 900–1099 — 264, 760–899 — компактная 88). */
internal fun railWidth(windowWidth: Dp): Dp = when {
    windowWidth >= 1100.dp -> 300.dp
    windowWidth >= 900.dp -> 264.dp
    else -> CompactRailWidth
}

internal val CompactRailWidth = 88.dp

/** Карточка выбранного автомобиля и конфигурации внизу колонки. */
internal data class RailCar(val title: String, val configuration: String, val usb: Boolean)

/**
 * Колонка шагов (W1, вариант B): заголовок со счётчиком «N из M», шаги со статусами, «бегущая» линия
 * перезагрузки под завершённым шагом, карточка автомобиля внизу. Тап по шагу — короткая справка рядом, перехода нет.
 */
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
    val colors = cmpThemeColors()
    val compact = width <= CompactRailWidth
    var help by remember { mutableStateOf<Pair<RailKey, Int>?>(null) }
    var railTop by remember { mutableIntStateOf(0) }
    CmpBox(
        modifier = modifier
            .onGloballyPositioned { railTop = it.positionInWindow().y.toInt() }
            .width(width)
            .fillMaxHeight()
            .background(colors.surfaceContainerLow)
            .testTag(STEP_RAIL_TAG),
    ) {
        CmpColumn(modifier = Modifier.fillMaxHeight()) {
            RailHeader(model, compact, dense)
            CmpColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = 2.dp),
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
            Modifier.align(Alignment.CenterEnd).width(1.dp).fillMaxHeight().background(colors.outlineVariant),
        )
        help?.let { (key, top) ->
            val status = model.rows.firstOrNull { it.key == key }?.status
            StepHelp(key = key, status = status, x = width - if (compact) 8.dp else 16.dp, y = top) { help = null }
        }
    }
}

@Composable
private fun RailHeader(model: RailModel, compact: Boolean, dense: Boolean) {
    val colors = cmpThemeColors()
    val counter = if (model.done) {
        stringResource(Strings.string.fastmount_rail_label_done)
    } else {
        stringResource(Strings.string.fastmount_rail_format_counter, model.currentNumber, model.total)
    }
    if (compact) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CmpText(
                text = if (model.done) "✓" else "${model.currentNumber}/${model.total}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            CmpText(
                text = stringResource(Strings.string.fastmount_rail_label_step),
                style = CmpTextStyle.Caption,
                color = colors.onSurfaceVariant,
            )
        }
    } else {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(
                start = 20.dp,
                end = 20.dp,
                top = if (dense) 14.dp else 18.dp,
                bottom = if (dense) 8.dp else 12.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpText(
                text = stringResource(Strings.string.fastmount_common_title_screen),
                style = CmpTextStyle.Title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            CmpText(text = counter, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurfaceVariant)
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
    val colors = cmpThemeColors()
    val current = row.status == RailStatus.CURRENT
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(if (current) colors.secondaryContainer else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 0.dp else 10.dp, vertical = 6.dp)
            .animateContentSize(),
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.spacedBy(12.dp),
        verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top,
    ) {
        CmpBox(modifier = Modifier.width(MarkerSize), contentAlignment = Alignment.Center) {
            Marker(row.status, row.number)
        }
        if (!compact) {
            CmpColumn(modifier = Modifier.weight(1f).padding(top = 6.dp)) {
                CmpText(
                    text = stringResource(row.key.title()),
                    fontSize = 16.sp,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Medium,
                    color = titleColor(row.status),
                )
                statusText(row.status)?.let { text ->
                    CmpText(
                        text = stringResource(text),
                        style = CmpTextStyle.Body,
                        fontWeight = if (row.status == RailStatus.NEXT) FontWeight.SemiBold else null,
                        color = if (row.status == RailStatus.NEXT) colors.primary else colors.onSurfaceVariant,
                    )
                }
                if (row.key == RailKey.PREP && current) PrepSubSteps(position.prepSub)
            }
        }
    }
}

@Composable
private fun titleColor(status: RailStatus) = when (status) {
    RailStatus.CURRENT -> cmpThemeColors().onSecondaryContainer
    RailStatus.OPTIONAL, RailStatus.SKIPPED, RailStatus.AHEAD -> cmpThemeColors().onSurfaceVariant
    RailStatus.DONE, RailStatus.NEXT -> cmpThemeColors().onSurface
}

@Composable
private fun PrepSubSteps(current: PrepSub) {
    val colors = cmpThemeColors()
    CmpColumn(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
        PrepSub.entries.forEach { sub ->
            val done = sub.ordinal < current.ordinal
            val active = sub == current
            CmpRow(
                modifier = Modifier.heightIn(min = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CmpBox(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                    when {
                        done -> CmpIcon(
                            icon = CmpIcons.Check,
                            contentDescription = null,
                            tint = colors.onSecondaryContainer,
                            modifier = Modifier.size(20.dp),
                        )

                        active -> CmpIcon(
                            icon = CmpIcons.ChevronRight,
                            contentDescription = null,
                            tint = colors.onSecondaryContainer,
                            modifier = Modifier.size(20.dp),
                        )

                        else -> CmpBox(
                            Modifier.size(12.dp).border(1.5.dp, colors.onSurfaceVariant, CircleShape),
                        )
                    }
                }
                CmpText(
                    text = stringResource(sub.title()),
                    fontSize = 15.sp,
                    fontWeight = if (active) FontWeight.Bold else null,
                    color = if (active || done) colors.onSecondaryContainer else colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Marker(status: RailStatus, number: Int?) {
    val colors = cmpThemeColors()
    val label = number?.toString().orEmpty()
    when (status) {
        RailStatus.DONE -> MarkerCircle(colors.primary, null) {
            CmpIcon(CmpIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(22.dp))
        }

        RailStatus.CURRENT -> CmpBox(
            modifier = Modifier
                .size(MarkerSize + 8.dp)
                .background(colors.primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            MarkerCircle(colors.primary, null) {
                CmpText(text = label, color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        RailStatus.NEXT -> {
            val pulse = rememberInfiniteTransition(label = "next").animateFloat(
                initialValue = 1f,
                targetValue = PULSE_MIN_ALPHA,
                animationSpec = infiniteRepeatable(tween(PULSE_MILLIS / 2), RepeatMode.Reverse),
                label = "pulse",
            )
            MarkerCircle(colors.background, colors.primary, Modifier.alpha(pulse.value)) {
                CmpText(text = label, color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        RailStatus.OPTIONAL -> MarkerCircle(colors.background, colors.outline, size = SmallMarkerSize) {
            CmpIcon(
                CmpIcons.Add,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        RailStatus.SKIPPED -> MarkerCircle(colors.surfaceContainerHigh, colors.outlineVariant, size = SmallMarkerSize) {
            CmpIcon(
                CmpIcons.Remove,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        RailStatus.AHEAD -> MarkerCircle(colors.background, colors.outline) {
            CmpText(text = label, color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun MarkerCircle(
    fill: Color,
    border: Color?,
    modifier: Modifier = Modifier,
    size: Dp = MarkerSize,
    content: @Composable () -> Unit,
) {
    CmpBox(
        modifier = modifier
            .size(size)
            .background(fill, CircleShape)
            .then(if (border != null) Modifier.border(2.dp, border, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Линия между шагами: пунктир — к Clone «по необходимости», цвет — пройдено/впереди. */
@Composable
private fun Connector(row: RailRow, next: RailRow, compact: Boolean, dense: Boolean) {
    val colors = cmpThemeColors()
    val optional = { r: RailRow -> r.status == RailStatus.OPTIONAL || r.status == RailStatus.SKIPPED }
    val dotted = (next.key == RailKey.CLONE && optional(next)) || (row.key == RailKey.CLONE && optional(row))
    val passed = next.status == RailStatus.DONE || next.status == RailStatus.CURRENT
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (dense) 6.dp else 10.dp)
            .padding(horizontal = if (compact) 0.dp else 10.dp),
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.Start,
    ) {
        CmpBox(modifier = Modifier.width(MarkerSize).fillMaxHeight(), contentAlignment = Alignment.Center) {
            CmpBox(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .alpha(if (dotted) DOTTED_ALPHA else 1f)
                    .background(if (passed && !dotted) colors.primary else colors.outlineVariant),
            )
        }
    }
}

private const val DOTTED_ALPHA = 0.5f

/** Перезагрузка — «бегущая» линия под завершённым шагом и плашка «Перезагрузка · м:сс». */
@Composable
private fun RebootLine(seconds: Int, compact: Boolean) {
    val colors = cmpThemeColors()
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
            .heightIn(min = if (compact) 40.dp else 48.dp)
            .padding(horizontal = if (compact) 0.dp else 10.dp),
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpBox(modifier = Modifier.width(MarkerSize).height(40.dp), contentAlignment = Alignment.Center) {
            CmpBox(Modifier.width(4.dp).fillMaxHeight().alpha(run).background(colors.primary, RoundedCornerShape(2.dp)))
        }
        if (!compact) {
            CmpRow(
                modifier = Modifier
                    .background(colors.primaryContainer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CmpIcon(
                    icon = CmpIcons.Autorenew,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(20.dp).rotate(angle),
                )
                CmpText(
                    text = stringResource(Strings.string.fastmount_rail_format_reboot, formatElapsed(seconds)),
                    color = colors.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun RailCarCard(car: RailCar) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CmpIcon(
            icon = if (car.usb) CmpIcons.Usb else CmpIcons.Bluetooth,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(
                text = car.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CmpText(
                text = car.configuration,
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Короткая справка по шагу рядом с колонкой (макет: всплывающая подсказка, а не раскрытие строки). */
@Composable
private fun StepHelp(key: RailKey, status: RailStatus?, x: Dp, y: Int, onClose: () -> Unit) {
    val colors = cmpThemeColors()
    val density = LocalDensity.current
    val offset = with(density) { IntOffset(x.roundToPx(), y) }
    Popup(offset = offset, onDismissRequest = onClose, properties = PopupProperties(focusable = true)) {
        CmpColumn(
            modifier = Modifier
                .width(TooltipWidth)
                .background(colors.onSurface, RoundedCornerShape(16.dp))
                .clickable(onClick = onClose)
                .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 14.dp),
        ) {
            CmpRow(verticalAlignment = Alignment.CenterVertically) {
                val suffix = status?.let(::statusText)?.let { " · " + stringResource(it).lowercase() }.orEmpty()
                CmpText(
                    text = stringResource(key.title()) + suffix,
                    color = colors.surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                CmpBox(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CmpIcon(
                        icon = CmpIcons.Close,
                        contentDescription = stringResource(Strings.string.fastmount_rail_a11y_close),
                        tint = colors.surface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            CmpText(
                text = stringResource(key.help()),
                style = CmpTextStyle.Body,
                color = colors.surface,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(end = 8.dp),
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
