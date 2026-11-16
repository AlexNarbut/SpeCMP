package ru.alarmtrade.specmp.core.push

/** Firebase Messaging из Swift (iOS, macOS): у Kotlin нет его биндингов. Ошибка — текст, `null` — успех. */
interface PushMessagingClient {
    fun fetchToken(completion: (token: String?, error: String?) -> Unit)

    fun deleteToken(completion: (error: String?) -> Unit)
}
