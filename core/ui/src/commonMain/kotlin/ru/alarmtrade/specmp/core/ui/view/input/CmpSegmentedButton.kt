package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.core.ui.view.text.toTextStyle

@Composable
fun CmpSegmentedButton(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 48.dp,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = minHeight),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = CmpTheme.colors.backgroundSurfacePrimary,
                    activeContentColor = CmpTheme.colors.foregroundOnLightPrimaryDefault,
                    activeBorderColor = CmpTheme.colors.backgroundSurfacePrimary,
                    inactiveContainerColor = CmpTheme.colors.backgroundSurfaceUltraHighlight,
                    inactiveContentColor = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    inactiveBorderColor = CmpTheme.colors.borderLight,
                ),
                label = {
                    Text(
                        text = label,
                        style = CmpTextStyle.Headline3.toTextStyle(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}
