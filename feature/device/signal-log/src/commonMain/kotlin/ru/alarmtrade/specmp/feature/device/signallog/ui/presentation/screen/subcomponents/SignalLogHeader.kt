package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.signallog_log_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.signallog_log_a11y_search
import ru.alarmtrade.specmp.core.localization.resources.signallog_log_btn_search
import ru.alarmtrade.specmp.core.localization.resources.signallog_log_format_subtitle
import ru.alarmtrade.specmp.core.localization.resources.signallog_log_title
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_a11y_next
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_a11y_prev
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_btn_only
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_format_position
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_placeholder_query
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogActions
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogLayout
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogSearch
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogState

internal const val SIGNAL_LOG_SEARCH_TAG = "signallog_search"
internal const val SIGNAL_LOG_SEARCH_FIELD_TAG = "signallog_search_field"

private val RowMinHeight = 64.dp

@Composable
internal fun SignalLogHeader(state: SignalLogState, actions: SignalLogActions, layout: SignalLogLayout) {
    CmpColumn(modifier = Modifier.fillMaxWidth()) {
        val search = state.search
        if (search == null) {
            MainBar(state = state, actions = actions, layout = layout)
        } else {
            SearchBar(search = search, actions = actions)
        }
        CmpDivider(color = CmpTheme.colors.borderHighlight)
    }
}

@Composable
private fun MainBar(state: SignalLogState, actions: SignalLogActions, layout: SignalLogLayout) {
    val name = state.deviceName
    val systemId = state.systemId
    val subtitle = when {
        name != null && systemId != null ->
            stringResource(Strings.string.signallog_log_format_subtitle, name, systemId.toString())

        else -> name
    }
    CmpTopAppBar(
        title = stringResource(Strings.string.signallog_log_title),
        subtitle = subtitle,
        large = layout.sidePanel,
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.signallog_log_a11y_back),
            )
        },
        actions = {
            if (layout.compact) {
                CmpIconButton(
                    onClick = actions.onSearchOpen,
                    icon = CmpIcons.Search,
                    contentDescription = stringResource(Strings.string.signallog_log_a11y_search),
                    enabled = state.hasLines,
                    modifier = Modifier.testTag(SIGNAL_LOG_SEARCH_TAG),
                )
            } else {
                CmpButton(
                    text = stringResource(Strings.string.signallog_log_btn_search),
                    onClick = actions.onSearchOpen,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                    icon = CmpIcons.Search,
                    enabled = state.hasLines,
                    uppercase = false,
                    modifier = Modifier.testTag(SIGNAL_LOG_SEARCH_TAG),
                )
            }
        },
    )
}

@Composable
private fun SearchBar(search: SignalLogSearch, actions: SignalLogActions) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(focus) { focus.requestFocus() }
    val colors = CmpTheme.colors
    val current = search.current
    val hasQuery = search.query.isNotBlank()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .padding(horizontal = CmpSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIconButton(
            onClick = actions.onSearchClose,
            icon = CmpIcons.ArrowBack,
            contentDescription = stringResource(Strings.string.signallog_search_a11y_close),
        )
        CmpSearchField(
            value = search.query,
            onValueChange = actions.onSearchQuery,
            placeholder = stringResource(Strings.string.signallog_search_placeholder_query),
            clearContentDescription = stringResource(Strings.string.signallog_search_a11y_clear),
            leadingIcon = false,
            valueStyle = CmpTextStyle.Headline3,
            modifier = Modifier.weight(1f).focusRequester(focus).testTag(SIGNAL_LOG_SEARCH_FIELD_TAG),
        )
        if (hasQuery) {
            val found = search.matches.isNotEmpty()
            CmpText(
                text = stringResource(
                    Strings.string.signallog_search_format_position,
                    if (found) current + 1 else 0,
                    search.matches.size,
                ),
                style = CmpTextStyle.Headline3,
                color = if (found) colors.foregroundOnDarkPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
                maxLines = 1,
                modifier = Modifier.padding(start = CmpSpacing.space8),
            )
        }
        CmpIconButton(
            onClick = { actions.onSearchStep(true) },
            icon = CmpIcons.KeyboardArrowUp,
            contentDescription = stringResource(Strings.string.signallog_search_a11y_prev),
            enabled = current > 0,
        )
        CmpIconButton(
            onClick = { actions.onSearchStep(false) },
            icon = CmpIcons.KeyboardArrowDown,
            contentDescription = stringResource(Strings.string.signallog_search_a11y_next),
            enabled = current >= 0 && current < search.matches.lastIndex,
        )
        CmpIconButton(
            onClick = actions.onOnlyMatchesToggle,
            icon = CmpIcons.FilterList,
            contentDescription = stringResource(Strings.string.signallog_search_btn_only),
            enabled = hasQuery,
            selected = search.onlyMatches,
        )
    }
}
