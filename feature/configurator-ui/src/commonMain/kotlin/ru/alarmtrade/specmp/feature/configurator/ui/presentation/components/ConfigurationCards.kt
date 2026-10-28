package ru.alarmtrade.specmp.feature.configurator.ui.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_format_meta
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_label_only_author
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_more
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_rating
import ru.alarmtrade.specmp.core.ui.format.rememberCmpDateLabeler
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.ConfiguratorTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ModelBadge

/** Дата и время изменения: «Сегодня, 14:20», «21 октября, 09:05». */
@Composable
internal fun rememberDateTimeText(): (LocalDateTime) -> String {
    val dateLabel = rememberCmpDateLabeler()
    return { time ->
        "${dateLabel(time.date)}, ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    }
}

/** Оценка облачной конфигурации: «★ 4». Нет оценок — ничего. */
@Composable
internal fun RatingLabel(rating: Int?, modifier: Modifier = Modifier) {
    rating ?: return
    val description = stringResource(Strings.string.configurator_common_a11y_rating, rating)
    CmpRow(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CmpIcon(
            icon = CmpIcons.Star,
            contentDescription = null,
            tint = cmpDomainColors().rating,
            modifier = Modifier.size(20.dp)
        )
        CmpText(text = rating.toString(), style = CmpTextStyle.BodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** Карточка «моей» конфигурации: название, модель, дата изменения, описание (2 строки), меню ⋮. */
@Composable
internal fun MineCard(
    item: LocalConfiguration,
    selected: Boolean,
    dateText: (LocalDateTime) -> String,
    onClick: () -> Unit,
    onMenu: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(ConfiguratorTestTags.mine(item.id)),
        border = if (selected) BorderStroke(2.dp, colors.primary) else null
    ) {
        CmpRow(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)) {
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpText(
                    text = item.name,
                    style = CmpTextStyle.BodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                CmpFlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ModelBadge(item.deviceName)
                    CmpText(text = dateText(item.updated), style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
                }
                if (item.description.isNotBlank()) {
                    CmpText(
                        text = item.description,
                        style = CmpTextStyle.Body,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            onMenu?.let {
                CmpIconButton(
                    onClick = it,
                    icon = CmpIcons.MoreVert,
                    contentDescription = stringResource(Strings.string.configurator_common_a11y_more),
                    modifier = Modifier.testTag(ConfiguratorTestTags.mineMenu(item.id))
                )
            }
        }
    }
}

/** Карточка облачной конфигурации в списке: название, описание, автомобиль, модель, автор и дата, оценка. */
@Composable
internal fun CloudItemCard(
    item: CloudConfigurationSummary,
    selected: Boolean,
    dateText: (LocalDateTime) -> String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(ConfiguratorTestTags.cloud(item.id)),
        border = if (selected) BorderStroke(2.dp, colors.primary) else null
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CmpRow(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpText(
                    text = item.name,
                    style = CmpTextStyle.BodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                RatingLabel(item.rating)
            }
            if (item.shortDescription.isNotBlank()) {
                CmpText(
                    text = item.shortDescription,
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            CloudMeta(item, dateText)
        }
    }
}

/** Автомобиль, модель блока, «Видна только вам», автор · дата. */
@Composable
internal fun CloudMeta(item: CloudConfigurationSummary, dateText: (LocalDateTime) -> String) {
    val colors = cmpThemeColors()
    CmpText(text = item.car.title, style = CmpTextStyle.Body, maxLines = 1, overflow = TextOverflow.Ellipsis)
    CmpFlowRow(
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModelBadge(item.deviceName)
        if (item.onlyForAuthor) {
            CmpText(
                text = stringResource(Strings.string.configurator_cloud_label_only_author),
                style = CmpTextStyle.Body,
                color = colors.onTertiaryContainer
            )
        }
        CmpText(
            text = stringResource(Strings.string.configurator_cloud_format_meta, item.author, dateText(item.updated)),
            style = CmpTextStyle.Body,
            color = colors.onSurfaceVariant
        )
    }
}
