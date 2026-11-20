package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_brake
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_door_front_left
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_label_hood
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_closed_f
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_open_m
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zone_value_pressed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_format_chip
import ru.alarmtrade.specmp.core.speech.SpeechMode
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue

class CheckStateVoiceTest {

    private val txt = Strings.string
    private val rest = Zone.entries.associateWith { ZoneValue.OFF }

    private fun line(label: StringResource, value: StringResource) =
        voiceText(txt.devicestate_zones_format_chip, voiceText(label), voiceText(value))

    @Test
    fun `first snapshot is silent`() {
        assertNull(zonesPhrase(null, rest + (Zone.HOOD to ZoneValue.ON)))
    }

    @Test
    fun `unchanged zones are silent`() {
        assertNull(zonesPhrase(rest, rest))
    }

    @Test
    fun `changed zone is spoken as on screen`() {
        assertEquals(
            VoicePhrase(listOf(line(txt.devicestate_zone_label_hood, txt.devicestate_zone_value_open_m))),
            zonesPhrase(rest, rest + (Zone.HOOD to ZoneValue.ON)),
        )
    }

    @Test
    fun `several changes are one phrase in screen order`() {
        val previous = rest + (Zone.DOOR_FRONT_LEFT to ZoneValue.ON)
        val current = rest + (Zone.BRAKE to ZoneValue.ON) + (Zone.HOOD to ZoneValue.ON)
        val phrase = zonesPhrase(previous, current)
        assertEquals(
            listOf(
                line(txt.devicestate_zone_label_door_front_left, txt.devicestate_zone_value_closed_f),
                line(txt.devicestate_zone_label_hood, txt.devicestate_zone_value_open_m),
                line(txt.devicestate_zone_label_brake, txt.devicestate_zone_value_pressed),
            ),
            phrase?.lines,
        )
        assertEquals(SpeechMode.FLUSH, phrase?.mode)
    }

    @Test
    fun `settled passes the first value at once and the last one after a pause`() = runTest {
        val source = MutableSharedFlow<Int>()
        val out = mutableListOf<Int>()
        backgroundScope.launch { source.settled(2.seconds).collect { out += it } }
        runCurrent()

        source.emit(1)
        runCurrent()
        assertEquals(listOf(1), out)

        source.emit(2)
        advanceTimeBy(1_000)
        source.emit(3)
        advanceTimeBy(1_900)
        runCurrent()
        assertEquals(listOf(1), out)

        advanceTimeBy(200)
        runCurrent()
        assertEquals(listOf(1, 3), out)
    }
}
