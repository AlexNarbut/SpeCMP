package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/** Код/PIN по ячейкам (kit InputDigit): 56×72, цифра Title1, [secure] — точки; не-цифры и лишнее отбрасываются. */
@Composable
fun CmpCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    clearContentDescription: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    length: Int = 4,
    isError: Boolean = false,
    secure: Boolean = false,
    enabled: Boolean = true,
    showClear: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val colors = CmpTheme.colors
    Row(
        modifier = modifier.keepFocusOnTap().alpha(if (enabled) 1f else DISABLED_ALPHA),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(length)) },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = keyboardActions,
            interactionSource = interaction,
            modifier = fieldModifier
                .focusRequester(focusRequester)
                .semantics { contentDescription = label },
            decorationBox = { _ ->
                Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    repeat(length) { index ->
                        val active = focused && index == value.length.coerceAtMost(length - 1)
                        val borderColor = when {
                            isError -> colors.errorDefault
                            active -> colors.borderFocus
                            else -> colors.borderLight
                        }
                        Box(
                            modifier = Modifier
                                .size(width = DigitWidth, height = DigitHeight)
                                .background(
                                    color = if (active) {
                                        colors.backgroundSurfaceHighlight
                                    } else {
                                        colors.backgroundSurfaceUltraHighlight
                                    },
                                    shape = RoundedCornerShape(CmpRadius.inputs),
                                )
                                .border(
                                    width = if (isError || active) ActiveBorder else CmpBorderWidth.inputs,
                                    color = borderColor,
                                    shape = RoundedCornerShape(CmpRadius.inputs),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            val digit = value.getOrNull(index)?.toString().orEmpty()
                            CmpText(
                                text = if (secure && digit.isNotEmpty()) SECURE_DOT else digit,
                                style = CmpTextStyle.Title1,
                                color = colors.foregroundOnDarkPrimaryDefault,
                            )
                        }
                    }
                }
            },
        )
        if (showClear && enabled && value.isNotEmpty()) {
            CmpIconButton(
                onClick = {
                    onValueChange("")
                    focusRequester.requestFocus()
                },
                icon = CmpIcons.Close,
                contentDescription = clearContentDescription,
            )
        }
    }
}

private val DigitWidth = 56.dp
private val DigitHeight = 72.dp
private val ActiveBorder = 2.dp
private const val SECURE_DOT = "•"
private const val DISABLED_ALPHA = 0.4f
