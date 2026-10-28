package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountError
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
 * (пользователь возвращается туда, где был). Непрочитанные в чате опрашивает репозиторий, пока экран на виду.
 * Работа с блоком — через [DeviceScope] (отменяется при потере связи).
 */
class AdvancedMountRootViewModel(
    private val deviceSession: DeviceSession,
    private val repository: AdvancedMountRepository,
    dispatchers: AppDispatchers
) : BaseViewModel() {

    private val deviceScope = DeviceScope(viewModelScope, deviceSession)

    private val _state = MutableStateFlow(AdvancedMountRootState())

    // Поллинг непрочитанных живёт в репозитории; WhileSubscribed общего state останавливает его, пока корень
    // перекрыт дочерним экраном. Ошибка опроса не сбрасывает последнее известное значение.
    private val unreadCount: Flow<Int> = repository.observeUnreadChatCount()
        .mapNotNull { it.getValueIfSuccess() }
        .onStart { emit(0) }

    // Удалённый монтаж приходит по каналу чата (сессия): подписываемся один раз после первой успешной проверки
    // блока и держим подписку всю жизнь VM (Lazily), без WhileSubscribed.
    private val remoteAvailable: StateFlow<Boolean> = flow {
        _state.first { it.content.content is MountAccess.Ready }
        emitAll(repository.remoteMountAvailable())
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val state: StateFlow<AdvancedMountRootState> = combine(_state, unreadCount, remoteAvailable) {
            state,
            unread,
            remote
        ->
        state.copy(unreadCount = unread, remoteAvailable = remote)
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), _state.value)

    private val _effect = sideEffectFlow<AdvancedMountRootEffect>()
    val effect: SharedFlow<AdvancedMountRootEffect> = _effect.asSharedFlow()

    // Отменяет предыдущую проверку при повторе или новом блоке. Пишется и читается только на main.
    private var loadJob: Job? = null

    init {
        safeLaunch {
            deviceSession.transport.filterNotNull().collect { transport ->
                _state.update { it.copy(transport = transport) }
            }
        }
        deviceScope.onReady(::onDeviceReady)
        safeLaunch {
            val enabled = (repository.isProductInfoEnabled() as? Response.Success)?.value ?: false
            _state.update { it.copy(productInfo = enabled) }
        }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val previous = _state.value
        _state.update { it.copy(device = device) }
        val sameDevice = previous.device?.systemId == device.systemId
        if (!sameDevice || previous.content is LCEState.Error) load(device)

        val imei = (deviceSession.readImei() as? Response.Success)?.value?.takeIf { it.isNotBlank() }
        _state.update { it.copy(imei = imei ?: it.imei) }
    }

    private fun load(device: AlarmDevice) {
        loadJob?.cancel()
        loadJob = safeLaunch {
            _state.update { it.copy(content = LCEState.Loading, dialog = null) }
            val content = when (
                val access = repository.verify(
                    device.systemId,
                    device.systemName,
                    device.firmwareVersion
                )
            ) {
                is Response.Error -> LCEState.Error(MountError.from(access.exception))

                is Response.Success -> if (access.value.hasAccess) {
                    LCEState.Content(
                        MountAccess.Ready(
                            sections = device.features.toSections(),
                            unsupported = device.features == null,
                            modelNotInDatabase = device.features == null && !device.modelInDatabase
                        )
                    )
                } else {
                    LCEState.Content(MountAccess.Denied(access.value.message))
                }
            }
            _state.update { it.copy(content = content) }
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

    /** «Перейти к обновлению»: открыть проверку прошивок — вход в загрузчик (с PIN) запрашивает она. */
    fun onCanUpdateConfirm() {
        _state.update { it.copy(dialog = null) }
        _effect.tryEmit(AdvancedMountRootEffect.OpenSection(MountSection.FIRMWARE_UPDATE))
    }

    fun onRemoteSyncClick() {
        _effect.tryEmit(AdvancedMountRootEffect.OpenRemoteSync)
    }

    fun onProductInfoClick() {
        _effect.tryEmit(AdvancedMountRootEffect.OpenProductInfo)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

sealed interface AdvancedMountRootEffect {
    data class OpenSection(val section: MountSection) : AdvancedMountRootEffect

    data object OpenSupportChat : AdvancedMountRootEffect

    data object OpenRemoteSync : AdvancedMountRootEffect

    data object OpenProductInfo : AdvancedMountRootEffect
}
