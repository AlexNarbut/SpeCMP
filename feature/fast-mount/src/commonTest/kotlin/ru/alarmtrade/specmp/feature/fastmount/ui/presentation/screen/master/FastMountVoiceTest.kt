package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_failed
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_signal
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_skipped
import ru.alarmtrade.specmp.core.speech.SpeechMode
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoiceText
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText
import ru.alarmtrade.specmp.feature.fastmount.domain.model.AutoTaskKind
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTask
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus

class FastMountVoiceTest {

    private val txt = Strings.string

    private fun task(
        id: Int,
        status: MountTaskStatus = MountTaskStatus.PENDING,
        active: Boolean = false,
        auto: AutoTaskKind? = null,
    ) = MountTask(id, "Задание $id", status, active, auto, options = emptyList())

    private fun list(vararg tasks: MountTask) = MountChecklist(tasks.toList())

    private val pending = list(task(1, active = true, auto = AutoTaskKind.HOOD), task(2), task(3))

    @Test
    fun `empty checklist is silent`() {
        assertNull(checklistPhrase(null, MountChecklist()))
    }

    @Test
    fun `opened step speaks the active task`() {
        assertEquals(VoicePhrase(listOf(VoiceText.Raw("Задание 1"))), checklistPhrase(null, pending))
        assertEquals(VoicePhrase(listOf(VoiceText.Raw("Задание 1"))), checklistPhrase(MountChecklist(), pending))
    }

    @Test
    fun `opened step with everything closed speaks the summary`() {
        val done = list(task(1, MountTaskStatus.PASSED), task(2, MountTaskStatus.FAILED))
        assertEquals(VoicePhrase(listOf(voiceText(txt.fastmount_voice_format_done, 1, 2))), checklistPhrase(null, done))
    }

    @Test
    fun `auto task closed by signal is queued before the next task`() {
        val next = list(task(1, MountTaskStatus.PASSED, auto = AutoTaskKind.HOOD), task(2, active = true), task(3))
        assertEquals(
            VoicePhrase(
                listOf(voiceText(txt.fastmount_voice_format_signal, "Задание 1"), VoiceText.Raw("Задание 2")),
                SpeechMode.QUEUE,
            ),
            checklistPhrase(pending, next),
        )
    }

    @Test
    fun `manual task passed speaks only the next task`() {
        val before = list(task(1, active = true), task(2))
        val after = list(task(1, MountTaskStatus.PASSED), task(2, active = true))
        assertEquals(VoicePhrase(listOf(VoiceText.Raw("Задание 2"))), checklistPhrase(before, after))
    }

    @Test
    fun `failed and skipped tasks are queued`() {
        val before = list(task(1, active = true), task(2), task(3))
        assertEquals(
            VoicePhrase(listOf(voiceText(txt.fastmount_voice_format_failed, "Задание 1")), SpeechMode.QUEUE),
            checklistPhrase(before, list(task(1, MountTaskStatus.FAILED), task(2), task(3))),
        )
        assertEquals(
            VoicePhrase(listOf(voiceText(txt.fastmount_voice_format_skipped, "Задание 1")), SpeechMode.QUEUE),
            checklistPhrase(before, list(task(1, MountTaskStatus.SKIPPED), task(2), task(3))),
        )
    }

    @Test
    fun `last task closed adds the summary`() {
        val before = list(task(1, MountTaskStatus.PASSED), task(2, active = true))
        val after = list(task(1, MountTaskStatus.PASSED), task(2, MountTaskStatus.SKIPPED))
        assertEquals(
            VoicePhrase(
                listOf(
                    voiceText(txt.fastmount_voice_format_skipped, "Задание 2"),
                    voiceText(txt.fastmount_voice_format_done, 1, 2),
                ),
                SpeechMode.QUEUE,
            ),
            checklistPhrase(before, after),
        )
    }

    @Test
    fun `finishing with open tasks speaks only the summary`() {
        val before = list(task(1, MountTaskStatus.PASSED), task(2, active = true), task(3))
        val after =
            list(task(1, MountTaskStatus.PASSED), task(2, MountTaskStatus.SKIPPED), task(3, MountTaskStatus.SKIPPED))
        assertEquals(
            VoicePhrase(listOf(voiceText(txt.fastmount_voice_format_done, 1, 3)), SpeechMode.QUEUE),
            checklistPhrase(before, after),
        )
    }

    @Test
    fun `no visible change is silent`() {
        assertNull(checklistPhrase(pending, pending))
    }

    @Test
    fun `checklist is voiced only on the system check step`() {
        val checklist = list(task(1, active = true))
        val state = FastMountState(
            screen = WizardScreen.MASTER,
            master = MasterState(step = FastMountStep.SYSTEM_CHECK, checklist = checklist),
        )
        assertEquals(checklist, state.voiceChecklist())
        assertEquals(
            MountChecklist(),
            state.copy(master = state.master.copy(step = FastMountStep.FEEDBACK)).voiceChecklist(),
        )
        assertEquals(MountChecklist(), state.copy(screen = WizardScreen.SUMMARY).voiceChecklist())
    }
}
