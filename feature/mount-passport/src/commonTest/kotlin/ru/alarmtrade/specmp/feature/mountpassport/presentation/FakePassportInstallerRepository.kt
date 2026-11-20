package ru.alarmtrade.specmp.feature.mountpassport.presentation

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportInstallerRepository

internal class FakePassportInstallerRepository : PassportInstallerRepository {
    var result: Response<PassportInstaller> =
        Response.Success(PassportInstaller(name = "Алексей Смирнов", contact = "+79123456789"))

    override suspend fun installer(): Response<PassportInstaller> = result
}
