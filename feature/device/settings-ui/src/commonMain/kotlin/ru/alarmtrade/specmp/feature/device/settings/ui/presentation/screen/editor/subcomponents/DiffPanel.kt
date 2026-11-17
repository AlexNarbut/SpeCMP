package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
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

@Composable
internal fun DiffPanel(state: DiffState, wide: Boolean, actions: TransferActions) {
    val title = stringResource(Strings.string.settings_diff_title)
    val subtitle = stringResource(
        Strings.string.settings_diff_subtitle,
        state.title.ifEmpty { stringResource(Strings.string.settings_source_title_defaults) },
    )
    if (wide) {
        CmpDialog(
            onDismissRequest = actions.onDiffDismiss,
            width = CmpDialogWidth.Wide,
            title = {
                CmpColumn {
                    DialogTitle(text = title, onClose = actions.onDiffDismiss)
                    CmpText(
                        text = subtitle,
                        style = CmpTextStyle.Paragraph2,
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            },
            text = {
                CmpColumn(modifier = Modifier.fillMaxWidth().heightIn(max = WideListMaxHeight)) {
                    DiffBody(state, wide = true)
                }
            },
            confirmButton = { DiffButtons(state, actions) },
        )
    } else {
        CmpFullScreenDialog(onDismissRequest = actions.onDiffDismiss) {
            CmpColumn(modifier = Modifier.fillMaxSize().background(CmpTheme.colors.backgroundPagePrimary)) {
                CmpTopAppBar(
                    title = title,
                    subtitle = subtitle,
                    navigationIcon = {
                        CmpIconButton(
                            onClick = actions.onDiffDismiss,
                            icon = CmpIcons.Close,
                            contentDescription = stringResource(Strings.string.settings_common_a11y_close),
                        )
                    },
                )
                CmpColumn(modifier = Modifier.weight(1f).fillMaxWidth()) { DiffBody(state, wide = false) }
                CmpDivider()
                CmpRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space12),
                    horizontalArrangement = Arrangement.End,
                ) { DiffButtons(state, actions) }
            }
        }
    }
}

@Composable
private fun DiffButtons(state: DiffState, actions: TransferActions) {
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state) {
            is DiffState.Busy -> CmpButton(
                text = stringResource(Strings.string.settings_common_btn_cancel),
                onClick = actions.onDiffDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )

            is DiffState.Error -> {
                CmpButton(
                    text = stringResource(Strings.string.settings_common_btn_close),
                    onClick = actions.onDiffDismiss,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
                CmpButton(
                    text = stringResource(Strings.string.settings_common_btn_retry),
                    onClick = actions.onDiffRetry,
                )
            }

            is DiffState.Ready -> if (state.diff.isEmpty) {
                CmpButton(
                    text = stringResource(Strings.string.settings_common_btn_close),
                    onClick = actions.onDiffDismiss,
                )
            } else {
                CmpButton(
                    text = stringResource(Strings.string.settings_common_btn_cancel),
                    onClick = actions.onDiffDismiss,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
                CmpButton(
                    text = stringResource(Strings.string.settings_diff_btn_load),
                    onClick = actions.onDiffConfirm,
                    modifier = Modifier.testTag(SettingsTestTags.DIFF_LOAD),
                )
            }
        }
    }
}

@Composable
private fun DiffBody(state: DiffState, wide: Boolean) {
    val colors = CmpTheme.colors
    when (state) {
        is DiffState.Busy -> CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpCircularProgressIndicator()
            CmpText(
                text = stringResource(Strings.string.settings_diff_loading_busy),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }

        is DiffState.Error -> CmpStateBox(
            icon = CmpIcons.ErrorOutline,
            title = stringResource(Strings.string.settings_diff_error),
            tone = CmpStateTone.Error,
            compact = true,
        )

        is DiffState.Ready -> if (state.diff.isEmpty) {
            CmpStateBox(
                icon = CmpIcons.CheckCircle,
                title = stringResource(Strings.string.settings_diff_msg_same),
                compact = true,
            )
        } else {
            DiffList(state.diff, state.pendingChanges, wide)
        }
    }
}

@Composable
private fun DiffList(diff: SettingsDiff, pendingChanges: Int, wide: Boolean) {
    val colors = CmpTheme.colors
    var expanded by rememberSaveable(diff) { mutableStateOf(diff.initiallyExpanded()) }
    var openRow by remember(diff) { mutableStateOf<Int?>(null) }
    var query by rememberSaveable(diff) { mutableStateOf("") }
    // Фильтр — доменное правило поиска; пересчёт только при смене диффа или запроса, а не на каждую рекомпозицию.
    val q = query.trim()
    val shown = remember(diff, q) { diff.filter(q) }
    val sections = shown.sections
    val canCode = shown.canCode
    val sidePadding = if (wide) CmpSpacing.space8 else CmpSpacing.space16
    LazyColumn(modifier = Modifier.fillMaxWidth().hideKeyboardOnScroll()) {
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
                onRowToggle = { id -> openRow = if (openRow == id) null else id },
            )
        }
        if (q.isNotEmpty() && sections.isEmpty() && canCode == null) {
            item(key = "nothing") {
                CmpText(
                    text = stringResource(Strings.string.settings_diff_msg_search_empty, q),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space24),
                )
            }
        }
    }
}

@Composable
private fun DiffSummary(
    diff: SettingsDiff,
    pendingChanges: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = modifier.fillMaxWidth().padding(vertical = CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpText(
            text = if (diff.isEmpty) {
                stringResource(Strings.string.settings_diff_msg_same)
            } else {
                stringResource(Strings.string.settings_diff_msg_summary, diff.total, diff.sectionCount)
            },
            style = CmpTextStyle.Headline2,
        )
        if (pendingChanges > 0 && !diff.isEmpty) {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.warningHighlight, CardShape)
                    .border(CmpBorderWidth.listItem, colors.borderHighlight, CardShape)
                    .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.warningDefault)
                CmpText(
                    text = stringResource(Strings.string.settings_diff_msg_pending, pendingChanges),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkPrimaryDefault,
                )
            }
        }
        if (diff.total > SEARCH_FROM) {
            CmpSearchField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(Strings.string.settings_diff_placeholder_search),
                clearContentDescription = stringResource(Strings.string.settings_common_a11y_close),
                modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.DIFF_SEARCH),
            )
        }
    }
}

@Composable
private fun CanRow(change: SettingChange, wide: Boolean, open: Boolean, onToggle: () -> Unit) {
    val colors = CmpTheme.colors
    CmpColumn(modifier = Modifier.padding(horizontal = if (wide) 0.dp else CmpSpacing.space8)) {
        DiffRow(change, wide, highlighted = true, open = open, onToggle = onToggle)
        CmpRow(
            modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = CmpIcons.Memory,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.size(CmpSize.iconS),
            )
            CmpText(
                text = stringResource(Strings.string.settings_diff_hint_can),
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

private fun SettingsDiff.initiallyExpanded(): List<Int> =
    if (total > COLLAPSE_FROM) sections.take(1).map { it.key } else sections.map { it.key }

private fun LazyListScope.sectionItems(
    section: SectionChanges,
    expanded: Boolean,
    wide: Boolean,
    openRow: Int?,
    onToggle: () -> Unit,
    onRowToggle: (Int) -> Unit,
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
    val colors = CmpTheme.colors
    CmpColumn(modifier = Modifier.fillMaxWidth()) {
        CmpDivider()
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CmpSize.input)
                .clickable(onClick = onToggle)
                .testTag(SettingsTestTags.diffSection(section.key))
                .padding(start = CmpSpacing.space8, end = CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpIcon(
                icon = if (expanded) CmpIcons.KeyboardArrowDown else CmpIcons.ChevronRight,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
            )
            section.section?.number?.takeIf { it.isNotEmpty() }?.let {
                CmpText(text = it, style = CmpTextStyle.Additional0, color = colors.foregroundOnDarkSecondaryDefault)
            }
            CmpText(
                text = section.section?.name ?: stringResource(Strings.string.settings_diff_label_root),
                style = CmpTextStyle.Headline3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            CmpCountPill(count = section.changes.size)
        }
    }
}

@Composable
private fun ColumnsHeader() {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        listOf(
            Strings.string.settings_diff_label_col_number to null,
            Strings.string.settings_diff_label_col_setting to NAME_WEIGHT,
            Strings.string.settings_diff_label_col_was to 1f,
            Strings.string.settings_diff_label_col_will to 1f,
        ).forEach { (text, weight) ->
            CmpText(
                text = stringResource(text),
                style = CmpTextStyle.Additional1,
                uppercase = true,
                color = colors.foregroundOnDarkSecondaryDefault,
                modifier = if (weight == null) Modifier.width(NumberColumn) else Modifier.weight(weight),
            )
        }
    }
}

@Composable
private fun DiffRow(change: SettingChange, wide: Boolean, highlighted: Boolean, open: Boolean, onToggle: () -> Unit) {
    val colors = CmpTheme.colors
    val setting = change.after
    val title = change.title()
    val parts = channelParts(change)
    val was = valueText(change.before, change.before.value)
    val will = valueText(setting, setting.value)
    val lines = if (open) Int.MAX_VALUE else 1
    val overflow = if (open) TextOverflow.Clip else TextOverflow.Ellipsis
    val content = colors.foregroundOnDarkPrimaryDefault
    CmpBox(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.buttonM)
            .then(if (highlighted) Modifier.background(colors.warningHighlight, RowShape) else Modifier)
            .clickable(onClick = onToggle)
            .testTag(SettingsTestTags.diffRow(setting.id))
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (wide) {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = setting.number,
                    style = CmpTextStyle.Additional0,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.width(NumberColumn),
                )
                CmpText(
                    text = title,
                    style = CmpTextStyle.Paragraph2,
                    color = content,
                    maxLines = lines,
                    overflow = overflow,
                    modifier = Modifier.weight(NAME_WEIGHT),
                )
                if (parts != null) {
                    CmpText(
                        text = parts.joinToString(", "),
                        style = CmpTextStyle.Headline3,
                        color = content,
                        maxLines = lines,
                        overflow = overflow,
                        modifier = Modifier.weight(2f),
                    )
                } else {
                    WasWillCells(was, will, lines, overflow)
                }
            }
        } else {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (setting.number.isNotEmpty()) {
                        CmpText(
                            text = setting.number,
                            style = CmpTextStyle.Additional0,
                            color = colors.foregroundOnDarkSecondaryDefault,
                        )
                    }
                    CmpText(
                        text = title,
                        style = CmpTextStyle.Paragraph2,
                        color = content,
                        maxLines = lines,
                        overflow = overflow,
                        modifier = Modifier.weight(1f),
                    )
                    if (!open) {
                        CmpText(
                            text = parts?.joinToString(", ") ?: "$was → $will",
                            style = CmpTextStyle.Headline3,
                            color = content,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = CompactValueWidth),
                        )
                    }
                }
                if (open) {
                    if (parts != null) {
                        parts.forEach { CmpText(text = it, style = CmpTextStyle.Headline3, color = content) }
                    } else {
                        CmpText(
                            text = stringResource(Strings.string.settings_diff_label_was, was),
                            style = CmpTextStyle.Paragraph2,
                            color = colors.foregroundOnDarkSecondaryDefault,
                        )
                        CmpText(
                            text = stringResource(Strings.string.settings_diff_label_will, will),
                            style = CmpTextStyle.Headline3,
                            color = content,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.WasWillCells(was: String, will: String, lines: Int, overflow: TextOverflow) {
    val colors = CmpTheme.colors
    CmpText(
        text = was,
        style = CmpTextStyle.Paragraph2,
        color = colors.foregroundOnDarkSecondaryDefault,
        maxLines = lines,
        overflow = overflow,
        modifier = Modifier.weight(1f),
    )
    CmpRow(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(
            icon = CmpIcons.ArrowForward,
            contentDescription = null,
            tint = colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.size(CmpSize.iconS),
        )
        CmpText(
            text = will,
            style = CmpTextStyle.Headline3,
            color = colors.foregroundOnDarkPrimaryDefault,
            maxLines = lines,
            overflow = overflow,
        )
    }
}

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

private fun SettingChange.title(): String = settingTitle(after.name, after.channelOrNull)

private val SectionChanges.key: Int get() = section?.id ?: 0

private val RowShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.cardSmall)
private val CardShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.cardMedium)
private val NumberColumn = 64.dp
private val CompactValueWidth = 190.dp
private val WideListMaxHeight = 560.dp
private const val NAME_WEIGHT = 1.3f
private const val COLLAPSE_FROM = 10
private const val SEARCH_FROM = 20
