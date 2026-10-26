package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
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
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

class FirmwareArchiveViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeFirmwareRepository()
    private val viewModels = mutableListOf<FirmwareArchiveViewModel>()

    private fun version(value: String, description: String = "") =
        FirmwareVersion(0, value, LocalDate(2026, 10, 14), description, "https://fw/$value")

    private fun page(number: Int, vararg versions: String) =
        Response.Success(FirmwareVersionPage(versions.map { version(it) }, number))

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

    private fun TestScope.viewModel(
        installed: String? = "3.57",
        request: FirmwareArchiveRequest =
            FirmwareArchiveRequest(1, FirmwareChannel.RELEASE, installed, "DX4G  77", UpdateProfile.Main)
    ): Pair<FirmwareArchiveViewModel, MutableList<FirmwareArchiveEffect>> {
        val vm = FirmwareArchiveViewModel(
            request,
            repository,
            AppDispatchers(dispatcher, dispatcher, dispatcher)
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<FirmwareArchiveEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun List<FirmwareArchiveEffect>.picked(): List<String> =
        filterIsInstance<FirmwareArchiveEffect.Picked>().map { it.version.version }

    private fun FirmwareArchiveViewModel.rows(): List<ArchiveRowUi> = (state.value.content as ArchiveContent.Ready).rows

    @Test
    fun `first page is shown newest first with marks relative to the installed version`() = test {
        repository.archivePages[1] = page(1, "3.55", "3.61", "3.57")
        val (vm, _) = viewModel()

        val rows = vm.rows()
        assertEquals(listOf("3.61", "3.57", "3.55"), rows.map { it.version })
        assertEquals(
            listOf(ArchiveRowKind.New, ArchiveRowKind.Installed, ArchiveRowKind.Older),
            rows.map { it.kind }
        )
        // Выбрана верхняя (самая новая) версия.
        assertEquals("3.61", vm.state.value.selected)
        assertEquals("DX4G  77", vm.state.value.deviceTitle)
    }

    @Test
    fun `versions are ordered by number not by text`() = test {
        repository.archivePages[1] = page(1, "3.9", "3.10", "3.2")
        val (vm, _) = viewModel(installed = null)

        assertEquals(listOf("3.10", "3.9", "3.2"), vm.rows().map { it.version })
        // Установленная версия неизвестна — все версии «новые» и выбираются сразу.
        assertEquals(setOf(ArchiveRowKind.New), vm.rows().map { it.kind }.toSet())
    }

    @Test
    fun `next page is loaded at the end, duplicates are dropped and a page without news ends the list`() = test {
        repository.archivePages[1] = page(1, "3.61", "3.60")
        repository.archivePages[2] = page(2, "3.60", "3.58")
        repository.archivePages[3] = page(3, "3.58", "3.60")
        val (vm, _) = viewModel()

        vm.onEndReached()
        runCurrent()
        assertEquals(listOf("3.61", "3.60", "3.58"), vm.rows().map { it.version })

        vm.onEndReached()
        runCurrent()
        assertEquals(listOf(1, 2, 3), repository.archiveCalls)

        // Страница 3 ничего нового не принесла — это конец, дальше не запрашиваем.
        vm.onEndReached()
        runCurrent()
        assertEquals(listOf(1, 2, 3), repository.archiveCalls)
    }

    @Test
    fun `loading more is not repeated while it runs and not after an error`() = test {
        repository.archivePages[1] = page(1, "3.61")
        val (vm, _) = viewModel()

        vm.onEndReached()
        vm.onEndReached()
        runCurrent()
        assertEquals(listOf(1, 2), repository.archiveCalls)

        // Страница 2 — ошибка: список остаётся, баннер с повтором, новые запросы не идут сами.
        val ready = vm.state.value.content as ArchiveContent.Ready
        assertEquals(true, ready.moreError)
        assertEquals(false, ready.loadingMore)
        assertEquals(listOf("3.61"), ready.rows.map { it.version })
        vm.onEndReached()
        runCurrent()
        assertEquals(listOf(1, 2), repository.archiveCalls)

        repository.archivePages[2] = page(2, "3.60")
        vm.onRetryMore()
        runCurrent()
        assertEquals(listOf("3.61", "3.60"), vm.rows().map { it.version })
        assertEquals(false, (vm.state.value.content as ArchiveContent.Ready).moreError)
    }

    @Test
    fun `first page failure is a full screen error and refresh loads again`() = test {
        val (vm, _) = viewModel()
        assertEquals(ArchiveContent.Error, vm.state.value.content)

        repository.archivePages[1] = page(1, "3.61")
        vm.onRefresh()
        runCurrent()

        assertIs<ArchiveContent.Ready>(vm.state.value.content)
    }

    @Test
    fun `empty answer is the empty state`() = test {
        repository.archivePages[1] = page(1)
        val (vm, _) = viewModel()

        assertEquals(ArchiveContent.Empty, vm.state.value.content)
    }

    @Test
    fun `newer and installed versions are picked at once and close the archive`() = test {
        repository.archivePages[1] = page(1, "3.61", "3.57", "3.55")
        val (vm, effects) = viewModel()

        vm.onPick("3.61")
        runCurrent()
        assertEquals(listOf("3.61"), effects.picked())

        // Переустановка текущей версии — без предупреждения.
        vm.onPick("3.57")
        runCurrent()
        assertEquals(listOf("3.61", "3.57"), effects.picked())
        assertNull(vm.state.value.rollback)
    }

    @Test
    fun `older version asks for confirmation before it is picked`() = test {
        repository.archivePages[1] = page(1, "3.61", "3.57", "3.55")
        val (vm, effects) = viewModel()

        vm.onPick("3.55")
        runCurrent()
        assertEquals(RollbackUi("3.55", "3.57"), vm.state.value.rollback)
        assertEquals(emptyList(), effects)

        vm.onRollbackDismiss()
        runCurrent()
        assertNull(vm.state.value.rollback)
        assertEquals(emptyList(), effects)

        vm.onPick("3.55")
        vm.onRollbackConfirm()
        runCurrent()
        assertEquals(listOf("3.55"), effects.picked())
        assertNull(vm.state.value.rollback)
    }

    @Test
    fun `selecting a row only highlights it`() = test {
        repository.archivePages[1] = page(1, "3.61", "3.57")
        val (vm, effects) = viewModel()

        vm.onSelectRow("3.57")
        runCurrent()

        assertEquals("3.57", vm.state.value.selected)
        assertEquals(emptyList(), effects)
    }

    // ───── CAN-фильтр ─────

    @Test
    fun `can filter reloads the list on a full code and on clearing, not on a partial code`() = test {
        repository.archivePages[1] = page(1, "3.61")
        val (vm, _) = viewModel()
        repository.archiveRequests.clear()

        vm.onCanInput("12")
        runCurrent()
        assertEquals(emptyList<Triple<Long, Int, String?>>(), repository.archiveRequests)
        assertEquals("12", vm.state.value.canFilter?.input)
        assertNull(vm.state.value.canFilter?.applied)

        vm.onCanInput("12a34")
        runCurrent()
        assertEquals(listOf<Triple<Long, Int, String?>>(Triple(1L, 1, "1234")), repository.archiveRequests)
        assertEquals("1234", vm.state.value.canFilter?.applied)

        vm.onCanInput("")
        runCurrent()
        assertEquals(Triple(1L, 1, null), repository.archiveRequests.last())
        assertNull(vm.state.value.canFilter?.applied)
    }

    @Test
    fun `vehicle from the catalog applies its can code and is shown under the field`() = test {
        repository.archivePages[1] = page(1, "3.61")
        val (vm, _) = viewModel()

        vm.onVehiclePickerOpen()
        runCurrent()
        assertEquals(true, vm.state.value.vehiclePicker)

        vm.onVehiclePicked("0213", "Toyota Camry XV70")
        runCurrent()
        val filter = vm.state.value.canFilter
        assertEquals("0213", filter?.applied)
        assertEquals("Toyota Camry XV70", filter?.carTitle)
        assertEquals(false, vm.state.value.vehiclePicker)
        assertEquals(Triple(1L, 1, "0213"), repository.archiveRequests.last())

        // Ручная правка кода — автомобиль больше не подписан.
        vm.onCanInput("021")
        runCurrent()
        assertNull(vm.state.value.canFilter?.carTitle)
    }

    @Test
    fun `bluetooth module has no can filter`() = test {
        repository.archivePages[1] = page(1, "2.15")
        val request = FirmwareArchiveRequest(1, FirmwareChannel.RELEASE, "2.15", "BT", UpdateProfile.Bluetooth)
        val (vm, _) = viewModel(request = request)

        assertNull(vm.state.value.canFilter)
    }

    @Test
    fun `archive from the vehicle card resolves the main module, filters by can and does not pick`() = test {
        repository.mainModule = Response.Success(42)
        repository.archivePages[1] = page(1, "3.61")
        val request = FirmwareArchiveRequest(
            moduleId = null,
            channel = FirmwareChannel.RELEASE,
            installedVersion = "3.57",
            deviceTitle = "DX4G  77",
            profile = UpdateProfile.Main,
            deviceName = "DX4G",
            canCode = "0213",
            viewOnly = true
        )
        val (vm, effects) = viewModel(request = request)

        assertEquals(listOf("DX4G"), repository.mainModuleCalls)
        assertEquals(listOf<Triple<Long, Int, String?>>(Triple(42L, 1, "0213")), repository.archiveRequests)
        assertEquals(true, vm.state.value.viewOnly)

        vm.onPick("3.61")
        runCurrent()
        assertEquals(emptyList(), effects.picked())
    }

    @Test
    fun `model without a main module is empty, module lookup failure is an error`() = test {
        val request = FirmwareArchiveRequest(null, FirmwareChannel.RELEASE, null, "X", UpdateProfile.Main, "X")
        repository.mainModule = Response.Success(null)
        val (empty, _) = viewModel(request = request)
        assertIs<ArchiveContent.Empty>(empty.state.value.content)

        repository.mainModule = Response.Error(IllegalStateException("offline"))
        val (failed, _) = viewModel(request = request)
        assertIs<ArchiveContent.Error>(failed.state.value.content)
        assertEquals(emptyList<Triple<Long, Int, String?>>(), repository.archiveRequests)
    }

    @Test
    fun `can of a version is shown in its row`() = test {
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(listOf(version("3.61").copy(canCode = "0213", canVersion = "2.0")), 1)
        )
        val (vm, _) = viewModel()

        val row = vm.rows().single()
        assertEquals("0213", row.canCode)
        assertEquals("2.0", row.canVersion)
    }
}
