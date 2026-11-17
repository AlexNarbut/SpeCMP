package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_error_export
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_error_not_theme
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_error_read
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_msg_colors_reset
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_msg_exported
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_msg_imported
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_msg_imported_fixed
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_btn_apply
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_btn_revert
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_hint_storage
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_msg_applied
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_status_applied
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_status_dirty
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_title
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_value_dark
import ru.alarmtrade.specmp.core.localization.resources.appearance_main_value_light
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpActionBar
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.AdvancedSection
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.ColorEditor
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.ColorSection
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.FileSection
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.Hint
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.PresetSection
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.ShapeSection
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents.ThemePreview

private val ContentMaxWidth = 1040.dp
private val PreviewWidthMedium = 320.dp
private val PreviewWidthExpanded = 400.dp

/** Экран в теме приложения; черновик виден только в предпросмотре и образцах. */
@Composable
internal fun AppearanceScreen(state: AppearanceState, actions: AppearanceActions, snackbar: CmpSnackbarHostState) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = stringResource(Strings.string.appearance_main_title),
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.appearance_main_a11y_back),
                    )
                },
            )
        },
        snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            AdaptiveContent(state, actions, padding, Modifier.weight(1f))
            ApplyBar(state.isDirty, actions)
        }
    }
}

/** Панель «Применить» — под содержимым, не поверх: её фон полупрозрачный. */
@Composable
private fun AdaptiveContent(
    state: AppearanceState,
    actions: AppearanceActions,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val direction = LocalLayoutDirection.current
    CmpAdaptiveBox(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = padding.calculateStartPadding(direction),
                end = padding.calculateEndPadding(direction),
            ),
        contentAlignment = Alignment.TopCenter,
    ) { window ->
        val content = PaddingValues(
            start = if (window.isCompact) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium,
            end = if (window.isCompact) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium,
            top = padding.calculateTopPadding() + CmpSpacing.screenPaddingTop,
            bottom = CmpSpacing.screenPaddingBottom,
        )
        if (window.isCompact) {
            CompactLayout(state, actions, content)
        } else {
            val previewWidth = if (window.atLeast(
                    CmpWidthClass.Expanded,
                )
            ) {
                PreviewWidthExpanded
            } else {
                PreviewWidthMedium
            }
            WideLayout(state, actions, content, previewWidth)
        }
        state.editor?.let { editor ->
            ColorEditor(
                editor = editor,
                mode = state.mode,
                isDark = state.isDark,
                compact = window.isCompact,
                actions = actions.editor,
            )
        }
    }
}

@Composable
private fun CompactLayout(state: AppearanceState, actions: AppearanceActions, content: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(content)
            .testTag("appearance_scroll"),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        PreviewPane(state, actions)
        Settings(state, actions)
    }
}

@Composable
private fun WideLayout(state: AppearanceState, actions: AppearanceActions, content: PaddingValues, previewWidth: Dp) {
    Row(
        modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space32),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = content.calculateStartPadding(LocalLayoutDirection.current))
                .padding(top = content.calculateTopPadding(), bottom = content.calculateBottomPadding())
                .testTag("appearance_scroll"),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            Settings(state, actions)
        }
        Column(
            modifier = Modifier
                .width(previewWidth)
                .verticalScroll(rememberScrollState())
                .padding(end = content.calculateEndPadding(LocalLayoutDirection.current))
                .padding(top = content.calculateTopPadding(), bottom = content.calculateBottomPadding()),
        ) {
            PreviewPane(state, actions)
        }
    }
}

@Composable
private fun PreviewPane(state: AppearanceState, actions: AppearanceActions) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpSegmentedButton(
            options = listOf(
                stringResource(Strings.string.appearance_main_value_light),
                stringResource(Strings.string.appearance_main_value_dark),
            ),
            selectedIndex = if (state.isDark) 1 else 0,
            onSelect = { actions.onModeSelect(it == 1) },
            modifier = Modifier.fillMaxWidth().testTag("appearance_mode"),
        )
        ThemePreview(mode = state.mode, isDark = state.isDark)
        Hint(stringResource(Strings.string.appearance_main_hint_storage))
    }
}

@Composable
private fun Settings(state: AppearanceState, actions: AppearanceActions) {
    PresetSection(selected = state.preset, onSelect = actions.onPresetSelect)
    ColorSection(mode = state.mode, isDark = state.isDark, actions = actions)
    ShapeSection(state = state, actions = actions)
    AdvancedSection(
        mode = state.mode,
        isDark = state.isDark,
        expanded = state.advancedExpanded,
        actions = actions,
    )
    FileSection(onExport = actions.onExport, onImport = actions.onImport)
}

/** На узком окне — только кнопки на всю ширину: статус черновика виден по их доступности. */
@Composable
private fun ApplyBar(isDirty: Boolean, actions: AppearanceActions) {
    CmpAdaptiveBox(modifier = Modifier.fillMaxWidth()) { window ->
        CmpActionBar {
            val buttonModifier = if (window.isCompact) Modifier.weight(1f) else Modifier
            if (!window.isCompact) {
                CmpText(
                    text = stringResource(
                        if (isDirty) {
                            Strings.string.appearance_main_status_dirty
                        } else {
                            Strings.string.appearance_main_status_applied
                        },
                    ),
                    style = CmpTextStyle.Paragraph3,
                    color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 2,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                )
            }
            CmpButton(
                text = stringResource(Strings.string.appearance_main_btn_revert),
                onClick = actions.onRevert,
                enabled = isDirty,
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.S,
                modifier = buttonModifier.testTag("appearance_revert"),
            )
            CmpButton(
                text = stringResource(Strings.string.appearance_main_btn_apply),
                onClick = actions.onApply,
                enabled = isDirty,
                type = CmpButtonType.Accent,
                size = CmpButtonSize.S,
                modifier = buttonModifier.testTag("appearance_apply"),
            )
        }
    }
}

/** Сообщения VM — снекбаром; ожидание показа — в своей корутине, сбор эффектов не ждёт UI. */
@Composable
internal fun AppearanceEffects(sideEffect: Flow<AppearanceEffect>, snackbar: CmpSnackbarHostState) {
    val scope = rememberCoroutineScope()
    sideEffect.collectSideEffect { effect ->
        scope.launch {
            val (message, tone) = when (effect) {
                AppearanceEffect.Applied ->
                    getString(Strings.string.appearance_main_msg_applied) to CmpSnackbarTone.Success

                is AppearanceEffect.Exported ->
                    getString(Strings.string.appearance_file_msg_exported, effect.fileName) to CmpSnackbarTone.Success

                AppearanceEffect.ExportFailed ->
                    getString(Strings.string.appearance_file_error_export) to CmpSnackbarTone.Error

                AppearanceEffect.ImportFailed ->
                    getString(Strings.string.appearance_file_error_read) to CmpSnackbarTone.Error

                AppearanceEffect.NotThemeFile ->
                    getString(Strings.string.appearance_file_error_not_theme) to CmpSnackbarTone.Error

                is AppearanceEffect.Imported -> importedMessage(effect)
            }
            snackbar.showSnackbar(message, tone = tone)
        }
    }
}

private suspend fun importedMessage(effect: AppearanceEffect.Imported): Pair<String, CmpSnackbarTone> = when {
    effect.colorsReset -> getString(Strings.string.appearance_file_msg_colors_reset) to CmpSnackbarTone.Info

    effect.fixedColors > 0 ->
        getString(Strings.string.appearance_file_msg_imported_fixed, effect.fixedColors) to CmpSnackbarTone.Info

    else -> getString(Strings.string.appearance_file_msg_imported) to CmpSnackbarTone.Success
}
