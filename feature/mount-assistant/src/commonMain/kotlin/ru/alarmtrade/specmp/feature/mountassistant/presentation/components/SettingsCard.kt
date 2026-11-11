package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_edit
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_open_in_settings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_show_all
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_hint_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_msg_editor_only
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_msg_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_title_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_title_settings
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultContent
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.settingValueText

private const val SHORT_LIST = 5

/** Найденные настройки: значение, «Изменить» (правка прямо в карточке) и «Открыть в настройках». */
@Composable
internal fun SettingsCard(entryId: Long, query: String, content: ResultContent, callbacks: FeedCallbacks) {
    val data = content as? ResultContent.SettingsData
    val nothing = data != null && data.found.isEmpty()
    CardShell(modifier = Modifier.testTag(resultTag(entryId))) {
        CardHeader(
            icon = if (nothing) CmpIcons.SearchOff else CmpIcons.Tune,
            title = stringResource(
                if (nothing) {
                    Strings.string.mountassistant_card_title_nothing
                } else {
                    Strings.string.mountassistant_card_title_settings
                },
            ),
            error = content is ResultContent.Error,
        ) {
            if (data != null && !nothing) AssistantPill(text = data.found.size.toString(), tone = PillTone.INFO)
        }
        when {
            content == ResultContent.Loading -> SkeletonRows(rows = 3)
            content is ResultContent.Error -> ResultError(content.failure) { callbacks.onRetry(entryId) }
            nothing -> NothingFound(query)
            data != null -> SettingsList(entryId, query, data, callbacks)
        }
    }
}

@Composable
private fun NothingFound(query: String) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CmpText(
            text = stringResource(Strings.string.mountassistant_card_msg_nothing, query),
            style = CmpTextStyle.BodyLarge,
        )
        CmpText(
            text = stringResource(Strings.string.mountassistant_card_hint_nothing),
            style = CmpTextStyle.BodyLarge,
            color = cmpThemeColors().onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsList(entryId: Long, query: String, data: ResultContent.SettingsData, callbacks: FeedCallbacks) {
    var showAll by rememberSaveable(entryId) { mutableStateOf(false) }
    val shown = if (showAll) data.found else data.found.take(SHORT_LIST)
    CmpColumn {
        shown.forEachIndexed { index, setting ->
            if (index > 0) CmpDivider()
            SettingRow(entryId, query, setting, data, callbacks)
        }
        if (!showAll && data.found.size > SHORT_LIST) {
            CmpOutlinedButton(
                text = stringResource(Strings.string.mountassistant_card_btn_show_all, data.found.size),
                onClick = { showAll = true },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(min = 48.dp),
            )
        }
    }
}

@Composable
private fun SettingRow(
    entryId: Long,
    query: String,
    setting: FoundSetting,
    data: ResultContent.SettingsData,
    callbacks: FeedCallbacks,
) {
    val colors = cmpThemeColors()
    val editing = data.editing == setting.id
    val value = setting.value
    CmpColumn(modifier = Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = highlighted(setting, query),
                    style = CmpTextStyle.BodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                CmpText(
                    text = setting.path.joinToString(" › "),
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant,
                )
            }
            if (value != null) AssistantPill(text = settingValueText(setting, value))
        }
        val draft = data.draft
        if (editing && value != null && draft != null) {
            SettingEditorBlock(
                setting = setting,
                current = value,
                draft = draft,
                onDraft = { callbacks.onDraftChange(entryId, it) },
                onCancel = { callbacks.onEditCancel(entryId) },
                onNext = { callbacks.onEditNext(entryId) },
            )
            OpenInSettingsAction { callbacks.onOpenInSettings(setting.id) }
        } else {
            CmpFlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (setting.editor != null && value != null) {
                    TextAction(CmpIcons.Edit, stringResource(Strings.string.mountassistant_card_btn_edit)) {
                        callbacks.onEditStart(entryId, setting.id)
                    }
                }
                OpenInSettingsAction { callbacks.onOpenInSettings(setting.id) }
            }
            if (setting.editor == null) {
                CmpText(
                    text = stringResource(Strings.string.mountassistant_card_msg_editor_only),
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** Доступно и во время правки: редактор откроется на разделе этой настройки, оттуда — все остальные. */
@Composable
private fun OpenInSettingsAction(onClick: () -> Unit) {
    TextAction(CmpIcons.OpenInNew, stringResource(Strings.string.mountassistant_card_btn_open_in_settings), onClick)
}

@Composable
internal fun TextAction(icon: CmpIcons, text: String, onClick: () -> Unit) {
    CmpTextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        CmpText(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}

/** Подсветка совпадений поиска в названии: диапазоны от SDK, иначе — вхождения запроса. */
@Composable
private fun highlighted(setting: FoundSetting, query: String): AnnotatedString =
    highlighted(setting.name, setting.nameHighlights.ifEmpty { occurrences(setting.name, query) })

private fun occurrences(text: String, query: String): List<IntRange> {
    if (query.isBlank()) return emptyList()
    val ranges = mutableListOf<IntRange>()
    var from = text.indexOf(query, ignoreCase = true)
    while (from >= 0) {
        ranges += from until from + query.length
        from = text.indexOf(query, startIndex = from + query.length, ignoreCase = true)
    }
    return ranges
}
