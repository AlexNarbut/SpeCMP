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
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBarStyle
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import specmp.feature.main.generated.resources.Res
import specmp.feature.main.generated.resources.main_common_a11y_database_updated
import specmp.feature.main.generated.resources.main_common_a11y_open_menu
import specmp.feature.main.generated.resources.main_common_loading_database_update
import specmp.feature.main.generated.resources.main_common_status_database_updated
import specmp.feature.main.generated.resources.main_main_title_app

@Composable
internal fun MainHomeTopBar(
    isCompact: Boolean,
    isLoading: Boolean,
    isDatabaseSyncing: Boolean,
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
                text = stringResource(Res.string.main_main_title_app),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground,
            )
        },
        navigationIcon = {
            CmpIconButton(
                onClick = onMenuClick,
                icon = CmpIcons.Menu,
                contentDescription = stringResource(Res.string.main_common_a11y_open_menu),
                enabled = !isLoading,
            )
        },
        actions = {
            if (!isLoading) {
                MainDatabaseStatusChip(
                    isCompact = isCompact,
                    isDatabaseSyncing = isDatabaseSyncing,
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
    isDatabaseSyncing: Boolean,
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

    Row(
        modifier = chipModifier.clip(CircleShape),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isDatabaseSyncing) {
            CmpCircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.5.dp,
                color = colors.primary,
            )
            if (!isCompact) {
                CmpText(
                    text = stringResource(Res.string.main_common_loading_database_update),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurfaceVariant,
                )
            }
        } else {
            CmpIcon(
                icon = CmpIcons.CheckCircle,
                contentDescription = if (isCompact) {
                    stringResource(Res.string.main_common_a11y_database_updated)
                } else {
                    null
                },
                modifier = Modifier.size(if (isCompact) 22.dp else 18.dp),
                tint = MainHomeTokens.Success,
            )
            if (!isCompact) {
                CmpText(
                    text = stringResource(Res.string.main_common_status_database_updated),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}
