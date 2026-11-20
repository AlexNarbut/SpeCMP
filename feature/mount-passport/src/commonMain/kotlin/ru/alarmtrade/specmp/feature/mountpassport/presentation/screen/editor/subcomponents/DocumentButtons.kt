package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_pdf
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_zip
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_pdf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_update
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpActionBar
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorActions

private val ActionMaxWidth = 560.dp
private val SingleRowMinWidth = 480.dp

/**
 * Закреплённый низ предотчёта (телефон, узкое окно): черновик — «Сформировать»; «Оформлен» — «Обновить» и рядом
 * «Экспорт PDF» / «Экспорт ZIP» (не помещаются в ряд — экспорт строкой выше).
 */
@Composable
internal fun EditorDocumentBar(passport: MountPassport, actions: PassportEditorActions, exporting: Boolean) {
    val issued = passport.status == PassportStatus.ISSUED
    CmpActionBar {
        BoxWithConstraints(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val width = Modifier.widthIn(max = ActionMaxWidth).fillMaxWidth()
            when {
                !issued -> MainDocumentButton(issued = false, actions.onCreatePdf, CmpButtonSize.L, width)

                maxWidth >= SingleRowMinWidth -> Row(
                    modifier = width,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    ExportButtons(actions, exporting, CmpButtonSize.L, fill = true)
                    MainDocumentButton(issued = true, actions.onCreatePdf, CmpButtonSize.L, Modifier.weight(1f))
                }

                else -> Column(modifier = width, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                        ExportButtons(actions, exporting, CmpButtonSize.L, fill = true)
                    }
                    MainDocumentButton(issued = true, actions.onCreatePdf, CmpButtonSize.L, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** Шапка предотчёта широкого окна: «Экспорт PDF» / «Экспорт ZIP» (у «Оформлен») и главная кнопка справа. */
@Composable
internal fun WorkspaceDocumentActions(passport: MountPassport, actions: PassportEditorActions, exporting: Boolean) {
    val issued = passport.status == PassportStatus.ISSUED
    Row(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (issued) ExportButtons(actions, exporting, CmpButtonSize.M, fill = false)
        MainDocumentButton(issued, actions.onCreatePdf, CmpButtonSize.M)
    }
}

@Composable
private fun MainDocumentButton(
    issued: Boolean,
    onClick: () -> Unit,
    size: CmpButtonSize,
    modifier: Modifier = Modifier,
) {
    CmpButton(
        text = stringResource(
            if (issued) Strings.string.mountpassport_editor_btn_update else Strings.string.mountpassport_editor_btn_pdf,
        ),
        onClick = onClick,
        size = size,
        icon = if (issued) CmpIcons.Refresh else null,
        uppercase = false,
        modifier = modifier.testTag(MountPassportTags.PDF),
    )
}

@Composable
private fun RowScope.ExportButtons(
    actions: PassportEditorActions,
    exporting: Boolean,
    size: CmpButtonSize,
    fill: Boolean,
) {
    val width = if (fill) Modifier.weight(1f) else Modifier
    CmpButton(
        text = stringResource(Strings.string.common_btn_export_pdf),
        onClick = actions.onExportPdf,
        type = CmpButtonType.Secondary,
        size = size,
        icon = CmpIcons.PictureAsPdf,
        loading = exporting,
        uppercase = false,
        modifier = width.testTag(MountPassportTags.EXPORT_PDF),
    )
    CmpButton(
        text = stringResource(Strings.string.common_btn_export_zip),
        onClick = actions.onExportZip,
        type = CmpButtonType.Secondary,
        size = size,
        icon = CmpIcons.Inventory2,
        uppercase = false,
        modifier = width.testTag(MountPassportTags.EXPORT_ZIP),
    )
}
