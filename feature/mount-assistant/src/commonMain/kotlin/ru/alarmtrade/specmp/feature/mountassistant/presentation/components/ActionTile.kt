package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StatusTone

internal enum class TileVariant { HERO, TOPIC, ACTION, SETTING }

internal enum class TileCorner { NONE, MORE, OPENS, LOCK }

/** Плитка пульта: содержимое уже переведено в строки; [filled] — следующий шаг или рекомендуемое действие. */
internal data class TileContent(
    val variant: TileVariant,
    val title: String,
    val subtitle: String,
    val icon: CmpIcons,
    val filled: Boolean = false,
    val blocked: Boolean = false,
    val corner: TileCorner = TileCorner.NONE,
    val tone: StatusTone? = null,
    val kicker: String? = null,
    val value: String? = null,
)

private val TileShape = RoundedCornerShape(20.dp)
private const val PULSE_REPEAT = 2
private const val PULSE_MILLIS = 1_100
private const val FILLED_ICON_ALPHA = 0.18f

/**
 * [width] `null` — по ширине ячейки сетки; [pulseSeq] — новое значение мигает плиткой два раза; [dense] — низкая
 * плитка карусели: заголовок рядом с иконкой, подпись в одну строку, высоту задаёт вызывающий.
 */
@Composable
internal fun ActionTile(
    content: TileContent,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    pulseSeq: Long? = null,
    dense: Boolean = false,
) {
    val colors = cmpThemeColors()
    val filled = content.filled && !content.blocked
    val background = tileBackground(content, filled)
    val foreground = if (filled) colors.onPrimary else colors.onSurface
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(pulseSeq) {
        if (pulseSeq == null) return@LaunchedEffect
        repeat(PULSE_REPEAT) {
            pulse.snapTo(1f)
            pulse.animateTo(0f, tween(durationMillis = PULSE_MILLIS))
        }
    }
    val tall = content.variant == TileVariant.HERO && width == null
    CmpSurface(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .then(if (dense) Modifier else Modifier.heightIn(min = if (tall) 112.dp else 124.dp))
            .tileFrame(pulse = {
                pulse.value
            }, ring = colors.primary, dashed = colors.outline.takeIf { content.blocked })
            .testTag(tag)
            .clip(TileShape)
            .clickable(onClick = onClick),
        shape = TileShape,
        color = background,
        contentColor = foreground,
        border = if (filled || content.blocked) null else BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = if (filled) 1.dp else 0.dp,
    ) {
        if (dense) {
            DenseTileBody(content, filled, foreground)
            return@CmpSurface
        }
        CmpColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TileTop(content, filled, foreground)
            CmpText(
                text = content.title,
                style = if (content.variant == TileVariant.HERO) CmpTextStyle.Headline1 else CmpTextStyle.Paragraph1,
                color = foreground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f, fill = false))
            TileBottom(content, filled, foreground)
        }
    }
}

// Значок перехода — в нижней строке: рядом с заголовком он отнимал ширину, и короткие слова рвались посередине.
@Composable
private fun DenseTileBody(content: TileContent, filled: Boolean, foreground: Color) {
    val hero = content.variant == TileVariant.HERO
    CmpColumn(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            DenseLead(content, filled)
            CmpColumn(modifier = Modifier.weight(1f)) {
                if (hero && content.kicker != null) {
                    CmpText(
                        text = content.kicker,
                        style = CmpTextStyle.Paragraph2,
                        color = foreground,
                    )
                }
                CmpText(
                    text = content.title,
                    style = CmpTextStyle.Paragraph1,
                    color = foreground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        CmpRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            CmpBox(modifier = Modifier.weight(1f)) {
                TileBottom(content, filled, foreground, maxLines = if (hero) 1 else 2)
            }
            TileCornerIcon(content, filled, foreground)
        }
    }
}

@Composable
private fun DenseLead(content: TileContent, filled: Boolean) {
    val colors = cmpThemeColors()
    val (background, tint) = when {
        filled -> colors.onPrimary.copy(alpha = FILLED_ICON_ALPHA) to colors.onPrimary
        content.blocked -> colors.surfaceContainerHighest to colors.onSurfaceVariant
        else -> colors.primaryContainer to colors.onPrimaryContainer
    }
    IconBox(icon = content.icon, background = background, tint = tint, size = 36.dp, iconSize = 20.dp)
}

@Composable
private fun tileBackground(content: TileContent, filled: Boolean): Color {
    val colors = cmpThemeColors()
    return when {
        filled -> colors.primary
        content.blocked -> colors.surfaceContainer
        else -> assistantCardColor()
    }
}

/** Кольцо мигания и пунктир заблокированной плитки ([dashed] — цвет пунктира или `null`). */
internal fun Modifier.tileFrame(pulse: () -> Float, ring: Color, dashed: Color?, corner: Dp = 20.dp): Modifier =
    drawBehind {
        val radius = CornerRadius(corner.toPx())
        val value = pulse()
        if (value > 0f) {
            drawRoundRect(
                color = ring.copy(alpha = value),
                cornerRadius = radius,
                style = Stroke(width = 6.dp.toPx() * (1f - value) + 2.dp.toPx()),
            )
        }
        if (dashed != null) {
            drawRoundRect(
                color = dashed,
                cornerRadius = radius,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                ),
            )
        }
    }

@Composable
private fun TileTop(content: TileContent, filled: Boolean, foreground: Color) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = if (content.variant == TileVariant.SETTING) 24.dp else 40.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TileLead(content, filled)
        if (content.variant == TileVariant.HERO && content.kicker != null) {
            CmpIcon(icon = CmpIcons.Bolt, contentDescription = null, modifier = Modifier.size(18.dp), tint = foreground)
            CmpText(
                text = content.kicker,
                style = CmpTextStyle.Paragraph2,
                color = foreground,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TileCornerIcon(content, filled, foreground)
    }
}

@Composable
private fun TileLead(content: TileContent, filled: Boolean) {
    val colors = cmpThemeColors()
    if (content.variant == TileVariant.SETTING) {
        CmpIcon(icon = CmpIcons.Tune, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.primary)
        CmpText(
            text = content.kicker.orEmpty(),
            style = CmpTextStyle.Paragraph2,
            color = colors.onSurfaceVariant,
        )
        return
    }
    val (background, tint) = when {
        filled -> colors.onPrimary.copy(alpha = FILLED_ICON_ALPHA) to colors.onPrimary
        content.blocked -> colors.surfaceContainerHighest to colors.onSurfaceVariant
        else -> colors.primaryContainer to colors.onPrimaryContainer
    }
    IconBox(icon = content.icon, background = background, tint = tint)
}

@Composable
private fun TileCornerIcon(content: TileContent, filled: Boolean, foreground: Color) {
    val colors = cmpThemeColors()
    val corner = when {
        content.blocked -> CmpIcons.Lock
        content.corner == TileCorner.MORE -> CmpIcons.ChevronRight
        content.corner == TileCorner.OPENS -> CmpIcons.OpenInNew
        else -> null
    }
    if (corner != null) {
        CmpIcon(
            icon = corner,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (filled) foreground else colors.onSurfaceVariant,
        )
    }
    if (content.variant == TileVariant.HERO && !content.blocked) {
        CmpIcon(icon = CmpIcons.ArrowForward, contentDescription = null, tint = foreground)
    }
}

@Composable
private fun TileBottom(content: TileContent, filled: Boolean, foreground: Color, maxLines: Int = 2) {
    val colors = cmpThemeColors()
    if (content.value != null) {
        AssistantPill(text = content.value)
        return
    }
    val tone = content.tone
    val dot = tone?.takeIf {
        content.variant == TileVariant.TOPIC && !content.blocked
    }?.let { toneColor(it, colors.outline) }
    CmpRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (dot != null) CmpBox(modifier = Modifier.size(8.dp).background(dot, CircleShape)) {}
        val warn = tone == StatusTone.WARN || tone == StatusTone.LOCK
        CmpText(
            text = content.subtitle,
            style = CmpTextStyle.Paragraph2,
            color = when {
                filled -> foreground
                content.blocked || warn -> colors.tertiary
                else -> colors.onSurfaceVariant
            },
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun toneColor(tone: StatusTone, neutral: Color): Color {
    val colors = cmpThemeColors()
    return when (tone) {
        StatusTone.OK -> cmpDomainColors().success
        StatusTone.WARN, StatusTone.LOCK -> colors.tertiary
        StatusTone.OFF, StatusTone.NEUTRAL -> neutral
    }
}
