package ru.alarmtrade.specmp.feature.appearance.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceActions
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceScreen
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceState
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.ColorEditorActions
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.editorInput
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.openEditor
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.withBackground

/** Состояния экрана «Оформление» по макету `design/features/appearance/`; PNG — в `build/state-gallery/appearance`. */
class AppearanceGallery {

    private val outDir = File("build/state-gallery/appearance")
    private val actions = AppearanceActions(
        {}, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, {}, {}, {}, {}, {}, {}, {},
        ColorEditorActions({}, {}, {}, {}, {}, {}),
    )
    private val initial = AppearanceState(applied = ThemePreset.Default, isDark = false)

    private fun render(name: String, state: AppearanceState) = StateGallery.render(outDir, name) {
        AppearanceScreen(state = state, actions = actions, snackbar = CmpSnackbarHostState())
    }

    @Test
    fun default() = render("01-default", initial)

    @Test
    fun presetDark() = render("02-workshop-dark", initial.copy(draft = ThemePreset.Workshop.spec, isDark = true))

    @Test
    fun gradientAdvanced() = render(
        "03-gradient-advanced",
        initial.copy(draft = ThemePreset.Graphite.spec, advancedExpanded = true)
            .withBackground(ThemeBackground.Gradient),
    )

    @Test
    fun editorFailing() = render(
        "04-editor-failing",
        initial.openEditor(ThemeColorRole.Ink).editorInput("#F0F0F0"),
    )

    @Test
    fun editorPalette() = render(
        "05-editor-palette",
        initial.openEditor(ThemeColorRole.Primary).let {
            it.copy(editor = it.editor?.copy(paletteOpen = true, candidate = ThemeColor(0x1F7A55)))
        },
    )
}
