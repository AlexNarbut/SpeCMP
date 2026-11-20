package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_failed
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_signal
import ru.alarmtrade.specmp.core.localization.resources.fastmount_voice_format_skipped
import ru.alarmtrade.specmp.core.speech.SpeechMode
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoiceText
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTask
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus

private val txt = Strings.string

/** Чек-лист озвучивается только на шаге проверки системы; на остальных шагах — пустой. */
internal fun FastMountState.voiceChecklist(): MountChecklist {
    val onCheck = screen == WizardScreen.MASTER && master.screen == MasterScreen.SYSTEM_CHECK
    return if (onCheck) master.checklist else MountChecklist()
}

/**
 * Чек-лист проверки системы: итоги закрытых заданий (автозадание — «есть сигнал», провал, пропуск) и «Проверка
 * завершена» — в очередь, затем новое активное задание. Без итогов новое задание прерывает прежнюю фразу. Пустой
 * [previous] — шаг только открыт, озвучку включили или вернулись: текущее задание (или итог, если всё закрыто).
 */
internal fun checklistPhrase(previous: MountChecklist?, current: MountChecklist): VoicePhrase? {
    if (current.tasks.isEmpty()) return null
    val before = previous?.tasks?.associateBy { it.id }.orEmpty()
    val active = current.tasks.firstOrNull { it.active }
    if (before.isEmpty()) {
        val line = if (current.allClosed) current.summary() else active?.let { VoiceText.Raw(it.text) }
        return line?.let { VoicePhrase(listOf(it)) }
    }
    val closed = current.tasks.filter { it.status != MountTaskStatus.PENDING && before[it.id]?.isPending() == true }
    // «Завершить» с незакрытыми заданиями пропускает их все разом — перечислять их незачем, хватит итога.
    val bulkSkip = closed.count { it.status == MountTaskStatus.SKIPPED } > 1
    val results = if (bulkSkip) emptyList() else closed.mapNotNull { it.result() }
    val finished = current.allClosed && previous?.allClosed != true
    val next = active?.takeIf { before[it.id]?.active != true }?.let { VoiceText.Raw(it.text) }
    val lines = results + listOfNotNull(current.summary().takeIf { finished }, next)
    if (lines.isEmpty()) return null
    val mode = if (results.isEmpty() && !finished) SpeechMode.FLUSH else SpeechMode.QUEUE
    return VoicePhrase(lines, mode)
}

private fun MountTask.result(): VoiceText? = when (status) {
    MountTaskStatus.PASSED -> auto?.let { voiceText(txt.fastmount_voice_format_signal, text) }
    MountTaskStatus.FAILED -> voiceText(txt.fastmount_voice_format_failed, text)
    MountTaskStatus.SKIPPED -> voiceText(txt.fastmount_voice_format_skipped, text)
    MountTaskStatus.PENDING -> null
}

private fun MountTask.isPending(): Boolean = status == MountTaskStatus.PENDING

private fun MountChecklist.summary(): VoiceText =
    voiceText(txt.fastmount_voice_format_done, tasks.size - skipped - failed, tasks.size)
