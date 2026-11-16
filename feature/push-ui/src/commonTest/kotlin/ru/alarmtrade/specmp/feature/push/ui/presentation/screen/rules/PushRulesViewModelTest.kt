package ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.push.PushType
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.push.domain.model.PushRules
import ru.alarmtrade.specmp.feature.push.domain.repository.PushRulesRepository
import ru.alarmtrade.specmp.feature.push.ui.presentation.model.PushRulesError

class PushRulesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val repository = object : PushRulesRepository {
        var rules: Response<PushRules> = Response.Success(PushRules(setOf(1, 2, 5)))
        var saveResponse: Response<Unit> = Response.Success(Unit)
        var saveGate: CompletableDeferred<Unit>? = null
        val saved = mutableListOf<PushRules>()

        override suspend fun rules(): Response<PushRules> = rules

        override suspend fun save(rules: PushRules): Response<Unit> {
            saved += rules
            saveGate?.await()
            return saveResponse
        }
    }

    private fun TestScope.viewModel() = PushRulesViewModel(repository, testDispatchers(dispatcher))
        .also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loaded rules are shown and nothing to save yet`() = runTest(dispatcher) {
        val vm = viewModel()

        assertEquals(LCEState.Content(PushRules(setOf(1, 2, 5))), vm.state.value.saved)
        assertEquals(PushRules(setOf(1, 2, 5)), vm.state.value.selected)
        assertFalse(vm.state.value.canSave)
    }

    @Test
    fun `toggling back to the server rules disables save`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onToggle(PushType.WARNING, true)
        assertTrue(vm.state.value.canSave)
        vm.onToggle(PushType.WARNING, false)

        assertFalse(vm.state.value.canSave)
    }

    @Test
    fun `save sends the selection with unsupported codes and reports success`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onToggle(PushType.CHAT, false)
        vm.onSave()

        assertEquals(listOf(PushRules(setOf(1, 2))), repository.saved)
        assertEquals(LCEState.Content(PushRules(setOf(1, 2))), vm.state.value.saved)
        assertFalse(vm.state.value.canSave)
        assertEquals(PushRulesEffect.Saved, vm.sideEffect.first())
    }

    @Test
    fun `failed save keeps the selection and reports the error`() = runTest(dispatcher) {
        repository.saveResponse = Response.Error(AppException(ApiError.NoConnection, IllegalStateException("offline")))
        val vm = viewModel()

        vm.onToggle(PushType.WARNING, true)
        vm.onSave()

        assertEquals(PushRules(setOf(1, 2, 4, 5)), vm.state.value.selected)
        assertTrue(vm.state.value.canSave)
        assertEquals(
            PushRulesEffect.SaveFailed(PushRulesError.Common(ApiError.NoConnection)),
            vm.sideEffect.first(),
        )
    }

    @Test
    fun `double tap on save sends once and toggles are ignored while saving`() = runTest(dispatcher) {
        repository.saveGate = CompletableDeferred()
        val vm = viewModel()

        vm.onToggle(PushType.WARNING, true)
        vm.onSave()
        vm.onSave()
        vm.onToggle(PushType.NOTIFICATION, false)
        assertTrue(vm.state.value.isSaving)
        repository.saveGate?.complete(Unit)

        assertEquals(1, repository.saved.size)
        assertEquals(PushRules(setOf(1, 2, 4, 5)), vm.state.value.selected)
    }

    @Test
    fun `load failure shows an error and retry reloads`() = runTest(dispatcher) {
        repository.rules = Response.Error(IllegalStateException("boom"))
        val vm = viewModel()

        assertEquals(LCEState.Error(PushRulesError.Failed), vm.state.value.saved)

        repository.rules = Response.Success(PushRules(setOf(4)))
        vm.onRetry()

        assertIs<LCEState.Content<PushRules, PushRulesError>>(vm.state.value.saved)
        assertEquals(PushRules(setOf(4)), vm.state.value.selected)
    }
}
