package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.LegacyBaseViewModel
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/**
 * K4 список облачных конфигураций автомобиля [carId]: приходит целиком; сортировка «Лучшие / Новые», фильтр по модели
 * блока — локально. [deviceName] — модель блока, под которую запрашивать (пусто — все модели).
 */
internal class CloudListViewModel(
    private val carId: Int,
    private val deviceName: String,
    private val cloud: CloudConfigurationsRepository,
    private val dispatchers: AppDispatchers,
) : LegacyBaseViewModel() {

    private val _state = MutableStateFlow(CloudListState(modelFilter = deviceName.ifEmpty { null }))

    private val retryRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Загрузка — при первой подписке на state и по «Повторить»;
    // collectLatest отменяет незаконченную загрузку при повторе.
    val state: StateFlow<CloudListState> = _state
        .map { it.withLists() }
        .launchOnFirstStart { retryRequests.onStart { emit(Unit) }.collectLatest { load() } }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), _state.value)

    private suspend fun load() {
        _state.update { it.copy(list = LCEState.Loading) }
        val response = withContext(dispatchers.io) { cloud.byCar(carId, deviceName) }
        _state.update { it.copy(list = response.asLCEState(ConfiguratorError::from)) }
    }

    fun onRetry() {
        retryRequests.tryEmit(Unit)
    }

    fun onOrder(order: CloudConfigurationOrder) = _state.update { it.copy(order = order) }

    fun onModelFilter(deviceName: String?) = _state.update { it.copy(modelFilter = deviceName) }

    fun onSelect(id: Int) = _state.update { it.copy(selectedId = id) }

    private companion object {
        val STOP_TIMEOUT = 5.seconds
    }
}

internal fun CloudConfiguration.toSource(): SettingsSource.RemoteConfiguration =
    SettingsSource.RemoteConfiguration(id = summary.id, deviceName = summary.deviceName, title = summary.name)
