package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.files.TextFilePickResult
import ru.alarmtrade.specmp.core.files.TextFileSaveResult
import ru.alarmtrade.specmp.core.files.TextFileType
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonShape
import ru.alarmtrade.specmp.core.ui.theme.custom.ContrastCheckKind
import ru.alarmtrade.specmp.core.ui.theme.custom.TextScale
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeContrast
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeFileCodec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeSpec

class AppearanceViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val store = FakeCustomThemeStore()
    private val picker = FakeTextFilePicker()
    private val saver = FakeTextFileSaver()
    private val effects = mutableListOf<AppearanceEffect>()

    private val defaultLight = ThemePreset.Default.light
    private val defaultDark = ThemePreset.Default.dark

    private fun TestScope.viewModel(isDark: Boolean = false) =
        AppearanceViewModel(store, picker, saver, testDispatchers(dispatcher), isDark).also { vm ->
            backgroundScope.launch { vm.state.collect {} }
            backgroundScope.launch { vm.sideEffect.collect { effects += it } }
        }

    private fun AppearanceViewModel.current() = state.value

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts from the applied theme and the app mode, linked when shapes match`() = runTest(dispatcher) {
        val vm = viewModel(isDark = true)

        assertEquals(ThemePreset.Default, vm.current().draft)
        assertTrue(vm.current().isDark)
        assertTrue(vm.current().linked)
        assertFalse(vm.current().isDirty)
        assertEquals(ThemePreset.Pandora, vm.current().preset)
    }

    @Test
    fun `starts unlinked when the stored theme has different shapes`() = runTest(dispatcher) {
        val spec = ThemeSpec(defaultLight, defaultDark.withRadius(4))
        val vm = AppearanceViewModel(FakeCustomThemeStore(spec), picker, saver, testDispatchers(dispatcher), false)

        assertFalse(vm.state.value.linked)
    }

    @Test
    fun `preset replaces the draft and marks it dirty`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onPresetSelect(ThemePreset.Workshop)

        assertEquals(ThemePreset.Workshop.spec, vm.current().draft)
        assertEquals(ThemePreset.Workshop, vm.current().preset)
        assertTrue(vm.current().isDirty)
        assertTrue(store.saved.isEmpty())
    }

    @Test
    fun `failing color blocks done and the suggested fix unblocks it`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onColorEdit(ThemeColorRole.Ink)
        vm.onEditorInput("f0f0f0")
        val editor = assertNotNull(vm.current().editor)
        assertEquals("F0F0F0", editor.input)
        assertTrue(editor.inputValid)
        assertTrue(editor.failing)
        assertFalse(editor.canSave)
        assertTrue(editor.checks.any { it.kind == ContrastCheckKind.InkOnBackground && !it.passes })

        vm.onEditorDone()
        assertNotNull(vm.current().editor)
        assertEquals(defaultLight.palette.ink, vm.current().draft.light.palette.ink)

        val fix = assertNotNull(editor.fix)
        vm.onEditorFix()
        assertEquals(fix, vm.current().editor?.candidate)
        assertTrue(vm.current().editor?.canSave == true)

        vm.onEditorDone()
        assertNull(vm.current().editor)
        assertEquals(fix, vm.current().draft.light.palette.ink)
        assertEquals(defaultDark, vm.current().draft.dark)
        assertTrue(ThemeContrast.passes(vm.current().draft.light, defaultLight))
    }

    @Test
    fun `invalid hex keeps the last valid candidate and blocks done`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onColorEdit(ThemeColorRole.Primary)
        vm.onEditorColorPick(requireNotNull(ThemeColor.parse("#1F7A55")))
        vm.onEditorInput("#12")

        val editor = assertNotNull(vm.current().editor)
        assertFalse(editor.inputValid)
        assertFalse(editor.canSave)
        assertEquals(ThemeColor.parse("#1F7A55"), editor.candidate)
    }

    @Test
    fun `cancel discards the edited color`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onColorEdit(ThemeColorRole.Primary)
        vm.onEditorColorPick(requireNotNull(ThemeColor.parse("#1F7A55")))
        vm.onEditorCancel()

        assertNull(vm.current().editor)
        assertEquals(ThemePreset.Default, vm.current().draft)
    }

    @Test
    fun `colors are edited in the selected mode only`() = runTest(dispatcher) {
        val vm = viewModel()
        val black = requireNotNull(ThemeColor.parse("#000000"))

        vm.onModeSelect(isDark = true)
        vm.onColorEdit(ThemeColorRole.Background)
        vm.onEditorColorPick(black)
        vm.onEditorDone()

        assertNull(vm.current().editor)
        assertEquals(black, vm.current().draft.dark.palette.background)
        assertEquals(defaultLight, vm.current().draft.light)
    }

    @Test
    fun `shape edits go to both modes when linked and to the selected one when not`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onTextScaleSelect(TextScale.Largest)
        vm.onRadiusChange(20)
        assertEquals(TextScale.Largest, vm.current().draft.light.textScale)
        assertEquals(TextScale.Largest, vm.current().draft.dark.textScale)
        assertEquals(20, vm.current().draft.dark.radius)

        vm.onLinkedToggle()
        assertFalse(vm.current().linked)
        vm.onButtonShapeSelect(ButtonShape.Pill)

        assertEquals(ButtonShape.Pill, vm.current().draft.light.buttonShape)
        assertEquals(ButtonShape.Rounded, vm.current().draft.dark.buttonShape)
    }

    @Test
    fun `turning linked on copies shape of the selected mode to the other`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onLinkedToggle()
        vm.onModeSelect(isDark = true)
        vm.onButtonShapeSelect(ButtonShape.Square)
        vm.onRadiusChange(4)
        vm.onButtonFillSelect(ButtonFill.Tonal)

        vm.onLinkedToggle()

        val light = vm.current().draft.light
        assertTrue(vm.current().linked)
        assertEquals(ButtonShape.Square, light.buttonShape)
        assertEquals(4, light.radius)
        assertEquals(ButtonFill.Tonal, light.buttonFill)
        assertTrue(light.sameShapeAs(vm.current().draft.dark))
    }

    @Test
    fun `linking does not copy a fill that fails contrast in the other mode`() = runTest(dispatcher) {
        val vm = viewModel()
        val night = ThemePreset.NightShift.spec
        // Светлая: акцент проходит 3:1 для заливки, но не 4.5:1 для контурной кнопки.
        val light = night.light.copy(
            buttonFill = ButtonFill.Filled,
            palette = night.light.palette
                .with(ThemeColorRole.Primary, requireNotNull(ThemeColor.parse("#808080")))
                .with(ThemeColorRole.OnPrimary, ThemeColor.Black),
        )
        val state = vm.current().copy(draft = ThemeSpec(light, night.dark), linked = false, isDark = true)
        assertTrue(ThemeContrast.passes(light, defaultLight))
        assertFalse(ThemeContrast.canUseFill(light, defaultLight, ButtonFill.Outlined))

        val linked = state.toggleLinked()

        assertTrue(linked.linked)
        assertEquals(ButtonFill.Filled, linked.draft.light.buttonFill)
        assertEquals(night.dark.radius, linked.draft.light.radius)
        assertEquals(night.dark.textScale, linked.draft.light.textScale)
    }

    @Test
    fun `outlined fill is unavailable on the default theme and selecting it does nothing`() = runTest(dispatcher) {
        val vm = viewModel()

        assertFalse(vm.current().fillAvailable(ButtonFill.Outlined))
        assertTrue(vm.current().fillAvailable(ButtonFill.Tonal))
        vm.onButtonFillSelect(ButtonFill.Outlined)

        assertEquals(ButtonFill.Filled, vm.current().draft.light.buttonFill)
        assertFalse(vm.current().isDirty)
    }

    @Test
    fun `gradient fixes the second color to pass contrast`() = runTest(dispatcher) {
        val vm = viewModel()
        val darkEnd = requireNotNull(ThemeColor.parse("#0B1220"))
        vm.onPresetSelect(ThemePreset.Graphite)
        val graphite = ThemePreset.Graphite.spec.light
        val spoiled = vm.current().copy(
            draft = vm.current().draft.copy(
                light = graphite.copy(palette = graphite.palette.with(ThemeColorRole.Background2, darkEnd)),
            ),
        )

        val result = spoiled.withBackground(ThemeBackground.Gradient)

        val light = result.draft.light
        assertEquals(ThemeBackground.Gradient, light.background)
        assertNotEquals(darkEnd, light.palette.background2)
        assertTrue(ThemeContrast.passes(light, defaultLight))
    }

    @Test
    fun `gradient falls back to solid when no second color passes`() = runTest(dispatcher) {
        val vm = viewModel()
        // Белому тексту нужен тёмный конец градиента, тёмно-серому акценту — светлый: подходящего нет.
        val mode = defaultLight.copy(
            palette = defaultLight.palette.copy(
                ink = ThemeColor.White,
                primary = requireNotNull(ThemeColor.parse("#333333")),
            ),
        )
        val state = vm.current().copy(draft = vm.current().draft.copy(light = mode))

        val result = state.withBackground(ThemeBackground.Gradient)

        assertEquals(ThemeBackground.Solid, result.draft.light.background)
    }

    @Test
    fun `background type is set for the selected mode`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onBackgroundSelect(ThemeBackground.Gradient)

        assertEquals(ThemeBackground.Gradient, vm.current().draft.light.background)
        assertEquals(ThemeBackground.Glow, vm.current().draft.dark.background)
        assertTrue(ThemeContrast.passes(vm.current().draft.light, defaultLight))
    }

    @Test
    fun `apply saves the draft to the store and revert restores the applied theme`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onPresetSelect(ThemePreset.Contrast)
        vm.onApply()

        assertEquals(listOf(ThemePreset.Contrast.spec), store.saved)
        assertEquals(ThemePreset.Contrast.spec, vm.current().applied)
        assertFalse(vm.current().isDirty)
        assertEquals(listOf<AppearanceEffect>(AppearanceEffect.Applied), effects)

        vm.onPresetSelect(ThemePreset.Graphite)
        vm.onRevert()
        assertEquals(ThemePreset.Contrast.spec, vm.current().draft)
        assertFalse(vm.current().isDirty)
    }

    @Test
    fun `apply without changes saves nothing`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onApply()

        assertTrue(store.saved.isEmpty())
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `export writes the draft as a theme file`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onPresetSelect(ThemePreset.Workshop)

        vm.onExport()

        val (name, type, text) = saver.saves.single()
        assertEquals("pandora-theme", name)
        assertEquals(TextFileType.Json, type)
        assertEquals(ThemeFileCodec.encode(ThemePreset.Workshop.spec), text)
        assertEquals(listOf<AppearanceEffect>(AppearanceEffect.Exported("pandora-theme.json")), effects)
    }

    @Test
    fun `export cancel shows nothing and failure is reported`() = runTest(dispatcher) {
        val vm = viewModel()

        saver.result = TextFileSaveResult.Cancelled
        vm.onExport()
        assertTrue(effects.isEmpty())

        saver.result = TextFileSaveResult.Failed
        vm.onExport()
        assertEquals(listOf<AppearanceEffect>(AppearanceEffect.ExportFailed), effects)
    }

    @Test
    fun `import loads a theme file into the draft`() = runTest(dispatcher) {
        val vm = viewModel()
        picker.result = TextFilePickResult.Picked("theme.json", ThemeFileCodec.encode(ThemePreset.Graphite.spec))

        vm.onImport()

        assertEquals(ThemePreset.Graphite.spec, vm.current().draft)
        assertTrue(vm.current().isDirty)
        assertTrue(store.saved.isEmpty())
        assertEquals(listOf<AppearanceEffect>(AppearanceEffect.Imported(0, colorsReset = false)), effects)
    }

    @Test
    fun `import reports fixed colors`() = runTest(dispatcher) {
        val vm = viewModel()
        val text = """{"format":"pandora-spec-theme","version":1,"light":{"c":{"ink":"#F0F0F0"}}}"""
        picker.result = TextFilePickResult.Picked("theme.json", text)

        vm.onImport()

        val imported = effects.single() as AppearanceEffect.Imported
        assertEquals(1, imported.fixedColors)
        assertFalse(imported.colorsReset)
        assertTrue(ThemeContrast.passes(vm.current().draft.light, defaultLight))
    }

    @Test
    fun `import of a non-theme file keeps the draft`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onPresetSelect(ThemePreset.Contrast)
        picker.result = TextFilePickResult.Picked("notes.json", """{"hello":"world"}""")

        vm.onImport()

        assertEquals(ThemePreset.Contrast.spec, vm.current().draft)
        assertEquals(listOf<AppearanceEffect>(AppearanceEffect.NotThemeFile), effects)
    }

    @Test
    fun `import read failure is reported and cancel shows nothing`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onImport()
        assertTrue(effects.isEmpty())

        picker.result = TextFilePickResult.TooLarge
        vm.onImport()
        picker.result = TextFilePickResult.Failed
        vm.onImport()
        assertEquals(listOf<AppearanceEffect>(AppearanceEffect.ImportFailed, AppearanceEffect.ImportFailed), effects)
    }

    @Test
    fun `importing different shapes unlinks the modes`() = runTest(dispatcher) {
        val vm = viewModel()
        val spec = ThemeSpec(defaultLight, defaultDark.withRadius(4))
        picker.result = TextFilePickResult.Picked("theme.json", ThemeFileCodec.encode(spec))

        vm.onImport()

        assertFalse(vm.current().linked)
        assertEquals(4, vm.current().draft.dark.radius)
    }

    @Test
    fun `element radius changes only that element`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onElementRadiusChange(RadiusElement.Chips, 6)
        vm.onElementRadiusChange(RadiusElement.Cards, 40)

        val radii = vm.current().draft.light.radii
        assertEquals(6, radii.chips)
        assertEquals(28, radii.cards)
        assertEquals(defaultLight.radii.buttons, radii.buttons)
        assertEquals(6, vm.current().draft.dark.radii.chips)
    }
}
