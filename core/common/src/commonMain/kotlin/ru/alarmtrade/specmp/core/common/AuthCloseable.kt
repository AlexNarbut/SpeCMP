package ru.alarmtrade.specmp.core.common

/**
 * Сброс состояния app-scoped синглтонов при смене аккаунта; вызывает `LogoutUseCase` после успешного logout.
 * Не бросает: ошибки очистки логирует сама реализация.
 */
interface AuthCloseable {

    suspend fun release()
}
