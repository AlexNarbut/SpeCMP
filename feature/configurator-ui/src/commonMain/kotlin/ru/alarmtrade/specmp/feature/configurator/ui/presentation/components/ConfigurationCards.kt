package ru.alarmtrade.specmp.feature.configurator.ui.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_format_meta
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_label_only_author
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_more
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_rating
import ru.alarmtrade.specmp.core.ui.format.rememberCmpDateLabeler
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.ConfiguratorTestTags
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ModelBadge

@Composable
internal fun rememberDateTimeText(): (LocalDateTime) -> String {
    val dateLabel = rememberCmpDateLabeler()
    return { time ->
        "${dateLabel(time.date)}, ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    }
}

@Composable
internal fun RatingLabel(rating: Int?, modifier: Modifier = Modifier) {
    rating ?: return
    val description = stringResource(Strings.string.configurator_common_a11y_rating, rating)
    CmpRow(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        CmpIcon(
            icon = CmpIcons.Star,
            contentDescription = null,
            tint = cmpDomainColors().rating,
            modifier = Modifier.size(CmpSize.iconM),
        )
        CmpText(text = rating.toString(), style = CmpTextStyle.Headline3)
    }
}

@Composable
internal fun OnlyAuthorTag(modifier: Modifier = Modifier) {
    CmpTag(
        text = stringResource(Strings.string.configurator_cloud_label_only_author),
        tone = CmpTagTone.Warning,
        icon = CmpIcons.Lock,
        modifier = modifier,
    )
}

private val SelectedBorder = BorderStroke(CmpBorderWidth.listItem, Color.Transparent)

@Composable
private fun cardContainer(selected: Boolean): Color =
    if (selected) CmpTheme.colors.backgroundSurfaceHighlight else Color.Unspecified

@Composable
internal fun MineCard(
    item: LocalConfiguration,
    selected: Boolean,
    dateText: (LocalDateTime) -> String,
    onClick: () -> Unit,
    onMenu: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(ConfiguratorTestTags.mine(item.id)),
        border = if (selected) SelectedBorder else null,
        containerColor = cardContainer(selected),
    ) {
        CmpRow(
            modifier = Modifier.padding(
                start = CmpSpacing.space16,
                top = CmpSpacing.space12,
                bottom = CmpSpacing.space12,
                end = if (onMenu != null) CmpSpacing.space4 else CmpSpacing.space16,
            ),
        ) {
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpText(
                    text = item.name,
                    style = CmpTextStyle.Headline2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                CmpFlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                ) {
                    ModelBadge(item.deviceName)
                    CmpText(
                        text = dateText(item.updated),
                        style = CmpTextStyle.Paragraph3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                if (item.description.isNotBlank()) {
                    CmpText(
                        text = item.description,
                        style = CmpTextStyle.Paragraph2,
                        color = colors.foregroundOnDarkSecondaryDefault,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            onMenu?.let {
                CmpIconButton(
                    onClick = it,
                    icon = CmpIcons.MoreVert,
                    contentDescription = stringResource(Strings.string.configurator_common_a11y_more),
                    tint = colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.testTag(ConfiguratorTestTags.mineMenu(item.id)),
                )
            }
        }
    }
}

@Composable
internal fun CloudItemCard(
    item: CloudConfigurationSummary,
    selected: Boolean,
    dateText: (LocalDateTime) -> String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(ConfiguratorTestTags.cloud(item.id)),
        border = if (selected) SelectedBorder else null,
        containerColor = cardContainer(selected),
    ) {
        CmpColumn(
            modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpRow(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpText(
                    text = item.name,
                    style = CmpTextStyle.Headline2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                RatingLabel(item.rating)
            }
            if (item.shortDescription.isNotBlank()) {
                CmpText(
                    text = item.shortDescription,
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CmpText(
                text = item.car.title,
                style = CmpTextStyle.Paragraph2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CmpFlowRow(
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            ) {
                ModelBadge(item.deviceName)
                if (item.onlyForAuthor) OnlyAuthorTag()
            }
            CmpText(
                text = stringResource(
                    Strings.string.configurator_cloud_format_meta,
                    item.author,
                    dateText(item.updated),
                ),
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}
