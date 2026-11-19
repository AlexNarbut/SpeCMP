package ru.alarmtrade.specmp.feature.productinfo.presentation

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController
import ru.alarmtrade.specmp.core.barcode.BarcodeScannerAccess
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductKit
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductNotFoundException
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductQueryError
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductUnit
import ru.alarmtrade.specmp.feature.productinfo.domain.repository.ProductInfoRepository
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoError
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoArgs
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoEffect
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ProductInfoViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeProductInfoRepository()
    private val permissions = FakePermissions()
    private val effects = mutableListOf<ProductInfoEffect>()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(query: String? = null, connectedId: Long? = null): ProductInfoViewModel {
        val vm = ProductInfoViewModel(
            ProductInfoArgs(query, connectedId),
            repository,
            permissions,
            testDispatchers(dispatcher),
        )
        backgroundScope.launch { vm.state.collect {} }
        backgroundScope.launch { vm.sideEffect.collect { effects += it } }
        return vm
    }

    @Test
    fun fromHomeNothingIsSearched() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(LCEState.None, vm.state.value.result)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun initialQueryIsSearchedAndMarkedConnected() = runTest(dispatcher) {
        repository.answer = { Response.Success(info(id = it)) }
        val vm = viewModel(query = "107131", connectedId = 107131)
        advanceUntilIdle()

        assertEquals(listOf(107131L), repository.requests)
        assertIs<LCEState.Content<ProductInfo, ProductInfoError>>(vm.state.value.result)
        assertTrue(vm.state.value.isConnectedUnit)
    }

    @Test
    fun otherProductDropsConnectedMark() = runTest(dispatcher) {
        repository.answer = { Response.Success(info(id = it)) }
        val vm = viewModel(query = "107131", connectedId = 107131)
        advanceUntilIdle()

        vm.onQueryChange("555")
        vm.onSearch()
        advanceUntilIdle()

        assertFalse(vm.state.value.isConnectedUnit)
    }

    @Test
    fun invalidInputShowsErrorWithoutRequest() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onSearch()
        assertEquals(ProductQueryError.EMPTY, vm.state.value.queryError)
        vm.onQueryChange("12ab")
        assertNull(vm.state.value.queryError)
        vm.onSearch()
        assertEquals(ProductQueryError.NOT_DIGITS, vm.state.value.queryError)
        advanceUntilIdle()

        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun barcodeWithSpacesIsNormalized() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onQueryChange("4 607123 456789")
        vm.onSearch()
        advanceUntilIdle()

        assertEquals("4607123456789", vm.state.value.query)
        assertEquals(listOf(4607123456789L), repository.requests)
    }

    @Test
    fun notFoundAndNetworkAreDifferentErrors() = runTest(dispatcher) {
        repository.answer = { Response.Error(ProductNotFoundException(it)) }
        val vm = viewModel(query = "1234567")
        advanceUntilIdle()
        assertEquals(LCEState.Error(ProductInfoError.NotFound(1234567)), vm.state.value.result)

        repository.answer = { Response.Error(AppException(ApiError.NoConnection, IllegalStateException())) }
        vm.onRetry()
        advanceUntilIdle()
        assertEquals(LCEState.Error(ProductInfoError.Common(ApiError.NoConnection)), vm.state.value.result)
        assertEquals(listOf(1234567L, 1234567L), repository.requests)
    }

    @Test
    fun newSearchCancelsPrevious() = runTest(dispatcher) {
        val slow = CompletableDeferred<Response<ProductInfo>>()
        repository.answer = { if (it == 1L) slow.await() else Response.Success(info(id = it)) }
        val vm = viewModel(query = "1")
        advanceUntilIdle()

        vm.onQueryChange("2")
        vm.onSearch()
        advanceUntilIdle()
        slow.complete(Response.Success(info(id = 1)))
        advanceUntilIdle()

        val content = assertIs<LCEState.Content<ProductInfo, ProductInfoError>>(vm.state.value.result)
        assertEquals(2L, content.content.product?.id)
    }

    @Test
    fun expandAllTogglesAndResetsOnNewSearch() = runTest(dispatcher) {
        repository.answer = { Response.Success(info(id = it, components = 3)) }
        val vm = viewModel(query = "5")
        advanceUntilIdle()

        vm.onToggleComponent(1)
        assertEquals(setOf(1), vm.state.value.expanded)
        vm.onToggleAll()
        assertTrue(vm.state.value.allExpanded)
        vm.onToggleAll()
        assertTrue(vm.state.value.expanded.isEmpty())

        vm.onToggleComponent(0)
        vm.onSearch()
        advanceUntilIdle()
        assertTrue(vm.state.value.expanded.isEmpty())
    }

    @Test
    fun `granted camera opens the scanner, a scanned number is searched`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onScanClick()
        advanceUntilIdle()
        assertEquals(BarcodeScannerAccess.GRANTED, vm.state.value.scanner)

        vm.onScanned("4123700107131")
        advanceUntilIdle()

        assertNull(vm.state.value.scanner)
        assertEquals("4123700107131", vm.state.value.query)
        assertEquals(listOf(4123700107131L), repository.requests)
    }

    @Test
    fun `denied camera can be asked again, denied forever leads to settings`() = runTest(dispatcher) {
        permissions.camera = PermissionResult.Denied(canOpenSettings = false)
        val vm = viewModel()
        vm.onScanClick()
        advanceUntilIdle()
        assertEquals(BarcodeScannerAccess.DENIED, vm.state.value.scanner)

        permissions.camera = PermissionResult.Denied(canOpenSettings = true)
        vm.onScanAccessRetry()
        advanceUntilIdle()
        assertEquals(BarcodeScannerAccess.BLOCKED, vm.state.value.scanner)

        vm.onScanOpenSettings()
        assertEquals(1, permissions.settingsOpened)
        assertNull(vm.state.value.scanner)
    }

    @Test
    fun `manual input closes the scanner and focuses the field`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onScanClick()
        advanceUntilIdle()

        vm.onScanManual()
        advanceUntilIdle()

        assertNull(vm.state.value.scanner)
        assertEquals(listOf<ProductInfoEffect>(ProductInfoEffect.FocusQuery), effects)
    }
}

private class FakePermissions : PermissionsProvider {
    var camera: PermissionResult = PermissionResult.Granted
    var settingsOpened = 0

    override fun getController(): PermissionsController = error("not used")
    override fun getBlePermission(): List<Permission> = emptyList()
    override fun isLocationEnabled(): Boolean = true
    override fun openLocationSettings() = Unit
    override fun openBluetoothSettings() = Unit
    override fun openAppSettings() {
        settingsOpened++
    }

    override suspend fun requestCameraPermission(): PermissionResult = camera
}

private class FakeProductInfoRepository : ProductInfoRepository {
    var answer: suspend (Long) -> Response<ProductInfo> = { Response.Success(info(id = it)) }
    val requests = mutableListOf<Long>()

    override suspend fun find(id: Long): Response<ProductInfo> {
        requests += id
        return answer(id)
    }

    override suspend fun isEnabled(): Response<Boolean> = Response.Success(true)
}

private fun info(id: Long, components: Int = 0): ProductInfo = ProductInfo(
    product = ProductUnit(id = id, name = "Pandora DX-4GS", serial = null, factoryDate = null, firmwares = emptyList()),
    kit = if (components == 0) {
        null
    } else {
        ProductKit(
            name = "Kit",
            serial = null,
            packageDate = null,
            components = List(components) { ProductUnit(null, "C$it", null, null, emptyList()) },
        )
    },
)
