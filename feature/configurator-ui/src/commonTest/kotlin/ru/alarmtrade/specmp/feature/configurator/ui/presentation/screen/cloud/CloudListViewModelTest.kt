package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.FakePaneWidthStore
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeCloudRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloudSummary

class CloudListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val cloud = FakeCloudRepository()
    private val best = cloudSummary(1, rating = 5, day = 1)
    private val newest = cloudSummary(2, deviceName = "DX-4GS", day = 9)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(deviceName: String = ""): CloudListViewModel {
        val vm = CloudListViewModel(7, deviceName, cloud, FakePaneWidthStore(), testDispatchers(dispatcher))
        backgroundScope.launch { vm.state.collect {} }
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `list loads for the car and is sorted best first`() = runTest(dispatcher) {
        cloud.list = Response.Success(listOf(newest, best))
        val vm = viewModel()

        assertEquals(listOf(7 to ""), cloud.byCarCalls)
        assertEquals(listOf(best, newest), vm.state.value.shown)
        assertEquals(listOf("DX-4GS", "DX-9X"), vm.state.value.models)
    }

    @Test
    fun `order and model filter recompute the shown list`() = runTest(dispatcher) {
        cloud.list = Response.Success(listOf(newest, best))
        val vm = viewModel()

        vm.onOrder(CloudConfigurationOrder.NEWEST)
        assertEquals(listOf(newest, best), vm.state.value.shown)

        vm.onModelFilter("DX-9X")
        assertEquals(listOf(best), vm.state.value.shown)
    }

    @Test
    fun `unit model is the initial filter`() = runTest(dispatcher) {
        cloud.list = Response.Success(listOf(newest, best))
        val vm = viewModel(deviceName = "DX-4GS")

        assertEquals(listOf(7 to "DX-4GS"), cloud.byCarCalls)
        assertEquals(listOf(newest), vm.state.value.shown)
    }

    @Test
    fun `retry after error loads again`() = runTest(dispatcher) {
        cloud.list = Response.Error(IllegalStateException("offline"))
        val vm = viewModel()
        assertIs<LCEState.Error<*>>(vm.state.value.list)

        cloud.list = Response.Success(listOf(best))
        vm.onRetry()

        assertEquals(LCEState.Content(listOf(best)), vm.state.value.list)
        assertEquals(listOf(best), vm.state.value.shown)
    }

    @Test
    fun `select keeps the chosen id`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onSelect(2)

        assertEquals(2, vm.state.value.selectedId)
    }
}
