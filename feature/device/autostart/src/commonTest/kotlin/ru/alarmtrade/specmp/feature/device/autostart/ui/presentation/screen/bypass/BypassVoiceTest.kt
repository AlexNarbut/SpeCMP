package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_ecu_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_error_option_what
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_critical_lada
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ecu
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_hood_open
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_hood_open_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_msg_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_clone_required
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_error
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_title_not_supported
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_title_interrupted
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_subtitle_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_lada_title_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_subtitle_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_toyota_title_instruction
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_msg_bypass_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_msg_bypass_start
import ru.alarmtrade.specmp.core.speech.SpeechMode
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText

class BypassVoiceTest {

    private val txt = Strings.string

    private fun running(kind: BypassStepKind, subject: BypassSubject) =
        bypassPhrase(BypassPhase.Running(BypassStep(kind, subject)))

    @Test
    fun `plain start is silent`() {
        assertNull(bypassPhrase(BypassPhase.Start()))
    }

    @Test
    fun `entering program mode announces the start`() {
        assertEquals(voicePhrase(voiceText(txt.autostart_voice_msg_bypass_start)), bypassPhrase(BypassPhase.Running()))
    }

    @Test
    fun `action step speaks the command`() {
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_item_hood_open)),
            running(BypassStepKind.ACTION, BypassSubject.HOOD_OPEN),
        )
    }

    @Test
    fun `accepted step speaks the card text`() {
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_item_hood_open_done)),
            running(BypassStepKind.ACCEPTED, BypassSubject.HOOD_OPEN),
        )
    }

    @Test
    fun `wait step speaks the card text`() {
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_item_ecu)),
            running(BypassStepKind.WAIT, BypassSubject.ECU_EXCHANGE),
        )
    }

    @Test
    fun `critical step adds do not interrupt`() {
        assertEquals(
            voicePhrase(
                voiceText(txt.autostart_bypass_item_critical_lada),
                voiceText(txt.autostart_bypass_title_critical),
            ),
            running(BypassStepKind.CRITICAL, BypassSubject.LADA_CAN),
        )
    }

    @Test
    fun `instruction step speaks its title and lead`() {
        assertEquals(
            voicePhrase(
                voiceText(txt.autostart_lada_title_instruction),
                voiceText(txt.autostart_lada_subtitle_instruction),
            ),
            running(BypassStepKind.INSTRUCTION, BypassSubject.LADA_APS),
        )
        assertEquals(
            voicePhrase(
                voiceText(txt.autostart_toyota_title_instruction),
                voiceText(txt.autostart_toyota_subtitle_instruction),
            ),
            running(BypassStepKind.INSTRUCTION, BypassSubject.TOYOTA_DAIHATSU),
        )
    }

    @Test
    fun `every step has a phrase that flushes the previous one`() {
        BypassStepKind.entries.forEach { kind ->
            BypassSubject.entries.forEach { subject ->
                val phrase = running(kind, subject)
                if (BypassStep(kind, subject).text() != null || subject.instruction() != null) {
                    assertEquals(SpeechMode.FLUSH, phrase?.mode, "$kind $subject")
                }
            }
        }
    }

    @Test
    fun `results are spoken`() {
        assertEquals(
            voicePhrase(voiceText(txt.autostart_voice_msg_bypass_done)),
            bypassPhrase(BypassPhase.Finished(BypassResult.Success)),
        )
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_title_clone_required)),
            bypassPhrase(BypassPhase.Finished(BypassResult.CloneRequired)),
        )
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_title_not_supported)),
            bypassPhrase(BypassPhase.Finished(BypassResult.NotSupported)),
        )
    }

    @Test
    fun `error speaks the title and what happened`() {
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_title_error), voiceText(txt.autostart_bypass_error_option_what)),
            bypassPhrase(BypassPhase.Finished(BypassResult.Error(BypassError.OPTION_NOT_SELECTED))),
        )
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_title_error)),
            bypassPhrase(BypassPhase.Finished(BypassResult.Error(BypassError.GENERAL))),
        )
        assertEquals(
            voicePhrase(voiceText(txt.autostart_bypass_title_error), voiceText(txt.autostart_bypass_error_ecu_what, 7)),
            bypassPhrase(BypassPhase.Finished(BypassResult.Error(BypassError.VAG_ECU_EXCHANGE, code = 7))),
        )
    }

    @Test
    fun `connection loss on a step is spoken`() {
        assertEquals(
            voicePhrase(
                voiceText(txt.autostart_common_title_interrupted),
                voiceText(txt.autostart_bypass_msg_interrupted),
            ),
            bypassPhrase(BypassPhase.Start(interrupted = true)),
        )
    }
}
