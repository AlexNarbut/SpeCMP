package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.updateIf
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.SourceKey
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.toSource
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.LegacyBaseDeviceViewModel
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
    private val dispatchers: AppDispatchers,
) : LegacyBaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(ModelsState(fileName = file?.name))

    val state: StateFlow<ModelsState> = combine(_state, connectedDeviceName) { state, connected ->
        state.copy(connected = connected)
    }
        .launchOnFirstStart {
            val recent = withContext(dispatchers.io) { models.recent() }
            _state.update { it.copy(recent = recent) }
            // Под тем же замком, что и «Повторить»: первая загрузка и повтор не идут параллельно.
            executeWithProceedLock { load() }
        }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), _state.value)

    private val _effect = sideEffectFlow<ModelsEffect>()
    val effect: SharedFlow<ModelsEffect> = _effect.asSharedFlow()

    private suspend fun load() {
        _state.update { it.copy(list = LCEState.Loading) }
        val response = withContext(dispatchers.io) { models.models() }
        _state.update { it.copy(list = response.asLCEState(ConfiguratorError::from)) }
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
        // Файл уже проверяется — повторный выбор не запускает вторую проверку.
        if (!_state.updateIf({ !it.checking }) { it.copy(checking = true) }) return
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

    /** Недавние — хранилище (Settings + JSON, атомарно само): правка на io, в state — новый список. */
    private fun updateRecent(change: suspend () -> List<String>) {
        safeLaunch {
            val recent = withContext(dispatchers.io) { change() }
            _state.update { it.copy(recent = recent) }
        }
    }

    private companion object {
        val STOP_TIMEOUT = 5.seconds
    }
}
