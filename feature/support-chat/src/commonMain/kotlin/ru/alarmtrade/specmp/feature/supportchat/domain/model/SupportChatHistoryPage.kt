package ru.alarmtrade.specmp.feature.supportchat.domain.model

/**
 * Страница истории чата, возвращаемая [ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository.loadNextMessages].
 *
 * Контракт гарантирует завершение вызова: страница возвращается вызывающему
 * (или приходит ошибка/таймаут) — не бывает «успеха без данных».
 */
data class SupportChatHistoryPage(
    val messages: List<SupportChatMessage>,

    /**
     * Есть ли за этой страницей более старая история.
     *
     * HEURISTIC, а не факт источника: Specialist SDK (1.1.76) не отдаёт признак
     * конца истории — `ChatEvent.Chat.MessagesPage` несёт только список сообщений.
     * До появления флага в SDK считаем «страница пустая — значит, история кончилась».
     * Риск зафиксирован: повторная выдача уже известной страницы выключит пагинацию.
     */
    val hasMore: Boolean,
)
