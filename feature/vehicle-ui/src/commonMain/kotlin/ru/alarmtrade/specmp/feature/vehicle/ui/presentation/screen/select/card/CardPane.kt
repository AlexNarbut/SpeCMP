package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTabItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTabRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.FadeContent
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.LoadFailed
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.PHOTO_RATIO
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardTab
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardUi
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CatalogLevel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.Load
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.tabs
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_card_btn_pick
import specmp.feature.vehicle_ui.generated.resources.vehicle_card_msg_no_tabs
import specmp.feature.vehicle_ui.generated.resources.vehicle_tab_label_features
import specmp.feature.vehicle_ui.generated.resources.vehicle_tab_label_manuals
import specmp.feature.vehicle_ui.generated.resources.vehicle_tab_label_maps
import specmp.feature.vehicle_ui.generated.resources.vehicle_tab_label_points
import specmp.feature.vehicle_ui.generated.resources.vehicle_tab_label_schemes

/**
 * Карточка автомобиля с загрузкой: скелетон, ошибка с «Повторить», готовая карточка. [wide] — раскладка
 * шапки и матрицы функций для ширины самой панели; [pick] — закреплённая снизу кнопка «Выбрать».
 */
@Composable
internal fun CardPane(
    card: Load<CarCard>,
    ui: CardUi,
    groups: Set<CarFeatureGroup>,
    wide: Boolean,
    pick: Boolean,
    actions: CardActions,
    onRetry: (CatalogLevel) -> Unit,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp
) {
    // Скелетон → карточка / ошибка — затуханием, а не резкой подменой.
    FadeContent(targetState = card, contentKey = { it::class }, modifier = modifier) { shown ->
        CardPaneState(shown, ui, groups, wide, pick, actions, onRetry, onPick, padding)
    }
}

@Suppress("LongParameterList") // проброс параметров CardPane в одну фазу загрузки
@Composable
private fun CardPaneState(
    card: Load<CarCard>,
    ui: CardUi,
    groups: Set<CarFeatureGroup>,
    wide: Boolean,
    pick: Boolean,
    actions: CardActions,
    onRetry: (CatalogLevel) -> Unit,
    onPick: (Int) -> Unit,
    padding: Dp,
    modifier: Modifier = Modifier
) {
    when (card) {
        Load.Idle, Load.Loading -> CardSkeleton(wide = wide, modifier = modifier.padding(padding))

        is Load.Failed -> LoadFailed(offline = card.offline, onRetry = {
            onRetry(CatalogLevel.CARD)
        }, modifier = modifier)

        is Load.Ready -> CmpColumn(modifier = modifier.fillMaxSize().testTag(VehicleTestTags.CARD)) {
            CardContent(
                card = card.value,
                ui = ui,
                groups = groups,
                wide = wide,
                actions = actions,
                padding = padding,
                modifier = Modifier.weight(1f)
            )
            if (pick) PickBar(card = card.value, padding = padding, onPick = onPick)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardContent(
    card: CarCard,
    ui: CardUi,
    groups: Set<CarFeatureGroup>,
    wide: Boolean,
    actions: CardActions,
    padding: Dp,
    modifier: Modifier = Modifier
) {
    val tabs = card.tabs()
    val tab = ui.tab?.takeIf { it in tabs } ?: tabs.firstOrNull()
    val colors = cmpThemeColors()
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = padding + 8.dp)) {
        item(key = "header") {
            CardHeader(
                card = card,
                wide = wide,
                notesExpanded = ui.notesExpanded,
                actions = actions,
                modifier = Modifier.padding(start = padding, end = padding, top = padding)
            )
        }
        if (tab == null) {
            item(key = "no-tabs") { NoTabs(Modifier.padding(padding)) }
            return@LazyColumn
        }
        stickyHeader(key = "tabs") {
            CmpSurface(color = colors.background, modifier = Modifier.padding(top = 12.dp)) {
                CmpTabRow(
                    selectedTabIndex = tabs.indexOf(tab),
                    tabs = tabs.map { CmpTabItem(tabTitle(it)) },
                    onTabSelect = { actions.onTabSelect(tabs[it]) },
                    scrollable = true
                )
            }
        }
        item(key = "tab-${tab.name}") {
            CmpColumn(modifier = Modifier.padding(start = padding, end = padding, top = 14.dp)) {
                TabBody(card = card, tab = tab, ui = ui, groups = groups, wide = wide, actions = actions)
            }
        }
    }
}

@Composable
private fun TabBody(
    card: CarCard,
    tab: CardTab,
    ui: CardUi,
    groups: Set<CarFeatureGroup>,
    wide: Boolean,
    actions: CardActions
) {
    when (tab) {
        CardTab.FEATURES -> {
            val shown = if (groups.isEmpty()) card.features else card.features.filter { it.group in groups }
            if (wide) FeaturesColumnsHeader()
            FeaturesTab(
                features = shown,
                total = card.features.size,
                filtered = groups.isNotEmpty(),
                collapsed = ui.collapsedGroups,
                wide = wide,
                onGroupToggle = actions.onGroupToggle
            )
        }

        CardTab.MOUNT_POINTS -> {
            val point = ui.point?.let { card.mountPoints.getOrNull(it) }
            if (point == null) {
                MountPointsList(points = card.mountPoints, onOpen = actions.onPointOpen)
            } else {
                MountPointDetail(
                    point = point,
                    photo = ui.photo,
                    onBack = actions.onPointClose,
                    onPhotoChange = actions.onPhotoChange,
                    onFullScreen = actions.onViewerOpen
                )
            }
        }

        CardTab.SCHEMES -> DocumentsList(documents = card.schemes, onOpen = actions.onOpenDocument)

        CardTab.MAPS -> DocumentsList(documents = card.mountingMaps, onOpen = actions.onOpenDocument)

        CardTab.USER_MANUALS -> UserManualsList(manuals = card.userManuals, onOpen = actions.onOpenUserManual)
    }
}

@Composable
private fun tabTitle(tab: CardTab): String = stringResource(
    when (tab) {
        CardTab.FEATURES -> Res.string.vehicle_tab_label_features
        CardTab.MOUNT_POINTS -> Res.string.vehicle_tab_label_points
        CardTab.SCHEMES -> Res.string.vehicle_tab_label_schemes
        CardTab.MAPS -> Res.string.vehicle_tab_label_maps
        CardTab.USER_MANUALS -> Res.string.vehicle_tab_label_manuals
    }
)

@Composable
private fun NoTabs(modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpIcon(icon = CmpIcons.Info, contentDescription = null, tint = colors.onSurfaceVariant)
        CmpText(text = stringResource(Res.string.vehicle_card_msg_no_tabs), style = CmpTextStyle.BodyLarge)
    }
}

/** «Выбрать · Toyota Camry 2.5 AT» — закреплена под карточкой. */
@Composable
private fun PickBar(card: CarCard, padding: Dp, onPick: (Int) -> Unit) {
    val colors = cmpThemeColors()
    CmpSurface(color = colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        CmpButton(
            onClick = { onPick(card.summary.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding, vertical = 12.dp)
                .heightIn(min = 56.dp)
                .testTag(VehicleTestTags.CARD_PICK)
        ) {
            CmpIcon(icon = CmpIcons.Check, contentDescription = null)
            CmpText(
                text = stringResource(Res.string.vehicle_card_btn_pick, card.summary.fullName()),
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1
            )
        }
    }
}

/** Скелетон карточки: фото, две строки, плитка CAN, два бейджа. */
@Composable
internal fun CardSkeleton(wide: Boolean, modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpSkeleton(
                Modifier.width(if (wide) 200.dp else 120.dp).aspectRatio(PHOTO_RATIO),
                shape = RoundedCornerShape(14.dp)
            )
            CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpSkeleton(Modifier.width(180.dp).height(26.dp))
                CmpSkeleton(Modifier.width(120.dp).height(18.dp))
            }
        }
        CmpSkeleton(Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(14.dp))
        CmpSkeleton(Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp))
        CmpSkeleton(Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp))
    }
}
