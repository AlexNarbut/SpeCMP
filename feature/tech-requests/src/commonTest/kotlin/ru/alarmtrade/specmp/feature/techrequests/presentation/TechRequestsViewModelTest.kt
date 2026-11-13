package ru.alarmtrade.specmp.feature.techrequests.presentation

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
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.PlatformError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.techrequests.domain.list.TechRequestList
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestException
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestFailure
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestField
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestPage
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestSubmitStep
import ru.alarmtrade.specmp.feature.techrequests.domain.repository.TechRequestsRepository
import ru.alarmtrade.specmp.feature.techrequests.domain.techRequest
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.DraftPrefill
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.RequestAction
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.SubmitStage
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestSending
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsDialog
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsEffect
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsState
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class TechRequestsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeDeviceSessionRepository()
    private val repository = FakeTechRequestsRepository()
    private val account = FakeProfileRepository()
    private val viewModels = mutableListOf<TechRequestsViewModel>()
    private val effects = mutableListOf<TechRequestsEffect>()

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

    private fun TestScope.viewModel(withDevice: Boolean = true, prefill: DraftPrefill? = null): TechRequestsViewModel {
        val session = testDeviceSession(
            sessionRepository,
            dispatcher = dispatcher,
            transport = ConnectionTransport.BLE.takeIf { withDevice },
        )
        val vm = TechRequestsViewModel(
            session,
            repository,
            account,
            testDispatchers(dispatcher),
            RussianLocale,
            prefill,
        )
            .also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm
    }

    private fun TechRequestsViewModel.listIds(): List<Long> =
        assertIs<LCEState.Content<TechRequestList, TechRequestsError>>(state.value.requests).content.items.map { it.id }

    private fun TestScope.fill(vm: TechRequestsViewModel, issue: String = "тест") {
        vm.onNameChange("Иван")
        vm.onPhoneChange("79001234567")
        vm.onIssueChange(issue)
        runCurrent()
    }

    @Test
    fun firstPageThenNextUntilEnd() = test {
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(3), techRequest(2)), hasMore = true))
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(1)), hasMore = false))
        val vm = viewModel()

        assertEquals(listOf(3L, 2L), vm.listIds())
        vm.onLoadMore()
        runCurrent()

        assertEquals(listOf(3L, 2L, 1L), vm.listIds())
        assertFalse(vm.state.value.canLoadMore)
        vm.onLoadMore()
        runCurrent()
        assertEquals(2, repository.pageCalls)
    }

    @Test
    fun firstPageErrorIsFullScreenAndRetryLoads() = test {
        repository.pages += Response.Error(AppException(ApiError.NoConnection, IllegalStateException()))
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(1)), hasMore = false))
        val vm = viewModel()

        assertEquals(
            TechRequestsError.Common(ApiError.NoConnection),
            assertIs<LCEState.Error<TechRequestsError>>(vm.state.value.requests).error,
        )
        vm.onRetry()
        runCurrent()
        assertEquals(listOf(1L), vm.listIds())
    }

    @Test
    fun nextPageErrorKeepsListAndRetries() = test {
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(2)), hasMore = true))
        repository.pages += Response.Error(IllegalStateException("boom"))
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(1)), hasMore = false))
        val vm = viewModel()

        vm.onLoadMore()
        runCurrent()
        assertEquals(TechRequestsError.Unknown, vm.state.value.moreError)
        assertEquals(listOf(2L), vm.listIds())

        vm.onRetryMore()
        runCurrent()
        assertEquals(listOf(2L, 1L), vm.listIds())
        assertNull(vm.state.value.moreError)
    }

    @Test
    fun withoutDeviceListWorksButCreateExplains() = test {
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        val vm = viewModel(withDevice = false)

        assertFalse(vm.state.value.canCreate)
        vm.onCreateClick()
        runCurrent()

        assertEquals(TechRequestsDialog.NoDevice, vm.state.value.dialog)
        assertFalse(vm.state.value.formOpen)
    }

    @Test
    fun deviceIsShownForAttachment() = test {
        val vm = viewModel()

        assertTrue(vm.state.value.canCreate)
        assertEquals(sessionRepository.device.systemName, vm.state.value.device?.model)
    }

    @Test
    fun profileContactIsPrefilledAndNormalized() = test {
        account.profile = Response.Success(AccountProfile("Иван", "ivan@mail.ru", "8 (900) 123-45-67"))
        val vm = viewModel()

        assertEquals("Иван", vm.state.value.form.name)
        assertEquals("79001234567", vm.state.value.form.phoneDigits)
    }

    @Test
    fun errorsAppearOnlyAfterFirstAttempt() = test {
        account.profile = Response.Success(AccountProfile("", "ivan@mail.ru"))
        val vm = viewModel()
        vm.onIssueChange("")
        runCurrent()
        assertTrue(vm.state.value.form.errors.isEmpty())

        vm.onSubmit()
        runCurrent()

        assertEquals(
            setOf(TechRequestField.Name, TechRequestField.Phone, TechRequestField.Issue),
            vm.state.value.form.errors,
        )
        assertEquals(TechRequestSending.Idle, vm.state.value.sending)
        assertTrue(repository.submitted.isEmpty())

        fill(vm)
        assertTrue(vm.state.value.form.errors.isEmpty())
    }

    @Test
    fun issueIsCappedAtLimit() = test {
        val vm = viewModel()

        vm.onIssueChange("a".repeat(TechRequestDraft.ISSUE_MAX_LENGTH + 10))
        runCurrent()

        assertEquals(TechRequestDraft.ISSUE_MAX_LENGTH, vm.state.value.form.issue.length)
    }

    @Test
    fun submitGoesThroughStagesAndHighlightsCreated() = test {
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(1)), hasMore = false))
        val vm = viewModel()
        fill(vm)
        repository.submitFlow = flow {
            emit(Response.Success(TechRequestSubmitStep.ReadingDevice))
            emit(Response.Success(TechRequestSubmitStep.Sending))
            emit(Response.Success(TechRequestSubmitStep.Created(techRequest(10))))
        }

        vm.onSubmit()
        runCurrent()

        assertEquals(TechRequestSending.Sent(10, "79001234567"), vm.state.value.sending)
        assertEquals(listOf(10L, 1L), vm.listIds())
        assertEquals(10L, vm.state.value.highlightedId)
        assertEquals("тест", repository.submitted.single().issue)

        advanceTimeBy(4_001)
        assertNull(vm.state.value.highlightedId)

        vm.onSentDone()
        runCurrent()
        assertEquals("", vm.state.value.form.issue)
        assertEquals(TechRequestSending.Idle, vm.state.value.sending)
    }

    @Test
    fun limitFailureKeepsDraft() = test {
        val vm = viewModel()
        fill(vm)
        repository.submitFlow = flowOf(
            Response.Success(TechRequestSubmitStep.ReadingDevice),
            Response.Error(TechRequestException(TechRequestFailure.Limit, IllegalStateException())),
        )

        vm.onSubmit()
        runCurrent()

        assertEquals(TechRequestSending.Failed(TechRequestsError.Limit), vm.state.value.sending)
        assertEquals("тест", vm.state.value.form.issue)
    }

    @Test
    fun doubleTapSendsOnce() = test {
        val vm = viewModel()
        fill(vm)
        repository.submitFlow = flow {
            emit(Response.Success(TechRequestSubmitStep.ReadingDevice))
            awaitCancellation()
        }

        vm.onSubmit()
        vm.onSubmit()
        runCurrent()

        assertEquals(1, repository.submitted.size)
    }

    @Test
    fun connectionLossDuringSendingFails() = test {
        val vm = viewModel()
        fill(vm)
        repository.submitFlow = flow {
            emit(Response.Success(TechRequestSubmitStep.ReadingDevice))
            awaitCancellation()
        }
        vm.onSubmit()
        runCurrent()
        assertEquals(TechRequestSending.InProgress(SubmitStage.ReadingDevice), vm.state.value.sending)

        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()

        assertEquals(
            TechRequestSending.Failed(TechRequestsError.Common(PlatformError.DeviceDisconnected)),
            vm.state.value.sending,
        )
    }

    @Test
    fun stoppingSendingReloadsList() = test {
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(1)), hasMore = false))
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(2), techRequest(1)), hasMore = false))
        val vm = viewModel()
        fill(vm)
        repository.submitFlow = flow {
            emit(Response.Success(TechRequestSubmitStep.Sending))
            awaitCancellation()
        }
        vm.onSubmit()
        runCurrent()

        vm.onCancelSending()
        runCurrent()

        assertEquals(TechRequestSending.Idle, vm.state.value.sending)
        assertEquals(TechRequestsEffect.SendingStopped, effects.last())
        assertEquals(listOf(2L, 1L), vm.listIds())
    }

    @Test
    fun closeRequestAfterConfirmation() = test {
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(5)), hasMore = false))
        val vm = viewModel()

        vm.onRequestAction(RequestAction.Close, 5)
        runCurrent()
        assertEquals(TechRequestsDialog.Confirm(RequestAction.Close, 5), vm.state.value.dialog)
        vm.onConfirmAction()
        runCurrent()

        assertEquals(listOf("close" to 5L), repository.actions)
        assertNull(vm.state.value.pendingActionId)
        assertEquals(TechRequestsEffect.ActionDone(RequestAction.Close), effects.last())
        assertEquals(TechRequestStatus.FINISHED_BY_USER, vm.state.value.statusOf(5))
    }

    @Test
    fun failedCancelKeepsStatusAndReports() = test {
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(5)), hasMore = false))
        repository.actionResult = Response.Error(AppException(ApiError.NoConnection, IllegalStateException()))
        val vm = viewModel()

        vm.onRequestAction(RequestAction.Cancel, 5)
        vm.onConfirmAction()
        runCurrent()

        assertEquals(TechRequestStatus.OPEN, vm.state.value.statusOf(5))
        assertEquals(
            TechRequestsEffect.ActionFailed(RequestAction.Cancel, TechRequestsError.Common(ApiError.NoConnection)),
            effects.last(),
        )
    }

    @Test
    fun updatesAreMergedIntoList() = test {
        repository.pages += Response.Success(TechRequestPage(listOf(techRequest(5)), hasMore = false))
        val vm = viewModel()

        repository.updates.emit(Response.Success(listOf(techRequest(5, TechRequestStatus.IN_PROCESS))))
        runCurrent()

        assertEquals(TechRequestStatus.IN_PROCESS, vm.state.value.statusOf(5))
    }

    @Test
    fun prefillOpensFormWithErrorTextAndOrigin() = test {
        val vm = viewModel(prefill = DraftPrefill("Процедура Clone", "Процедура Clone: ошибка записи"))

        assertTrue(vm.state.value.formOpen)
        assertEquals("Процедура Clone", vm.state.value.origin)
        assertEquals("Процедура Clone: ошибка записи ", vm.state.value.form.issue)
        assertEquals("Иван", vm.state.value.form.name)

        vm.onDiscardDraft()
        runCurrent()
        assertNull(vm.state.value.origin)
    }

    @Test
    fun closingFormWithDraftAsksFirst() = test {
        val vm = viewModel()
        vm.onCreateClick()
        runCurrent()
        vm.onIssueChange("черновик")
        runCurrent()

        vm.onCloseForm()
        runCurrent()
        assertEquals(TechRequestsDialog.DiscardDraft, vm.state.value.dialog)
        assertTrue(vm.state.value.formOpen)

        vm.onDiscardDraft()
        runCurrent()
        assertFalse(vm.state.value.formOpen)
        assertEquals("", vm.state.value.form.issue)
        advanceUntilIdle()
    }
}

private fun TechRequestsState.statusOf(id: Long): TechRequestStatus? =
    (requests as? LCEState.Content)?.content?.find(id)?.status

private object RussianLocale : AppLocaleStore {
    override fun current(): AppLocale = AppLocale.Russian

    override fun save(locale: AppLocale) = Unit
}

private class FakeProfileRepository : AccountRepository {
    var profile: Response<AccountProfile> = Response.Success(AccountProfile("Иван", "ivan@mail.ru", "+79001234567"))

    override suspend fun currentProfile(): Response<AccountProfile> = profile
}

private class FakeTechRequestsRepository : TechRequestsRepository {
    val updates = MutableSharedFlow<Response<List<TechRequest>>>()
    val pages = ArrayDeque<Response<TechRequestPage>>()
    var pageCalls = 0
    var submitFlow: Flow<Response<TechRequestSubmitStep>> = flowOf()
    val submitted = mutableListOf<TechRequestDraft>()
    var actionResult: Response<Unit> = Response.Success(Unit)
    val actions = mutableListOf<Pair<String, Long>>()

    override fun updates(): Flow<Response<List<TechRequest>>> = updates

    override suspend fun loadFirstPage(): Response<TechRequestPage> = nextPage()

    override suspend fun loadNextPage(): Response<TechRequestPage> = nextPage()

    private fun nextPage(): Response<TechRequestPage> {
        pageCalls++
        return pages.removeFirstOrNull() ?: Response.Success(TechRequestPage(emptyList(), hasMore = false))
    }

    override fun submit(draft: TechRequestDraft): Flow<Response<TechRequestSubmitStep>> {
        submitted += draft
        return submitFlow
    }

    override suspend fun cancel(id: Long): Response<Unit> {
        actions += "cancel" to id
        return actionResult
    }

    override suspend fun close(id: Long): Response<Unit> {
        actions += "close" to id
        return actionResult
    }
}
