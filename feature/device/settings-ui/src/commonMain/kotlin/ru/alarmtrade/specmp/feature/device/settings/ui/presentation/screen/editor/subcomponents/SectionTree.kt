package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.valueText

private val LevelIndent = 14.dp
private val RowHeight = 40.dp
private val ToggleSize = 32.dp

@Immutable
private data class TreeRow(
    val section: SettingsSection,
    val depth: Int,
    val hasSubsections: Boolean,
    val expanded: Boolean,
) {
    val enterable: Boolean get() = section.children.isNotEmpty()
}

/**
 * Раскрытие переключается стрелкой или повторным нажатием на выбранный раздел (в том числе на пути к нему);
 * свёрнутое на пути сбрасывается при смене выбора.
 */
@Composable
internal fun SectionTree(
    root: SettingsSection,
    selectedId: Int?,
    selectedPath: List<Int>,
    sectionChanges: Map<Int, Int>,
    changedIds: Set<Int>,
    /** Ширина колонки; лямбда — читается только при измерении, перетаскивание не рекомпозирует дерево. */
    width: () -> Dp,
    onEditSetting: (settingId: Int) -> Unit,
    onSelect: (sectionId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    var expanded by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var collapsed by rememberSaveable(selectedId) { mutableStateOf(listOf<Int>()) }

    val toggle = { id: Int, isOpen: Boolean ->
        if (isOpen) {
            expanded = expanded - id
            collapsed = collapsed + id
        } else {
            expanded = expanded + id
            collapsed = collapsed - id
        }
    }
    // Строки пересобираются только при смене меню или раскрытия: равные строки дерева не перерисовываются.
    val rows = remember(root, expanded, collapsed, selectedPath) {
        val open = expanded.toSet() + selectedPath - collapsed.toSet()
        buildList {
            fun walk(section: SettingsSection, depth: Int) {
                // Пустые разделы (связки «И» / «ИЛИ» между условиями, резерв) в дереве не показываются.
                val visible = section.children.filterIsInstance<SettingsSection>().filter { it.children.isNotEmpty() }
                visible.forEach { child ->
                    val hasSubsections = child.children.any { it is SettingsSection && it.children.isNotEmpty() }
                    val isOpen = hasSubsections && child.id in open
                    add(TreeRow(child, depth, hasSubsections, isOpen))
                    if (isOpen) walk(child, depth + 1)
                }
            }
            walk(root, 0)
        }
    }

    val rootSettings = remember(root) { root.children.filterIsInstance<Setting>() }

    LazyColumn(
        modifier = modifier
            .layout { measurable, constraints ->
                val px = constraints.constrainWidth(width().roundToPx())
                val placeable = measurable.measure(constraints.copy(minWidth = px, maxWidth = px))
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .fillMaxHeight()
            .background(colors.surfaceContainerLow)
            .padding(vertical = 4.dp),
    ) {
        items(rootSettings, key = { "setting-${it.id}" }) { setting ->
            TreeSettingRow(setting = setting, changed = setting.id in changedIds, onClick = {
                onEditSetting(setting.id)
            })
        }
        items(rows, key = { it.section.id }) { row ->
            TreeRowView(
                row = row,
                selected = row.section.id == selectedId,
                changes = sectionChanges[row.section.id] ?: 0,
                onToggle = { toggle(row.section.id, row.expanded) },
                onSelect = {
                    if (row.section.id == selectedId && row.hasSubsections) {
                        toggle(row.section.id, row.expanded)
                    } else {
                        onSelect(row.section.id)
                    }
                },
            )
        }
    }
}

@Composable
private fun TreeSettingRow(setting: Setting, changed: Boolean, onClick: () -> Unit) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .heightIn(min = RowHeight)
            .testTag(SettingsTestTags.row(setting.id))
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpBox(modifier = Modifier.size(width = ToggleSize, height = RowHeight), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = CmpIcons.Settings,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        CmpText(
            text = setting.number.takeIf { it.isNotEmpty() }?.let { "$it ${setting.name}" } ?: setting.name,
            style = CmpTextStyle.Paragraph1,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (changed) CmpDot()
        CmpText(
            text = valueText(setting),
            style = CmpTextStyle.Headline2,
            color = colors.primary,
            maxLines = 1,
            modifier = Modifier.padding(end = 12.dp),
        )
    }
}

@Composable
private fun TreeRowView(row: TreeRow, selected: Boolean, changes: Int, onToggle: () -> Unit, onSelect: () -> Unit) {
    val colors = cmpThemeColors()
    val content = when {
        selected -> colors.onSecondaryContainer
        row.enterable -> colors.onSurface
        else -> colors.onSurfaceVariant
    }
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .heightIn(min = RowHeight)
            .testTag(SettingsTestTags.tree(row.section.id))
            .clip(CircleShape)
            .background(if (selected) colors.secondaryContainer else colors.surfaceContainerLow)
            .then(if (row.enterable) Modifier.clickable(role = Role.Button, onClick = onSelect) else Modifier)
            .padding(start = LevelIndent * row.depth),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpBox(modifier = Modifier.size(width = ToggleSize, height = RowHeight), contentAlignment = Alignment.Center) {
            if (row.hasSubsections) {
                CmpIconButton(
                    onClick = onToggle,
                    icon = if (row.expanded) CmpIcons.KeyboardArrowDown else CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(ToggleSize),
                )
            }
        }
        CmpRow(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = RowHeight)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpText(
                text = row.section.number.takeIf { it.isNotEmpty() }?.let { "$it ${row.section.name}" }
                    ?: row.section.name,
                style = if (selected) CmpTextStyle.Headline2 else CmpTextStyle.Paragraph1,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (changes > 0) CmpCountPill(count = changes)
        }
    }
}
