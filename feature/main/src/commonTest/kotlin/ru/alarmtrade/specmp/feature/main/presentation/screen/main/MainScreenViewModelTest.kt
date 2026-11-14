package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
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
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdate
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainAppUpdateRepository
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile

class MainScreenViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeMainRepository()
    private val appUpdates = FakeMainAppUpdateRepository()
    private val authRepository = FakeAuthStateRepository()

    private fun createViewModel() = MainScreenViewModel(
        LogoutUseCase(authRepository, emptyList()),
        repository,
        appUpdates,
        { AppVersion(name = "1.0", code = 1) },
        testDispatchers(dispatcher),
    )

    private fun TestScope.viewModel() = createViewModel().also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

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
        repository.cachedProfile = MainUserProfile(name = "Cached", email = "cached@example.com")
        repository.profile = Response.Error(IllegalStateException("offline"))
        val vm = viewModel()

        val state = vm.state.value
        assertFalse(state.isInitialLoading)
        assertEquals(MainProfile(name = "Cached", email = "cached@example.com"), state.profile)
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
    }

    private class FakeAuthStateRepository : AuthStateRepository {
        var logoutResponse: Response<Unit> = Response.Success(Unit)

        override fun observeIsLoggedIn(): Flow<Response<Boolean>> = flowOf(Response.Success(true))

        override suspend fun logout(): Response<Unit> = logoutResponse
    }
}
