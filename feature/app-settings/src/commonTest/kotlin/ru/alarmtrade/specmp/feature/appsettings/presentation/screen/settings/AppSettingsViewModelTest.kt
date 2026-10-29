package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppRestarter
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore

class AppSettingsViewModelTest {

    private val store = object : AppLocaleStore {
        var saved = AppLocale.English
        override fun current(): AppLocale = saved
        override fun save(locale: AppLocale) {
            saved = locale
        }
    }
    private var restarts = 0

    private fun viewModel(restartSupported: Boolean) = AppSettingsViewModel(
        store,
        AppRestarter {
            restarts++
            restartSupported
        }
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `apply without a change does nothing`() {
        val vm = viewModel(restartSupported = true)

        vm.onApply()

        assertFalse(vm.state.value.canApply)
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `confirmed change saves the language and restarts the app`() {
        val vm = viewModel(restartSupported = true)

        vm.onLocaleSelect(AppLocale.Russian)
        vm.onApply()
        assertEquals(AppSettingsDialog.CONFIRM, vm.state.value.dialog)
        vm.onConfirm()

        assertEquals(AppLocale.Russian, store.saved)
        assertEquals(1, restarts)
    }

    @Test
    fun `without a platform restart the user is asked to restart manually`() {
        val vm = viewModel(restartSupported = false)

        vm.onLocaleSelect(AppLocale.Russian)
        vm.onApply()
        vm.onConfirm()

        assertEquals(AppLocale.Russian, store.saved)
        assertEquals(AppSettingsDialog.RESTART_MANUALLY, vm.state.value.dialog)
        assertFalse(vm.state.value.canApply)
    }

    @Test
    fun `cancel keeps the saved language`() {
        val vm = viewModel(restartSupported = true)

        vm.onLocaleSelect(AppLocale.Russian)
        vm.onApply()
        vm.onDialogDismiss()

        assertEquals(AppLocale.English, store.saved)
        assertEquals(0, restarts)
    }
}
