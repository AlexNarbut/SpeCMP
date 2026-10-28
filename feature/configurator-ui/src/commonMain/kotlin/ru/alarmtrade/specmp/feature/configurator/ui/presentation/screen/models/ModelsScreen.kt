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
import specmp.feature.configurator_ui.generated.resources.Res
import specmp.feature.configurator_ui.generated.resources.configurator_a11y_back
import specmp.feature.configurator_ui.generated.resources.configurator_a11y_clear
import specmp.feature.configurator_ui.generated.resources.configurator_btn_cancel
import specmp.feature.configurator_ui.generated.resources.configurator_btn_other_model
import specmp.feature.configurator_ui.generated.resources.configurator_btn_retry
import specmp.feature.configurator_ui.generated.resources.configurator_file_checking
import specmp.feature.configurator_ui.generated.resources.configurator_file_error_msg
import specmp.feature.configurator_ui.generated.resources.configurator_file_error_title
import specmp.feature.configurator_ui.generated.resources.configurator_models_a11y_recent_remove
import specmp.feature.configurator_ui.generated.resources.configurator_models_all
import specmp.feature.configurator_ui.generated.resources.configurator_models_connected
import specmp.feature.configurator_ui.generated.resources.configurator_models_error
import specmp.feature.configurator_ui.generated.resources.configurator_models_error_msg
import specmp.feature.configurator_ui.generated.resources.configurator_models_nothing
import specmp.feature.configurator_ui.generated.resources.configurator_models_recent
import specmp.feature.configurator_ui.generated.resources.configurator_models_search
import specmp.feature.configurator_ui.generated.resources.configurator_models_sub_file
import specmp.feature.configurator_ui.generated.resources.configurator_models_sub_new
import specmp.feature.configurator_ui.generated.resources.configurator_models_title

/** K2 выбор модели блока: подключённый блок и недавние сверху, поиск, все модели. */
@Composable
internal fun ModelsScreen(
    state: ModelsState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onPick: (String) -> Unit,
    onRecentRemove: (String) -> Unit,
    onRetry: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Res.string.configurator_models_title)) },
                subtitle = {
                    CmpText(
                        text = state.fileName?.let { stringResource(Res.string.configurator_models_sub_file, it) }
                            ?: stringResource(Res.string.configurator_models_sub_new),
                        style = CmpTextStyle.Caption,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.configurator_a11y_back)
                    )
                }
            )
        }
    ) { padding ->
        CmpColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            CmpSearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(Res.string.configurator_models_search),
                clearContentDescription = stringResource(Res.string.configurator_a11y_clear),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag(ConfiguratorTestTags.MODELS_SEARCH)
            )
            when (val list = state.list) {
                ModelsList.Loading -> CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpCircularProgressIndicator()
                }

                ModelsList.Error -> CmpStateBox(
                    icon = CmpIcons.CloudOff,
                    title = stringResource(Res.string.configurator_models_error),
                    text = stringResource(Res.string.configurator_models_error_msg),
                    tone = CmpStateTone.Error,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CmpButton(
                        text = stringResource(Res.string.configurator_btn_retry),
                        onClick = onRetry,
                        modifier = Modifier.testTag(ConfiguratorTestTags.RETRY)
                    )
                }

                is ModelsList.Ready -> ModelsList(state, list.models, onPick, onRecentRemove)
            }
        }
    }
    state.fileError?.let { model ->
        CmpDialog(
            onDismissRequest = onDialogDismiss,
            title = { CmpText(text = stringResource(Res.string.configurator_file_error_title, model)) },
            text = { CmpText(text = stringResource(Res.string.configurator_file_error_msg)) },
            confirmButton = {
                CmpButton(text = stringResource(Res.string.configurator_btn_other_model), onClick = onDialogDismiss)
            },
            dismissButton = {
                CmpTextButton(text = stringResource(Res.string.configurator_btn_cancel), onClick = onBack)
            }
        )
    }
    if (state.checking) {
        CmpDialog(
            onDismissRequest = {},
            title = { CmpText(text = stringResource(Res.string.configurator_file_checking)) },
            text = {
                CmpBox(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CmpCircularProgressIndicator()
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun ModelsList(
    state: ModelsState,
    models: List<String>,
    onPick: (String) -> Unit,
    onRecentRemove: (String) -> Unit
) {
    val q = state.query.trim()
    val filtered = models.filter { q.isEmpty() || it.contains(q, ignoreCase = true) }
    val recent = state.recent.filter { q.isEmpty() || it.contains(q, ignoreCase = true) }
    val connected = state.connected?.takeIf { q.isEmpty() || it.contains(q, ignoreCase = true) }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        connected?.let { name ->
            group(Res.string.configurator_models_connected, listOf(name), CmpIcons.Bluetooth, onPick)
        }
        if (recent.isNotEmpty()) {
            group(Res.string.configurator_models_recent, recent, CmpIcons.History, onPick, onRemove = onRecentRemove)
        }
        if (filtered.isEmpty()) {
            item(key = "nothing") {
                CmpText(
                    text = stringResource(Res.string.configurator_models_nothing, q),
                    color = cmpThemeColors().onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
        } else {
            group(Res.string.configurator_models_all, filtered, null, onPick)
        }
    }
}

/** Группа моделей; [onRemove] — «✕» справа у каждой (недавние). */
private fun LazyListScope.group(
    title: StringResource,
    names: List<String>,
    icon: CmpIcons?,
    onPick: (String) -> Unit,
    onRemove: ((String) -> Unit)? = null
) {
    item(key = "header_${title.key}") {
        CmpColumn {
            CmpDivider(color = cmpThemeColors().outlineVariant)
            CmpText(
                text = stringResource(title),
                style = CmpTextStyle.Body,
                fontWeight = FontWeight.Medium,
                color = cmpThemeColors().onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            icon?.let { CmpIcon(icon = it, contentDescription = null, tint = cmpThemeColors().onSurfaceVariant) }
            CmpText(text = name, style = CmpTextStyle.BodyLarge, modifier = Modifier.weight(1f))
            onRemove?.let { remove ->
                CmpIconButton(
                    onClick = { remove(name) },
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Res.string.configurator_models_a11y_recent_remove),
                    tint = cmpThemeColors().onSurfaceVariant
                )
            }
        }
    }
}
