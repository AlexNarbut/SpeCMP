package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_other_model
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.configurator_file_error_msg
import ru.alarmtrade.specmp.core.localization.resources.configurator_file_error_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_file_loading_checking
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_a11y_recent_remove
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_error
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_error_msg
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_label_all
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_label_connected
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_label_recent
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_msg_nothing
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_placeholder_search
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_subtitle_file
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_subtitle_new
import ru.alarmtrade.specmp.core.localization.resources.configurator_models_title
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.ConfiguratorTestTags
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.SkeletonCards
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.text

@Composable
internal fun ModelsScreen(
    state: ModelsState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onPick: (String) -> Unit,
    onRecentRemove: (String) -> Unit,
    onRetry: () -> Unit,
    onDialogDismiss: () -> Unit,
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = stringResource(Strings.string.configurator_models_title),
                subtitle = state.fileName?.let { stringResource(Strings.string.configurator_models_subtitle_file, it) }
                    ?: stringResource(Strings.string.configurator_models_subtitle_new),
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.configurator_common_a11y_back),
                    )
                },
            )
        },
    ) { padding ->
        CmpColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            CmpSearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(Strings.string.configurator_models_placeholder_search),
                clearContentDescription = stringResource(Strings.string.configurator_common_a11y_clear),
                modifier = Modifier.fillMaxWidth()
                    .padding(
                        start = CmpSpacing.screenPaddingH,
                        end = CmpSpacing.screenPaddingH,
                        top = CmpSpacing.space4,
                        bottom = CmpSpacing.space8,
                    )
                    .testTag(ConfiguratorTestTags.MODELS_SEARCH),
            )
            when (val list = state.list) {
                LCEState.None, LCEState.Loading -> SkeletonCards(SKELETON_ROWS, cardHeight = CmpSize.listItem)

                is LCEState.Error -> CmpStateBox(
                    icon = CmpIcons.CloudOff,
                    title = list.error.text(Strings.string.configurator_models_error),
                    text = stringResource(Strings.string.configurator_models_error_msg),
                    tone = CmpStateTone.Error,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CmpButton(
                        text = stringResource(Strings.string.configurator_common_btn_retry),
                        onClick = onRetry,
                        modifier = Modifier.testTag(ConfiguratorTestTags.RETRY),
                    )
                }

                is LCEState.Content -> ModelsList(state, list.content, onPick, onRecentRemove)
            }
        }
    }
    state.fileError?.let { model ->
        CmpDialog(
            onDismissRequest = onDialogDismiss,
            title = {
                CmpText(
                    text = stringResource(Strings.string.configurator_file_error_title, model),
                    style = CmpTextStyle.Headline1,
                )
            },
            text = {
                CmpText(
                    text = stringResource(Strings.string.configurator_file_error_msg),
                    style = CmpTextStyle.Paragraph2,
                )
            },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.configurator_common_btn_other_model),
                    onClick = onDialogDismiss,
                    size = CmpButtonSize.S,
                )
            },
            dismissButton = {
                CmpButton(
                    text = stringResource(Strings.string.configurator_common_btn_cancel),
                    onClick = onBack,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
            },
        )
    }
    if (state.checking) {
        CmpDialog(
            onDismissRequest = {},
            title = {
                CmpText(
                    text = stringResource(Strings.string.configurator_file_loading_checking),
                    style = CmpTextStyle.Headline1,
                )
            },
            text = { CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
            confirmButton = {},
        )
    }
}

@Composable
private fun ModelsList(
    state: ModelsState,
    models: List<String>,
    onPick: (String) -> Unit,
    onRecentRemove: (String) -> Unit,
) {
    val q = state.query.trim()
    val filtered = models.filter { q.isEmpty() || it.contains(q, ignoreCase = true) }
    val recent = state.recent.filter { q.isEmpty() || it.contains(q, ignoreCase = true) }
    val connected = state.connected?.takeIf { q.isEmpty() || it.contains(q, ignoreCase = true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().hideKeyboardOnScroll(),
        contentPadding = PaddingValues(
            start = CmpSpacing.screenPaddingH,
            end = CmpSpacing.screenPaddingH,
            bottom = CmpSpacing.screenPaddingBottom,
        ),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        connected?.let { name ->
            group(Strings.string.configurator_models_label_connected, listOf(name), CmpIcons.Bluetooth, onPick)
        }
        if (recent.isNotEmpty()) {
            group(
                Strings.string.configurator_models_label_recent,
                recent,
                CmpIcons.History,
                onPick,
                onRemove = onRecentRemove,
            )
        }
        if (filtered.isEmpty()) {
            item(key = "nothing") {
                CmpStateBox(
                    icon = CmpIcons.SearchOff,
                    title = stringResource(Strings.string.configurator_models_msg_nothing, q),
                    compact = true,
                )
            }
        } else {
            group(Strings.string.configurator_models_label_all, filtered, CmpIcons.Memory, onPick)
        }
    }
}

private fun LazyListScope.group(
    title: StringResource,
    names: List<String>,
    icon: CmpIcons,
    onPick: (String) -> Unit,
    onRemove: ((String) -> Unit)? = null,
) {
    item(key = "header_${title.key}") {
        CmpText(
            text = stringResource(title),
            style = CmpTextStyle.Headline3,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.padding(top = CmpSpacing.space12, bottom = CmpSpacing.space4),
        )
    }
    items(names, key = { "${title.key}_$it" }) { name ->
        CmpListItem(
            title = name,
            icon = icon,
            onClick = { onPick(name) },
            trailing = onRemove?.let { remove ->
                {
                    CmpIconButton(
                        onClick = { remove(name) },
                        icon = CmpIcons.Close,
                        contentDescription = stringResource(Strings.string.configurator_models_a11y_recent_remove),
                        tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().testTag(ConfiguratorTestTags.model(name)),
        )
    }
}

private const val SKELETON_ROWS = 8
