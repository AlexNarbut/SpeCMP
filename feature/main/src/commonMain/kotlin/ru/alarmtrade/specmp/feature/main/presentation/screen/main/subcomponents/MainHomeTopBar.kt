package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.main_common_a11y_open_menu
import ru.alarmtrade.specmp.core.localization.resources.main_common_loading_database_applying
import ru.alarmtrade.specmp.core.localization.resources.main_common_loading_database_checking
import ru.alarmtrade.specmp.core.localization.resources.main_common_loading_database_downloading
import ru.alarmtrade.specmp.core.localization.resources.main_common_status_database_app_update_required
import ru.alarmtrade.specmp.core.localization.resources.main_common_status_database_update_failed
import ru.alarmtrade.specmp.core.localization.resources.main_common_status_database_updated
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_app
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBarStyle
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.main.presentation.model.MainDatabaseSyncBadge

@Composable
internal fun MainHomeTopBar(
    isCompact: Boolean,
    isLoading: Boolean,
    databaseSyncBadge: MainDatabaseSyncBadge,
    onMenuClick: () -> Unit,
    onRequiredUpdateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpTopAppBar(
        title = stringResource(Strings.string.main_main_title_app),
        modifier = modifier
            .fillMaxWidth()
            .height(CmpSize.topbar),
        navigationIcon = {
            CmpIconButton(
                onClick = onMenuClick,
                icon = CmpIcons.Menu,
                contentDescription = stringResource(Strings.string.main_common_a11y_open_menu),
                enabled = !isLoading,
            )
        },
        actions = {
            if (!isLoading && databaseSyncBadge == MainDatabaseSyncBadge.AppUpdateRequired) {
                MainRequiredUpdateButton(onClick = onRequiredUpdateClick)
            }
            if (!isLoading) {
                MainDatabaseStatusChip(
                    isCompact = isCompact,
                    badge = databaseSyncBadge,
                )
            }
        },
        style = CmpTopAppBarStyle.CenterAligned,
    )
}

@Composable
private fun MainDatabaseStatusChip(isCompact: Boolean, badge: MainDatabaseSyncBadge, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val pill = RoundedCornerShape(CmpRadius.round)
    val chipModifier = if (isCompact) {
        modifier.size(CmpSize.touchMin)
    } else {
        modifier
            .height(CmpSize.touchMin)
            .background(colors.backgroundSurfaceHighlight, pill)
            .padding(start = CmpSpacing.space12, end = CmpSpacing.space16)
    }
    val label = badge.label()
    val iconSize = CmpSize.iconM

    Row(
        modifier = chipModifier.clip(pill),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (badge) {
            MainDatabaseSyncBadge.CheckingVersion,
            MainDatabaseSyncBadge.Downloading,
            MainDatabaseSyncBadge.Applying,
            -> CmpCircularProgressIndicator(
                modifier = Modifier.size(iconSize),
                strokeWidth = 2.5.dp,
                color = colors.brandPrimaryDefault,
            )

            MainDatabaseSyncBadge.Updated -> CmpIcon(
                icon = CmpIcons.CheckCircle,
                contentDescription = if (isCompact) label else null,
                modifier = Modifier.size(iconSize),
                tint = colors.successDefault,
            )

            is MainDatabaseSyncBadge.UpdateFailed,
            MainDatabaseSyncBadge.AppUpdateRequired,
            -> CmpIcon(
                icon = CmpIcons.Warning,
                contentDescription = if (isCompact) label else null,
                modifier = Modifier.size(iconSize),
                tint = colors.errorDefault,
            )
        }

        if (!isCompact) {
            CmpText(
                text = label,
                style = CmpTextStyle.Headline3,
                color = colors.foregroundOnDarkSecondaryDefault,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MainDatabaseSyncBadge.label(): String = when (this) {
    MainDatabaseSyncBadge.CheckingVersion -> stringResource(Strings.string.main_common_loading_database_checking)

    MainDatabaseSyncBadge.Downloading -> stringResource(Strings.string.main_common_loading_database_downloading)

    MainDatabaseSyncBadge.Applying -> stringResource(Strings.string.main_common_loading_database_applying)

    MainDatabaseSyncBadge.Updated -> stringResource(Strings.string.main_common_status_database_updated)

    is MainDatabaseSyncBadge.UpdateFailed ->
        error?.message() ?: stringResource(Strings.string.main_common_status_database_update_failed)

    MainDatabaseSyncBadge.AppUpdateRequired -> stringResource(
        Strings.string.main_common_status_database_app_update_required,
    )
}
