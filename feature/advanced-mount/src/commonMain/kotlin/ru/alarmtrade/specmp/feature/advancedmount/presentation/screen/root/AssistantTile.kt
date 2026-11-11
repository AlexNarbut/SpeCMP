package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_label_new
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_assistant_limited
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionPainter
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionSubtitle
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionTitle

internal const val ASSISTANT_TILE_TAG = "advanced_mount_assistant"

/** Плитка «Ассистент» — первая и на всю ширину сетки; [limited] — блок не из базы, действий меньше. */
@Composable
internal fun AssistantTile(limited: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(durationMillis = 260)) }
    val shape = RoundedCornerShape(18.dp)
    CmpSurface(
        modifier = modifier
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 6.dp.toPx()
            }
            .testTag(ASSISTANT_TILE_TAG)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        border = BorderStroke(1.5.dp, colors.primary),
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp)
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpSurface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(16.dp), color = colors.primary) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpIcon(
                        painter = sectionPainter(MountSection.ASSISTANT),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = colors.onPrimary,
                    )
                }
            }
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = sectionTitle(MountSection.ASSISTANT),
                        style = CmpTextStyle.Title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    NewBadge()
                }
                CmpText(
                    text = if (limited) {
                        stringResource(Strings.string.advancedmount_sections_subtitle_assistant_limited)
                    } else {
                        sectionSubtitle(MountSection.ASSISTANT)
                    },
                    style = CmpTextStyle.Body,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CmpIcon(icon = CmpIcons.ChevronRight, contentDescription = null, tint = colors.onPrimaryContainer)
        }
    }
}

@Composable
private fun NewBadge() {
    val colors = cmpThemeColors()
    CmpSurface(modifier = Modifier.height(24.dp).clip(CircleShape), shape = CircleShape, color = colors.primary) {
        CmpBox(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
            CmpText(
                text = stringResource(Strings.string.advancedmount_sections_label_new),
                style = CmpTextStyle.Body,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimary,
                maxLines = 1,
            )
        }
    }
}
