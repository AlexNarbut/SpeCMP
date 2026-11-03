package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

class FirmwareSystemsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeFirmwareRepository()
    private val viewModels = mutableListOf<FirmwareSystemsViewModel>()

    private class FakePreferences(var order: SystemsOrder = SystemsOrder.Added) : FirmwareArchivePreferences {
        override fun systemsOrder() = order

        override fun setSystemsOrder(order: SystemsOrder) {
            this.order = order
        }
    }

    private val preferences = FakePreferences()

    private val main = FirmwareSubsystem(10, "Основная программа", "DX4G", FirmwareModuleKind.AlarmMain)
    private val bluetooth = FirmwareSubsystem(11, "Bluetooth-модуль", "BTM2", FirmwareModuleKind.BluetoothModule)
    private val tag = FirmwareSubsystem(20, "Метка BT-760", "BT760", FirmwareModuleKind.Unknown)
    private val dx = FirmwareSystem(1, "Pandora DX-4G", listOf(main, bluetooth))
    private val bt760 = FirmwareSystem(2, "BT-760", listOf(tag))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.viewModel(): FirmwareSystemsViewModel {
        val vm = FirmwareSystemsViewModel(repository, preferences, AppDispatchers(dispatcher, dispatcher, dispatcher))
            .also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        return vm
    }

    private fun FirmwareSystemsViewModel.rows(): List<SystemRowUi> = (state.value.content as LCEState.Content).content

    @Test
    fun `systems keep the database order (latest first), collapsed, and expand on tap`() = test {
        repository.systemsResult = Response.Success(listOf(dx, bt760))
        val vm = viewModel()

        assertEquals(SystemsOrder.Added, vm.state.value.order)
        assertEquals(listOf("Pandora DX-4G", "BT-760"), vm.rows().map { it.system.name })
        assertEquals(listOf(false, false), vm.rows().map { it.expanded })

        vm.onToggle(dx.id)
        runCurrent()
        assertEquals(true, vm.rows().first { it.system.id == dx.id }.expanded)
    }

    @Test
    fun `search by subsystem shows only matching subsystems expanded, by system shows all of them`() = test {
        repository.systemsResult = Response.Success(listOf(dx, bt760))
        val vm = viewModel()

        vm.onQueryChange("btm2")
        runCurrent()
        assertEquals(listOf(SystemRowUi(dx, listOf(bluetooth), expanded = true)), vm.rows())

        vm.onQueryChange("dx-4g")
        runCurrent()
        assertEquals(listOf(SystemRowUi(dx, listOf(main, bluetooth), expanded = true)), vm.rows())

        vm.onQueryChange("нет такого")
        runCurrent()
        assertEquals(emptyList(), vm.rows())
    }

    @Test
    fun `alphabetical order is applied at once and remembered`() = test {
        repository.systemsResult = Response.Success(listOf(dx, bt760))
        val vm = viewModel()

        vm.onOrderSelect(SystemsOrder.Alphabet)
        runCurrent()
        assertEquals(listOf("BT-760", "Pandora DX-4G"), vm.rows().map { it.system.name })
        assertEquals(SystemsOrder.Alphabet, preferences.order)

        // Следующий вход в архив — сразу по алфавиту.
        val next = viewModel()
        assertEquals(SystemsOrder.Alphabet, next.state.value.order)
        assertEquals(listOf("BT-760", "Pandora DX-4G"), next.rows().map { it.system.name })
    }

    @Test
    fun `read failure is an error and retry reloads`() = test {
        repository.systemsResult = Response.Error(IllegalStateException("db"))
        val vm = viewModel()
        assertIs<LCEState.Error<*>>(vm.state.value.content)

        repository.systemsResult = Response.Success(listOf(bt760))
        vm.onRetry()
        runCurrent()
        assertEquals(1, vm.rows().size)
    }

    @Test
    fun `picked subsystem opens its archive, can filter only for the alarm main program`() {
        val alarm = archiveRequestOf(dx, main)
        assertEquals(10L, alarm.moduleId)
        assertEquals(true, alarm.noUnit)
        assertEquals(true, alarm.canFilterAllowed)
        assertEquals("DX4G", alarm.moduleName)
        assertEquals("Pandora DX-4G", alarm.deviceName)

        val bt = archiveRequestOf(dx, bluetooth)
        assertEquals(UpdateProfile.Bluetooth, bt.profile)
        assertEquals(false, bt.canFilterAllowed)
        assertEquals(false, archiveRequestOf(bt760, tag).canFilterAllowed)
    }
}
