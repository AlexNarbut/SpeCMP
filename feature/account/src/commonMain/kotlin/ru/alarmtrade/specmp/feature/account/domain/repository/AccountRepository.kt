package ru.alarmtrade.specmp.feature.account.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile

/** Без кэша: жизненным циклом сессии владеет вызывающий; ошибка профиля — ошибка вызывающей операции. */
interface AccountRepository {

    suspend fun currentProfile(): Response<AccountProfile>

    /** Подготовленное изображение (сервер его не уменьшает); успех — абсолютный URL нового аватара. */
    suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): Response<String>
}
