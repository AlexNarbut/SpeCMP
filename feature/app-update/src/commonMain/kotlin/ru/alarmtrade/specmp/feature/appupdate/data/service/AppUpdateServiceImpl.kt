package ru.alarmtrade.specmp.feature.appupdate.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheckState
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateDownload
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateFailure
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateInstallResult
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePackage
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateProcess
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateRelease
import ru.alarmtrade.specmp.feature.appupdate.domain.repository.AppUpdateRepository
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateService

internal class AppUpdateServiceImpl(
    private val repository: AppUpdateRepository,
    private val installer: AppUpdateInstaller,
    private val scope: CoroutineScope,
) : AppUpdateService {

    private val state = MutableStateFlow(AppUpdateProcess(platform = installer.platform))
    private val checkJob = MutableStateFlow<Job?>(null)
    private val downloadJob = MutableStateFlow<Job?>(null)

    override val process: StateFlow<AppUpdateProcess> = state.asStateFlow()

    override fun check(withChannelVersions: Boolean) {
        if (state.value.isTransferring) return
        replace(checkJob) { load(withChannelVersions) }
    }

    override fun ensureChecked(withChannelVersions: Boolean) {
        val check = state.value.check
        if (check is AppUpdateCheckState.Idle || check is AppUpdateCheckState.Failed) check(withChannelVersions)
    }

    override fun selectChannel(id: Int) {
        if (state.value.isTransferring || state.value.overview?.selectedId == id) return
        replace(checkJob) {
            state.update { it.copy(check = AppUpdateCheckState.Checking) }
            when (val selected = repository.selectChannel(id)) {
                is Response.Success -> load(withChannelVersions = true)

                is Response.Error -> state.update {
                    it.copy(check = AppUpdateCheckState.Failed(selected.exception.appError))
                }
            }
        }
    }

    override fun download(anyFile: Boolean) {
        val release = state.value.available ?: return
        if (state.value.isTransferring) return
        if (!anyFile && !installer.accepts(release.fileName)) {
            state.update { it.copy(download = AppUpdateDownload.Failed(AppUpdateFailure.WrongFile)) }
            return
        }
        replace(downloadJob) {
            state.update { it.copy(download = AppUpdateDownload.InProgress(release, bytes = 0)) }
            val result = receive(release)
            state.update { it.copy(download = result) }
        }
    }

    override fun cancelDownload() {
        downloadJob.value?.cancel()
        state.update {
            if (it.download is AppUpdateDownload.InProgress) it.copy(download = AppUpdateDownload.Idle) else it
        }
    }

    override fun install() {
        val current = state.value
        val target = current.download.downloaded ?: return
        val release = (current.download as? AppUpdateDownload.Ready)?.release ?: current.available ?: return
        replace(downloadJob) {
            state.update { it.copy(download = AppUpdateDownload.Installing(release)) }
            val failure = when (val result = installer.install(target)) {
                is Response.Success -> when (result.value) {
                    AppUpdateInstallResult.WRONG_FILE -> AppUpdateFailure.WrongFile
                    AppUpdateInstallResult.FAILED -> AppUpdateFailure.Install
                }

                is Response.Error -> AppUpdateFailure.Install
            }
            state.update { it.copy(download = AppUpdateDownload.Failed(failure, target)) }
        }
    }

    override suspend fun saveCopy(): Response<Unit> {
        if (state.value.download.downloaded == null) {
            download(anyFile = true)
            downloadJob.value?.join()
        }
        val target = state.value.download.downloaded
            ?: return Response.Error(IllegalStateException("Nothing downloaded"))
        return installer.export(target)
    }

    private suspend fun load(withChannelVersions: Boolean) {
        state.update { it.copy(check = AppUpdateCheckState.Checking) }
        val response = repository.overview(withChannelVersions)
        state.update { process ->
            val check = when (response) {
                is Response.Success -> AppUpdateCheckState.Loaded(response.value)
                is Response.Error -> AppUpdateCheckState.Failed(response.exception.appError)
            }
            val updated = process.copy(check = check)
            // Скачанное остаётся, пока в канале та же версия; ошибка прошлой загрузки после проверки не нужна.
            val keep = (process.download as? AppUpdateDownload.Ready)?.release?.version == updated.available?.version
            if (process.isTransferring || keep) updated else updated.copy(download = AppUpdateDownload.Idle)
        }
    }

    private suspend fun receive(release: AppUpdateRelease): AppUpdateDownload {
        val target = when (val prepared = installer.prepare(release.fileName)) {
            is Response.Success -> prepared.value
            is Response.Error -> return AppUpdateDownload.Failed(AppUpdateFailure.FileWrite)
        }
        var bytes = 0L
        var reported = 0L
        val failure = repository.download(release.downloadUrl).mapNotNull { response ->
            when (response) {
                is Response.Error -> AppUpdateFailure.Transfer(response.exception.appError)

                is Response.Success -> when (installer.append(target, response.value)) {
                    is Response.Error -> AppUpdateFailure.FileWrite

                    is Response.Success -> {
                        bytes += response.value.size
                        if (bytes - reported >= PROGRESS_STEP_BYTES) {
                            reported = bytes
                            state.update { it.copy(download = AppUpdateDownload.InProgress(release, bytes)) }
                        }
                        null
                    }
                }
            }
        }.firstOrNull()
        return when {
            failure != null -> AppUpdateDownload.Failed(failure)
            bytes == 0L -> AppUpdateDownload.Failed(AppUpdateFailure.Transfer(null))
            else -> AppUpdateDownload.Ready(release, target)
        }
    }

    private fun replace(slot: MutableStateFlow<Job?>, block: suspend CoroutineScope.() -> Unit) {
        val job = scope.launch(start = CoroutineStart.LAZY, block = block)
        slot.getAndUpdate { job }?.cancel()
        job.start()
    }

    private val AppUpdateProcess.isTransferring: Boolean
        get() = download is AppUpdateDownload.InProgress || download is AppUpdateDownload.Installing

    private val AppUpdateDownload.downloaded: AppUpdatePackage?
        get() = when (this) {
            is AppUpdateDownload.Ready -> target
            is AppUpdateDownload.Failed -> target
            else -> null
        }

    private companion object {
        const val PROGRESS_STEP_BYTES = 256L * 1024
    }
}
