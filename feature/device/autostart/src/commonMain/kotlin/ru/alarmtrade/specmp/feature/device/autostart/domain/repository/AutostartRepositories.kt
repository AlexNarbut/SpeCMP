package ru.alarmtrade.specmp.feature.device.autostart.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent

/** Обучение блока бесключевому обходу штатного иммобилайзера (SDK ведёт шаги, приложение их показывает). */
interface BypassLearningRepository {
    /**
     * Запустить обучение: шаги до итога ([BypassEvent.Finished]) или сбоя ([BypassEvent.Failure]).
     * Блок должен быть в режиме программирования (`ProgramModeRepository.enter`).
     */
    fun learn(): Flow<BypassEvent>
}

/** Процедура Clone: ключ вычисляет сервер, приложение пишет его в блок. */
interface CloneRepository {
    /** Оставшееся число попыток Clone для подключённого блока; `null` — узнать не удалось (сеть, сервер). */
    suspend fun attempts(): Int?

    /** Запустить процедуру; режим программирования SDK включает сам (неверный PIN — [CloneEvent.WrongPin]). */
    fun run(): Flow<CloneEvent>
}
