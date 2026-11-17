package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_chat
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_btn_next
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepNext

@Composable
internal fun AutostartPrimaryButton(
    text: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
) {
    CmpButton(text = stringResource(text), onClick = onClick, icon = icon, fullWidth = true, modifier = modifier)
}

@Composable
internal fun AutostartSecondaryButton(
    text: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    enabled: Boolean = true,
) {
    CmpButton(
        text = stringResource(text),
        onClick = onClick,
        type = CmpButtonType.Secondary,
        icon = icon,
        enabled = enabled,
        fullWidth = true,
        modifier = modifier,
    )
}

@Composable
internal fun ColumnScope.AutostartChatButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(
        text = stringResource(Strings.string.autostart_common_btn_chat),
        onClick = onClick,
        type = CmpButtonType.Tertiary,
        size = CmpButtonSize.S,
        icon = CmpIcons.SupportAgent,
        modifier = modifier.align(Alignment.CenterHorizontally),
    )
}

@Composable
internal fun StepNextButton(next: AutostartStepNext, onClick: () -> Unit, modifier: Modifier = Modifier) {
    next.error?.let { error -> CmpBanner(text = error, tone = CmpBannerTone.Error) }
    CmpButton(
        text = stringResource(Strings.string.autostart_common_btn_next),
        onClick = onClick,
        loading = next.busy,
        fullWidth = true,
        modifier = modifier,
    )
}
