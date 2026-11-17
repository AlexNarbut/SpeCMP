package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeContrast
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeRadii
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeSpec

/** Контраст считается относительно темы по умолчанию того же режима (`ThemeContrast`). */
internal fun contrastBase(isDark: Boolean): ThemeModeSpec = ThemePreset.Default.mode(isDark)

/** Темы, в которые идут правки размеров, формы и кнопок. */
internal fun AppearanceState.shapeTargets(): List<Boolean> = if (linked) listOf(false, true) else listOf(isDark)

internal fun AppearanceState.editShape(edit: (ThemeModeSpec) -> ThemeModeSpec): AppearanceState =
    copy(draft = shapeTargets().fold(draft) { spec, dark -> spec.withMode(dark, edit(spec.mode(dark))) })

internal fun AppearanceState.editMode(edit: (ThemeModeSpec) -> ThemeModeSpec): AppearanceState =
    copy(draft = draft.withMode(isDark, edit(mode)))

internal fun AppearanceState.fillAvailable(fill: ButtonFill): Boolean =
    shapeTargets().all { dark -> ThemeContrast.canUseFill(draft.mode(dark), contrastBase(dark), fill) }

internal fun AppearanceState.withFill(fill: ButtonFill): AppearanceState =
    if (fillAvailable(fill)) editShape { it.copy(buttonFill = fill) } else this

/** Градиент проверяет второй цвет: не проходит — подбирается ближайший, не подобрать — заливка (как в макете). */
internal fun AppearanceState.withBackground(type: ThemeBackground): AppearanceState = editMode { mode ->
    val next = mode.copy(background = type)
    val base = contrastBase(isDark)
    val endFails = ThemeContrast.failing(next, base).any { it.kind.involves(ThemeColorRole.Background2) }
    if (!endFails) return@editMode next
    val end = ThemeContrast.nearest(next, base, ThemeColorRole.Background2, next.palette.background2)
    if (end == null) {
        next.copy(background = ThemeBackground.Solid)
    } else {
        next.copy(palette = next.palette.with(ThemeColorRole.Background2, end))
    }
}

/** Включение копирует размеры и форму выбранной темы в другую; заливку — только если она там проходит контраст. */
internal fun AppearanceState.toggleLinked(): AppearanceState {
    if (linked) return copy(linked = false)
    val otherDark = !isDark
    val source = mode
    val shaped = draft.mode(otherDark).withShapeOf(source)
    val other = if (ThemeContrast.canUseFill(shaped, contrastBase(otherDark), source.buttonFill)) {
        shaped.copy(buttonFill = source.buttonFill)
    } else {
        shaped
    }
    return copy(linked = true, draft = draft.withMode(otherDark, other))
}

/** Новый черновик целиком (импорт, отмена): связь остаётся, только если формы тем совпадают. */
internal fun AppearanceState.withDraft(spec: ThemeSpec): AppearanceState =
    copy(draft = spec, linked = linked && spec.light.sameShapeAs(spec.dark), editor = null)

internal fun AppearanceState.withRadius(base: Int): AppearanceState = editShape { it.withRadius(base) }

internal fun AppearanceState.withElementRadius(element: RadiusElement, value: Int): AppearanceState = editShape {
    val v = value.coerceIn(0, ThemeRadii.MAX)
    val radii = when (element) {
        RadiusElement.Buttons -> it.radii.copy(buttons = v)
        RadiusElement.Inputs -> it.radii.copy(inputs = v)
        RadiusElement.Cards -> it.radii.copy(cards = v)
        RadiusElement.Chips -> it.radii.copy(chips = v)
        RadiusElement.Sheets -> it.radii.copy(sheets = v)
    }
    it.copy(radii = radii)
}

internal fun AppearanceState.openEditor(role: ThemeColorRole): AppearanceState {
    val color = mode.palette[role]
    return copy(editor = editorFor(role, color.hex, color, paletteOpen = false))
}

internal fun AppearanceState.editorInput(text: String): AppearanceState {
    val editor = editor ?: return this
    // Текст поля не переписывается (без «#» спереди): иначе курсор Compose встаёт не туда и ввод путается.
    val input = text.uppercase().take(MAX_INPUT)
    val candidate = ThemeColor.parse(normalizeHex(input)) ?: editor.candidate
    return copy(editor = editorFor(editor.role, input, candidate, editor.paletteOpen))
}

internal fun AppearanceState.editorColor(color: ThemeColor): AppearanceState {
    val editor = editor ?: return this
    return copy(editor = editorFor(editor.role, color.hex, color, editor.paletteOpen))
}

internal fun AppearanceState.saveEditor(): AppearanceState {
    val editor = editor ?: return this
    if (!editor.canSave) return this
    return editMode { it.copy(palette = it.palette.with(editor.role, editor.candidate)) }.copy(editor = null)
}

private fun AppearanceState.editorFor(
    role: ThemeColorRole,
    input: String,
    candidate: ThemeColor,
    paletteOpen: Boolean,
): ColorEditorState {
    val base = contrastBase(isDark)
    val palette = mode.palette.with(role, candidate)
    val checks = ThemeContrast.checksFor(mode, base, role, candidate)
        .map { ContrastResult(it.kind, it.ratio(palette), it.min) }
    val fix = if (checks.all { it.passes }) null else ThemeContrast.nearest(mode, base, role, candidate)
    return ColorEditorState(role, input, candidate, checks, fix, paletteOpen)
}

/** `2a62f5` → `#2A62F5`; лишнее отрезается до `#RRGGBB`. */
internal fun normalizeHex(text: String): String {
    val digits = text.trim().removePrefix("#").filterNot { it.isWhitespace() }.uppercase().take(HEX_DIGITS)
    return "#$digits"
}

private const val HEX_DIGITS = 6
private const val MAX_INPUT = HEX_DIGITS + 1
