package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountError
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.toSections
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

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
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<AdvancedMountRootState, AdvancedMountRootEffect>(deviceSession, AdvancedMountRootState()) {

    override suspend fun onStart() {
        // Поллинг непрочитанных живёт в репозитории; привязка к подписке останавливает его, пока корень
        // перекрыт дочерним экраном. Ошибка опроса не сбрасывает последнее известное значение.
        repository.observeUnreadChatCount()
            .flowOn(dispatchers.io)
            .mapNotNull { it.getValueIfSuccess() }
            .onStart { emit(0) }
            .collectIntoState { unread -> copy(unreadCount = unread) }
        // Транспорт последнего подключения: на время переподключения (null) показываем прежний.
        deviceSession.transport.collectIntoState { transport -> copy(transport = transport ?: this.transport) }
        // Удалённый монтаж приходит по каналу чата (сессия): подписываемся один раз после первой успешной проверки
        // блока и держим подписку всю жизнь VM.
        safeLaunch {
            select { content.content is MountAccess.Ready }.first { it }
            repository.remoteMountAvailable()
                .flowOn(dispatchers.io)
                .collect { available -> reduce { copy(remoteAvailable = available.getValueIfSuccess() ?: false) } }
        }
        // Кнопка «Информация о товаре» — один раз за жизнь VM.
        safeLaunch {
            val enabled = withContext(dispatchers.io) { repository.isProductInfoEnabled() }
            reduce { copy(productInfo = (enabled as? Response.Success)?.value ?: false) }
        }
        // Реакция на каждое (пере)подключение блока живёт всю жизнь VM: проверка нового блока и IMEI.
        onEachDeviceReady(::onDeviceReady)
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val previous = currentState
        reduce { copy(device = device) }
        val sameDevice = previous.device?.systemId == device.systemId
        if (!sameDevice || previous.content is LCEState.Error) load(device)

        val imei = (withContext(dispatchers.io) { deviceSession.readImei() } as? Response.Success)?.value
            ?.takeIf { it.isNotBlank() }
        reduce { copy(imei = imei ?: this.imei) }
    }

    // Проверка блока: новый запрос (повтор или другой блок) отменяет незаконченную прежнюю.
    private fun load(device: AlarmDevice) {
        launchReplacing(VERIFY_KEY) { verify(device) }
    }

    private suspend fun verify(device: AlarmDevice) {
        reduce { copy(content = LCEState.Loading, dialog = null) }
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
                            bluetooth = deviceSession.transport.value == ConnectionTransport.BLE,
                        ),
                        unsupported = device.features == null,
                        modelNotInDatabase = device.features == null && !device.modelInDatabase,
                    ),
                )
            } else {
                LCEState.Content(MountAccess.Denied(access.value.message))
            }
        }
        reduce { copy(content = content) }
    }

    /** «Исправить» / «Повторить»: заново проверить блок и собрать разделы. */
    fun onRetry() {
        currentState.device?.let(::load)
    }

    fun onSectionClick(section: MountSection) {
        when {
            currentState.canLocked && section.needsCan -> reduce { copy(dialog = MountDialog.CAN_UPDATE) }
            section == MountSection.SUPPORT_CHAT -> postSideEffect(AdvancedMountRootEffect.OpenSupportChat)
            else -> postSideEffect(AdvancedMountRootEffect.OpenSection(section))
        }
    }

    fun onCanHintClick() {
        reduce { copy(dialog = MountDialog.CAN_HINT) }
    }

    fun onDialogDismiss() {
        reduce { copy(dialog = null) }
    }

    /** «Перейти к обновлению»: открыть проверку прошивок — вход в загрузчик (с PIN) запрашивает она. */
    fun onCanUpdateConfirm() {
        reduce { copy(dialog = null) }
        postSideEffect(AdvancedMountRootEffect.OpenSection(MountSection.FIRMWARE_UPDATE))
    }

    fun onRemoteSyncClick() {
        postSideEffect(AdvancedMountRootEffect.OpenRemoteSync)
    }

    fun onProductInfoClick() {
        postSideEffect(AdvancedMountRootEffect.OpenProductInfo)
    }

    private companion object {
        const val VERIFY_KEY = "verify"
    }
}
