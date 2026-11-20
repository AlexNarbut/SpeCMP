package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.runningFold
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.speech.Speaker
import ru.alarmtrade.specmp.core.speech.SpeechMode

/** Строки собираются до первой фразы: FLUSH и следующие за ним QUEUE ставятся без пауз между вызовами. */
suspend fun Speaker.say(phrase: VoicePhrase) {
    val texts = phrase.lines.map { it.resolve() }
    texts.forEachIndexed { index, text -> speak(text, if (index == 0) phrase.mode else SpeechMode.QUEUE) }
}

/**
 * Озвучка экрана, пока идёт сбор: при включённой озвучке каждое значение [states] сравнивается с предыдущим
 * ([phrase], первое значение — с `null`: включили озвучку или вернулись на экран); выключение — [Speaker.stop].
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T : Any> Speaker.follow(
    enabled: Flow<Boolean>,
    states: Flow<T>,
    phrase: (previous: T?, current: T) -> VoicePhrase?,
) {
    enabled.distinctUntilChanged()
        .flatMapLatest { on -> if (on) states.cues(phrase) else flowOf(Cue.Silence) }
        .collect { cue ->
            when (cue) {
                Cue.Silence -> stop()
                is Cue.Say -> say(cue.phrase)
            }
        }
}

private sealed interface Cue {
    data object Silence : Cue

    data class Say(val phrase: VoicePhrase) : Cue
}

private class Transition<T : Any>(val previous: T?, val current: T)

private fun <T : Any> Flow<T>.cues(phrase: (previous: T?, current: T) -> VoicePhrase?): Flow<Cue> =
    runningFold<T, Transition<T>?>(null) { last, current -> Transition(last?.current, current) }
        .filterNotNull()
        .mapNotNull { transition -> phrase(transition.previous, transition.current)?.let(Cue::Say) }

@Suppress("SpreadOperator") // аргументов у строки — единицы, а getString принимает только vararg
private suspend fun VoiceText.resolve(): String = when (this) {
    is VoiceText.Raw -> text
    is VoiceText.Res -> getString(resource, *args.map { (it as? VoiceText)?.resolve() ?: it }.toTypedArray())
}
