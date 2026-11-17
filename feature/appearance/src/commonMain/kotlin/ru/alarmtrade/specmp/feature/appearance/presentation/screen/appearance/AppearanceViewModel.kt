package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.files.TextFilePickResult
import ru.alarmtrade.specmp.core.files.TextFilePicker
import ru.alarmtrade.specmp.core.files.TextFileSaveResult
import ru.alarmtrade.specmp.core.files.TextFileSaver
import ru.alarmtrade.specmp.core.files.TextFileType
import ru.alarmtrade.specmp.core.ui.theme.CustomThemeStore
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonShape
import ru.alarmtrade.specmp.core.ui.theme.custom.ControlPadding
import ru.alarmtrade.specmp.core.ui.theme.custom.GradientDirection
import ru.alarmtrade.specmp.core.ui.theme.custom.TextScale
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeFileCodec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeImportResult
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel

/** [isDark] — тема приложения при входе: с неё начинается настройка. */
internal class AppearanceViewModel(
    private val store: CustomThemeStore,
    private val filePicker: TextFilePicker,
    private val fileSaver: TextFileSaver,
    private val dispatchers: AppDispatchers,
    isDark: Boolean,
) : BaseViewModel<AppearanceState, AppearanceEffect>(AppearanceState(applied = store.theme.value, isDark = isDark)) {

    fun onModeSelect(isDark: Boolean) {
        intent { reduce { copy(isDark = isDark, editor = null) } }
    }

    fun onPresetSelect(preset: ThemePreset) {
        intent { reduce { copy(draft = preset.spec) } }
    }

    fun onBackgroundSelect(type: ThemeBackground) {
        intent { reduce { withBackground(type) } }
    }

    fun onGradientDirectionSelect(direction: GradientDirection) {
        intent { reduce { editMode { it.copy(gradientDirection = direction) } } }
    }

    fun onColorEdit(role: ThemeColorRole) {
        intent { reduce { openEditor(role) } }
    }

    fun onEditorInput(text: String) {
        intent { reduce { editorInput(text) } }
    }

    fun onEditorColorPick(color: ThemeColor) {
        intent { reduce { editorColor(color) } }
    }

    fun onEditorFix() {
        intent { reduce { editor?.fix?.let(::editorColor) ?: this } }
    }

    fun onEditorPaletteToggle() {
        intent { reduce { copy(editor = editor?.let { it.copy(paletteOpen = !it.paletteOpen) }) } }
    }

    fun onEditorDone() {
        intent { reduce { saveEditor() } }
    }

    fun onEditorCancel() {
        intent { reduce { copy(editor = null) } }
    }

    fun onLinkedToggle() {
        intent { reduce { toggleLinked() } }
    }

    fun onTextScaleSelect(scale: TextScale) {
        intent { reduce { editShape { it.copy(textScale = scale) } } }
    }

    fun onRadiusChange(radius: Int) {
        intent { reduce { withRadius(radius) } }
    }

    fun onElementRadiusChange(element: RadiusElement, radius: Int) {
        intent { reduce { withElementRadius(element, radius) } }
    }

    fun onButtonFillSelect(fill: ButtonFill) {
        intent { reduce { withFill(fill) } }
    }

    fun onButtonShapeSelect(shape: ButtonShape) {
        intent { reduce { editShape { it.copy(buttonShape = shape) } } }
    }

    fun onButtonPaddingSelect(padding: ControlPadding) {
        intent { reduce { editShape { it.copy(buttonPadding = padding) } } }
    }

    fun onChipPaddingSelect(padding: ControlPadding) {
        intent { reduce { editShape { it.copy(chipPadding = padding) } } }
    }

    fun onAdvancedToggle() {
        intent { reduce { copy(advancedExpanded = !advancedExpanded) } }
    }

    fun onApply() {
        intent {
            executeWithProceedLock {
                val draft = currentState.draft
                if (draft == currentState.applied) return@executeWithProceedLock
                withContext(dispatchers.io) { store.save(draft) }
                reduce { copy(applied = draft) }
                postSideEffect(AppearanceEffect.Applied)
            }
        }
    }

    fun onRevert() {
        intent { reduce { withDraft(applied) } }
    }

    fun onExport() {
        intent {
            executeWithProceedLock {
                val text = withContext(dispatchers.default) { ThemeFileCodec.encode(currentState.draft) }
                when (val result = fileSaver.save(EXPORT_NAME, TextFileType.Json, text)) {
                    is TextFileSaveResult.Saved -> postSideEffect(AppearanceEffect.Exported(result.name))
                    TextFileSaveResult.Failed -> postSideEffect(AppearanceEffect.ExportFailed)
                    TextFileSaveResult.Cancelled -> Unit
                }
            }
        }
    }

    fun onImport() {
        intent {
            executeWithProceedLock {
                when (val picked = filePicker.pick(TextFileType.Json)) {
                    is TextFilePickResult.Picked -> importTheme(picked.text)

                    TextFilePickResult.Failed, TextFilePickResult.TooLarge -> postSideEffect(
                        AppearanceEffect.ImportFailed,
                    )

                    TextFilePickResult.Cancelled -> Unit
                }
            }
        }
    }

    private suspend fun importTheme(text: String) = subIntent {
        val base = currentState.draft
        when (val result = withContext(dispatchers.default) { ThemeFileCodec.decode(text, base) }) {
            ThemeImportResult.NotThemeFile -> postSideEffect(AppearanceEffect.NotThemeFile)

            is ThemeImportResult.Imported -> {
                reduce { withDraft(result.spec) }
                postSideEffect(AppearanceEffect.Imported(result.fixedColors, result.colorsReset))
            }
        }
    }

    private companion object {
        const val EXPORT_NAME = "pandora-theme"
    }
}
