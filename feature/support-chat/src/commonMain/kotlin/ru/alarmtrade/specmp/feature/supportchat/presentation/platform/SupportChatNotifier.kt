package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

/**
 * Уведомления ОС о сообщениях техподдержки, пока приложение открыто (desktop — push у него нет). Вызовы
 * [start]/[show]/[dispose] — с Main; [isDenied] ждёт ответа системы — с фонового потока.
 */
interface SupportChatNotifier {

    fun start()

    /** [text] `null` — сообщение без текста (вложение). */
    fun show(text: String?)

    /** Пользователь запретил уведомления: системный запрос повторно не появится — только настройки. */
    fun isDenied(): Boolean

    fun openSettings()

    fun dispose()
}
