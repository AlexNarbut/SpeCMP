package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StatusTone
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TopicStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TopicStatusKind

fun topicStatus(topic: AssistantTopic, facts: DeviceFacts, progress: MountProgress): TopicStatus = when (topic) {
    AssistantTopic.FIRMWARE -> firmwareStatus(facts, progress)

    AssistantTopic.AUTOSTART -> autostartStatus(facts, progress.autostart)

    AssistantTopic.SETTINGS -> settingsStatus(facts, progress)

    AssistantTopic.STATE -> stateStatus(progress)

    AssistantTopic.DEVICES -> if (facts.factoryCan) {
        TopicStatus(StatusTone.LOCK, TopicStatusKind.CanLocked)
    } else {
        TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Devices)
    }

    AssistantTopic.VEHICLE -> TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Vehicle)

    AssistantTopic.SUPPORT -> TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Support)
}

private fun firmwareStatus(facts: DeviceFacts, progress: MountProgress): TopicStatus {
    val check = progress.firmware
    val latest = check?.main?.latest
    return when {
        !facts.online -> TopicStatus(StatusTone.OFF, TopicStatusKind.Firmware.NoNetwork)
        facts.factoryCan -> TopicStatus(StatusTone.WARN, TopicStatusKind.Firmware.FactoryCan)
        check == null -> TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Firmware.Unchecked(facts.firmwareVersion))
        check.updateAvailable && latest != null -> TopicStatus(StatusTone.WARN, TopicStatusKind.Firmware.Update(latest))
        else -> TopicStatus(StatusTone.OK, TopicStatusKind.Firmware.Actual)
    }
}

private fun autostartStatus(facts: DeviceFacts, status: AutostartStatus?): TopicStatus {
    val bypass = facts.has(AssistantCapability.BYPASS)
    val cloneOpen =
        facts.has(AssistantCapability.CLONE) && status?.clone in setOf(CloneState.REQUIRED, CloneState.FAILED)
    return when {
        facts.factoryCan -> TopicStatus(StatusTone.LOCK, TopicStatusKind.CanLocked)
        status == null -> TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Autostart.Unknown)
        bypass && !status.bypassLearned -> TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Autostart.BypassNotLearned)
        cloneOpen -> TopicStatus(StatusTone.WARN, TopicStatusKind.Autostart.CloneNeeded)
        else -> TopicStatus(StatusTone.OK, TopicStatusKind.Autostart.Ready)
    }
}

private fun settingsStatus(facts: DeviceFacts, progress: MountProgress): TopicStatus = when {
    facts.factoryCan -> TopicStatus(StatusTone.LOCK, TopicStatusKind.CanLocked)

    progress.settingsChanged > 0 -> TopicStatus(
        StatusTone.OK,
        TopicStatusKind.Settings.Changed(progress.settingsChanged),
    )

    else -> TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Settings.Unchanged)
}

private fun stateStatus(progress: MountProgress): TopicStatus {
    val state = progress.state
    return when {
        state == null -> TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.State.Unchecked)
        state.warnings > 0 -> TopicStatus(StatusTone.WARN, TopicStatusKind.State.Warnings(state.warnings))
        else -> TopicStatus(StatusTone.OK, TopicStatusKind.State.Ok)
    }
}

/** Плитка «Паспорт монтажа» пульта: оформлен ли сегодня паспорт блока. */
fun passportStatus(progress: MountProgress): TopicStatus {
    val passport = progress.passport?.takeIf { it.issued }
    return if (passport == null) {
        TopicStatus(StatusTone.NEUTRAL, TopicStatusKind.Passport.NotIssued)
    } else {
        TopicStatus(StatusTone.OK, TopicStatusKind.Passport.Issued(passport.number))
    }
}
