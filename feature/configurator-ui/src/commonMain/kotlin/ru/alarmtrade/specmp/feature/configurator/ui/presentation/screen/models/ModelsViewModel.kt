package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.SourceKey
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.toSource
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * K2 выбор модели блока: «С нуля» (заводские настройки модели) или модель для файла `.dxs` (K3 — модели в файле нет).
 * Сверху — модель подключённого блока и недавние; поиск по названию. Файл проверяется чтением в выбранной модели:
 * не открылся — «повреждён или для другой модели», выбрать другую.
 */
internal class ModelsViewModel(
    private val file: SourceKey.File?,
    private val models: DeviceModelsRepository,
    private val settings: DeviceSettingsRepository,
    deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val _state = MutableStateFlow(ModelsState(fileName = file?.name))

    // Первая загрузка — при первой подписке на state, один раз за жизнь VM (Lazily).
    private val initialLoad: StateFlow<Unit?> = flow {
        val recent = withContext(dispatchers.io) { models.recent() }
        _state.update { it.copy(recent = recent) }
        load()
        emit(Unit)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Модель подключённого блока — из сессии, а не снимком на старте: блок подключают и отключают при открытом экране.
    private val connected: Flow<String?> = deviceSession.state
        .map { (it as? DeviceSessionState.Ready)?.device?.systemName }
        .distinctUntilChanged()

    val state: StateFlow<ModelsState> = combine(_state, initialLoad, connected) { state, _, connected ->
        state.copy(connected = connected)
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), _state.value)

    private val _effect = sideEffectFlow<ModelsEffect>()
    val effect: SharedFlow<ModelsEffect> = _effect.asSharedFlow()

    private suspend fun load() {
        _state.update { it.copy(list = LCEState.Loading) }
        val response = withContext(dispatchers.io) { models.models() }
        _state.update {
            it.copy(
                list = when (response) {
                    is Response.Error -> {
                        logger.w(response.exception) { "Device models read failed: ${response.message}" }
                        LCEState.Error(ConfiguratorError.from(response.exception))
                    }

                    is Response.Success -> LCEState.Content(response.value)
                }
            )
        }
    }

    fun onRetry() {
        safeLaunch { executeWithProceedLock { load() } }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onDialogDismiss() = _state.update { it.copy(fileError = null) }

    /** «✕» у недавней модели: убрать из списка. */
    fun onRecentRemove(deviceName: String) {
        updateRecent { models.removeRecent(deviceName) }
    }

    fun onPick(deviceName: String) {
        updateRecent { models.pushRecent(deviceName) }
        if (file == null) {
            _effect.tryEmit(ModelsEffect.Open(SourceKey.Defaults(deviceName)))
            return
        }
        val key = file.copy(deviceName = deviceName)
        _state.update { it.copy(checking = true) }
        safeLaunch {
            val source = key.toSource()
            val response = try {
                withContext(dispatchers.io) {
                    try {
                        settings.open(source, reload = true)
                    } finally {
                        settings.close(source)
                    }
                }
            } finally {
                _state.update { it.copy(checking = false) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Settings file did not open as $deviceName: ${response.message}" }
                    _state.update { it.copy(fileError = deviceName) }
                }

                is Response.Success -> _effect.tryEmit(ModelsEffect.Open(key))
            }
        }
    }

    /** Недавние — хранилище (Settings + JSON): правка и перечитка на io. */
    private fun updateRecent(change: () -> Unit) {
        safeLaunch {
            val recent = withContext(dispatchers.io) {
                change()
                models.recent()
            }
            _state.update { it.copy(recent = recent) }
        }
    }

    private companion object {
        val STOP_TIMEOUT = 5.seconds
    }
}
