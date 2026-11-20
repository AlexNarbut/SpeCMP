package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_a11y_pick_cancel
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_a11y_transfer
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_backup_later
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_export
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_export_all
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_export_picked
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_import
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_pick_all
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_pick_hint
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_unpick_all
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_item_import
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_item_pick
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_backup
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_subtitle_pick
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_title_backup
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_title_picked
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
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
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags

private val PickBarMaxWidth = 560.dp

/** Шапка режима выбора: «Выбрано: N», «Отменить выбор», «Выбрать все» / «Снять все». */
@Composable
internal fun PickTopBar(
    picked: Int,
    allPicked: Boolean,
    onCancel: () -> Unit,
    onPickAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpTopAppBar(
        title = stringResource(Strings.string.mountpassport_list_title_picked, picked),
        subtitle = stringResource(Strings.string.mountpassport_list_subtitle_pick),
        modifier = modifier,
        navigationIcon = {
            CmpIconButton(
                onClick = onCancel,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.mountpassport_list_a11y_pick_cancel),
            )
        },
        actions = {
            CmpButton(
                text = stringResource(
                    if (allPicked) {
                        Strings.string.mountpassport_list_btn_unpick_all
                    } else {
                        Strings.string.mountpassport_list_btn_pick_all
                    },
                ),
                onClick = onPickAll,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                uppercase = false,
                modifier = Modifier.padding(end = CmpSpacing.space8).testTag(MountPassportTags.LIST_PICK_ALL),
            )
        },
    )
}

/**
 * Перенос в шапке архива: шире телефона ([wide]) — кнопки «Импорт» и «Экспорт» (режим выбора), на телефоне — «⋯»
 * с импортом, выбором и «Экспорт всех»; экспорт — только если есть что экспортировать ([canExport]).
 */
@Composable
internal fun RowScope.TransferActions(
    wide: Boolean,
    canExport: Boolean,
    onImport: () -> Unit,
    onPickStart: () -> Unit,
    onExportAll: () -> Unit,
) {
    if (wide) {
        Row(
            modifier = Modifier.padding(end = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_list_btn_import),
                onClick = onImport,
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.S,
                icon = CmpIcons.FolderOpen,
                uppercase = false,
                modifier = Modifier.testTag(MountPassportTags.LIST_IMPORT),
            )
            if (canExport) {
                CmpButton(
                    text = stringResource(Strings.string.mountpassport_list_btn_export),
                    onClick = onPickStart,
                    type = CmpButtonType.Secondary,
                    size = CmpButtonSize.S,
                    icon = CmpIcons.Inventory2,
                    uppercase = false,
                    modifier = Modifier.testTag(MountPassportTags.LIST_EXPORT_PICK),
                )
            }
        }
        return
    }
    var open by remember { mutableStateOf(false) }
    Box {
        CmpIconButton(
            onClick = { open = true },
            icon = CmpIcons.MoreVert,
            contentDescription = stringResource(Strings.string.mountpassport_list_a11y_transfer),
            modifier = Modifier.testTag(MountPassportTags.LIST_TRANSFER_MENU),
        )
        CmpDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            CmpDropdownMenuItem(
                text = stringResource(Strings.string.mountpassport_list_item_import),
                icon = CmpIcons.FolderOpen,
                onClick = {
                    open = false
                    onImport()
                },
                modifier = Modifier.testTag(MountPassportTags.LIST_IMPORT),
            )
            if (canExport) {
                CmpDropdownMenuItem(
                    text = stringResource(Strings.string.mountpassport_list_item_pick),
                    icon = CmpIcons.DoneAll,
                    onClick = {
                        open = false
                        onPickStart()
                    },
                    modifier = Modifier.testTag(MountPassportTags.LIST_EXPORT_PICK),
                )
                CmpDropdownMenuItem(
                    text = stringResource(Strings.string.mountpassport_list_btn_export_all),
                    icon = CmpIcons.Inventory2,
                    onClick = {
                        open = false
                        onExportAll()
                    },
                    modifier = Modifier.testTag(MountPassportTags.LIST_EXPORT_ALL),
                )
            }
        }
    }
}

/** Низ режима выбора: «Экспорт: N»; пока ничего не отмечено — «Отметьте паспорта». */
@Composable
internal fun PickBottomBar(picked: Int, onExport: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpDivider()
        CmpButton(
            text = if (picked > 0) {
                stringResource(Strings.string.mountpassport_list_btn_export_picked, picked)
            } else {
                stringResource(Strings.string.mountpassport_list_btn_pick_hint)
            },
            onClick = onExport,
            enabled = picked > 0,
            size = CmpButtonSize.XL,
            icon = CmpIcons.Inventory2,
            fullWidth = true,
            uppercase = false,
            modifier = Modifier
                .widthIn(max = PickBarMaxWidth)
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12)
                .testTag(MountPassportTags.LIST_EXPORT_PICKED),
        )
    }
}

/** Ежемесячная плашка «Сохраните копию архива»: «Экспорт всех» / «Позже». */
@Composable
internal fun BackupReminder(onExportAll: () -> Unit, onLater: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.brandPrimaryHighlight)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(
                start = CmpSpacing.space16,
                end = CmpSpacing.space12,
                top = CmpSpacing.space12,
                bottom = CmpSpacing.space8,
            ),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            CmpIcon(icon = CmpIcons.CloudSync, contentDescription = null, tint = colors.brandPrimaryDefault)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(
                    text = stringResource(Strings.string.mountpassport_list_title_backup),
                    style = CmpTextStyle.Headline3,
                )
                CmpText(
                    text = stringResource(Strings.string.mountpassport_list_msg_backup),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_list_btn_backup_later),
                onClick = onLater,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                uppercase = false,
                modifier = Modifier.testTag(MountPassportTags.BACKUP_LATER),
            )
            CmpButton(
                text = stringResource(Strings.string.mountpassport_list_btn_export_all),
                onClick = onExportAll,
                size = CmpButtonSize.S,
                icon = CmpIcons.Inventory2,
                uppercase = false,
                modifier = Modifier.testTag(MountPassportTags.BACKUP_EXPORT),
            )
        }
    }
}
