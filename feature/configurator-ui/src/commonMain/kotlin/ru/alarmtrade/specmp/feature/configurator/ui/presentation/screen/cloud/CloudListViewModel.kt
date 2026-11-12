package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.layout.PaneWidthStore
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.LIST_PANE_KEY
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError

/** Список приходит целиком: сортировка и фильтр по модели — локально; [deviceName] пусто — все модели. */
internal class CloudListViewModel(
    private val carId: Int,
    private val deviceName: String,
    private val cloud: CloudConfigurationsRepository,
    private val paneWidths: PaneWidthStore,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<CloudListState, Nothing>(
    CloudListState(
        modelFilter = deviceName.ifEmpty { null },
        listWidthDp = paneWidths.widthsDp.value.getValueIfSuccess()?.get(LIST_PANE_KEY),
    ),
) {

    fun onListWidthChange(widthDp: Int) {
        intent {
            reduce { copy(listWidthDp = widthDp) }
            withContext(dispatchers.io) { paneWidths.setWidthDp(LIST_PANE_KEY, widthDp) }
                .onError { logger.w(it) { "List width was not saved" } }
        }
    }

    override suspend fun onStart() {
        load()
    }

    // Загрузка живёт с VM, не с подпиской экрана; повтор отменяет незаконченную.
    private fun load() {
        launchReplacing(LOAD_KEY) {
            reduce { copy(list = LCEState.Loading).withLists() }
            val response = withContext(dispatchers.io) { cloud.byCar(carId, deviceName) }
            reduce { copy(list = response.asLCEState(ConfiguratorError::from)).withLists() }
        }
    }

    fun onRetry() {
        load()
    }

    fun onOrder(order: CloudConfigurationOrder) {
        reduce { copy(order = order).withLists() }
    }

    fun onModelFilter(deviceName: String?) {
        reduce { copy(modelFilter = deviceName).withLists() }
    }

    fun onSelect(id: Int) {
        reduce { copy(selectedId = id) }
    }

    private companion object {
        const val LOAD_KEY = "load"
    }
}
