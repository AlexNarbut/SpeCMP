package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.sortedBy
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError

@Immutable
internal data class CloudListState(
    val list: LCEState<List<CloudConfigurationSummary>, ConfiguratorError> = LCEState.Loading,
    val order: CloudConfigurationOrder = CloudConfigurationOrder.BEST,
    val modelFilter: String? = null,
    /** Выбранная (широкое окно — карточка справа). */
    val selectedId: Int? = null
) {
    private val all: List<CloudConfigurationSummary> get() = list.content.orEmpty()

    val models: List<String> get() = all.map { it.deviceName }.distinct().sortedBy { it.lowercase() }

    val shown: List<CloudConfigurationSummary>
        get() = all.filter { modelFilter == null || it.deviceName.equals(modelFilter, ignoreCase = true) }
            .sortedBy(order)
}
