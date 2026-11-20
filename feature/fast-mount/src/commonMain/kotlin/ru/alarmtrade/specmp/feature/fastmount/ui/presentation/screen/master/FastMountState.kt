package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.runtime.Immutable
import kotlin.time.TimeSource
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

/** @property fixed блок, зафиксированный на первом подключении; монтаж идёт только с ним. */
@Immutable
data class FastMountState(
    val fixed: FixedDevice? = null,
    val unit: UnitInfo? = null,
    val wrongDevice: String? = null,
    val screen: WizardScreen = WizardScreen.CHECK,
    val check: UnitCheck = UnitCheck.Checking,
    val car: MountCar? = null,
    val summary: SummaryLoad = SummaryLoad.Loading,
    val configuration: FastMountConfiguration? = null,
    val plan: FastMountPlan? = null,
    val master: MasterState = MasterState(),
    val dialog: WizardDialog? = null,
) {
    val usb: Boolean get() = fixed?.transport == ConnectionTransport.USB
}

@Immutable
data class FixedDevice(
    val systemId: Long,
    val systemName: String,
    val title: String,
    val transport: ConnectionTransport?,
)

@Immutable
data class UnitInfo(val firmware: String, val canCode: String?, val canVersion: String?, val canFactory: Boolean)

enum class WizardScreen { CHECK, FIRMWARE, VEHICLE, CONFIGURATION, SUMMARY, MASTER }

@Immutable
sealed interface UnitCheck {
    data object Checking : UnitCheck

    data class Unsupported(val reason: UnsupportedReason) : UnitCheck

    data class Failed(val error: FastMountError) : UnitCheck

    data class Done(val verdict: UnitVerdict) : UnitCheck
}

@Immutable
sealed interface UnitVerdict {
    val blocking: Boolean

    data object Ready : UnitVerdict {
        override val blocking = false
    }

    data class Outdated(val latest: String) : UnitVerdict {
        override val blocking = false
    }

    data object FactoryCan : UnitVerdict {
        override val blocking = true
    }

    data class NoAccess(val message: String) : UnitVerdict {
        override val blocking = true
    }
}

enum class UnsupportedReason { NOT_ALARM, MICROBOT, NOT_IN_DATABASE }

@Immutable
sealed interface SummaryLoad {
    data object Loading : SummaryLoad

    data object Ready : SummaryLoad

    data class Failed(val configurationId: Int, val error: FastMountError) : SummaryLoad
}

/** @property stepAttempt номер прохода обхода/Clone: «Вернуться к монтажу» начинает их заново (новые VM экранов). */
@Immutable
data class MasterState(
    val starting: Boolean = false,
    val startError: FastMountError? = null,
    val step: FastMountStep? = null,
    val busy: Boolean = false,
    val error: FastMountError? = null,
    val reboot: RebootWait? = null,
    val stepAttempt: Int = 0,
    val cloneNeeded: Boolean? = null,
    val slaveDone: Boolean = false,
    val mountMap: MountMapState = MountMapState.None,
    val currentTask: Int? = null,
    val statuses: Map<Int, MountTaskStatus> = emptyMap(),
    val checklist: MountChecklist = MountChecklist(),
    val rating: Int = 0,
    val feedback: String = "",
    val startedAt: TimeSource.Monotonic.ValueTimeMark? = null,
    val mountMinutes: Int = 0,
    val toast: WizardToast? = null,
    val result: FastMountResult? = null,
    val passport: PassportFlow = PassportFlow(),
) {
    val screen: MasterScreen
        get() = when {
            reboot != null || step == FastMountStep.REBOOTING -> MasterScreen.REBOOTING

            step == null -> MasterScreen.STARTING

            else -> when (step) {
                FastMountStep.WRITING_SETTINGS -> MasterScreen.WRITING
                FastMountStep.MOUNT -> MasterScreen.MOUNT
                FastMountStep.KEY_BYPASS -> MasterScreen.BYPASS
                FastMountStep.CLONE -> MasterScreen.CLONE
                FastMountStep.SLAVE -> MasterScreen.SLAVE
                FastMountStep.SYSTEM_CHECK -> MasterScreen.SYSTEM_CHECK
                FastMountStep.FEEDBACK -> MasterScreen.FEEDBACK
                FastMountStep.FINISH -> MasterScreen.DONE
                FastMountStep.REBOOTING -> MasterScreen.REBOOTING
            }
        }
}

/**
 * Паспорт монтажа с финала: [screen] — открытый экран паспорта (`null` — финал мастера), [attempt] — номер
 * считывания (новое считывание — новые VM экранов паспорта), [passportId] — паспорт, созданный считыванием.
 */
@Immutable
data class PassportFlow(
    val screen: PassportScreen? = null,
    val attempt: Int = 0,
    val passportId: String? = null,
    val passport: FastMountPassport? = null,
)

enum class PassportScreen { READING, EDITOR }

enum class MasterScreen {
    STARTING,
    WRITING,
    REBOOTING,
    MOUNT,
    BYPASS,
    CLONE,
    SLAVE,
    SYSTEM_CHECK,
    FEEDBACK,
    DONE,
}

@Immutable
data class RebootWait(val from: FastMountStep)

@Immutable
sealed interface MountMapState {
    data object None : MountMapState

    data object Downloading : MountMapState

    /** Скачанная карта: файл открывается из памяти системным приложением. Массив не меняется — `@Immutable`. */
    @Immutable
    class Ready(val bytes: ByteArray) : MountMapState {
        val sizeBytes: Int get() = bytes.size
    }

    data object Failed : MountMapState
}

enum class WizardDialog {
    EXIT,

    TO_MOUNT,

    TO_MOUNT_CLONE,

    MOUNT_DONE,

    SLAVE_DONE,
}

enum class WizardToast { SETTINGS_WRITTEN, FILE_NO_APP, FILE_FAILED }
