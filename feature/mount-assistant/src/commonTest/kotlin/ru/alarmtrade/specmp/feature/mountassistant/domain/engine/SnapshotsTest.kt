package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockSnapshot
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SnapshotChange

class SnapshotsTest {
    private val before = BlockSnapshot(
        firmwareVersion = "3.56",
        can = FactoryCan,
        autostart = autostart(learned = false, clone = CloneState.REQUIRED, attempts = 3),
        settingsChanged = 0,
    )

    @Test
    fun `same block gives no changes`() {
        assertEquals(emptyList(), before.diffTo(before))
    }

    @Test
    fun `every changed field is reported`() {
        val can = CanFacts(factory = false, code = "1234", version = "12.05")
        val after = BlockSnapshot(
            firmwareVersion = "3.57",
            can = can,
            autostart = autostart(learned = true, clone = CloneState.DONE, attempts = 2),
            settingsChanged = 2,
        )
        assertEquals(
            listOf(
                SnapshotChange.Firmware("3.56", "3.57"),
                SnapshotChange.Can(FactoryCan, can),
                SnapshotChange.Bypass(before = false, after = true),
                SnapshotChange.Clone(CloneState.REQUIRED, CloneState.DONE),
                SnapshotChange.CloneAttempts(3, 2),
                SnapshotChange.Settings(0, 2),
            ),
            before.diffTo(after),
        )
    }

    @Test
    fun `unread autostart is not compared`() {
        assertEquals(emptyList(), before.diffTo(before.copy(autostart = null)))
    }

    @Test
    fun `snapshot takes firmware and can from facts and the rest from progress`() {
        val progress = MountProgress(autostart = autostart(learned = true), settingsChanged = 4)
        assertEquals(
            BlockSnapshot("3.56", facts().can, progress.autostart, 4),
            snapshotOf(facts(), progress),
        )
    }
}
