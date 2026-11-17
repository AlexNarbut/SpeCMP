package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

@Composable
fun CmpCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = CmpTheme.colors.brandPrimaryDefault,
            uncheckedColor = CmpTheme.colors.borderSecondary,
            checkmarkColor = CmpTheme.colors.staticWhite,
        ),
    )
}

@Composable
fun CmpTriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TriStateCheckbox(
        state = state,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = CmpTheme.colors.brandPrimaryDefault,
            uncheckedColor = CmpTheme.colors.borderSecondary,
            checkmarkColor = CmpTheme.colors.staticWhite,
        ),
    )
}
