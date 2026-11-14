package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.main_banner_a11y_app_update_hide
import ru.alarmtrade.specmp.core.localization.resources.main_banner_subtitle_app_update
import ru.alarmtrade.specmp.core.localization.resources.main_banner_title_app_update
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_a11y_app_update_dot
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_btn_app_update_install
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_app_update_installing
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_subtitle_app_update_available
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_subtitle_app_update_downloading
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_subtitle_app_update_ready
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_title_app_update_available
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_title_app_update_downloading
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_title_app_update_ready
import ru.alarmtrade.specmp.core.localization.resources.main_toast_btn_app_update
import ru.alarmtrade.specmp.core.localization.resources.main_toast_msg_app_update
import ru.alarmtrade.specmp.core.localization.resources.main_topbar_a11y_app_update_required
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbar
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarData
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdate
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainAppUpdateActions

private val BlockShape = RoundedCornerShape(14.dp)
private val BannerShape = RoundedCornerShape(16.dp)
private const val BYTES_IN_MB = 1024L * 1024L

internal object MainUpdateTags {
    const val SIDE_BLOCK = "main_update_side_block"
    const val SIDE_INSTALL = "main_update_side_install"
    const val BANNER = "main_update_banner"
    const val BANNER_HIDE = "main_update_banner_hide"
    const val TOAST = "main_update_toast"
    const val REQUIRED = "main_update_required"
    const val MENU_DOT = "main_update_menu_dot"
}

/** Desktop: блок внизу бокового меню — доступна / загрузка / загружено + «Установить» / установка. */
@Composable
internal fun MainSideUpdateBlock(update: MainAppUpdate, actions: MainAppUpdateActions, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    when (update) {
        is MainAppUpdate.Available -> SideRow(
            onClick = actions.onOpenPrompt,
            accent = true,
            icon = CmpIcons.SystemUpdate,
            title = stringResource(Strings.string.main_drawer_title_app_update_available),
            subtitle = stringResource(Strings.string.main_drawer_subtitle_app_update_available, update.version),
            modifier = modifier,
            trailing = CmpIcons.ChevronRight,
        )

        is MainAppUpdate.Downloading -> CmpClickableSurface(
            onClick = actions.onOpenPrompt,
            modifier = modifier.fillMaxWidth().testTag(MainUpdateTags.SIDE_BLOCK),
            shape = BlockShape,
            color = colors.surfaceContainerHigh,
            contentColor = colors.onSurface,
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SideTexts(
                    icon = CmpIcons.Download,
                    iconTint = colors.primary,
                    title = stringResource(Strings.string.main_drawer_title_app_update_downloading, update.version),
                    subtitle = stringResource(
                        Strings.string.main_drawer_subtitle_app_update_downloading,
                        (update.bytes / BYTES_IN_MB).toInt(),
                    ),
                )
                CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(6.dp))
            }
        }

        is MainAppUpdate.Downloaded -> Column(
            modifier = modifier
                .fillMaxWidth()
                .background(colors.primaryContainer, BlockShape)
                .padding(14.dp)
                .testTag(MainUpdateTags.SIDE_BLOCK),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SideTexts(
                icon = CmpIcons.DownloadDone,
                iconTint = colors.onPrimaryContainer,
                title = stringResource(Strings.string.main_drawer_title_app_update_ready),
                subtitle = stringResource(Strings.string.main_drawer_subtitle_app_update_ready, update.version),
            )
            CmpButton(
                onClick = actions.onInstall,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(MainUpdateTags.SIDE_INSTALL),
            ) {
                CmpIcon(icon = CmpIcons.RestartAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                CmpText(
                    text = stringResource(Strings.string.main_drawer_btn_app_update_install),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        MainAppUpdate.Installing -> Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(colors.surfaceContainerHigh, BlockShape)
                .padding(horizontal = 14.dp)
                .testTag(MainUpdateTags.SIDE_BLOCK),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            CmpText(
                text = stringResource(Strings.string.main_drawer_label_app_update_installing),
                style = CmpTextStyle.LabelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        MainAppUpdate.None, MainAppUpdate.Checking -> Unit
    }
}

@Composable
private fun SideRow(
    onClick: () -> Unit,
    accent: Boolean,
    icon: CmpIcons,
    title: String,
    subtitle: String,
    trailing: CmpIcons,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val content = if (accent) colors.onPrimaryContainer else colors.onSurface
    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp).testTag(MainUpdateTags.SIDE_BLOCK),
        shape = BlockShape,
        color = if (accent) colors.primaryContainer else colors.surfaceContainerHigh,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) { SideTexts(icon, content, title, subtitle) }
            CmpIcon(icon = trailing, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SideTexts(icon: CmpIcons, iconTint: Color, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        CmpIcon(icon = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Column {
            CmpText(text = title, style = CmpTextStyle.LabelLarge, fontWeight = FontWeight.SemiBold)
            CmpText(text = subtitle, style = CmpTextStyle.LabelMedium)
        }
    }
}

/** Телефон и планшет: закрываемая плашка в начале главного; закрытая — до следующей новой версии. */
@Composable
internal fun MainUpdateBanner(version: String, actions: MainAppUpdateActions, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpClickableSurface(
        onClick = actions.onOpenPrompt,
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp).testTag(MainUpdateTags.BANNER),
        shape = BannerShape,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 4.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(icon = CmpIcons.SystemUpdate, contentDescription = null, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = stringResource(Strings.string.main_banner_title_app_update, version),
                    style = CmpTextStyle.TitleMedium,
                )
                CmpText(
                    text = stringResource(Strings.string.main_banner_subtitle_app_update),
                    style = CmpTextStyle.Body,
                )
            }
            CmpIconButton(
                onClick = actions.onDismissBanner,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.main_banner_a11y_app_update_hide),
                modifier = Modifier.testTag(MainUpdateTags.BANNER_HIDE),
            )
        }
    }
}

/** Desktop: уведомление при старте, один раз за сессию; само скрывается. */
@Composable
internal fun MainUpdateToast(version: String, actions: MainAppUpdateActions, modifier: Modifier = Modifier) {
    CmpSnackbar(
        data = CmpSnackbarData(
            message = stringResource(Strings.string.main_toast_msg_app_update, version),
            actionLabel = stringResource(Strings.string.main_toast_btn_app_update),
            withDismissAction = true,
        ),
        onActionClick = actions.onToastAction,
        onDismiss = actions.onToastDismiss,
        modifier = modifier.widthIn(max = 480.dp).testTag(MainUpdateTags.TOAST),
    )
}

/** Топбар: «Нужно обновить приложение» — база настроек требует более новую версию. */
@Composable
internal fun MainRequiredUpdateButton(onClick: () -> Unit) {
    Box {
        CmpIconButton(
            onClick = onClick,
            icon = CmpIcons.SystemUpdate,
            contentDescription = stringResource(Strings.string.main_topbar_a11y_app_update_required),
            modifier = Modifier.testTag(MainUpdateTags.REQUIRED),
        )
        UpdateDot(modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp))
    }
}

@Composable
internal fun MainUpdateMenuDot(modifier: Modifier = Modifier) {
    val description = stringResource(Strings.string.main_drawer_a11y_app_update_dot)
    UpdateDot(modifier = modifier.semantics { contentDescription = description }.testTag(MainUpdateTags.MENU_DOT))
}

@Composable
private fun UpdateDot(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(10.dp).background(cmpThemeColors().error, CircleShape))
}
