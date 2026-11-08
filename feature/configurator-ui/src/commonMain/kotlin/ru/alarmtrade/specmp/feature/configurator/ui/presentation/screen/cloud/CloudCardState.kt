package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError

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
    val connectedDeviceName: String? = null,
) {
    /** CAN-код конфигурации не совпадает с единой CAN-прошивкой блока — загрузить нельзя. */
    val canMismatch: Boolean
        get() = firmwareCanCode != null && canCode != null && canCode != firmwareCanCode
}

@Immutable
internal data class CloudSaveForm(val name: String, val description: String)
