package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState

enum class PairingPhase {
    /** Вход в режим программирования (D1, первый шаг). */
    ENTERING,

    /** Чтение таблицы слотов (D1, второй шаг). */
    READING,
    READY,

    /** Блок не пускает в режим программирования: охрана, зажигание (D11). */
    REFUSED,

    /** Таблицу прочитать не удалось (D12). */
    READ_FAILED,
}

/** Что открыто для выбранного слота: на телефоне — лист, на широком окне — панель справа. */
enum class SlotSheet { ACTIONS, ADDRESS, INSTRUCTION }

enum class PairingOutcome { WAITING, SUCCESS, FAILED }

/** Режим приживления слота [slot]: отсчёт [secondsLeft] из [PAIRING_SECONDS]. */
@Immutable
data class PairingSession(val slot: Int, val secondsLeft: Int, val outcome: PairingOutcome)

@Immutable
data class SlotFlash(val slot: Int, val seq: Int)

@Immutable
sealed interface PairingDialog {
    data class Delete(val slot: Int) : PairingDialog

    data object Unsupported : PairingDialog
}

@Immutable
data class PairingState(
    val deviceName: String? = null,
    val systemId: Long? = null,
    val bluetooth: Boolean = true,
    val phase: PairingPhase = PairingPhase.ENTERING,
    val slots: List<DynamicPeripheral> = emptyList(),
    val selected: Int? = null,
    val sheet: SlotSheet? = null,
    val address: String = "",
    val addressError: Boolean = false,
    val pairing: PairingSession? = null,
    val instructionOpen: Boolean = false,
    val deleting: Set<Int> = emptySet(),
    val flash: SlotFlash? = null,
    val dialog: PairingDialog? = null,
    val pin: ServicePinDialogState? = null,
    val pollGeneration: Int = 0,
) {
    fun slot(id: Int?): DynamicPeripheral? = id?.let { wanted -> slots.firstOrNull { it.slot == wanted } }

    val occupied: Int get() = slots.count { it.paired }

    val waiting: Boolean get() = pairing?.outcome == PairingOutcome.WAITING
}

const val PAIRING_SECONDS: Int = 120
