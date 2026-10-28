package ru.alarmtrade.specmp.feature.account.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile

/**
 * Профильный источник данных аккаунта.
 *
 * Единая точка доступа к identity текущего пользователя для всех фич
 * (потребность, общая для двух и более фич, живёт в своей фиче, а не дублируется
 * через SDK-интеракторы).
 *
 * Методы не кэшируют результат: жизненным циклом сессии владеет вызывающий
 * код (обычно открытие сессии фичи). Ошибка получения профиля — это ошибка
 * вызывающей операции, а не повод для молчаливой деградации.
 */
interface AccountRepository {

    /**
     * Возвращает профиль текущего аккаунта.
     */
    suspend fun currentProfile(): Response<AccountProfile>
}
