package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.toSections
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceScope
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * Корень расширенного монтажа: шапка подключённого блока и меню разделов по его возможностям.
 * Работа с устройством — через [DeviceScope]; без связи экран перекрыт обёрткой interaction,
 * а состояние здесь сохраняет последний блок.
 */
class AdvancedMountRootViewModel(
    deviceSession: DeviceSession,
) : BaseViewModel() {

    private val deviceScope = DeviceScope(viewModelScope, deviceSession)

    val state: StateFlow<AdvancedMountRootState> = deviceScope.state
        .filterIsInstance<DeviceSessionState.Ready>()
        .map { ready -> AdvancedMountRootState(device = ready.device, sections = ready.device.features.toSections()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AdvancedMountRootState())

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

@Immutable
data class AdvancedMountRootState(
    val device: AlarmDevice? = null,
    val sections: List<MountSection> = emptyList(),
)
