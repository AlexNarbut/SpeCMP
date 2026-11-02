package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_status_changed
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone

private const val FLASH_MILLIS = 1_800
private const val FLASH_ALPHA = 0.35f
private const val ROW_ACTIVE_ALPHA = 0.75f
internal val CardShape = RoundedCornerShape(20.dp)

// Скелетон «Подробно»: три группы зон по 6 / 5 / 4 строки (как в макете).
private val SkeletonRows = listOf(6, 5, 4)
private const val SKELETON_TITLE_FRACTION = 0.45f
private val PILL_MAX_WIDTH = 150.dp

/** Цвета тона зоны (макет `TONE`). */
internal data class ToneColors(
    val row: Color,
    val pill: Color,
    val pillText: Color,
    val icon: Color,
    val iconTint: Color,
    val text: Color
)

internal fun CmpThemeColors.tone(tone: ZoneTone): ToneColors = when (tone) {
    ZoneTone.REST -> ToneColors(
        row = Color.Transparent,
        pill = surfaceContainerHighest,
        pillText = onSurface,
        icon = surfaceContainerHighest,
        iconTint = onSurfaceVariant,
        text = onSurface
    )

    ZoneTone.ACTIVE -> ToneColors(
        row = primaryContainer.copy(alpha = ROW_ACTIVE_ALPHA),
        pill = primary,
        pillText = onPrimary,
        icon = primary,
        iconTint = onPrimary,
        text = onPrimaryContainer
    )

    ZoneTone.ALARM -> ToneColors(
        row = errorContainer,
        pill = error,
        pillText = onError,
        icon = error,
        iconTint = onError,
        text = onErrorContainer
    )

    ZoneTone.MUTED -> ToneColors(
        row = Color.Transparent,
        pill = Color.Transparent,
        pillText = onSurfaceVariant,
        icon = surfaceContainerHigh,
        iconTint = onSurfaceVariant,
        text = onSurface
    )
}

/** Акцент значения: тревога — error, активно — primary, иначе обычный текст. */
internal fun CmpThemeColors.accent(tone: ZoneTone): Color = when (tone) {
    ZoneTone.ALARM -> error
    ZoneTone.ACTIVE -> primary
    ZoneTone.REST, ZoneTone.MUTED -> onSurface
}

/** Карточка-секция: заголовок слева, итог справа. */
@Composable
internal fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    aside: String? = null,
    contentPadding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpColumn {
            if (title != null) {
                CmpRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CmpText(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (aside != null) CmpText(text = aside, fontSize = 14.sp, color = colors.onSurfaceVariant)
                }
            }
            CmpColumn(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

/** Плашка значения зоны. */
@Composable
internal fun ValuePill(text: String, tone: ZoneTone, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors().tone(tone)
    val outline = cmpThemeColors().outline
    CmpBox(
        modifier = modifier
            .widthIn(max = PILL_MAX_WIDTH)
            .heightIn(min = 32.dp)
            .background(colors.pill, RoundedCornerShape(16.dp))
            .then(if (tone == ZoneTone.MUTED) Modifier.border(1.dp, outline, RoundedCornerShape(16.dp)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        CmpText(
            text = text,
            color = colors.pillText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/** Метка «Изменилось». */
@Composable
internal fun ChangedTag(modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpBox(
        modifier = modifier
            .heightIn(min = 22.dp)
            .background(colors.primary, CircleShape)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CmpText(
            text = stringResource(Strings.string.devicestate_zones_status_changed),
            color = colors.onPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * Подсветка изменения: вспышка (гаснет за 1,8 с) и обводка — пока [on]. [key] — номер снимка: новый снимок с тем же
 * изменением даёт новую вспышку.
 */
@Composable
internal fun BoxScope.ChangeFlash(on: Boolean, key: Int, color: Color, radius: Dp = 14.dp) {
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
            .border(2.dp, color, RoundedCornerShape(radius))
    )
}

/** Строка «подпись — значение» карточек «Двигатель», «Связь», «Дополнительно». */
@Composable
internal fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tone: ZoneTone = ZoneTone.REST,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpText(text = label, fontSize = 16.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            trailing?.invoke()
            CmpText(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.accent(tone))
        }
    }
}

/** Баннер под статусом: режим ТО / программирования, данные устарели, выход из программирования. */
@Composable
internal fun StateBanner(
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: CmpIcons? = null,
    neutral: Boolean = false,
    busy: Boolean = false,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    val colors = cmpThemeColors()
    val background = if (neutral) colors.surfaceContainerHighest else colors.tertiaryContainer
    val content = if (neutral) colors.onSurface else colors.onTertiaryContainer
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(16.dp))
            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            busy -> CmpCircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = content)

            icon != null -> CmpIcon(
                icon = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(26.dp)
            )
        }
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(text = title, color = content, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (text != null) CmpText(text = text, color = content, fontSize = 14.sp)
        }
        if (action != null) CmpOutlinedButton(text = action, onClick = onAction)
    }
}

/** Плитка показателя: подпись сверху, крупное значение с единицей. */
@Composable
internal fun MetricTile(label: String, value: String, unit: String?, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = modifier
            .heightIn(min = 72.dp)
            .background(colors.surfaceContainerHigh, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
    ) {
        CmpText(
            text = label,
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CmpRow(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpText(text = value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
            if (unit != null) CmpText(text = unit, fontSize = 16.sp, color = colors.onSurfaceVariant)
        }
    }
}

/** Скелетон карточки зон (ждём первый снимок). */
@Composable
internal fun ZonesSkeleton(modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkeletonRows.forEach { rows ->
            SectionCard {
                CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION).heightIn(min = 22.dp))
                    repeat(rows) {
                        CmpRow(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CmpSkeleton(modifier = Modifier.size(44.dp), shape = CircleShape)
                            CmpSkeleton(modifier = Modifier.weight(1f).heightIn(min = 18.dp))
                            CmpSkeleton(modifier = Modifier.widthIn(min = 72.dp).heightIn(min = 30.dp))
                        }
                    }
                }
            }
        }
    }
}
