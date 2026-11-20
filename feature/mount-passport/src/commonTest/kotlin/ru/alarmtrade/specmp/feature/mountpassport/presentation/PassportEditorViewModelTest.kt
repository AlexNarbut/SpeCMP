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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.files.ImageSource
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPeripherals
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhoto
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportSettingsSnapshot
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStageNotReadException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageFullException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitMismatchException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUsbPeripheral
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehiclePick
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorArgs
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorEffect
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportField
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportWork
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PhotoSheet
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PhotoSource
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.RereadError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.SaveStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.VinError

@OptIn(ExperimentalCoroutinesApi::class)
class PassportEditorViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeMountPassportRepository()
    private val configurations = FakePassportConfigurationsRepository()
    private val permissions = FakePermissions()
    private val saver = FakeSettingsFileSaver()
    private val reading = FakePassportReadingRepository()
    private val viewModels = mutableListOf<PassportEditorViewModel>()

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
        connectedId: Long? = null,
        picker: FakeImagePicker = FakeImagePicker(),
    ): Pair<PassportEditorViewModel, MutableList<PassportEditorEffect>> {
        val session = testDeviceSession(
            FakeDeviceSessionRepository(
                testDevice(connectedId ?: UNIT_ID),
                link = MutableStateFlow(linkState(connectedId != null)),
            ),
            dispatcher = dispatcher,
            transport = ConnectionTransport.USB.takeIf { connectedId != null },
        )
        val vm = PassportEditorViewModel(
            args = PassportEditorArgs("p1"),
            deviceSession = session,
            repository = repository,
            reading = reading,
            configurations = configurations,
            permissions = permissions,
            imagePicker = picker,
            settingsFileSaver = saver,
            dispatchers = testDispatchers(dispatcher),
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<PassportEditorEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun passportWith(block: MountPassport.() -> MountPassport) {
        repository.passport = Response.Success(testPassport().block())
    }

    @Test
    fun loadsPassportAndFillsForm(): TestResult = test {
        val (vm, _) = viewModel()

        assertEquals("p1", vm.state.value.passport.content?.id)
        assertEquals("О 456 КМ 199", vm.state.value.form.plate)
        assertEquals("Автозвук", vm.state.value.form.organization)
        assertEquals(SaveStatus.SAVED, vm.state.value.save)
    }

    @Test
    fun loadErrorThenRetry(): TestResult = test {
        repository.passport = Response.Error(IllegalStateException("unreadable"))
        val (vm, _) = viewModel()
        assertEquals(LCEState.Error(PassportError.Unknown), vm.state.value.passport)

        repository.passport = Response.Success(testPassport())
        vm.onRetry()
        runCurrent()

        assertEquals("p1", vm.state.value.passport.content?.id)
    }

    @Test
    fun seriesOfEditsIsOneWrite(): TestResult = test {
        val (vm, _) = viewModel()

        vm.onFieldChange(PassportField.CLIENT_NAME, "И")
        vm.onFieldChange(PassportField.CLIENT_NAME, "Ир")
        advanceTimeBy(300)
        vm.onFieldChange(PassportField.CLIENT_NAME, "Ирина")
        assertEquals(SaveStatus.PENDING, vm.state.value.save)
        assertTrue(repository.edits.isEmpty())

        advanceTimeBy(1_000)

        assertEquals(1, repository.edits.size)
        assertEquals("Ирина", repository.edits.single().client.name)
        assertEquals(SaveStatus.SAVED, vm.state.value.save)
    }

    @Test
    fun saveFailureIsShown(): TestResult = test {
        repository.editResult = Response.Error(IllegalStateException("io"))
        val (vm, _) = viewModel()

        vm.onFieldChange(PassportField.PLATE, "А 1")
        advanceTimeBy(1_000)

        assertEquals(SaveStatus.FAILED, vm.state.value.save)
    }

    @Test
    fun backWritesPendingEditAndCloses(): TestResult = test {
        val (vm, effects) = viewModel()

        vm.onFieldChange(PassportField.REMARKS, "Сирена под капотом")
        vm.onBack()
        runCurrent()

        assertEquals("Сирена под капотом", repository.edits.single().remarks)
        assertEquals(listOf<PassportEditorEffect>(PassportEditorEffect.Close), effects)
        advanceTimeBy(2_000)
        assertEquals(1, repository.edits.size)
    }

    @Test
    fun backWithoutEditsDoesNotWrite(): TestResult = test {
        val (vm, effects) = viewModel()

        vm.onBack()
        runCurrent()

        assertTrue(repository.edits.isEmpty())
        assertEquals(listOf<PassportEditorEffect>(PassportEditorEffect.Close), effects)
    }

    @Test
    fun invalidFieldsShowErrorsAndAreNotWritten(): TestResult = test {
        passportWith { copy(vehicle = vehicle.copy(vin = "XWEPH81ADN0012345", mileageKm = 10)) }
        val (vm, _) = viewModel()

        vm.onFieldChange(PassportField.VIN, "xweph81adn001234o")
        vm.onFieldChange(PassportField.MILEAGE, "12к")
        vm.onFieldChange(PassportField.CLIENT_PHONE, "7903")
        advanceTimeBy(1_000)

        assertEquals("XWEPH81ADN001234O", vm.state.value.form.vin)
        assertEquals(VinError.INVALID_CHARS, vm.state.value.vinError)
        assertTrue(vm.state.value.mileageInvalid)
        assertTrue(vm.state.value.phoneInvalid)
        val edit = repository.edits.last()
        assertEquals("XWEPH81ADN0012345", edit.vehicle.vin)
        assertEquals(10L, edit.vehicle.mileageKm)
    }

    @Test
    fun remarksAreLimited(): TestResult = test {
        val (vm, _) = viewModel()

        vm.onFieldChange(PassportField.REMARKS, "x".repeat(1_200))

        assertEquals(MountPassport.MAX_REMARKS_LENGTH, vm.state.value.form.remarks.length)
    }

    @Test
    fun worksAreCheckboxesForAdvancedMount(): TestResult = test {
        passportWith { copy(origin = PassportOrigin.ADVANCED_MOUNT) }
        val (vm, _) = viewModel()
        assertTrue(vm.state.value.worksEditable)

        vm.onWorkToggle(PassportWork.CLONE, true)
        advanceTimeBy(1_000)

        assertTrue(vm.state.value.form.works.clone)
        assertTrue(repository.edits.single().works.clone)
    }

    @Test
    fun worksAreReadOnlyForFastMount(): TestResult = test {
        passportWith { copy(origin = PassportOrigin.FAST_MOUNT, works = works.copy(fromMaster = true)) }
        val (vm, _) = viewModel()
        assertFalse(vm.state.value.worksEditable)

        vm.onWorkToggle(PassportWork.CLONE, true)
        advanceTimeBy(1_000)

        assertFalse(vm.state.value.form.works.clone)
        assertTrue(repository.edits.isEmpty())
    }

    @Test
    fun rereadVisibleOnlyWithSameUnitConnected(): TestResult = test {
        val (same, _) = viewModel(connectedId = UNIT_ID)
        val (other, _) = viewModel(connectedId = 999)
        val (none, _) = viewModel(connectedId = null)

        assertTrue(same.state.value.rereadVisible)
        assertFalse(other.state.value.rereadVisible)
        assertFalse(none.state.value.rereadVisible)
        assertFalse(same.state.value.readUnitVisible)
    }

    @Test
    fun passportWithoutUnitOffersReadingWhenThatUnitIsConnected(): TestResult = test {
        repository.passport = Response.Success(testPassport(manual = true))
        val (vm, _) = viewModel(connectedId = UNIT_ID)

        assertFalse(vm.state.value.rereadVisible)
        assertTrue(vm.state.value.readUnitVisible)
    }

    @Test
    fun cameraPhotoIsPreparedWithThumbnailAndAdded(): TestResult = test {
        val picker = FakeImagePicker()
        val (vm, _) = viewModel(picker = picker)

        vm.onAddPhoto()
        assertEquals(PhotoSheet.Pick, vm.state.value.photoSheet)
        vm.onPhotoSource(PhotoSource.CAMERA)
        runCurrent()

        assertEquals(listOf(ImageSource.Camera), picker.requests)
        assertEquals(listOf(320), picker.preparedSides)
        val (jpeg, thumb) = repository.addedPhotos.single()
        assertTrue(jpeg.contentEquals(PICKED_BYTES))
        assertTrue(thumb.contentEquals(THUMB_BYTES))
        assertEquals(1, vm.state.value.passport.content?.photos?.size)
        assertNull(vm.state.value.photoSheet)
        assertFalse(vm.state.value.addingPhoto)
    }

    @Test
    fun cameraDeniedOffersSettingsAndGallery(): TestResult = test {
        permissions.camera = PermissionResult.Denied(canOpenSettings = true)
        val picker = FakeImagePicker()
        val (vm, _) = viewModel(picker = picker)

        vm.onAddPhoto()
        vm.onPhotoSource(PhotoSource.CAMERA)
        runCurrent()

        assertEquals(PhotoSheet.CameraDenied(canOpenSettings = true), vm.state.value.photoSheet)
        assertTrue(picker.requests.isEmpty())

        vm.onOpenAppSettings()
        assertEquals(1, permissions.settingsOpened)
        assertNull(vm.state.value.photoSheet)
    }

    @Test
    fun withoutCameraAddGoesStraightToFile(): TestResult = test {
        val picker = FakeImagePicker(canTakePhoto = false)
        val (vm, _) = viewModel(picker = picker)

        vm.onAddPhoto()
        runCurrent()

        assertNull(vm.state.value.photoSheet)
        assertEquals(listOf(ImageSource.Gallery), picker.requests)
    }

    @Test
    fun photoLimitStopsBeforePicker(): TestResult = test {
        passportWith { copy(photos = List(MountPassport.MAX_PHOTOS) { PassportPhoto("ph$it", "/p", "/t", null) }) }
        val picker = FakeImagePicker()
        val (vm, _) = viewModel(picker = picker)

        vm.onAddPhoto()
        runCurrent()

        assertEquals(PassportError.PhotoLimit, vm.state.value.photoError)
        assertNull(vm.state.value.photoSheet)
        assertTrue(picker.requests.isEmpty())
    }

    @Test
    fun noSpaceIsShownUnderPhotos(): TestResult = test {
        repository.addPhotoResult = Response.Error(PassportStorageFullException())
        val (vm, _) = viewModel()

        vm.onAddPhoto()
        vm.onPhotoSource(PhotoSource.GALLERY)
        runCurrent()

        assertEquals(PassportError.StorageFull, vm.state.value.photoError)
        assertFalse(vm.state.value.addingPhoto)
    }

    @Test
    fun photoDeleteNeedsConfirmationAndClosesViewerWhenEmpty(): TestResult = test {
        passportWith { copy(photos = listOf(PassportPhoto("ph1", "/p", "/t", null))) }
        val (vm, _) = viewModel()
        vm.onPhotoOpen(0)

        vm.onPhotoDeleteRequest("ph1")
        assertEquals("ph1", vm.state.value.photoToDelete)
        assertTrue(repository.deletedPhotos.isEmpty())

        vm.onPhotoDeleteConfirm()
        runCurrent()

        assertEquals(listOf("ph1"), repository.deletedPhotos)
        assertNull(vm.state.value.photoToDelete)
        assertNull(vm.state.value.viewer)
        assertEquals(0, vm.state.value.passport.content?.photos?.size)
    }

    @Test
    fun captionIsWrittenOnceAfterTyping(): TestResult = test {
        passportWith { copy(photos = listOf(PassportPhoto("ph1", "/p", "/t", null))) }
        val (vm, _) = viewModel()

        vm.onPhotoCaptionChange("ph1", "Ре")
        vm.onPhotoCaptionChange("ph1", "Реле")
        assertEquals("Реле", vm.state.value.passport.content?.photos?.single()?.caption)
        advanceTimeBy(1_000)

        assertEquals(listOf<Pair<String, String?>>("ph1" to "Реле"), repository.captions)
    }

    @Test
    fun settingsAreSavedToFile(): TestResult = test {
        passportWith { copy(settingsSnapshot = PassportSettingsSnapshot("Pandora DX-91", CREATED, canFactory = false)) }
        val (vm, effects) = viewModel()

        vm.onSaveSettingsFile()
        runCurrent()

        val (path, name) = saver.saves.single()
        assertEquals("/data/p1/settings.dxs", path)
        assertTrue(name.endsWith(".dxs"))
        assertIs<PassportEditorEffect.SettingsFileSaved>(effects.single())
    }

    @Test
    fun settingsAreSavedToMyConfigurations(): TestResult = test {
        passportWith { copy(settingsSnapshot = PassportSettingsSnapshot("Pandora DX-91", CREATED, canFactory = false)) }
        val (vm, effects) = viewModel()

        vm.onSaveSettingsToConfigurations()
        runCurrent()

        assertEquals(Triple("Toyota RAV4", "Pandora DX-91", "/data/p1/settings.dxs"), configurations.saves.single())
        assertEquals(listOf<PassportEditorEffect>(PassportEditorEffect.ConfigurationSaved), effects)
    }

    @Test
    fun rereadReplacesPassportData(): TestResult = test {
        val usb = PassportPeripherals(usb = listOf(PassportUsbPeripheral("RHM", "Модуль", 1, true, true)))
        reading.rereadResult = Response.Success(testPassport().copy(peripherals = usb))
        val (vm, effects) = viewModel(connectedId = UNIT_ID)

        vm.onReread(PassportReadStage.PERIPHERALS)

        assertEquals(listOf(PassportReadStage.PERIPHERALS), reading.rereads)
        assertEquals(usb, vm.state.value.passport.content?.peripherals)
        assertNull(vm.state.value.rereading)
        assertEquals(listOf<PassportEditorEffect>(PassportEditorEffect.Reread), effects)
    }

    @Test
    fun rereadShowsProgressAndIgnoresRepeats(): TestResult = test {
        reading.rereadDelayMs = 1_000
        val (vm, _) = viewModel(connectedId = UNIT_ID)

        vm.onReread(PassportReadStage.SETTINGS)
        vm.onReread(PassportReadStage.PARAMETERS)

        assertEquals(PassportReadStage.SETTINGS, vm.state.value.rereading)
        advanceTimeBy(1_100)
        assertEquals(listOf(PassportReadStage.SETTINGS), reading.rereads)
        assertNull(vm.state.value.rereading)
    }

    @Test
    fun rereadOtherUnitIsReported(): TestResult = test {
        reading.rereadResult = Response.Error(PassportUnitMismatchException("1234567", "7654321"))
        val (vm, effects) = viewModel(connectedId = UNIT_ID)

        vm.onReread(PassportReadStage.PARAMETERS)

        assertEquals(
            listOf<PassportEditorEffect>(PassportEditorEffect.RereadFailed(RereadError.WrongUnit("7654321"))),
            effects,
        )
        assertEquals("p1", vm.state.value.passport.content?.id)
    }

    @Test
    fun rereadNotReadIsReported(): TestResult = test {
        reading.rereadResult = Response.Error(PassportStageNotReadException(connectionLost = true))
        val (vm, effects) = viewModel(connectedId = UNIT_ID)

        vm.onReread(PassportReadStage.SETTINGS)

        assertEquals(
            listOf<PassportEditorEffect>(PassportEditorEffect.RereadFailed(RereadError.NotRead(connectionLost = true))),
            effects,
        )
        assertNull(vm.state.value.rereading)
    }

    @Test
    fun outsideChangeReloadsSavedPassport(): TestResult = test {
        val (vm, _) = viewModel()
        val later = Instant.fromEpochMilliseconds(CREATED.toEpochMilliseconds() + 60_000)

        passportWith { copy(updatedAt = later, unit = unit.copy(manual = false, imei = "861234050118734")) }
        repository.all.value = Response.Success(listOf(summary(updatedAt = later)))
        runCurrent()

        assertEquals("861234050118734", vm.state.value.passport.content?.unit?.imei)
    }

    @Test
    fun outsideChangeDoesNotDropUnsavedInput(): TestResult = test {
        val (vm, _) = viewModel()
        val later = Instant.fromEpochMilliseconds(CREATED.toEpochMilliseconds() + 60_000)

        vm.onFieldChange(PassportField.CLIENT_NAME, "Ирина")
        repository.all.value = Response.Success(listOf(summary(updatedAt = later)))
        runCurrent()

        assertEquals("Ирина", vm.state.value.form.clientName)
    }

    @Test
    fun carIsPickedFromTheCatalogAndSaved(): TestResult = test {
        passportWith { copy(vehicle = PassportVehicle()) }
        val (vm, _) = viewModel()

        vm.onVehiclePickStart()
        assertTrue(vm.state.value.vehiclePicking)
        vm.onVehiclePickCancel()
        assertFalse(vm.state.value.vehiclePicking)

        vm.onVehiclePickStart()
        vm.onVehiclePicked(PassportVehiclePick(catalogCarId = "15", title = "Toyota RAV4 2019–2024"))
        advanceTimeBy(1_000)

        assertFalse(vm.state.value.vehiclePicking)
        assertEquals("Toyota RAV4 2019–2024", vm.state.value.form.carTitle)
        assertEquals("15", repository.edits.last().vehicle.catalogCarId)
    }
}
