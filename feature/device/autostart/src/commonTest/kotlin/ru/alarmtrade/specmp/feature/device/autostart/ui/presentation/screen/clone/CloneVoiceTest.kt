package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_code
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_error_reading
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_processing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_queue
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_reading
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_verifying
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
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText

class CloneVoiceTest {

    private val txt = Strings.string

    private fun stage(number: Int, title: StringResource) =
        voiceText(txt.autostart_voice_format_clone_stage, number, CloneStage.COUNT, voiceText(title))

    @Test
    fun `start without interruption is silent`() {
        assertNull(clonePhrase(ClonePhase.Start()))
    }

    @Test
    fun `stage speaks its number and title`() {
        assertEquals(
            voicePhrase(stage(2, txt.autostart_clone_item_stage_reading)),
            clonePhrase(ClonePhase.Running(CloneStage.READING)),
        )
    }

    @Test
    fun `server queue and processing are separate phrases`() {
        assertEquals(
            voicePhrase(stage(3, txt.autostart_clone_item_stage_queue)),
            clonePhrase(ClonePhase.Running(CloneStage.CALCULATING, inQueue = true)),
        )
        assertEquals(
            voicePhrase(stage(3, txt.autostart_clone_item_stage_processing)),
            clonePhrase(ClonePhase.Running(CloneStage.CALCULATING)),
        )
    }

    @Test
    fun `locked stages add do not interrupt`() {
        assertEquals(
            voicePhrase(
                stage(5, txt.autostart_clone_item_stage_verifying),
                voiceText(txt.autostart_status_label_critical),
            ),
            clonePhrase(ClonePhase.Running(CloneStage.VERIFYING)),
        )
        CloneStage.entries.forEach { stage ->
            val lines = clonePhrase(ClonePhase.Running(stage))?.lines.orEmpty()
            assertEquals(if (stage.locked) 2 else 1, lines.size, stage.name)
        }
    }

    @Test
    fun `results are spoken`() {
        assertEquals(
            voicePhrase(voiceText(txt.autostart_clone_title_success)),
            clonePhrase(ClonePhase.Finished(CloneResult.Completed(already = false))),
        )
        assertEquals(
            voicePhrase(voiceText(txt.autostart_clone_title_already)),
            clonePhrase(ClonePhase.Finished(CloneResult.Completed(already = true))),
        )
        assertEquals(
            voicePhrase(voiceText(txt.autostart_clone_title_not_supported)),
            clonePhrase(ClonePhase.Finished(CloneResult.NotSupported)),
        )
    }

    @Test
    fun `failure speaks the title and the error`() {
        assertEquals(
            voicePhrase(voiceText(txt.autostart_clone_title_error), voiceText(txt.autostart_clone_error_reading)),
            clonePhrase(ClonePhase.Finished(CloneResult.Failed(CloneFailure.READING))),
        )
        assertEquals(
            voicePhrase(voiceText(txt.autostart_clone_title_error), voiceText(txt.autostart_clone_error_code, 4)),
            clonePhrase(ClonePhase.Finished(CloneResult.Failed(CloneFailure.DATA))),
        )
    }

    @Test
    fun `interruption speaks whether the attempt was spent`() {
        assertEquals(
            voicePhrase(
                voiceText(txt.autostart_common_title_interrupted),
                voiceText(txt.autostart_clone_msg_interrupted_kept),
            ),
            clonePhrase(ClonePhase.Start(CloneInterruption.ATTEMPT_KEPT)),
        )
        assertEquals(
            voicePhrase(
                voiceText(txt.autostart_common_title_interrupted),
                voiceText(txt.autostart_clone_msg_interrupted_check),
            ),
            clonePhrase(ClonePhase.Start(CloneInterruption.CHECK_ATTEMPTS)),
        )
    }
}
