package ru.alarmtrade.specmp.feature.appupdate.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdatesChannel
import ru.alarmtrade.specialist.domain.appUpdates.UpdateChannelAccessLevel
import ru.alarmtrade.specialist.domain.appUpdates.UpdateVersionInformation
import ru.alarmtrade.specialist.domain.errors.UpdateVersionNotFoundException
import ru.alarmtrade.specmp.core.common.AppVersionProvider
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.appupdate.data.datasource.AppUpdateDataSource
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateChannel
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheck
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateOverview
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateRelease
import ru.alarmtrade.specmp.feature.appupdate.domain.repository.AppUpdateRepository

internal class SpecialistAppUpdateRepository(
    private val dataSource: AppUpdateDataSource,
    private val appVersionProvider: AppVersionProvider,
) : AppUpdateRepository {
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun overview(withChannelVersions: Boolean): Response<AppUpdateOverview> {
        val channels = when (val response = dataSource.channels().toResponse()) {
            is Response.Success -> response.value
            is Response.Error -> return response
        }
        val selected = selectedChannel(channels)
        val settingsVersion = dataSource.settingsVersion().toResponse().getValueIfSuccess()?.long
        val asked = channels.filter { withChannelVersions || it.id == selected?.id }
        val latest = coroutineScope {
            asked.map { channel -> async { channel.id to dataSource.latestVersion(channel.id) } }.awaitAll().toMap()
        }
        val selectedLatest = selected?.let { latest[it.id] }
        val check = when {
            selected == null || selectedLatest == null -> AppUpdateCheck.EmptyChannel
            selectedLatest is SpecialistResponse.Success -> selectedLatest.value.toCheck()
            selectedLatest.isEmptyChannel() -> AppUpdateCheck.EmptyChannel
            else -> return selectedLatest.toResponse() as Response.Error
        }
        return Response.Success(
            AppUpdateOverview(
                check = check,
                channels = channels.map { it.toDomain(latest[it.id]) },
                selectedId = selected?.id,
                settingsVersion = settingsVersion,
            ),
        )
    }

    override suspend fun selectChannel(id: Int): Response<Unit> = dataSource.selectChannel(id).toResponse()

    override fun download(url: String): Flow<Response<ByteArray>> = flow {
        emitAll(safeFlowRun { dataSource.download(url).map { it.toResponse() } })
    }

    // Как в SDK: сохранённый канал, пока он доступен, иначе первый публичный — он и сохраняется.
    private suspend fun selectedChannel(channels: List<AppUpdatesChannel>): AppUpdatesChannel? {
        val savedId = dataSource.selectedChannelId().toResponse().getValueIfSuccess()
        channels.firstOrNull { it.id == savedId }?.let { return it }
        val fallback =
            channels.firstOrNull { it.accessLevel == UpdateChannelAccessLevel.PUBLIC } ?: channels.firstOrNull()
        fallback?.let { channel ->
            dataSource.selectChannel(channel.id).toResponse().onError {
                logger.w(it) { "Default channel is not saved" }
            }
        }
        return fallback
    }

    private fun UpdateVersionInformation.toCheck(): AppUpdateCheck {
        val release = toRelease() ?: return AppUpdateCheck.EmptyChannel
        val current = appVersionProvider.current().code
        return when {
            release.versionCode > current && release.downloadUrl.isNotBlank() -> AppUpdateCheck.Available(release)
            release.versionCode < current -> AppUpdateCheck.Older(release)
            else -> AppUpdateCheck.UpToDate
        }
    }

    private fun AppUpdatesChannel.toDomain(latest: SpecialistResponse<UpdateVersionInformation>?) = AppUpdateChannel(
        id = id,
        name = name.trim(),
        isDefault = accessLevel == UpdateChannelAccessLevel.PUBLIC,
        latest = (latest as? SpecialistResponse.Success)?.value?.toRelease(),
        isEmpty = latest?.isEmptyChannel() == true,
    )

    private fun SpecialistResponse<*>.isEmptyChannel(): Boolean =
        this is SpecialistResponse.Error && exception is UpdateVersionNotFoundException

    private fun UpdateVersionInformation.toRelease(): AppUpdateRelease? {
        val name = version.trim()
        if (name.isBlank()) return null
        return AppUpdateRelease(
            version = name,
            versionCode = versionInt,
            notes = description.trim(),
            publishedAt = publishedDate.date,
            downloadUrl = downloadUrl.trim(),
        )
    }
}
