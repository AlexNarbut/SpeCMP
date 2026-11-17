package ru.alarmtrade.specmp.core.common

/**
 * Сброс состояния app-scoped синглтонов при выходе из аккаунта — любом переходе «авторизован → не авторизован»
 * (кнопка «Выйти», отзыв сессии, в том числе пока приложение было выгружено); один раз на сессию, на старте без
 * сессии не вызывается. Вызывает `ReleaseAccountStateUseCase` (`feature/auth-state`). Не бросает: ошибки очистки
 * логирует сама реализация.
 */
interface AuthCloseable {

    suspend fun release()
}
