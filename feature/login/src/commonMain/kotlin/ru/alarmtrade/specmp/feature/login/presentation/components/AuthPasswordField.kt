package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_common_a11y_hide_password
import ru.alarmtrade.specmp.core.localization.resources.login_common_a11y_show_password
import ru.alarmtrade.specmp.core.localization.resources.login_common_label_password
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    CmpTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = stringResource(Strings.string.login_common_label_password),
        singleLine = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        leadingIcon = {
            CmpIcon(
                icon = CmpIcons.Lock,
                contentDescription = null
            )
        },
        trailingIcon = {
            CmpIconButton(
                onClick = onPasswordVisibilityChange,
                icon = if (isPasswordVisible) CmpIcons.VisibilityOff else CmpIcons.Visibility,
                contentDescription = if (isPasswordVisible) {
                    stringResource(Strings.string.login_common_a11y_hide_password)
                } else {
                    stringResource(Strings.string.login_common_a11y_show_password)
                },
                enabled = enabled
            )
        }
    )
}
