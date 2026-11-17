package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

@Composable
fun CmpRadioButton(selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = RadioButtonDefaults.colors(
            selectedColor = CmpTheme.colors.brandPrimaryDefault,
            unselectedColor = CmpTheme.colors.borderSecondary,
        ),
    )
}
