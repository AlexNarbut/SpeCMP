package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton

/** Главная кнопка экрана (во всю ширину колонки кнопок, высота макета). */
@Composable
internal fun AutostartPrimaryButton(text: StringResource, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(
        text = stringResource(text),
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(AutostartButtonHeight)
    )
}

/** Вторая кнопка экрана (обводкой). */
@Composable
internal fun AutostartSecondaryButton(
    text: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    CmpOutlinedButton(
        text = stringResource(text),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(AutostartButtonHeight)
    )
}
