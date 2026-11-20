package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.Availability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.NextStep
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.NextStepReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TopicAvailability

fun availability(action: AssistantAction, facts: DeviceFacts, progress: MountProgress): Availability = when {
    hidden(action, facts, progress) -> Availability.Hidden
    action.updatedCan && facts.factoryCan -> Availability.Blocked(BlockReason.FACTORY_CAN, AssistantAction.CAN_UPDATE)
    action.network && !facts.online -> Availability.Blocked(BlockReason.NO_NETWORK, fix = null)
    else -> Availability.Available
}

private fun hidden(action: AssistantAction, facts: DeviceFacts, progress: MountProgress): Boolean = when {
    !facts.inDatabase && action !in AssistantAction.ForUnknownModel -> true
    !facts.supports(action) -> true
    action == AssistantAction.FIRMWARE_UPDATE -> progress.firmware?.updateAvailable != true
    action == AssistantAction.CAN_UPDATE -> facts.can == null
    action == AssistantAction.PRODUCT_INFO -> !facts.productSearch
    else -> false
}

private fun DeviceFacts.supports(action: AssistantAction): Boolean {
    if (action.bluetooth && transport != AssistantTransport.BLE) return false
    if (!inDatabase || action.capabilities.isEmpty()) return true
    return action.capabilities.any(::has)
}

fun topics(facts: DeviceFacts, progress: MountProgress): List<TopicAvailability> =
    AssistantTopic.entries.mapNotNull { topic ->
        val visible = AssistantAction.of(topic).associateWith { availability(it, facts, progress) }
            .filterValues { it != Availability.Hidden }
        if (visible.isEmpty()) {
            null
        } else {
            TopicAvailability(topic, visible.keys.toList(), blocked = visible.values.all { it is Availability.Blocked })
        }
    }

/** Маршрут монтажа: первое невыполненное применимое звено. */
fun nextStep(facts: DeviceFacts, progress: MountProgress): NextStep {
    if (!facts.inDatabase) {
        return if (facts.online) {
            NextStep(AssistantAction.FIRMWARE_CHECK, NextStepReason.UNKNOWN_MODEL_FIRMWARE)
        } else {
            NextStep(AssistantAction.SUPPORT, NextStepReason.UNKNOWN_MODEL_SUPPORT)
        }
    }
    if (facts.factoryCan) return NextStep(AssistantAction.CAN_UPDATE, NextStepReason.FACTORY_CAN)
    return firmwareStep(facts, progress)
        ?: autostartStep(facts, progress)
        ?: stateStep(facts, progress)
        ?: passportStep(progress)
        ?: lastStep(facts)
}

private fun firmwareStep(facts: DeviceFacts, progress: MountProgress): NextStep? {
    if (!facts.has(AssistantCapability.FIRMWARE) || !facts.online) return null
    val check = progress.firmware ?: return NextStep(AssistantAction.FIRMWARE_CHECK, NextStepReason.FIRMWARE_UNCHECKED)
    return NextStep(AssistantAction.FIRMWARE_UPDATE, NextStepReason.FIRMWARE_OUTDATED).takeIf { check.updateAvailable }
}

private fun autostartStep(facts: DeviceFacts, progress: MountProgress): NextStep? {
    val bypass = facts.has(AssistantCapability.BYPASS)
    val clone = facts.has(AssistantCapability.CLONE)
    if (!bypass && !clone) return null
    val status = progress.autostart
        ?: return NextStep(AssistantAction.AUTOSTART_STATUS, NextStepReason.AUTOSTART_UNKNOWN)
    return when {
        bypass && !status.bypassLearned -> NextStep(AssistantAction.BYPASS_RUN, NextStepReason.BYPASS_NOT_LEARNED)

        clone && facts.online && status.clone in CloneOpen ->
            NextStep(AssistantAction.CLONE_RUN, NextStepReason.CLONE_REQUIRED)

        else -> null
    }
}

private val CloneOpen = setOf(CloneState.REQUIRED, CloneState.FAILED)

private fun stateStep(facts: DeviceFacts, progress: MountProgress): NextStep? =
    NextStep(AssistantAction.STATE, NextStepReason.STATE_UNCHECKED)
        .takeIf { facts.transport == AssistantTransport.BLE && progress.state == null }

/** Последнее звено маршрута — паспорт монтажа, пока сегодня он не оформлен. */
private fun passportStep(progress: MountProgress): NextStep? =
    NextStep(AssistantAction.MOUNT_PASSPORT, NextStepReason.PASSPORT_NOT_ISSUED)
        .takeIf { progress.passport?.issued != true }

private fun lastStep(facts: DeviceFacts): NextStep = if (facts.has(AssistantCapability.SETTINGS)) {
    NextStep(AssistantAction.SETTINGS_FIND, NextStepReason.ROUTE_DONE)
} else {
    NextStep(AssistantAction.SUPPORT, NextStepReason.NOTHING_LEFT)
}
