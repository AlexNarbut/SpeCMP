package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_export
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_export_one
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_import
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents.ExportBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents.ImportBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents.TransferButton
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents.TransferButtonSpec
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents.transferButtons

private val DialogWidth = CmpDialogWidth(fraction = 0.6f, min = 480.dp, max = 560.dp)
private val SheetMaxBodyHeight = 560.dp

/** Экспорт / импорт: на телефоне — лист снизу, шире ([wide]) — диалог; пока идёт работа, не закрывается. */
@Composable
internal fun PassportTransferSheet(state: PassportTransferState, actions: PassportTransferActions, wide: Boolean) {
    val sheet = state.sheet ?: return
    val busy = when (sheet) {
        is TransferSheet.Export -> sheet.busy
        is TransferSheet.Import -> sheet.busy
    }
    val dismiss = { if (!busy) actions.onClose() }
    val buttons = transferButtons(sheet, state.canShare, actions)
    if (wide) {
        CmpDialog(
            onDismissRequest = dismiss,
            title = { CmpText(text = transferTitle(sheet), style = CmpTextStyle.Headline1) },
            text = { TransferBody(sheet, actions) },
            confirmButton = { buttons.firstOrNull()?.let { TransferButton(it, fullWidth = false) } },
            dismissButton = buttons.getOrNull(1)?.let { { TransferButton(it, fullWidth = false) } },
            width = DialogWidth,
            modifier = Modifier.testTag(MountPassportTags.TRANSFER),
        )
    } else {
        CmpBottomSheet(onDismissRequest = dismiss, modifier = Modifier.testTag(MountPassportTags.TRANSFER)) {
            TransferPanel(sheet, actions, busy, buttons)
        }
    }
}

/** Содержимое листа: заголовок с «Закрыть», тело с прокруткой и кнопки столбиком. */
@Composable
internal fun TransferPanel(
    sheet: TransferSheet,
    actions: PassportTransferActions,
    busy: Boolean,
    buttons: List<TransferButtonSpec>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = CmpSpacing.space20, end = CmpSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(text = transferTitle(sheet), style = CmpTextStyle.Headline1, modifier = Modifier.weight(1f))
            if (!busy) {
                CmpIconButton(
                    onClick = actions.onClose,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.mountpassport_transfer_a11y_close),
                )
            }
        }
        Column(
            modifier = Modifier
                .heightIn(max = SheetMaxBodyHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CmpSpacing.space20, vertical = CmpSpacing.space8),
        ) {
            TransferBody(sheet, actions)
        }
        if (buttons.isNotEmpty()) {
            CmpDivider()
            Column(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = CmpSpacing.space20,
                    vertical = CmpSpacing.space12,
                ),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                buttons.forEach { TransferButton(it, fullWidth = true) }
            }
        }
    }
}

@Composable
private fun TransferBody(sheet: TransferSheet, actions: PassportTransferActions) {
    when (sheet) {
        is TransferSheet.Export -> ExportBody(sheet, actions)
        is TransferSheet.Import -> ImportBody(sheet.phase, actions)
    }
}

@Composable
private fun transferTitle(sheet: TransferSheet): String = when (sheet) {
    is TransferSheet.Import -> stringResource(Strings.string.mountpassport_transfer_title_import)

    is TransferSheet.Export -> if (sheet.ids.size == 1) {
        stringResource(Strings.string.mountpassport_transfer_title_export_one)
    } else {
        pluralStringResource(Strings.plurals.mountpassport_transfer_title_export, sheet.ids.size, sheet.ids.size)
    }
}
