package ru.alarmtrade.specmp.core.speech

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopSpeechCommandsTest {

    private val sayOutput = """
        Albert              en_US    # Hello! My name is Albert.
        Aman                en_IN    # Hello! My name is Aman.
        Bad News            en_US    # Hello! My name is Bad News.
        Eddy (Английский (США)) en_US    # Hello! My name is Eddy.
        Milena              ru_RU    # Здравствуйте! Меня зовут Милена.
        Samantha            en_US    # Hello! My name is Samantha.
        Zarvox              en_US    # Hello! My name is Zarvox.
        Amélie              fr_CA    # Bonjour! Je m’appelle Amélie.
    """.trimIndent()

    @Test
    fun `os is detected by name`() {
        assertEquals(DesktopOs.MAC, DesktopOs.of("Mac OS X"))
        assertEquals(DesktopOs.WINDOWS, DesktopOs.of("Windows 11"))
        assertEquals(DesktopOs.OTHER, DesktopOs.of("Linux"))
        assertEquals(DesktopOs.OTHER, DesktopOs.of(""))
    }

    @Test
    fun `say voices are parsed including names with spaces and brackets`() {
        val voices = DesktopSpeechCommands.parseSayVoices(sayOutput)

        assertEquals(8, voices.size)
        assertEquals(SayVoice("Bad News", "en_US"), voices[2])
        assertEquals(SayVoice("Eddy (Английский (США))", "en_US"), voices[3])
        assertEquals(SayVoice("Milena", "ru_RU"), voices[4])
    }

    @Test
    fun `voice is picked by language and skips novelty voices`() {
        val voices = DesktopSpeechCommands.parseSayVoices(sayOutput)

        assertEquals("Milena", DesktopSpeechCommands.sayVoiceFor(voices, "ru")?.name)
        assertEquals("Samantha", DesktopSpeechCommands.sayVoiceFor(voices, "en")?.name)
        assertEquals("Amélie", DesktopSpeechCommands.sayVoiceFor(voices, "fr")?.name)
        assertNull(DesktopSpeechCommands.sayVoiceFor(voices, "de"))
    }

    @Test
    fun `without preferred voice the default region wins over others`() {
        val voices = listOf(SayVoice("Albert", "en_US"), SayVoice("Aman", "en_IN"), SayVoice("Eddy", "en_US"))

        assertEquals("Eddy", DesktopSpeechCommands.sayVoiceFor(voices, "en")?.name)
    }

    @Test
    fun `only novelty voices are still a voice`() {
        val voices = listOf(SayVoice("Zarvox", "en_US"))

        assertEquals("Zarvox", DesktopSpeechCommands.sayVoiceFor(voices, "en")?.name)
    }

    @Test
    fun `say command reads text from stdin`() {
        assertEquals(
            listOf("say", "-v", "Eddy (Английский (США))", "-f", "-"),
            DesktopSpeechCommands.say(SayVoice("Eddy (Английский (США))", "en_US")),
        )
    }

    @Test
    fun `windows commands encode script and pick voice by culture`() {
        val command = DesktopSpeechCommands.windowsSpeak("ru")

        assertEquals(listOf("powershell", "-NoProfile", "-NonInteractive", "-EncodedCommand"), command.dropLast(1))
        val script = String(Base64.getDecoder().decode(command.last()), Charsets.UTF_16LE)
        assertEquals(DesktopSpeechCommands.windowsScript("ru", speak = true), script)
        assertTrue("TwoLetterISOLanguageName -eq 'ru'" in script)
        assertTrue("exit ${DesktopSpeechCommands.WINDOWS_NO_VOICE_EXIT}" in script)
        assertTrue("OpenStandardInput()" in script)
    }

    @Test
    fun `windows probe only checks the voice`() {
        val script = DesktopSpeechCommands.windowsScript("en", speak = false)

        assertTrue(script.trimEnd().endsWith("exit 0"))
        assertFalse("Speak(" in script)
    }

    @Test
    fun `language tag is validated before it gets into the script`() {
        assertFailsWith<IllegalArgumentException> { DesktopSpeechCommands.windowsScript("ru'; rm -rf /", speak = true) }
        assertFailsWith<IllegalArgumentException> { DesktopSpeechCommands.windowsProbe("RU") }
    }
}
