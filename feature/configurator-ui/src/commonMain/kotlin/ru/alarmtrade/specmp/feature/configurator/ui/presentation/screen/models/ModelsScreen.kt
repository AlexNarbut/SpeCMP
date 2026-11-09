package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.ConfiguratorTestTags
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
                title = { CmpText(text = stringResource(Strings.string.configurator_models_title)) },
                subtitle = {
                    CmpText(
                        text =
                            state.fileName?.let { stringResource(Strings.string.configurator_models_subtitle_file, it) }
                                ?: stringResource(Strings.string.configurator_models_subtitle_new),
                        style = CmpTextStyle.Caption,
                        maxLines = 1,
                    )
                },
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag(ConfiguratorTestTags.MODELS_SEARCH),
            )
            when (val list = state.list) {
                LCEState.None, LCEState.Loading -> CmpBox(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpCircularProgressIndicator()
                }

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
            title = { CmpText(text = stringResource(Strings.string.configurator_file_error_title, model)) },
            text = { CmpText(text = stringResource(Strings.string.configurator_file_error_msg)) },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.configurator_common_btn_other_model),
                    onClick = onDialogDismiss,
                )
            },
            dismissButton = {
                CmpTextButton(text = stringResource(Strings.string.configurator_common_btn_cancel), onClick = onBack)
            },
        )
    }
    if (state.checking) {
        CmpDialog(
            onDismissRequest = {},
            title = { CmpText(text = stringResource(Strings.string.configurator_file_loading_checking)) },
            text = {
                CmpBox(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CmpCircularProgressIndicator()
                }
            },
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
    LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                CmpText(
                    text = stringResource(Strings.string.configurator_models_msg_nothing, q),
                    color = cmpThemeColors().onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
        } else {
            group(Strings.string.configurator_models_label_all, filtered, null, onPick)
        }
    }
}

private fun LazyListScope.group(
    title: StringResource,
    names: List<String>,
    icon: CmpIcons?,
    onPick: (String) -> Unit,
    onRemove: ((String) -> Unit)? = null,
) {
    item(key = "header_${title.key}") {
        CmpColumn {
            CmpDivider(color = cmpThemeColors().outlineVariant)
            CmpText(
                text = stringResource(title),
                style = CmpTextStyle.Body,
                fontWeight = FontWeight.Medium,
                color = cmpThemeColors().onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            )
        }
    }
    items(names, key = { "${title.key}_$it" }) { name ->
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { onPick(name) }
                .testTag(ConfiguratorTestTags.model(name))
                .padding(start = 16.dp, end = if (onRemove != null) 4.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            icon?.let { CmpIcon(icon = it, contentDescription = null, tint = cmpThemeColors().onSurfaceVariant) }
            CmpText(text = name, style = CmpTextStyle.BodyLarge, modifier = Modifier.weight(1f))
            onRemove?.let { remove ->
                CmpIconButton(
                    onClick = { remove(name) },
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.configurator_models_a11y_recent_remove),
                    tint = cmpThemeColors().onSurfaceVariant,
                )
            }
        }
    }
}
