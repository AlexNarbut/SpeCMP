package ru.alarmtrade.specmp.feature.mountassistant.data.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.PendingHandoff
import ru.alarmtrade.specmp.feature.mountassistant.domain.service.AssistantJournal

internal class AssistantJournalImpl : AssistantJournal {
    private val journal = MutableStateFlow(JournalState())

    override val state: StateFlow<JournalState> = journal.asStateFlow()

    override fun bind(systemId: Long) {
        journal.update { if (it.systemId == systemId) it else JournalState(systemId = systemId) }
    }

    override fun append(entries: List<JournalEntry>) {
        journal.update { it.copy(entries = it.entries + entries) }
    }

    override fun update(id: Long, transform: (JournalEntry) -> JournalEntry) {
        journal.update { state ->
            state.copy(entries = state.entries.map { if (it.id == id) transform(it) else it })
        }
    }

    override fun updateProgress(transform: (MountProgress) -> MountProgress) {
        journal.update { it.copy(progress = transform(it.progress)) }
    }

    override fun beginHandoff(handoff: PendingHandoff) {
        journal.update { it.copy(pendingHandoff = handoff) }
    }

    override fun endHandoff() {
        journal.update { it.copy(pendingHandoff = null) }
    }
}
