package ru.alarmtrade.specmp.feature.device.autostart.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent

/** Обучение блока бесключевому обходу штатного иммобилайзера (SDK ведёт шаги, приложение их показывает). */
interface BypassLearningRepository {
    /**
     * Запустить обучение: шаги до итога ([BypassEvent.Finished]) или сбоя — последнего `Response.Error` с
     * `BypassLearningException`. Блок должен быть в режиме программирования (`ProgramModeRepository.enter`).
     */
    fun learn(): Flow<Response<BypassEvent>>
}

/** Процедура Clone: ключ вычисляет сервер, приложение пишет его в блок. */
interface CloneRepository {
    /** Оставшееся число попыток Clone для подключённого блока; ошибка — узнать не удалось (сеть, сервер). */
    suspend fun attempts(): Response<Int?>

    /**
     * Запустить процедуру: этапы до итога ([CloneEvent.Finished]) или последнего `Response.Error` с
     * `CloneProcedureException` (неверный PIN — `CloneProcedureFailure.WRONG_PIN`). Режим программирования SDK
     * включает сам.
     */
    fun run(): Flow<Response<CloneEvent>>
}
