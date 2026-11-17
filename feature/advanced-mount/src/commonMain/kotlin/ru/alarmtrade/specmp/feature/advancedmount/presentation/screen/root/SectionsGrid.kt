package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_msg_locked
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_support_chat_unread
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_support_chat_unread
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionPainter
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionSubtitle
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionTitle

private const val FIRMWARE_CHECK_TAG = "advanced_mount_firmware_check"
private val BadgeRing = 2.dp
private val BadgeOffset = 6.dp

@Composable
internal fun SectionsGrid(
    sections: List<MountSection>,
    layout: MountLayout,
    canLocked: Boolean,
    unreadCount: Int,
    limited: Boolean,
    onClick: (MountSection) -> Unit,
) {
    val columns = if (layout == MountLayout.Compact) 1 else 2
    val featured = MountSection.ASSISTANT.takeIf { it in sections }
    val first = if (featured != null) 1 else 0
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        if (featured != null) {
            AssistantTile(limited = limited, onClick = { onClick(featured) }, modifier = Modifier.fillMaxWidth())
        }
        sections.filter { it != featured }.chunked(columns).forEachIndexed { rowIndex, row ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                row.forEachIndexed { columnIndex, section ->
                    SectionTile(
                        section = section,
                        locked = canLocked && section.needsCan,
                        unreadCount = if (section == MountSection.SUPPORT_CHAT) unreadCount else 0,
                        big = layout != MountLayout.Compact,
                        appearIndex = first + rowIndex * columns + columnIndex,
                        onClick = { onClick(section) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                repeat(columns - row.size) { CmpBox(modifier = Modifier.weight(1f)) {} }
            }
        }
    }
}

@Composable
internal fun Modifier.tileAppear(appearIndex: Int): Modifier {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(durationMillis = 260, delayMillis = appearIndex * 35)) }
    return graphicsLayer {
        alpha = appear.value
        translationY = (1f - appear.value) * 6.dp.toPx()
    }
}

@Composable
private fun SectionTile(
    section: MountSection,
    locked: Boolean,
    unreadCount: Int,
    big: Boolean,
    appearIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier
            .tileAppear(appearIndex)
            .then(if (section == MountSection.FIRMWARE_UPDATE) Modifier.testTag(FIRMWARE_CHECK_TAG) else Modifier),
    ) {
        CmpRow(
            modifier = Modifier.fillMaxSize().heightIn(min = if (big) CmpSize.rowSection else CmpSize.listItem)
                .padding(start = CmpSpacing.space16, end = CmpSpacing.space12)
                .padding(vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionIconBox(section, locked, unreadCount)
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                val unread = section == MountSection.SUPPORT_CHAT && unreadCount > 0
                CmpText(
                    text = if (unread) {
                        stringResource(Strings.string.advancedmount_sections_title_support_chat_unread)
                    } else {
                        sectionTitle(section)
                    },
                    style = CmpTextStyle.Headline2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                CmpText(
                    text = when {
                        locked -> stringResource(Strings.string.advancedmount_sections_msg_locked)
                        unread -> stringResource(Strings.string.advancedmount_sections_subtitle_support_chat_unread)
                        else -> sectionSubtitle(section)
                    },
                    style = if (locked) CmpTextStyle.Additional0 else CmpTextStyle.Paragraph3,
                    color = if (locked) {
                        colors.foregroundOnDarkPrimaryDefault
                    } else {
                        colors.foregroundOnDarkSecondaryDefault
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun SectionIconBox(section: MountSection, locked: Boolean, unreadCount: Int) {
    val colors = CmpTheme.colors
    CmpBox(modifier = Modifier.size(CmpSize.iconXxl)) {
        CmpBox(
            modifier = Modifier.fillMaxSize().background(
                if (locked) colors.warningHighlight else colors.brandPrimaryHighlight,
                RoundedCornerShape(CmpRadius.cardSmall),
            ),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                painter = sectionPainter(section),
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconL),
                tint = if (locked) colors.foregroundOnDarkPrimaryDefault else colors.brandPrimaryDefault,
            )
        }
        if (locked) {
            CmpBox(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = BadgeOffset, y = BadgeOffset)
                    .size(CmpSize.iconM + BadgeRing * 2)
                    .border(BadgeRing, colors.backgroundPagePrimary, CircleShape)
                    .padding(BadgeRing)
                    .background(colors.backgroundSurfacePrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = CmpIcons.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(CmpSize.iconS),
                    tint = colors.foregroundOnLightPrimaryDefault,
                )
            }
        }
        if (unreadCount > 0) {
            CmpBox(
                modifier = Modifier.align(Alignment.TopStart).offset(x = -BadgeOffset, y = -BadgeOffset)
                    .heightIn(min = CmpSize.iconM + BadgeRing * 2)
                    .widthIn(min = CmpSize.iconM + BadgeRing * 2)
                    .border(BadgeRing, colors.backgroundPagePrimary, CircleShape)
                    .padding(BadgeRing)
                    .background(colors.brandPrimaryDefault, CircleShape)
                    .padding(horizontal = BadgeOffset),
                contentAlignment = Alignment.Center,
            ) {
                CmpText(text = unreadCount.toString(), style = CmpTextStyle.Additional1, color = colors.onBrand)
            }
        }
    }
}
