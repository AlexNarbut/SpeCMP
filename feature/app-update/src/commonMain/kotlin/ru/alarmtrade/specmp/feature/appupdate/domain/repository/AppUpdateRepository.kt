package ru.alarmtrade.specmp.feature.appupdate.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateOverview

interface AppUpdateRepository {
    /** Выбранный канал и его последняя версия против установленной; [withChannelVersions] — версии всех каналов. */
    suspend fun overview(withChannelVersions: Boolean): Response<AppUpdateOverview>

    suspend fun selectChannel(id: Int): Response<Unit>

    fun download(url: String): Flow<Response<ByteArray>>
}
