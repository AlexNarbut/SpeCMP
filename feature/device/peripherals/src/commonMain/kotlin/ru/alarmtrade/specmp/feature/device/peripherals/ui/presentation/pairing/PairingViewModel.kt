package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntryResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.toPeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.DynamicPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

/**
 * Таблица опрашивается, пока экран подписан (`repeatOnSubscription`). Блок входит в режим программирования до
 * чтения — через [ProgramModeEntry], чтобы неверный PIN давал диалог и кулдаун. Итог приживления и удаления виден
 * только по опросу: SDK подтверждает лишь приём команды.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PairingViewModel(
    deviceSession: DeviceSession,
    private val repository: DynamicPeripheralsRepository,
    private val programMode: ProgramModeEntry,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<PairingState, PairingEffect>(deviceSession, PairingState()) {

    override suspend fun IntentScope.onStart() {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost { reduce { copy(pin = null, deleting = emptySet()) } }
        intent { repeatOnSubscription { poll() } }
        pinCooldown.lockedFor(select { systemId.takeIf { pin != null } })
            .collectIntoState { lock -> copy(pin = pin?.copy(lockedFor = lock)) }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) = subIntent {
        reduce {
            copy(
                deviceName = device.systemName,
                systemId = device.systemId,
                bluetooth = deviceSession.transport.value != ConnectionTransport.USB,
            )
        }
    }

    private suspend fun poll() = subIntent {
        // SDK — на IO (slotsFlow); combine и пересборка групп таблицы — на Default; на Main — только ошибки.
        combine(connectedDevice, select { pollGeneration }) { device, _ -> device }
            .flatMapLatest { device -> if (device == null) emptyFlow() else slotsFlow(device.systemId) }
            .onEach { response -> if (response is Response.Success) onTable(response.value) }
            .flowOn(dispatchers.default)
            .collect { response ->
                if (response is Response.Error) onReadError(response.exception.toPeripheralFailure())
            }
    }

    private fun slotsFlow(systemId: Long): Flow<Response<List<DynamicPeripheral>>> = flow {
        subIntent {
            reduce { if (slots.isEmpty()) copy(phase = PairingPhase.ENTERING) else this }
            when (programMode.enter(systemId, pin = null)) {
                ProgramModeEntryResult.ENTERED -> {
                    reduce { if (slots.isEmpty()) copy(phase = PairingPhase.READING) else this }
                    emitAll(repository.slots())
                }

                ProgramModeEntryResult.PIN_LOCKED -> showPin(wrong = false)

                ProgramModeEntryResult.WRONG_PIN -> showPin(wrong = false)

                ProgramModeEntryResult.FAILED -> reduce { copy(phase = PairingPhase.REFUSED) }
            }
        }
    }.flowOn(dispatchers.io)

    private suspend fun onTable(slots: List<DynamicPeripheral>) = subIntent {
        val removed = currentState.deleting.filter { id -> slots.firstOrNull { it.slot == id }?.paired == false }
        reduce {
            val session = pairing?.takeIf { current ->
                current.outcome == PairingOutcome.WAITING &&
                    slots.firstOrNull { it.slot == current.slot }?.paired == true
            }
            copy(
                phase = PairingPhase.READY,
                slots = slots,
                groups = groupSlots(slots),
                occupied = slots.count { it.paired },
                deleting = deleting - removed.toSet(),
                pairing = session?.copy(outcome = PairingOutcome.SUCCESS) ?: pairing,
                flash = session?.let { SlotFlash(it.slot, (flash?.seq ?: 0) + 1) } ?: flash,
            )
        }
        if (removed.isNotEmpty()) postSideEffect(PairingEffect.Snackbar(PairingMessage.DELETED))
    }

    // Таблица уже есть — переподписываемся молча (поток SDK кончается на первой ошибке); нет — экран ошибки.
    private suspend fun onReadError(failure: PeripheralFailure) = subIntent {
        when {
            failure == PeripheralFailure.WRONG_PIN -> showPin(wrong = false)

            currentState.slots.isNotEmpty() -> {
                delay(RETRY_PAUSE)
                reduce { copy(pollGeneration = pollGeneration + 1) }
            }

            failure == PeripheralFailure.PROGRAM_MODE_REFUSED -> reduce { copy(phase = PairingPhase.REFUSED) }

            else -> reduce { copy(phase = PairingPhase.READ_FAILED) }
        }
    }

    fun onRetry() {
        intent { reduce { copy(phase = PairingPhase.ENTERING, pollGeneration = pollGeneration + 1) } }
    }

    fun onSlotClick(slot: Int) {
        intent {
            if (currentState.waiting) return@intent
            reduce { copy(selected = slot, sheet = SlotSheet.ACTIONS, address = "", addressError = false) }
        }
    }

    fun onSheetDismiss() {
        intent { reduce { copy(sheet = null) } }
    }

    fun onInstruction(slot: Int) {
        intent { reduce { copy(selected = slot, sheet = SlotSheet.INSTRUCTION) } }
    }

    fun onInstructionToggle() {
        intent { reduce { copy(instructionOpen = !instructionOpen) } }
    }

    /** Датчикам шин нужен адрес — сначала форма адреса. */
    fun onPair(slot: Int) {
        intent {
            val target = currentState.slot(slot) ?: return@intent
            if (currentState.waiting) return@intent
            if (target.type == PeripheralType.TPMS) {
                reduce { copy(selected = slot, sheet = SlotSheet.ADDRESS, address = "", addressError = false) }
            } else {
                pair(target, address = null)
            }
        }
    }

    fun onAddressChange(value: String) {
        intent {
            val clean = value.uppercase().filter { it.isDigit() || it in 'A'..'Z' }.take(ADDRESS_LENGTH)
            reduce { copy(address = clean, addressError = false) }
        }
    }

    fun onAddressSubmit() {
        intent {
            val state = currentState
            val target = state.slot(state.selected) ?: return@intent
            if (!AddressPattern.matches(state.address)) {
                reduce { copy(addressError = true) }
                return@intent
            }
            pair(target, state.address)
        }
    }

    private fun pair(target: DynamicPeripheral, address: String?) {
        launchOnDevice(PAIR_KEY) {
            reduce { copy(sheet = null, selected = target.slot, instructionOpen = false) }
            when (val result = withContext(dispatchers.io) { repository.pair(target.slot, address) }) {
                is Response.Success -> startCountdown(target.slot)
                is Response.Error -> onCommandError(result.exception.toPeripheralFailure())
            }
        }
    }

    private suspend fun startCountdown(slot: Int) = subIntent {
        reduce { copy(pairing = PairingSession(slot, PAIRING_SECONDS, PairingOutcome.WAITING)) }
        for (left in PAIRING_SECONDS - 1 downTo 0) {
            delay(TICK)
            if (currentState.pairing?.outcome != PairingOutcome.WAITING) return@subIntent
            reduce {
                val session = pairing ?: return@reduce this
                copy(
                    pairing = session.copy(
                        secondsLeft = left,
                        outcome = if (left == 0) PairingOutcome.FAILED else session.outcome,
                    ),
                )
            }
        }
    }

    /** Отмена только скрывает плашку: блок сам выйдет из режима приживления по таймауту. */
    fun onPairingCancel() {
        intent {
            if (!currentState.waiting) return@intent
            cancelLaunched(PAIR_KEY)
            reduce { copy(pairing = null) }
        }
    }

    fun onPairingClose() {
        intent { reduce { copy(pairing = null) } }
    }

    fun onPairingRetry() {
        intent {
            val slot = currentState.slot(currentState.pairing?.slot) ?: return@intent
            reduce { copy(pairing = null) }
            onPair(slot.slot)
        }
    }

    fun onDelete(slot: Int) {
        intent { reduce { copy(dialog = PairingDialog.Delete(slot), sheet = null) } }
    }

    fun onDeleteConfirm() {
        intent {
            val dialog = currentState.dialog as? PairingDialog.Delete ?: return@intent
            reduce { copy(dialog = null, deleting = deleting + dialog.slot) }
            launchOnDevice {
                val result = withContext(dispatchers.io) { repository.delete(dialog.slot) }
                if (result is Response.Error) {
                    reduce { copy(deleting = deleting - dialog.slot) }
                    onCommandError(result.exception.toPeripheralFailure())
                }
            }
        }
    }

    fun onUpdate(slot: Int) {
        intent {
            reduce { copy(sheet = null) }
            launchOnDevice {
                when (val result = withContext(dispatchers.io) { repository.update(slot) }) {
                    is Response.Success -> postSideEffect(PairingEffect.Snackbar(PairingMessage.UPDATE_SENT))
                    is Response.Error -> onCommandError(result.exception.toPeripheralFailure())
                }
            }
        }
    }

    fun onDialogDismiss() {
        intent { reduce { copy(dialog = null) } }
    }

    fun onPinSubmit(pin: String) {
        val dialog = currentState.pin ?: return
        if (dialog.sending) return
        val systemId = currentState.systemId ?: return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        launchOnDevice {
            reduce { copy(pin = this.pin?.copy(sending = true)) }
            when (withContext(dispatchers.io) { programMode.enter(systemId, servicePin) }) {
                ProgramModeEntryResult.ENTERED ->
                    reduce { copy(pin = null, phase = PairingPhase.READING, pollGeneration = pollGeneration + 1) }

                ProgramModeEntryResult.PIN_LOCKED -> showPin(wrong = false)

                ProgramModeEntryResult.WRONG_PIN -> showPin(wrong = true)

                ProgramModeEntryResult.FAILED -> reduce { copy(pin = null, phase = PairingPhase.REFUSED) }
            }
        }
    }

    fun onPinDismiss() {
        intent { reduce { if (pin?.sending == true) this else copy(pin = null) } }
    }

    private suspend fun onCommandError(failure: PeripheralFailure) = subIntent {
        when (failure) {
            PeripheralFailure.UNSUPPORTED -> reduce { copy(dialog = PairingDialog.Unsupported) }

            PeripheralFailure.WRONG_PIN -> showPin(wrong = false)

            PeripheralFailure.PROGRAM_MODE_REFUSED -> reduce { copy(phase = PairingPhase.REFUSED) }

            PeripheralFailure.NOT_CONFIRMED, PeripheralFailure.UNKNOWN ->
                postSideEffect(PairingEffect.Snackbar(PairingMessage.COMMAND_FAILED))
        }
    }

    private suspend fun showPin(wrong: Boolean) = subIntent {
        reduce {
            val previous = pin
            copy(
                pin = ServicePinDialogState(
                    wrong = wrong,
                    wrongCount = (previous?.wrongCount ?: 0) + if (wrong) 1 else 0,
                    lockedFor = previous?.lockedFor,
                ),
            )
        }
    }

    private companion object {
        const val PAIR_KEY = "pair"
        const val ADDRESS_LENGTH = 8
        val AddressPattern = Regex("[0-9A-F]{8}")
        val TICK = 1.seconds
        val RETRY_PAUSE = 2.seconds
    }
}
