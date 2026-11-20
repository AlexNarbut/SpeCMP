package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_label_no_car
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_error_export
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_data
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_more
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_pdf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_photos
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_data
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_export
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_export_error
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_packing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_packing_one
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_pdf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_photos
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_photos_none
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_ready
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_settings_none
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_settings_some
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_packing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_ready
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.megabytesLabel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportPassport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.TransferSheet

private const val CHIPS_SHOWN = 4
private const val PHOTO_ORIGINAL_BYTES = 500_000L
private const val SEPARATOR = " · "

/** Экспорт: состав архива с переключателями → сборка с прогрессом → «Архив готов» / ошибка. */
@Composable
internal fun ExportBody(sheet: TransferSheet.Export, actions: PassportTransferActions, modifier: Modifier = Modifier) {
    when (val phase = sheet.phase) {
        ExportPhase.Loading -> Box(
            modifier = modifier.fillMaxWidth().padding(vertical = CmpSpacing.space32),
            contentAlignment = Alignment.Center,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconL))
        }

        ExportPhase.Form -> ExportForm(sheet, actions, modifier)

        is ExportPhase.Packing -> TransferStatus(
            mark = StatusMark.PROGRESS,
            title = stringResource(Strings.string.mountpassport_transfer_title_packing),
            subtitle = if (phase.total == 1) {
                stringResource(Strings.string.mountpassport_transfer_msg_packing_one)
            } else {
                val current = (phase.done + 1).coerceAtMost(phase.total)
                stringResource(Strings.string.mountpassport_transfer_msg_packing, current, phase.total)
            },
            progress = phase.done.toFloat() / phase.total.coerceAtLeast(1),
            modifier = modifier.testTag(MountPassportTags.TRANSFER_PROGRESS),
        )

        is ExportPhase.Ready -> TransferStatus(
            mark = StatusMark.DONE,
            title = stringResource(Strings.string.mountpassport_transfer_title_ready),
            subtitle = stringResource(
                Strings.string.mountpassport_transfer_msg_ready,
                phase.archive.fileName,
                megabytesLabel(phase.archive.sizeBytes),
            ),
            modifier = modifier,
        )

        is ExportPhase.Failed -> ErrorNote(
            title = stringResource(Strings.string.mountpassport_transfer_error_export),
            text = stringResource(Strings.string.mountpassport_transfer_msg_export_error),
            modifier = modifier.padding(vertical = CmpSpacing.space8),
        )
    }
}

@Composable
private fun ExportForm(sheet: TransferSheet.Export, actions: PassportTransferActions, modifier: Modifier = Modifier) {
    val many = sheet.ids.size > 1
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(
            text = stringResource(Strings.string.mountpassport_transfer_msg_export),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.padding(bottom = CmpSpacing.space8),
        )
        if (many) PassportChips(sheet.passports, total = sheet.ids.size)
        ArchivePart(
            icon = CmpIcons.PictureAsPdf,
            title = stringResource(Strings.string.mountpassport_transfer_label_pdf),
            subtitle = stringResource(Strings.string.mountpassport_transfer_msg_pdf),
            on = true,
            onToggle = null,
        )
        ArchivePart(
            icon = CmpIcons.Description,
            title = stringResource(Strings.string.mountpassport_transfer_label_data),
            subtitle = stringResource(Strings.string.mountpassport_transfer_msg_data),
            on = true,
            onToggle = null,
            file = PASSPORT_JSON,
        )
        val withSettings = sheet.withSettings
        ArchivePart(
            icon = CmpIcons.Tune,
            title = stringResource(Strings.string.mountpassport_transfer_label_settings),
            subtitle = when {
                withSettings == 0 -> stringResource(Strings.string.mountpassport_transfer_msg_settings_none)

                many -> stringResource(
                    Strings.string.mountpassport_transfer_msg_settings_some,
                    withSettings,
                    sheet.ids.size,
                )

                else -> stringResource(Strings.string.mountpassport_transfer_msg_settings)
            },
            on = withSettings > 0 && sheet.includeSettings,
            onToggle = actions.onIncludeSettingsChange.takeIf { withSettings > 0 },
            file = SETTINGS_DXS,
            modifier = Modifier.testTag(MountPassportTags.TRANSFER_SETTINGS),
        )
        val photos = sheet.photos
        ArchivePart(
            icon = CmpIcons.PhotoLibrary,
            title = stringResource(Strings.string.mountpassport_transfer_label_photos),
            subtitle = if (photos == 0) {
                stringResource(Strings.string.mountpassport_transfer_msg_photos_none)
            } else {
                stringResource(
                    Strings.string.mountpassport_transfer_msg_photos,
                    photos,
                    megabytesLabel(photos * PHOTO_ORIGINAL_BYTES),
                )
            },
            on = photos > 0 && sheet.includePhotos,
            onToggle = actions.onIncludePhotosChange.takeIf { photos > 0 },
            file = PHOTOS_DIR,
            modifier = Modifier.testTag(MountPassportTags.TRANSFER_PHOTOS),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PassportChips(passports: List<ExportPassport>, total: Int) {
    val colors = CmpTheme.colors
    val noCar = stringResource(Strings.string.mountpassport_common_label_no_car)
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = CmpSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        passports.take(CHIPS_SHOWN).forEach { passport ->
            val text = listOfNotNull(passport.carTitle ?: noCar, passport.plate).joinToString(SEPARATOR)
            CmpText(
                text = text,
                style = CmpTextStyle.Additional0,
                color = colors.foregroundOnDarkPrimaryDefault,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(CmpRadius.tags))
                    .background(colors.backgroundSurfaceHighlight)
                    .padding(horizontal = CmpSpacing.space8, vertical = CmpSpacing.space4),
            )
        }
        if (total > CHIPS_SHOWN) {
            CmpText(
                text = stringResource(Strings.string.mountpassport_transfer_label_more, total - CHIPS_SHOWN),
                style = CmpTextStyle.Additional0,
                color = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.padding(vertical = CmpSpacing.space4),
            )
        }
    }
}

private const val PASSPORT_JSON = "passport.json"
private const val SETTINGS_DXS = "settings.dxs"
private const val PHOTOS_DIR = "photos/"
