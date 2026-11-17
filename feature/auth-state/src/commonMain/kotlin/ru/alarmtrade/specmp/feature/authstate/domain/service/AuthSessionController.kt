package ru.alarmtrade.specmp.feature.authstate.domain.service

/**
 * Наблюдает сессию всю жизнь процесса: вход ставит отметку сессии, переход в «не авторизован» (выход, отзыв сессии
 * сервером, в том числе пока приложение было выгружено) сбрасывает состояние аккаунта — `ReleaseAccountStateUseCase`.
 */
interface AuthSessionController {
    /** Один раз из стартера приложения; повтор ничего не делает. */
    fun start()
}
