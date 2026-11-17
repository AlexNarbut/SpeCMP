package ru.alarmtrade.specmp.core.ui.view.sheet

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.view.input.clearFocusOnTapOutside

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmpBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.clearFocusOnTapOutside(),
        sheetState = sheetState,
        containerColor = if (containerColor == Color.Unspecified) {
            CmpTheme.colors.backgroundPagePrimary
        } else {
            containerColor
        },
        contentColor = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
        shape = RoundedCornerShape(topStart = CmpRadius.sheet, topEnd = CmpRadius.sheet),
        scrimColor = CmpTheme.colors.backgroundOverlay,
        tonalElevation = 0.dp,
        content = { content() },
    )
}
