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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems.archiveRequestOf
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
            FirmwareArchiveRequest(1, FirmwareChannel.RELEASE, installed, "DX4G  77", UpdateProfile.Main),
        exporter: FirmwareFileExporter? = null,
        subscribed: Boolean = true,
    ): Pair<FirmwareArchiveViewModel, MutableList<FirmwareArchiveEffect>> {
        val vm = FirmwareArchiveViewModel(
            request,
            repository,
            AppDispatchers(dispatcher, dispatcher, dispatcher),
            exporter,
        ).also { viewModels += it }
        if (subscribed) backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<FirmwareArchiveEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun List<FirmwareArchiveEffect>.picked(): List<String> =
        filterIsInstance<FirmwareArchiveEffect.Picked>().map { it.version.version }

    private fun FirmwareArchiveViewModel.rows(): List<ArchiveRowUi> = (state.value.content as LCEState.Content).content

    @Test
    fun `first page is shown newest first with marks relative to the installed version`() = test {
        repository.archivePages[1] = page(1, "3.55", "3.61", "3.57")
        val (vm, _) = viewModel()

        val rows = vm.rows()
        assertEquals(listOf("3.61", "3.57", "3.55"), rows.map { it.version })
        assertEquals(
            listOf(ArchiveRowKind.New, ArchiveRowKind.Installed, ArchiveRowKind.Older),
            rows.map { it.kind },
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
        val ready = vm.state.value.content as LCEState.Content
        assertEquals(FirmwareError.LoadFailed, ready.updateError)
        assertEquals(false, ready.isUpdating)
        assertEquals(listOf("3.61"), ready.content.map { it.version })
        vm.onEndReached()
        runCurrent()
        assertEquals(listOf(1, 2), repository.archiveCalls)

        repository.archivePages[2] = page(2, "3.60")
        vm.onRetryMore()
        runCurrent()
        assertEquals(listOf("3.61", "3.60"), vm.rows().map { it.version })
        assertEquals(null, (vm.state.value.content as LCEState.Content).updateError)
    }

    @Test
    fun `refresh while the next page loads drops the stale page`() = test {
        repository.archivePages[1] = page(1, "3.61", "3.60")
        repository.archivePages[2] = page(2, "3.58")
        val (vm, _) = viewModel()
        repository.archiveDelayMillis[2] = 1_000

        vm.onEndReached()
        runCurrent()
        // Обновление, пока страница 2 ещё грузится: её ответ придёт в уже сброшенный список.
        vm.onRefresh()
        advanceTimeBy(1_001)
        runCurrent()

        assertEquals(listOf("3.61", "3.60"), vm.rows().map { it.version })
        assertEquals(listOf(1, 2, 1), repository.archiveCalls)
    }

    @Test
    fun `first page failure is a full screen error and refresh loads again`() = test {
        val (vm, _) = viewModel()
        assertEquals(LCEState.Error(FirmwareError.LoadFailed), vm.state.value.content)

        repository.archivePages[1] = page(1, "3.61")
        vm.onRefresh()
        runCurrent()

        assertIs<LCEState.Content<*, *>>(vm.state.value.content)
    }

    @Test
    fun `empty answer is the empty state`() = test {
        repository.archivePages[1] = page(1)
        val (vm, _) = viewModel()

        assertEquals(LCEState.Content(emptyList<ArchiveRowUi>()), vm.state.value.content)
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
            viewOnly = true,
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
        assertEquals(LCEState.Content(emptyList<ArchiveRowUi>()), empty.state.value.content)

        repository.mainModule = Response.Error(IllegalStateException("offline"))
        val (failed, _) = viewModel(request = request)
        assertIs<LCEState.Error<*>>(failed.state.value.content)
        assertEquals(emptyList<Triple<Long, Int, String?>>(), repository.archiveRequests)
    }

    @Test
    fun `can of a version is shown in its row`() = test {
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(listOf(version("3.61").copy(canCode = "0213", canVersion = "2.0")), 1),
        )
        val (vm, _) = viewModel()

        val row = vm.rows().single()
        assertEquals("0213", row.canCode)
        assertEquals("2.0", row.canVersion)
    }

    private fun deviceCanRequest(factory: Boolean = false) = FirmwareArchiveRequest(
        moduleId = 1,
        channel = FirmwareChannel.RELEASE,
        installedVersion = "3.57",
        deviceTitle = "DX9X  77",
        profile = UpdateProfile.Main,
        deviceName = "DX9X",
        deviceCanCode = "2154".takeUnless { factory },
        deviceCanVersion = 103.takeUnless { factory },
        deviceCanProfile = "1.03".takeUnless { factory },
        deviceCanFactory = factory,
    )

    private fun canVersion(value: String, code: String, profile: String, profileCode: Int) = version(
        value,
    ).copy(
        url = "https://fw/$value/$code/$profile",
        canCode = code,
        canVersion = profile,
        canVersionCode = profileCode,
    )

    @Test
    fun `block can filters by code and can profile version on open and highlights its rows`() = test {
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(
                listOf(
                    canVersion("3.61", "2154", "1.03", 103),
                    canVersion("3.60", "3312", "1.01", 101),
                    version("3.58"),
                ),
                1,
            ),
        )
        val (vm, _) = viewModel(request = deviceCanRequest())

        assertEquals(listOf<FirmwareCan?>(FirmwareCan("2154", 103)), repository.archiveCans)
        val filter = vm.state.value.canFilter
        assertEquals("2154", filter?.input)
        assertEquals("1.03", filter?.deviceProfile)
        assertEquals(true, filter?.isDeviceCode)
        assertEquals(false, filter?.canResetToDevice)
        assertEquals(listOf(true, false, false), vm.rows().map { it.canMatchesDevice })
    }

    @Test
    fun `another code is searched without the can profile version and reset returns to the block can`() = test {
        repository.archivePages[1] = page(1, "3.61")
        val (vm, _) = viewModel(request = deviceCanRequest())

        vm.onCanInput("3312")
        runCurrent()
        assertEquals(FirmwareCan("3312", null), repository.archiveCans.last())
        assertEquals(true, vm.state.value.canFilter?.canResetToDevice)

        vm.onCanResetToDevice()
        runCurrent()
        assertEquals(FirmwareCan("2154", 103), repository.archiveCans.last())
        assertEquals("2154", vm.state.value.canFilter?.input)
        assertEquals(false, vm.state.value.canFilter?.canResetToDevice)

        // Пустое поле — все версии; код блока по-прежнему можно вернуть.
        vm.onCanInput("")
        runCurrent()
        assertNull(repository.archiveCans.last())
        assertEquals(true, vm.state.value.canFilter?.canResetToDevice)
    }

    @Test
    fun `vehicle code from the card wins over the block can but block rows stay highlighted`() = test {
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(listOf(version("3.61").copy(canCode = "2154")), 1),
        )
        val (vm, _) = viewModel(request = deviceCanRequest().copy(canCode = "0213", viewOnly = true))

        assertEquals(listOf<FirmwareCan?>(FirmwareCan("0213", null)), repository.archiveCans)
        assertEquals(listOf(true), vm.rows().map { it.canMatchesDevice })
    }

    @Test
    fun `factory can settings open all versions with nothing to highlight or reset`() = test {
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(listOf(canVersion("3.57", "2154", "1.03", 103), version("3.57")), 1),
        )
        val (vm, _) = viewModel(request = deviceCanRequest(factory = true))

        assertEquals(listOf<FirmwareCan?>(null), repository.archiveCans)
        val filter = vm.state.value.canFilter
        assertEquals(true, filter?.deviceFactory)
        assertEquals(false, filter?.canResetToDevice)
        // Сборка под CAN у блока с заводскими настройками — не «Установлена», без CAN — установлена.
        assertEquals(listOf(ArchiveRowKind.New, ArchiveRowKind.Installed), vm.rows().map { it.kind })

        vm.onCanResetToDevice()
        runCurrent()
        assertEquals(1, repository.archiveCans.size)
    }

    @Test
    fun `installed is only the block build, the same number under another can or profile is new`() = test {
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(
                listOf(
                    canVersion("3.57", "2154", "1.03", 103),
                    canVersion("3.57", "2154", "1.02", 102),
                    canVersion("3.57", "0188", "1.00", 100),
                ),
                1,
            ),
        )
        val (vm, _) = viewModel(request = deviceCanRequest().copy(canCode = null, deviceCanCode = "2154"))
        vm.onCanInput("")
        runCurrent()

        val rows = vm.rows()
        assertEquals(listOf(ArchiveRowKind.Installed, ArchiveRowKind.New, ArchiveRowKind.New), rows.map { it.kind })
        assertEquals(listOf(false, true, false), rows.map { it.canProfileDiffers })
    }

    @Test
    fun `one version number under two can codes is two rows and the picked row is the right file`() = test {
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(
                listOf(
                    version("3.61").copy(url = "https://fw/2154", canCode = "2154", canVersion = "1.03"),
                    version("3.61").copy(url = "https://fw/3312", canCode = "3312", canVersion = "1.01"),
                ),
                1,
            ),
        )
        repository.archivePages[2] = Response.Success(
            FirmwareVersionPage(listOf(version("3.61").copy(canCode = "3312", canVersion = "1.01")), 2),
        )
        val (vm, effects) = viewModel(request = deviceCanRequest().copy(canCode = null, deviceCanCode = null))

        assertEquals(listOf("3.61|2154|1.03", "3.61|3312|1.01"), vm.rows().map { it.key })

        // Та же сборка на следующей странице — повтор, конец списка.
        vm.onEndReached()
        runCurrent()
        assertEquals(2, vm.rows().size)

        vm.onPick("3.61|3312|1.01")
        runCurrent()
        val picked = effects.filterIsInstance<FirmwareArchiveEffect.Picked>().single().version
        assertEquals("https://fw/3312", picked.url)
    }

    private fun noUnitRequest(module: Long = 42, alarm: Boolean = true) = archiveRequestOf(
        FirmwareSystem(1, "Pandora DX-4GS", emptyList()),
        FirmwareSubsystem(
            module,
            if (alarm) "Основная программа" else "Bluetooth-модуль",
            if (alarm) "DX-4GS" else "BT-M2",
            if (alarm) FirmwareModuleKind.AlarmMain else FirmwareModuleKind.BluetoothModule,
        ),
    )

    @Test
    fun `no unit archive opens the picked subsystem and switches channel`() = test {
        repository.archivePages[1] = page(1, "3.61")
        val (vm, _) = viewModel(request = noUnitRequest())

        assertEquals(true, vm.state.value.noUnit)
        assertEquals(true, vm.state.value.viewOnly)
        assertEquals(true, vm.state.value.canFilter?.noUnit)
        assertEquals(Triple(42L, 1, null), repository.archiveRequests.last())
        assertEquals(emptyList(), repository.moduleCalls)

        vm.onChannelSelect(FirmwareChannel.BETA)
        runCurrent()
        assertEquals(FirmwareChannel.BETA, vm.state.value.channel)
        assertEquals(2, repository.archiveRequests.size)
    }

    @Test
    fun `can filter is only for the alarm main program`() = test {
        repository.archivePages[1] = page(1, "1.16")
        val (bluetooth, _) = viewModel(request = noUnitRequest(module = 7, alarm = false))
        assertNull(bluetooth.state.value.canFilter)

        // Основная программа не сигнализации (часы, трекер) — тоже без фильтра, с блоком или без.
        val (watch, _) = viewModel(request = noUnitRequest().copy(canFilterAllowed = false))
        assertNull(watch.state.value.canFilter)
        val (unit, _) = viewModel(
            request = FirmwareArchiveRequest(
                1,
                FirmwareChannel.RELEASE,
                null,
                "PW2",
                UpdateProfile.Main,
                canFilterAllowed = false,
            ),
        )
        assertNull(unit.state.value.canFilter)
    }

    @Test
    fun `subsystem without versions is no firmware, empty under a can filter is not`() = test {
        repository.archivePages[1] = page(1)
        val (vm, _) = viewModel(request = noUnitRequest())
        assertEquals(true, vm.state.value.noModelFirmware)

        vm.onCanInput("2154")
        runCurrent()
        assertEquals(false, vm.state.value.noModelFirmware)
    }

    @Test
    fun `download saves the file under a readable name and shows the saved dialog`() = test {
        repository.mainModule = Response.Success(42)
        repository.archivePages[1] = Response.Success(
            FirmwareVersionPage(
                listOf(
                    version("3.61 C").copy(
                        url = "https://fw/files/main.pld?t=1",
                        canCode = "2154",
                        canVersion = "1.03",
                    ),
                ),
                1,
            ),
        )
        repository.files["https://fw/files/main.pld?t=1"] = Response.Success(ByteArray(16))
        val exporter = FakeFileExporter()
        val (vm, _) = viewModel(request = noUnitRequest(), exporter = exporter)
        val key = vm.rows().single().key

        vm.onDownload(key)
        // Повторное нажатие, пока файл скачивается, вторую загрузку не запускает.
        vm.onDownload(key)
        runCurrent()
        assertEquals(1, repository.downloads.size)

        assertEquals(listOf("DX-4GS_3.61C_CAN2154-1.03.pld" to 16), exporter.saved)
        assertNull(vm.state.value.busy)
        val saved = vm.state.value.saved
        assertEquals("DX-4GS_3.61C_CAN2154-1.03.pld", saved?.fileName)

        // «Поделиться» из диалога: файл скачивается заново и уходит системному меню.
        vm.onSavedShare()
        runCurrent()
        assertNull(vm.state.value.saved)
        assertEquals(listOf("DX-4GS_3.61C_CAN2154-1.03.pld"), exporter.shared)
    }

    @Test
    fun `download, save and share failures are banners, cancel is silent`() = test {
        repository.archivePages[1] = page(1, "3.61")
        val exporter = FakeFileExporter()
        val (vm, _) = viewModel(request = noUnitRequest(), exporter = exporter)

        vm.onDownload("3.61")
        runCurrent()
        assertIs<ArchiveExportError.Download>(vm.state.value.exportError)
        assertEquals(emptyList(), exporter.saved)

        repository.files["https://fw/3.61"] = Response.Success(ByteArray(4))
        exporter.saveResult = FirmwareSaveResult.Cancelled
        vm.onDownload("3.61")
        runCurrent()
        assertNull(vm.state.value.exportError)
        assertNull(vm.state.value.saved)

        exporter.saveResult = FirmwareSaveResult.Failed
        vm.onDownload("3.61")
        runCurrent()
        assertEquals(ArchiveExportError.Save, vm.state.value.exportError)

        exporter.shareResult = false
        vm.onShare("3.61")
        runCurrent()
        assertEquals(ArchiveExportError.Share, vm.state.value.exportError)

        vm.onExportErrorDismiss()
        runCurrent()
        assertNull(vm.state.value.exportError)
    }

    @Test
    fun `archive with a unit does not download`() = test {
        repository.archivePages[1] = page(1, "3.61")
        repository.files["https://fw/3.61"] = Response.Success(ByteArray(4))
        val exporter = FakeFileExporter()
        val (vm, _) = viewModel(exporter = exporter)

        vm.onDownload("3.61")
        runCurrent()
        assertEquals(emptyList(), exporter.saved)
    }

    @Test
    fun `returning to the screen keeps the list without loading it again`() = test {
        repository.archivePages[1] = page(1, "3.61", "3.60")
        val (vm, _) = viewModel(subscribed = false)
        var screen = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        vm.onSelectRow("3.60")

        screen.cancel()
        advanceTimeBy(10_000)
        runCurrent()
        screen = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()

        assertEquals(listOf("3.61", "3.60"), vm.rows().map { it.version })
        assertEquals("3.60", vm.state.value.selected)
        assertEquals(listOf(1), repository.archiveCalls)
        screen.cancel()
    }
}
