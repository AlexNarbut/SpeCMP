package ru.alarmtrade.specmp.core.speech.piper

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private val POLL_INTERVAL = 20.milliseconds

/** Ждёт, пока устройство проиграет [frames] кадров; ожидание отменяемое (FLUSH/stop обрывают звук). */
internal suspend fun awaitPlayed(frames: Int, playedFrames: () -> Int) {
    while (playedFrames() < frames) delay(POLL_INTERVAL)
}
