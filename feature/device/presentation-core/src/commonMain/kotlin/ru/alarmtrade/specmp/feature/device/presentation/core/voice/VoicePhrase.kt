package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.speech.SpeechMode

/** Строка озвучки; аргументы [Res] — числа, строки или вложенные [VoiceText]. */
sealed interface VoiceText {
    data class Res(val resource: StringResource, val args: List<Any> = emptyList()) : VoiceText

    /** Текст из данных (задание SDK), а не из ресурсов. */
    data class Raw(val text: String) : VoiceText
}

/** Строки звучат по очереди: первая — в режиме [mode], остальные — следом ([SpeechMode.QUEUE]). */
data class VoicePhrase(val lines: List<VoiceText>, val mode: SpeechMode = SpeechMode.FLUSH)

fun voiceText(resource: StringResource, vararg args: Any): VoiceText = VoiceText.Res(resource, args.toList())

fun voicePhrase(vararg lines: VoiceText, mode: SpeechMode = SpeechMode.FLUSH): VoicePhrase =
    VoicePhrase(lines.toList(), mode)

/** Та же строка на экране: озвучка и UI берут текст из одного места. */
@Suppress("SpreadOperator") // аргументов у строки — единицы, а stringResource принимает только vararg
@Composable
fun VoiceText.text(): String = when (this) {
    is VoiceText.Raw -> text
    is VoiceText.Res -> stringResource(resource, *args.map { (it as? VoiceText)?.text() ?: it }.toTypedArray())
}
