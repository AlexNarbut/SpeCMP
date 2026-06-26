package ru.alarmtrade.specmp.core.ui.view.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon

@Composable
fun CmpDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    shape: Shape? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        title = title,
        text = text,
        icon = icon?.let { { CmpIcon(imageVector = it, contentDescription = null) } },
        shape = shape ?: androidx.compose.material3.AlertDialogDefaults.shape,
    )
}
