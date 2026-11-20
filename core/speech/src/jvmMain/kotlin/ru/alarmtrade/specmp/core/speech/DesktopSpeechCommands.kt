package ru.alarmtrade.specmp.core.speech

import java.util.Base64

internal enum class DesktopOs {
    MAC,
    WINDOWS,
    OTHER,
    ;

    companion object {
        fun of(osName: String): DesktopOs {
            val name = osName.lowercase()
            return when {
                name.startsWith("mac") || name.startsWith("darwin") -> MAC
                name.startsWith("windows") -> WINDOWS
                else -> OTHER
            }
        }
    }
}

internal data class SayVoice(val name: String, val locale: String)

/** Текст фразы в команды не попадает: его пишут в stdin процесса. */
internal object DesktopSpeechCommands {

    val sayVoices: List<String> = listOf("say", "-v", "?")

    private val sayVoiceLine = Regex("""^(.+?)\s+([A-Za-z]{2,3}[_-][A-Za-z0-9_-]+)\s+#""")
    private val languageTagFormat = Regex("[a-z]{2,3}")

    fun parseSayVoices(output: String): List<SayVoice> = output.lineSequence()
        .mapNotNull { line -> sayVoiceLine.find(line.trimEnd()) }
        .map { match -> SayVoice(name = match.groupValues[1].trim(), locale = match.groupValues[2]) }
        .toList()

    // В списке say есть «звуковые эффекты» (Bells, Zarvox…) — для подсказок берём обычный голос, лучше основной.
    fun sayVoiceFor(voices: List<SayVoice>, languageTag: String): SayVoice? {
        val language = languageTag.lowercase()
        val ofLanguage = voices.filter { it.locale.substringBefore('_').substringBefore('-').lowercase() == language }
        val regular = ofLanguage.filter { it.name !in sayNoveltyVoices }
        return regular.firstOrNull { it.name in sayPreferredVoices }
            ?: regular.firstOrNull { it.locale.equals("${language}_${defaultRegion(language)}", ignoreCase = true) }
            ?: regular.firstOrNull()
            ?: ofLanguage.firstOrNull()
    }

    private fun defaultRegion(language: String): String = sayDefaultRegions[language] ?: language.uppercase()

    private val sayPreferredVoices = setOf("Milena", "Samantha")
    private val sayDefaultRegions = mapOf("en" to "US")
    private val sayNoveltyVoices = setOf(
        "Albert", "Bad News", "Bahh", "Bells", "Boing", "Bubbles", "Cellos", "Fred", "Good News", "Jester",
        "Junior", "Kathy", "Organ", "Ralph", "Superstar", "Trinoids", "Whisper", "Wobble", "Zarvox",
    )

    fun say(voice: SayVoice): List<String> = listOf("say", "-v", voice.name, "-f", "-")

    fun windowsProbe(languageTag: String): List<String> = powershell(windowsScript(languageTag, speak = false))

    fun windowsSpeak(languageTag: String): List<String> = powershell(windowsScript(languageTag, speak = true))

    fun windowsScript(languageTag: String, speak: Boolean): String {
        require(languageTagFormat.matches(languageTag)) { "Invalid language tag: '$languageTag'" }
        val action = if (speak) {
            "\$s.SelectVoice(\$v.VoiceInfo.Name)\n" +
                "\$stdin = [Console]::OpenStandardInput()\n" +
                "\$r = New-Object System.IO.StreamReader(\$stdin, [System.Text.Encoding]::UTF8)\n" +
                "\$s.Speak(\$r.ReadToEnd())"
        } else {
            "exit 0"
        }
        return """
            |${'$'}ErrorActionPreference = 'Stop'
            |Add-Type -AssemblyName System.Speech
            |${'$'}s = New-Object System.Speech.Synthesis.SpeechSynthesizer
            |${'$'}v = ${'$'}s.GetInstalledVoices() |
            |    Where-Object { ${'$'}_.Enabled -and ${'$'}_.VoiceInfo.Culture.TwoLetterISOLanguageName -eq '$languageTag' } |
            |    Select-Object -First 1
            |if (-not ${'$'}v) { exit $WINDOWS_NO_VOICE_EXIT }
            |$action
        """.trimMargin()
    }

    const val WINDOWS_NO_VOICE_EXIT = 3

    private fun powershell(script: String): List<String> = listOf(
        "powershell",
        "-NoProfile",
        "-NonInteractive",
        "-EncodedCommand",
        Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_16LE)),
    )
}
