package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_btn_done
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_btn_fix
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_btn_palette
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_error_fixable
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_error_related
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_format_min
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_format_ratio
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_label_contrast
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_label_hex
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_subtitle_dark
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_subtitle_light
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_value_sample_background
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_value_sample_button
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_value_sample_caption
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_value_sample_card
import ru.alarmtrade.specmp.core.localization.resources.appearance_editor_value_sample_field
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeProvider
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBorder
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPageBackground
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appearance.presentation.model.ThemeSwatches
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.ColorEditorActions
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.ColorEditorState
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.ContrastResult

private val SampleMinHeight = 96.dp

/** Редактор цвета: шторка снизу на Compact, диалог на широком окне. Пипетки нет — HEX, образцы и палитра. */
@Composable
internal fun ColorEditor(
    editor: ColorEditorState,
    mode: ThemeModeSpec,
    isDark: Boolean,
    compact: Boolean,
    actions: ColorEditorActions,
) {
    val title = editor.role.title()
    val subtitle = stringResource(
        if (isDark) Strings.string.appearance_editor_subtitle_dark else Strings.string.appearance_editor_subtitle_light,
    )
    if (compact) {
        CmpBottomSheet(onDismissRequest = actions.onCancel) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CmpSpacing.screenPaddingH)
                    .padding(bottom = CmpSpacing.space16)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EditorTitle(title, subtitle, Modifier.weight(1f))
                    CmpIconButton(
                        onClick = actions.onCancel,
                        icon = CmpIcons.Close,
                        contentDescription = stringResource(Strings.string.appearance_editor_a11y_close),
                    )
                }
                EditorBody(editor, mode, isDark, actions)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                ) {
                    CancelButton(actions.onCancel, Modifier.weight(1f))
                    DoneButton(editor.canSave, actions.onDone, Modifier.weight(1f))
                }
            }
        }
    } else {
        CmpDialog(
            onDismissRequest = actions.onCancel,
            title = { EditorTitle(title, subtitle) },
            text = { EditorBody(editor, mode, isDark, actions) },
            confirmButton = { DoneButton(editor.canSave, actions.onDone) },
            dismissButton = { CancelButton(actions.onCancel) },
        )
    }
}

@Composable
private fun EditorTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(text = title, style = CmpTextStyle.Headline1, color = CmpTheme.colors.foregroundOnDarkPrimaryDefault)
        Hint(subtitle)
    }
}

@Composable
private fun CancelButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(
        text = stringResource(Strings.string.appearance_editor_btn_cancel),
        onClick = onClick,
        type = CmpButtonType.Secondary,
        size = CmpButtonSize.S,
        modifier = modifier,
    )
}

@Composable
private fun DoneButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(
        text = stringResource(Strings.string.appearance_editor_btn_done),
        onClick = onClick,
        enabled = enabled,
        type = CmpButtonType.Accent,
        size = CmpButtonSize.S,
        modifier = modifier.testTag("appearance_editor_done"),
    )
}

@Composable
private fun EditorBody(editor: ColorEditorState, mode: ThemeModeSpec, isDark: Boolean, actions: ColorEditorActions) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        EditorBodyContent(editor, mode, isDark, actions)
    }
}

@Composable
private fun ColumnScope.EditorBodyContent(
    editor: ColorEditorState,
    mode: ThemeModeSpec,
    isDark: Boolean,
    actions: ColorEditorActions,
) {
    EditorSample(editor, mode, isDark)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpTextField(
            value = editor.input,
            onValueChange = actions.onInput,
            label = stringResource(Strings.string.appearance_editor_label_hex),
            isError = !editor.inputValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            leadingIcon = { ColorSwatch(editor.candidate, size = CmpSize.iconM) },
            modifier = Modifier.weight(1f).testTag("appearance_editor_hex"),
        )
        CmpIconButton(
            onClick = actions.onPaletteToggle,
            icon = CmpIcons.Palette,
            contentDescription = stringResource(Strings.string.appearance_editor_btn_palette),
            selected = editor.paletteOpen,
        )
    }
    SwatchGrid(ThemeSwatches.Quick, editor, actions)
    if (editor.paletteOpen) {
        SwatchGrid(ThemeSwatches.Palette, editor, actions)
    }
    ContrastChecks(editor.checks)
    if (editor.failing) {
        FailureBlock(editor, actions.onFix)
    }
}

@Composable
private fun SwatchGrid(colors: List<ThemeColor>, editor: ColorEditorState, actions: ColorEditorActions) {
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        colors.forEach { color ->
            ColorSwatch(
                color = color,
                selected = color == editor.candidate,
                onClick = { actions.onColorPick(color) },
            )
        }
    }
}

/** Как цвет выглядит в своей роли: в черновике с этим цветом. */
@Composable
private fun EditorSample(editor: ColorEditorState, mode: ThemeModeSpec, isDark: Boolean) {
    val sample = mode.copy(palette = mode.palette.with(editor.role, editor.candidate))
    val frame = RoundedCornerShape(CmpRadius.cardSmall)
    val border = CmpTheme.colors.borderHighlight
    CmpThemeProvider(sample, isDark) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SampleMinHeight)
                .clip(frame)
                .cmpPageBackground()
                .cmpBorder(CmpBorderWidth.listItem, border, frame)
                .padding(CmpSpacing.space16),
            contentAlignment = Alignment.Center,
        ) {
            SampleContent(editor.role, sample)
        }
    }
}

@Composable
private fun SampleContent(role: ThemeColorRole, mode: ThemeModeSpec) {
    val colors = CmpTheme.colors
    when (role) {
        ThemeColorRole.Primary, ThemeColorRole.OnPrimary -> CmpButton(
            text = stringResource(Strings.string.appearance_editor_value_sample_button),
            onClick = {},
            type = CmpButtonType.Accent,
        )

        ThemeColorRole.Background, ThemeColorRole.Background2 -> CmpText(
            text = stringResource(Strings.string.appearance_editor_value_sample_background),
            style = CmpTextStyle.Headline2,
            color = colors.foregroundOnDarkPrimaryDefault,
        )

        ThemeColorRole.Outline -> CmpTextField(
            value = stringResource(Strings.string.appearance_editor_value_sample_field),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        else -> CmpCard(modifier = Modifier.fillMaxWidth()) {
            CmpText(
                text = when (role) {
                    ThemeColorRole.Card -> stringResource(Strings.string.appearance_editor_value_sample_card)
                    ThemeColorRole.Ink2 -> stringResource(Strings.string.appearance_editor_value_sample_caption)
                    else -> role.title()
                },
                style = CmpTextStyle.Headline2,
                color = mode.palette[if (role == ThemeColorRole.Card) ThemeColorRole.Ink else role].toColor(),
                modifier = Modifier.padding(CmpSpacing.space16),
            )
        }
    }
}

@Composable
private fun ContrastChecks(checks: List<ContrastResult>) {
    if (checks.isEmpty()) return
    val colors = CmpTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        FieldLabel(stringResource(Strings.string.appearance_editor_label_contrast))
        checks.forEach { check ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpIcon(
                    icon = if (check.passes) CmpIcons.CheckCircle else CmpIcons.Cancel,
                    contentDescription = null,
                    tint = if (check.passes) colors.successDefault else colors.errorDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )
                CmpText(
                    text = check.kind.title(),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkPrimaryDefault,
                    modifier = Modifier.weight(1f),
                )
                CmpText(
                    text = stringResource(Strings.string.appearance_editor_format_ratio, formatRatio(check.ratio)),
                    style = CmpTextStyle.Headline3,
                    color = colors.foregroundOnDarkPrimaryDefault,
                )
                CmpText(
                    text = stringResource(Strings.string.appearance_editor_format_min, formatMin(check.min)),
                    style = CmpTextStyle.Additional1,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
    }
}

@Composable
private fun FailureBlock(editor: ColorEditorState, onFix: () -> Unit) {
    val fix = editor.fix
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpText(
            text = stringResource(
                if (fix != null) {
                    Strings.string.appearance_editor_error_fixable
                } else {
                    Strings.string.appearance_editor_error_related
                },
            ),
            style = CmpTextStyle.Paragraph3,
            color = CmpTheme.colors.errorDefault,
        )
        if (fix != null) {
            CmpButton(
                text = stringResource(Strings.string.appearance_editor_btn_fix, fix.hex),
                onClick = onFix,
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.S,
                uppercase = false,
                fullWidth = true,
                modifier = Modifier.testTag("appearance_editor_fix"),
            )
        }
    }
}
