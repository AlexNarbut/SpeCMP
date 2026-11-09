package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_can2_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_diag_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_door_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_door_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_efi_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_efi_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_general_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_immo_off_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_immo_off_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_izs_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_key_used_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_lada_can_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_lada_can_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_lada_vesta_patch_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_lada_vesta_patch_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_new_selected_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_new_unsupported_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_no_response_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_no_vin_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_not_supported_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_old_selected_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_old_unsupported_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_option_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_remote_start_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_repeat_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_rx_tx_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_start_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_start_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_timeout_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_timeout_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_critical_alarm
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_critical_ecu
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_critical_lada
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_critical_lada_vesta
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ecu
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ecu_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_engine_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_engine_start_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_engine_stop
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_engine_stop_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_hood_close
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_hood_close_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_hood_open
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_hood_open_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ignition_off
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ignition_off_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ignition_on
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ignition_on_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_learning
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_prepare
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_remove_key
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_remove_key_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_1_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_2_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_2_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_3_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_3_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_4_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_4_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_5_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_5_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_6_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_6_fail
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_6_forbidden
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_6_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_7_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_7_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_item_7_then
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_subtitle_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_title_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_1_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_2_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_3_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_3_count
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_4_action
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_4_count
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_4_then
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_item_5_response
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_subtitle_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_title_instruction
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartIllustration
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.IllustrationMode

private val txt = Strings.string

@Suppress("CyclomaticComplexMethod") // таблица соответствий шаг → иллюстрация
internal fun BypassStep.illustration(): AutostartIllustration {
    val done = kind == BypassStepKind.ACCEPTED
    return when (subject) {
        BypassSubject.PREPARE -> AutostartIllustration.IGNITION_OFF_REQUEST

        BypassSubject.IGNITION_ON ->
            if (done) AutostartIllustration.IGNITION_ON_DONE else AutostartIllustration.IGNITION_ON_REQUEST

        BypassSubject.IGNITION_OFF ->
            if (done) AutostartIllustration.IGNITION_OFF_DONE else AutostartIllustration.IGNITION_OFF_REQUEST

        BypassSubject.ENGINE_START ->
            if (done) AutostartIllustration.ENGINE_START_DONE else AutostartIllustration.ENGINE_START_REQUEST

        BypassSubject.ENGINE_STOP ->
            if (done) AutostartIllustration.ENGINE_STOP_DONE else AutostartIllustration.ENGINE_STOP_REQUEST

        BypassSubject.REMOVE_KEY -> AutostartIllustration.KEY_OUT

        BypassSubject.HOOD_OPEN ->
            if (done) AutostartIllustration.HOOD_OPEN_DONE else AutostartIllustration.HOOD_OPEN_REQUEST

        BypassSubject.HOOD_CLOSE ->
            if (done) AutostartIllustration.HOOD_CLOSE_DONE else AutostartIllustration.HOOD_CLOSE_REQUEST

        BypassSubject.ECU_EXCHANGE, BypassSubject.ECU_EXCHANGE_FINISHED, BypassSubject.ALARM_EXCHANGE ->
            AutostartIllustration.EXCHANGE

        BypassSubject.LEARNING -> AutostartIllustration.LEARNING

        BypassSubject.LADA_APS, BypassSubject.LADA_CAN, BypassSubject.LADA_VESTA_CAN -> AutostartIllustration.LADA

        BypassSubject.TOYOTA_DAIHATSU -> AutostartIllustration.TOYOTA
    }
}

internal fun BypassStepKind.illustrationMode(): IllustrationMode = when (this) {
    BypassStepKind.ACTION, BypassStepKind.INSTRUCTION -> IllustrationMode.Request
    BypassStepKind.ACCEPTED -> IllustrationMode.Done
    BypassStepKind.WAIT, BypassStepKind.CRITICAL -> IllustrationMode.Wait
}

/** У инструкций текста нет (`null`) — они показываются списком ([instruction]). */
@Suppress("CyclomaticComplexMethod")
internal fun BypassStep.text(): StringResource? {
    val done = kind == BypassStepKind.ACCEPTED
    return when (subject) {
        BypassSubject.PREPARE -> txt.autostart_bypass_item_prepare

        BypassSubject.IGNITION_ON ->
            if (done) txt.autostart_bypass_item_ignition_on_done else txt.autostart_bypass_item_ignition_on

        BypassSubject.IGNITION_OFF ->
            if (done) txt.autostart_bypass_item_ignition_off_done else txt.autostart_bypass_item_ignition_off

        BypassSubject.ENGINE_START ->
            if (done) txt.autostart_bypass_item_engine_start_done else txt.autostart_bypass_item_engine_start

        BypassSubject.ENGINE_STOP ->
            if (done) txt.autostart_bypass_item_engine_stop_done else txt.autostart_bypass_item_engine_stop

        BypassSubject.REMOVE_KEY ->
            if (done) txt.autostart_bypass_item_remove_key_done else txt.autostart_bypass_item_remove_key

        BypassSubject.HOOD_OPEN ->
            if (done) txt.autostart_bypass_item_hood_open_done else txt.autostart_bypass_item_hood_open

        BypassSubject.HOOD_CLOSE ->
            if (done) txt.autostart_bypass_item_hood_close_done else txt.autostart_bypass_item_hood_close

        BypassSubject.ECU_EXCHANGE -> if (kind == BypassStepKind.CRITICAL) {
            txt.autostart_bypass_item_critical_ecu
        } else {
            txt.autostart_bypass_item_ecu
        }

        BypassSubject.ECU_EXCHANGE_FINISHED -> txt.autostart_bypass_item_ecu_done

        BypassSubject.ALARM_EXCHANGE -> txt.autostart_bypass_item_critical_alarm

        BypassSubject.LEARNING -> txt.autostart_bypass_item_learning

        BypassSubject.LADA_CAN -> txt.autostart_bypass_item_critical_lada

        BypassSubject.LADA_VESTA_CAN -> txt.autostart_bypass_item_critical_lada_vesta

        BypassSubject.LADA_APS, BypassSubject.TOYOTA_DAIHATSU -> null
    }
}

internal data class ErrorTexts(val what: StringResource?, val check: StringResource?)

@Suppress("CyclomaticComplexMethod")
internal fun BypassError.texts(): ErrorTexts = when (this) {
    BypassError.GENERAL -> ErrorTexts(null, txt.autostart_bypass_error_general_check)

    BypassError.START_FAILED ->
        ErrorTexts(txt.autostart_bypass_error_start_what, txt.autostart_bypass_error_start_check)

    BypassError.OPTION_NOT_SELECTED -> ErrorTexts(txt.autostart_bypass_error_option_what, null)

    BypassError.NOT_SUPPORTED -> ErrorTexts(txt.autostart_bypass_error_not_supported_what, null)

    BypassError.TOYOTA_EFI ->
        ErrorTexts(txt.autostart_bypass_error_efi_what, txt.autostart_bypass_error_efi_check)

    BypassError.TOYOTA_DIAG_BLOCKED -> ErrorTexts(txt.autostart_bypass_error_diag_what, null)

    BypassError.TOYOTA_REMOTE_START_04, BypassError.TOYOTA_REMOTE_START_05 ->
        ErrorTexts(txt.autostart_bypass_error_remote_start_what, null)

    BypassError.TOYOTA_CHECK_RX_TX -> ErrorTexts(null, txt.autostart_bypass_error_rx_tx_check)

    BypassError.KIA_IGNITION_TIMEOUT ->
        ErrorTexts(txt.autostart_bypass_error_timeout_what, txt.autostart_bypass_error_timeout_check)

    BypassError.KIA_OLD_ALGORITHM_02, BypassError.KIA_OLD_ALGORITHM_03 ->
        ErrorTexts(txt.autostart_bypass_error_old_selected_what, null)

    BypassError.KIA_NEW_ALGORITHM -> ErrorTexts(txt.autostart_bypass_error_new_selected_what, null)

    BypassError.KIA_IMMOBILIZER_OFF ->
        ErrorTexts(txt.autostart_bypass_error_immo_off_what, txt.autostart_bypass_error_immo_off_check)

    BypassError.VAG_CAN_BUS -> ErrorTexts(null, txt.autostart_bypass_error_can2_check)

    BypassError.VAG_OLD_ALGORITHM_02 -> ErrorTexts(txt.autostart_bypass_error_old_unsupported_what, null)

    BypassError.VAG_OLD_ALGORITHM_03, BypassError.VAG_NEW_ALGORITHM ->
        ErrorTexts(txt.autostart_bypass_error_new_unsupported_what, null)

    BypassError.VAG_ECU_EXCHANGE -> ErrorTexts(null, null)

    BypassError.MERCEDES_DRIVER_DOOR ->
        ErrorTexts(txt.autostart_bypass_error_door_what, txt.autostart_bypass_error_door_check)

    BypassError.MERCEDES_NO_RESPONSE_02, BypassError.MERCEDES_NO_RESPONSE_04, BypassError.MERCEDES_NO_RESPONSE_0F ->
        ErrorTexts(txt.autostart_bypass_error_no_response_what, null)

    BypassError.MERCEDES_KEY_IZS -> ErrorTexts(txt.autostart_bypass_error_izs_what, null)

    BypassError.MERCEDES_KEY_USED -> ErrorTexts(txt.autostart_bypass_error_key_used_what, null)

    BypassError.MERCEDES_NO_VIN -> ErrorTexts(txt.autostart_bypass_error_no_vin_what, null)

    BypassError.LADA_CAN_BUS ->
        ErrorTexts(txt.autostart_bypass_error_lada_can_what, txt.autostart_bypass_error_lada_can_check)

    BypassError.LADA_REPEAT -> ErrorTexts(null, txt.autostart_bypass_error_repeat_check)

    BypassError.LADA_VESTA_CAN_PATCH_SEND ->
        ErrorTexts(txt.autostart_bypass_error_lada_vesta_patch_what, txt.autostart_bypass_error_lada_vesta_patch_check)
}

internal enum class InstructionKey { RED, BLACK }

internal data class InstructionItem(
    val action: StringResource? = null,
    val count: StringResource? = null,
    val forbidden: StringResource? = null,
    val response: StringResource? = null,
    val wait: Boolean = false,
    val fail: StringResource? = null,
    val then: StringResource? = null,
    val key: InstructionKey? = null,
)

internal data class Instruction(val title: StringResource, val lead: StringResource, val items: List<InstructionItem>)

internal fun BypassSubject.instruction(): Instruction? = when (this) {
    BypassSubject.LADA_APS -> LadaInstruction
    BypassSubject.TOYOTA_DAIHATSU -> ToyotaInstruction
    else -> null
}

private val LadaInstruction = Instruction(
    title = txt.autostart_lada_title_instruction,
    lead = txt.autostart_lada_subtitle_instruction,
    items = listOf(
        InstructionItem(action = txt.autostart_lada_item_1_action, key = InstructionKey.RED),
        InstructionItem(
            action = txt.autostart_lada_item_2_action,
            response = txt.autostart_lada_item_2_response,
            key = InstructionKey.RED,
        ),
        InstructionItem(
            action = txt.autostart_lada_item_3_action,
            response = txt.autostart_lada_item_3_response,
            key = InstructionKey.BLACK,
        ),
        InstructionItem(
            action = txt.autostart_lada_item_4_action,
            response = txt.autostart_lada_item_4_response,
            key = InstructionKey.BLACK,
        ),
        InstructionItem(
            action = txt.autostart_lada_item_5_action,
            response = txt.autostart_lada_item_5_response,
            key = InstructionKey.RED,
        ),
        InstructionItem(
            action = txt.autostart_lada_item_6_action,
            forbidden = txt.autostart_lada_item_6_forbidden,
            response = txt.autostart_lada_item_6_response,
            fail = txt.autostart_lada_item_6_fail,
            key = InstructionKey.RED,
        ),
        InstructionItem(
            action = txt.autostart_lada_item_7_action,
            response = txt.autostart_lada_item_7_response,
            then = txt.autostart_lada_item_7_then,
            key = InstructionKey.RED,
        ),
    ),
)

private val ToyotaInstruction = Instruction(
    title = txt.autostart_toyota_title_instruction,
    lead = txt.autostart_toyota_subtitle_instruction,
    items = listOf(
        InstructionItem(action = txt.autostart_toyota_item_1_action),
        InstructionItem(action = txt.autostart_toyota_item_2_action),
        InstructionItem(action = txt.autostart_toyota_item_3_action, count = txt.autostart_toyota_item_3_count),
        InstructionItem(
            action = txt.autostart_toyota_item_4_action,
            count = txt.autostart_toyota_item_4_count,
            then = txt.autostart_toyota_item_4_then,
        ),
        InstructionItem(response = txt.autostart_toyota_item_5_response, wait = true),
    ),
)
