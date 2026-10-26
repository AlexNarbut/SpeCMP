package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
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
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.VehicleRow
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.groupLabel
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_doc_a11y_open
import specmp.feature.vehicle_ui.generated.resources.vehicle_features_label_control
import specmp.feature.vehicle_ui.generated.resources.vehicle_features_label_none
import specmp.feature.vehicle_ui.generated.resources.vehicle_features_label_read
import specmp.feature.vehicle_ui.generated.resources.vehicle_features_msg_filtered
import specmp.feature.vehicle_ui.generated.resources.vehicle_features_msg_filtered_none
import specmp.feature.vehicle_ui.generated.resources.vehicle_features_msg_legend
import specmp.feature.vehicle_ui.generated.resources.vehicle_features_msg_warning
import specmp.feature.vehicle_ui.generated.resources.vehicle_points_label_no_photos
import specmp.feature.vehicle_ui.generated.resources.vehicle_points_label_photos

/** Доля столбца названия функции на широком окне (столбцы шин — по 1). */
private const val FEATURE_NAME_WEIGHT = 1.2f

/**
 * Вкладка «Функции» (матрица B): группы со счётчиком и сворачиванием; у функции — «Чтение» и «Управление»
 * с чипами шин (узкое окно — строками под названием, широкое — два столбца); внизу — предупреждение.
 */
@Composable
internal fun FeaturesTab(
    features: List<CarFeature>,
    total: Int,
    filtered: Boolean,
    collapsed: Set<CarFeatureGroup>,
    wide: Boolean,
    onGroupToggle: (CarFeatureGroup) -> Unit
) {
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CmpText(
            text = stringResource(Res.string.vehicle_features_msg_legend),
            style = CmpTextStyle.Body,
            color = colors.onSurfaceVariant
        )
        if (filtered) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                CmpIcon(
                    icon = CmpIcons.FilterAlt,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                CmpText(
                    text = stringResource(Res.string.vehicle_features_msg_filtered, features.size, total),
                    style = CmpTextStyle.Body,
                    color = colors.primary
                )
            }
        }
        if (features.isEmpty()) {
            CmpStateBox(
                icon = CmpIcons.FilterAltOff,
                title = stringResource(Res.string.vehicle_features_msg_filtered_none),
                compact = true
            )
        }
        CarFeatureGroup.entries.forEach { group ->
            val inGroup = features.filter { it.group == group }
            if (inGroup.isNotEmpty()) {
                val open = group !in collapsed
                GroupHeader(group = group, count = inGroup.size, open = open, onToggle = { onGroupToggle(group) })
                if (open) inGroup.forEach { FeatureRow(it, wide) }
            }
        }
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(colors.surfaceContainerHigh, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.onSurfaceVariant)
            CmpText(text = stringResource(Res.string.vehicle_features_msg_warning), style = CmpTextStyle.Body)
        }
    }
}

@Composable
private fun GroupHeader(group: CarFeatureGroup, count: Int, open: Boolean, onToggle: () -> Unit) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh)
            .clickable(onClick = onToggle)
            .heightIn(min = 52.dp)
            .padding(start = 12.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CmpText(
            text = groupLabel(group),
            style = CmpTextStyle.BodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        CmpText(text = count.toString(), style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
        CmpIcon(
            icon = if (open) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureRow(feature: CarFeature, wide: Boolean) {
    val colors = cmpThemeColors()
    val name: @Composable () -> Unit = {
        CmpColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(text = feature.name, style = CmpTextStyle.BodyLarge, color = colors.onSurface)
            feature.description?.let { CmpText(text = it, style = CmpTextStyle.Body, color = colors.onSurfaceVariant) }
        }
    }
    val read = stringResource(Res.string.vehicle_features_label_read)
    val control = stringResource(Res.string.vehicle_features_label_control)
    if (wide) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CmpBox(modifier = Modifier.weight(FEATURE_NAME_WEIGHT)) { name() }
            BusList(label = null, buses = feature.reads, modifier = Modifier.weight(1f))
            BusList(label = null, buses = feature.controls, modifier = Modifier.weight(1f))
        }
    } else {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            name()
            BusList(label = read, buses = feature.reads)
            BusList(label = control, buses = feature.controls)
        }
    }
}

/** Подпись («Чтение»/«Управление», на широком окне — в заголовке столбца) и чипы шин; пусто — «—». */
@Composable
private fun BusList(label: String?, buses: List<CarBus>, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpFlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        label?.let {
            CmpText(
                text = it,
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
                modifier = Modifier.width(110.dp)
            )
        }
        if (buses.isEmpty()) {
            CmpText(
                text = stringResource(Res.string.vehicle_features_label_none),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant
            )
        } else {
            buses.forEach { BusChip(it) }
        }
    }
}

/** Заголовки столбцов матрицы на широком окне. */
@Composable
internal fun FeaturesColumnsHeader() {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpBox(modifier = Modifier.weight(FEATURE_NAME_WEIGHT))
        CmpText(
            text = stringResource(Res.string.vehicle_features_label_read),
            style = CmpTextStyle.Body,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        CmpText(
            text = stringResource(Res.string.vehicle_features_label_control),
            style = CmpTextStyle.Body,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Плитка-иконка строки вкладки (точки подключения, PDF, народные карты). */
@Composable
internal fun TileIcon(icon: CmpIcons, background: Color, tint: Color) {
    CmpBox(
        modifier = Modifier.size(48.dp).background(background, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = tint)
    }
}

/** Список точек подключения: тип, фото. */
@Composable
internal fun MountPointsList(points: List<MountPoint>, onOpen: (Int) -> Unit) {
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        points.forEachIndexed { index, point ->
            VehicleRow(
                title = AnnotatedString(point.title),
                subtitle = if (point.photos.isEmpty()) {
                    stringResource(Res.string.vehicle_points_label_no_photos, point.type)
                } else {
                    stringResource(Res.string.vehicle_points_label_photos, point.type, point.photos.size)
                },
                onClick = { onOpen(index) },
                lead = { TileIcon(CmpIcons.Cable, colors.secondaryContainer, colors.onSecondaryContainer) },
                modifier = Modifier.testTag(VehicleTestTags.point(index))
            )
        }
    }
}

/** Документы PDF (схемы, карты монтажа): открываются системным просмотрщиком. */
@Composable
internal fun DocumentsList(documents: List<CarDocument>, onOpen: (String) -> Unit) {
    val colors = cmpThemeColors()
    val openLabel = stringResource(Res.string.vehicle_doc_a11y_open)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        documents.forEach { document ->
            VehicleRow(
                title = AnnotatedString(document.name),
                subtitle = document.description,
                onClick = { onOpen(document.url) },
                chevron = false,
                lead = { TileIcon(CmpIcons.PictureAsPdf, colors.errorContainer, colors.onErrorContainer) },
                trailing = { CmpIcon(icon = CmpIcons.OpenInNew, contentDescription = openLabel, tint = colors.primary) }
            )
        }
    }
}

/** Народные карты; без экрана инструкций ([onOpen] `null`) строки без перехода. */
@Composable
internal fun UserManualsList(manuals: List<UserManualRef>, onOpen: ((Int) -> Unit)?) {
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        manuals.forEach { manual ->
            VehicleRow(
                title = AnnotatedString(manual.title),
                subtitle = manual.description,
                onClick = { onOpen?.invoke(manual.id) },
                chevron = onOpen != null,
                lead = { TileIcon(CmpIcons.Groups, colors.secondaryContainer, colors.onSecondaryContainer) }
            )
        }
    }
}
