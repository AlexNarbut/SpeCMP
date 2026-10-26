package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

/**
 * Поле короткого цифрового кода по ячейкам (CAN-код — 4 цифры): цифровая клавиатура, ввод отбрасывает не-цифры и
 * лишнее сверх [length]. Рамка активной ячейки — primary, [isError] — все ячейки error. «✕» очищает поле
 * ([clearContentDescription]). [label] — подпись поля для доступности.
 */
@Composable
fun CmpCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    clearContentDescription: String,
    modifier: Modifier = Modifier,
    length: Int = 4,
    isError: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(length)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            interactionSource = interaction,
            modifier = Modifier
                .focusRequester(focusRequester)
                .semantics { contentDescription = label },
            decorationBox = { _ ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(length) { index ->
                        val active = focused && index == value.length.coerceAtMost(length - 1)
                        val borderColor = when {
                            isError -> colors.error
                            active -> colors.primary
                            else -> colors.outline
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 58.dp, height = 60.dp)
                                .border(
                                    width = if (isError || active) 2.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value.getOrNull(index)?.toString().orEmpty(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }
        )
        if (value.isNotEmpty()) {
            CmpIconButton(
                onClick = {
                    onValueChange("")
                    focusRequester.requestFocus()
                },
                icon = CmpIcons.Close,
                contentDescription = clearContentDescription
            )
        }
    }
}
