package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

/**
 * Состояние мастера быстрого монтажа: подготовка (блок → автомобиль → конфигурация → сводка) и шаги SDK.
 *
 * @property fixed блок, зафиксированный на первом подключении; монтаж идёт только с ним.
 * @property unit данные блока с последнего подключения (прошивка, CAN) — карточка «Проверка блока» и сводка.
 * @property wrongDevice подключён другой блок — его название; мастер стоит, пока не вернётся [fixed].
 */
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
    val dialog: WizardDialog? = null
) {
    val usb: Boolean get() = fixed?.transport == ConnectionTransport.USB
}

/** Блок монтажа. [title] — «Модель · ID». */
@Immutable
data class FixedDevice(
    val systemId: Long,
    val systemName: String,
    val title: String,
    val transport: ConnectionTransport?
)

/** Данные блока для карточек: прошивка без `VER`, CAN-прошивка ([canCode] `null` — блок без CAN-прошивки). */
@Immutable
data class UnitInfo(val firmware: String, val canCode: String?, val canVersion: String?, val canFactory: Boolean)

/** Экран подготовки; [MASTER] — шаги SDK (какой — по [MasterState.screen]). */
enum class WizardScreen { CHECK, FIRMWARE, VEHICLE, CONFIGURATION, SUMMARY, MASTER }

/** Проверка блока перед монтажом (F2/F3). */
@Immutable
sealed interface UnitCheck {
    data object Checking : UnitCheck

    data class Unsupported(val reason: UnsupportedReason) : UnitCheck

    data class Failed(val error: FastMountError) : UnitCheck

    data class Done(val verdict: UnitVerdict) : UnitCheck
}

/** Итог проверки: монтаж можно начинать ([blocking] = false) или сначала решить проблему. */
@Immutable
sealed interface UnitVerdict {
    val blocking: Boolean

    data object Ready : UnitVerdict {
        override val blocking = false
    }

    /** Доступна новая прошивка [latest] — рекомендация, не блокирует. */
    data class Outdated(val latest: String) : UnitVerdict {
        override val blocking = false
    }

    /** В блоке заводские CAN-настройки — монтаж невозможен до обновления CAN-прошивки. */
    data object FactoryCan : UnitVerdict {
        override val blocking = true
    }

    /** Нет доступа к блоку на сервере; [message] — причина от сервера. */
    data class NoAccess(val message: String) : UnitVerdict {
        override val blocking = true
    }
}

enum class UnsupportedReason { NOT_ALARM, MICROBOT, NOT_IN_DATABASE }

@Immutable
sealed interface SummaryLoad {
    data object Loading : SummaryLoad

    data object Ready : SummaryLoad

    data class Failed(val error: FastMountError) : SummaryLoad
}

/**
 * Шаги SDK.
 *
 * @property starting идёт старт (сервер, геолокация); [startError] — старт не удался.
 * @property step текущий шаг SDK; `null` — ещё не стартовали.
 * @property busy ждём ответа SDK (переход, запись) — главная кнопка со спиннером, повторный тап невозможен.
 * @property error ошибка текущего шага — плашка с причиной и «Повторить».
 * @property reboot ждём перезагрузку блока после шага [RebootWait.from] — экран «Перезагрузка блока…».
 * @property stepAttempt номер прохода обхода/Clone: «Вернуться к монтажу» начинает их заново (новые VM экранов).
 * @property cloneNeeded обход потребовал Clone (`true`), не потребовал (`false`), ещё неизвестно (`null`).
 * @property statuses статусы заданий проверки системы по id вопроса.
 * @property mountMinutes длительность монтажа (с «Начать монтаж») — итоги на шаге отзыва.
 */
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
    val mountMinutes: Int = 0,
    val toast: WizardToast? = null
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
    DONE
}

/** Ожидание перезагрузки после шага [from] (таймер ведёт экран). */
@Immutable
data class RebootWait(val from: FastMountStep)

/** Карта монтажа (PDF): нет в конфигурации, качается, скачана ([sizeBytes]), ошибка. */
@Immutable
sealed interface MountMapState {
    data object None : MountMapState

    data object Downloading : MountMapState

    data class Ready(val sizeBytes: Int) : MountMapState

    data object Failed : MountMapState
}

enum class WizardDialog {
    /** «Выйти из быстрого монтажа?» — монтаж, ошибка записи. */
    EXIT,

    /** «Вернуться к монтажу?» — обход (Clone не нужен). */
    TO_MOUNT,

    /** «Вернуться к монтажу?» — обход и Clone придётся пройти заново. */
    TO_MOUNT_CLONE,

    /** «Монтаж завершён?» — блок перезагрузится. */
    MOUNT_DONE,

    /** «Обучение Slave завершено?» */
    SLAVE_DONE
}

enum class WizardToast { SETTINGS_WRITTEN, FILE_NO_APP, FILE_FAILED }

/** Разовые действия мастера, которые выполняет UI. */
sealed interface FastMountEffect {
    /** Выйти из монтажа (сессия закрывается обёрткой). */
    data object Exit : FastMountEffect

    /** Выйти с возвратом к поиску блока. */
    data object ChooseAnother : FastMountEffect

    /** Выйти в расширенный монтаж (новый поиск). */
    data object AdvancedMount : FastMountEffect

    /** Открыть файл системным приложением. */
    class OpenFile(val name: String, val bytes: ByteArray) : FastMountEffect
}
