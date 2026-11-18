package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppVersion
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.files.ImageCropRect
import ru.alarmtrade.specmp.core.files.ImagePickResult
import ru.alarmtrade.specmp.core.files.ImagePrepareResult
import ru.alarmtrade.specmp.core.files.ImageSource
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.domain.repository.SessionMarkRepository
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.ReleaseAccountStateUseCase
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdate
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainAppUpdateRepository
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.model.MainAvatarError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile
import ru.alarmtrade.specmp.feature.push.testing.TestPushRegistration

class MainScreenViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeMainRepository()
    private val appUpdates = FakeMainAppUpdateRepository()
    private val authRepository = FakeAuthStateRepository()
    private val permissions = FakeNotificationPermissions()
    private val push = TestPushRegistration()
    private var imagePicker = FakeImagePicker()

    private fun createViewModel() = MainScreenViewModel(
        LogoutUseCase(authRepository, ReleaseAccountStateUseCase(NoSessionMarks, emptyList())),
        repository,
        appUpdates,
        { AppVersion(name = "1.0", code = 1) },
        permissions,
        push.useCase,
        testDispatchers(dispatcher),
        imagePicker,
    )

    private fun TestScope.viewModel() = createViewModel().also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `main start asks for notifications and registers the push token`() = runTest(dispatcher) {
        viewModel()

        assertEquals(1, permissions.notificationRequests)
        assertEquals(listOf("token"), push.pushTokens.sends.map { it.first })
    }

    @Test
    fun `denied notifications still register the push token`() = runTest(dispatcher) {
        permissions.result = PermissionResult.Denied(canOpenSettings = true)

        viewModel()

        assertEquals(1, push.pushTokens.sends.size)
    }

    @Test
    fun `push registration failure does not break the main screen`() = runTest(dispatcher) {
        push.pushTokens.result = Response.Error(IllegalStateException("offline"))

        val vm = viewModel()

        assertFalse(vm.state.value.isInitialLoading)
        assertEquals(MainProfile(name = "Ivan", email = "ivan@example.com"), vm.state.value.profile)
    }

    @Test
    fun `avatar tap with a camera opens the source choice`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onAvatarClicked()

        assertTrue(vm.state.value.isAvatarSourceChoiceOpen)
        assertTrue(imagePicker.requests.isEmpty())
    }

    @Test
    fun `avatar tap without a camera picks a file right away`() = runTest(dispatcher) {
        imagePicker = FakeImagePicker(canTakePhoto = false)
        val vm = viewModel()

        vm.onAvatarClicked()

        assertFalse(vm.state.value.isAvatarSourceChoiceOpen)
        assertEquals(listOf(ImageSource.Gallery), imagePicker.requests)
        assertTrue(vm.state.value.avatarCrop != null)
    }

    @Test
    fun `uploaded avatar replaces the shown one`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAvatarClicked()

        vm.onAvatarSourceSelected(ImageSource.Gallery)
        vm.onAvatarCropConfirmed(CROP)

        val state = vm.state.value
        assertFalse(state.isAvatarSourceChoiceOpen)
        assertNull(state.avatarCrop)
        assertEquals(listOf<ImageCropRect?>(CROP), imagePicker.prepared)
        assertFalse(state.isAvatarUploading)
        assertNull(state.avatarError)
        assertEquals(NEW_AVATAR, state.profile.avatarUrl)
        assertEquals(listOf("avatar.jpg"), repository.uploadedAvatars)
        assertEquals(0, permissions.cameraRequests)
    }

    @Test
    fun `cancelled image choice changes nothing`() = runTest(dispatcher) {
        imagePicker.result = ImagePickResult.Cancelled
        val vm = viewModel()
        val before = vm.state.value

        vm.onAvatarSourceSelected(ImageSource.Gallery)

        assertEquals(before.profile, vm.state.value.profile)
        assertNull(vm.state.value.avatarError)
        assertFalse(vm.state.value.isAvatarUploading)
        assertTrue(repository.uploadedAvatars.isEmpty())
    }

    @Test
    fun `failed upload shows an error and keeps the old avatar`() = runTest(dispatcher) {
        repository.cachedProfile = MainUserProfile("Ivan", "ivan@example.com", avatarUrl = OLD_AVATAR)
        repository.profile = Response.Success(MainUserProfile("Ivan", "ivan@example.com", avatarUrl = OLD_AVATAR))
        repository.avatarResult = Response.Error(IllegalStateException("too large"))
        val vm = viewModel()

        vm.onAvatarSourceSelected(ImageSource.Gallery)
        vm.onAvatarCropConfirmed(CROP)

        val state = vm.state.value
        assertIs<MainAvatarError.UploadFailed>(state.avatarError)
        assertFalse(state.isAvatarUploading)
        assertEquals(OLD_AVATAR, state.profile.avatarUrl)
    }

    @Test
    fun `unreadable image shows an error without uploading`() = runTest(dispatcher) {
        imagePicker.result = ImagePickResult.Failed
        val vm = viewModel()

        vm.onAvatarSourceSelected(ImageSource.Gallery)

        assertEquals(MainAvatarError.ImageNotRead, vm.state.value.avatarError)
        assertTrue(repository.uploadedAvatars.isEmpty())
    }

    @Test
    fun `dismissing the avatar error hides it and keeps the avatar`() = runTest(dispatcher) {
        repository.avatarResult = Response.Error(IllegalStateException("too large"))
        val vm = viewModel()
        vm.onAvatarSourceSelected(ImageSource.Gallery)
        vm.onAvatarCropConfirmed(CROP)
        val avatarBefore = vm.state.value.profile.avatarUrl

        vm.onAvatarErrorDismissed()

        assertNull(vm.state.value.avatarError)
        assertEquals(avatarBefore, vm.state.value.profile.avatarUrl)
    }

    @Test
    fun `avatar taps during an upload are ignored`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        repository.avatarGate = gate
        val vm = viewModel()

        vm.onAvatarSourceSelected(ImageSource.Gallery)
        vm.onAvatarCropConfirmed(CROP)
        assertTrue(vm.state.value.isAvatarUploading)

        vm.onAvatarClicked()
        vm.onAvatarSourceSelected(ImageSource.Gallery)
        vm.onAvatarCropConfirmed(CROP)
        assertFalse(vm.state.value.isAvatarSourceChoiceOpen)
        assertNull(vm.state.value.avatarCrop)

        gate.complete(Unit)
        assertFalse(vm.state.value.isAvatarUploading)
        assertEquals(1, imagePicker.requests.size)
        assertEquals(1, repository.uploadedAvatars.size)
    }

    @Test
    fun `camera asks for the permission and a denial shows an error`() = runTest(dispatcher) {
        permissions.cameraResult = PermissionResult.Denied(canOpenSettings = true)
        val vm = viewModel()

        vm.onAvatarSourceSelected(ImageSource.Camera)

        assertEquals(1, permissions.cameraRequests)
        assertEquals(MainAvatarError.CameraDenied, vm.state.value.avatarError)
        assertTrue(imagePicker.requests.isEmpty())
    }

    @Test
    fun `granted camera takes a photo`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onAvatarSourceSelected(ImageSource.Camera)
        vm.onAvatarCropConfirmed(CROP)

        assertEquals(listOf(ImageSource.Camera), imagePicker.requests)
        assertEquals(NEW_AVATAR, vm.state.value.profile.avatarUrl)
    }

    @Test
    fun `picked image opens the crop screen without uploading`() = runTest(dispatcher) {
        val image = testPickedImage()
        imagePicker.result = ImagePickResult.Picked(image)
        val vm = viewModel()

        vm.onAvatarSourceSelected(ImageSource.Gallery)

        assertSame(image, vm.state.value.avatarCrop)
        assertFalse(vm.state.value.isAvatarUploading)
        assertTrue(imagePicker.prepared.isEmpty())
        assertTrue(repository.uploadedAvatars.isEmpty())
    }

    @Test
    fun `cancelled crop closes the screen without uploading or an error`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAvatarSourceSelected(ImageSource.Gallery)

        vm.onAvatarCropCancelled()

        assertNull(vm.state.value.avatarCrop)
        assertNull(vm.state.value.avatarError)
        assertTrue(imagePicker.prepared.isEmpty())
        assertTrue(repository.uploadedAvatars.isEmpty())
        vm.onAvatarCropConfirmed(CROP)
        assertTrue(repository.uploadedAvatars.isEmpty())
    }

    @Test
    fun `failed crop preparation shows an error without uploading`() = runTest(dispatcher) {
        imagePicker.prepareResult = ImagePrepareResult.Failed
        val vm = viewModel()
        vm.onAvatarSourceSelected(ImageSource.Gallery)

        vm.onAvatarCropConfirmed(CROP)

        assertEquals(MainAvatarError.ImageNotRead, vm.state.value.avatarError)
        assertFalse(vm.state.value.isAvatarUploading)
        assertTrue(repository.uploadedAvatars.isEmpty())
    }

    @Test
    fun `first subscription loads the home data`() = runTest(dispatcher) {
        repository.features = MainUserAppFeatures(hasProductSearch = true)
        val vm = viewModel()

        val state = vm.state.value
        assertFalse(state.isInitialLoading)
        assertEquals(MainProfile(name = "Ivan", email = "ivan@example.com"), state.profile)
        assertEquals(42L, state.databaseVersion)
        assertEquals(1, appUpdates.checks)
        assertEquals("1.0", state.appVersionName)
        assertTrue(MainHomeAction.ProductInfo in state.visibleActions)
        assertFalse(MainHomeAction.TechSupportChat in state.visibleActions)
    }

    @Test
    fun `cached profile is shown when the network fails`() = runTest(dispatcher) {
        repository.cachedProfile = MainUserProfile(
            name = "Cached",
            email = "cached@example.com",
            avatarUrl = "https://example.com/cached.jpg",
        )
        repository.profile = Response.Error(IllegalStateException("offline"))
        val vm = viewModel()

        val state = vm.state.value
        assertFalse(state.isInitialLoading)
        assertEquals(
            MainProfile(name = "Cached", email = "cached@example.com", avatarUrl = "https://example.com/cached.jpg"),
            state.profile,
        )
        assertTrue(state.userDataError != null)
    }

    @Test
    fun `opening the menu and the refresh button reload the profile`() = runTest(dispatcher) {
        val vm = viewModel()
        val before = repository.profileRequests

        vm.onMenuClicked()
        vm.onUserDataRefreshClicked()

        assertEquals(before + 2, repository.profileRequests)
    }

    @Test
    fun `chat card follows chat availability`() = runTest(dispatcher) {
        val vm = viewModel()

        repository.chatAvailability.value = Response.Success(true)

        assertTrue(MainHomeAction.TechSupportChat in vm.state.value.visibleActions)
    }

    @Test
    fun `chat card is hidden only by an explicit server answer`() = runTest(dispatcher) {
        repository.chatAvailability.value = Response.Error(IllegalStateException("offline"))
        val vm = viewModel()
        assertTrue(MainHomeAction.TechSupportChat in vm.state.value.visibleActions)

        repository.chatAvailability.value = Response.Success(false)
        assertFalse(MainHomeAction.TechSupportChat in vm.state.value.visibleActions)

        repository.chatAvailability.value = Response.Error(IllegalStateException("offline"))
        assertFalse(MainHomeAction.TechSupportChat in vm.state.value.visibleActions)
    }

    @Test
    fun `home action and settings navigate`() = runTest(dispatcher) {
        val vm = viewModel()
        val effects = mutableListOf<MainScreenEffect>()
        backgroundScope.launch { vm.sideEffect.collect { effects += it } }

        vm.onMenuClicked()
        assertTrue(vm.state.value.isMenuOpen)
        vm.onSettingsClicked()
        vm.onHomeActionClicked(MainHomeAction.Cars)

        assertFalse(vm.state.value.isMenuOpen)
        assertEquals(
            listOf<MainScreenEffect>(
                MainScreenEffect.Navigate(MainNavigationTarget.Settings),
                MainScreenEffect.Navigate(MainHomeAction.Cars.target),
            ),
            effects,
        )
    }

    @Test
    fun `appearance menu item closes the menu and navigates`() = runTest(dispatcher) {
        val vm = viewModel()
        val effects = mutableListOf<MainScreenEffect>()
        backgroundScope.launch { vm.sideEffect.collect { effects += it } }

        vm.onMenuClicked()
        vm.onAppearanceClicked()

        assertFalse(vm.state.value.isMenuOpen)
        assertEquals(listOf<MainScreenEffect>(MainScreenEffect.Navigate(MainNavigationTarget.Appearance)), effects)
    }

    @Test
    fun `logout error is shown once and reset on return to the screen`() = runTest(dispatcher) {
        authRepository.logoutResponse = Response.Error(IllegalStateException())
        val vm = createViewModel()
        val subscription = backgroundScope.launch { vm.state.collect {} }

        vm.onLogout()
        assertIs<LCEState.Error<*>>(vm.state.value.logoutState)

        subscription.cancel()
        advanceTimeBy(10.seconds)
        backgroundScope.launch { vm.state.collect {} }

        assertNull(vm.state.value.logoutError)
    }

    @Test
    fun `successful logout keeps the button locked`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onLogout()

        assertTrue(vm.state.value.isLogoutInProgress)
    }

    @Test
    fun `desktop shows the update toast once and mobile shows the banner until dismissed`() = runTest(dispatcher) {
        val vm = viewModel()

        appUpdates.status.value = MainAppUpdateStatus(installsItself = true, update = MainAppUpdate.Available("2.2.0"))
        assertEquals("2.2.0", vm.state.value.updateToastVersion)
        assertFalse(vm.state.value.showUpdateBanner)
        vm.onUpdateToastDismissed()
        appUpdates.status.value = MainAppUpdateStatus(installsItself = true, update = MainAppUpdate.Available("2.2.1"))
        assertEquals(null, vm.state.value.updateToastVersion)

        appUpdates.status.value = MainAppUpdateStatus(installsItself = false, update = MainAppUpdate.Available("2.2.1"))
        assertTrue(vm.state.value.showUpdateBanner)
        vm.onUpdateBannerDismissed()
        assertFalse(vm.state.value.showUpdateBanner)
        assertEquals(listOf("2.2.1"), appUpdates.dismissed)
    }

    private class FakeMainAppUpdateRepository : MainAppUpdateRepository {
        val status = MutableStateFlow(MainAppUpdateStatus(installsItself = false, update = MainAppUpdate.None))
        var checks = 0
        val dismissed = mutableListOf<String>()

        override fun status(): Flow<Response<MainAppUpdateStatus>> = status.map { Response.Success(it) }

        override suspend fun check(): Response<Unit> {
            checks++
            return Response.Success(Unit)
        }

        override suspend fun download(): Response<Unit> = Response.Success(Unit)

        override suspend fun dismissedBannerVersion(): Response<String?> = Response.Success(null)

        override suspend fun dismissBanner(version: String): Response<Unit> {
            dismissed += version
            return Response.Success(Unit)
        }
    }

    private class FakeMainRepository : MainRepository {
        var features = MainUserAppFeatures()
        var profile: Response<MainUserProfile> = Response.Success(
            MainUserProfile(name = "Ivan", email = "ivan@example.com"),
        )
        var cachedProfile: MainUserProfile? = null
        var profileRequests = 0
        val chatAvailability = MutableStateFlow<Response<Boolean>>(Response.Success(false))

        override suspend fun currentUserProfile(): Response<MainUserProfile> = profile.also { profileRequests++ }

        override suspend fun cachedUserProfile(): Response<MainUserProfile?> = Response.Success(cachedProfile)

        override suspend fun cachedUserAppFeatures(): Response<MainUserAppFeatures?> = Response.Success(null)

        override suspend fun cachedUserCommercial(): Response<MainUserCommercial?> = Response.Success(null)

        override suspend fun currentSettingsVersion(): Response<Long?> = Response.Success(1L)

        override fun synchronizeSettingsVersion(): Flow<Response<MainDatabaseSyncStatus>> = flowOf(
            Response.Success(MainDatabaseSyncStatus.InProgress.CheckingVersion),
            Response.Success(MainDatabaseSyncStatus.Finished.Success(newVersion = 42L)),
        )

        override suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures> = Response.Success(features)

        override suspend fun currentUserCommercial(): Response<MainUserCommercial> =
            Response.Success(MainUserCommercial())

        override fun observeChatAvailability(): Flow<Response<Boolean>> = chatAvailability

        var avatarResult: Response<String> = Response.Success(NEW_AVATAR)
        var avatarGate: CompletableDeferred<Unit>? = null
        val uploadedAvatars = mutableListOf<String>()

        override suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): Response<String> {
            uploadedAvatars += fileName
            avatarGate?.await()
            return avatarResult
        }
    }

    private class FakeAuthStateRepository : AuthStateRepository {
        var logoutResponse: Response<Unit> = Response.Success(Unit)

        override fun observeIsLoggedIn(): Flow<Response<Boolean>> = flowOf(Response.Success(true))

        override suspend fun logout(): Response<Unit> = logoutResponse
    }

    private object NoSessionMarks : SessionMarkRepository {
        override suspend fun isMarked(): Response<Boolean> = Response.Success(false)

        override suspend fun mark(): Response<Unit> = Response.Success(Unit)

        override suspend fun clear(): Response<Unit> = Response.Success(Unit)
    }

    private companion object {
        const val NEW_AVATAR = "https://example.com/new.jpg"
        val CROP = ImageCropRect(left = 50, top = 0, width = 300, height = 300, imageWidth = 400, imageHeight = 300)
        const val OLD_AVATAR = "https://example.com/old.jpg"
    }
}
