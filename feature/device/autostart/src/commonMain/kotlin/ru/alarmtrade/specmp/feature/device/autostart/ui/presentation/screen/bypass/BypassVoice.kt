package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_ecu_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_msg_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_clone_required
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_error
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_not_supported
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_title_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_msg_bypass_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_msg_bypass_start
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoiceText
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText

private val txt = Strings.string

/** Фраза фазы обхода (звучит, когда фаза сменилась): старт, шаг — текст карточки, итог, обрыв связи. */
internal fun bypassPhrase(phase: BypassPhase): VoicePhrase? = when (phase) {
    is BypassPhase.Start -> if (phase.interrupted) {
        voicePhrase(voiceText(txt.autostart_common_title_interrupted), voiceText(txt.autostart_bypass_msg_interrupted))
    } else {
        null
    }

    is BypassPhase.Running -> if (phase.step == null) {
        voicePhrase(voiceText(txt.autostart_voice_msg_bypass_start))
    } else {
        stepPhrase(phase.step)
    }

    is BypassPhase.Finished -> resultPhrase(phase.result)
}

/** Инструкция (LADA APS, Toyota) — заголовок и вводная, сам список шагов не читается. */
private fun stepPhrase(step: BypassStep): VoicePhrase? {
    val text = step.text() ?: return step.subject.instruction()?.let {
        voicePhrase(voiceText(it.title), voiceText(it.lead))
    }
    return if (step.kind == BypassStepKind.CRITICAL) {
        voicePhrase(voiceText(text), voiceText(txt.autostart_bypass_title_critical))
    } else {
        voicePhrase(voiceText(text))
    }
}

private fun resultPhrase(result: BypassResult): VoicePhrase = when (result) {
    BypassResult.Success -> voicePhrase(voiceText(txt.autostart_voice_msg_bypass_done))
    BypassResult.CloneRequired -> voicePhrase(voiceText(txt.autostart_bypass_title_clone_required))
    BypassResult.NotSupported -> voicePhrase(voiceText(txt.autostart_bypass_title_not_supported))
    is BypassResult.Error -> VoicePhrase(listOfNotNull(voiceText(txt.autostart_bypass_title_error), result.what()))
}

/** «Что случилось» по ошибке обхода: карточка ошибки, заявка на техпомощь и озвучка. */
internal fun BypassResult.Error.what(): VoiceText? = if (error == BypassError.VAG_ECU_EXCHANGE) {
    voiceText(txt.autostart_bypass_error_ecu_what, code ?: 0)
} else {
    error.texts().what?.let { voiceText(it) }
}
