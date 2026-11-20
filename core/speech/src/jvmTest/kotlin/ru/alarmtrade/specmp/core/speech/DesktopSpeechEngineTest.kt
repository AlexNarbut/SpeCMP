package ru.alarmtrade.specmp.core.speech

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope

class DesktopSpeechEngineTest {

    @Test
    fun `other os is unsupported and speaks nothing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val engine = DesktopSpeechEngine(
            os = DesktopOs.OTHER,
            languageTag = "ru",
            io = dispatcher,
            scope = serviceScope(dispatcher, parent = backgroundScope.coroutineContext),
        )

        engine.prepare()
        engine.speak("Включите зажигание")

        assertEquals(SpeechAvailability.Unsupported, engine.availability.value)
    }
}
