@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.core.ui.view.text.toTextStyle

enum class CmpTextFieldStyle { Filled, Outlined }

@Composable
fun CmpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    style: CmpTextFieldStyle = CmpTextFieldStyle.Outlined,
    shape: Shape = RoundedCornerShape(CmpRadius.inputs),
    valueStyle: CmpTextStyle = CmpTextStyle.Headline2,
    containerColor: Color = Color.Unspecified,
) {
    val labelComposable: (@Composable () -> Unit)? = label?.let { { Text(text = it) } }
    val placeholderComposable: (@Composable () -> Unit)? = placeholder?.let {
        { Text(text = it, maxLines = if (singleLine) 1 else Int.MAX_VALUE, overflow = TextOverflow.Ellipsis) }
    }
    val resolvedKeyboardOptions = if (singleLine && keyboardOptions.imeAction == ImeAction.Default) {
        keyboardOptions.copy(imeAction = ImeAction.Done)
    } else {
        keyboardOptions
    }
    val dismissKeyboardActions = rememberDismissKeyboardActions()
    val resolvedKeyboardActions = if (keyboardActions === KeyboardActions.Default) {
        dismissKeyboardActions
    } else {
        keyboardActions
    }
    val fieldModifier = modifier
        .then(if (singleLine) Modifier.heightIn(min = CmpSize.input) else Modifier)
        .keepFocusOnTap()
    val valueTextStyle = valueStyle.toTextStyle()
    val fieldColors = cmpTextFieldColors(containerColor)

    when (style) {
        CmpTextFieldStyle.Outlined -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier,
            label = labelComposable,
            placeholder = placeholderComposable,
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = resolvedKeyboardOptions,
            keyboardActions = resolvedKeyboardActions,
            textStyle = valueTextStyle,
            shape = shape,
            colors = fieldColors,
        )

        CmpTextFieldStyle.Filled -> TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier,
            label = labelComposable,
            placeholder = placeholderComposable,
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = resolvedKeyboardOptions,
            keyboardActions = resolvedKeyboardActions,
            textStyle = valueTextStyle,
            shape = shape,
            colors = fieldColors,
        )
    }
}

@Composable
private fun rememberDismissKeyboardActions(): KeyboardActions {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember(focusManager, keyboardController) {
        val dismiss: KeyboardActionScope.() -> Unit = {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
        KeyboardActions(onDone = dismiss, onGo = dismiss, onSearch = dismiss)
    }
}

/** Цвета полей kit InputMain: ступень чернил, рамка `borderLight` → `borderFocus` в фокусе, ошибка — `errorDefault`. */
@Composable
internal fun cmpTextFieldColors(containerColor: Color = Color.Unspecified): TextFieldColors {
    val c = CmpTheme.colors
    val custom = containerColor != Color.Unspecified
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = c.foregroundOnDarkPrimaryDefault,
        unfocusedTextColor = c.foregroundOnDarkPrimaryDefault,
        disabledTextColor = c.foregroundOnDarkPrimaryDisabled,
        errorTextColor = c.foregroundOnDarkPrimaryDefault,
        focusedContainerColor = if (custom) containerColor else c.backgroundSurfaceHighlight,
        unfocusedContainerColor = if (custom) containerColor else c.backgroundSurfaceUltraHighlight,
        disabledContainerColor = c.backgroundSurfaceUltraHighlight,
        errorContainerColor = c.backgroundSurfaceUltraHighlight,
        cursorColor = c.brandPrimaryDefault,
        errorCursorColor = c.errorDefault,
        focusedBorderColor = c.borderFocus,
        unfocusedBorderColor = c.borderLight,
        disabledBorderColor = c.borderUltraHighlight,
        errorBorderColor = c.errorDefault,
        focusedLeadingIconColor = c.foregroundOnDarkSecondaryDefault,
        unfocusedLeadingIconColor = c.foregroundOnDarkSecondaryDefault,
        focusedTrailingIconColor = c.foregroundOnDarkSecondaryDefault,
        unfocusedTrailingIconColor = c.foregroundOnDarkSecondaryDefault,
        errorTrailingIconColor = c.errorDefault,
        focusedLabelColor = c.borderFocus,
        unfocusedLabelColor = c.foregroundOnDarkSecondaryDefault,
        errorLabelColor = c.errorDefault,
        focusedPlaceholderColor = c.foregroundOnDarkTertiary,
        unfocusedPlaceholderColor = c.foregroundOnDarkTertiary,
        focusedSupportingTextColor = c.foregroundOnDarkSecondaryDefault,
        unfocusedSupportingTextColor = c.foregroundOnDarkSecondaryDefault,
        errorSupportingTextColor = c.errorDefault,
    )
}
