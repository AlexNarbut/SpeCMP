package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_btn_logout
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_error_logout_failed
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_app_update_check
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_app_update_checking
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_database_update
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_database_updating
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_database_version
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_email_missing
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_name_missing
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_profile
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_settings
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_theme
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_version
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_subtitle_app_update_available
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_title_app_update_available
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.lce.isContent
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.layout.CmpSpacer
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.presentation.model.MainError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenState
import specmp.feature.main.generated.resources.Res
import specmp.feature.main.generated.resources.app_settings
import specmp.feature.main.generated.resources.app_update

@Suppress("LongMethod")
@Composable
internal fun MainHomeDrawer(
    state: MainScreenState,
    isDark: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onDatabaseSyncClick: () -> Unit,
    onAppUpdateCheckClick: () -> Unit,
    onAppUpdateOpenClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    colors: CmpThemeColors = cmpThemeColors()
) {
    val dividerColor = colors.outlineVariant

    CmpSurface(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(max = 320.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = dividerColor,
                    start = Offset(size.width - strokeWidth / 2f, 0f),
                    end = Offset(size.width - strokeWidth / 2f, size.height),
                    strokeWidth = strokeWidth
                )
            },
        color = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(contentPadding)
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 16.dp)
        ) {
            MainProfileBlock(
                profile = state.profile,
                colors = colors
            )

            CmpDivider(color = colors.outlineVariant)

            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MainDrawerRow(
                    icon = if (isDark) CmpIcons.DarkMode else CmpIcons.LightMode,
                    label = stringResource(Strings.string.main_drawer_label_theme),
                    colors = colors,
                    trailing = {
                        CmpSwitch(
                            checked = isDark,
                            onCheckedChange = onThemeChange
                        )
                    },
                    onClick = { onThemeChange(!isDark) }
                )

                MainDrawerRow(
                    icon = CmpIcons.Refresh,
                    label = if (state.isDatabaseSyncing) {
                        stringResource(Strings.string.main_drawer_label_database_updating)
                    } else {
                        stringResource(Strings.string.main_drawer_label_database_update)
                    },
                    colors = colors,
                    trailing = {
                        if (state.isDatabaseSyncing) {
                            CmpCircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = colors.primary
                            )
                        }
                    },
                    enabled = !state.isDatabaseSyncing,
                    onClick = onDatabaseSyncClick
                )

                if (state.isAppUpdateSupported) {
                    MainDrawerRow(
                        iconPainter = painterResource(Res.drawable.app_update),
                        label = if (state.isAppUpdateChecking) {
                            stringResource(Strings.string.main_drawer_label_app_update_checking)
                        } else {
                            stringResource(Strings.string.main_drawer_label_app_update_check)
                        },
                        colors = colors,
                        trailing = {
                            if (state.isAppUpdateChecking) {
                                CmpCircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = colors.primary
                                )
                            }
                        },
                        enabled = !state.isAppUpdateChecking,
                        onClick = onAppUpdateCheckClick
                    )
                }

                MainDrawerRow(
                    iconPainter = painterResource(Res.drawable.app_settings),
                    label = stringResource(Strings.string.main_drawer_label_settings),
                    colors = colors,
                    onClick = onSettingsClick
                )

                MainDrawerRow(
                    icon = CmpIcons.ExitToApp,
                    label = stringResource(Strings.string.main_drawer_btn_logout),
                    colors = colors,
                    trailing = {
                        if (state.isLogoutInProgress) {
                            CmpCircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = colors.primary
                            )
                        }
                    },
                    enabled = !state.isLogoutInProgress,
                    onClick = onLogout
                )
            }

            state.logoutError?.let { error ->
                MainLogoutErrorMessage(
                    error = error,
                    colors = colors,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            CmpSpacer(modifier = Modifier.weight(1f))

            MainDrawerFooter(
                state = state,
                colors = colors,
                onAppUpdateOpenClick = onAppUpdateOpenClick
            )
        }
    }
}

@Composable
private fun MainProfileBlock(profile: MainProfile, colors: CmpThemeColors, modifier: Modifier = Modifier) {
    val name = profile.name.trim()
    val email = profile.email.trim()
    val initials = mainProfileInitials(name)
    val fallbackProfile = stringResource(Strings.string.main_drawer_label_profile)
    val nameMissing = stringResource(Strings.string.main_drawer_label_name_missing)
    val emailMissing = stringResource(Strings.string.main_drawer_label_email_missing)
    val primary = when {
        name.isNotBlank() -> name
        email.isNotBlank() -> email
        else -> fallbackProfile
    }
    val secondary = if (name.isBlank()) {
        nameMissing
    } else {
        email.ifBlank { emailMissing }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CmpSurface(
                modifier = Modifier.matchParentSize(),
                color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (initials.isBlank()) {
                        CmpIcon(
                            icon = CmpIcons.Person,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = colors.onPrimaryContainer
                        )
                    } else {
                        CmpText(
                            text = initials,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CmpText(
                text = primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            CmpText(
                text = secondary,
                fontSize = 14.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MainDrawerRow(
    label: String,
    colors: CmpThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    iconPainter: Painter? = null,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit = {}
) {
    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(MainHomeTokens.CardRadius),
        color = colors.surface,
        contentColor = colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                iconPainter != null -> {
                    CmpIcon(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colors.onSurfaceVariant
                    )
                }

                icon != null -> {
                    CmpIcon(
                        icon = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colors.onSurfaceVariant
                    )
                }
            }
            CmpText(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = colors.onSurface
            )
            trailing()
        }
    }
}

@Composable
private fun MainDrawerFooter(
    state: MainScreenState,
    colors: CmpThemeColors,
    onAppUpdateOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val availableAppVersion = state.appUpdateCheckResult as? MainAppUpdateCheckResult.Available
        if (
            state.isAppUpdateSupported &&
            state.appUpdateState.isContent &&
            availableAppVersion != null
        ) {
            MainAppUpdateBanner(
                version = availableAppVersion.versionName,
                colors = colors,
                onClick = onAppUpdateOpenClick
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CmpText(
                text = stringResource(Strings.string.main_drawer_label_version, state.appVersionName),
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )
            state.databaseVersion?.let { databaseVersion ->
                CmpText(
                    text = stringResource(
                        Strings.string.main_drawer_label_database_version,
                        databaseVersion.toString()
                    ),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MainAppUpdateBanner(
    version: String,
    colors: CmpThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MainHomeTokens.CardRadius),
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CmpIcon(
                painter = painterResource(Res.drawable.app_update),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colors.onPrimaryContainer
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                CmpText(
                    text = stringResource(Strings.string.main_drawer_title_app_update_available),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onPrimaryContainer
                )
                CmpText(
                    text = stringResource(
                        Strings.string.main_drawer_subtitle_app_update_available,
                        version
                    ),
                    fontSize = 12.sp,
                    color = colors.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = colors.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MainLogoutErrorMessage(error: MainError, colors: CmpThemeColors, modifier: Modifier = Modifier) {
    val text = when (error) {
        is MainError.Common -> error.error.message()
        MainError.Failed -> stringResource(Strings.string.main_drawer_error_logout_failed)
    }

    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MainHomeTokens.CardRadius),
        color = colors.errorContainer,
        contentColor = colors.onErrorContainer
    ) {
        CmpText(
            text = text,
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
            color = colors.onErrorContainer
        )
    }
}

private fun mainProfileInitials(name: String): String = name
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
    .joinToString("")
