package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_a11y_submit
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_placeholder_query
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBorder
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow

internal const val SEARCH_FIELD_TAG = "mountassistant_search"
internal const val SEARCH_SUBMIT_TAG = "mountassistant_search_submit"

// Вид CmpSearchField, но через CmpTextField: у CmpSearchField нет onSearch, а Enter/«Поиск» отправляет запрос в ленту.
@Composable
internal fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    focusRequester: FocusRequester,
    horizontalPadding: Dp,
) {
    CmpRow(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
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
            shape = RoundedCornerShape(CmpRadius.search),
        )
        SubmitButton(active = query.isNotBlank(), onSubmit = onSubmit)
    }
}

/** «Найти»: радиус кнопок; без запроса — ступень с рамкой, с запросом — заливка чернилами (Primary). */
@Composable
private fun SubmitButton(active: Boolean, onSubmit: () -> Unit) {
    val colors = CmpTheme.colors
    val description = stringResource(Strings.string.mountassistant_search_a11y_submit)
    val shape = RoundedCornerShape(CmpRadius.buttons)
    CmpBox(
        modifier = Modifier.size(CmpSize.input)
            .cmpPressable(onClick = onSubmit, enabled = active, shape = shape)
            .background(if (active) colors.backgroundSurfacePrimary else colors.backgroundSurfaceHighlight, shape)
            .cmpBorder(
                CmpBorderWidth.inputs,
                if (active) colors.backgroundSurfacePrimary else colors.borderLight,
                shape,
            )
            .semantics { contentDescription = description }
            .testTag(SEARCH_SUBMIT_TAG),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(
            icon = CmpIcons.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(CmpSize.iconM),
            tint = if (active) colors.foregroundOnLightPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
        )
    }
}
