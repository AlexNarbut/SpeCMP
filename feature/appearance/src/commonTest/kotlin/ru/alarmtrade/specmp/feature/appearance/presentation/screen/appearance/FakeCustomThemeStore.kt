package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.ui.theme.CustomThemeStore
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeSpec

internal class FakeCustomThemeStore(initial: ThemeSpec = ThemePreset.Default) : CustomThemeStore {
    val saved = mutableListOf<ThemeSpec>()
    private val state = MutableStateFlow(initial)

    override val theme: StateFlow<ThemeSpec> = state

    override fun save(spec: ThemeSpec) {
        saved += spec
        state.value = spec
    }
}
