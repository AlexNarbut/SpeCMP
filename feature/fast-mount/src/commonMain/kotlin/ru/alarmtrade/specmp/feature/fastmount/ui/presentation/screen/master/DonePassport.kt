package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_pdf
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_zip
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_btn_passport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_format_passport_number
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_msg_passport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_title_passport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_title_passport_issued
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.MainButton
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountPassportExport

internal const val FAST_MOUNT_PASSPORT_TAG = "fastmount_passport"
internal const val FAST_MOUNT_PASSPORT_EXPORT_PDF_TAG = "fastmount_passport_export_pdf"
internal const val FAST_MOUNT_PASSPORT_EXPORT_ZIP_TAG = "fastmount_passport_export_zip"

/** Экспорт оформленного паспорта вокруг финала ([content] — финал с кнопками), его подставляет корень. */
internal typealias PassportExportSlot =
    @Composable (passportId: String, content: @Composable (MountPassportExport) -> Unit) -> Unit

private val PassportIconBox = 48.dp
private val ActionMinWidth = 180.dp

/** Блок «Паспорт монтажа» на финале: второе действие, «На главную» остаётся главной кнопкой. */
@Composable
internal fun DonePassportBlock(
    passport: FastMountPassport?,
    export: MountPassportExport?,
    actions: MasterActions,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val issuedNumber = passport?.takeIf { it.issued }?.number
    val issued = issuedNumber != null
    CmpCard(modifier = modifier.fillMaxWidth().testTag(FAST_MOUNT_PASSPORT_TAG)) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            ) {
                CmpBox(
                    modifier = Modifier
                        .size(PassportIconBox)
                        .background(
                            if (issued) colors.successHighlight else colors.backgroundSurfaceHighlight,
                            RoundedCornerShape(CmpRadius.cardMedium),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        icon = if (issued) CmpIcons.TaskAlt else CmpIcons.Description,
                        contentDescription = null,
                        tint = if (issued) colors.successDefault else colors.foregroundOnDarkPrimaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                }
                CmpColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                ) {
                    CmpText(
                        text = stringResource(
                            if (issued) {
                                Strings.string.fastmount_done_title_passport_issued
                            } else {
                                Strings.string.fastmount_done_title_passport
                            },
                        ),
                        style = CmpTextStyle.Headline2,
                    )
                    CmpText(
                        text = if (issuedNumber != null) {
                            stringResource(Strings.string.fastmount_done_format_passport_number, issuedNumber)
                        } else {
                            stringResource(Strings.string.fastmount_done_msg_passport)
                        },
                        style = CmpTextStyle.Paragraph3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
            if (issued) {
                export?.let { ExportActions(it) }
            } else {
                MainButton(
                    text = stringResource(Strings.string.fastmount_done_btn_passport),
                    icon = CmpIcons.Description,
                    outlined = true,
                    onClick = actions.onPassportStart,
                )
            }
        }
    }
}

/** Кнопки в ряд от 180 dp каждая, уже — столбиком. */
@Composable
private fun ExportActions(export: MountPassportExport) {
    val pdf: @Composable (Modifier) -> Unit = { modifier ->
        MainButton(
            text = stringResource(Strings.string.common_btn_export_pdf),
            icon = CmpIcons.PictureAsPdf,
            outlined = true,
            busy = export.pdfBusy,
            onClick = export.onPdf,
            modifier = modifier.testTag(FAST_MOUNT_PASSPORT_EXPORT_PDF_TAG),
        )
    }
    val zip: @Composable (Modifier) -> Unit = { modifier ->
        MainButton(
            text = stringResource(Strings.string.common_btn_export_zip),
            icon = CmpIcons.Inventory2,
            outlined = true,
            onClick = export.onZip,
            modifier = modifier.testTag(FAST_MOUNT_PASSPORT_EXPORT_ZIP_TAG),
        )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= ActionMinWidth * 2 + CmpSpacing.space8) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                pdf(Modifier.weight(1f))
                zip(Modifier.weight(1f))
            }
        } else {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                pdf(Modifier)
                zip(Modifier)
            }
        }
    }
}
