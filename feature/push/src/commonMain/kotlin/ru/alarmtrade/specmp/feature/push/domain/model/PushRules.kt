package ru.alarmtrade.specmp.feature.push.domain.model

import ru.alarmtrade.specmp.core.push.PushType

/** [codes] — `type_id`, которые сервер шлёт аккаунту; неподдерживаемые приложением коды сохраняются как были. */
data class PushRules(val codes: Set<Int>) {

    fun isEnabled(type: PushType): Boolean = type.code in codes

    fun with(type: PushType, enabled: Boolean): PushRules =
        PushRules(if (enabled) codes + type.code else codes - type.code)
}
