package ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsWrites
import ru.alarmtrade.specmp.feature.device.settings.domain.service.SettingsWrite
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.availability
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.diffTo
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.snapshotOf
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.topics
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantWriteException
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.Availability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BYPASS_VARIANT_SETTING_ID
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.PendingHandoff
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultContent
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultRequest
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingsSourceKind
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StepTitle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantAutostartRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantDeviceRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantFirmwareRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantSettingsRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantStateRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.service.AssistantJournal
import ru.alarmtrade.specmp.feature.mountassistant.presentation.navigation.AssistantNavigationTarget

/** Что и когда писать в журнал, решает VM; журнал переживает экран и прошивку. */
class MountAssistantViewModel(
    deviceSession: DeviceSession,
    private val journal: AssistantJournal,
    private val devices: AssistantDeviceRepository,
    private val firmware: AssistantFirmwareRepository,
    private val autostart: AssistantAutostartRepository,
    private val vehicleState: AssistantStateRepository,
    private val settings: AssistantSettingsRepository,
    private val sessions: DeviceSettingsSessionController,
    private val settingsWrites: DeviceSettingsWrites,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
    private val clock: Clock,
) : BaseDeviceViewModel<MountAssistantState, MountAssistantEffect>(deviceSession, MountAssistantState()) {

    override suspend fun onStart() {
        // Новая VM после загрузчика: возврат с экрана прошивки разбирается, когда блок снова на связи.
        val pending = journal.state.value.pendingHandoff != null
        reduce { copy(awaitingReturn = pending) }
        journal.state.collectIntoState { copy(journal = it) }
        devices.facts().collectIntoState(onError = { this }) { facts -> copy(facts = facts ?: this.facts) }
        // Записи редактора настроек, пока он открыт поверх ассистента: иначе возврат скажет «блок не изменился».
        safeLaunch {
            settingsWrites.writes().collect { write -> write.getValueIfSuccess()?.let(::onSettingsWritten) }
        }
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost(::failRunning)
        pinCooldown.lockedFor(
            select {
                pin?.systemId
            },
        ).collectIntoState { lock -> copy(pin = pin?.copy(lockedFor = lock)) }
    }

    // Редактор настроек к этому моменту закрыт (он выше в стеке): сессию блока, открытую ассистентом, убираем.
    override fun onCleared() {
        sessions.discardSession(SettingsSource.Device)
        super.onCleared()
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        journal.bind(device.systemId)
        if (journal.state.value.entries.isEmpty()) journal.append(listOf(JournalEntry.Greeting(nextId())))
        val facts = awaitFacts() ?: return
        reduce { copy(facts = facts) }
        if (journal.state.value.progress.autostart == null && facts.readsAutostart()) readAutostartQuietly()
        if (currentState.awaitingReturn) handleReturn()
    }

    private suspend fun awaitFacts(): DeviceFacts? = withTimeoutOrNull(FACTS_TIMEOUT) {
        devices.facts().mapNotNull { (it as? Response.Success)?.value }.first()
    }

    private fun DeviceFacts.readsAutostart(): Boolean =
        (has(AssistantCapability.BYPASS) || has(AssistantCapability.CLONE)) && !factoryCan

    private suspend fun readAutostartQuietly() {
        val response = withContext(dispatchers.io) { autostart.status() }
        if (response is Response.Success) journal.updateProgress { it.copy(autostart = response.value) }
    }

    fun onActionClick(action: AssistantAction) {
        val facts = currentState.facts ?: return
        when (val available = availability(action, facts, journal.state.value.progress)) {
            Availability.Hidden -> Unit

            is Availability.Blocked -> {
                journal.append(listOf(JournalEntry.Notice(nextId(), available.reason)))
                if (available.fix != null) reduce { copy(level = DockLevel.Home) }
            }

            Availability.Available -> perform(action)
        }
    }

    fun onTopicClick(topic: AssistantTopic) {
        val facts = currentState.facts ?: return
        val actions = topics(facts, journal.state.value.progress).firstOrNull { it.topic == topic }?.actions ?: return
        val single = actions.singleOrNull()
        if (single != null) {
            onActionClick(single)
        } else {
            clearSearch()
            openTopic(topic, pulse = false)
        }
    }

    fun onModuleArchiveClick(moduleId: Long) {
        val module = journal.state.value.progress.firmware?.modules?.firstOrNull { it.id == moduleId } ?: return
        if (allowed(AssistantAction.FIRMWARE_ARCHIVE)) archiveHandoff(module, withStep = true)
    }

    /** Обновить или перепрошить модуль последней версией: экран прошивок сразу предложит загрузчик. */
    fun onModuleFlashClick(moduleId: Long) {
        val module = journal.state.value.progress.firmware?.modules?.firstOrNull { it.id == moduleId } ?: return
        if (module.latest == null || !allowed(AssistantAction.FIRMWARE_ARCHIVE)) return
        val title = if (module.updateAvailable) StepTitle.Action(AssistantAction.FIRMWARE_UPDATE) else StepTitle.Reflash
        journal.append(listOf(JournalEntry.Step(nextId(), title, clock.now())))
        beginHandoff(AssistantAction.FIRMWARE_UPDATE, AssistantNavigationTarget.FirmwareFlash(module.id))
    }

    fun onTopicBack() {
        reduce { copy(level = DockLevel.Home) }
    }

    fun onCanUpdateClick() {
        onActionClick(AssistantAction.CAN_UPDATE)
    }

    fun onQueryChange(query: String) {
        reduce { copy(query = query, dockCollapsed = false) }
        searchLive(query.trim())
    }

    /** [topic] — тема, найденная по запросу: без настроек поиск открывает её, а не карточку в ленте. */
    fun onSearchSubmit(topic: AssistantTopic?) {
        val query = currentState.query.trim()
        val facts = currentState.facts
        if (query.isEmpty() || facts == null) return
        val find = availability(AssistantAction.SETTINGS_FIND, facts, journal.state.value.progress)
        if (find is Availability.Blocked) {
            onActionClick(AssistantAction.SETTINGS_FIND)
            return
        }
        clearSearch()
        if (find == Availability.Hidden || (topic != null && currentState.liveSettings.isEmpty())) {
            if (topic != null) openTopic(topic, pulse = false)
            return
        }
        val stepId = nextId()
        journal.append(
            listOf(
                JournalEntry.Step(stepId, StepTitle.Search(query), clock.now()),
                JournalEntry.Result(stepId + 1, ResultRequest.Settings(query), ResultContent.Loading),
            ),
        )
        runResult(stepId + 1, ResultRequest.Settings(query))
    }

    /** Настройка из поиска в пульте: карточка в ленте сразу с правкой. */
    fun onSettingPick(settingId: Int) {
        val facts = currentState.facts ?: return
        val setting = currentState.liveSettings.firstOrNull { it.id == settingId } ?: return
        if (availability(AssistantAction.SETTINGS_FIND, facts, journal.state.value.progress) !=
            Availability.Available
        ) {
            onActionClick(AssistantAction.SETTINGS_FIND)
            return
        }
        clearSearch()
        val stepId = nextId()
        val request = ResultRequest.Settings(query = "", settingId = settingId)
        journal.append(
            listOf(
                JournalEntry.Step(stepId, StepTitle.Setting(setting.name), clock.now()),
                JournalEntry.Result(stepId + 1, request, ResultContent.Loading),
            ),
        )
        runResult(stepId + 1, request)
    }

    fun onEditStart(entryId: Long, settingId: Int) {
        updateSettings(entryId) { data ->
            val setting = data.found.firstOrNull { it.id == settingId }
            if (setting?.editor == null) data else data.copy(editing = settingId, draft = setting.value)
        }
    }

    fun onDraftChange(entryId: Long, value: AssistantSettingValue) {
        updateSettings(entryId) { it.copy(draft = value) }
    }

    fun onEditCancel(entryId: Long) {
        updateSettings(entryId) { it.copy(editing = null, draft = null) }
    }

    fun onEditNext(entryId: Long) {
        val data = settingsData(entryId) ?: return
        val setting = data.found.firstOrNull { it.id == data.editing } ?: return
        val from = setting.value ?: return
        val to = data.draft?.takeIf { it != from } ?: return
        updateSettings(entryId) { it.copy(editing = null, draft = null) }
        val confirmId = nextId()
        journal.append(listOf(JournalEntry.Confirm(confirmId, setting, from, to, WriteStatus.PENDING)))
        launchOnDevice(key = "unsaved:$confirmId") {
            val others = withContext(dispatchers.io) { settings.otherUnsaved(setting.id) }.getValueIfSuccess() ?: 0
            updateConfirm(confirmId) { it.copy(otherUnsaved = others) }
        }
    }

    /** «Записать» из ожидания — значение в сессию и запись; «Повторить» после ошибки — только запись сессии. */
    fun onWriteConfirm(entryId: Long) {
        val confirm = confirm(entryId) ?: return
        if (confirm.status != WriteStatus.PENDING && confirm.status != WriteStatus.ERROR) return
        val retry = confirm.status == WriteStatus.ERROR
        updateConfirm(entryId) { it.copy(status = WriteStatus.WRITING, failure = null, pinSkipped = false) }
        val job = launchOnDevice(key = "write:$entryId") {
            val response = withContext(dispatchers.io) {
                if (retry) settings.save() else settings.write(confirm.setting.id, confirm.to)
            }
            finishWrite(entryId, response)
        }
        if (job ==
            null
        ) {
            updateConfirm(entryId) { it.copy(status = WriteStatus.ERROR, failure = ResultFailure.LinkLost) }
        }
    }

    /** Отмена после попытки записи возвращает старое значение в сессию: следующая запись не унесёт его в блок. */
    fun onWriteCancel(entryId: Long) {
        val confirm = confirm(entryId) ?: return
        if (confirm.status == WriteStatus.WRITING || confirm.status == WriteStatus.DONE) return
        val dirty = confirm.status != WriteStatus.PENDING || confirm.pinSkipped
        updateConfirm(entryId) { it.copy(status = WriteStatus.CANCELLED) }
        if (!dirty) return
        launchOnDevice {
            withContext(dispatchers.io) { settings.revert(confirm.setting.id, confirm.from) }
                .onError { logger.w(it) { "Assistant could not revert setting ${confirm.setting.id}" } }
        }
    }

    fun onPinSubmit(pin: String) {
        val prompt = currentState.pin ?: return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        if (pinCooldown.remainingNow(prompt.systemId) > Duration.ZERO) return
        reduce { copy(pin = prompt.copy(sending = true)) }
        val job = launchOnDevice(key = "write:${prompt.entryId}") {
            val saved = withContext(dispatchers.io) { deviceSession.setServicePin(servicePin) }
            val response = if (saved is Response.Error) saved else withContext(dispatchers.io) { settings.save() }
            reduce { copy(pin = this.pin?.copy(sending = false)) }
            finishWrite(prompt.entryId, response)
        }
        if (job == null) {
            reduce { copy(pin = null) }
            updateConfirm(prompt.entryId) { it.copy(status = WriteStatus.ERROR, failure = ResultFailure.LinkLost) }
        }
    }

    fun onPinDismiss() {
        val prompt = currentState.pin ?: return
        reduce { copy(pin = null) }
        updateConfirm(prompt.entryId) { it.copy(status = WriteStatus.PENDING, pinSkipped = true) }
    }

    fun onOpenInSettings(settingId: Int) {
        if (!allowed(AssistantAction.SETTINGS_ALL)) return
        journal.append(listOf(JournalEntry.Step(nextId(), StepTitle.OpenInSettings, clock.now())))
        beginHandoff(AssistantAction.SETTINGS_ALL, AssistantNavigationTarget.SettingsEditor(focusSettingId = settingId))
    }

    /** Выбор источника продолжается в редакторе: он открывается сразу с меню «Загрузить из…». */
    fun onSourcePick(entryId: Long, kind: SettingsSourceKind) {
        val facts = currentState.facts ?: return
        if (kind == SettingsSourceKind.CLOUD && !facts.online) return
        if (!allowed(AssistantAction.SETTINGS_LOAD)) return
        journal.update(entryId) { (it as? JournalEntry.SourcePicker)?.copy(used = kind) ?: it }
        beginHandoff(AssistantAction.SETTINGS_LOAD, AssistantNavigationTarget.SettingsEditor(openLoad = true))
    }

    fun onRetry(entryId: Long) {
        val entry = journal.state.value.entries.firstOrNull { it.id == entryId } as? JournalEntry.Result ?: return
        journal.update(entryId) { (it as? JournalEntry.Result)?.copy(content = ResultContent.Loading) ?: it }
        runResult(entryId, entry.request)
    }

    /** Экран снова виден: если ассистент ждал возврата с другого экрана — сравнить блок «до» и «после». */
    fun onScreenResumed() {
        handleReturn()
    }

    fun onDockToggle() {
        reduce { copy(dockCollapsed = !dockCollapsed) }
    }

    fun onFeedScrolledUp() {
        if (currentState.query.isEmpty()) reduce { copy(dockCollapsed = true) }
    }

    fun onHeaderToggle(collapsed: Boolean) {
        reduce { copy(header = if (collapsed) HeaderMode.OPEN else HeaderMode.CLOSED) }
    }

    private fun perform(action: AssistantAction) {
        reduce { copy(query = "", header = HeaderMode.AUTO) }
        when (action) {
            AssistantAction.SETTINGS_FIND -> {
                reduce { copy(dockCollapsed = false) }
                postSideEffect(MountAssistantEffect.FocusSearch)
            }

            AssistantAction.FIRMWARE_CHECK -> startResult(action, ResultRequest.Firmware)

            AssistantAction.STATE -> startResult(action, ResultRequest.State)

            AssistantAction.AUTOSTART_STATUS -> startResult(action, ResultRequest.Autostart)

            AssistantAction.SETTINGS_LOAD -> {
                val stepId = nextId()
                journal.append(
                    listOf(
                        JournalEntry.Step(stepId, StepTitle.Action(action), clock.now()),
                        JournalEntry.SourcePicker(stepId + 1),
                    ),
                )
                openTopic(AssistantTopic.SETTINGS, pulse = false)
            }

            AssistantAction.FIRMWARE_ARCHIVE -> openArchive()

            else -> handoff(action)
        }
    }

    /** Один модуль — сразу его архив; несколько — карточка прошивки с выбором; не проверяли — сначала проверка. */
    private fun openArchive() {
        val check = journal.state.value.progress.firmware
        val single = check?.modules?.singleOrNull()
        when {
            single != null -> archiveHandoff(single, withStep = true)

            check == null || check.modules.isEmpty() -> {
                startResult(AssistantAction.FIRMWARE_ARCHIVE, ResultRequest.Firmware, forArchive = true)
            }

            else -> {
                val stepId = nextId()
                journal.append(
                    listOf(
                        JournalEntry.Step(stepId, StepTitle.Action(AssistantAction.FIRMWARE_ARCHIVE), clock.now()),
                        JournalEntry.Result(
                            stepId + 1,
                            ResultRequest.Firmware,
                            ResultContent.FirmwareData(check, currentState.facts?.can),
                        ),
                    ),
                )
                openTopic(AssistantTopic.FIRMWARE, pulse = false)
            }
        }
    }

    private fun archiveHandoff(module: FirmwareModuleCheck, withStep: Boolean) {
        if (withStep) {
            val title = StepTitle.Action(AssistantAction.FIRMWARE_ARCHIVE)
            journal.append(listOf(JournalEntry.Step(nextId(), title, clock.now())))
        }
        val target = AssistantNavigationTarget.FirmwareArchive(module.id, module.role, module.installed)
        beginHandoff(AssistantAction.FIRMWARE_ARCHIVE, target)
    }

    private fun handoff(action: AssistantAction) {
        val target = action.target() ?: return
        journal.append(listOf(JournalEntry.Step(nextId(), StepTitle.Action(action), clock.now())))
        beginHandoff(action, target)
    }

    private fun beginHandoff(action: AssistantAction, target: AssistantNavigationTarget) {
        val facts = currentState.facts ?: return
        journal.beginHandoff(PendingHandoff(action, snapshotOf(facts, journal.state.value.progress)))
        postSideEffect(MountAssistantEffect.Navigate(target))
    }

    /** Доступно — `true`; заблокировано — заметка в ленту (как у плитки). */
    private fun allowed(action: AssistantAction): Boolean {
        val facts = currentState.facts ?: return false
        val available = availability(action, facts, journal.state.value.progress)
        if (available is Availability.Blocked) onActionClick(action)
        return available == Availability.Available
    }

    private fun searchLive(query: String) {
        val facts = currentState.facts
        val searchable = facts != null && query.length >= LIVE_MIN_LENGTH &&
            availability(AssistantAction.SETTINGS_FIND, facts, journal.state.value.progress) == Availability.Available
        if (!searchable) {
            cancelLaunched(LIVE_KEY)
            reduce { copy(liveSettings = emptyList()) }
            return
        }
        launchOnDevice(key = LIVE_KEY) {
            delay(LIVE_DEBOUNCE)
            val found = withContext(dispatchers.io) { settings.find(query) }.getValueIfSuccess().orEmpty()
            reduce { if (this.query.trim() == query) copy(liveSettings = found) else this }
        }
    }

    // Настройки читаются с блока — только если они ассистенту доступны; без них карточка статуса остаётся без строки.
    private suspend fun bypassVariant(): FoundSetting? {
        val facts = currentState.facts ?: return null
        val find = availability(AssistantAction.SETTINGS_FIND, facts, journal.state.value.progress)
        if (find != Availability.Available) return null
        return withContext(dispatchers.io) { settings.setting(BYPASS_VARIANT_SETTING_ID) }.getValueIfSuccess()
    }

    private fun onSettingsWritten(write: SettingsWrite) {
        if (write.source == SettingsSource.Device) countWritten(write.settingNames, write.settingNames.size)
    }

    private fun countWritten(names: List<String>, count: Int) {
        journal.updateProgress {
            it.copy(settingsChanged = it.settingsChanged + count, changedSettings = it.changedSettings + names)
        }
    }

    private fun clearSearch() {
        cancelLaunched(LIVE_KEY)
        reduce { copy(query = "", liveSettings = emptyList(), header = HeaderMode.AUTO) }
    }

    private fun finishWrite(entryId: Long, response: Response<Unit>) {
        when (response) {
            is Response.Success -> {
                updateConfirm(entryId) { it.copy(status = WriteStatus.DONE, failure = null) }
                val name = (
                    journal.state.value.entries.firstOrNull {
                        it.id == entryId
                    } as? JournalEntry.Confirm
                    )?.setting?.name
                countWritten(listOfNotNull(name), count = 1)
                reduce { copy(pin = null) }
                openTopic(AssistantTopic.SETTINGS, pulse = true)
            }

            is Response.Error -> if ((response.exception as? AssistantWriteException)?.failure ==
                WriteFailure.WRONG_PIN
            ) {
                updateConfirm(entryId) { it.copy(status = WriteStatus.PIN) }
                showPin(entryId)
            } else {
                updateConfirm(entryId) { it.copy(status = WriteStatus.ERROR, failure = response.exception.toFailure()) }
                reduce { copy(pin = null) }
            }
        }
    }

    /** Повторный неверный PIN (диалог уже открыт) — блокировка ввода, как у входа в режим программирования. */
    private fun showPin(entryId: Long) {
        val systemId = currentState.facts?.systemId ?: return
        val wrong = currentState.pin != null
        if (wrong) pinCooldown.start(systemId)
        reduce {
            copy(
                pin = PinPrompt(
                    entryId = entryId,
                    systemId = systemId,
                    wrongCount = (pin?.wrongCount ?: 0) + if (wrong) 1 else 0,
                    lockedFor = pin?.lockedFor,
                ),
            )
        }
    }

    private fun Throwable.toFailure(): ResultFailure {
        val cause = (this as? AssistantWriteException)?.cause ?: this
        return cause.appError?.let(ResultFailure::Common) ?: ResultFailure.Failed
    }

    private fun settingsData(entryId: Long): ResultContent.SettingsData? =
        ((journal.state.value.entries.firstOrNull { it.id == entryId } as? JournalEntry.Result)?.content)
            as? ResultContent.SettingsData

    private fun updateSettings(entryId: Long, transform: (ResultContent.SettingsData) -> ResultContent.SettingsData) {
        journal.update(entryId) { entry ->
            val result = entry as? JournalEntry.Result
            val data = result?.content as? ResultContent.SettingsData
            if (result == null || data == null) entry else result.copy(content = transform(data))
        }
    }

    private fun confirm(entryId: Long): JournalEntry.Confirm? =
        journal.state.value.entries.firstOrNull { it.id == entryId } as? JournalEntry.Confirm

    private fun updateConfirm(entryId: Long, transform: (JournalEntry.Confirm) -> JournalEntry.Confirm) {
        journal.update(entryId) { (it as? JournalEntry.Confirm)?.let(transform) ?: it }
    }

    private fun handleReturn() {
        val pending = journal.state.value.pendingHandoff ?: return
        reduce { copy(awaitingReturn = false) }
        val quiet = pending.action in QuietReturns
        val cardId = if (quiet) null else checkingCardFor(pending.action)
        val job = launchOnDevice(key = RETURN_KEY) {
            val facts = awaitFacts() ?: return@launchOnDevice
            reduce { copy(facts = facts) }
            rereadAfter(pending, facts)
            val changes = pending.before.diffTo(snapshotOf(facts, journal.state.value.progress))
            when {
                cardId != null -> journal.update(cardId) { JournalEntry.Return(cardId, pending.action, changes) }
                changes.isNotEmpty() -> journal.append(listOf(JournalEntry.Return(nextId(), pending.action, changes)))
            }
            journal.endHandoff()
            if (cardId != null || changes.isNotEmpty()) pulseTopic(pending.action.topic)
        }
        if (job == null) reduce { copy(awaitingReturn = true) }
    }

    /** Повторный вызов (возврат на экран и переподключение сразу) не плодит карточек «Перечитываем блок». */
    private fun checkingCardFor(action: AssistantAction): Long {
        val last = journal.state.value.entries.lastOrNull()
        if (last is JournalEntry.Return && last.action == action && last.checking) return last.id
        val id = nextId()
        journal.append(listOf(JournalEntry.Return(id, action, changes = null)))
        return id
    }

    private suspend fun rereadAfter(pending: PendingHandoff, facts: DeviceFacts) {
        val autostartTouched = pending.action.topic == AssistantTopic.AUTOSTART || pending.before.autostart != null
        if (autostartTouched && facts.readsAutostart()) readAutostartQuietly()
        val firmwareChecked = journal.state.value.progress.firmware != null
        if (firmwareChecked && pending.action.topic == AssistantTopic.FIRMWARE) {
            val response = withContext(dispatchers.io) { firmware.check() }
            if (response is Response.Success) journal.updateProgress { it.copy(firmware = response.value) }
        }
    }

    private fun startResult(action: AssistantAction, request: ResultRequest, forArchive: Boolean = false) {
        val stepId = nextId()
        val resultId = stepId + 1
        journal.append(
            listOf(
                JournalEntry.Step(stepId, StepTitle.Action(action), clock.now()),
                JournalEntry.Result(resultId, request, ResultContent.Loading),
            ),
        )
        if (forArchive) reduce { copy(archiveCard = resultId) }
        runResult(resultId, request)
    }

    private fun runResult(entryId: Long, request: ResultRequest) {
        val job = launchOnDevice(key = "result:$entryId") {
            val content = load(request)
            setContent(entryId, content)
            if (content is ResultContent.Data) openTopic(request.topic, pulse = true)
            if (content is ResultContent.FirmwareData && currentState.archiveCard == entryId) {
                reduce { copy(archiveCard = null) }
                content.check.modules.singleOrNull()?.let { archiveHandoff(it, withStep = false) }
            }
        }
        if (job == null) setContent(entryId, ResultContent.Error(ResultFailure.LinkLost))
    }

    private suspend fun load(request: ResultRequest): ResultContent = when (request) {
        ResultRequest.Firmware -> withContext(dispatchers.io) { firmware.check() }.toContent { check ->
            journal.updateProgress { it.copy(firmware = check) }
            ResultContent.FirmwareData(check, currentState.facts?.can)
        }

        ResultRequest.State -> withContext(dispatchers.io) { vehicleState.summary() }.toContent { summary ->
            journal.updateProgress { it.copy(state = summary) }
            ResultContent.StateData(summary)
        }

        ResultRequest.Autostart -> withContext(dispatchers.io) { autostart.status() }.toContent { status ->
            journal.updateProgress { it.copy(autostart = status) }
            ResultContent.AutostartData(status, bypassVariant())
        }

        is ResultRequest.Settings -> loadSettings(request)
    }

    private suspend fun loadSettings(request: ResultRequest.Settings): ResultContent {
        val settingId = request.settingId
            ?: return withContext(dispatchers.io) {
                settings.find(request.query)
            }.toContent(ResultContent::SettingsData)
        return withContext(dispatchers.io) { settings.setting(settingId) }.toContent { setting ->
            val editable = setting.editor != null && setting.value != null
            ResultContent.SettingsData(
                found = listOf(setting),
                editing = setting.id.takeIf { editable },
                draft = setting.value.takeIf { editable },
            )
        }
    }

    private inline fun <T> Response<T>.toContent(onSuccess: (T) -> ResultContent): ResultContent = when (this) {
        is Response.Success -> onSuccess(value)
        is Response.Error -> ResultContent.Error(exception.appError?.let(ResultFailure::Common) ?: ResultFailure.Failed)
    }

    private fun setContent(entryId: Long, content: ResultContent) {
        journal.update(entryId) { (it as? JournalEntry.Result)?.copy(content = content) ?: it }
    }

    private fun failRunning() {
        val entries = journal.state.value.entries
        entries.filter { it is JournalEntry.Result && it.content == ResultContent.Loading }
            .forEach { setContent(it.id, ResultContent.Error(ResultFailure.LinkLost)) }
        // Сравнение «до/после» прервала потеря связи: доделаем, когда блок вернётся.
        val returning = entries.lastOrNull() as? JournalEntry.Return
        if (returning?.checking == true) reduce { copy(awaitingReturn = true) }
        entries.filter { it is JournalEntry.Confirm && it.status == WriteStatus.WRITING }
            .forEach { entry ->
                updateConfirm(entry.id) { it.copy(status = WriteStatus.ERROR, failure = ResultFailure.LinkLost) }
            }
        reduce { copy(pin = null) }
    }

    // После возврата с другого экрана пульт остаётся на уровне, с которого уходили: иначе приходится шагать назад.
    private fun pulseTopic(topic: AssistantTopic) {
        reduce { copy(pulse = Pulse(topic, (this.pulse?.seq ?: 0) + 1)) }
    }

    private fun openTopic(topic: AssistantTopic, pulse: Boolean) {
        reduce {
            copy(
                level = DockLevel.Topic(topic),
                dockCollapsed = false,
                pulse = if (pulse) Pulse(topic, (this.pulse?.seq ?: 0) + 1) else this.pulse,
            )
        }
    }

    private fun nextId(): Long = journal.state.value.nextId

    private val ResultRequest.topic: AssistantTopic
        get() = when (this) {
            ResultRequest.Firmware -> AssistantTopic.FIRMWARE
            ResultRequest.State -> AssistantTopic.STATE
            ResultRequest.Autostart -> AssistantTopic.AUTOSTART
            is ResultRequest.Settings -> AssistantTopic.SETTINGS
        }

    private fun AssistantAction.target(): AssistantNavigationTarget? = HandoffTargets[this]

    private companion object {
        /** HANDOFF → экран; IN_FEED и архив (модуль выбирают в карточке прошивки) здесь не значатся. */
        val HandoffTargets: Map<AssistantAction, AssistantNavigationTarget> = mapOf(
            AssistantAction.FIRMWARE_UPDATE to AssistantNavigationTarget.FirmwareUpdate,
            AssistantAction.CAN_UPDATE to AssistantNavigationTarget.FirmwareUpdate,
            AssistantAction.BYPASS_RUN to AssistantNavigationTarget.BypassLearning,
            AssistantAction.CLONE_RUN to AssistantNavigationTarget.Clone,
            AssistantAction.SETTINGS_ALL to AssistantNavigationTarget.SettingsEditor(),
            AssistantAction.CONFIGURATOR to AssistantNavigationTarget.Configurator,
            AssistantAction.STATE_FULL to AssistantNavigationTarget.CheckState,
            AssistantAction.SYSTEM_DEVICES to AssistantNavigationTarget.SystemDevices,
            AssistantAction.PAIRING to AssistantNavigationTarget.Pairing,
            AssistantAction.VEHICLES to AssistantNavigationTarget.Vehicles,
            AssistantAction.TICKET_NEW to AssistantNavigationTarget.TechRequestNew,
            AssistantAction.TICKETS to AssistantNavigationTarget.TechRequests,
            AssistantAction.SUPPORT to AssistantNavigationTarget.SupportChat,
        )

        val FACTS_TIMEOUT = 5.seconds
        const val RETURN_KEY = "return"
        const val LIVE_KEY = "live-search"
        const val LIVE_MIN_LENGTH = 3
        val LIVE_DEBOUNCE = 300.milliseconds

        /** Экраны, которые блок обычно не меняют: карточка возврата — только если снимок изменился. */
        val QuietReturns = setOf(
            AssistantAction.SUPPORT,
            AssistantAction.TICKET_NEW,
            AssistantAction.TICKETS,
            // Периферию снимок не видит: «без изменений» после приживления вводило бы в заблуждение.
            AssistantAction.SYSTEM_DEVICES,
            AssistantAction.PAIRING,
            AssistantAction.VEHICLES,
            AssistantAction.FIRMWARE_ARCHIVE,
            AssistantAction.CONFIGURATOR,
            AssistantAction.STATE_FULL,
        )
    }
}
