package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_hint_forming
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_loading_forming
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_msg_error
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_subtitle_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_error
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_issued
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState

/**
 * Широкое окно после «Сформировать» / «Обновить»: «Паспорт оформлен · №» с «Экспорт PDF» и «Экспорт ZIP». Пока
 * документ собирается — экспорт недоступен и диалог не закрывается.
 */
@Composable
internal fun IssuedDialog(state: PassportReadyState, actions: PassportReadyActions) {
    val issue = state.issue ?: return
    val number = state.passport?.number.orEmpty()
    CmpDialog(
        onDismissRequest = actions.onIssueDismiss,
        modifier = Modifier.testTag(MountPassportTags.ISSUED_DIALOG),
        icon = (if (issue is DocumentStatus.Failed) CmpIcons.ErrorOutline else CmpIcons.TaskAlt).imageVector(),
        title = {
            CmpText(
                text = stringResource(
                    when (issue) {
                        DocumentStatus.Forming -> Strings.string.mountpassport_ready_loading_forming
                        DocumentStatus.Ready -> Strings.string.mountpassport_ready_title_issued
                        is DocumentStatus.Failed -> Strings.string.mountpassport_ready_title_error
                    },
                ),
                style = CmpTextStyle.Headline1,
            )
        },
        text = {
            CmpText(
                text = when (issue) {
                    DocumentStatus.Forming -> stringResource(Strings.string.mountpassport_ready_hint_forming)
                    DocumentStatus.Ready -> stringResource(Strings.string.mountpassport_ready_subtitle_number, number)
                    is DocumentStatus.Failed -> stringResource(Strings.string.mountpassport_ready_msg_error)
                },
                style = CmpTextStyle.Paragraph2,
            )
        },
        confirmButton = {
            if (issue is DocumentStatus.Failed) {
                CmpButton(
                    text = stringResource(Strings.string.mountpassport_common_btn_retry),
                    onClick = actions.onIssueRetry,
                    size = CmpButtonSize.M,
                    icon = CmpIcons.Refresh,
                    modifier = Modifier.testTag(MountPassportTags.READY_RETRY),
                )
            } else {
                ExportPdfButton(state, actions, CmpButtonSize.M, enabled = issue == DocumentStatus.Ready)
            }
        },
        dismissButton = if (issue is DocumentStatus.Failed) {
            null
        } else {
            { ExportZipButton(actions, CmpButtonSize.M, enabled = issue == DocumentStatus.Ready) }
        },
    )
}
