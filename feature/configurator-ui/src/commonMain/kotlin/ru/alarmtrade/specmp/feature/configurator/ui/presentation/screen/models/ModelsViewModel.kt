package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.SourceKey
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.toSource
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/** Файл `.dxs` проверяется чтением в выбранной модели: не открылся — выбрать другую. */
internal class ModelsViewModel(
    private val file: SourceKey.File?,
    private val models: DeviceModelsRepository,
    private val settings: DeviceSettingsRepository,
    deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<ModelsState, ModelsEffect>(deviceSession, ModelsState(fileName = file?.name)) {

    override suspend fun IntentScope.onStart() {
        connectedDeviceName.collectIntoState { connected -> copy(connected = connected) }
        when (val recent = withContext(dispatchers.io) { models.recent() }) {
            is Response.Success -> reduce { copy(recent = recent.value) }
            is Response.Error -> logger.w(recent.exception) { "Recent models read failed" }
        }
        // Под тем же замком, что и «Повторить»: первая загрузка и повтор не идут параллельно.
        executeWithProceedLock { load() }
    }

    private suspend fun load() = subIntent {
        reduce { copy(list = LCEState.Loading) }
        val response = withContext(dispatchers.io) { models.models() }
        reduce { copy(list = response.asLCEState(ConfiguratorError::from)) }
    }

    fun onRetry() {
        intent { executeWithProceedLock { load() } }
    }

    fun onQueryChange(query: String) {
        intent { reduce { copy(query = query) } }
    }

    fun onDialogDismiss() {
        intent { reduce { copy(fileError = null) } }
    }

    fun onRecentRemove(deviceName: String) {
        updateRecent { models.removeRecent(deviceName) }
    }

    fun onPick(deviceName: String) {
        updateRecent { models.pushRecent(deviceName) }
        intent {
            if (file == null) {
                postSideEffect(ModelsEffect.Open(SourceKey.Defaults(deviceName)))
                return@intent
            }
            if (currentState.checking) return@intent
            reduce { copy(checking = true) }
            val key = file.copy(deviceName = deviceName)
            val source = key.toSource()
            val response = try {
                withContext(dispatchers.io) {
                    try {
                        settings.open(source, reload = true)
                    } finally {
                        settings.close(source).onError { logger.w(it) { "Failed to close the settings session" } }
                    }
                }
            } finally {
                reduce { copy(checking = false) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Settings file did not open as $deviceName: ${response.message}" }
                    reduce { copy(fileError = deviceName) }
                }

                is Response.Success -> postSideEffect(ModelsEffect.Open(key))
            }
        }
    }

    private fun updateRecent(change: suspend () -> Response<List<String>>) {
        intent {
            when (val recent = withContext(dispatchers.io) { change() }) {
                is Response.Success -> reduce { copy(recent = recent.value) }
                is Response.Error -> logger.w(recent.exception) { "Recent models update failed" }
            }
        }
    }
}
