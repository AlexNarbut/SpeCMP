package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

internal class CloudCardViewModel(
    private val id: Int,
    firmwareCanCode: String?,
    private val cloud: CloudConfigurationsRepository,
    private val settings: DeviceSettingsRepository,
    private val configurations: LocalConfigurationsRepository,
    deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<CloudCardState, CloudCardEffect>(
    deviceSession,
    CloudCardState(firmwareCanCode = firmwareCanCode),
) {

    override suspend fun onStart() {
        connectedDeviceName.collectIntoState { connected -> copy(connectedDeviceName = connected) }
        load()
    }

    // Загрузка живёт с VM, не с подпиской экрана; повтор отменяет незаконченную.
    private fun load() {
        launchReplacing(LOAD_KEY) {
            reduce { copy(card = LCEState.Loading) }
            val response = withContext(dispatchers.io) { cloud.card(id) }
            reduce { copy(card = response.asLCEState(ConfiguratorError::from)) }
            response.getValueIfSuccess()?.let { loadCanCode(it) }
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
                settings.close(source).onError { logger.w(it) { "Failed to close the settings session" } }
            }
        }
        reduce { copy(canCode = code?.takeIf { value -> value.isNotBlank() }) }
    }

    fun onRetry() {
        load()
    }

    fun onDescriptionToggle() {
        reduce { copy(descriptionExpanded = !descriptionExpanded) }
    }

    fun onAttachmentOpen(attachment: ConfigurationAttachment) {
        if (attachment.isPdf) {
            postSideEffect(CloudCardEffect.OpenUrl(attachment.url))
        } else {
            reduce { copy(viewer = attachment) }
        }
    }

    fun onViewerClose() {
        reduce { copy(viewer = null) }
    }

    fun onSaveRequest() {
        val card = currentState.card.content ?: return
        reduce {
            copy(
                saveForm = CloudSaveForm(
                    name = card.summary.name.take(LocalConfiguration.NAME_MAX_LENGTH),
                    description = card.summary.shortDescription.take(LocalConfiguration.DESCRIPTION_MAX_LENGTH),
                ),
            )
        }
    }

    fun onSaveChange(name: String, description: String) {
        reduce { copy(saveForm = saveForm?.copy(name = name, description = description)) }
    }

    fun onSaveDismiss() {
        reduce { copy(saveForm = null) }
    }

    fun onSaveSubmit() {
        val card = currentState.card.content ?: return
        val form = currentState.saveForm ?: return
        if (form.name.isBlank()) return
        reduce { copy(saveForm = null, busy = true) }
        intent {
            val source = card.toSource()
            val response = try {
                withContext(dispatchers.io) {
                    try {
                        configurations.create(
                            form.name.trim(),
                            form.description.trim(),
                            card.summary.deviceName,
                            source,
                        )
                    } finally {
                        settings.close(source).onError { logger.w(it) { "Failed to close the settings session" } }
                    }
                }
            } finally {
                reduce { copy(busy = false) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Save cloud configuration failed: ${response.message}" }
                    postSideEffect(CloudCardEffect.SaveFailed)
                }

                is Response.Success -> postSideEffect(CloudCardEffect.SavedToMine(response.value))
            }
        }
    }

    private companion object {
        const val LOAD_KEY = "load"
    }
}
