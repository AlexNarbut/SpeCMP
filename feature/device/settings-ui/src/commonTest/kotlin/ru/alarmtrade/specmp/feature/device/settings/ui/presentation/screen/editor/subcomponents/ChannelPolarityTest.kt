package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue

class ChannelPolarityTest {

    private fun output(value: Polarity, default: Polarity, editable: Boolean) = OutputSetting(
        id = 1,
        number = "5.1",
        name = "CH1",
        description = null,
        value = SettingValue.Output(OutputValue(emptyList(), value)),
        channel = Channel(1, "CH1", emptyList()),
        canEditPolarity = editable,
        defaultPolarity = default,
        availableLogics = emptyList(),
        enabledLogics = emptyList()
    )

    @Test
    fun `fixed polarity is shown from the device default`() {
        assertEquals(Polarity.MINUS, output(Polarity.UNDEFINED, Polarity.MINUS, editable = false).shownPolarity)
        assertEquals(Polarity.MINUS, output(Polarity.PLUS, Polarity.MINUS, editable = false).shownPolarity)
    }

    @Test
    fun `editable polarity is the current value, default when undefined`() {
        assertEquals(Polarity.PLUS, output(Polarity.PLUS, Polarity.MINUS, editable = true).shownPolarity)
        assertEquals(Polarity.MINUS, output(Polarity.UNDEFINED, Polarity.MINUS, editable = true).shownPolarity)
    }
}
