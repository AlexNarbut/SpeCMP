package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
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

    private val _state = MutableStateFlow(
        ModelsState(
            fileName = file?.name,
            recent = models.recent(),
            connected = (deviceSession.state.value as? DeviceSessionState.Ready)?.device?.systemName
        )
    )
    val state: StateFlow<ModelsState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<ModelsEffect>()
    val effect: SharedFlow<ModelsEffect> = _effect.asSharedFlow()

    init {
        safeLaunch { load() }
    }

    private suspend fun load() {
        _state.update { it.copy(list = ModelsList.Loading) }
        val response = withContext(dispatchers.io) { models.models() }
        _state.update {
            it.copy(
                list = when (response) {
                    is Response.Error -> {
                        logger.w(response.exception) { "Device models read failed: ${response.message}" }
                        ModelsList.Error
                    }

                    is Response.Success -> ModelsList.Ready(response.value)
                }
            )
        }
    }

    fun onRetry() {
        safeLaunch { load() }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onDialogDismiss() = _state.update { it.copy(fileError = null) }

    /** «✕» у недавней модели: убрать из списка. */
    fun onRecentRemove(deviceName: String) {
        models.removeRecent(deviceName)
        _state.update { it.copy(recent = models.recent()) }
    }

    fun onPick(deviceName: String) {
        models.pushRecent(deviceName)
        _state.update { it.copy(recent = models.recent()) }
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
                    settings.open(source, reload = true).also { settings.close(source) }
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
}

@Immutable
internal data class ModelsState(
    val list: ModelsList = ModelsList.Loading,
    /** Имя файла (K3) — подзаголовок; `null` — «С нуля». */
    val fileName: String? = null,
    val recent: List<String> = emptyList(),
    val connected: String? = null,
    val query: String = "",
    /** Проверяем файл в выбранной модели. */
    val checking: Boolean = false,
    /** Файл не открылся для этой модели. */
    val fileError: String? = null
)

internal sealed interface ModelsList {
    data object Loading : ModelsList
    data object Error : ModelsList
    data class Ready(val models: List<String>) : ModelsList
}

internal sealed interface ModelsEffect {
    data class Open(val source: SourceKey) : ModelsEffect
}
