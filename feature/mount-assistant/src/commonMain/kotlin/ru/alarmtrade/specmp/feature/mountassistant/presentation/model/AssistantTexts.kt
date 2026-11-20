package ru.alarmtrade.specmp.feature.mountassistant.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_autostart_status
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_bypass_run
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_can_update
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_clone_run
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_configurator
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_firmware_archive
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_firmware_check
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_firmware_update
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_firmware_update_to
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_mount_passport
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_pairing
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_product_info
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_settings_all
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_settings_find
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_settings_load
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_state
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_state_full
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_support
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_system_devices
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_ticket_new
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_tickets
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_vehicles
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_block_label_factory_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_block_label_no_network
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_block_msg_factory_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_block_msg_no_network
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_off
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_on
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_autostart_unknown
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_bypass_not_learned
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_clone_required
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_factory_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_firmware_outdated
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_firmware_unchecked
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_nothing_left
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_passport
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_route_done
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_state_unchecked
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_unknown_model_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_next_msg_unknown_model_support
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_autostart
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_devices
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_passport
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_settings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_state
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_support
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_value_keywords_vehicle
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_autostart_ready
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_autostart_unknown
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_bypass_not_learned
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_can_locked
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_clone_needed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_devices
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_factory_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_firmware_actual
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_firmware_unchecked
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_firmware_update
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_no_network
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_passport_issued
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_passport_none
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_settings_changed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_settings_unchanged
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_state_ok
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_state_unchecked
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_state_warnings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_support
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_status_label_vehicle
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_topic_title_autostart
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_topic_title_devices
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_topic_title_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_topic_title_settings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_topic_title_state
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_topic_title_support
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_topic_title_vehicle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.NextStepReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingEditor
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TopicStatusKind

private val AssistantTopic.title: StringResource
    get() = when (this) {
        AssistantTopic.FIRMWARE -> Strings.string.mountassistant_topic_title_firmware
        AssistantTopic.AUTOSTART -> Strings.string.mountassistant_topic_title_autostart
        AssistantTopic.SETTINGS -> Strings.string.mountassistant_topic_title_settings
        AssistantTopic.STATE -> Strings.string.mountassistant_topic_title_state
        AssistantTopic.DEVICES -> Strings.string.mountassistant_topic_title_devices
        AssistantTopic.VEHICLE -> Strings.string.mountassistant_topic_title_vehicle
        AssistantTopic.SUPPORT -> Strings.string.mountassistant_topic_title_support
    }

private val AssistantTopic.keywords: StringResource
    get() = when (this) {
        AssistantTopic.FIRMWARE -> Strings.string.mountassistant_search_value_keywords_firmware
        AssistantTopic.AUTOSTART -> Strings.string.mountassistant_search_value_keywords_autostart
        AssistantTopic.SETTINGS -> Strings.string.mountassistant_search_value_keywords_settings
        AssistantTopic.STATE -> Strings.string.mountassistant_search_value_keywords_state
        AssistantTopic.DEVICES -> Strings.string.mountassistant_search_value_keywords_devices
        AssistantTopic.VEHICLE -> Strings.string.mountassistant_search_value_keywords_vehicle
        AssistantTopic.SUPPORT -> Strings.string.mountassistant_search_value_keywords_support
    }

private val AssistantAction.title: StringResource
    get() = when (this) {
        AssistantAction.FIRMWARE_UPDATE -> Strings.string.mountassistant_action_title_firmware_update
        AssistantAction.FIRMWARE_CHECK -> Strings.string.mountassistant_action_title_firmware_check
        AssistantAction.CAN_UPDATE -> Strings.string.mountassistant_action_title_can_update
        AssistantAction.FIRMWARE_ARCHIVE -> Strings.string.mountassistant_action_title_firmware_archive
        AssistantAction.AUTOSTART_STATUS -> Strings.string.mountassistant_action_title_autostart_status
        AssistantAction.BYPASS_RUN -> Strings.string.mountassistant_action_title_bypass_run
        AssistantAction.CLONE_RUN -> Strings.string.mountassistant_action_title_clone_run
        AssistantAction.SETTINGS_FIND -> Strings.string.mountassistant_action_title_settings_find
        AssistantAction.SETTINGS_LOAD -> Strings.string.mountassistant_action_title_settings_load
        AssistantAction.SETTINGS_ALL -> Strings.string.mountassistant_action_title_settings_all
        AssistantAction.CONFIGURATOR -> Strings.string.mountassistant_action_title_configurator
        AssistantAction.STATE -> Strings.string.mountassistant_action_title_state
        AssistantAction.STATE_FULL -> Strings.string.mountassistant_action_title_state_full
        AssistantAction.MOUNT_PASSPORT -> Strings.string.mountassistant_action_title_mount_passport
        AssistantAction.SYSTEM_DEVICES -> Strings.string.mountassistant_action_title_system_devices
        AssistantAction.PAIRING -> Strings.string.mountassistant_action_title_pairing
        AssistantAction.PRODUCT_INFO -> Strings.string.mountassistant_action_title_product_info
        AssistantAction.VEHICLES -> Strings.string.mountassistant_action_title_vehicles
        AssistantAction.TICKET_NEW -> Strings.string.mountassistant_action_title_ticket_new
        AssistantAction.TICKETS -> Strings.string.mountassistant_action_title_tickets
        AssistantAction.SUPPORT -> Strings.string.mountassistant_action_title_support
    }

@Composable
internal fun topicTitle(topic: AssistantTopic): String = stringResource(topic.title)

/** «Обновить до 3.57», если версия обновления известна. */
@Composable
internal fun actionTitle(action: AssistantAction, version: String? = null): String =
    if (action == AssistantAction.FIRMWARE_UPDATE && version != null) {
        stringResource(Strings.string.mountassistant_action_title_firmware_update_to, version)
    } else {
        stringResource(action.title)
    }

/** Названия и ключевые слова каталога для поиска: ядро языка интерфейса не знает. */
@Composable
internal fun rememberCatalogTexts(): CatalogTexts = CatalogTexts(
    actions = AssistantAction.entries.associateWith { stringResource(it.title) },
    topics = AssistantTopic.entries.associateWith { stringResource(it.title) },
    keywords = AssistantTopic.entries.associateWith { topic -> stringResource(topic.keywords).keywordList() },
    actionKeywords = mapOf(
        AssistantAction.MOUNT_PASSPORT to
            stringResource(Strings.string.mountassistant_search_value_keywords_passport).keywordList(),
    ),
)

private fun String.keywordList(): List<String> = split(',').map(String::trim).filter(String::isNotEmpty)

internal data class CatalogTexts(
    val actions: Map<AssistantAction, String>,
    val topics: Map<AssistantTopic, String>,
    val keywords: Map<AssistantTopic, List<String>>,
    val actionKeywords: Map<AssistantAction, List<String>>,
)

@Composable
internal fun statusText(kind: TopicStatusKind): String = when (kind) {
    is TopicStatusKind.Firmware -> firmwareStatusText(kind)

    is TopicStatusKind.Autostart -> autostartStatusText(kind)

    is TopicStatusKind.Settings -> settingsStatusText(kind)

    is TopicStatusKind.State -> stateStatusText(kind)

    TopicStatusKind.Devices -> stringResource(Strings.string.mountassistant_status_label_devices)

    TopicStatusKind.Vehicle -> stringResource(Strings.string.mountassistant_status_label_vehicle)

    TopicStatusKind.Support -> stringResource(Strings.string.mountassistant_status_label_support)

    TopicStatusKind.Passport.NotIssued -> stringResource(Strings.string.mountassistant_status_label_passport_none)

    is TopicStatusKind.Passport.Issued ->
        stringResource(Strings.string.mountassistant_status_label_passport_issued, kind.number.takeLast(SHORT_NUMBER))
}

private const val SHORT_NUMBER = 8

@Composable
private fun firmwareStatusText(kind: TopicStatusKind.Firmware): String = when (kind) {
    TopicStatusKind.Firmware.NoNetwork -> stringResource(Strings.string.mountassistant_status_label_no_network)

    TopicStatusKind.Firmware.FactoryCan -> stringResource(Strings.string.mountassistant_status_label_factory_can)

    is TopicStatusKind.Firmware.Unchecked ->
        stringResource(Strings.string.mountassistant_status_label_firmware_unchecked, kind.version)

    is TopicStatusKind.Firmware.Update ->
        stringResource(Strings.string.mountassistant_status_label_firmware_update, kind.version)

    TopicStatusKind.Firmware.Actual -> stringResource(Strings.string.mountassistant_status_label_firmware_actual)
}

@Composable
private fun autostartStatusText(kind: TopicStatusKind.Autostart): String = when (kind) {
    TopicStatusKind.CanLocked -> stringResource(Strings.string.mountassistant_status_label_can_locked)

    TopicStatusKind.Autostart.Unknown -> stringResource(Strings.string.mountassistant_status_label_autostart_unknown)

    TopicStatusKind.Autostart.BypassNotLearned ->
        stringResource(Strings.string.mountassistant_status_label_bypass_not_learned)

    TopicStatusKind.Autostart.CloneNeeded -> stringResource(Strings.string.mountassistant_status_label_clone_needed)

    TopicStatusKind.Autostart.Ready -> stringResource(Strings.string.mountassistant_status_label_autostart_ready)
}

@Composable
private fun settingsStatusText(kind: TopicStatusKind.Settings): String = when (kind) {
    TopicStatusKind.CanLocked -> stringResource(Strings.string.mountassistant_status_label_can_locked)

    is TopicStatusKind.Settings.Changed ->
        stringResource(Strings.string.mountassistant_status_label_settings_changed, kind.count)

    TopicStatusKind.Settings.Unchanged -> stringResource(Strings.string.mountassistant_status_label_settings_unchanged)
}

@Composable
private fun stateStatusText(kind: TopicStatusKind.State): String = when (kind) {
    TopicStatusKind.State.Unchecked -> stringResource(Strings.string.mountassistant_status_label_state_unchecked)

    is TopicStatusKind.State.Warnings ->
        stringResource(Strings.string.mountassistant_status_label_state_warnings, kind.count)

    TopicStatusKind.State.Ok -> stringResource(Strings.string.mountassistant_status_label_state_ok)
}

@Composable
internal fun nextStepText(reason: NextStepReason, firmwareVersion: String): String = when (reason) {
    NextStepReason.UNKNOWN_MODEL_FIRMWARE -> stringResource(
        Strings.string.mountassistant_next_msg_unknown_model_firmware,
    )

    NextStepReason.UNKNOWN_MODEL_SUPPORT -> stringResource(Strings.string.mountassistant_next_msg_unknown_model_support)

    NextStepReason.FACTORY_CAN -> stringResource(Strings.string.mountassistant_next_msg_factory_can)

    NextStepReason.FIRMWARE_UNCHECKED ->
        stringResource(Strings.string.mountassistant_next_msg_firmware_unchecked, firmwareVersion)

    NextStepReason.FIRMWARE_OUTDATED ->
        stringResource(Strings.string.mountassistant_next_msg_firmware_outdated, firmwareVersion)

    NextStepReason.AUTOSTART_UNKNOWN -> stringResource(Strings.string.mountassistant_next_msg_autostart_unknown)

    NextStepReason.BYPASS_NOT_LEARNED -> stringResource(Strings.string.mountassistant_next_msg_bypass_not_learned)

    NextStepReason.CLONE_REQUIRED -> stringResource(Strings.string.mountassistant_next_msg_clone_required)

    NextStepReason.STATE_UNCHECKED -> stringResource(Strings.string.mountassistant_next_msg_state_unchecked)

    NextStepReason.PASSPORT_NOT_ISSUED -> stringResource(Strings.string.mountassistant_next_msg_passport)

    NextStepReason.ROUTE_DONE -> stringResource(Strings.string.mountassistant_next_msg_route_done)

    NextStepReason.NOTHING_LEFT -> stringResource(Strings.string.mountassistant_next_msg_nothing_left)
}

@Composable
internal fun blockLabel(reason: BlockReason): String = when (reason) {
    BlockReason.FACTORY_CAN -> stringResource(Strings.string.mountassistant_block_label_factory_can)
    BlockReason.NO_NETWORK -> stringResource(Strings.string.mountassistant_block_label_no_network)
}

@Composable
internal fun blockMessage(reason: BlockReason): String = when (reason) {
    BlockReason.FACTORY_CAN -> stringResource(Strings.string.mountassistant_block_msg_factory_can)
    BlockReason.NO_NETWORK -> stringResource(Strings.string.mountassistant_block_msg_no_network)
}

/** Значение настройки так, как его покажет редактор: подпись переключателя, пункт списка, число с единицами. */
@Composable
internal fun settingValueText(setting: FoundSetting, value: AssistantSettingValue): String = when (value) {
    is AssistantSettingValue.Switch -> {
        val editor = setting.editor as? SettingEditor.Switch
        if (value.enabled) {
            editor?.onTitle ?: stringResource(Strings.string.mountassistant_card_value_on)
        } else {
            editor?.offTitle ?: stringResource(Strings.string.mountassistant_card_value_off)
        }
    }

    is AssistantSettingValue.Option ->
        (setting.editor as? SettingEditor.Options)?.options?.getOrNull(value.index) ?: value.index.toString()

    is AssistantSettingValue.Number -> numberText(value.value, (setting.editor as? SettingEditor.Number)?.units)
}

internal fun numberText(value: Int, units: String?): String = if (units.isNullOrBlank()) "$value" else "$value $units"
