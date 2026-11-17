package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_hint_documents
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_hint_installation_photos
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_msg_empty_subtitle
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_msg_empty_title
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
internal fun SupportChatEmptyState(modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space24, Alignment.CenterVertically),
    ) {
        GhostPreview()

        CmpColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpText(
                text = stringResource(Strings.string.supportchat_chat_msg_empty_title),
                style = CmpTextStyle.Headline1,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Strings.string.supportchat_chat_msg_empty_subtitle),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = EMPTY_TEXT_MAX_WIDTH),
            )
        }

        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            EmptyHintChip(
                icon = CmpIcons.AttachFile,
                label = stringResource(Strings.string.supportchat_chat_hint_documents),
            )
            EmptyHintChip(
                icon = CmpIcons.Image,
                label = stringResource(Strings.string.supportchat_chat_hint_installation_photos),
            )
        }
    }
}

@Composable
private fun GhostPreview() {
    val colors = CmpTheme.colors
    val badgeShape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpBox(
        modifier = Modifier
            .widthIn(max = GHOST_PREVIEW_MAX_WIDTH)
            .fillMaxWidth()
            .padding(bottom = CmpSpacing.space8),
        contentAlignment = Alignment.Center,
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            GhostBubble(isMine = true, widthFraction = 0.58f)
            GhostBubble(isMine = false, widthFraction = 0.70f)
            GhostBubble(isMine = true, widthFraction = 0.44f)
        }

        CmpBox(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = CmpSpacing.space24)
                .size(CmpSize.iconXxl)
                .background(colors.backgroundPagePrimary, badgeShape)
                .background(colors.brandPrimaryHighlight, badgeShape)
                .border(CmpBorderWidth.listItem, colors.borderLight, badgeShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.SupportAgent,
                contentDescription = null,
                tint = colors.brandPrimaryDefault,
                modifier = Modifier.size(CmpSize.iconL),
            )
        }
    }
}

@Composable
private fun GhostBubble(isMine: Boolean, widthFraction: Float) {
    val colors = CmpTheme.colors
    val fill = if (isMine) colors.brandPrimaryHighlight else colors.backgroundSurfaceUltraHighlight
    val stroke = if (isMine) colors.brandPrimaryDisabled else colors.borderMediumHighlight
    val bar = colors.backgroundSurfaceLight
    val corner = CmpRadius.cardSmall
    CmpBox(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        CmpBox(
            modifier = Modifier
                .fillMaxWidth(fraction = widthFraction)
                .drawBehind {
                    val radius = corner.toPx()
                    drawRoundRect(color = fill, cornerRadius = CornerRadius(radius, radius))
                    drawRoundRect(
                        color = stroke,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(
                            width = CmpBorderWidth.listItem.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(GHOST_DASH.toPx(), GHOST_GAP.toPx()),
                            ),
                        ),
                    )
                }
                .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space16),
        ) {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                modifier = Modifier.fillMaxWidth(),
            ) {
                GHOST_BAR_WEIGHTS.forEach { weight ->
                    CmpBox(
                        modifier = Modifier
                            .weight(weight)
                            .height(GHOST_BAR_HEIGHT)
                            .background(bar, RoundedCornerShape(CmpRadius.round)),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHintChip(icon: CmpIcons, label: String) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.round)
    CmpRow(
        modifier = Modifier
            .background(colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.chips, colors.borderHighlight, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = colors.brandPrimaryDefault,
            modifier = Modifier.size(CmpSize.iconS),
        )
        CmpText(
            text = label,
            style = CmpTextStyle.Headline3,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

private val EMPTY_TEXT_MAX_WIDTH = 340.dp
private val GHOST_PREVIEW_MAX_WIDTH = 300.dp
private val GHOST_BAR_HEIGHT = 6.dp
private val GHOST_DASH = 6.dp
private val GHOST_GAP = 5.dp
private val GHOST_BAR_WEIGHTS = listOf(0.5f, 0.3f, 0.4f)
