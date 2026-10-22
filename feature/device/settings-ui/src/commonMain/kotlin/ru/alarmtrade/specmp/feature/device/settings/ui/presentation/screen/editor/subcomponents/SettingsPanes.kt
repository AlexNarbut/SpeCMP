package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsState
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_editor_msg_empty_section
import specmp.feature.device.settings_ui.generated.resources.settings_editor_msg_empty_section_hint
import specmp.feature.device.settings_ui.generated.resources.settings_search_a11y_clear
import specmp.feature.device.settings_ui.generated.resources.settings_search_msg_empty
import specmp.feature.device.settings_ui.generated.resources.settings_search_msg_empty_hint
import specmp.feature.device.settings_ui.generated.resources.settings_search_msg_prompt
import specmp.feature.device.settings_ui.generated.resources.settings_search_placeholder

private val ContentMaxWidth = 720.dp

/**
 * Содержимое раздела: строки его пунктов. На широкой раскладке — заголовок раздела со счётчиком и строки в карточке.
 */
@Composable
internal fun SectionPane(
    section: SettingsSection,
    state: DeviceSettingsState,
    wide: Boolean,
    actions: SettingRowActions
) {
    if (section.children.isEmpty()) {
        EmptyMessage(
            icon = CmpIcons.List,
            title = stringResource(Res.string.settings_editor_msg_empty_section),
            hint = stringResource(Res.string.settings_editor_msg_empty_section_hint)
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = if (wide) PaddingValues(horizontal = 24.dp, vertical = 16.dp) else PaddingValues(0.dp)
    ) {
        if (wide) {
            item(key = "header") {
                SectionHeader(section, state.sectionChanges[section.id] ?: 0)
            }
        }
        rows(section.children, state, wide, actions, paths = null)
    }
}

@Composable
private fun SectionHeader(section: SettingsSection, changes: Int) {
    CmpRow(
        modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().padding(start = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpText(
            text = if (section.number.isEmpty()) section.name else "${section.number}  ${section.name}",
            style = CmpTextStyle.Title,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (changes > 0) CmpCountPill(count = changes)
    }
}

/** Поиск по названию, номеру и каналу: поле и результаты с путём раздела. */
@Composable
internal fun SearchPane(
    state: DeviceSettingsState,
    wide: Boolean,
    onQueryChange: (String) -> Unit,
    actions: SettingRowActions
) {
    val query = state.searchQuery
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(Res.string.settings_search_placeholder),
            singleLine = true,
            leadingIcon = { CmpIcon(icon = CmpIcons.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    CmpIconButton(
                        onClick = { onQueryChange("") },
                        icon = CmpIcons.Close,
                        contentDescription = stringResource(Res.string.settings_search_a11y_clear)
                    )
                }
            } else {
                null
            },
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        when {
            query.isBlank() -> EmptyMessage(
                icon = CmpIcons.Search,
                title = stringResource(Res.string.settings_search_msg_prompt),
                hint = null
            )

            state.searchHits.isEmpty() -> EmptyMessage(
                icon = CmpIcons.Search,
                title = stringResource(Res.string.settings_search_msg_empty, query.trim()),
                hint = stringResource(Res.string.settings_search_msg_empty_hint)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = if (wide) PaddingValues(horizontal = 24.dp, vertical = 8.dp) else PaddingValues(0.dp)
            ) {
                rows(
                    nodes = state.searchHits.map { it.node },
                    state = state,
                    wide = wide,
                    actions = actions,
                    paths = state.searchHits.map { hit ->
                        hit.path.joinToString(" › ") { section ->
                            if (section.number.isEmpty()) section.name else "${section.number} ${section.name}"
                        }
                    }
                )
            }
        }
    }
}

/** Строки пунктов: на компактной раскладке — лениво по строке, на широкой — одной карточкой с рамкой. */
private fun LazyListScope.rows(
    nodes: List<SettingNode>,
    state: DeviceSettingsState,
    wide: Boolean,
    actions: SettingRowActions,
    paths: List<String>?
) {
    if (wide) {
        item(key = "card") {
            CmpBox(modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth()) {
                CmpSurface(
                    shape = RoundedCornerShape(16.dp),
                    color = cmpThemeColors().surface,
                    border = BorderStroke(1.dp, cmpThemeColors().outlineVariant)
                ) {
                    CmpColumn {
                        nodes.forEachIndexed { index, node ->
                            NodeRow(
                                node,
                                index == nodes.lastIndex,
                                state,
                                wide = true,
                                actions,
                                paths?.getOrNull(index)
                            )
                        }
                    }
                }
            }
        }
    } else {
        itemsIndexed(nodes, key = { _, node -> node.id }) { index, node ->
            NodeRow(node, index == nodes.lastIndex, state, wide = false, actions, paths?.getOrNull(index))
        }
    }
}

@Composable
private fun NodeRow(
    node: SettingNode,
    last: Boolean,
    state: DeviceSettingsState,
    wide: Boolean,
    actions: SettingRowActions,
    path: String?
) {
    SettingNodeRow(
        node = node,
        changed = node.id in state.changedIds,
        sectionChanges = state.sectionChanges[node.id] ?: 0,
        wide = wide,
        actions = actions,
        path = path?.takeIf { it.isNotEmpty() }
    )
    if (!last) CmpDivider(color = cmpThemeColors().outlineVariant)
}
