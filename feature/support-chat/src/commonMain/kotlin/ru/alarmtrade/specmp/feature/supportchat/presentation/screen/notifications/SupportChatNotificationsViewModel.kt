package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.notifications

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatNotifier

/**
 * Живёт с авторизованной зоной (её хранилище VM очищается при выходе из аккаунта): всё это время держит сессию чата —
 * в SDK подписчики считаются, с экраном чата соединение одно — и показывает чужие сообщения через [notifier].
 */
class SupportChatNotificationsViewModel(
    private val repository: SupportChatRepository,
    private val notifier: SupportChatNotifier,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<SupportChatNotificationsState, Nothing>(SupportChatNotificationsState()) {

    override suspend fun IntentScope.onStart() {
        notifier.start()
        // Системный запрос разрешения — один раз; после отказа ведём в настройки на каждом входе в аккаунт.
        val denied = withContext(dispatchers.io) { notifier.isDenied() }
        reduce { copy(askToEnable = denied) }
        repository.session().collect { response ->
            val message = ((response as? Response.Success)?.value as? SupportChatEvent.MessageReceived)?.message
            if (message != null && !message.isMine) notifier.show(message.text?.takeIf { it.isNotBlank() })
        }
    }

    fun onOpenSettings() {
        intent {
            reduce { copy(askToEnable = false) }
            notifier.openSettings()
        }
    }

    fun onDismiss() {
        intent { reduce { copy(askToEnable = false) } }
    }

    override fun onCleared() {
        notifier.dispose()
        super.onCleared()
    }
}
