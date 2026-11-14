package ru.alarmtrade.specmp.feature.appupdate.data.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheck
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateDownload
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateFailure
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateInstallResult
import ru.alarmtrade.specmp.feature.appupdate.support.FakeAppUpdateRepository
import ru.alarmtrade.specmp.feature.appupdate.support.FakeInstaller
import ru.alarmtrade.specmp.feature.appupdate.support.TEST_PACKAGE

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateServiceImplTest {

    private val repository = FakeAppUpdateRepository()

    private fun TestScope.service(installer: FakeInstaller = FakeInstaller()) =
        AppUpdateServiceImpl(repository, installer, backgroundScope)

    @Test
    fun downloadWritesChunksAndIsReady() = runTest(UnconfinedTestDispatcher()) {
        val installer = FakeInstaller()
        val service = service(installer)
        service.check(withChannelVersions = true)

        service.download(anyFile = false)

        assertIs<AppUpdateDownload.Ready>(service.process.value.download)
        assertEquals(3, installer.written)
    }

    @Test
    fun foreignInstallerIsNotDownloadedUnlessSavedAsFile() = runTest(UnconfinedTestDispatcher()) {
        val service = service(FakeInstaller(accepts = false))
        service.check(withChannelVersions = true)

        service.download(anyFile = false)
        assertEquals(AppUpdateDownload.Failed(AppUpdateFailure.WrongFile), service.process.value.download)
        assertEquals(0, repository.downloads)

        service.download(anyFile = true)
        assertIs<AppUpdateDownload.Ready>(service.process.value.download)
    }

    @Test
    fun failedInstallKeepsFile() = runTest(UnconfinedTestDispatcher()) {
        val service = service(FakeInstaller(installResult = AppUpdateInstallResult.FAILED))
        service.check(withChannelVersions = true)
        service.download(anyFile = false)

        service.install()

        assertEquals(AppUpdateDownload.Failed(AppUpdateFailure.Install, TEST_PACKAGE), service.process.value.download)
    }

    @Test
    fun recheckKeepsDownloadedSameVersionAndChannelSwitchRechecks() = runTest(UnconfinedTestDispatcher()) {
        val service = service()
        service.check(withChannelVersions = true)
        service.download(anyFile = false)

        service.check(withChannelVersions = true)
        assertIs<AppUpdateDownload.Ready>(service.process.value.download)

        repository.check = AppUpdateCheck.UpToDate
        service.selectChannel(2)
        assertEquals(listOf(2), repository.selected)
        assertEquals(AppUpdateCheck.UpToDate, service.process.value.overview?.check)
        assertEquals(AppUpdateDownload.Idle, service.process.value.download)
    }
}
