package ru.alarmtrade.specmp.feature.mountpassport.data.repository

import ru.alarmtrade.specialist.domain.mountPassport.MountPassportDocumentPhotos
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStatus
import ru.alarmtrade.specialist.interactors.mountPassport.requests.BuildMountPassportHtmlRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.SetMountPassportStatusRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportDocumentDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toSdk
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentFormat
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentStrings
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportDocumentRepository

/** Фото — внутри HTML (`EMBEDDED`): платформенному рендеру PDF не нужен доступ к папке паспорта. */
internal class SpecialistPassportDocumentRepository(private val dataSource: PassportDocumentDataSource) :
    PassportDocumentRepository {

    override suspend fun buildHtml(
        id: String,
        strings: PassportDocumentStrings,
        format: PassportDocumentFormat,
    ): Response<String> = dataSource.buildHtml(
        BuildMountPassportHtmlRequest(id, strings.toSdk(), format.toSdk(), MountPassportDocumentPhotos.EMBEDDED),
    ).toResponse().map { it.html }

    override suspend fun markIssued(id: String): Response<MountPassport> =
        dataSource.setStatus(SetMountPassportStatusRequest(id, MountPassportStatus.ISSUED)).withFiles(dataSource::files)
}
