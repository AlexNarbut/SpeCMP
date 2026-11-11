package ru.alarmtrade.specmp.feature.mountassistant.domain.model

/** Блок до перехода на другой экран и после возврата: итог перехода — их разница. */
data class BlockSnapshot(
    val firmwareVersion: String,
    val can: CanFacts?,
    val autostart: AutostartStatus?,
    val settingsChanged: Int,
    val changedSettings: List<String> = emptyList(),
)

sealed interface SnapshotChange {
    data class Firmware(val before: String, val after: String) : SnapshotChange

    data class Can(val before: CanFacts?, val after: CanFacts?) : SnapshotChange

    data class Bypass(val before: Boolean, val after: Boolean) : SnapshotChange

    data class Clone(val before: CloneState, val after: CloneState) : SnapshotChange

    data class CloneAttempts(val before: Int?, val after: Int?) : SnapshotChange

    /** [names] — настройки, записанные в блок за время перехода. */
    data class Settings(val before: Int, val after: Int, val names: List<String> = emptyList()) : SnapshotChange
}

/** Переход на экран другой фичи, после которого ассистент ждёт возврата. */
data class PendingHandoff(val action: AssistantAction, val before: BlockSnapshot)
