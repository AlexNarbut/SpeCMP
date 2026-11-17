package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_a11y_scan
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_btn_find
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_error_empty
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_error_not_digits
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_error_too_long
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_label_query
import ru.alarmtrade.specmp.core.localization.resources.productinfo_search_placeholder_query
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductQueryError
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoTags

private val FieldHeight = 56.dp

@Composable
internal fun SearchBar(
    query: String,
    error: ProductQueryError?,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    onScan: (() -> Unit)?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val focusManager = LocalFocusManager.current
    val search = {
        focusManager.clearFocus()
        onSearch()
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CmpText(
            text = stringResource(Strings.string.productinfo_search_label_query),
            color = if (error != null) colors.error else colors.onSurfaceVariant,
            style = CmpTextStyle.Paragraph2,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f).focusRequester(focusRequester).testTag(ProductInfoTags.QUERY),
                placeholder = stringResource(Strings.string.productinfo_search_placeholder_query),
                isError = error != null,
                singleLine = true,
                trailingIcon = if (query.isNotEmpty() || onScan != null) {
                    { FieldActions(clear = if (query.isNotEmpty()) onClear else null, onScan = onScan) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { search() }),
            )
            CmpButton(
                text = stringResource(Strings.string.productinfo_search_btn_find),
                onClick = search,
                modifier = Modifier.height(FieldHeight).testTag(ProductInfoTags.SEARCH),
            )
        }
        if (error != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpIcon(
                    icon = CmpIcons.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.error,
                )
                CmpText(text = error.text(), color = colors.error, style = CmpTextStyle.Paragraph2)
            }
        }
    }
}

@Composable
private fun FieldActions(clear: (() -> Unit)?, onScan: (() -> Unit)?) {
    val colors = cmpThemeColors()
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (clear != null) {
            CmpIconButton(
                onClick = clear,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.productinfo_search_a11y_clear),
                modifier = Modifier.testTag(ProductInfoTags.CLEAR),
            )
        }
        if (onScan != null) {
            if (clear != null) CmpVerticalDivider(modifier = Modifier.height(28.dp), color = colors.outlineVariant)
            CmpIconButton(
                onClick = onScan,
                icon = CmpIcons.QrCodeScanner,
                contentDescription = stringResource(Strings.string.productinfo_search_a11y_scan),
                modifier = Modifier.testTag(ProductInfoTags.SCAN),
                tint = colors.primary,
            )
        }
    }
}

@Composable
private fun ProductQueryError.text(): String = stringResource(
    when (this) {
        ProductQueryError.EMPTY -> Strings.string.productinfo_search_error_empty
        ProductQueryError.NOT_DIGITS -> Strings.string.productinfo_search_error_not_digits
        ProductQueryError.TOO_LONG -> Strings.string.productinfo_search_error_too_long
    },
)
