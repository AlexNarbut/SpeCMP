package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

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
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.sortedBy
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * K4 список облачных конфигураций автомобиля [carId]: приходит целиком; сортировка «Лучшие / Новые», фильтр по модели
 * блока — локально. [deviceName] — модель блока, под которую запрашивать (пусто — все модели).
 */
internal class CloudListViewModel(
    private val carId: Int,
    private val deviceName: String,
    private val cloud: CloudConfigurationsRepository,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val _state = MutableStateFlow(CloudListState(modelFilter = deviceName.ifEmpty { null }))
    val state: StateFlow<CloudListState> = _state.asStateFlow()

    init {
        safeLaunch { load() }
    }

    private suspend fun load() {
        _state.update { it.copy(list = LCEState.Loading) }
        val response = withContext(dispatchers.io) { cloud.byCar(carId, deviceName) }
        _state.update {
            it.copy(
                list = when (response) {
                    is Response.Error -> {
                        logger.w(response.exception) { "Cloud configurations read failed: ${response.message}" }
                        LCEState.Error(ConfiguratorError.from(response.exception))
                    }

                    is Response.Success -> LCEState.Content(response.value)
                }
            )
        }
    }

    fun onRetry() {
        safeLaunch { load() }
    }

    fun onOrder(order: CloudConfigurationOrder) = _state.update { it.copy(order = order) }

    fun onModelFilter(deviceName: String?) = _state.update { it.copy(modelFilter = deviceName) }

    fun onSelect(id: Int) = _state.update { it.copy(selectedId = id) }
}

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

internal fun CloudConfiguration.toSource(): SettingsSource.RemoteConfiguration =
    SettingsSource.RemoteConfiguration(id = summary.id, deviceName = summary.deviceName, title = summary.name)

@Immutable
internal data class CloudCardState(
    val card: LCEState<CloudConfiguration, ConfiguratorError> = LCEState.Loading,
    val canCode: String? = null,
    val firmwareCanCode: String? = null,
    val descriptionExpanded: Boolean = false,
    /** Открытое вложение-картинка (просмотр с зумом). */
    val viewer: ConfigurationAttachment? = null,
    val saveForm: CloudSaveForm? = null,
    val busy: Boolean = false,
    val connectedDeviceName: String? = null
) {
    /** CAN-код конфигурации не совпадает с единой CAN-прошивкой блока — загрузить нельзя. */
    val canMismatch: Boolean
        get() = firmwareCanCode != null && canCode != null && canCode != firmwareCanCode
}

@Immutable
internal data class CloudSaveForm(val name: String, val description: String)

internal sealed interface CloudCardEffect {
    data class OpenUrl(val url: String) : CloudCardEffect
    data class SavedToMine(val configuration: SettingsSource.LocalConfiguration) : CloudCardEffect
    data object SaveFailed : CloudCardEffect
}
