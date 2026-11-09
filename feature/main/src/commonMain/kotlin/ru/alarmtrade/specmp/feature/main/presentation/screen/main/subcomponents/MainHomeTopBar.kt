package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBarStyle
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.main.presentation.model.MainDatabaseSyncBadge

@Composable
internal fun MainHomeTopBar(
    isCompact: Boolean,
    isLoading: Boolean,
    databaseSyncBadge: MainDatabaseSyncBadge,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: CmpThemeColors = cmpThemeColors(),
) {
    CmpTopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        title = {
            CmpText(
                text = stringResource(Strings.string.main_main_title_app),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground,
            )
        },
        navigationIcon = {
            CmpIconButton(
                onClick = onMenuClick,
                icon = CmpIcons.Menu,
                contentDescription = stringResource(Strings.string.main_common_a11y_open_menu),
                enabled = !isLoading,
            )
        },
        actions = {
            if (!isLoading) {
                MainDatabaseStatusChip(
                    isCompact = isCompact,
                    badge = databaseSyncBadge,
                    colors = colors,
                )
            }
        },
        containerColor = Color.Transparent,
        contentColor = colors.onBackground,
        style = CmpTopAppBarStyle.CenterAligned,
    )
}

@Composable
private fun MainDatabaseStatusChip(
    isCompact: Boolean,
    badge: MainDatabaseSyncBadge,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    val chipModifier = if (isCompact) {
        modifier.size(40.dp)
    } else {
        modifier
            .height(40.dp)
            .background(colors.surfaceVariant, CircleShape)
            .padding(start = 10.dp, end = 14.dp)
    }
    val label = badge.label()

    Row(
        modifier = chipModifier.clip(CircleShape),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (badge) {
            MainDatabaseSyncBadge.CheckingVersion,
            MainDatabaseSyncBadge.Downloading,
            MainDatabaseSyncBadge.Applying,
            -> CmpCircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.5.dp,
                color = colors.primary,
            )

            MainDatabaseSyncBadge.Updated -> CmpIcon(
                icon = CmpIcons.CheckCircle,
                contentDescription = if (isCompact) label else null,
                modifier = Modifier.size(if (isCompact) 22.dp else 18.dp),
                tint = MainHomeTokens.Success,
            )

            is MainDatabaseSyncBadge.UpdateFailed,
            MainDatabaseSyncBadge.AppUpdateRequired,
            -> CmpIcon(
                icon = CmpIcons.Warning,
                contentDescription = if (isCompact) label else null,
                modifier = Modifier.size(if (isCompact) 22.dp else 18.dp),
                tint = colors.error,
            )
        }

        if (!isCompact) {
            CmpText(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
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
