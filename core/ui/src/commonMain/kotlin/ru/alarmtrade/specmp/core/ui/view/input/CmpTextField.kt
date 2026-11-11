@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

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
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    style: CmpTextFieldStyle = CmpTextFieldStyle.Outlined,
) {
    val labelComposable: (@Composable () -> Unit)? = label?.let { { CmpText(text = it) } }
    val placeholderComposable: (@Composable () -> Unit)? = placeholder?.let { { CmpText(text = it) } }
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
    val fieldModifier = modifier.keepFocusOnTap()

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
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = resolvedKeyboardOptions,
            keyboardActions = resolvedKeyboardActions,
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
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = resolvedKeyboardOptions,
            keyboardActions = resolvedKeyboardActions,
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
