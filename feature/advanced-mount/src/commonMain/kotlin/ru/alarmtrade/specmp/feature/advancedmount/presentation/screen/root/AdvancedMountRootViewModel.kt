package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.toSections
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceScope
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * Корень расширенного монтажа: шапка блока и разделы.
 *
 * На каждом подключении блока: IMEI; для нового блока — проверка на сервере → разделы по возможностям
 * и подписка на удалённый монтаж. После переподключения того же блока разделы не перезагружаются
 * (пользователь возвращается туда, где был). Непрочитанные в чате опрашиваются раз в минуту.
 * Работа с блоком — через [DeviceScope] (отменяется при потере связи).
 */
class AdvancedMountRootViewModel(
    private val deviceSession: DeviceSession,
    private val repository: AdvancedMountRepository,
) : BaseViewModel() {

    private val deviceScope = DeviceScope(viewModelScope, deviceSession)

    private val _state = MutableStateFlow(AdvancedMountRootState())
    val state: StateFlow<AdvancedMountRootState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<AdvancedMountRootEffect>()
    val effect: SharedFlow<AdvancedMountRootEffect> = _effect.asSharedFlow()

    private var loadJob: Job? = null
    private var remoteJob: Job? = null

    init {
        safeLaunch {
            deviceSession.transport.filterNotNull().collect { transport -> _state.update { it.copy(transport = transport) } }
        }
        deviceScope.onReady(::onDeviceReady)
        safeLaunch {
            val enabled = (repository.isProductInfoEnabled() as? Response.Success)?.value ?: false
            _state.update { it.copy(productInfo = enabled) }
        }
        safeLaunch {
            while (isActive) {
                (repository.unreadChatCount() as? Response.Success)?.let { count ->
                    _state.update { it.copy(unreadCount = count.value) }
                }
                delay(UNREAD_POLL_MILLIS)
            }
        }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val previous = _state.value
        _state.update { it.copy(device = device) }
        val sameDevice = previous.device?.systemId == device.systemId
        if (!sameDevice || previous.content == MountContent.VerifyError) load(device)

        val imei = (deviceSession.readImei() as? Response.Success)?.value?.takeIf { it.isNotBlank() }
        _state.update { it.copy(imei = imei ?: it.imei) }
    }

    private fun load(device: AlarmDevice) {
        loadJob?.cancel()
        loadJob = safeLaunch {
            _state.update { it.copy(content = MountContent.Loading, dialog = null) }
            val content = when (val access = repository.verify(device.systemId, device.systemName, device.firmwareVersion)) {
                is Response.Error -> MountContent.VerifyError
                is Response.Success -> if (access.value.hasAccess) {
                    MountContent.Ready(sections = device.features.toSections(), unsupported = device.features == null)
                } else {
                    MountContent.Denied(access.value.message)
                }
            }
            _state.update { it.copy(content = content) }
            if (content is MountContent.Ready) listenRemoteMount()
        }
    }

    private fun listenRemoteMount() {
        if (remoteJob?.isActive == true) return
        remoteJob = safeLaunch {
            repository.remoteMountAvailable().collect { available -> _state.update { it.copy(remoteAvailable = available) } }
        }
    }

    /** «Исправить» / «Повторить»: заново проверить блок и собрать разделы. */
    fun onRetry() {
        _state.value.device?.let(::load)
    }

    fun onSectionClick(section: MountSection) {
        val state = _state.value
        when {
            state.canLocked && section.needsCan -> _state.update { it.copy(dialog = MountDialog.CAN_UPDATE) }
            section == MountSection.SUPPORT_CHAT -> _effect.tryEmit(AdvancedMountRootEffect.OpenSupportChat)
            else -> _effect.tryEmit(AdvancedMountRootEffect.OpenSection(section))
        }
    }

    fun onCanHintClick() {
        _state.update { it.copy(dialog = MountDialog.CAN_HINT) }
    }

    fun onDialogDismiss() {
        _state.update { it.copy(dialog = null) }
    }

    /** «Перейти к обновлению»: блок уходит в загрузчик, обёртка покажет экран загрузчика после переподключения. */
    fun onCanUpdateConfirm() {
        _state.update { it.copy(dialog = null) }
        deviceScope.launch { deviceSession.setBootMode() }
    }

    fun onRemoteSyncClick() {
        _effect.tryEmit(AdvancedMountRootEffect.OpenRemoteSync)
    }

    fun onProductInfoClick() {
        _effect.tryEmit(AdvancedMountRootEffect.OpenProductInfo)
    }

    private companion object {
        const val UNREAD_POLL_MILLIS = 60_000L
    }
}

sealed interface AdvancedMountRootEffect {
    data class OpenSection(val section: MountSection) : AdvancedMountRootEffect

    data object OpenSupportChat : AdvancedMountRootEffect

    data object OpenRemoteSync : AdvancedMountRootEffect

    data object OpenProductInfo : AdvancedMountRootEffect
}
