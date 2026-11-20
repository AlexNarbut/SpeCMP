package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_algorithm
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_attempts
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_calc
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_code
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_debug
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_denied
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_device_id
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_reading
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_request
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_required
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_unknown
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_unsupported
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_verifying
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_writing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_preparing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_processing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_queue
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_reading
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_verifying
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_writing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_interrupted_check
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_msg_interrupted_kept
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_already
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_error
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_not_supported
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_success
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_title_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_label_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_format_clone_stage
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoiceText
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText

private val txt = Strings.string

/**
 * Фраза фазы Clone (звучит, когда фаза сменилась): этап, на этапах записи — «Не прерывайте соединение», итог, обрыв.
 */
internal fun clonePhrase(phase: ClonePhase): VoicePhrase? = when (phase) {
    is ClonePhase.Start -> phase.interruption?.let { interruption ->
        val reason = when (interruption) {
            CloneInterruption.ATTEMPT_KEPT -> txt.autostart_clone_msg_interrupted_kept
            CloneInterruption.CHECK_ATTEMPTS -> txt.autostart_clone_msg_interrupted_check
        }
        voicePhrase(voiceText(txt.autostart_common_title_interrupted), voiceText(reason))
    }

    is ClonePhase.Running -> {
        val stage = voiceText(
            txt.autostart_voice_format_clone_stage,
            phase.stage.number,
            CloneStage.COUNT,
            voiceText(phase.stageTitle()),
        )
        if (phase.stage.locked) {
            voicePhrase(stage, voiceText(txt.autostart_status_label_critical))
        } else {
            voicePhrase(stage)
        }
    }

    is ClonePhase.Finished -> when (val result = phase.result) {
        is CloneResult.Completed -> voicePhrase(
            voiceText(if (result.already) txt.autostart_clone_title_already else txt.autostart_clone_title_success),
        )

        CloneResult.NotSupported -> voicePhrase(voiceText(txt.autostart_clone_title_not_supported))

        is CloneResult.Failed -> voicePhrase(voiceText(txt.autostart_clone_title_error), result.failure.message())
    }
}

internal fun ClonePhase.Running.stageTitle(): StringResource = when (stage) {
    CloneStage.PREPARING -> txt.autostart_clone_item_stage_preparing

    CloneStage.READING -> txt.autostart_clone_item_stage_reading

    CloneStage.CALCULATING -> if (inQueue) {
        txt.autostart_clone_item_stage_queue
    } else {
        txt.autostart_clone_item_stage_processing
    }

    CloneStage.WRITING -> txt.autostart_clone_item_stage_writing

    CloneStage.VERIFYING -> txt.autostart_clone_item_stage_verifying
}

/** Текст ошибки Clone — на экране, в заявке на техпомощь и в озвучке. */
@Suppress("CyclomaticComplexMethod") // таблица соответствий ошибка → текст
internal fun CloneFailure.message(): VoiceText = when (this) {
    CloneFailure.DATA -> voiceText(txt.autostart_clone_error_code, ERROR_CODE_DATA)
    CloneFailure.ENCRYPT -> voiceText(txt.autostart_clone_error_code, ERROR_CODE_ENCRYPT)
    CloneFailure.ALGORITHM -> voiceText(txt.autostart_clone_error_code, ERROR_CODE_ALGORITHM)
    CloneFailure.UNKNOWN -> voiceText(txt.autostart_clone_error_unknown)
    CloneFailure.READING -> voiceText(txt.autostart_clone_error_reading)
    CloneFailure.WRITING -> voiceText(txt.autostart_clone_error_writing)
    CloneFailure.VERIFYING -> voiceText(txt.autostart_clone_error_verifying)
    CloneFailure.REQUIRED -> voiceText(txt.autostart_clone_error_required)
    CloneFailure.CALC_ERROR -> voiceText(txt.autostart_clone_error_calc)
    CloneFailure.ALGORITHM_ERROR -> voiceText(txt.autostart_clone_error_algorithm)
    CloneFailure.WRONG_REQUEST -> voiceText(txt.autostart_clone_error_request)
    CloneFailure.DEBUG -> voiceText(txt.autostart_clone_error_debug)
    CloneFailure.UNSUPPORTED -> voiceText(txt.autostart_clone_error_unsupported)
    CloneFailure.WRONG_DEVICE_ID -> voiceText(txt.autostart_clone_error_device_id)
    CloneFailure.ATTEMPTS_EXCEEDED -> voiceText(txt.autostart_clone_error_attempts)
    CloneFailure.DEVICE_DENIED -> voiceText(txt.autostart_clone_error_denied)
}

private const val ERROR_CODE_DATA = 4
private const val ERROR_CODE_ENCRYPT = 5
private const val ERROR_CODE_ALGORITHM = 6
