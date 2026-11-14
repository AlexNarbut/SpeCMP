package ru.alarmtrade.specmp.feature.appupdate.presentation

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppVersion
import ru.alarmtrade.specmp.feature.appupdate.data.service.AppUpdateServiceImpl
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateStage
import ru.alarmtrade.specmp.feature.appupdate.presentation.navigation.AppUpdateEntryPoint
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateArgs
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateDialog
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateEffect
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateViewModel
import ru.alarmtrade.specmp.feature.appupdate.support.FakeAppUpdateRepository
import ru.alarmtrade.specmp.feature.appupdate.support.FakeInstaller

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeAppUpdateRepository()
    private val effects = mutableListOf<AppUpdateEffect>()
    private var deviceBusy = false

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(
        installer: FakeInstaller = FakeInstaller(),
        from: AppUpdateEntryPoint = AppUpdateEntryPoint.MENU,
    ): AppUpdateViewModel {
        val service = AppUpdateServiceImpl(repository, installer, backgroundScope)
        val vm = AppUpdateViewModel(
            AppUpdateArgs(from),
            service,
            installer,
            { deviceBusy },
            { AppVersion("2.1.0", 249) },
        )
        backgroundScope.launch { vm.state.collect {} }
        backgroundScope.launch { vm.sideEffect.collect { effects += it } }
        return vm
    }

    @Test
    fun menuChecksWithChannelVersionsOnDesktop() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(listOf(true), repository.overviewRequests)
        assertIs<AppUpdateStage.Available>(vm.state.value.stage)
        assertEquals(true, vm.state.value.showChannels)
    }

    @Test
    fun mobileOpensStore() = runTest(dispatcher) {
        val vm = viewModel(FakeInstaller(platform = AppUpdatePlatform.ANDROID))
        advanceUntilIdle()

        vm.onUpdate()

        assertEquals(false, vm.state.value.showChannels)
        assertIs<AppUpdateEffect.OpenStorePage>(effects.single())
    }

    @Test
    fun busyDeviceAsksBeforeInstall() = runTest(dispatcher) {
        deviceBusy = true
        val vm = viewModel()
        advanceUntilIdle()
        vm.onUpdate()
        advanceUntilIdle()

        vm.onInstall()

        assertEquals(AppUpdateDialog.DeviceBusy, vm.state.value.dialog)
        vm.onDismissDialog()
        assertNull(vm.state.value.dialog)
        assertIs<AppUpdateStage.Downloaded>(vm.state.value.stage)
    }

    @Test
    fun betaChannelAsksAndDefaultSwitchesAtOnce() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onSelectChannel(2)
        assertIs<AppUpdateDialog.SwitchChannel>(vm.state.value.dialog)
        vm.onConfirmDialog()
        advanceUntilIdle()
        assertEquals(listOf(2), repository.selected)

        vm.onSelectChannel(1)
        advanceUntilIdle()
        assertEquals(listOf(2, 1), repository.selected)
    }

    @Test
    fun saveFileDownloadsThenExports() = runTest(dispatcher) {
        val installer = FakeInstaller(accepts = false)
        val vm = viewModel(installer, from = AppUpdateEntryPoint.PROMPT)
        advanceUntilIdle()

        vm.onSave()
        advanceUntilIdle()

        assertEquals(1, installer.exports)
        assertEquals(listOf<AppUpdateEffect>(AppUpdateEffect.Saved), effects)
        assertEquals(true, vm.state.value.showLater)
    }
}
