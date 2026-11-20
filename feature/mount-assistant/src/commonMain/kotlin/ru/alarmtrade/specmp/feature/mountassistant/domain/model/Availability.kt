package ru.alarmtrade.specmp.feature.mountassistant.domain.model

sealed interface Availability {
    data object Available : Availability

    /** Нет возможности у блока или не тот транспорт: действия в пульте нет. */
    data object Hidden : Availability

    /** [fix] — действие, которое снимает блокировку. */
    data class Blocked(val reason: BlockReason, val fix: AssistantAction?) : Availability
}

enum class BlockReason { FACTORY_CAN, NO_NETWORK }

data class NextStep(val action: AssistantAction, val reason: NextStepReason)

enum class NextStepReason {
    UNKNOWN_MODEL_FIRMWARE,
    UNKNOWN_MODEL_SUPPORT,
    FACTORY_CAN,
    FIRMWARE_UNCHECKED,
    FIRMWARE_OUTDATED,
    AUTOSTART_UNKNOWN,
    BYPASS_NOT_LEARNED,
    CLONE_REQUIRED,
    STATE_UNCHECKED,
    PASSPORT_NOT_ISSUED,
    ROUTE_DONE,
    NOTHING_LEFT,
}

enum class StatusTone { OK, WARN, LOCK, OFF, NEUTRAL }

data class TopicStatus(val tone: StatusTone, val kind: TopicStatusKind)

/** Статус темы по группам: у каждой темы свой набор. */
sealed interface TopicStatusKind {
    sealed interface Firmware : TopicStatusKind {
        data object NoNetwork : Firmware

        data object FactoryCan : Firmware

        data class Unchecked(val version: String) : Firmware

        data class Update(val version: String) : Firmware

        data object Actual : Firmware
    }

    sealed interface Autostart : TopicStatusKind {
        data object Unknown : Autostart

        data object BypassNotLearned : Autostart

        data object CloneNeeded : Autostart

        data object Ready : Autostart
    }

    sealed interface Settings : TopicStatusKind {
        data class Changed(val count: Int) : Settings

        data object Unchanged : Settings
    }

    sealed interface State : TopicStatusKind {
        data object Unchecked : State

        data class Warnings(val count: Int) : State

        data object Ok : State
    }

    sealed interface Passport : TopicStatusKind {
        data object NotIssued : Passport

        data class Issued(val number: String) : Passport
    }

    /** Заводской CAN закрывает обход, Clone и настройки. */
    data object CanLocked : Autostart, Settings

    data object Devices : TopicStatusKind

    data object Vehicle : TopicStatusKind

    data object Support : TopicStatusKind
}

/** Тема в пульте: [blocked] — все её видимые действия заблокированы. */
data class TopicAvailability(val topic: AssistantTopic, val actions: List<AssistantAction>, val blocked: Boolean)
