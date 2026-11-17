package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_label_new
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_assistant_limited
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
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
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
    val colors = CmpTheme.colors
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.tileAppear(appearIndex = 0).testTag(ASSISTANT_TILE_TAG),
        border = BorderStroke(CmpBorderWidth.listItem, colors.brandPrimaryDefault),
        containerColor = colors.brandPrimaryHighlight,
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.rowSection)
                .padding(start = CmpSpacing.space16, end = CmpSpacing.space12)
                .padding(vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpBox(
                modifier = Modifier.size(CmpSize.iconXxl)
                    .background(colors.brandPrimaryDefault, RoundedCornerShape(CmpRadius.cardMedium)),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    painter = sectionPainter(MountSection.ASSISTANT),
                    contentDescription = null,
                    modifier = Modifier.size(CmpSize.iconL),
                    tint = colors.onBrand,
                )
            }
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = sectionTitle(MountSection.ASSISTANT),
                        style = CmpTextStyle.Headline1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    CmpTag(
                        text = stringResource(Strings.string.advancedmount_sections_label_new),
                        tone = CmpTagTone.Brand,
                    )
                }
                CmpText(
                    text = if (limited) {
                        stringResource(Strings.string.advancedmount_sections_subtitle_assistant_limited)
                    } else {
                        sectionSubtitle(MountSection.ASSISTANT)
                    },
                    style = CmpTextStyle.Paragraph2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CmpIcon(icon = CmpIcons.ChevronRight, contentDescription = null, tint = colors.brandPrimaryDefault)
        }
    }
}
