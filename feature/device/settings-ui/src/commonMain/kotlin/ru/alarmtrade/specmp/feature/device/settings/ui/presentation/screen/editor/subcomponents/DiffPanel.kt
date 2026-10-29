package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_nc
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_no
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_empty
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_minus_sign
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_plus_sign
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_btn_load
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_error
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_hint_can
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_label_col_number
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_label_col_setting
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_label_col_was
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_label_col_will
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_label_root
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_label_was
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_label_will
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_loading_busy
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_msg_pending
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_msg_same
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_msg_search_empty
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_msg_summary
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_placeholder_search
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_subtitle
import ru.alarmtrade.specmp.core.localization.resources.settings_diff_title
import ru.alarmtrade.specmp.core.localization.resources.settings_source_title_defaults
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SectionChanges
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingChange
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsDiff
import ru.alarmtrade.specmp.feature.device.settings.domain.model.channelOrNull
import ru.alarmtrade.specmp.feature.device.settings.domain.model.filter
import ru.alarmtrade.specmp.feature.device.settings.domain.model.logicsChange
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.settingTitle
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.valueText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DiffState

/**
 * K9 «Что изменится» перед загрузкой конфигурации: итог одной строкой, предупреждение о незаписанных правках, CAN-код
 * первой выделенной строкой, разделы с числом изменений (свёрнуты, если изменений больше 10 — раскрыт первый), строки
 * «было → станет» (тап — полные значения), поиск при > 20 изменений. Телефон — полноэкранно, широкое окно — диалог
 * с колонками «№ / Настройка / Было / Станет».
 */
@Composable
internal fun DiffPanel(state: DiffState, wide: Boolean, actions: TransferActions) {
    val title = stringResource(Strings.string.settings_diff_title)
    val subtitle = stringResource(
        Strings.string.settings_diff_subtitle,
        state.title.ifEmpty { stringResource(Strings.string.settings_source_title_defaults) }
    )
    if (wide) {
        CmpDialog(
            onDismissRequest = actions.onDiffDismiss,
            width = CmpDialogWidth.Wide,
            title = {
                CmpColumn {
                    DialogTitle(text = title, onClose = actions.onDiffDismiss)
                    CmpText(text = subtitle, style = CmpTextStyle.Body, color = cmpThemeColors().onSurfaceVariant)
                }
            },
            text = {
                CmpColumn(modifier = Modifier.fillMaxWidth().heightIn(max = WideListMaxHeight)) {
                    DiffBody(state, wide = true)
                }
            },
            confirmButton = { DiffButtons(state, actions) }
        )
    } else {
        CmpFullScreenDialog(onDismissRequest = actions.onDiffDismiss) {
            CmpColumn(modifier = Modifier.fillMaxSize().background(cmpThemeColors().surface)) {
                CmpTopAppBar(
                    title = { CmpText(text = title) },
                    subtitle = { CmpText(text = subtitle, style = CmpTextStyle.Caption, maxLines = 1) },
                    navigationIcon = {
                        CmpIconButton(
                            onClick = actions.onDiffDismiss,
                            icon = CmpIcons.Close,
                            contentDescription = stringResource(Strings.string.settings_common_a11y_close)
                        )
                    }
                )
                CmpColumn(modifier = Modifier.weight(1f).fillMaxWidth()) { DiffBody(state, wide = false) }
                CmpDivider(color = cmpThemeColors().outlineVariant)
                CmpRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) { DiffButtons(state, actions) }
            }
        }
    }
}

@Composable
private fun DiffButtons(state: DiffState, actions: TransferActions) {
    CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        when (state) {
            is DiffState.Busy -> CmpTextButton(
                text = stringResource(Strings.string.settings_common_btn_cancel),
                onClick = actions.onDiffDismiss
            )

            is DiffState.Error -> {
                CmpTextButton(
                    text = stringResource(Strings.string.settings_common_btn_close),
                    onClick = actions.onDiffDismiss
                )
                CmpButton(
                    text = stringResource(Strings.string.settings_common_btn_retry),
                    onClick = actions.onDiffRetry
                )
            }

            is DiffState.Ready -> if (state.diff.isEmpty) {
                CmpButton(
                    text = stringResource(Strings.string.settings_common_btn_close),
                    onClick = actions.onDiffDismiss
                )
            } else {
                CmpTextButton(
                    text = stringResource(Strings.string.settings_common_btn_cancel),
                    onClick = actions.onDiffDismiss
                )
                CmpButton(
                    text = stringResource(Strings.string.settings_diff_btn_load),
                    onClick = actions.onDiffConfirm,
                    modifier = Modifier.widthIn(min = 140.dp).testTag(SettingsTestTags.DIFF_LOAD)
                )
            }
        }
    }
}

@Composable
private fun DiffBody(state: DiffState, wide: Boolean) {
    val colors = cmpThemeColors()
    when (state) {
        is DiffState.Busy -> CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            CmpText(text = stringResource(Strings.string.settings_diff_loading_busy), color = colors.onSurfaceVariant)
        }

        is DiffState.Error -> CmpText(
            text = stringResource(Strings.string.settings_diff_error),
            modifier = Modifier.padding(24.dp)
        )

        is DiffState.Ready -> DiffList(state.diff, state.pendingChanges, wide)
    }
}

@Composable
private fun DiffList(diff: SettingsDiff, pendingChanges: Int, wide: Boolean) {
    val colors = cmpThemeColors()
    var expanded by rememberSaveable(diff) { mutableStateOf(diff.initiallyExpanded()) }
    var openRow by remember(diff) { mutableStateOf<Int?>(null) }
    var query by rememberSaveable(diff) { mutableStateOf("") }
    // Фильтр — доменное правило поиска; пересчёт только при смене диффа или запроса, а не на каждую рекомпозицию.
    val q = query.trim()
    val shown = remember(diff, q) { diff.filter(q) }
    val sections = shown.sections
    val canCode = shown.canCode
    val sidePadding = if (wide) 0.dp else 16.dp
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item(key = "summary") {
            DiffSummary(diff, pendingChanges, query, { query = it }, Modifier.padding(horizontal = sidePadding))
        }
        if (wide && !diff.isEmpty) item(key = "columns") { ColumnsHeader() }
        canCode?.let { change ->
            item(key = "can") {
                CanRow(change, wide, open = openRow == change.after.id) {
                    openRow = if (openRow == change.after.id) null else change.after.id
                }
            }
        }
        sections.forEach { section ->
            sectionItems(
                section = section,
                expanded = q.isNotEmpty() || section.key in expanded,
                wide = wide,
                openRow = openRow,
                onToggle = {
                    expanded = if (section.key in expanded) expanded - section.key else expanded + section.key
                },
                onRowToggle = { id -> openRow = if (openRow == id) null else id }
            )
        }
        if (q.isNotEmpty() && sections.isEmpty() && canCode == null) {
            item(key = "nothing") {
                CmpText(
                    text = stringResource(Strings.string.settings_diff_msg_search_empty, q),
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
        }
    }
}

/** Итог, предупреждение о незаписанных правках, поиск (при > 20 изменений). */
@Composable
private fun DiffSummary(
    diff: SettingsDiff,
    pendingChanges: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CmpText(
            text = if (diff.isEmpty) {
                stringResource(Strings.string.settings_diff_msg_same)
            } else {
                stringResource(Strings.string.settings_diff_msg_summary, diff.total, diff.sectionCount)
            },
            style = CmpTextStyle.BodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (pendingChanges > 0 && !diff.isEmpty) {
            CmpText(
                text = stringResource(Strings.string.settings_diff_msg_pending, pendingChanges),
                style = CmpTextStyle.Body,
                color = colors.onErrorContainer,
                modifier = Modifier.fillMaxWidth().background(colors.errorContainer, RowShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        if (diff.total > SEARCH_FROM) {
            CmpSearchField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(Strings.string.settings_diff_placeholder_search),
                clearContentDescription = stringResource(Strings.string.settings_common_a11y_close),
                modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.DIFF_SEARCH)
            )
        }
    }
}

/** CAN-код — первой выделенной строкой и подсказка про прошивку. */
@Composable
private fun CanRow(change: SettingChange, wide: Boolean, open: Boolean, onToggle: () -> Unit) {
    val colors = cmpThemeColors()
    CmpColumn(modifier = Modifier.padding(horizontal = if (wide) 0.dp else 8.dp)) {
        DiffRow(change, wide, highlighted = true, open = open, onToggle = onToggle)
        CmpRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CmpIcon(
                icon = CmpIcons.Memory,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            CmpText(
                text = stringResource(Strings.string.settings_diff_hint_can),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/** Раскрытые сразу разделы: изменений больше 10 — только первый, иначе все. */
private fun SettingsDiff.initiallyExpanded(): List<Int> =
    if (total > COLLAPSE_FROM) sections.take(1).map { it.key } else sections.map { it.key }

/** Разделы с изменениями, подходящими под запрос (пустой — все). */
private fun LazyListScope.sectionItems(
    section: SectionChanges,
    expanded: Boolean,
    wide: Boolean,
    openRow: Int?,
    onToggle: () -> Unit,
    onRowToggle: (Int) -> Unit
) {
    item(key = "section_${section.key}") { SectionHeader(section, expanded, onToggle) }
    if (expanded) {
        items(section.changes, key = { "row_${it.after.id}" }) { change ->
            DiffRow(change, wide, highlighted = false, open = openRow == change.after.id) {
                onRowToggle(change.after.id)
            }
        }
    }
}

@Composable
private fun SectionHeader(section: SectionChanges, expanded: Boolean, onToggle: () -> Unit) {
    val colors = cmpThemeColors()
    CmpColumn(modifier = Modifier.fillMaxWidth()) {
        CmpDivider(color = colors.outlineVariant)
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClick = onToggle)
                .testTag(SettingsTestTags.diffSection(section.key))
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CmpIcon(
                icon = if (expanded) CmpIcons.KeyboardArrowDown else CmpIcons.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant
            )
            section.section?.number?.takeIf { it.isNotEmpty() }?.let {
                CmpText(text = it, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
            }
            CmpText(
                text = section.section?.name ?: stringResource(Strings.string.settings_diff_label_root),
                style = CmpTextStyle.BodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            CmpCountPill(count = section.changes.size)
        }
    }
}

@Composable
private fun ColumnsHeader() {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            Strings.string.settings_diff_label_col_number to null,
            Strings.string.settings_diff_label_col_setting to NAME_WEIGHT,
            Strings.string.settings_diff_label_col_was to 1f,
            Strings.string.settings_diff_label_col_will to 1f
        ).forEach { (text, weight) ->
            CmpText(
                text = stringResource(text).uppercase(),
                style = CmpTextStyle.Caption,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
                modifier = if (weight == null) Modifier.width(NumberColumn) else Modifier.weight(weight)
            )
        }
    }
}

/** Строка изменения: номер, название, «было → станет» (каналы — «+ функция, − функция»); тап — полные значения. */
@Composable
private fun DiffRow(change: SettingChange, wide: Boolean, highlighted: Boolean, open: Boolean, onToggle: () -> Unit) {
    val colors = cmpThemeColors()
    val setting = change.after
    val title = change.title()
    val parts = channelParts(change)
    val was = valueText(change.before, change.before.value)
    val will = valueText(setting, setting.value)
    val lines = if (open) Int.MAX_VALUE else 1
    val overflow = if (open) TextOverflow.Clip else TextOverflow.Ellipsis
    val content = if (highlighted) colors.onTertiaryContainer else colors.onSurface
    CmpBox(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (highlighted) Modifier.background(colors.tertiaryContainer, RowShape) else Modifier)
            .clickable(onClick = onToggle)
            .testTag(SettingsTestTags.diffRow(setting.id))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (wide) {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpText(
                    text = setting.number,
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.width(NumberColumn)
                )
                CmpText(
                    text = title,
                    style = CmpTextStyle.BodyLarge,
                    color = content,
                    maxLines = lines,
                    overflow = overflow,
                    modifier = Modifier.weight(NAME_WEIGHT)
                )
                if (parts != null) {
                    CmpText(
                        text = parts.joinToString(", "),
                        style = CmpTextStyle.BodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = content,
                        maxLines = lines,
                        overflow = overflow,
                        modifier = Modifier.weight(2f)
                    )
                } else {
                    CmpText(
                        text = was,
                        style = CmpTextStyle.BodyLarge,
                        color = colors.onSurfaceVariant,
                        maxLines = lines,
                        overflow = overflow,
                        modifier = Modifier.weight(1f)
                    )
                    CmpText(
                        text = "→ $will",
                        style = CmpTextStyle.BodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = content,
                        maxLines = lines,
                        overflow = overflow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (setting.number.isNotEmpty()) {
                        CmpText(text = setting.number, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
                    }
                    CmpText(
                        text = title,
                        style = CmpTextStyle.BodyLarge,
                        color = content,
                        maxLines = lines,
                        overflow = overflow,
                        modifier = Modifier.weight(1f)
                    )
                    if (!open) {
                        CmpText(
                            text = parts?.joinToString(", ") ?: "$was → $will",
                            style = CmpTextStyle.BodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = content,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = CompactValueWidth)
                        )
                    }
                }
                if (open) {
                    if (parts != null) {
                        parts.forEach { CmpText(text = it, style = CmpTextStyle.BodyLarge, color = content) }
                    } else {
                        CmpText(
                            text = stringResource(Strings.string.settings_diff_label_was, was),
                            color = colors.onSurfaceVariant
                        )
                        CmpText(
                            text = stringResource(Strings.string.settings_diff_label_will, will),
                            fontWeight = FontWeight.SemiBold,
                            color = content
                        )
                    }
                }
            }
        }
    }
}

/** Канал: смена типа контакта / полярности и функции «+ …» / «− …»; не канал — `null`. */
@Composable
private fun channelParts(change: SettingChange): List<String>? {
    val logics = change.logicsChange() ?: return null
    val parts = mutableListOf<String>()
    val before = change.before.value
    val after = change.after.value
    if (before is SettingValue.Input && after is SettingValue.Input && before.value.type != after.value.type) {
        parts += "${contactText(before.value.type)} → ${contactText(after.value.type)}"
    }
    if (before is SettingValue.Output && after is SettingValue.Output &&
        before.value.polarity != after.value.polarity
    ) {
        parts += "${polarityText(before.value.polarity)} → ${polarityText(after.value.polarity)}"
    }
    logics.added.forEach { parts += "+ ${it.name}" }
    logics.removed.forEach { parts += "− ${it.name}" }
    return parts.ifEmpty { null }
}

@Composable
private fun contactText(type: InputContactType): String = when (type) {
    InputContactType.NORMALLY_OPEN -> stringResource(Strings.string.settings_common_value_contact_no)
    InputContactType.NORMALLY_CLOSED -> stringResource(Strings.string.settings_common_value_contact_nc)
    InputContactType.UNDEFINED -> stringResource(Strings.string.settings_common_value_empty)
}

@Composable
private fun polarityText(polarity: Polarity): String = when (polarity) {
    Polarity.PLUS -> stringResource(Strings.string.settings_common_value_polarity_plus_sign)
    Polarity.MINUS -> stringResource(Strings.string.settings_common_value_polarity_minus_sign)
    Polarity.UNDEFINED -> stringResource(Strings.string.settings_common_value_empty)
}

/** Название строки: у каналов — «IN1 · Концевик дверей». */
private fun SettingChange.title(): String = settingTitle(after.name, after.channelOrNull)

/** Ключ раздела в списке: id раздела, корневые настройки — 0. */
private val SectionChanges.key: Int get() = section?.id ?: 0

private val RowShape = RoundedCornerShape(12.dp)
private val NumberColumn = 64.dp
private val CompactValueWidth = 190.dp
private val WideListMaxHeight = 560.dp
private const val NAME_WEIGHT = 1.3f
private const val COLLAPSE_FROM = 10
private const val SEARCH_FROM = 20
