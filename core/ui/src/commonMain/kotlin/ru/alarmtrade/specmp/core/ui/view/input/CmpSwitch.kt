package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

@Composable
fun CmpSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    thumbContent: (@Composable () -> Unit)? = null,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        thumbContent = thumbContent,
        colors = SwitchDefaults.colors(
            checkedThumbColor = CmpTheme.colors.staticWhite,
            checkedTrackColor = CmpTheme.colors.brandPrimaryDefault,
            checkedBorderColor = CmpTheme.colors.brandPrimaryDefault,
            uncheckedThumbColor = CmpTheme.colors.backgroundSurfaceSecondary,
            uncheckedTrackColor = CmpTheme.colors.backgroundSurfaceHighlight,
            uncheckedBorderColor = CmpTheme.colors.borderLight,
        ),
    )
}
