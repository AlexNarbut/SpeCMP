package ru.alarmtrade.specmp.feature.techrequests.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestPage
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestSubmitStep

interface TechRequestsRepository {
    /** Обновлённые заявки после создания, отмены и закрытия (порциями, без повторов из прошлого). */
    fun updates(): Flow<Response<List<TechRequest>>>

    /** Сбрасывает постраничную загрузку: первая страница. */
    suspend fun loadFirstPage(): Response<TechRequestPage>

    suspend fun loadNextPage(): Response<TechRequestPage>

    /** Читает подключённый блок и отправляет заявку; финал — `Created` или `Response.Error`. */
    fun submit(draft: TechRequestDraft): Flow<Response<TechRequestSubmitStep>>

    suspend fun cancel(id: Long): Response<Unit>

    suspend fun close(id: Long): Response<Unit>
}
