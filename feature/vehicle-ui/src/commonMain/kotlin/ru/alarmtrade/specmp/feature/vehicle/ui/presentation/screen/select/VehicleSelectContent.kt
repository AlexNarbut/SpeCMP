@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.FadeContent
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card.CardPane
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card.PhotoViewer
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card.VehicleDialogs
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.CatalogList
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.CrumbsBar
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.FilterPanel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.SearchArea
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.TopBar
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.TypeChoice
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.WideCatalog
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.brandItems
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.canItems
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.modelItems
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents.trimItems

/** Экран выбора автомобиля без ViewModel: каркас, тело по раскладке, фильтр, диалоги, просмотр фото. */
@Composable
internal fun VehicleSelectContent(
    state: VehicleSelectState,
    layout: VehicleLayout,
    windowWidth: Dp,
    actions: VehicleSelectActions,
    snackbarHostState: CmpSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    CmpScaffold(
        modifier = modifier,
        topBar = { TopBar(state = state, wide = layout.isWide, actions = actions) },
        snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.showTypeChoice -> TypeChoice(wide = layout.isWide, onPick = actions.onTypePick)
                layout == VehicleLayout.COMPACT -> CompactBody(state = state, actions = actions)
                else -> WideCatalog(state = state, layout = layout, windowWidth = windowWidth, actions = actions)
            }
        }
    }
    state.filterDraft?.let { FilterPanel(draft = it, wide = layout.isWide, actions = actions) }
    state.dialog?.let { VehicleDialogs(dialog = it, wide = layout.isWide, actions = actions.card) }
    val point = state.cardUi.point?.let { index -> state.card.content?.mountPoints?.getOrNull(index) }
    if (state.cardUi.viewerOpen && point != null) {
        PhotoViewer(
            point = point,
            photo = state.cardUi.photo,
            onPhotoChange = actions.card.onPhotoChange,
            onClose = actions.card.onViewerClose,
        )
    }
}

/** Ключ шага узкой раскладки: смена ключа — плавный переход, у каждого шага своя прокрутка. */
private fun VehicleSelectState.compactKey(): Any = when (compactStep()) {
    CompactStep.BRANDS -> "brands"
    CompactStep.MODELS -> "models-${selection.brand?.id}"
    CompactStep.TRIMS -> "trims-${selection.model?.id}"
    CompactStep.CAN -> "can"
    CompactStep.CARD -> "card-${selection.carId}"
}

/** Узкое окно: шаги «марки → модели → комплектации → карточка» (или поиск по CAN → карточка). */
@Composable
private fun CompactBody(state: VehicleSelectState, actions: VehicleSelectActions) {
    val step = state.compactStep()
    // Прокрутка марок переживает уход на модели и возврат.
    val brandsList = rememberLazyListState()
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        if (step != CompactStep.CARD) {
            SearchArea(
                state = state,
                wide = false,
                actions = actions,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            )
        }
        CrumbsBar(state = state, actions = actions, typeChip = step != CompactStep.CARD)
        FadeContent(targetState = state, contentKey = { it.compactKey() }, modifier = Modifier.weight(1f)) { shown ->
            when (val shownStep = shown.compactStep()) {
                CompactStep.CARD -> CardPane(
                    card = shown.card,
                    ui = shown.cardUi,
                    groups = shown.groups,
                    wide = false,
                    pick = shown.mode.pick,
                    actions = actions.card,
                    onRetry = actions.onRetry,
                    onPick = actions.onPick,
                )

                else -> CatalogList(
                    listState = if (shownStep ==
                        CompactStep.BRANDS
                    ) {
                        brandsList
                    } else {
                        rememberLazyListState()
                    },
                ) {
                    when (shownStep) {
                        CompactStep.BRANDS -> brandItems(shown, actions, showRecent = true, compact = false)
                        CompactStep.MODELS -> modelItems(shown, actions, accordion = false, compact = false)
                        CompactStep.TRIMS -> trimItems(shown, actions, inline = false)
                        CompactStep.CAN -> canItems(shown, actions, compact = false)
                        CompactStep.CARD -> Unit
                    }
                }
            }
        }
    }
}

/** Ограничение ширины содержимого на широком окне (карточка на Medium). */
internal val CardMaxWidth = 840.dp

@Composable
internal fun CenteredCard(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = CardMaxWidth).fillMaxWidth()) { content() }
    }
}
