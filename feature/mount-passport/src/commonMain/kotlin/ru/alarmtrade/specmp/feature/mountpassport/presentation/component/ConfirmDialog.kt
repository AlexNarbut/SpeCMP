package ru.alarmtrade.specmp.feature.mountpassport.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_delete
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/** Подтверждение удаления: «Удалить» — опасное действие без фона, «Отмена» — закрыть. */
@Composable
internal fun DeleteConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmTag: String,
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        icon = CmpIcons.Delete.imageVector(),
        title = { CmpText(text = title, style = CmpTextStyle.Headline1) },
        text = { CmpText(text = text, style = CmpTextStyle.Paragraph2) },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_common_btn_delete),
                onClick = onConfirm,
                type = CmpButtonType.TertiaryDanger,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(confirmTag),
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_common_btn_cancel),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.M,
            )
        },
    )
}
