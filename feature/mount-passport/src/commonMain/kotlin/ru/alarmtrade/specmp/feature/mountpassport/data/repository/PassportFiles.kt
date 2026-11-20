package ru.alarmtrade.specmp.feature.mountpassport.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport

/** Паспорт SDK → domain с абсолютными путями фото: папка паспорта — из [files]. */
internal suspend fun SpecialistResponse<SdkMountPassport>.withFiles(
    files: suspend (MountPassportIdRequest) -> SpecialistResponse<MountPassportFiles>,
): Response<MountPassport> = toResponse().mapIfSuccess { passport ->
    files(MountPassportIdRequest(passport.id)).toResponse().map { passport.toDomain(it) }
}
