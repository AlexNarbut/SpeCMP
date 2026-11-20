package ru.alarmtrade.specmp.feature.mountpassport.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller

/** Исполнитель из профиля аккаунта (имя, контакт): фича от аккаунта не зависит, адаптер — в корне приложения. */
interface PassportInstallerRepository {
    suspend fun installer(): Response<PassportInstaller>
}
