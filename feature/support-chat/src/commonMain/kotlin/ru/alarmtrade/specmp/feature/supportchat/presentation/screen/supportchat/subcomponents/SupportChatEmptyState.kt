package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.support_chat_chip_documents
import specmp.feature.support_chat.generated.resources.support_chat_chip_installation_photos
import specmp.feature.support_chat.generated.resources.support_chat_msg_empty_subtitle
import specmp.feature.support_chat.generated.resources.support_chat_msg_empty_title

@Composable
internal fun SupportChatEmptyState(
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
    ) {
        GhostPreview(colors = colors)

        CmpColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpText(
                text = stringResource(Res.string.support_chat_msg_empty_title),
                style = CmpTextStyle.Title,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Res.string.support_chat_msg_empty_subtitle),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 340.dp),
            )
        }

        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmptyHintChip(
                icon = CmpIcons.Description,
                label = stringResource(Res.string.support_chat_chip_documents),
                colors = colors,
            )
            EmptyHintChip(
                icon = CmpIcons.Image,
                label = stringResource(Res.string.support_chat_chip_installation_photos),
                colors = colors,
            )
        }
    }
}

@Composable
private fun GhostPreview(colors: CmpThemeColors) {
    CmpBox(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GhostBubble(isMine = true, widthFraction = 0.58f, colors = colors)
            GhostBubble(isMine = false, widthFraction = 0.70f, colors = colors)
            GhostBubble(isMine = true, widthFraction = 0.44f, colors = colors)
        }

        CmpSurface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp)
                .size(56.dp),
            shape = RoundedCornerShape(18.dp),
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
            border = BorderStroke(
                width = 1.dp,
                color = colors.primary.copy(alpha = 0.25f),
            ),
            shadowElevation = 2.dp,
        ) {
            CmpBox(contentAlignment = Alignment.Center) {
                CmpIcon(
                    icon = CmpIcons.SupportAgent,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun GhostBubble(
    isMine: Boolean,
    widthFraction: Float,
    colors: CmpThemeColors,
) {
    CmpBox(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        CmpBox(
            modifier = Modifier
                .fillMaxWidth(fraction = widthFraction)
                .drawBehind {
                    val radius = 14.dp.toPx()
                    drawRoundRect(
                        color = if (isMine) {
                            colors.primary.copy(alpha = 0.06f)
                        } else {
                            colors.onSurface.copy(alpha = 0.035f)
                        },
                        cornerRadius = CornerRadius(radius, radius),
                    )
                    drawRoundRect(
                        color = if (isMine) {
                            colors.primary.copy(alpha = 0.34f)
                        } else {
                            colors.onSurface.copy(alpha = 0.22f)
                        },
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(6.dp.toPx(), 5.dp.toPx()),
                            ),
                        ),
                    )
                }
                .padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val weights = listOf(0.5f, 0.3f, 0.4f)
                weights.forEach { weight ->
                    CmpBox(
                        modifier = Modifier
                            .weight(weight)
                            .size(height = 6.dp, width = 0.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(colors.onSurface.copy(alpha = 0.18f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHintChip(
    icon: CmpIcons,
    label: String,
    colors: CmpThemeColors,
) {
    CmpSurface(
        shape = RoundedCornerShape(percent = 50),
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant,
        ),
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp),
            )
            CmpText(text = label)
        }
    }
}
