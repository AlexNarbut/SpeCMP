package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_screen
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.WorkspaceDocumentActions

/**
 * Предотчёт в широком окне (≥ 840): одна шапка во всю ширину (номер, дата, «Сохранено», «Сформировать» /
 * «Обновить», у «Оформлен» — экспорт), под ней поля слева (42 %, 380–520 dp) и живой лист справа ([preview]).
 */
@Composable
internal fun PassportWorkspace(
    state: PassportEditorState,
    actions: PassportEditorActions,
    snackbar: CmpSnackbarHostState,
    exporting: Boolean = false,
    preview: @Composable (Modifier) -> Unit,
) {
    val passport = state.passport.content
    Column(modifier = Modifier.fillMaxSize()) {
        CmpTopAppBar(
            title = stringResource(Strings.string.mountpassport_editor_title_screen),
            subtitle = passport?.let { passportSubtitle(it) },
            navigationIcon = {
                CmpIconButton(
                    onClick = actions.onBack,
                    icon = CmpIcons.ArrowBack,
                    contentDescription = stringResource(Strings.string.mountpassport_common_a11y_back),
                )
            },
            actions = {
                if (passport != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SaveStatusLabel(state.save)
                        WorkspaceDocumentActions(passport, actions, exporting)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag(MountPassportTags.WORKSPACE_TOP_BAR),
        )
        CmpDivider()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val fieldsWidth = (maxWidth * FIELDS_SHARE).coerceIn(FieldsMinWidth, FieldsMaxWidth)
            Row(modifier = Modifier.fillMaxSize()) {
                PassportEditorScreen(
                    state = state,
                    actions = actions,
                    snackbar = snackbar,
                    embedded = true,
                    showPdf = false,
                    showHeader = false,
                    modifier = Modifier.width(fieldsWidth),
                )
                CmpVerticalDivider()
                preview(Modifier.weight(1f))
            }
        }
    }
}

private val FieldsMinWidth = 380.dp
private val FieldsMaxWidth = 520.dp
private const val FIELDS_SHARE = 0.42f
