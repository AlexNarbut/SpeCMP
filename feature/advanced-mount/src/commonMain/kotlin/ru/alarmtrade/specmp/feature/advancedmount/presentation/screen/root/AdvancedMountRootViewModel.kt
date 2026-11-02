package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountError
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.toSections
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel

/**
 * Корень расширенного монтажа: шапка блока и разделы.
 *
 * На каждом подключении блока: IMEI; для нового блока — проверка на сервере → разделы по возможностям
 * и подписка на удалённый монтаж. После переподключения того же блока разделы не перезагружаются
 * (пользователь возвращается туда, где был). Непрочитанные в чате опрашивает репозиторий, пока экран на виду.
 * Работа с блоком — через [BaseDeviceViewModel] (отменяется при потере связи).
 */
class AdvancedMountRootViewModel(
    deviceSession: DeviceSession,
    private val repository: AdvancedMountRepository,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(AdvancedMountRootState())

    // Поллинг непрочитанных живёт в репозитории; WhileSubscribed общего state останавливает его, пока корень
    // перекрыт дочерним экраном. Ошибка опроса не сбрасывает последнее известное значение.
    private val unreadCount: Flow<Int> = repository.observeUnreadChatCount()
        .flowOn(dispatchers.io)
        .mapNotNull { it.getValueIfSuccess() }
        .onStart { emit(0) }

    // Удалённый монтаж приходит по каналу чата (сессия): подписываемся один раз после первой успешной проверки
    // блока и держим подписку всю жизнь VM (Lazily), без WhileSubscribed.
    private val remoteAvailable: StateFlow<Boolean> = flow {
        _state.first { it.content.content is MountAccess.Ready }
        emitAll(repository.remoteMountAvailable())
    }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Транспорт последнего подключения: на время переподключения (null) показываем прежний.
    private val transport: Flow<ConnectionTransport?> = deviceSession.transport
        .runningFold(null as ConnectionTransport?) { last, current -> current ?: last }

    // Кнопка «Информация о товаре» — один раз за жизнь VM, при первой подписке на state.
    private val productInfo: StateFlow<Boolean> = flow {
        emit((repository.isProductInfoEnabled() as? Response.Success)?.value ?: false)
    }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val state: StateFlow<AdvancedMountRootState> = combine(
        _state,
        unreadCount,
        remoteAvailable,
        transport,
        productInfo
    ) { state, unread, remote, transport, productInfo ->
        state.copy(unreadCount = unread, remoteAvailable = remote, transport = transport, productInfo = productInfo)
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), _state.value)

    private val _effect = sideEffectFlow<AdvancedMountRootEffect>()
    val effect: SharedFlow<AdvancedMountRootEffect> = _effect.asSharedFlow()

    // Проверка блока: новый запрос (повтор или другой блок) отменяет незаконченную прежнюю —
    // collectLatest, без поля-Job.
    private val loadRequests = MutableSharedFlow<AlarmDevice>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)

    init {
        safeLaunch { loadRequests.collectLatest(::verify) }
        // Реакция на каждое (пере)подключение блока живёт всю жизнь VM: проверка нового блока и IMEI.
        onEachDeviceReady(::onDeviceReady)
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val previous = _state.value
        _state.update { it.copy(device = device) }
        val sameDevice = previous.device?.systemId == device.systemId
        if (!sameDevice || previous.content is LCEState.Error) load(device)

        val imei = (withContext(dispatchers.io) { deviceSession.readImei() } as? Response.Success)?.value
            ?.takeIf { it.isNotBlank() }
        _state.update { it.copy(imei = imei ?: it.imei) }
    }

    private fun load(device: AlarmDevice) {
        loadRequests.tryEmit(device)
    }

    private suspend fun verify(device: AlarmDevice) {
        _state.update { it.copy(content = LCEState.Loading, dialog = null) }
        val content = when (
            val access = withContext(dispatchers.io) {
                repository.verify(device.systemId, device.systemName, device.firmwareVersion)
            }
        ) {
            is Response.Error -> LCEState.Error(MountError.from(access.exception))

            is Response.Success -> if (access.value.hasAccess) {
                LCEState.Content(
                    MountAccess.Ready(
                        sections = device.features.toSections(
                            bluetooth = deviceSession.transport.value == ConnectionTransport.BLE
                        ),
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
