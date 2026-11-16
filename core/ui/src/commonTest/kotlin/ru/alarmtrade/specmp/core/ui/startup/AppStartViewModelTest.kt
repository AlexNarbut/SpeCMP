package ru.alarmtrade.specmp.core.ui.startup

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppServicesStarter
import ru.alarmtrade.specmp.core.common.AppStartup
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore

class AppStartViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val servicesStarted = mutableListOf<String>()
    private val services = AppServicesStarter(listOf("deviceSession" to { servicesStarted += "deviceSession" }))

    private val startup = object : AppStartup {
        val readyFlow = MutableStateFlow(false)
        val languages = mutableListOf<String>()
        override val ready: StateFlow<Boolean> = readyFlow

        override fun start(languageTag: String) {
            languages += languageTag
        }
    }

    private val locales = object : AppLocaleStore {
        override fun current(): AppLocale = AppLocale.Russian

        override fun save(locale: AppLocale) = Unit
    }

    private fun TestScope.viewModel() = AppStartViewModel(services, startup, locales)
        .also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts app services and SDK setup in the app language`() = runTest(dispatcher) {
        viewModel()

        assertEquals(listOf("deviceSession"), servicesStarted)
        assertEquals(listOf(AppLocale.Russian.languageTag), startup.languages)
    }

    @Test
    fun `UI waits until the setup is ready`() = runTest(dispatcher) {
        val vm = viewModel()
        assertFalse(vm.state.value.isReady)

        startup.readyFlow.update { true }

        assertTrue(vm.state.value.isReady)
    }
}
