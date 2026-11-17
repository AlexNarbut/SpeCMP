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
import androidx.compose.runtime.ReadOnlyComposable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_msg_empty_section
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_msg_empty_section_hint
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_msg_no_settings
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_msg_no_settings_hint
import ru.alarmtrade.specmp.core.localization.resources.settings_search_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.settings_search_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.settings_search_msg_empty_hint
import ru.alarmtrade.specmp.core.localization.resources.settings_search_msg_prompt
import ru.alarmtrade.specmp.core.localization.resources.settings_search_placeholder
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SearchHighlights
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSearchHit
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.carLabel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.RowMarks

private val ContentMaxWidth = 720.dp
private val CardRadius: Dp
    @Composable @ReadOnlyComposable
    get() = CmpRadius.cardMedium
private val CardBorder = CmpBorderWidth.listItem

@Composable
internal fun SectionPane(
    section: SettingsSection,
    marks: RowMarks,
    wide: Boolean,
    actions: SettingRowActions,
    noSettings: Boolean = false,
) {
    if (noSettings) {
        EmptyMessage(
            icon = CmpIcons.List,
            title = stringResource(Strings.string.settings_editor_msg_no_settings),
            hint = stringResource(Strings.string.settings_editor_msg_no_settings_hint),
        )
        return
    }
    if (section.children.isEmpty()) {
        EmptyMessage(
            icon = CmpIcons.List,
            title = stringResource(Strings.string.settings_editor_msg_empty_section),
            hint = stringResource(Strings.string.settings_editor_msg_empty_section_hint),
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().hideKeyboardOnScroll(),
        contentPadding = if (wide) {
            PaddingValues(horizontal = CmpSpacing.space24, vertical = CmpSpacing.space16)
        } else {
            PaddingValues(0.dp)
        },
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
        modifier = Modifier.widthIn(
            max = ContentMaxWidth,
        ).fillMaxWidth().padding(start = CmpSpacing.space4, bottom = CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpText(
            text = if (section.number.isEmpty()) section.name else "${section.number}  ${section.name}",
            style = CmpTextStyle.Headline1,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (changes > 0) CmpCountPill(count = changes)
    }
}

/** Текст поля — локальный (набор не перерисовывает экран); [query] и [hits] приходят из state вместе. */
@Composable
internal fun SearchPane(
    query: String,
    hits: List<SettingsSearchHit>,
    marks: RowMarks,
    wide: Boolean,
    onQueryChange: (String) -> Unit,
    actions: SettingRowActions,
) {
    var text by rememberSaveable { mutableStateOf(query) }
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpSearchField(
            value = text,
            onValueChange = {
                text = it
                onQueryChange(it)
            },
            placeholder = stringResource(Strings.string.settings_search_placeholder),
            clearContentDescription = stringResource(Strings.string.settings_search_a11y_clear),
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space8)
                .testTag(SettingsTestTags.SEARCH_FIELD),
        )
        when {
            text.isBlank() -> EmptyMessage(
                icon = CmpIcons.Search,
                title = stringResource(Strings.string.settings_search_msg_prompt),
                hint = null,
            )

            query.isBlank() -> Unit

            hits.isEmpty() -> EmptyMessage(
                icon = CmpIcons.Search,
                title = stringResource(Strings.string.settings_search_msg_empty, query.trim()),
                hint = stringResource(Strings.string.settings_search_msg_empty_hint),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().hideKeyboardOnScroll(),
                contentPadding = if (wide) {
                    PaddingValues(horizontal = CmpSpacing.space24, vertical = CmpSpacing.space8)
                } else {
                    PaddingValues(0.dp)
                },
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
                    },
                    highlights = hits.map { it.highlights },
                )
            }
        }
    }
}

/** Каждая строка рисует свой отрезок карточки ([cardSegment]): длинный раздел не композируется разом. */
private fun LazyListScope.rows(
    nodes: List<SettingNode>,
    marks: RowMarks,
    wide: Boolean,
    actions: SettingRowActions,
    paths: List<String>?,
    highlights: List<SearchHighlights>? = null,
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
                        color = CmpTheme.colors.backgroundSurfaceCard,
                        borderColor = CmpTheme.colors.borderHighlight,
                        cardRadius = CardRadius,
                    ),
            ) {
                CmpColumn {
                    NodeRow(
                        node,
                        last,
                        marks,
                        wide = true,
                        actions,
                        paths?.getOrNull(index),
                        highlights?.getOrNull(index),
                    )
                }
            }
        } else {
            NodeRow(node, last, marks, wide = false, actions, paths?.getOrNull(index), highlights?.getOrNull(index))
        }
    }
}

private fun Modifier.cardSegment(
    first: Boolean,
    last: Boolean,
    color: Color,
    borderColor: Color,
    cardRadius: Dp,
): Modifier {
    val top = if (first) cardRadius else 0.dp
    val bottom = if (last) cardRadius else 0.dp
    return clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
        .background(color)
        .drawBehind {
            val stroke = CardBorder.toPx()
            val radius = cardRadius.toPx()
            // Скруглённая рамка, продолженная за край отрезка там, где карточка продолжается: обрезка оставляет
            // от неё только нужные стороны.
            val y0 = if (first) stroke / 2 else -radius - stroke
            val y1 = if (last) size.height - stroke / 2 else size.height + radius + stroke
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(stroke / 2, y0),
                size = Size(size.width - stroke, y1 - y0),
                cornerRadius = CornerRadius(radius - stroke / 2),
                style = Stroke(stroke),
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
    path: String?,
    highlights: SearchHighlights?,
) {
    SettingNodeRow(
        node = node,
        changed = node.id in marks.changedIds,
        sectionChanges = marks.sectionChanges[node.id] ?: 0,
        wide = wide,
        actions = actions,
        path = path?.takeIf { it.isNotEmpty() },
        note = (node as? CanCodeSetting)?.value?.value?.carId?.let(marks.cars::get)?.let { carLabel(it) },
        highlights = highlights,
    )
    if (!last) CmpDivider()
}
