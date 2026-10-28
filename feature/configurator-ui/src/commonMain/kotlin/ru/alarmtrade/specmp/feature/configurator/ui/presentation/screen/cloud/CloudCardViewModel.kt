package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * K4 карточка облачной конфигурации: полное описание, монтажная карта, вложения, CAN-код (из настроек конфигурации)
 * и предупреждение, если он не совпадает с единой CAN-прошивкой блока ([firmwareCanCode]); «Сохранить в мои».
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class CloudCardViewModel(
    private val id: Int,
    firmwareCanCode: String?,
    private val cloud: CloudConfigurationsRepository,
    private val settings: DeviceSettingsRepository,
    private val configurations: LocalConfigurationsRepository,
    deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(CloudCardState(firmwareCanCode = firmwareCanCode))

    private val retryRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Загрузка — при первой подписке на state (Lazily, один раз за жизнь VM) и по «Повторить»; mapLatest отменяет
    // незаконченную загрузку при повторе.
    private val loads: StateFlow<Unit?> = retryRequests
        .onStart { emit(Unit) }
        .mapLatest { load() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val state: StateFlow<CloudCardState> = combine(_state, loads, connectedDeviceName) { state, _, connected ->
        state.copy(connectedDeviceName = connected)
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), _state.value)

    private val _effect = sideEffectFlow<CloudCardEffect>()
    val effect: SharedFlow<CloudCardEffect> = _effect.asSharedFlow()

    private suspend fun load() {
        _state.update { it.copy(card = LCEState.Loading) }
        when (val response = withContext(dispatchers.io) { cloud.card(id) }) {
            is Response.Error -> {
                logger.w(response.exception) { "Cloud configuration read failed: ${response.message}" }
                _state.update { it.copy(card = LCEState.Error(ConfiguratorError.from(response.exception))) }
            }

            is Response.Success -> {
                _state.update { it.copy(card = LCEState.Content(response.value)) }
                loadCanCode(response.value)
            }
        }
    }

    /** CAN-код — из настроек конфигурации (в метаданных его нет). Не прочитался — без строки CAN. */
    private suspend fun loadCanCode(card: CloudConfiguration) {
        val source = card.toSource()
        val code = withContext(dispatchers.io) {
            try {
                settings.open(source, reload = true).getValueIfSuccess()
                    ?.settings()?.filterIsInstance<CanCodeSetting>()?.firstOrNull()?.value?.value?.code
            } finally {
                settings.close(source)
            }
        }
        _state.update { it.copy(canCode = code?.takeIf { value -> value.isNotBlank() }) }
    }

    fun onRetry() {
        retryRequests.tryEmit(Unit)
    }

    fun onDescriptionToggle() = _state.update { it.copy(descriptionExpanded = !it.descriptionExpanded) }

    fun onAttachmentOpen(attachment: ConfigurationAttachment) {
        if (attachment.isPdf) {
            _effect.tryEmit(CloudCardEffect.OpenUrl(attachment.url))
        } else {
            _state.update { it.copy(viewer = attachment) }
        }
    }

    fun onViewerClose() = _state.update { it.copy(viewer = null) }

    fun onSaveRequest() {
        val card = _state.value.card.content ?: return
        _state.update {
            it.copy(
                saveForm = CloudSaveForm(
                    name = card.summary.name.take(LocalConfiguration.NAME_MAX_LENGTH),
                    description = card.summary.shortDescription.take(LocalConfiguration.DESCRIPTION_MAX_LENGTH)
                )
            )
        }
    }

    fun onSaveChange(name: String, description: String) = _state.update {
        it.copy(saveForm = it.saveForm?.copy(name = name, description = description))
    }

    fun onSaveDismiss() = _state.update { it.copy(saveForm = null) }

    fun onSaveSubmit() {
        val card = _state.value.card.content ?: return
        val form = _state.value.saveForm ?: return
        if (form.name.isBlank()) return
        _state.update { it.copy(saveForm = null, busy = true) }
        safeLaunch {
            val source = card.toSource()
            val response = try {
                withContext(dispatchers.io) {
                    try {
                        configurations.create(
                            form.name.trim(),
                            form.description.trim(),
                            card.summary.deviceName,
                            source
                        )
                    } finally {
                        settings.close(source)
                    }
                }
            } finally {
                _state.update { it.copy(busy = false) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Save cloud configuration failed: ${response.message}" }
                    _effect.tryEmit(CloudCardEffect.SaveFailed)
                }

                is Response.Success -> _effect.tryEmit(CloudCardEffect.SavedToMine(response.value))
            }
        }
    }

    private companion object {
        val STOP_TIMEOUT = 5.seconds
    }
}
