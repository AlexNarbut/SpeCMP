package ru.alarmtrade.specmp.feature.supportchat.domain.model

/**
 * Страница истории.
 *
 * @param nextCursor курсор следующей (более старой) страницы; `null` — истории больше нет.
 */
data class SupportChatHistoryPage(
    val messages: List<SupportChatMessage>,
    val nextCursor: SupportChatHistoryCursor?,
)

/** Непрозрачный курсор истории: только хранить и передавать обратно. */
data class SupportChatHistoryCursor(val token: Int)

/** Команда отклонена: соединение не в состоянии `Ready`. Сообщение ждёт в очереди, а не падает. */
class SupportChatNotConnectedException(cause: Throwable? = null) : Exception("Chat is not connected", cause)
