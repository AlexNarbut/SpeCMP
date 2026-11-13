package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers

/** Телефон с маской страны. [digits] — цифры с кодом страны без `+` (см. [PhoneNumbers]). */
@Composable
fun CmpPhoneField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    CmpTextField(
        value = digits,
        onValueChange = { text -> onDigitsChange(PhoneNumbers.fromRaw(text)) },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        supportingText = supportingText,
        visualTransformation = PhoneVisualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = imeAction),
        keyboardActions = keyboardActions,
    )
}

internal object PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = PhoneNumbers.format(text.text)
        return TransformedText(AnnotatedString(formatted), PhoneOffsetMapping(formatted))
    }
}

/** Значение — только цифры, в маске они идут в том же порядке; `+` и разделители — лишние символы. */
internal class PhoneOffsetMapping(private val formatted: String) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 1
        var seen = 0
        formatted.forEachIndexed { index, char ->
            if (char.isDigit() && ++seen == offset) return index + 1
        }
        return formatted.length
    }

    override fun transformedToOriginal(offset: Int): Int =
        formatted.take(offset.coerceIn(0, formatted.length)).count(Char::isDigit)
}
