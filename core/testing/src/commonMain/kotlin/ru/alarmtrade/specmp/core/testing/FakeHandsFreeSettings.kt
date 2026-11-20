package ru.alarmtrade.specmp.core.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.speech.HandsFreeSettings

class FakeHandsFreeSettings(enabled: Boolean = false) : HandsFreeSettings {
    override val enabled = MutableStateFlow(enabled)

    override fun setEnabled(enabled: Boolean) {
        this.enabled.update { enabled }
    }
}
