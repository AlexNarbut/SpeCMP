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

/**
 * Выбор одного варианта из нескольких коротких (2–3) сегментами одной кнопки. Сегменты делят ширину поровну,
 * выбранный отмечен галочкой, длинная подпись обрезается. Высота по умолчанию — 48 dp (зона касания).
 */
@Composable
fun CmpSegmentedButton(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 48.dp
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = minHeight),
                label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}
