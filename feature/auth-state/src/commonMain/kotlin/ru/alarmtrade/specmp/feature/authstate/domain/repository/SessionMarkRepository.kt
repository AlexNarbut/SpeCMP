package ru.alarmtrade.specmp.feature.authstate.domain.repository

import ru.alarmtrade.specmp.core.common.Response

/** Отметка «в приложении была сессия, состояние аккаунта ещё не сброшено»; переживает перезапуск процесса. */
interface SessionMarkRepository {
    suspend fun isMarked(): Response<Boolean>

    suspend fun mark(): Response<Unit>

    suspend fun clear(): Response<Unit>
}
