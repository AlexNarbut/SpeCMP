package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import kotlin.time.Duration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_format_chip
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.VoicePhrase
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.voiceText
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.label
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.zoneValueRes

internal fun VehicleState.zoneValues(): Map<Zone, ZoneValue> = Zone.entries.associateWith(::zone)

/**
 * Изменения зон с прошлой фразы — как на экране, «Капот: Открыт», зоны по порядку экрана; `previous == null`
 * (включили озвучку, вернулись на экран) — молчим: читать все зоны подряд незачем.
 */
internal fun zonesPhrase(previous: Map<Zone, ZoneValue>?, current: Map<Zone, ZoneValue>): VoicePhrase? {
    if (previous == null) return null
    val lines = Zone.entries.filter { previous[it] != current[it] }.map { zone ->
        val value = current[zone] ?: ZoneValue.OFF
        voiceText(
            Strings.string.devicestate_zones_format_chip,
            voiceText(zone.label),
            voiceText(zoneValueRes(zone.kind, value)),
        )
    }
    return lines.takeIf { it.isNotEmpty() }?.let(::VoicePhrase)
}

/** Первое значение сразу, дальше — последнее за [window] затишья: быстрые изменения склеиваются в одну фразу. */
@OptIn(FlowPreview::class)
internal fun <T> Flow<T>.settled(window: Duration): Flow<T> =
    withIndex().debounce { if (it.index == 0) Duration.ZERO else window }.map { it.value }
