package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_format_alarm
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_format_stale
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_loading_first
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_msg_online
import ru.alarmtrade.specmp.core.localization.resources.devicestate_hero_status_alarm
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateState
import specmp.feature.device.state.generated.resources.Res
import specmp.feature.device.state.generated.resources.devicestate_status_active_arm
import specmp.feature.device.state.generated.resources.devicestate_status_arm
import specmp.feature.device.state.generated.resources.devicestate_status_disarm
import specmp.feature.device.state.generated.resources.devicestate_status_sensors

internal const val CHECK_STATE_HERO_TAG = "devicestate_hero"
private const val ICON_BG_ALPHA = 0.18f
private const val TITLE_SKELETON_FRACTION = 0.55f
private const val DOT_PULSE_MILLIS = 600
private const val DOT_MIN_SCALE = 0.6f
private const val FLASH_MILLIS = 1_800
private const val FLASH_ALPHA = 0.45f

@Composable
internal fun StatusHero(state: CheckStateState, modifier: Modifier = Modifier) {
    val vehicle = state.vehicle
    val alarm = state.alarm
    val stale = state.staleSeconds != null
    val style = heroStyle(state)
    val background = style.background
    val content = style.content
    val shape = RoundedCornerShape(20.dp)
    CmpBox(
        modifier = modifier
            .fillMaxWidth()
            .background(background, shape)
            .testTag(CHECK_STATE_HERO_TAG),
    ) {
        GuardFlash(on = state.guardChanged, key = state.tick, color = content)
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CmpBox(
                modifier = Modifier.size(44.dp).background(style.iconBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                when (val icon = style.icon) {
                    is HeroIcon.Vector -> CmpIcon(
                        icon = icon.icon,
                        contentDescription = null,
                        tint = style.iconTint,
                        modifier = Modifier.size(26.dp),
                    )

                    is HeroIcon.Drawable -> CmpIcon(
                        painter = painterResource(icon.drawable),
                        contentDescription = null,
                        tint = style.iconTint,
                        modifier = Modifier.size(36.dp),
                    )
                }
                if (vehicle != null && !stale) OnlineDot(tick = state.tick, ring = background)
            }
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (vehicle == null) {
                    CmpSkeleton(modifier = Modifier.fillMaxWidth(TITLE_SKELETON_FRACTION).heightIn(min = 22.dp))
                    CmpText(
                        text = stringResource(Strings.string.devicestate_hero_loading_first),
                        style = CmpTextStyle.Paragraph2,
                        color = content,
                    )
                } else {
                    val title = if (alarm) Strings.string.devicestate_hero_status_alarm else vehicle.security.title
                    CmpText(
                        text = stringResource(title),
                        style = CmpTextStyle.Headline2,
                        color = content,
                    )
                    HeroSubtitle(state = state, color = content)
                }
            }
        }
    }
}

/** Статус в шапке низкого окна (телефон-ландшафт): карточки статуса там нет. */
@Composable
internal fun StatusPill(state: CheckStateState, modifier: Modifier = Modifier) {
    val vehicle = state.vehicle
    val style = heroStyle(state)
    CmpRow(
        modifier = modifier
            .heightIn(min = 40.dp)
            .background(style.background, CircleShape)
            .padding(start = 10.dp, end = 14.dp)
            .testTag(CHECK_STATE_HERO_TAG),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (val icon = style.icon) {
            is HeroIcon.Vector -> CmpIcon(
                icon = icon.icon,
                contentDescription = null,
                tint = style.content,
                modifier = Modifier.size(22.dp),
            )

            is HeroIcon.Drawable -> CmpIcon(
                painter = painterResource(icon.drawable),
                contentDescription = null,
                tint = style.content,
                modifier = Modifier.size(24.dp),
            )
        }
        val title = when {
            vehicle == null -> Strings.string.devicestate_hero_loading_first
            state.alarm -> Strings.string.devicestate_hero_status_alarm
            else -> vehicle.security.title
        }
        CmpText(
            text = stringResource(title),
            style = CmpTextStyle.Headline3,
            color = style.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        state.staleSeconds?.let { stale ->
            CmpText(
                text = stringResource(Strings.string.devicestate_hero_format_stale, stale),
                style = CmpTextStyle.Paragraph2,
                color = style.content,
                maxLines = 1,
            )
        }
    }
}

private sealed interface HeroIcon {
    data class Vector(val icon: CmpIcons) : HeroIcon

    data class Drawable(val drawable: DrawableResource) : HeroIcon
}

private data class HeroStyle(
    val background: Color,
    val content: Color,
    val icon: HeroIcon,
    val iconBackground: Color,
    val iconTint: Color,
)

@Composable
private fun heroStyle(state: CheckStateState): HeroStyle {
    val colors = cmpThemeColors()
    val vehicle = state.vehicle
    val calm = vehicle == null || (vehicle.security == SecurityState.DISARMED && !state.alarm)
    val (background, content) = when {
        calm -> colors.surfaceContainerHigh to colors.onSurface
        state.alarm -> colors.error to colors.onError
        else -> colors.primary to colors.onPrimary
    }
    val icon = when {
        state.staleSeconds != null -> HeroIcon.Vector(CmpIcons.SyncProblem)
        vehicle == null -> HeroIcon.Vector(CmpIcons.Bluetooth)
        state.alarm -> HeroIcon.Drawable(Res.drawable.devicestate_status_sensors)
        else -> HeroIcon.Drawable(vehicle.security.icon)
    }
    return HeroStyle(
        background = background,
        content = content,
        icon = icon,
        iconBackground = colors.secondaryContainer.takeIf { calm } ?: content.copy(alpha = ICON_BG_ALPHA),
        iconTint = colors.onSecondaryContainer.takeIf { calm } ?: content,
    )
}

private val SecurityState.icon: DrawableResource
    get() = when (this) {
        SecurityState.DISARMED -> Res.drawable.devicestate_status_disarm
        SecurityState.ARMED -> Res.drawable.devicestate_status_arm
        SecurityState.ACTIVE_ARMED -> Res.drawable.devicestate_status_active_arm
    }

@Composable
private fun HeroSubtitle(state: CheckStateState, color: Color) {
    val vehicle = state.vehicle ?: return
    val stale = state.staleSeconds
    val text = when {
        stale != null -> stringResource(Strings.string.devicestate_hero_format_stale, stale)

        state.alarm -> {
            val zones = Zone.entries.filter { vehicle.zone(it).tone == ZoneTone.ALARM }.map { stringResource(it.label) }
            if (zones.isEmpty()) {
                null
            } else {
                stringResource(
                    Strings.string.devicestate_hero_format_alarm,
                    zones.joinToString(),
                )
            }
        }

        else -> vehicle.event?.let { stringResource(it.text) }
    } ?: return
    CmpText(text = text, style = CmpTextStyle.Paragraph2, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun OnlineDot(tick: Int, ring: Color) {
    val scale = remember(tick) { Animatable(DOT_MIN_SCALE) }
    LaunchedEffect(scale) { scale.animateTo(1f, tween(DOT_PULSE_MILLIS)) }
    val a11y = stringResource(Strings.string.devicestate_hero_msg_online)
    CmpBox(
        modifier = Modifier
            .size(44.dp)
            .padding(1.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        CmpBox(
            modifier = Modifier
                .offset(x = 1.dp, y = (-1).dp)
                .size(12.dp)
                .scale(scale.value)
                .background(cmpDomainColors().success, CircleShape)
                .border(2.dp, ring, CircleShape)
                .semantics { contentDescription = a11y },
        )
    }
}

@Composable
private fun GuardFlash(on: Boolean, key: Int, color: Color) {
    if (!on) return
    val alpha = remember(key) { Animatable(FLASH_ALPHA) }
    LaunchedEffect(alpha) { alpha.animateTo(0f, tween(FLASH_MILLIS)) }
    CmpBox(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .drawBehind {
                drawRoundRect(color = color.copy(alpha = alpha.value), cornerRadius = CornerRadius(20.dp.toPx()))
            },
    )
}
