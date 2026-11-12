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

    override suspend fun onStart() {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost { reduce { copy(pin = null, deleting = emptySet()) } }
        safeLaunch { repeatOnSubscription { poll() } }
        pinCooldown.lockedFor(select { systemId.takeIf { pin != null } })
            .collectIntoState { lock -> copy(pin = pin?.copy(lockedFor = lock)) }
    }

    private fun onDeviceReady(device: AlarmDevice) {
        reduce {
            copy(
                deviceName = device.systemName,
                systemId = device.systemId,
                bluetooth = deviceSession.transport.value != ConnectionTransport.USB,
            )
        }
    }

    private suspend fun poll() {
        combine(connectedDevice, select { pollGeneration }) { device, _ -> device }
            .flatMapLatest { device -> if (device == null) emptyFlow() else slotsFlow(device.systemId) }
            .collect(::onSlots)
    }

    private fun slotsFlow(systemId: Long): Flow<Response<List<DynamicPeripheral>>> = flow {
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
    }.flowOn(dispatchers.io)

    private suspend fun onSlots(response: Response<List<DynamicPeripheral>>) {
        when (response) {
            is Response.Success -> onTable(response.value)
            is Response.Error -> onReadError(response.exception.toPeripheralFailure())
        }
    }

    private fun onTable(slots: List<DynamicPeripheral>) {
        val removed = currentState.deleting.filter { id -> slots.firstOrNull { it.slot == id }?.paired == false }
        reduce {
            val session = pairing?.takeIf { current ->
                current.outcome == PairingOutcome.WAITING &&
                    slots.firstOrNull { it.slot == current.slot }?.paired == true
            }
            copy(
                phase = PairingPhase.READY,
                slots = slots,
                deleting = deleting - removed.toSet(),
                pairing = session?.copy(outcome = PairingOutcome.SUCCESS) ?: pairing,
                flash = session?.let { SlotFlash(it.slot, (flash?.seq ?: 0) + 1) } ?: flash,
            )
        }
        if (removed.isNotEmpty()) postSideEffect(PairingEffect.Snackbar(PairingMessage.DELETED))
    }

    // Таблица уже есть — переподписываемся молча (поток SDK кончается на первой ошибке); нет — экран ошибки.
    private suspend fun onReadError(failure: PeripheralFailure) {
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
        reduce { copy(phase = PairingPhase.ENTERING, pollGeneration = pollGeneration + 1) }
    }

    fun onSlotClick(slot: Int) {
        if (currentState.waiting) return
        reduce { copy(selected = slot, sheet = SlotSheet.ACTIONS, address = "", addressError = false) }
    }

    fun onSheetDismiss() {
        reduce { copy(sheet = null) }
    }

    fun onInstruction(slot: Int) {
        reduce { copy(selected = slot, sheet = SlotSheet.INSTRUCTION) }
    }

    fun onInstructionToggle() {
        reduce { copy(instructionOpen = !instructionOpen) }
    }

    /** Датчикам шин нужен адрес — сначала форма адреса. */
    fun onPair(slot: Int) {
        val target = currentState.slot(slot) ?: return
        if (currentState.waiting) return
        if (target.type == PeripheralType.TPMS) {
            reduce { copy(selected = slot, sheet = SlotSheet.ADDRESS, address = "", addressError = false) }
        } else {
            pair(target, address = null)
        }
    }

    fun onAddressChange(value: String) {
        val clean = value.uppercase().filter { it.isDigit() || it in 'A'..'Z' }.take(ADDRESS_LENGTH)
        reduce { copy(address = clean, addressError = false) }
    }

    fun onAddressSubmit() {
        val state = currentState
        val target = state.slot(state.selected) ?: return
        if (!AddressPattern.matches(state.address)) {
            reduce { copy(addressError = true) }
            return
        }
        pair(target, state.address)
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

    private suspend fun startCountdown(slot: Int) {
        reduce { copy(pairing = PairingSession(slot, PAIRING_SECONDS, PairingOutcome.WAITING)) }
        for (left in PAIRING_SECONDS - 1 downTo 0) {
            delay(TICK)
            if (currentState.pairing?.outcome != PairingOutcome.WAITING) return
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
        if (!currentState.waiting) return
        cancelLaunched(PAIR_KEY)
        reduce { copy(pairing = null) }
    }

    fun onPairingClose() {
        reduce { copy(pairing = null) }
    }

    fun onPairingRetry() {
        val slot = currentState.slot(currentState.pairing?.slot) ?: return
        reduce { copy(pairing = null) }
        onPair(slot.slot)
    }

    fun onDelete(slot: Int) {
        reduce { copy(dialog = PairingDialog.Delete(slot), sheet = null) }
    }

    fun onDeleteConfirm() {
        val dialog = currentState.dialog as? PairingDialog.Delete ?: return
        reduce { copy(dialog = null, deleting = deleting + dialog.slot) }
        launchOnDevice {
            val result = withContext(dispatchers.io) { repository.delete(dialog.slot) }
            if (result is Response.Error) {
                reduce { copy(deleting = deleting - dialog.slot) }
                onCommandError(result.exception.toPeripheralFailure())
            }
        }
    }

    fun onUpdate(slot: Int) {
        reduce { copy(sheet = null) }
        launchOnDevice {
            when (val result = withContext(dispatchers.io) { repository.update(slot) }) {
                is Response.Success -> postSideEffect(PairingEffect.Snackbar(PairingMessage.UPDATE_SENT))
                is Response.Error -> onCommandError(result.exception.toPeripheralFailure())
            }
        }
    }

    fun onDialogDismiss() {
        reduce { copy(dialog = null) }
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
        reduce { if (pin?.sending == true) this else copy(pin = null) }
    }

    private fun onCommandError(failure: PeripheralFailure) {
        when (failure) {
            PeripheralFailure.UNSUPPORTED -> reduce { copy(dialog = PairingDialog.Unsupported) }

            PeripheralFailure.WRONG_PIN -> showPin(wrong = false)

            PeripheralFailure.PROGRAM_MODE_REFUSED -> reduce { copy(phase = PairingPhase.REFUSED) }

            PeripheralFailure.NOT_CONFIRMED, PeripheralFailure.UNKNOWN ->
                postSideEffect(PairingEffect.Snackbar(PairingMessage.COMMAND_FAILED))
        }
    }

    private fun showPin(wrong: Boolean) {
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
