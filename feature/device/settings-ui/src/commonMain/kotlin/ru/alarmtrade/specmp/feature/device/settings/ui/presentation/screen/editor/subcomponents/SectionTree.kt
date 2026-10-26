package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

private class TreeRow(
    val section: SettingsSection,
    val depth: Int,
    val hasSubsections: Boolean,
    val expanded: Boolean
) {
    /** Пустой раздел не выбирается: без нажатия и наведения, текст приглушён. */
    val enterable: Boolean get() = section.children.isNotEmpty()
}

/**
 * Дерево разделов для широкой раскладки. Настройки корневого уровня меню (как «Код модели» в старом приложении) —
 * строками над разделами: значение справа, нажатие открывает окно настройки. Выбранный раздел выделен, путь к нему
 * раскрывается при переходе; у разделов с изменениями — счётчик. Раскрытие переключается стрелкой или повторным
 * нажатием на выбранный раздел — в том числе на пути к выбранному (свёрнутое на пути сбрасывается при смене выбора).
 */
@Composable
internal fun SectionTree(
    root: SettingsSection,
    selectedId: Int?,
    selectedPath: List<Int>,
    sectionChanges: Map<Int, Int>,
    changedIds: Set<Int>,
    width: Dp,
    onEditSetting: (settingId: Int) -> Unit,
    onSelect: (sectionId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    var expanded by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var collapsed by rememberSaveable(selectedId) { mutableStateOf(listOf<Int>()) }
    val open = expanded.toSet() + selectedPath - collapsed.toSet()
    val toggle = { id: Int, isOpen: Boolean ->
        if (isOpen) {
            expanded = expanded - id
            collapsed = collapsed + id
        } else {
            expanded = expanded + id
            collapsed = collapsed - id
        }
    }
    val rows = buildList {
        fun walk(section: SettingsSection, depth: Int) {
            // Пустые разделы (связки «И» / «ИЛИ» между условиями, резерв) в дереве не показываются.
            section.children.filterIsInstance<SettingsSection>().filter { it.children.isNotEmpty() }.forEach { child ->
                val hasSubsections = child.children.any { it is SettingsSection && it.children.isNotEmpty() }
                val isOpen = hasSubsections && child.id in open
                add(TreeRow(child, depth, hasSubsections, isOpen))
                if (isOpen) walk(child, depth + 1)
            }
        }
        walk(root, 0)
    }

    val rootSettings = root.children.filterIsInstance<Setting>()

    LazyColumn(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(colors.surfaceContainerLow)
            .padding(vertical = 4.dp)
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
                }
            )
        }
    }
}

/** Настройка корневого уровня: шестерёнка, номер и название, значение; нажатие — окно настройки. */
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CmpBox(modifier = Modifier.size(width = ToggleSize, height = RowHeight), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = CmpIcons.Settings,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        CmpText(
            text = setting.number.takeIf { it.isNotEmpty() }?.let { "$it ${setting.name}" } ?: setting.name,
            style = CmpTextStyle.BodyLarge,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (changed) CmpDot()
        CmpText(
            text = valueText(setting),
            style = CmpTextStyle.BodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            maxLines = 1,
            modifier = Modifier.padding(end = 12.dp)
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
            // Наведение и нажатие — на всю «таблетку» строки, как у выбранного раздела.
            .testTag(SettingsTestTags.tree(row.section.id))
            .clip(CircleShape)
            .background(if (selected) colors.secondaryContainer else colors.surfaceContainerLow)
            .then(if (row.enterable) Modifier.clickable(role = Role.Button, onClick = onSelect) else Modifier)
            .padding(start = LevelIndent * row.depth),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CmpBox(modifier = Modifier.size(width = ToggleSize, height = RowHeight), contentAlignment = Alignment.Center) {
            if (row.hasSubsections) {
                CmpIconButton(
                    onClick = onToggle,
                    icon = if (row.expanded) CmpIcons.KeyboardArrowDown else CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(ToggleSize)
                )
            }
        }
        CmpRow(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = RowHeight)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CmpText(
                text = row.section.number.takeIf { it.isNotEmpty() }?.let { "$it ${row.section.name}" }
                    ?: row.section.name,
                style = CmpTextStyle.BodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else null,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (changes > 0) CmpCountPill(count = changes)
        }
    }
}
