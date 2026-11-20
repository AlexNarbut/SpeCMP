package ru.alarmtrade.specmp.mountpassport

import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportInstallerRepository

/**
 * Исполнитель паспорта — из профиля аккаунта: имя и контакт (телефон, иначе email). Профиль без кэша и может идти в
 * сеть, поэтому ожидание ограничено [PROFILE_TIMEOUT_MS]: считывание блока не ждёт медленную сеть.
 */
internal class AppPassportInstallerRepository(private val account: AccountRepository) : PassportInstallerRepository {

    override suspend fun installer(): Response<PassportInstaller> =
        withTimeoutOrNull(PROFILE_TIMEOUT_MS) { account.currentProfile() }
            ?.map { profile ->
                PassportInstaller(
                    name = profile.name.trim().takeIf { it.isNotEmpty() },
                    contact =
                        profile.phone?.trim()?.takeIf { it.isNotEmpty() }
                            ?: profile.email.trim().takeIf { it.isNotEmpty() },
                )
            }
            ?: Response.Error(IllegalStateException("Account profile timeout"))

    private companion object {
        const val PROFILE_TIMEOUT_MS = 3_000L
    }
}
