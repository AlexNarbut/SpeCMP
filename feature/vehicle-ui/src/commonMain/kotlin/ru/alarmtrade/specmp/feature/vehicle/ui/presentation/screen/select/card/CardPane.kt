package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_card_btn_pick
import ru.alarmtrade.specmp.core.localization.resources.vehicle_card_msg_no_tabs
import ru.alarmtrade.specmp.core.localization.resources.vehicle_tab_label_features
import ru.alarmtrade.specmp.core.localization.resources.vehicle_tab_label_manuals
import ru.alarmtrade.specmp.core.localization.resources.vehicle_tab_label_maps
import ru.alarmtrade.specmp.core.localization.resources.vehicle_tab_label_points
import ru.alarmtrade.specmp.core.localization.resources.vehicle_tab_label_schemes
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
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
import ru.alarmtrade.specmp.feature.vehicle.domain.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.FadeContent
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.LoadFailed
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.PHOTO_RATIO
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardTab
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardUi
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CatalogLevel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.tabs

/** [wide] — раскладка шапки и матрицы по ширине самой панели; [pick] — закреплённая снизу кнопка «Выбрать». */
@Composable
internal fun CardPane(
    card: LCEState<CarCard, CatalogError>,
    ui: CardUi,
    groups: Set<CarFeatureGroup>,
    wide: Boolean,
    pick: Boolean,
    actions: CardActions,
    onRetry: (CatalogLevel) -> Unit,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    padding: Dp = CmpSpacing.screenPaddingH,
) {
    FadeContent(targetState = card, contentKey = { it::class }, modifier = modifier) { shown ->
        CardPaneState(shown, ui, groups, wide, pick, actions, onRetry, onPick, padding)
    }
}

@Suppress("LongParameterList") // проброс параметров CardPane в одну фазу загрузки
@Composable
private fun CardPaneState(
    card: LCEState<CarCard, CatalogError>,
    ui: CardUi,
    groups: Set<CarFeatureGroup>,
    wide: Boolean,
    pick: Boolean,
    actions: CardActions,
    onRetry: (CatalogLevel) -> Unit,
    onPick: (Int) -> Unit,
    padding: Dp,
    modifier: Modifier = Modifier,
) {
    when (card) {
        LCEState.None, LCEState.Loading -> CardSkeleton(wide = wide, modifier = modifier.padding(padding))

        is LCEState.Error -> LoadFailed(error = card.error, onRetry = {
            onRetry(CatalogLevel.CARD)
        }, modifier = modifier)

        is LCEState.Content -> CmpColumn(modifier = modifier.fillMaxSize().testTag(VehicleTestTags.CARD)) {
            CardContent(
                card = card.content,
                ui = ui,
                groups = groups,
                wide = wide,
                actions = actions,
                padding = padding,
                modifier = Modifier.weight(1f),
            )
            if (pick) PickBar(card = card.content, padding = padding, onPick = onPick)
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
    modifier: Modifier = Modifier,
) {
    val tabs = card.tabs()
    val tab = ui.tab?.takeIf { it in tabs } ?: tabs.firstOrNull()
    val colors = CmpTheme.colors
    // Фильтр и раскладка функций по группам — один раз на карточку и фильтр, не на каждую рекомпозицию.
    val shownFeatures = remember(card.features, groups) {
        if (groups.isEmpty()) card.features else card.features.filter { it.group in groups }
    }
    val featureGroups = remember(shownFeatures) { featuresByGroup(shownFeatures) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = padding + CmpSpacing.space8),
    ) {
        item(key = "header") {
            CardHeader(
                card = card,
                wide = wide,
                notesExpanded = ui.notesExpanded,
                actions = actions,
                modifier = Modifier.padding(start = padding, end = padding, top = padding),
            )
        }
        if (tab == null) {
            item(key = "no-tabs") { NoTabs(Modifier.padding(padding)) }
            return@LazyColumn
        }
        stickyHeader(key = "tabs") {
            // Непрозрачная шапка на всю ширину, отступ сверху — внутри фона: прокручиваемое содержимое вкладки
            // уходит под вкладки и не просвечивает ни над ними, ни справа от них.
            CmpSurface(color = colors.backgroundPagePrimary, modifier = Modifier.fillMaxWidth()) {
                CmpTabRow(
                    selectedTabIndex = tabs.indexOf(tab),
                    tabs = tabs.map { CmpTabItem(tabTitle(it)) },
                    onTabSelect = { actions.onTabSelect(tabs[it]) },
                    modifier = Modifier.fillMaxWidth().padding(top = CmpSpacing.space12),
                    scrollable = true,
                )
            }
        }
        if (tab == CardTab.FEATURES) {
            featureItems(
                groups = featureGroups,
                shown = shownFeatures.size,
                total = card.features.size,
                filtered = groups.isNotEmpty(),
                collapsed = ui.collapsedGroups,
                wide = wide,
                padding = padding,
                onGroupToggle = actions.onGroupToggle,
            )
        } else {
            item(key = "tab-${tab.name}") {
                CmpColumn(modifier = Modifier.padding(start = padding, end = padding, top = CmpSpacing.space16)) {
                    TabBody(card = card, tab = tab, ui = ui, actions = actions)
                }
            }
        }
    }
}

/** «Функции» — не здесь: они элементами списка карточки ([featureItems]). */
@Composable
private fun TabBody(card: CarCard, tab: CardTab, ui: CardUi, actions: CardActions) {
    when (tab) {
        CardTab.FEATURES -> Unit

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
                    onFullScreen = actions.onViewerOpen,
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
        CardTab.FEATURES -> Strings.string.vehicle_tab_label_features
        CardTab.MOUNT_POINTS -> Strings.string.vehicle_tab_label_points
        CardTab.SCHEMES -> Strings.string.vehicle_tab_label_schemes
        CardTab.MAPS -> Strings.string.vehicle_tab_label_maps
        CardTab.USER_MANUALS -> Strings.string.vehicle_tab_label_manuals
    },
)

@Composable
private fun NoTabs(modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.Info, contentDescription = null, tint = colors.foregroundOnDarkSecondaryDefault)
        CmpText(text = stringResource(Strings.string.vehicle_card_msg_no_tabs), style = CmpTextStyle.Paragraph2)
    }
}

@Composable
private fun PickBar(card: CarCard, padding: Dp, onPick: (Int) -> Unit) {
    CmpSurface(color = CmpTheme.colors.backgroundPagePrimary, modifier = Modifier.fillMaxWidth()) {
        CmpColumn {
            CmpDivider()
            CmpButton(
                text = stringResource(Strings.string.vehicle_card_btn_pick, card.summary.fullName()),
                onClick = { onPick(card.summary.id) },
                icon = CmpIcons.Check,
                fullWidth = true,
                modifier = Modifier
                    .padding(horizontal = padding, vertical = CmpSpacing.space12)
                    .testTag(VehicleTestTags.CARD_PICK),
            )
        }
    }
}

@Composable
internal fun CardSkeleton(wide: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpSkeleton(
                Modifier.width(if (wide) 200.dp else 120.dp).aspectRatio(PHOTO_RATIO),
                shape = shape,
            )
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpSkeleton(Modifier.width(180.dp).height(26.dp))
                CmpSkeleton(Modifier.width(120.dp).height(18.dp))
            }
        }
        CmpSkeleton(Modifier.fillMaxWidth().height(64.dp), shape = shape)
        CmpSkeleton(Modifier.fillMaxWidth().height(64.dp), shape = shape)
        CmpSkeleton(Modifier.fillMaxWidth().height(64.dp), shape = shape)
    }
}
