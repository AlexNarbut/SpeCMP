package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.notifications

import androidx.compose.runtime.Immutable

/** [askToEnable] — уведомления запрещены в системе: предложить открыть настройки. */
@Immutable
data class SupportChatNotificationsState(val askToEnable: Boolean = false)
