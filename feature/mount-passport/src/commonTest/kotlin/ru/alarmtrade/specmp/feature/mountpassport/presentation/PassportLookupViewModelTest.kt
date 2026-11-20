package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.barcode.BarcodeScannerAccess
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.device.DeviceIdQueryError
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitLookup
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupEffect
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.UnitLookupStatus

@OptIn(ExperimentalCoroutinesApi::class)
class PassportLookupViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val reading = FakePassportReadingRepository()
    private val installers = FakePassportInstallerRepository()
    private val permissions = FakePermissions()
    private val viewModels = mutableListOf<PassportLookupViewModel>()

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

    private fun TestScope.viewModel(): Pair<PassportLookupViewModel, MutableList<PassportLookupEffect>> {
        val vm = PassportLookupViewModel(
            reading = reading,
            installers = installers,
            permissions = permissions,
            dispatchers = testDispatchers(dispatcher),
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<PassportLookupEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun PassportLookupViewModel.find(id: String) {
        onIdChange(id)
        onFind()
    }

    @Test
    fun emptyIdIsErrorWithoutRequest(): TestResult = test {
        val (vm, _) = viewModel()

        vm.find("  ")

        assertEquals(DeviceIdQueryError.EMPTY, vm.state.value.idError)
        assertTrue(reading.lookups.isEmpty())
    }

    @Test
    fun lettersAreNotDigits(): TestResult = test {
        val (vm, _) = viewModel()

        vm.find("12A4567")

        assertEquals(DeviceIdQueryError.NOT_DIGITS, vm.state.value.idError)
        assertTrue(reading.lookups.isEmpty())
    }

    @Test
    fun typingClearsErrorAndResult(): TestResult = test {
        reading.lookup = Response.Success(PassportUnitLookup.Found("Pandora DX-91", "1234567"))
        val (vm, _) = viewModel()
        vm.find("")
        vm.find("1234567")

        vm.onIdChange("12345678")

        assertNull(vm.state.value.idError)
        assertEquals(UnitLookupStatus.Idle, vm.state.value.lookup)
        assertFalse(vm.state.value.canProceed)
    }

    @Test
    fun foundUnitCreatesPassportWithServerModel(): TestResult = test {
        reading.lookup = Response.Success(PassportUnitLookup.Found("Pandora DX-91", "1234567"))
        val (vm, effects) = viewModel()

        vm.find(" 1234-567 ")
        assertEquals(listOf("1234567"), reading.lookups)
        assertEquals(UnitLookupStatus.Found("Pandora DX-91", "1234567"), vm.state.value.lookup)
        assertTrue(vm.state.value.canProceed)
        assertEquals(0, reading.modelCalls)

        vm.onNext()

        val (model, id, installer) = reading.creations.single()
        assertEquals("Pandora DX-91", model)
        assertEquals("1234567", id)
        assertEquals("Алексей Смирнов", installer.name)
        assertEquals(listOf<PassportLookupEffect>(PassportLookupEffect.Created("p1")), effects)
    }

    @Test
    fun notFoundAsksModelFromList(): TestResult = test {
        reading.lookup = Response.Success(PassportUnitLookup.NotFound)
        val (vm, _) = viewModel()

        vm.find("7654321")
        assertEquals(UnitLookupStatus.NotFound, vm.state.value.lookup)
        assertEquals<List<String>?>(listOf("Pandora DX-4GS", "Pandora DX-91"), vm.state.value.models.content)
        assertFalse(vm.state.value.canProceed)

        vm.onModelSelect("Pandora DX-4GS")
        vm.onNext()

        assertEquals("Pandora DX-4GS" to "7654321", reading.creations.single().let { it.first to it.second })
    }

    @Test
    fun offlineAllowsTypedModel(): TestResult = test {
        reading.lookup = Response.Success(PassportUnitLookup.Offline)
        val (vm, _) = viewModel()

        vm.find("1234567")
        vm.onOtherModel()
        assertFalse(vm.state.value.canProceed)
        vm.onCustomModelChange("  Pandora DX-90 ")
        vm.onNext()

        assertEquals(UnitLookupStatus.Offline, vm.state.value.lookup)
        assertEquals("Pandora DX-90", reading.creations.single().first)
    }

    @Test
    fun lookupErrorFallsBackToManual(): TestResult = test {
        reading.lookup = Response.Error(IllegalStateException("broken"))
        val (vm, _) = viewModel()

        vm.find("1234567")

        assertIs<UnitLookupStatus.Failed>(vm.state.value.lookup)
        assertTrue(vm.state.value.manual)
    }

    @Test
    fun modelsAreLoadedOnce(): TestResult = test {
        val (vm, _) = viewModel()

        vm.find("1")
        vm.find("2")

        assertEquals(1, reading.modelCalls)
    }

    @Test
    fun createErrorKeepsScreen(): TestResult = test {
        reading.lookup = Response.Success(PassportUnitLookup.Found("Pandora DX-91", "1234567"))
        reading.created = Response.Error(IllegalStateException("disk"))
        val (vm, effects) = viewModel()

        vm.find("1234567")
        vm.onNext()

        assertEquals(listOf<PassportLookupEffect>(PassportLookupEffect.CreateFailed(PassportError.Unknown)), effects)
        assertFalse(vm.state.value.creating)
        assertTrue(vm.state.value.canProceed)
    }

    @Test
    fun scannedNumberIsSearched(): TestResult = test {
        val (vm, _) = viewModel()

        vm.onScanClick()
        assertEquals(BarcodeScannerAccess.GRANTED, vm.state.value.scanner)
        vm.onScanned("4607123456789")

        assertNull(vm.state.value.scanner)
        assertEquals("4607123456789", vm.state.value.unitId)
        assertEquals(listOf("4607123456789"), reading.lookups)
    }

    @Test
    fun deniedCameraIsBlockedWithSettings(): TestResult = test {
        permissions.camera = PermissionResult.Denied(canOpenSettings = true)
        val (vm, effects) = viewModel()

        vm.onScanClick()
        assertEquals(BarcodeScannerAccess.BLOCKED, vm.state.value.scanner)
        vm.onScanManual()

        assertNull(vm.state.value.scanner)
        assertEquals(listOf<PassportLookupEffect>(PassportLookupEffect.FocusId), effects)
    }
}
