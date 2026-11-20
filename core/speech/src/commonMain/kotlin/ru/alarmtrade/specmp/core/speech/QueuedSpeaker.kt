package ru.alarmtrade.specmp.core.speech

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.safeRun

/** [stopRequests] — когда замолчать без участия экранов (приложение ушло в фон). */
internal class QueuedSpeaker(
    private val engine: SpeechEngine,
    private val scope: CoroutineScope,
    private val stopRequests: Flow<Unit> = emptyFlow(),
) : Speaker {

    private val queue = MutableStateFlow(SpeechQueue())
    private val started = MutableStateFlow(false)
    private val busy = MutableStateFlow(false)
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override val availability: StateFlow<SpeechAvailability> by lazy {
        engine.prepare()
        engine.availability
    }

    override val speaking: StateFlow<Boolean> = busy.asStateFlow()

    override fun speak(text: String, mode: SpeechMode) {
        if (text.isBlank()) return
        queue.update { it.enqueue(text, mode) }
        startWorker()
    }

    override fun stop() {
        queue.update { it.cleared() }
    }

    override fun openVoiceInstall() = engine.openVoiceInstall()

    private fun startWorker() {
        if (!started.compareAndSet(expect = false, update = true)) return
        scope.launch { stopRequests.collect { stop() } }
        scope.launch {
            engine.prepare()
            // Новое поколение (FLUSH, stop) отменяет текущую фразу: collectLatest дожидается старого блока.
            queue.map { it.generation }.distinctUntilChanged().collectLatest { generation -> drain(generation) }
        }
    }

    private suspend fun drain(generation: Long) {
        while (currentCoroutineContext().isActive) {
            val text = pop(generation)
            if (text == null) {
                busy.update { false }
                engine.release()
                queue.first { it.pending.isNotEmpty() }
            } else {
                busy.update { true }
                speakWithTimeout(text)
            }
        }
    }

    // Движок может не сообщить о конце фразы (Android TTS при падении нативного движка) — иначе очередь встанет.
    private suspend fun speakWithTimeout(text: String) {
        val timeout = SpeechTimeout.of(text)
        val finished = withTimeoutOrNull(timeout) { safeRun { engine.speak(text) } }
        if (finished == null) {
            logger.w { "Phrase timed out after $timeout, resetting engine" }
            safeRun { engine.reset() }
        }
    }

    private tailrec fun pop(generation: Long): String? {
        val current = queue.value
        if (current.generation != generation) return null
        val head = current.pending.firstOrNull() ?: return null
        return if (queue.compareAndSet(
                current,
                current.copy(pending = current.pending.drop(1)),
            )
        ) {
            head
        } else {
            pop(generation)
        }
    }
}

private data class SpeechQueue(
    val pending: List<String> = emptyList(),
    val last: String? = null,
    val generation: Long = 0,
) {
    fun enqueue(text: String, mode: SpeechMode): SpeechQueue = when {
        text == last -> this
        mode == SpeechMode.FLUSH -> SpeechQueue(pending = listOf(text), last = text, generation = generation + 1)
        else -> copy(pending = pending + text, last = text)
    }

    fun cleared(): SpeechQueue = SpeechQueue(generation = generation + 1)
}
