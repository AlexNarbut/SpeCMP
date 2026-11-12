package ru.alarmtrade.specmp.feature.mountassistant.domain.model

/** Что ассистент узнал о блоке в этой сессии; `null` — ещё не проверяли. */
data class MountProgress(
    val firmware: FirmwareCheck? = null,
    val autostart: AutostartStatus? = null,
    val state: StateSummary? = null,
    val settingsChanged: Int = 0,
    val changedSettings: List<String> = emptyList(),
)

data class FirmwareCheck(val modules: List<FirmwareModuleCheck>) {
    val main: FirmwareModuleCheck? get() = modules.firstOrNull { it.role == FirmwareModuleRole.MAIN }

    val updateAvailable: Boolean get() = main?.updateAvailable == true
}

/**
 * [id] — модуль на сервере прошивок; [installed] `null` — версию на блоке прочитать не удалось; [latest] `null` —
 * на сервере версии нет.
 */
data class FirmwareModuleCheck(
    val id: Long,
    val role: FirmwareModuleRole,
    val title: String,
    val installed: String?,
    val latest: String?,
    val updateAvailable: Boolean,
)

enum class FirmwareModuleRole { MAIN, BLUETOOTH, OTHER }

/** [cloneAttempts] `null` — блок не сообщил. */
data class AutostartStatus(val bypassLearned: Boolean, val clone: CloneState, val cloneAttempts: Int?)

enum class CloneState { NOT_NEEDED, REQUIRED, DONE, FAILED }

data class StateSummary(
    val armed: Boolean,
    val openZones: Set<StateZone>,
    val ignitionOn: Boolean,
    val engineRunning: Boolean,
    val voltage: Float,
    val temperature: Int,
) {
    val warnings: Int get() = openZones.size
}

enum class StateZone { DOORS, HOOD, TRUNK }
