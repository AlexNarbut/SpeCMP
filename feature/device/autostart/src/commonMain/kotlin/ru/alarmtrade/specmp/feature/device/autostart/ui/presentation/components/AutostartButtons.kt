package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_next
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepNext

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

/**
 * Итог шага в мастере быстрого монтажа: причина, по которой мастер не пустил дальше (если есть), и «Далее»
 * ([AutostartStepNext.busy] — спиннер, повторный тап невозможен).
 */
@Composable
internal fun StepNextButton(next: AutostartStepNext, onClick: () -> Unit, modifier: Modifier = Modifier) {
    next.error?.let { error ->
        InfoBanner(icon = CmpIcons.ErrorOutline, tone = BannerTone.Error, text = error)
    }
    CmpButton(
        onClick = onClick,
        enabled = !next.busy,
        modifier = modifier.fillMaxWidth().height(AutostartButtonHeight)
    ) {
        if (next.busy) {
            CmpCircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
        } else {
            CmpText(text = stringResource(Strings.string.autostart_common_btn_next))
        }
    }
}
