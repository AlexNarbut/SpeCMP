package ru.alarmtrade.specmp.core.push

/** Firebase Messaging из Swift (у Kotlin нет его биндингов). Ошибка — текст, `null` — успех. */
interface IosMessagingClient {
    fun fetchToken(completion: (token: String?, error: String?) -> Unit)

    fun deleteToken(completion: (error: String?) -> Unit)
}
