package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * K4 карточка облачной конфигурации: полное описание, монтажная карта, вложения, CAN-код (из настроек конфигурации)
 * и предупреждение, если он не совпадает с единой CAN-прошивкой блока ([firmwareCanCode]); «Сохранить в мои».
 */
internal class CloudCardViewModel(
    private val id: Int,
    firmwareCanCode: String?,
    private val cloud: CloudConfigurationsRepository,
    private val settings: DeviceSettingsRepository,
    private val configurations: LocalConfigurationsRepository,
    deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CloudCardState(
            firmwareCanCode = firmwareCanCode,
            connectedDeviceName = (deviceSession.state.value as? DeviceSessionState.Ready)?.device?.systemName
        )
    )
    val state: StateFlow<CloudCardState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<CloudCardEffect>()
    val effect: SharedFlow<CloudCardEffect> = _effect.asSharedFlow()

    init {
        safeLaunch { load() }
    }

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
            settings.open(source, reload = true).getValueIfSuccess()
                ?.settings()?.filterIsInstance<CanCodeSetting>()?.firstOrNull()?.value?.value?.code
                .also { settings.close(source) }
        }
        _state.update { it.copy(canCode = code?.takeIf { value -> value.isNotBlank() }) }
    }

    fun onRetry() {
        safeLaunch { load() }
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
                    configurations.create(
                        form.name.trim(),
                        form.description.trim(),
                        card.summary.deviceName,
                        from = source
                    ).also { settings.close(source) }
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
}
