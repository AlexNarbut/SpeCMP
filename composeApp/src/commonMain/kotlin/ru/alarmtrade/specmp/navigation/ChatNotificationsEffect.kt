package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable

/**
 * Desktop: уведомления ОС о новых сообщениях техподдержки, пока приложение открыто (push у десктопа нет).
 * Живёт с авторизованной зоной — после выхода из аккаунта не показывает. Android/iOS — push, здесь ничего.
 */
@Composable
internal expect fun ChatNotificationsEffect(isChatOnTop: Boolean)
