package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_screen
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_subtitle_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_issued
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.DocumentPages
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.ReadyBottomBar
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.ReadyTopActions

private val SheetMaxWidth = 560.dp
private val LowWindowHeight = 520.dp

/**
 * «Паспорт оформлен» (телефон, узкое окно): листы документа, «Стр. N из M», «Экспорт PDF» и «Экспорт ZIP» внизу,
 * «←» — назад в предотчёт. Низкое окно (< 520) — обе кнопки в шапке, низ не занимается.
 */
@Composable
internal fun PassportReadyScreen(
    state: PassportReadyState,
    actions: PassportReadyActions,
    snackbar: CmpSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag(MountPassportTags.READY)) {
        val low = maxHeight < LowWindowHeight
        val ready = state.document == DocumentStatus.Ready
        CmpScaffold(
            topBar = {
                CmpTopAppBar(
                    title = stringResource(
                        if (ready) {
                            Strings.string.mountpassport_ready_title_issued
                        } else {
                            Strings.string.mountpassport_editor_title_screen
                        },
                    ),
                    subtitle = state.passport?.let {
                        stringResource(Strings.string.mountpassport_ready_subtitle_number, it.number)
                    },
                    navigationIcon = {
                        CmpIconButton(
                            onClick = actions.onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.mountpassport_common_a11y_back),
                            modifier = Modifier.testTag(MountPassportTags.READY_BACK),
                        )
                    },
                    actions = { if (ready && low) ReadyTopActions(state, actions) },
                )
            },
            bottomBar = { if (!low || !ready) ReadyBottomBar(state, actions) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
        ) { padding ->
            DocumentPages(
                state = state,
                actions = actions,
                maxSheetWidth = SheetMaxWidth,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
