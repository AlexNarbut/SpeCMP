package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_status_changed
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone

private const val FLASH_MILLIS = 1_800
private const val FLASH_ALPHA = 0.35f
private val FlashBorder = 2.dp

private val SkeletonRows = listOf(6, 5, 4)
private const val SKELETON_TITLE_FRACTION = 0.45f
private val TagMaxWidth = 160.dp
private const val TAG_MAX_LINES = 2

internal data class ToneColors(val row: Color, val icon: Color, val iconTint: Color, val text: Color)

internal fun CmpColors.tone(tone: ZoneTone): ToneColors = when (tone) {
    ZoneTone.REST -> ToneColors(
        row = Color.Transparent,
        icon = backgroundSurfaceHighlight,
        iconTint = foregroundOnDarkSecondaryDefault,
        text = foregroundOnDarkPrimaryDefault,
    )

    ZoneTone.ACTIVE -> ToneColors(
        row = brandPrimaryHighlight,
        icon = brandPrimaryDefault,
        iconTint = onBrand,
        text = foregroundOnDarkPrimaryDefault,
    )

    ZoneTone.ALARM -> ToneColors(
        row = errorHighlight,
        icon = errorDefault,
        iconTint = staticWhite,
        text = foregroundOnDarkPrimaryDefault,
    )

    ZoneTone.MUTED -> ToneColors(
        row = Color.Transparent,
        icon = backgroundSurfaceHighlight,
        iconTint = foregroundOnDarkTertiary,
        text = foregroundOnDarkPrimaryDefault,
    )
}

internal fun CmpColors.accent(tone: ZoneTone): Color = when (tone) {
    ZoneTone.ALARM -> errorDefault
    ZoneTone.ACTIVE -> brandPrimaryDefault
    ZoneTone.REST, ZoneTone.MUTED -> foregroundOnDarkPrimaryDefault
}

internal fun ZoneTone.tagTone(): CmpTagTone = when (this) {
    ZoneTone.REST -> CmpTagTone.Neutral
    ZoneTone.ACTIVE -> CmpTagTone.Brand
    ZoneTone.ALARM -> CmpTagTone.Error
    ZoneTone.MUTED -> CmpTagTone.Outline
}

@Composable
internal fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    aside: String? = null,
    contentPadding: Dp = CmpSpacing.space12,
    content: @Composable ColumnScope.() -> Unit,
) {
    CmpCard(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(
                    start = CmpSpacing.space16,
                    end = CmpSpacing.space16,
                    top = CmpSpacing.space16,
                    bottom = CmpSpacing.space4,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpText(text = title, style = CmpTextStyle.Headline2, modifier = Modifier.weight(1f))
                if (aside != null) {
                    CmpText(
                        text = aside,
                        style = CmpTextStyle.Additional0,
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
        }
        CmpColumn(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
internal fun ValuePill(text: String, tone: ZoneTone, modifier: Modifier = Modifier) {
    CmpTag(
        text = text,
        tone = tone.tagTone(),
        maxLines = TAG_MAX_LINES,
        modifier = modifier.widthIn(max = TagMaxWidth),
    )
}

@Composable
internal fun ChangedTag(modifier: Modifier = Modifier) {
    CmpTag(
        text = stringResource(Strings.string.devicestate_zones_status_changed),
        tone = CmpTagTone.Brand,
        modifier = modifier,
    )
}

/** [key] — номер снимка: новый снимок с тем же изменением даёт новую вспышку. */
@Composable
internal fun BoxScope.ChangeFlash(on: Boolean, key: Int, color: Color, radius: Dp = CmpRadius.cardSmall) {
    if (!on) return
    val alpha = remember(key) { Animatable(FLASH_ALPHA) }
    LaunchedEffect(alpha) { alpha.animateTo(0f, tween(FLASH_MILLIS)) }
    CmpBox(
        modifier = Modifier
            .matchParentSize()
            .drawBehind {
                val corner = CornerRadius(radius.toPx())
                drawRoundRect(color = color.copy(alpha = alpha.value), cornerRadius = corner)
            }
            .border(FlashBorder, color, RoundedCornerShape(radius)),
    )
}

@Composable
internal fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tone: ZoneTone = ZoneTone.REST,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.touchMin)
            .padding(horizontal = CmpSpacing.space4, vertical = CmpSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpText(
            text = label,
            style = CmpTextStyle.Paragraph2,
            color = colors.foregroundOnDarkPrimaryDefault,
            modifier = Modifier.weight(1f),
        )
        CmpRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            trailing?.invoke()
            CmpText(text = value, style = CmpTextStyle.Headline3, color = colors.accent(tone))
        }
    }
}

@Composable
internal fun MetricTile(label: String, value: String, unit: String?, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = modifier
            .heightIn(min = TileMinHeight)
            .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.cardSmall))
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4, Alignment.CenterVertically),
    ) {
        CmpText(
            text = label,
            style = CmpTextStyle.Paragraph3,
            color = colors.foregroundOnDarkSecondaryDefault,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CmpRow(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = value, style = CmpTextStyle.Headline1, color = colors.foregroundOnDarkPrimaryDefault)
            if (unit != null) {
                CmpText(text = unit, style = CmpTextStyle.Paragraph2, color = colors.foregroundOnDarkSecondaryDefault)
            }
        }
    }
}

private val TileMinHeight = 72.dp

@Composable
internal fun ZonesSkeleton(modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        SkeletonRows.forEach { rows ->
            SectionCard {
                CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                    CmpSkeleton(
                        modifier = Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION).heightIn(min = CmpSpacing.space24),
                    )
                    repeat(rows) {
                        CmpRow(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                        ) {
                            CmpSkeleton(modifier = Modifier.size(CmpSize.iconXl), shape = CircleShape)
                            CmpSkeleton(modifier = Modifier.weight(1f).heightIn(min = CmpSpacing.space20))
                            CmpSkeleton(modifier = Modifier.widthIn(min = TileMinHeight).heightIn(min = CmpSize.tag))
                        }
                    }
                }
            }
        }
    }
}
