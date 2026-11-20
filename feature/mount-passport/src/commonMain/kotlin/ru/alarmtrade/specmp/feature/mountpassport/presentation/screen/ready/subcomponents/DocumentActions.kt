package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState

private val ActionsMaxWidth = 600.dp

/** Низ экрана «Паспорт оформлен»: «Экспорт PDF» и «Экспорт ZIP»; документ не собрался — «Повторить». */
@Composable
internal fun ReadyBottomBar(state: PassportReadyState, actions: PassportReadyActions, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CmpDivider()
        Row(
            modifier = Modifier
                .widthIn(max = ActionsMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.space20, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            if (state.document is DocumentStatus.Failed) {
                CmpButton(
                    text = stringResource(Strings.string.mountpassport_common_btn_retry),
                    onClick = actions.onRetry,
                    icon = CmpIcons.Refresh,
                    size = CmpButtonSize.XL,
                    fullWidth = true,
                )
            } else {
                val enabled = state.document == DocumentStatus.Ready
                ExportPdfButton(state, actions, CmpButtonSize.XL, enabled, Modifier.weight(1f))
                ExportZipButton(actions, CmpButtonSize.XL, enabled, Modifier.weight(1f))
            }
        }
    }
}

/** Низкое окно: обе кнопки экспорта — в шапке, низ не занимается. */
@Composable
internal fun ReadyTopActions(state: PassportReadyState, actions: PassportReadyActions) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val enabled = state.document == DocumentStatus.Ready
        ExportPdfButton(state, actions, CmpButtonSize.S, enabled)
        ExportZipButton(actions, CmpButtonSize.S, enabled, Modifier.padding(end = CmpSpacing.space8))
    }
}

@Composable
internal fun ExportPdfButton(
    state: PassportReadyState,
    actions: PassportReadyActions,
    size: CmpButtonSize,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    CmpButton(
        text = stringResource(Strings.string.common_btn_export_pdf),
        onClick = actions.onExportPdf,
        size = size,
        icon = CmpIcons.PictureAsPdf,
        enabled = enabled,
        loading = state.busy != null,
        uppercase = false,
        modifier = modifier.testTag(MountPassportTags.READY_EXPORT_PDF),
    )
}

@Composable
internal fun ExportZipButton(
    actions: PassportReadyActions,
    size: CmpButtonSize,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    CmpButton(
        text = stringResource(Strings.string.common_btn_export_zip),
        onClick = actions.onExportZip,
        type = CmpButtonType.Secondary,
        size = size,
        icon = CmpIcons.Inventory2,
        enabled = enabled,
        uppercase = false,
        modifier = modifier.testTag(MountPassportTags.READY_EXPORT_ZIP),
    )
}
