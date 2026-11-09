package ru.alarmtrade.specmp.feature.supportchat.domain.repository

import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage

/**
 * `Success` от SDK — не подтверждение доставки (при мёртвом сокете `writeMessage` отвечает успехом), поэтому запись
 * создаётся до отправки и живёт до серверного эха; после рестарта `Sent` восстанавливается как `Pending`.
 */
interface SupportChatOutbox : AuthCloseable {

    suspend fun load(): Response<List<SupportChatMessage>>

    /** Добавить или обновить запись (ключ — `message.id`). Только `Pending`/`Failed`. */
    suspend fun enqueue(message: SupportChatMessage): Response<Unit>

    /** Обновляет только существующую запись: если эхо пришло раньше и запись удалена, она не воскреснет. */
    suspend fun markSent(clientId: String): Response<Unit>

    suspend fun remove(clientId: String): Response<Unit>
}
