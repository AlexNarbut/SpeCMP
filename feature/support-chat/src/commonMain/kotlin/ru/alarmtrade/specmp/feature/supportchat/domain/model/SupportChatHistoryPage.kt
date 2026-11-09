package ru.alarmtrade.specmp.feature.supportchat.domain.model

/** [nextCursor] `null` — истории больше нет. */
data class SupportChatHistoryPage(val messages: List<SupportChatMessage>, val nextCursor: SupportChatHistoryCursor?)

/** Непрозрачный курсор истории: только хранить и передавать обратно. */
data class SupportChatHistoryCursor(val token: Int)
