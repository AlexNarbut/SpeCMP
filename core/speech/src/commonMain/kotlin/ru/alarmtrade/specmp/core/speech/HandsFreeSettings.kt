package ru.alarmtrade.specmp.core.speech

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Режим «Руки заняты»: общий для всех экранов шагов, переживает перезапуск и выход из аккаунта. */
interface HandsFreeSettings {
    val enabled: StateFlow<Boolean>

    fun setEnabled(enabled: Boolean)
}

internal class PersistentHandsFreeSettings(private val settings: Settings) : HandsFreeSettings {

    private val state = MutableStateFlow(settings.getBoolean(ENABLED_KEY, false))
    override val enabled: StateFlow<Boolean> = state.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        settings.putBoolean(ENABLED_KEY, enabled)
        state.update { enabled }
    }

    private companion object {
        const val ENABLED_KEY = "enabled"
    }
}
