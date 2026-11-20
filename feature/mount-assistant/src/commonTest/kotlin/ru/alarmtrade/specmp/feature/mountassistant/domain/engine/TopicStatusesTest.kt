package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateSummary
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateZone
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StatusTone
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TodayPassport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TopicStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TopicStatusKind

class TopicStatusesTest {
    private val empty = MountProgress()

    @Test
    fun firmware() {
        val topic = AssistantTopic.FIRMWARE
        assertEquals(
            TopicStatus(StatusTone.OFF, TopicStatusKind.Firmware.NoNetwork),
            topicStatus(topic, facts(online = false), empty),
        )
        assertEquals(
            TopicStatus(StatusTone.WARN, TopicStatusKind.Firmware.FactoryCan),
            topicStatus(topic, facts(can = FactoryCan), empty),
        )
        assertEquals(
            TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Firmware.Unchecked("3.56")),
            topicStatus(topic, facts(), empty),
        )
        assertEquals(
            TopicStatus(StatusTone.WARN, TopicStatusKind.Firmware.Update("3.57")),
            topicStatus(topic, facts(), MountProgress(firmware = firmwareCheck(true))),
        )
        assertEquals(
            TopicStatus(StatusTone.OK, TopicStatusKind.Firmware.Actual),
            topicStatus(topic, facts(), MountProgress(firmware = firmwareCheck(false))),
        )
    }

    @Test
    fun autostart() {
        val topic = AssistantTopic.AUTOSTART
        fun status(
            progress: MountProgress,
            f: ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts = facts(),
        ) = topicStatus(topic, f, progress)
        assertEquals(TopicStatus(StatusTone.LOCK, TopicStatusKind.CanLocked), status(empty, facts(can = FactoryCan)))
        assertEquals(TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Autostart.Unknown), status(empty))
        assertEquals(
            TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Autostart.BypassNotLearned),
            status(MountProgress(autostart = autostart(learned = false))),
        )
        assertEquals(
            TopicStatus(StatusTone.WARN, TopicStatusKind.Autostart.CloneNeeded),
            status(MountProgress(autostart = autostart(learned = true, clone = CloneState.REQUIRED))),
        )
        assertEquals(
            TopicStatus(StatusTone.OK, TopicStatusKind.Autostart.Ready),
            status(MountProgress(autostart = autostart(learned = true, clone = CloneState.DONE))),
        )
        val cloneOnly = facts(capabilities = setOf(AssistantCapability.CLONE))
        assertEquals(
            TopicStatus(StatusTone.WARN, TopicStatusKind.Autostart.CloneNeeded),
            status(MountProgress(autostart = autostart(learned = false, clone = CloneState.REQUIRED)), cloneOnly),
        )
    }

    @Test
    fun settings() {
        val topic = AssistantTopic.SETTINGS
        assertEquals(
            TopicStatus(StatusTone.LOCK, TopicStatusKind.CanLocked),
            topicStatus(topic, facts(can = FactoryCan), empty),
        )
        assertEquals(
            TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Settings.Unchanged),
            topicStatus(topic, facts(), empty),
        )
        assertEquals(
            TopicStatus(StatusTone.OK, TopicStatusKind.Settings.Changed(2)),
            topicStatus(topic, facts(), MountProgress(settingsChanged = 2)),
        )
    }

    @Test
    fun state() {
        val topic = AssistantTopic.STATE
        val calm = StateSummary(true, emptySet(), false, false, 12.6f, 20)
        assertEquals(
            TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.State.Unchecked),
            topicStatus(topic, facts(), empty),
        )
        assertEquals(
            TopicStatus(StatusTone.OK, TopicStatusKind.State.Ok),
            topicStatus(topic, facts(), MountProgress(state = calm)),
        )
        assertEquals(
            TopicStatus(StatusTone.WARN, TopicStatusKind.State.Warnings(2)),
            topicStatus(
                topic,
                facts(),
                MountProgress(state = calm.copy(openZones = setOf(StateZone.HOOD, StateZone.TRUNK))),
            ),
        )
    }

    @Test
    fun passport() {
        assertEquals(TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Passport.NotIssued), passportStatus(empty))
        assertEquals(
            TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Passport.NotIssued),
            passportStatus(MountProgress(passport = TodayPassport("p1", "20261119-1", issued = false))),
        )
        assertEquals(
            TopicStatus(StatusTone.OK, TopicStatusKind.Passport.Issued("20261119-1")),
            passportStatus(MountProgress(passport = TodayPassport("p1", "20261119-1", issued = true))),
        )
    }
}
