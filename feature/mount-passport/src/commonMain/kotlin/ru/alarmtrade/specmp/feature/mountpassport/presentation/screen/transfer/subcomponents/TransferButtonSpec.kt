package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_btn_save
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_btn_share
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_import
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_pick_another
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportAction
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ImportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.TransferSheet

/** Кнопка низа листа; первая в списке — главная. */
@Immutable
internal class TransferButtonSpec(
    val text: String,
    val onClick: () -> Unit,
    val tag: String,
    val primary: Boolean,
    val icon: CmpIcons? = null,
    val enabled: Boolean = true,
    val loading: Boolean = false,
)

@Composable
internal fun TransferButton(spec: TransferButtonSpec, fullWidth: Boolean, modifier: Modifier = Modifier) {
    CmpButton(
        text = spec.text,
        onClick = spec.onClick,
        type = if (spec.primary) CmpButtonType.Primary else CmpButtonType.Secondary,
        size = if (fullWidth && spec.primary) CmpButtonSize.XL else CmpButtonSize.L,
        icon = spec.icon,
        enabled = spec.enabled,
        loading = spec.loading,
        fullWidth = fullWidth,
        uppercase = false,
        modifier = modifier.testTag(spec.tag),
    )
}

/** Кнопки по шагу: без «Поделиться» (desktop) главная — «Сохранить файл»; пока идёт работа — без кнопок. */
@Composable
internal fun transferButtons(
    sheet: TransferSheet,
    canShare: Boolean,
    actions: PassportTransferActions,
): List<TransferButtonSpec> {
    val cancel = TransferButtonSpec(
        text = stringResource(Strings.string.mountpassport_transfer_btn_cancel),
        onClick = actions.onClose,
        tag = MountPassportTags.TRANSFER_CANCEL,
        primary = false,
    )
    return when (sheet) {
        is TransferSheet.Export -> when (val phase = sheet.phase) {
            ExportPhase.Form -> deliveryButtons(canShare, busy = null, actions, cancel)
            is ExportPhase.Ready -> deliveryButtons(canShare, phase.busy, actions, cancel)
            is ExportPhase.Failed -> listOf(retry(actions), cancel)
            ExportPhase.Loading, is ExportPhase.Packing -> emptyList()
        }

        is TransferSheet.Import -> when (val phase = sheet.phase) {
            is ImportPhase.NotArchive, is ImportPhase.NothingToImport, is ImportPhase.Failed ->
                listOf(pickAnother(actions), cancel)

            is ImportPhase.Summary -> listOf(importButton(phase, actions), cancel)

            is ImportPhase.Done -> listOf(
                TransferButtonSpec(
                    text = stringResource(Strings.string.mountpassport_transfer_btn_done),
                    onClick = actions.onClose,
                    tag = MountPassportTags.TRANSFER_DONE,
                    primary = true,
                ),
            )

            ImportPhase.Pick, is ImportPhase.Reading, is ImportPhase.Importing -> emptyList()
        }
    }
}

@Composable
private fun deliveryButtons(
    canShare: Boolean,
    busy: ExportAction?,
    actions: PassportTransferActions,
    cancel: TransferButtonSpec,
): List<TransferButtonSpec> {
    val save = TransferButtonSpec(
        text = stringResource(Strings.string.mountpassport_ready_btn_save),
        onClick = actions.onSave,
        tag = MountPassportTags.TRANSFER_SAVE,
        primary = !canShare,
        icon = CmpIcons.Download,
        enabled = busy == null,
        loading = busy == ExportAction.SAVE,
    )
    if (!canShare) return listOf(save, cancel)
    val share = TransferButtonSpec(
        text = stringResource(Strings.string.mountpassport_ready_btn_share),
        onClick = actions.onShare,
        tag = MountPassportTags.TRANSFER_SHARE,
        primary = true,
        icon = CmpIcons.Share,
        enabled = busy == null,
        loading = busy == ExportAction.SHARE,
    )
    return listOf(share, save)
}

@Composable
private fun retry(actions: PassportTransferActions) = TransferButtonSpec(
    text = stringResource(Strings.string.mountpassport_common_btn_retry),
    onClick = actions.onRetry,
    tag = MountPassportTags.TRANSFER_RETRY,
    primary = true,
    icon = CmpIcons.Refresh,
)

@Composable
private fun pickAnother(actions: PassportTransferActions) = TransferButtonSpec(
    text = stringResource(Strings.string.mountpassport_transfer_btn_pick_another),
    onClick = actions.onPickAnother,
    tag = MountPassportTags.TRANSFER_PICK_ANOTHER,
    primary = true,
    icon = CmpIcons.FolderOpen,
)

@Composable
private fun importButton(summary: ImportPhase.Summary, actions: PassportTransferActions): TransferButtonSpec {
    val count = summary.toImport
    return TransferButtonSpec(
        text = if (count > 0) {
            pluralStringResource(Strings.plurals.mountpassport_transfer_btn_import, count, count)
        } else {
            stringResource(Strings.string.mountpassport_transfer_btn_nothing)
        },
        onClick = actions.onImportConfirm,
        tag = MountPassportTags.TRANSFER_IMPORT,
        primary = true,
        icon = CmpIcons.DownloadDone,
        enabled = count > 0,
    )
}
