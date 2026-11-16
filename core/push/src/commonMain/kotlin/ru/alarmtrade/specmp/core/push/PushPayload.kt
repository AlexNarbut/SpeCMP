package ru.alarmtrade.specmp.core.push

/**
 * Push сервера: `data` перекрывает заголовок и текст из `notification`-блока. Неизвестный или пустой `type_id` —
 * [PushType.NOTIFICATION], как в старом приложении.
 */
data class PushPayload(
    val type: PushType,
    val title: String?,
    val body: String?,
    val actionUrl: String?,
    val imageUrl: String?,
) {
    val target: PushTarget
        get() = when {
            type == PushType.CHAT -> PushTarget.Chat
            actionUrl != null && type == PushType.NOTIFICATION && actionUrl.isWebUrl() -> PushTarget.Url(actionUrl)
            else -> PushTarget.App
        }

    companion object {
        const val KEY_TYPE = "type_id"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_ACTION_URL = "action_url"
        const val KEY_IMAGE_URL = "image_url"

        private val UNSUPPORTED_CODES = setOf(2, 3, 6)

        /** `null` — тип, который не показываем, или push без текста. */
        fun parse(data: Map<String, String>, notificationTitle: String? = null, notificationBody: String? = null) =
            parseType(data[KEY_TYPE])?.let { type ->
                PushPayload(
                    type = type,
                    title = data[KEY_TITLE].nonBlank() ?: notificationTitle.nonBlank(),
                    body = data[KEY_BODY].nonBlank() ?: notificationBody.nonBlank(),
                    actionUrl = data[KEY_ACTION_URL].nonBlank(),
                    imageUrl = data[KEY_IMAGE_URL].nonBlank(),
                ).takeIf { it.title != null || it.body != null }
            }

        /** Тап по уведомлению: данные push без своих ключей (обычный запуск приложения) — не push, `null`. */
        fun targetOf(data: Map<String, String>): PushTarget? {
            if (KEY_TYPE !in data && KEY_ACTION_URL !in data) return null
            val type = parseType(data[KEY_TYPE]) ?: return PushTarget.App
            return PushPayload(type, null, null, data[KEY_ACTION_URL].nonBlank(), null).target
        }

        /** `null` — тип, который не показываем. */
        fun typeOf(data: Map<String, String>): PushType? = parseType(data[KEY_TYPE])

        private fun parseType(raw: String?): PushType? {
            val code = raw?.trim()?.toIntOrNull()
            return when (code) {
                in UNSUPPORTED_CODES -> null
                else -> PushType.entries.firstOrNull { it.code == code } ?: PushType.NOTIFICATION
            }
        }

        private fun String?.nonBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

        private fun String.isWebUrl(): Boolean = startsWith("https://", ignoreCase = true) ||
            startsWith("http://", ignoreCase = true)
    }
}
