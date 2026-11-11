package ru.alarmtrade.specmp.feature.mountassistant.domain.model

enum class AssistantTopic { FIRMWARE, AUTOSTART, SETTINGS, STATE, VEHICLE, SUPPORT }

/** IN_FEED — ответ карточкой в журнале; HANDOFF — открывает экран другой фичи. */
enum class ActionKind { IN_FEED, HANDOFF }

/**
 * [capabilities] — нужна хотя бы одна; [network] — нужна сеть; [updatedCan] — закрыто заводским CAN (если CAN есть);
 * [bluetooth] — только по Bluetooth.
 */
enum class AssistantAction(
    val topic: AssistantTopic,
    val kind: ActionKind,
    val capabilities: Set<AssistantCapability> = emptySet(),
    val network: Boolean = false,
    val updatedCan: Boolean = false,
    val bluetooth: Boolean = false,
) {
    FIRMWARE_UPDATE(
        AssistantTopic.FIRMWARE,
        ActionKind.HANDOFF,
        setOf(AssistantCapability.FIRMWARE),
        network = true,
    ),
    FIRMWARE_CHECK(AssistantTopic.FIRMWARE, ActionKind.IN_FEED, setOf(AssistantCapability.FIRMWARE), network = true),
    CAN_UPDATE(AssistantTopic.FIRMWARE, ActionKind.HANDOFF, network = true),
    FIRMWARE_ARCHIVE(
        AssistantTopic.FIRMWARE,
        ActionKind.HANDOFF,
        setOf(AssistantCapability.FIRMWARE),
        network = true,
    ),
    AUTOSTART_STATUS(
        AssistantTopic.AUTOSTART,
        ActionKind.IN_FEED,
        setOf(AssistantCapability.BYPASS, AssistantCapability.CLONE),
        updatedCan = true,
    ),
    BYPASS_RUN(AssistantTopic.AUTOSTART, ActionKind.HANDOFF, setOf(AssistantCapability.BYPASS), updatedCan = true),
    CLONE_RUN(
        AssistantTopic.AUTOSTART,
        ActionKind.HANDOFF,
        setOf(AssistantCapability.CLONE),
        network = true,
        updatedCan = true,
    ),
    SETTINGS_FIND(AssistantTopic.SETTINGS, ActionKind.IN_FEED, setOf(AssistantCapability.SETTINGS), updatedCan = true),
    SETTINGS_LOAD(AssistantTopic.SETTINGS, ActionKind.IN_FEED, setOf(AssistantCapability.SETTINGS), updatedCan = true),
    SETTINGS_ALL(AssistantTopic.SETTINGS, ActionKind.HANDOFF, setOf(AssistantCapability.SETTINGS), updatedCan = true),
    CONFIGURATOR(AssistantTopic.SETTINGS, ActionKind.HANDOFF, setOf(AssistantCapability.SETTINGS)),
    STATE(AssistantTopic.STATE, ActionKind.IN_FEED, bluetooth = true),
    STATE_FULL(AssistantTopic.STATE, ActionKind.HANDOFF, bluetooth = true),
    VEHICLES(AssistantTopic.VEHICLE, ActionKind.HANDOFF),
    SUPPORT(AssistantTopic.SUPPORT, ActionKind.HANDOFF),
    ;

    companion object {
        /** Что доступно блоку, модели которого нет в базе. */
        val ForUnknownModel: Set<AssistantAction> =
            setOf(FIRMWARE_CHECK, FIRMWARE_UPDATE, FIRMWARE_ARCHIVE, VEHICLES, SUPPORT)

        /** Быстрые действия свёрнутого пульта. */
        val Quick: List<AssistantAction> = listOf(STATE, SETTINGS_LOAD, SETTINGS_ALL)

        fun of(topic: AssistantTopic): List<AssistantAction> = entries.filter { it.topic == topic }
    }
}
