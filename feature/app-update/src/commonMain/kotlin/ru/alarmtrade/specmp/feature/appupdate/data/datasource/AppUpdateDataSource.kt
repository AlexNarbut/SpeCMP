package ru.alarmtrade.specmp.feature.appupdate.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.LongWrapper
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdatesChannel
import ru.alarmtrade.specialist.domain.appUpdates.UpdateVersionInformation
import ru.alarmtrade.specialist.interactors.appUpdates.AppUpdatesInteractor
import ru.alarmtrade.specialist.interactors.appUpdates.requests.LatestAppVersionRequest
import ru.alarmtrade.specialist.interactors.appUpdates.requests.SetCurrentAppChannelRequest
import ru.alarmtrade.specialist.interactors.files.RemoteFilesInteractor
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest

internal interface AppUpdateDataSource {
    suspend fun channels(): SpecialistResponse<List<AppUpdatesChannel>>

    suspend fun selectedChannelId(): SpecialistResponse<Int>

    suspend fun selectChannel(id: Int): SpecialistResponse<Unit>

    suspend fun latestVersion(channelId: Int): SpecialistResponse<UpdateVersionInformation>

    suspend fun settingsVersion(): SpecialistResponse<LongWrapper>

    fun download(url: String): Flow<SpecialistResponse<ByteArray>>
}

internal class SpecialistAppUpdateDataSource(
    private val appUpdatesInteractor: AppUpdatesInteractor,
    private val filesInteractor: RemoteFilesInteractor,
) : AppUpdateDataSource {
    override suspend fun channels(): SpecialistResponse<List<AppUpdatesChannel>> =
        appUpdatesInteractor.getAvailableAppUpdateChannelsUseCase.execute()

    override suspend fun selectedChannelId(): SpecialistResponse<Int> =
        appUpdatesInteractor.getCurrentSelectedAppUpdateChannelIdUseCase.execute()

    override suspend fun selectChannel(id: Int): SpecialistResponse<Unit> =
        appUpdatesInteractor.setCurrentAppUpdateChannelUseCase.execute(SetCurrentAppChannelRequest(id))

    override suspend fun latestVersion(channelId: Int): SpecialistResponse<UpdateVersionInformation> =
        appUpdatesInteractor.getLatestAppVersionInChannelUseCase.execute(LatestAppVersionRequest(channelId))

    override suspend fun settingsVersion(): SpecialistResponse<LongWrapper> =
        appUpdatesInteractor.getCurrentSettingsVersionUseCase.execute()

    override fun download(url: String): Flow<SpecialistResponse<ByteArray>> = flow {
        emitAll(filesInteractor.downloadFileChunkedUseCase.execute(DownloadFileRequest(url)))
    }
}
