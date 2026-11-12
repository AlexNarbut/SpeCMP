package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_a11y_submit
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_placeholder_query
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface

internal const val SEARCH_FIELD_TAG = "mountassistant_search"
internal const val SEARCH_SUBMIT_TAG = "mountassistant_search_submit"

@Composable
internal fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    focusRequester: FocusRequester,
    horizontalPadding: Dp,
) {
    val colors = cmpThemeColors()
    val active = query.isNotBlank()
    CmpRow(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).focusRequester(focusRequester).testTag(SEARCH_FIELD_TAG),
            placeholder = stringResource(Strings.string.mountassistant_search_placeholder_query),
            singleLine = true,
            leadingIcon = { CmpIcon(icon = CmpIcons.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    CmpIconButton(
                        onClick = { onQueryChange("") },
                        icon = CmpIcons.Close,
                        contentDescription = stringResource(Strings.string.mountassistant_search_a11y_clear),
                    )
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        )
        val description = stringResource(Strings.string.mountassistant_search_a11y_submit)
        val shape = RoundedCornerShape(16.dp)
        CmpSurface(
            modifier = Modifier.size(56.dp).clip(shape)
                .clickable(enabled = active, onClick = onSubmit)
                .semantics { contentDescription = description }
                .testTag(SEARCH_SUBMIT_TAG),
            shape = shape,
            color = if (active) colors.primary else colors.surfaceContainerHighest,
            contentColor = if (active) colors.onPrimary else colors.onSurfaceVariant,
        ) {
            CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CmpIcon(icon = CmpIcons.ArrowForward, contentDescription = null, modifier = Modifier.size(26.dp))
            }
        }
    }
}
