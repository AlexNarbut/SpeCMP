package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItemStatus
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StatusTone

internal enum class TileVariant { HERO, TOPIC, ACTION, SETTING }

internal enum class TileCorner { NONE, MORE, OPENS, LOCK }

/** REGULAR — плитка сетки, DENSE — низкая плитка карусели, ROW — строка от 64 dp (колонка пульта низкого окна). */
internal enum class TileForm { REGULAR, DENSE, ROW }

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

private val TileShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.cardMedium)
private val TileMinHeight = 124.dp
private val HeroMinHeight = 112.dp
private val DenseLeadSize = 40.dp
private val RowMinHeight = 64.dp
private val StatusDotSize = 8.dp
private val PulseRingMax = 6.dp
private val PulseRingMin = 2.dp
private val DashWidth = 1.5.dp
private val DashOn = 6.dp
private val DashOff = 4.dp
private const val PULSE_REPEAT = 2
private const val PULSE_MILLIS = 1_100

/**
 * [width] `null` — по ширине ячейки сетки; [pulseSeq] — новое значение мигает плиткой два раза; [form] DENSE — низкая
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
    form: TileForm = TileForm.REGULAR,
) {
    val colors = CmpTheme.colors
    val filled = content.filled && !content.blocked
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(pulseSeq) {
        if (pulseSeq == null) return@LaunchedEffect
        repeat(PULSE_REPEAT) {
            pulse.snapTo(1f)
            pulse.animateTo(0f, tween(durationMillis = PULSE_MILLIS))
        }
    }
    val tall = content.variant == TileVariant.HERO && width == null
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .tileFrame(
                pulse = { pulse.value },
                ring = colors.brandPrimaryDefault,
                dashed = colors.borderLight.takeIf { content.blocked },
                corner = CmpRadius.cardMedium,
            )
            .testTag(tag),
        shape = TileShape,
        border = tileBorder(content, filled),
        containerColor = tileBackground(content, filled),
    ) {
        when (form) {
            TileForm.DENSE -> {
                DenseTileBody(content, filled)
                return@CmpClickableCard
            }

            TileForm.ROW -> {
                RowTileBody(content, filled)
                return@CmpClickableCard
            }

            TileForm.REGULAR -> Unit
        }
        CmpColumn(
            modifier = Modifier.heightIn(min = if (tall) HeroMinHeight else TileMinHeight)
                .fillMaxHeight()
                .padding(CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            TileTop(content, filled)
            CmpText(
                text = content.title,
                style = if (content.variant == TileVariant.HERO) CmpTextStyle.Headline1 else CmpTextStyle.Headline2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f, fill = false))
            TileBottom(content, filled)
        }
    }
}

// Значок перехода — в нижней строке: рядом с заголовком он отнимал ширину, и короткие слова рвались посередине.
@Composable
private fun DenseTileBody(content: TileContent, filled: Boolean) {
    val hero = content.variant == TileVariant.HERO
    CmpColumn(
        modifier = Modifier.fillMaxHeight().padding(CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TileIcon(content, filled, size = DenseLeadSize)
            CmpColumn(modifier = Modifier.weight(1f)) {
                if (hero && content.kicker != null) {
                    CmpText(
                        text = content.kicker,
                        style = CmpTextStyle.Additional1,
                        color = CmpTheme.colors.brandPrimaryDefault,
                    )
                }
                CmpText(
                    text = content.title,
                    style = if (hero) CmpTextStyle.Headline2 else CmpTextStyle.Headline3,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpBox(modifier = Modifier.weight(1f)) {
                TileBottom(content, filled, maxLines = if (hero) 1 else 2, tagLines = 1)
            }
            TileCornerIcon(content)
        }
    }
}

@Composable
private fun RowTileBody(content: TileContent, filled: Boolean) {
    CmpRow(
        modifier = Modifier.heightIn(min = RowMinHeight)
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TileIcon(content, filled, size = DenseLeadSize)
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(
                text = content.title,
                style = CmpTextStyle.Headline3,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            RowSubtitle(content, filled)
        }
        if (content.value != null) CmpTag(text = content.value) else TileCornerIcon(content)
    }
}

@Composable
private fun RowSubtitle(content: TileContent, filled: Boolean) {
    if (content.subtitle.isBlank()) return
    val colors = CmpTheme.colors
    val tone = content.tone
    val warn = tone == StatusTone.WARN || tone == StatusTone.LOCK
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (tone != null && content.variant == TileVariant.TOPIC && !content.blocked) {
            CmpBox(
                modifier = Modifier.size(StatusDotSize).background(tone.dotColor(), CircleShape),
            )
        }
        CmpText(
            text = content.subtitle,
            style = CmpTextStyle.Paragraph3,
            color = when {
                filled -> colors.foregroundOnDarkPrimaryDefault
                content.blocked || warn -> colors.warningDefault
                else -> colors.foregroundOnDarkSecondaryDefault
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusTone.dotColor(): Color = when (this) {
    StatusTone.OK -> CmpTheme.colors.successDefault
    StatusTone.WARN, StatusTone.LOCK -> CmpTheme.colors.warningDefault
    StatusTone.OFF, StatusTone.NEUTRAL -> CmpTheme.colors.borderLight
}

@Composable
private fun TileIcon(content: TileContent, filled: Boolean, size: Dp = CmpSize.touchMin) {
    val colors = CmpTheme.colors
    val (background, tint) = when {
        filled -> colors.brandPrimaryDefault to colors.onBrand
        content.blocked -> colors.backgroundSurfaceMediumHighlight to colors.foregroundOnDarkSecondaryDefault
        else -> colors.brandPrimaryHighlight to colors.brandPrimaryDefault
    }
    IconBox(icon = content.icon, background = background, tint = tint, size = size)
}

@Composable
private fun tileBackground(content: TileContent, filled: Boolean): Color = when {
    filled -> CmpTheme.colors.brandPrimaryHighlight
    content.blocked -> CmpTheme.colors.backgroundSurfaceHighlight
    else -> Color.Unspecified
}

@Composable
private fun tileBorder(content: TileContent, filled: Boolean): BorderStroke? = when {
    filled -> BorderStroke(CmpBorderWidth.listItem, CmpTheme.colors.brandPrimaryDefault)
    content.blocked -> BorderStroke(CmpBorderWidth.listItem, Color.Transparent)
    else -> null
}

/** Кольцо мигания и пунктир заблокированной плитки ([dashed] — цвет пунктира или `null`). */
internal fun Modifier.tileFrame(pulse: () -> Float, ring: Color, dashed: Color?, corner: Dp): Modifier = drawBehind {
    val radius = CornerRadius(corner.toPx())
    val value = pulse()
    if (value > 0f) {
        drawRoundRect(
            color = ring.copy(alpha = value),
            cornerRadius = radius,
            style = Stroke(width = PulseRingMax.toPx() * (1f - value) + PulseRingMin.toPx()),
        )
    }
    if (dashed != null) {
        drawRoundRect(
            color = dashed,
            cornerRadius = radius,
            style = Stroke(
                width = DashWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashOn.toPx(), DashOff.toPx())),
            ),
        )
    }
}

@Composable
private fun TileTop(content: TileContent, filled: Boolean) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(
            min = if (content.variant == TileVariant.SETTING) CmpSize.iconM else CmpSize.touchMin,
        ),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TileLead(content, filled)
        if (content.variant == TileVariant.HERO && content.kicker != null) {
            CmpIcon(
                icon = CmpIcons.Bolt,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconS),
                tint = CmpTheme.colors.brandPrimaryDefault,
            )
            CmpText(
                text = content.kicker,
                style = CmpTextStyle.Additional0,
                color = CmpTheme.colors.brandPrimaryDefault,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TileCornerIcon(content)
    }
}

@Composable
private fun TileLead(content: TileContent, filled: Boolean) {
    val colors = CmpTheme.colors
    if (content.variant == TileVariant.SETTING) {
        CmpIcon(
            icon = CmpIcons.Tune,
            contentDescription = null,
            modifier = Modifier.size(CmpSize.iconM),
            tint = colors.brandPrimaryDefault,
        )
        CmpText(
            text = content.kicker.orEmpty(),
            style = CmpTextStyle.Additional0,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
        return
    }
    TileIcon(content, filled)
}

@Composable
private fun TileCornerIcon(content: TileContent) {
    val colors = CmpTheme.colors
    val hero = content.variant == TileVariant.HERO && !content.blocked
    val corner = when {
        content.blocked -> CmpIcons.Lock
        content.corner == TileCorner.MORE -> CmpIcons.ChevronRight
        content.corner == TileCorner.OPENS -> CmpIcons.OpenInNew
        hero -> CmpIcons.ArrowForward
        else -> return
    }
    CmpIcon(
        icon = corner,
        contentDescription = null,
        modifier = Modifier.size(CmpSize.iconM),
        tint = if (hero) colors.brandPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
    )
}

@Composable
private fun TileBottom(content: TileContent, filled: Boolean, maxLines: Int = 2, tagLines: Int = 2) {
    val colors = CmpTheme.colors
    val tone = content.tone
    when {
        content.value != null -> CmpTag(text = content.value)

        tone != null && content.variant == TileVariant.TOPIC && !content.blocked ->
            CmpTag(text = content.subtitle, tone = tone.tagTone(), maxLines = tagLines)

        else -> {
            val warn = tone == StatusTone.WARN || tone == StatusTone.LOCK
            CmpText(
                text = content.subtitle,
                style = CmpTextStyle.Paragraph3,
                color = when {
                    filled -> colors.foregroundOnDarkPrimaryDefault
                    content.blocked || warn -> colors.warningDefault
                    else -> colors.foregroundOnDarkSecondaryDefault
                },
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun StatusTone.tagTone(): CmpTagTone = when (this) {
    StatusTone.OK -> CmpTagTone.Success
    StatusTone.WARN, StatusTone.LOCK -> CmpTagTone.Warning
    StatusTone.OFF, StatusTone.NEUTRAL -> CmpTagTone.Neutral
}

internal fun StatusTone.listStatus(): CmpListItemStatus? = when (this) {
    StatusTone.OK -> CmpListItemStatus.Ok
    StatusTone.WARN, StatusTone.LOCK -> CmpListItemStatus.Warn
    StatusTone.OFF, StatusTone.NEUTRAL -> null
}
