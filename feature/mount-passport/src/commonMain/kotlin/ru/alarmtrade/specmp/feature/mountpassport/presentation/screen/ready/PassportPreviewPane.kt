package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_hint_preview
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_preview
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.DocumentPages

private val SheetMaxWidth = 620.dp
private val LowWindowHeight = 520.dp

/**
 * Живой лист справа от предотчёта (широкое окно): «Предпросмотр» с пометкой, документ пересобирается после правок.
 * Действий над листом нет — они в общей шапке предотчёта. Низкое окно — заголовок без пометки.
 */
@Composable
internal fun PassportPreviewPane(
    state: PassportReadyState,
    actions: PassportReadyActions,
    snackbar: CmpSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag(MountPassportTags.PREVIEW)) {
        val dense = maxHeight < LowWindowHeight
        Column(modifier = Modifier.fillMaxSize()) {
            PreviewTitle(
                note = !dense,
                modifier = Modifier.fillMaxWidth().testTag(MountPassportTags.PREVIEW_HEADER).padding(
                    horizontal = CmpSpacing.space16,
                    vertical = if (dense) CmpSpacing.space8 else CmpSpacing.space12,
                ),
            )
            CmpDivider()
            DocumentPages(
                state = state,
                actions = actions,
                maxSheetWidth = SheetMaxWidth,
                modifier = Modifier.weight(1f),
            )
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) { CmpSnackbarHost(hostState = snackbar) }
    }
}

@Composable
private fun PreviewTitle(note: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        CmpText(
            text = stringResource(Strings.string.mountpassport_ready_title_preview),
            style = CmpTextStyle.Headline3,
            maxLines = 1,
        )
        if (note) {
            CmpText(
                text = stringResource(Strings.string.mountpassport_ready_hint_preview),
                style = CmpTextStyle.Additional0,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}
