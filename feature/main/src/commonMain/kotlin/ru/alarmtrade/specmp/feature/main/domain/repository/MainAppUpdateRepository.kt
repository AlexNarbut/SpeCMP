package ru.alarmtrade.specmp.feature.main.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateStatus

/** Порт: обновление приложения ведёт `feature/app-update`, адаптер — в composition root. */
interface MainAppUpdateRepository {
    fun status(): Flow<Response<MainAppUpdateStatus>>

    suspend fun check(): Response<Unit>

    /** Начать загрузку найденной версии (кнопка «Обновить» в уведомлении при старте). */
    suspend fun download(): Response<Unit>

    /** Версия, плашку которой закрыли на главном: до следующей новой версии не показываем. */
    suspend fun dismissedBannerVersion(): Response<String?>

    suspend fun dismissBanner(version: String): Response<Unit>
}
