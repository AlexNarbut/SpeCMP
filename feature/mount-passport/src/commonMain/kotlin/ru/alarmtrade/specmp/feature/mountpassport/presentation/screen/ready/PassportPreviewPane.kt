package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_hint_preview
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_label_pages
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_preview
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.DocumentButtons
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.DocumentMenu
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.DocumentPages
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.MainDocumentButton

private val SheetMaxWidth = 620.dp
private val LowWindowHeight = 520.dp

/**
 * Живой лист справа от предотчёта (широкое окно): действия над листом, документ пересобирается после правок.
 * Низкое окно — только главная кнопка и «⋯».
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(
                    start = CmpSpacing.space16,
                    end = CmpSpacing.space8,
                    top = if (dense) CmpSpacing.space4 else CmpSpacing.space8,
                    bottom = if (dense) CmpSpacing.space4 else CmpSpacing.space8,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    CmpText(
                        text = stringResource(Strings.string.mountpassport_ready_title_preview),
                        style = CmpTextStyle.Headline3,
                        maxLines = 1,
                    )
                    if (!dense) {
                        CmpText(
                            text = if (state.pageCount > 0) {
                                pluralStringResource(
                                    Strings.plurals.mountpassport_ready_label_pages,
                                    state.pageCount,
                                    state.pageCount,
                                )
                            } else {
                                stringResource(Strings.string.mountpassport_ready_hint_preview)
                            },
                            style = CmpTextStyle.Additional0,
                            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                            maxLines = 1,
                        )
                    }
                }
                if (dense) MainDocumentButton(state, actions) else DocumentButtons(state, actions, CmpButtonSize.S)
                DocumentMenu(state, actions, dense = dense, withEdit = false)
            }
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
