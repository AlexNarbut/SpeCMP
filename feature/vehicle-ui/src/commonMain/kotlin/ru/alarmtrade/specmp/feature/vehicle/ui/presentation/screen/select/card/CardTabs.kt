package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_doc_a11y_open
import ru.alarmtrade.specmp.core.localization.resources.vehicle_features_label_control
import ru.alarmtrade.specmp.core.localization.resources.vehicle_features_label_none
import ru.alarmtrade.specmp.core.localization.resources.vehicle_features_label_read
import ru.alarmtrade.specmp.core.localization.resources.vehicle_features_msg_filtered
import ru.alarmtrade.specmp.core.localization.resources.vehicle_features_msg_filtered_none
import ru.alarmtrade.specmp.core.localization.resources.vehicle_features_msg_legend
import ru.alarmtrade.specmp.core.localization.resources.vehicle_features_msg_warning
import ru.alarmtrade.specmp.core.localization.resources.vehicle_points_label_no_photos
import ru.alarmtrade.specmp.core.localization.resources.vehicle_points_label_photos
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBus
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarDocument
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeature
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.MountPoint
import ru.alarmtrade.specmp.feature.vehicle.domain.model.UserManualRef
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.BusChip
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.groupLabel

private const val FEATURE_NAME_WEIGHT = 1.2f

internal fun featuresByGroup(features: List<CarFeature>): List<Pair<CarFeatureGroup, List<CarFeature>>> {
    val byGroup = features.groupBy { it.group }
    return CarFeatureGroup.entries.mapNotNull { group -> byGroup[group]?.let { group to it } }
}

/** Каждая функция — отдельный элемент списка: при открытии карточки композируются только видимые. */
@Suppress("LongParameterList") // разметка вкладки: всё приходит из CardContent
internal fun LazyListScope.featureItems(
    groups: List<Pair<CarFeatureGroup, List<CarFeature>>>,
    shown: Int,
    total: Int,
    filtered: Boolean,
    collapsed: Set<CarFeatureGroup>,
    wide: Boolean,
    padding: Dp,
    onGroupToggle: (CarFeatureGroup) -> Unit,
) {
    val itemPadding = Modifier.padding(start = padding, end = padding, top = CmpSpacing.space8)
    item(key = "features-intro") {
        CmpColumn(modifier = Modifier.padding(start = padding, end = padding, top = CmpSpacing.space16)) {
            if (wide) FeaturesColumnsHeader()
            FeaturesIntro(shown = shown, total = total, filtered = filtered)
        }
    }
    groups.forEach { (group, inGroup) ->
        val open = group !in collapsed
        item(key = "features-group-${group.name}") {
            GroupHeader(
                group = group,
                count = inGroup.size,
                open = open,
                onToggle = { onGroupToggle(group) },
                modifier = itemPadding,
            )
        }
        if (open) {
            items(count = inGroup.size, key = { index -> "feature-${group.name}-$index" }) { index ->
                FeatureRow(inGroup[index], wide, modifier = itemPadding)
            }
        }
    }
    item(key = "features-warning") { FeaturesWarning(modifier = itemPadding) }
}

@Composable
private fun FeaturesIntro(shown: Int, total: Int, filtered: Boolean) {
    val colors = CmpTheme.colors
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpText(
            text = stringResource(Strings.string.vehicle_features_msg_legend),
            style = CmpTextStyle.Paragraph2,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
        if (filtered) {
            CmpTag(
                text = stringResource(Strings.string.vehicle_features_msg_filtered, shown, total),
                tone = CmpTagTone.Brand,
                icon = CmpIcons.FilterAlt,
            )
        }
        if (shown == 0) {
            CmpStateBox(
                icon = CmpIcons.FilterAltOff,
                title = stringResource(Strings.string.vehicle_features_msg_filtered_none),
                compact = true,
            )
        }
    }
}

@Composable
private fun FeaturesWarning(modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = CmpSpacing.space8)
            .background(colors.backgroundSurfaceHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.warningDefault)
        CmpText(text = stringResource(Strings.string.vehicle_features_msg_warning), style = CmpTextStyle.Paragraph2)
    }
}

@Composable
private fun GroupHeader(
    group: CarFeatureGroup,
    count: Int,
    open: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = CmpSpacing.space8)
            .heightIn(min = CmpSize.buttonM)
            .cmpPressable(onClick = onToggle, shape = shape)
            .background(colors.backgroundSurfaceHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(start = CmpSpacing.space16, end = CmpSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = groupLabel(group),
            style = CmpTextStyle.Headline2,
            autoSize = true,
            modifier = Modifier.weight(1f),
        )
        CmpText(
            text = count.toString(),
            style = CmpTextStyle.Additional0,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
        CmpIcon(
            icon = if (open) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.size(CmpSize.iconL),
        )
    }
}

@Composable
private fun FeatureRow(feature: CarFeature, wide: Boolean, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val name: @Composable () -> Unit = {
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = feature.name, style = CmpTextStyle.Paragraph1, color = colors.foregroundOnDarkPrimaryDefault)
            feature.description?.let {
                CmpText(text = it, style = CmpTextStyle.Paragraph3, color = colors.foregroundOnDarkSecondaryDefault)
            }
        }
    }
    if (wide) {
        CmpRow(
            modifier = modifier.fillMaxWidth().featureCell(),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpBox(modifier = Modifier.weight(FEATURE_NAME_WEIGHT)) { name() }
            BusList(label = null, buses = feature.reads, modifier = Modifier.weight(1f))
            BusList(label = null, buses = feature.controls, modifier = Modifier.weight(1f))
        }
    } else {
        CmpColumn(
            modifier = modifier.fillMaxWidth().featureCell(),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            name()
            // Подписи — только на узком окне: на широком они в заголовке столбцов.
            BusList(label = stringResource(Strings.string.vehicle_features_label_read), buses = feature.reads)
            BusList(label = stringResource(Strings.string.vehicle_features_label_control), buses = feature.controls)
        }
    }
}

@Composable
private fun BusList(label: String?, buses: List<CarBus>, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpFlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        label?.let {
            CmpText(
                text = it,
                style = CmpTextStyle.Additional0,
                color = colors.foregroundOnDarkSecondaryDefault,
                autoSize = true,
                modifier = Modifier.width(BusLabelWidth),
            )
        }
        if (buses.isEmpty()) {
            CmpText(
                text = stringResource(Strings.string.vehicle_features_label_none),
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkTertiary,
            )
        } else {
            buses.forEach { BusChip(it) }
        }
    }
}

@Composable
internal fun FeaturesColumnsHeader() {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpBox(modifier = Modifier.weight(FEATURE_NAME_WEIGHT))
        CmpText(
            text = stringResource(Strings.string.vehicle_features_label_read),
            style = CmpTextStyle.Additional0,
            color = colors.foregroundOnDarkSecondaryDefault,
            uppercase = true,
            autoSize = true,
            modifier = Modifier.weight(1f),
        )
        CmpText(
            text = stringResource(Strings.string.vehicle_features_label_control),
            style = CmpTextStyle.Additional0,
            color = colors.foregroundOnDarkSecondaryDefault,
            uppercase = true,
            autoSize = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun TileIcon(icon: CmpIcons, background: Color, tint: Color) {
    CmpBox(
        modifier = Modifier.size(CmpSize.touchMin).background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(CmpSize.iconM))
    }
}

@Composable
internal fun MountPointsList(points: List<MountPoint>, onOpen: (Int) -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        points.forEachIndexed { index, point ->
            CmpListItem(
                title = point.title,
                subtitle = if (point.photos.isEmpty()) {
                    stringResource(Strings.string.vehicle_points_label_no_photos, point.type)
                } else {
                    stringResource(Strings.string.vehicle_points_label_photos, point.type, point.photos.size)
                },
                icon = CmpIcons.Cable,
                onClick = { onOpen(index) },
                modifier = Modifier.fillMaxWidth().testTag(VehicleTestTags.point(index)),
            )
        }
    }
}

@Composable
internal fun DocumentsList(documents: List<CarDocument>, onOpen: (String) -> Unit) {
    val openLabel = stringResource(Strings.string.vehicle_doc_a11y_open)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        documents.forEach { document ->
            CmpListItem(
                title = document.name,
                subtitle = document.description,
                icon = CmpIcons.PictureAsPdf,
                onClick = { onOpen(document.url) },
                trailing = {
                    CmpIcon(
                        icon = CmpIcons.OpenInNew,
                        contentDescription = openLabel,
                        tint = CmpTheme.colors.brandPrimaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun UserManualsList(manuals: List<UserManualRef>, onOpen: ((Int) -> Unit)?) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        manuals.forEach { manual ->
            CmpListItem(
                title = manual.title,
                subtitle = manual.description,
                icon = CmpIcons.Groups,
                onClick = onOpen?.let { open -> { open(manual.id) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val BusLabelWidth = 110.dp

private fun Modifier.featureCell(): Modifier = padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space12)
