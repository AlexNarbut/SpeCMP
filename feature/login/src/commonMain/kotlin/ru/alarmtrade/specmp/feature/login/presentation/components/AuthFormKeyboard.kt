package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun rememberNextFieldKeyboardActions(): KeyboardActions {
    val focusManager = LocalFocusManager.current
    return remember(focusManager) {
        KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
    }
}

/** Return последнего поля — то же, что кнопка отправки: при недоступной кнопке ([enabled] = false) ничего не делает. */
@Composable
fun submitKeyboardActions(enabled: Boolean, onSubmit: () -> Unit): KeyboardActions {
    val focusManager = LocalFocusManager.current
    val submit: KeyboardActionScope.() -> Unit = {
        if (enabled) {
            focusManager.clearFocus()
            onSubmit()
        }
    }
    return KeyboardActions(onDone = submit, onGo = submit)
}

/** Хвост формы (чекбокс, кнопка отправки), который держится над клавиатурой, пока в фокусе последнее поле. */
@Stable
class AuthFormTail internal constructor() {
    internal val requester = BringIntoViewRequester()
    internal var isTriggerFocused by mutableStateOf(false)
    internal var trigger: LayoutCoordinates? = null
    internal var tail: LayoutCoordinates? = null

    // Прямоугольник от поля до хвоста: в него вложен запрос курсора поля, и очередь bringIntoView их не взаимоотменяет.
    internal suspend fun reveal() {
        val tailCoordinates = tail?.takeIf { it.isAttached } ?: return
        val triggerTop = trigger?.takeIf { it.isAttached }
            ?.let { tailCoordinates.localBoundingBoxOf(it, clipBounds = false).top }
            ?: 0f
        val size = tailCoordinates.size
        requester.bringIntoView(Rect(0f, minOf(triggerTop, 0f), size.width.toFloat(), size.height.toFloat()))
    }
}

// Вьюпорт формы сжимается вместе с IME (imePadding в AuthBody), поэтому хвост перепоказывается на каждом шаге высоты.
@Composable
fun rememberAuthFormTail(): AuthFormTail {
    val tail = remember { AuthFormTail() }
    val ime = WindowInsets.ime
    val density = LocalDensity.current
    LaunchedEffect(tail, ime, density) {
        snapshotFlow { tail.isTriggerFocused to ime.getBottom(density) }
            .collectLatest { (isFocused, _) ->
                // Отдельная корутина: прерванный жестом или новым bringIntoView показ не гасит сам эффект.
                if (isFocused) coroutineScope { launch { tail.reveal() } }
            }
    }
    return tail
}

fun Modifier.revealsFormTail(tail: AuthFormTail): Modifier = this
    .onPlaced { tail.trigger = it }
    .onFocusChanged { tail.isTriggerFocused = it.isFocused }

fun Modifier.formTail(tail: AuthFormTail): Modifier = this
    .bringIntoViewRequester(tail.requester)
    .onPlaced { tail.tail = it }
