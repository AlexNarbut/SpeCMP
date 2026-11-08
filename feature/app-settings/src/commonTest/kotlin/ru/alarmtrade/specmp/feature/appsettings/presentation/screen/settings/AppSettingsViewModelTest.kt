package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.AppRestarter
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.common.ConfigurationLanguageStore
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore

class AppSettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val store = object : AppLocaleStore {
        var saved = AppLocale.English
        override fun current(): AppLocale = saved
        override fun save(locale: AppLocale) {
            saved = locale
        }
    }

    private val configurationLanguages = object : ConfigurationLanguageStore {
        var saved: ConfigurationLanguage? = ConfigurationLanguage.RU
        var saveResponse: Response<Unit> = Response.Success(Unit)
        override suspend fun current(): ConfigurationLanguage? = saved
        override suspend fun save(language: ConfigurationLanguage): Response<Unit> {
            if (saveResponse is Response.Success) saved = language
            return saveResponse
        }
    }
    private var restarts = 0

    // Экран подписан на state — с этого стартует чтение языка конфигураций (launchOnFirstStart).
    private fun TestScope.viewModel(restartSupported: Boolean = true) = AppSettingsViewModel(
        store,
        AppRestarter {
            restarts++
            restartSupported
        },
        configurationLanguages,
        AppDispatchers(dispatcher, dispatcher, dispatcher),
    ).also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `apply without a change does nothing`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onApply()

        assertFalse(vm.state.value.canApply)
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `confirmed change saves the language and restarts the app`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onLocaleSelect(AppLocale.Russian)
        vm.onApply()
        assertEquals(AppSettingsDialog.CONFIRM, vm.state.value.dialog)
        vm.onConfirm()

        assertEquals(AppLocale.Russian, store.saved)
        assertEquals(1, restarts)
    }

    @Test
    fun `without a platform restart the user is asked to restart manually`() = runTest(dispatcher) {
        val vm = viewModel(restartSupported = false)

        vm.onLocaleSelect(AppLocale.Russian)
        vm.onApply()
        vm.onConfirm()

        assertEquals(AppLocale.Russian, store.saved)
        assertEquals(AppSettingsDialog.RESTART_MANUALLY, vm.state.value.dialog)
        assertFalse(vm.state.value.canApply)
    }

    @Test
    fun `cancel keeps the saved language`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onLocaleSelect(AppLocale.Russian)
        vm.onApply()
        vm.onDialogDismiss()

        assertEquals(AppLocale.English, store.saved)
        assertEquals(0, restarts)
    }

    @Test
    fun `configuration language is read on start and saved right away without a restart`() = runTest(dispatcher) {
        val vm = viewModel()
        assertEquals(ConfigurationLanguage.RU, vm.state.value.configurationLanguage)

        vm.onConfigurationLanguageSelect(ConfigurationLanguage.EN)

        assertEquals(ConfigurationLanguage.EN, configurationLanguages.saved)
        assertEquals(ConfigurationLanguage.EN, vm.state.value.configurationLanguage)
        assertEquals(0, restarts)
    }

    @Test
    fun `failed configuration language save rolls back with an error`() = runTest(dispatcher) {
        configurationLanguages.saveResponse = Response.Error(IllegalStateException())
        val vm = viewModel()

        vm.onConfigurationLanguageSelect(ConfigurationLanguage.EN)

        assertEquals(ConfigurationLanguage.RU, vm.state.value.configurationLanguage)
        assertTrue(vm.state.value.configurationLanguageError)

        configurationLanguages.saveResponse = Response.Success(Unit)
        vm.onConfigurationLanguageSelect(ConfigurationLanguage.EN)
        assertEquals(ConfigurationLanguage.EN, vm.state.value.configurationLanguage)
        assertFalse(vm.state.value.configurationLanguageError)
    }
}
