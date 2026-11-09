package ru.alarmtrade.specmp.feature.supportchat.domain.repository

import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage

/**
 * Персистентная очередь моих сообщений, не подтверждённых серверным эхом.
 *
 * `Success` от SDK — не подтверждение доставки: при мёртвом сокете `writeMessage`
 * отвечает успехом, и сообщение теряется. Поэтому запись создаётся **до** отправки
 * и живёт до серверного эха (замены оптимистичного сообщения), а не до `Success`:
 * `Pending` — ждёт отправки, `Failed` — ошибка, `Sent` — ушло в сокет, эха ещё нет.
 * После рестарта `Sent` восстанавливается как `Pending` (доставка неизвестна;
 * at-least-once, дубль отсекает сопоставление с историей). Очередь переживает
 * смерть VM и процесса.
 *
 * Обновляется **явными переходами**, а не снимком ленты: каждая операция —
 * атомарный read-modify-write под мьютексом реализации, поэтому конкурентные
 * переходы из разных корутин не теряются.
 *
 * [AuthCloseable]: `LogoutUseCase` очищает очередь при смене аккаунта.
 */
interface SupportChatOutbox : AuthCloseable {

    suspend fun load(): Response<List<SupportChatMessage>>

    /** Добавить или обновить запись (ключ — `message.id`). Только `Pending`/`Failed`. */
    suspend fun enqueue(message: SupportChatMessage): Response<Unit>

    /**
     * SDK принял сообщение, эха ещё нет. Обновляет **только существующую** запись:
     * если эхо успело прийти раньше и запись уже удалена, она не воскреснет.
     */
    suspend fun markSent(clientId: String): Response<Unit>

    /** Сообщение подтверждено серверным эхом или удалено пользователем. */
    suspend fun remove(clientId: String): Response<Unit>
}
