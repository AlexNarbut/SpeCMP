package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSearchHit
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.carLabel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.RowMarks
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_editor_msg_empty_section
import specmp.feature.device.settings_ui.generated.resources.settings_editor_msg_empty_section_hint
import specmp.feature.device.settings_ui.generated.resources.settings_search_a11y_clear
import specmp.feature.device.settings_ui.generated.resources.settings_search_msg_empty
import specmp.feature.device.settings_ui.generated.resources.settings_search_msg_empty_hint
import specmp.feature.device.settings_ui.generated.resources.settings_search_msg_prompt
import specmp.feature.device.settings_ui.generated.resources.settings_search_placeholder

private val ContentMaxWidth = 720.dp
private val CardRadius = 16.dp
private val CardBorder = 1.dp

/**
 * Содержимое раздела: строки его пунктов. На широкой раскладке — заголовок раздела со счётчиком и строки в карточке.
 */
@Composable
internal fun SectionPane(section: SettingsSection, marks: RowMarks, wide: Boolean, actions: SettingRowActions) {
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
                SectionHeader(section, marks.sectionChanges[section.id] ?: 0)
            }
        }
        rows(section.children, marks, wide, actions, paths = null)
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

/**
 * Поиск по названию, номеру и каналу: поле и результаты с путём раздела. Текст поля — локальный (набор не
 * перерисовывает экран); [query] и [hits] приходят из state вместе, после debounce.
 */
@Composable
internal fun SearchPane(
    query: String,
    hits: List<SettingsSearchHit>,
    marks: RowMarks,
    wide: Boolean,
    onQueryChange: (String) -> Unit,
    actions: SettingRowActions
) {
    var text by rememberSaveable { mutableStateOf(query) }
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpSearchField(
            value = text,
            onValueChange = {
                text = it
                onQueryChange(it)
            },
            placeholder = stringResource(Res.string.settings_search_placeholder),
            clearContentDescription = stringResource(Res.string.settings_search_a11y_clear),
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(SettingsTestTags.SEARCH_FIELD)
        )
        when {
            text.isBlank() -> EmptyMessage(
                icon = CmpIcons.Search,
                title = stringResource(Res.string.settings_search_msg_prompt),
                hint = null
            )

            // Текст набран, результат ещё считается (debounce).
            query.isBlank() -> Unit

            hits.isEmpty() -> EmptyMessage(
                icon = CmpIcons.Search,
                title = stringResource(Res.string.settings_search_msg_empty, query.trim()),
                hint = stringResource(Res.string.settings_search_msg_empty_hint)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = if (wide) PaddingValues(horizontal = 24.dp, vertical = 8.dp) else PaddingValues(0.dp)
            ) {
                rows(
                    nodes = hits.map { it.node },
                    marks = marks,
                    wide = wide,
                    actions = actions,
                    paths = hits.map { hit ->
                        hit.path.joinToString(" › ") { section ->
                            if (section.number.isEmpty()) section.name else "${section.number} ${section.name}"
                        }
                    }
                )
            }
        }
    }
}

/**
 * Строки пунктов, лениво по строке. На широкой раскладке строки складываются в карточку с рамкой: каждая рисует
 * свой отрезок карточки ([cardSegment]) — раздел из десятков строк и длинный результат поиска не композируются разом.
 */
private fun LazyListScope.rows(
    nodes: List<SettingNode>,
    marks: RowMarks,
    wide: Boolean,
    actions: SettingRowActions,
    paths: List<String>?
) {
    itemsIndexed(nodes, key = { _, node -> node.id }) { index, node ->
        val last = index == nodes.lastIndex
        if (wide) {
            CmpBox(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .cardSegment(
                        first = index == 0,
                        last = last,
                        color = cmpThemeColors().surface,
                        borderColor = cmpThemeColors().outlineVariant
                    )
            ) {
                CmpColumn { NodeRow(node, last, marks, wide = true, actions, paths?.getOrNull(index)) }
            }
        } else {
            NodeRow(node, last, marks, wide = false, actions, paths?.getOrNull(index))
        }
    }
}

/**
 * Отрезок карточки со скруглёнными углами и рамкой: у первой строки — верх, у последней — низ, у средних — только
 * боковые стороны (разделители между строками рисует сама строка).
 */
private fun Modifier.cardSegment(first: Boolean, last: Boolean, color: Color, borderColor: Color): Modifier {
    val top = if (first) CardRadius else 0.dp
    val bottom = if (last) CardRadius else 0.dp
    return clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
        .background(color)
        .drawBehind {
            val stroke = CardBorder.toPx()
            val radius = CardRadius.toPx()
            // Скруглённая рамка, продолженная за край отрезка там, где карточка продолжается: обрезка оставляет
            // от неё только нужные стороны.
            val y0 = if (first) stroke / 2 else -radius - stroke
            val y1 = if (last) size.height - stroke / 2 else size.height + radius + stroke
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(stroke / 2, y0),
                size = Size(size.width - stroke, y1 - y0),
                cornerRadius = CornerRadius(radius - stroke / 2),
                style = Stroke(stroke)
            )
        }
}

@Composable
private fun NodeRow(
    node: SettingNode,
    last: Boolean,
    marks: RowMarks,
    wide: Boolean,
    actions: SettingRowActions,
    path: String?
) {
    SettingNodeRow(
        node = node,
        changed = node.id in marks.changedIds,
        sectionChanges = marks.sectionChanges[node.id] ?: 0,
        wide = wide,
        actions = actions,
        path = path?.takeIf { it.isNotEmpty() },
        note = (node as? CanCodeSetting)?.value?.value?.carId?.let(marks.cars::get)?.let { carLabel(it) }
    )
    if (!last) CmpDivider(color = cmpThemeColors().outlineVariant)
}
