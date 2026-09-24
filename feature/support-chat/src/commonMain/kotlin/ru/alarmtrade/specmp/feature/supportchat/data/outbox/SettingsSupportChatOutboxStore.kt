package ru.alarmtrade.specmp.feature.supportchat.data.outbox

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Хранение outbox-очереди в `multiplatform-settings` как JSON одной строкой.
 * Синхронные операции (Settings в памяти + диск) — очередь мала (единицы
 * сообщений), поэтому полная перезапись снимка проще и надёжнее диффов.
 */
class SettingsSupportChatOutboxStore(
    private val settings: Settings,
) : SupportChatOutboxStore {

    private val json = Json { ignoreUnknownKeys = true }

    override fun load(): List<SupportChatOutboxEntry> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<SupportChatOutboxEntry>>(raw)
        } catch (e: Exception) {
            // Битый снимок — не повод ломать экран: начинаем с чистой очереди.
            emptyList()
        }
    }

    override fun save(entries: List<SupportChatOutboxEntry>) {
        settings.putString(KEY, json.encodeToString(entries))
    }

    override fun clear() {
        settings.remove(KEY)
    }

    private companion object {
        const val KEY = "support_chat_outbox"
    }
}
