package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_item_save_configurations
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_item_save_file
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_a11y_more
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_btn_edit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_btn_open
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_btn_save
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_btn_share
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_item_export
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_label_settings
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentAction
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState

private val ActionsMaxWidth = 600.dp
private val TileMinHeight = 72.dp
private val TileBorder = 1.dp

/** Низ экрана «Паспорт готов»: телефон — три плитки и «Поделиться», без «Поделиться» (desktop) — ряд кнопок. */
@Composable
internal fun ReadyBottomBar(state: PassportReadyState, actions: PassportReadyActions, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CmpDivider()
        Column(
            modifier = Modifier
                .widthIn(max = ActionsMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.space20, vertical = CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            when (state.document) {
                DocumentStatus.Ready -> if (state.canShare) {
                    PhoneActions(
                        state,
                        actions,
                    )
                } else {
                    DesktopActions(state, actions)
                }

                DocumentStatus.Forming -> EditButton(actions.onEdit, fullWidth = true)

                is DocumentStatus.Failed -> {
                    CmpButton(
                        text = stringResource(Strings.string.mountpassport_common_btn_retry),
                        onClick = actions.onRetry,
                        icon = CmpIcons.Refresh,
                        size = CmpButtonSize.XL,
                        fullWidth = true,
                    )
                    EditButton(actions.onEdit, fullWidth = true)
                }
            }
        }
    }
}

@Composable
private fun PhoneActions(state: PassportReadyState, actions: PassportReadyActions) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            Tile(
                CmpIcons.Download,
                Strings.string.mountpassport_ready_btn_save,
                MountPassportTags.READY_SAVE,
                actions.onSave,
            )
            Tile(
                CmpIcons.OpenInNew,
                Strings.string.mountpassport_ready_btn_open,
                MountPassportTags.READY_OPEN,
                actions.onOpen,
            )
            Tile(
                CmpIcons.Edit,
                Strings.string.mountpassport_ready_btn_edit,
                MountPassportTags.READY_EDIT,
                actions.onEdit,
            )
        }
        CmpButton(
            text = stringResource(Strings.string.mountpassport_ready_btn_share),
            onClick = actions.onShare,
            icon = CmpIcons.Share,
            size = CmpButtonSize.XL,
            fullWidth = true,
            loading = state.busy == DocumentAction.SHARE,
            modifier = Modifier.testTag(MountPassportTags.READY_SHARE),
        )
    }
}

@Composable
private fun DesktopActions(state: PassportReadyState, actions: PassportReadyActions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditButton(actions.onEdit, fullWidth = false)
        DocumentButtons(state, actions, size = CmpButtonSize.L)
    }
}

/** Действия над листом: «Открыть», «Сохранить файл» и главная — «Поделиться» на телефоне, «Сохранить файл» без неё. */
@Composable
internal fun RowScope.DocumentButtons(state: PassportReadyState, actions: PassportReadyActions, size: CmpButtonSize) {
    val ready = state.document == DocumentStatus.Ready
    CmpButton(
        text = stringResource(Strings.string.mountpassport_ready_btn_open),
        onClick = actions.onOpen,
        type = CmpButtonType.Secondary,
        size = size,
        icon = CmpIcons.OpenInNew,
        enabled = ready,
        uppercase = false,
        modifier = Modifier.testTag(MountPassportTags.READY_OPEN),
    )
    CmpButton(
        text = stringResource(Strings.string.mountpassport_ready_btn_save),
        onClick = actions.onSave,
        type = if (state.canShare) CmpButtonType.Secondary else CmpButtonType.Primary,
        size = size,
        icon = CmpIcons.Download,
        enabled = ready,
        loading = state.busy == DocumentAction.SAVE,
        uppercase = false,
        modifier = Modifier.testTag(MountPassportTags.READY_SAVE),
    )
    if (state.canShare) {
        CmpButton(
            text = stringResource(Strings.string.mountpassport_ready_btn_share),
            onClick = actions.onShare,
            size = size,
            icon = CmpIcons.Share,
            enabled = ready,
            loading = state.busy == DocumentAction.SHARE,
            uppercase = false,
            modifier = Modifier.testTag(MountPassportTags.READY_SHARE),
        )
    }
}

/** Низкое окно: одна главная кнопка, остальное — в «⋯». */
@Composable
internal fun MainDocumentButton(state: PassportReadyState, actions: PassportReadyActions) {
    val share = state.canShare
    CmpButton(
        text = stringResource(
            if (share) Strings.string.mountpassport_ready_btn_share else Strings.string.mountpassport_ready_btn_save,
        ),
        onClick = if (share) actions.onShare else actions.onSave,
        size = CmpButtonSize.S,
        icon = if (share) CmpIcons.Share else CmpIcons.Download,
        enabled = state.document == DocumentStatus.Ready,
        loading = state.busy == if (share) DocumentAction.SHARE else DocumentAction.SAVE,
        uppercase = false,
        modifier = Modifier.testTag(if (share) MountPassportTags.READY_SHARE else MountPassportTags.READY_SAVE),
    )
}

/**
 * «⋯»: в низком окне ([dense]) — действия, не уместившиеся рядом с главной кнопкой ([withEdit] — и «Изменить»);
 * всегда — «Экспорт паспорта (ZIP)» и снимок настроек блока, если он сохранён.
 */
@Composable
internal fun DocumentMenu(
    state: PassportReadyState,
    actions: PassportReadyActions,
    dense: Boolean,
    withEdit: Boolean,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        CmpIconButton(
            onClick = { open = true },
            icon = CmpIcons.MoreVert,
            contentDescription = stringResource(Strings.string.mountpassport_ready_a11y_more),
            modifier = Modifier.testTag(MountPassportTags.READY_MORE),
        )
        CmpDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            val ready = state.document == DocumentStatus.Ready
            if (dense) {
                MenuItem(CmpIcons.OpenInNew, Strings.string.mountpassport_ready_btn_open, ready) {
                    open = false
                    actions.onOpen()
                }
                if (state.canShare) {
                    MenuItem(CmpIcons.Download, Strings.string.mountpassport_ready_btn_save, ready) {
                        open = false
                        actions.onSave()
                    }
                }
                if (withEdit) {
                    MenuItem(CmpIcons.Edit, Strings.string.mountpassport_ready_btn_edit, enabled = true) {
                        open = false
                        actions.onEdit()
                    }
                }
            }
            if (dense) CmpDivider()
            MenuItem(CmpIcons.Inventory2, Strings.string.mountpassport_ready_item_export, enabled = true) {
                open = false
                actions.onExport()
            }
            if (state.hasSettings) {
                CmpDivider()
                CmpText(
                    text = stringResource(Strings.string.mountpassport_ready_label_settings),
                    style = CmpTextStyle.Additional0,
                    color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
                )
                MenuItem(CmpIcons.Save, Strings.string.mountpassport_editor_item_save_file, enabled = true) {
                    open = false
                    actions.onSaveSettingsFile()
                }
                MenuItem(
                    CmpIcons.FolderOpen,
                    Strings.string.mountpassport_editor_item_save_configurations,
                    enabled = true,
                ) {
                    open = false
                    actions.onSaveSettingsToConfigurations()
                }
            }
        }
    }
}

@Composable
private fun MenuItem(icon: CmpIcons, text: StringResource, enabled: Boolean, onClick: () -> Unit) {
    CmpDropdownMenuItem(text = stringResource(text), onClick = onClick, icon = icon, enabled = enabled)
}

@Composable
private fun EditButton(onEdit: () -> Unit, fullWidth: Boolean) {
    CmpButton(
        text = stringResource(Strings.string.mountpassport_ready_btn_edit),
        onClick = onEdit,
        type = if (fullWidth) CmpButtonType.Secondary else CmpButtonType.Tertiary,
        icon = CmpIcons.Edit,
        fullWidth = fullWidth,
        uppercase = false,
        modifier = Modifier.testTag(MountPassportTags.READY_EDIT),
    )
}

@Composable
private fun RowScope.Tile(icon: CmpIcons, text: StringResource, tag: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = TileMinHeight)
            .clip(shape)
            .border(TileBorder, CmpTheme.colors.borderLight, shape)
            .clickable(onClick = onClick)
            .padding(CmpSpacing.space8)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4, Alignment.CenterVertically),
    ) {
        CmpIcon(icon = icon, contentDescription = null)
        CmpText(text = stringResource(text), style = CmpTextStyle.Additional0, maxLines = 2)
    }
}
