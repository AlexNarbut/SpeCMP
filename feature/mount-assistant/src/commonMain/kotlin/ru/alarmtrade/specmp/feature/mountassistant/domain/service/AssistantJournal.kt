package ru.alarmtrade.specmp.feature.mountassistant.domain.service

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.PendingHandoff

/**
 * Журнал ассистента на время сессии блока: переживает экран (прошивка уводит граф монтажа из композиции). Только
 * хранит — что и когда писать, решает ViewModel.
 */
interface AssistantJournal {
    // Журнал в памяти: «упасть» ему нечем.
    @Suppress("konsist.service-contract")
    val state: StateFlow<JournalState>

    /** Другой блок — журнал и прогресс сбрасываются; тот же блок (переподключение) — остаются. */
    fun bind(systemId: Long)

    fun append(entries: List<JournalEntry>)

    fun update(id: Long, transform: (JournalEntry) -> JournalEntry)

    fun updateProgress(transform: (MountProgress) -> MountProgress)

    fun beginHandoff(handoff: PendingHandoff)

    fun endHandoff()
}
