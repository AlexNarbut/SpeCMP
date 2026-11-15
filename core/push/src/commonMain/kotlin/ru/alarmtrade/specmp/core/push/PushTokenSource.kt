package ru.alarmtrade.specmp.core.push

import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response

/**
 * Токен push-сервиса платформы (FCM). На logout ([AuthCloseable]) токен удаляется: следующий вход получит новый,
 * и сервер не шлёт push прежнего аккаунта на это устройство.
 */
interface PushTokenSource : AuthCloseable {

    /** `null` — у платформы нет push (desktop) или сервис недоступен. */
    suspend fun token(): Response<String?>
}
