package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.sortedBy
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.matchesModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.modelOptions

@Immutable
internal data class CloudListState(
    val list: LCEState<List<CloudConfigurationSummary>, ConfiguratorError> = LCEState.Loading,
    val order: CloudConfigurationOrder = CloudConfigurationOrder.BEST,
    val modelFilter: String? = null,
    /** Выбранная (широкое окно — карточка справа). */
    val selectedId: Int? = null,
    val listWidthDp: Int? = null,
    val models: List<String> = emptyList(),
    val shown: List<CloudConfigurationSummary> = emptyList(),
)

internal fun CloudListState.withLists(): CloudListState {
    val all = list.content.orEmpty()
    return copy(
        models = all.modelOptions { it.deviceName },
        shown = all.filter { matchesModel(it.deviceName, modelFilter) }.sortedBy(order),
    )
}
