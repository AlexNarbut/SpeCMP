package ru.alarmtrade.specmp.appupdate

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateInstallResult
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePackage
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller

/** Android и iOS: обновление только через магазин, загрузки и установки нет. */
internal class StoreAppUpdateInstaller(
    override val platform: AppUpdatePlatform,
    override val storeUrls: List<String>,
) : AppUpdateInstaller {
    override fun accepts(fileName: String): Boolean = false

    override suspend fun prepare(fileName: String): Response<AppUpdatePackage> = unsupported()

    override suspend fun append(target: AppUpdatePackage, chunk: ByteArray): Response<Unit> = unsupported()

    override suspend fun install(target: AppUpdatePackage): Response<AppUpdateInstallResult> = unsupported()

    override suspend fun export(target: AppUpdatePackage): Response<Unit> = unsupported()

    private fun <T> unsupported(): Response<T> = Response.Error(UnsupportedOperationException("Store-only platform"))
}
