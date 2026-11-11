package ru.alarmtrade.specmp.feature.mountassistant.data.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockSnapshot
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.PendingHandoff
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StepTitle

class AssistantJournalImplTest {
    private val journal = AssistantJournalImpl()
    private val handoff = PendingHandoff(AssistantAction.BYPASS_RUN, BlockSnapshot("3.56", null, null, 0))

    private fun fill() {
        journal.append(
            listOf(
                JournalEntry.Greeting(1),
                JournalEntry.Step(2, StepTitle.Action(AssistantAction.STATE), Instant.fromEpochSeconds(0)),
            ),
        )
        journal.updateProgress { it.copy(settingsChanged = 3) }
        journal.beginHandoff(handoff)
    }

    @Test
    fun `binding the same block keeps journal`() {
        journal.bind(7)
        fill()
        journal.bind(7)
        val state = journal.state.value
        assertEquals(2, state.entries.size)
        assertEquals(3, state.progress.settingsChanged)
        assertEquals(handoff, state.pendingHandoff)
        assertEquals(3L, state.nextId)
    }

    @Test
    fun `binding another block resets journal`() {
        journal.bind(7)
        fill()
        journal.bind(8)
        val state = journal.state.value
        assertEquals(8L, state.systemId)
        assertEquals(emptyList(), state.entries)
        assertEquals(0, state.progress.settingsChanged)
        assertNull(state.pendingHandoff)
        assertEquals(1L, state.nextId)
    }

    @Test
    fun `update replaces only the entry with the id`() {
        journal.bind(7)
        fill()
        journal.update(1) { JournalEntry.Notice(it.id, BlockReason.NO_NETWORK) }
        assertEquals(JournalEntry.Notice(1, BlockReason.NO_NETWORK), journal.state.value.entries[0])
        assertEquals(2L, journal.state.value.entries[1].id)
    }

    @Test
    fun `end handoff clears pending target`() {
        journal.bind(7)
        fill()
        journal.endHandoff()
        assertNull(journal.state.value.pendingHandoff)
    }
}
