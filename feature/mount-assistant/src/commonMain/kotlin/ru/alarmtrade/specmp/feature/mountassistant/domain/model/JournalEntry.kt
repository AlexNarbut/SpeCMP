package ru.alarmtrade.specmp.feature.mountassistant.domain.model

import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.AppError

sealed interface JournalEntry {
    val id: Long

    data class Greeting(override val id: Long) : JournalEntry

    data class Step(override val id: Long, val title: StepTitle, val at: Instant) : JournalEntry

    data class Notice(override val id: Long, val reason: BlockReason) : JournalEntry

    data class Result(override val id: Long, val request: ResultRequest, val content: ResultContent) : JournalEntry

    /** [used] — источник, по которому уже перешли в редактор. */
    data class SourcePicker(override val id: Long, val used: SettingsSourceKind? = null) : JournalEntry

    data class Confirm(
        override val id: Long,
        val setting: FoundSetting,
        val from: AssistantSettingValue,
        val to: AssistantSettingValue,
        val status: WriteStatus,
        val otherUnsaved: Int = 0,
        val pinSkipped: Boolean = false,
        val failure: ResultFailure? = null,
    ) : JournalEntry

    /** Паспорт монтажа: [passport] `null` или не оформлен — «Оформите паспорт», оформлен — «Паспорт оформлен · №». */
    data class Passport(override val id: Long, val passport: TodayPassport?) : JournalEntry

    data class Return(override val id: Long, val action: AssistantAction, val changes: List<SnapshotChange>?) :
        JournalEntry {
        val checking: Boolean get() = changes == null
    }
}

sealed interface StepTitle {
    val topic: AssistantTopic?

    data class Action(val action: AssistantAction) : StepTitle {
        override val topic: AssistantTopic get() = action.topic
    }

    data class Search(val query: String) : StepTitle {
        override val topic: AssistantTopic? get() = null
    }

    data class Setting(val name: String) : StepTitle {
        override val topic: AssistantTopic get() = AssistantTopic.SETTINGS
    }

    data object OpenInSettings : StepTitle {
        override val topic: AssistantTopic get() = AssistantTopic.SETTINGS
    }

    data object Reflash : StepTitle {
        override val topic: AssistantTopic get() = AssistantTopic.FIRMWARE
    }

    /** «Информация о товаре №…» из строки поиска. */
    data class ProductNumber(val number: String) : StepTitle {
        override val topic: AssistantTopic get() = AssistantTopic.DEVICES
    }
}

sealed interface ResultRequest {
    data object Firmware : ResultRequest

    data object State : ResultRequest

    data object Autostart : ResultRequest

    /** Товар подключённого блока. */
    data object Product : ResultRequest

    /** [query] — поиск; [settingId] — одна выбранная настройка. */
    data class Settings(val query: String, val settingId: Int? = null) : ResultRequest
}

sealed interface ResultContent {
    data object Loading : ResultContent

    data class Error(val failure: ResultFailure) : ResultContent

    sealed interface Data : ResultContent

    data class FirmwareData(val check: FirmwareCheck, val can: CanFacts?) : Data

    data class StateData(val summary: StateSummary) : Data

    /** [variant] — настройка «Вариант бесключевого обхода»; `null` — у блока её нет или настройки не прочитаны. */
    data class AutostartData(val status: AutostartStatus, val variant: FoundSetting? = null) : Data

    data class ProductData(val product: AssistantProduct) : Data

    /** Товара с ID блока нет на сервере. */
    data class ProductNotFound(val id: Long) : Data

    /** [editing] — настройка, открытая на правку, [draft] — её новое значение. */
    data class SettingsData(
        val found: List<FoundSetting>,
        val editing: Int? = null,
        val draft: AssistantSettingValue? = null,
    ) : Data
}

sealed interface ResultFailure {
    data object LinkLost : ResultFailure

    data object WrongPin : ResultFailure

    data object Failed : ResultFailure

    data class Common(val error: AppError) : ResultFailure
}

enum class WriteStatus { PENDING, PIN, WRITING, DONE, ERROR, CANCELLED }

/** Журнал ассистента для одного блока: [systemId] `null` — ещё не привязан. */
data class JournalState(
    val systemId: Long? = null,
    val entries: List<JournalEntry> = emptyList(),
    val progress: MountProgress = MountProgress(),
    val pendingHandoff: PendingHandoff? = null,
) {
    val nextId: Long get() = (entries.maxOfOrNull { it.id } ?: 0L) + 1
}
