package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_label_engine_block
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_label_keyless
import ru.alarmtrade.specmp.core.localization.resources.vehicle_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.vehicle_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.vehicle_mismatch_btn_confirm
import ru.alarmtrade.specmp.core.localization.resources.vehicle_mismatch_msg_text
import ru.alarmtrade.specmp.core.localization.resources.vehicle_mismatch_title_dialog
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpHtmlText
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.note
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CapabilityKind
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleDialog

/** Диалоги карточки: примечание к возможности (шторка на узком окне, диалог на широком), несовпадение CAN. */
@Composable
internal fun VehicleDialogs(dialog: VehicleDialog, wide: Boolean, actions: CardActions) {
    when (dialog) {
        is VehicleDialog.CapabilityNote -> CapabilityNote(dialog, wide, actions.onDialogDismiss)

        is VehicleDialog.CanMismatch -> CmpDialog(
            onDismissRequest = actions.onDialogDismiss,
            icon = CmpIcons.Warning.imageVector(),
            title = { CmpText(text = stringResource(Strings.string.vehicle_mismatch_title_dialog)) },
            text = {
                CmpText(
                    text = stringResource(
                        Strings.string.vehicle_mismatch_msg_text,
                        dialog.car.canCode,
                        dialog.firmwareCanCode,
                    ),
                    style = CmpTextStyle.BodyLarge,
                )
            },
            dismissButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.vehicle_common_btn_cancel),
                    onClick = actions.onDialogDismiss,
                )
            },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.vehicle_mismatch_btn_confirm),
                    onClick = actions.onMismatchConfirm,
                    modifier = Modifier.testTag(VehicleTestTags.MISMATCH_CONFIRM),
                )
            },
        )
    }
}

@Composable
private fun CapabilityNote(dialog: VehicleDialog.CapabilityNote, wide: Boolean, onDismiss: () -> Unit) {
    val title = stringResource(
        when (dialog.kind) {
            CapabilityKind.ENGINE_BLOCK -> Strings.string.vehicle_cap_label_engine_block
            CapabilityKind.KEYLESS_BYPASS -> Strings.string.vehicle_cap_label_keyless
        },
    )
    val note = dialog.capability.note.orEmpty()
    if (wide) {
        CmpDialog(
            onDismissRequest = onDismiss,
            title = { CmpText(text = title) },
            text = { CmpHtmlText(html = note) },
            confirmButton = {
                CmpTextButton(text = stringResource(Strings.string.vehicle_common_btn_close), onClick = onDismiss)
            },
        )
    } else {
        CmpBottomSheet(onDismissRequest = onDismiss) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CmpText(text = title, style = CmpTextStyle.Title)
                CmpHtmlText(html = note)
                CmpRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpButton(
                        text = stringResource(Strings.string.vehicle_common_btn_close),
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    )
                }
            }
        }
    }
}
