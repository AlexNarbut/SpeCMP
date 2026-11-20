package ru.alarmtrade.specmp.core.speech

import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

internal class DesktopSpeechEngine(
    private val os: DesktopOs,
    private val languageTag: String,
    private val io: CoroutineDispatcher,
    scope: CoroutineScope,
) : SpeechEngine {

    private val state = MutableStateFlow(
        if (os == DesktopOs.OTHER) SpeechAvailability.Unsupported else SpeechAvailability.Ready,
    )
    override val availability: StateFlow<SpeechAvailability> = state.asStateFlow()
    private val speakCommand = scope.async(io, start = CoroutineStart.LAZY) { resolveSpeakCommand() }

    override fun prepare() {
        speakCommand.start()
    }

    override suspend fun speak(text: String) {
        val command = speakCommand.await() ?: return
        withContext(io) {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start()
            try {
                process.outputStream.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                runInterruptible { process.waitFor() }
            } finally {
                process.destroyForcibly()
            }
        }
    }

    override fun release() = Unit

    override fun openVoiceInstall() = Unit

    private fun resolveSpeakCommand(): List<String>? = when (os) {
        DesktopOs.MAC -> resolveMac()
        DesktopOs.WINDOWS -> resolveWindows()
        DesktopOs.OTHER -> null
    }

    private fun resolveMac(): List<String>? {
        val output = run(DesktopSpeechCommands.sayVoices) ?: return unsupported()
        val voice = DesktopSpeechCommands.sayVoiceFor(DesktopSpeechCommands.parseSayVoices(output.text), languageTag)
            ?: return noVoice()
        return DesktopSpeechCommands.say(voice)
    }

    private fun resolveWindows(): List<String>? {
        val probe = run(DesktopSpeechCommands.windowsProbe(languageTag)) ?: return unsupported()
        return when (probe.exitCode) {
            0 -> DesktopSpeechCommands.windowsSpeak(languageTag)
            DesktopSpeechCommands.WINDOWS_NO_VOICE_EXIT -> noVoice()
            else -> unsupported()
        }
    }

    private fun noVoice(): List<String>? {
        state.update { SpeechAvailability.NoVoice(canInstall = false) }
        return null
    }

    private fun unsupported(): List<String>? {
        state.update { SpeechAvailability.Unsupported }
        return null
    }

    private fun run(command: List<String>): ProcessOutput? = try {
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        process.outputStream.close()
        val text = process.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        ProcessOutput(process.waitFor(), text)
    } catch (ignored: IOException) {
        null
    }

    private class ProcessOutput(val exitCode: Int, val text: String)
}
