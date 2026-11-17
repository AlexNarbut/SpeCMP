package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbar
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarData
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdate
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainAppUpdateActions

private val BlockShape = RoundedCornerShape(CmpRadius.cardSmall)
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
    val colors = CmpTheme.colors
    val blockModifier = modifier.fillMaxWidth().testTag(MainUpdateTags.SIDE_BLOCK)
    when (update) {
        is MainAppUpdate.Available -> Row(
            modifier = blockModifier
                .heightIn(min = CmpSize.buttonXl)
                .cmpPressable(onClick = actions.onOpenPrompt, shape = BlockShape)
                .background(colors.brandPrimaryHighlight, BlockShape)
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SideTexts(
                    icon = CmpIcons.SystemUpdate,
                    iconTint = colors.foregroundOnDarkPrimaryDefault,
                    title = stringResource(Strings.string.main_drawer_title_app_update_available),
                    subtitle = stringResource(Strings.string.main_drawer_subtitle_app_update_available, update.version),
                )
            }
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                tint = colors.foregroundOnDarkPrimaryDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
        }

        is MainAppUpdate.Downloading -> Column(
            modifier = blockModifier
                .cmpPressable(onClick = actions.onOpenPrompt, shape = BlockShape)
                .sideBlockSurface(colors.backgroundSurfaceUltraHighlight, colors.borderHighlight)
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            SideTexts(
                icon = CmpIcons.Download,
                iconTint = colors.brandPrimaryDefault,
                title = stringResource(Strings.string.main_drawer_title_app_update_downloading, update.version),
                subtitle = stringResource(
                    Strings.string.main_drawer_subtitle_app_update_downloading,
                    (update.bytes / BYTES_IN_MB).toInt(),
                ),
            )
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(6.dp))
        }

        is MainAppUpdate.Downloaded -> Column(
            modifier = blockModifier
                .sideBlockSurface(colors.brandPrimaryHighlight, colors.borderHighlight)
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            SideTexts(
                icon = CmpIcons.DownloadDone,
                iconTint = colors.foregroundOnDarkPrimaryDefault,
                title = stringResource(Strings.string.main_drawer_title_app_update_ready),
                subtitle = stringResource(Strings.string.main_drawer_subtitle_app_update_ready, update.version),
            )
            CmpButton(
                text = stringResource(Strings.string.main_drawer_btn_app_update_install),
                onClick = actions.onInstall,
                modifier = Modifier.testTag(MainUpdateTags.SIDE_INSTALL),
                size = CmpButtonSize.S,
                icon = CmpIcons.RestartAlt,
                fullWidth = true,
            )
        }

        MainAppUpdate.Installing -> Row(
            modifier = blockModifier
                .heightIn(min = CmpSize.buttonXl)
                .sideBlockSurface(colors.backgroundSurfaceUltraHighlight, colors.borderHighlight)
                .padding(horizontal = CmpSpacing.space16),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconM), strokeWidth = 2.5.dp)
            CmpText(
                text = stringResource(Strings.string.main_drawer_label_app_update_installing),
                style = CmpTextStyle.Headline3,
                color = colors.foregroundOnDarkPrimaryDefault,
            )
        }

        MainAppUpdate.None, MainAppUpdate.Checking -> Unit
    }
}

private fun Modifier.sideBlockSurface(color: Color, border: Color): Modifier =
    background(color, BlockShape).border(CmpBorderWidth.listItem, border, BlockShape)

@Composable
private fun SideTexts(icon: CmpIcons, iconTint: Color, title: String, subtitle: String) {
    val colors = CmpTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(CmpSize.iconM))
        Column {
            CmpText(text = title, style = CmpTextStyle.Headline3, color = colors.foregroundOnDarkPrimaryDefault)
            CmpText(text = subtitle, style = CmpTextStyle.Additional0, color = colors.foregroundOnDarkSecondaryDefault)
        }
    }
}

/** Телефон и планшет: закрываемая плашка в начале главного; закрытая — до следующей новой версии. */
@Composable
internal fun MainUpdateBanner(version: String, actions: MainAppUpdateActions, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.input)
            .testTag(MainUpdateTags.BANNER)
            .cmpPressable(onClick = actions.onOpenPrompt, shape = BlockShape)
            .sideBlockSurface(colors.brandPrimaryHighlight, colors.borderHighlight)
            .padding(
                start = CmpSpacing.space16,
                top = CmpSpacing.space8,
                end = CmpSpacing.space4,
                bottom = CmpSpacing.space8,
            ),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(
            icon = CmpIcons.SystemUpdate,
            contentDescription = null,
            tint = colors.brandPrimaryDefault,
            modifier = Modifier.size(CmpSize.iconL),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(
                text = stringResource(Strings.string.main_banner_title_app_update, version),
                style = CmpTextStyle.Headline2,
                color = colors.foregroundOnDarkPrimaryDefault,
            )
            CmpText(
                text = stringResource(Strings.string.main_banner_subtitle_app_update),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
        CmpIconButton(
            onClick = actions.onDismissBanner,
            icon = CmpIcons.Close,
            contentDescription = stringResource(Strings.string.main_banner_a11y_app_update_hide),
            modifier = Modifier.testTag(MainUpdateTags.BANNER_HIDE),
            tint = colors.foregroundOnDarkPrimaryDefault,
        )
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
        UpdateDot(modifier = Modifier.align(Alignment.TopEnd).padding(top = CmpSpacing.space8, end = CmpSpacing.space8))
    }
}

@Composable
internal fun MainUpdateMenuDot(modifier: Modifier = Modifier) {
    val description = stringResource(Strings.string.main_drawer_a11y_app_update_dot)
    UpdateDot(modifier = modifier.semantics { contentDescription = description }.testTag(MainUpdateTags.MENU_DOT))
}

@Composable
private fun UpdateDot(modifier: Modifier = Modifier) {
    CmpDot(modifier = modifier, size = MainHomeTokens.UpdateDot, color = CmpTheme.colors.brandPrimaryDefault)
}
