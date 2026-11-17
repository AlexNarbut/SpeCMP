package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.main_common_status_database_update_failed
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_btn_logout
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_btn_refresh_profile
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_error_logout_failed
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_error_profile_refresh
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_app_update_check
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_app_update_checking
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_app_update_menu
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_database_update
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_database_updating
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_database_version
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_email_missing
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_name_missing
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_profile
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_settings
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_theme
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_label_version
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.main.presentation.model.MainError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainAppUpdateActions
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
    appUpdateActions: MainAppUpdateActions,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onUserDataRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    colors: CmpColors = CmpTheme.colors,
) {
    val dividerColor = colors.borderHighlight

    CmpSurface(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(max = MainHomeTokens.DrawerModalWidth)
            .drawBehind {
                val strokeWidth = CmpBorderWidth.listItem.toPx()
                drawLine(
                    color = dividerColor,
                    start = Offset(size.width - strokeWidth / 2f, 0f),
                    end = Offset(size.width - strokeWidth / 2f, size.height),
                    strokeWidth = strokeWidth,
                )
            },
        color = colors.backgroundPagePrimary,
        contentColor = colors.foregroundOnDarkPrimaryDefault,
    ) {
        // На низком экране (iPhone в ландшафте, крупный шрифт) пункты не должны уходить за край: колонка
        // прокручивается, а подвал прижат вниз за счёт минимальной высоты и SpaceBetween вместо weight.
        BoxWithConstraints(modifier = Modifier.fillMaxHeight().padding(contentPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(
                        start = CmpSpacing.space16,
                        top = CmpSpacing.space20,
                        end = CmpSpacing.space16,
                        bottom = CmpSpacing.space16,
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    MainDrawerMenu(
                        state = state,
                        isDark = isDark,
                        colors = colors,
                        onThemeChange = onThemeChange,
                        onDatabaseSyncClick = onDatabaseSyncClick,
                        onAppUpdateCheckClick = appUpdateActions.onOpenFromMenu,
                        onSettingsClick = onSettingsClick,
                        onLogout = onLogout,
                        onUserDataRefreshClick = onUserDataRefreshClick,
                    )
                }

                MainDrawerFooter(
                    state = state,
                    colors = colors,
                    appUpdateActions = appUpdateActions,
                    modifier = Modifier.padding(top = CmpSpacing.space16),
                )
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun MainDrawerMenu(
    state: MainScreenState,
    isDark: Boolean,
    colors: CmpColors,
    onThemeChange: (Boolean) -> Unit,
    onDatabaseSyncClick: () -> Unit,
    onAppUpdateCheckClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onUserDataRefreshClick: () -> Unit,
) {
    Column {
        MainProfileBlock(
            profile = state.profile,
            isRefreshing = state.isUserDataRefreshing,
            colors = colors,
            onRefreshClick = onUserDataRefreshClick,
        )

        state.userDataError?.let { error ->
            MainDrawerErrorMessage(
                text = error.text(Strings.string.main_drawer_error_profile_refresh),
                colors = colors,
                modifier = Modifier.padding(bottom = CmpSpacing.space12),
            )
        }

        CmpDivider(color = colors.borderHighlight)

        Column(
            modifier = Modifier.padding(top = CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            MainDrawerRow(
                icon = if (isDark) CmpIcons.DarkMode else CmpIcons.LightMode,
                label = stringResource(Strings.string.main_drawer_label_theme),
                colors = colors,
                trailing = {
                    CmpSwitch(
                        checked = isDark,
                        onCheckedChange = onThemeChange,
                    )
                },
                onClick = { onThemeChange(!isDark) },
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
                            modifier = Modifier.size(CmpSize.iconM),
                            strokeWidth = 2.5.dp,
                            color = colors.brandPrimaryDefault,
                        )
                    }
                },
                enabled = !state.isDatabaseSyncing,
                onClick = onDatabaseSyncClick,
            )

            (state.databaseSyncState as? LCEState.Error)?.let { failed ->
                MainDrawerErrorMessage(
                    text = failed.error.text(Strings.string.main_common_status_database_update_failed),
                    colors = colors,
                )
            }

            MainDrawerRow(
                iconPainter = painterResource(Res.drawable.app_update),
                label = when {
                    state.isAppUpdateChecking -> stringResource(Strings.string.main_drawer_label_app_update_checking)
                    state.appUpdate.installsItself -> stringResource(Strings.string.main_drawer_label_app_update_check)
                    else -> stringResource(Strings.string.main_drawer_label_app_update_menu)
                },
                colors = colors,
                trailing = {
                    if (state.isAppUpdateChecking) {
                        CmpCircularProgressIndicator(
                            modifier = Modifier.size(CmpSize.iconM),
                            strokeWidth = 2.5.dp,
                            color = colors.brandPrimaryDefault,
                        )
                    } else if (!state.appUpdate.installsItself && state.availableUpdateVersion != null) {
                        MainUpdateMenuDot()
                    }
                },
                onClick = onAppUpdateCheckClick,
            )

            MainDrawerRow(
                iconPainter = painterResource(Res.drawable.app_settings),
                label = stringResource(Strings.string.main_drawer_label_settings),
                colors = colors,
                onClick = onSettingsClick,
            )

            MainDrawerRow(
                icon = CmpIcons.ExitToApp,
                label = stringResource(Strings.string.main_drawer_btn_logout),
                colors = colors,
                trailing = {
                    if (state.isLogoutInProgress) {
                        CmpCircularProgressIndicator(
                            modifier = Modifier.size(CmpSize.iconM),
                            strokeWidth = 2.5.dp,
                            color = colors.brandPrimaryDefault,
                        )
                    }
                },
                enabled = !state.isLogoutInProgress,
                onClick = onLogout,
            )
        }

        state.logoutError?.let { error ->
            MainDrawerErrorMessage(
                text = error.text(Strings.string.main_drawer_error_logout_failed),
                colors = colors,
                modifier = Modifier.padding(top = CmpSpacing.space12),
            )
        }
    }
}

@Composable
private fun MainProfileBlock(
    profile: MainProfile,
    isRefreshing: Boolean,
    colors: CmpColors,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            .padding(start = CmpSpacing.space4, end = CmpSpacing.space4, bottom = CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(MainHomeTokens.ProfileAvatar)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpSurface(
                modifier = Modifier.matchParentSize(),
                color = colors.brandPrimaryHighlight,
                contentColor = colors.foregroundOnDarkPrimaryDefault,
                shape = CircleShape,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (initials.isBlank()) {
                        CmpIcon(
                            icon = CmpIcons.Person,
                            contentDescription = null,
                            modifier = Modifier.size(CmpSize.iconL),
                            tint = colors.foregroundOnDarkPrimaryDefault,
                        )
                    } else {
                        CmpText(
                            text = initials,
                            style = CmpTextStyle.Headline3,
                            color = colors.foregroundOnDarkPrimaryDefault,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            CmpText(
                text = primary,
                style = CmpTextStyle.Headline2,
                color = colors.foregroundOnDarkPrimaryDefault,
            )
            CmpText(
                text = secondary,
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }

        Box(modifier = Modifier.size(CmpSize.touchMin), contentAlignment = Alignment.Center) {
            if (isRefreshing) {
                CmpCircularProgressIndicator(
                    modifier = Modifier.size(CmpSize.iconM),
                    strokeWidth = 2.5.dp,
                    color = colors.brandPrimaryDefault,
                )
            } else {
                CmpIconButton(
                    onClick = onRefreshClick,
                    icon = CmpIcons.Refresh,
                    contentDescription = stringResource(Strings.string.main_drawer_btn_refresh_profile),
                    tint = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
    }
}

@Composable
private fun MainDrawerRow(
    label: String,
    colors: CmpColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    iconPainter: Painter? = null,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit = {},
) {
    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.buttonL),
        enabled = enabled,
        shape = RoundedCornerShape(CmpRadius.listItem),
        color = Color.Transparent,
        contentColor = colors.foregroundOnDarkPrimaryDefault,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                iconPainter != null -> {
                    CmpIcon(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconM),
                        tint = colors.foregroundOnDarkSecondaryDefault,
                    )
                }

                icon != null -> {
                    CmpIcon(
                        icon = icon,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconM),
                        tint = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
            CmpText(
                text = label,
                style = CmpTextStyle.Paragraph1,
                modifier = Modifier.weight(1f),
                color = colors.foregroundOnDarkPrimaryDefault,
            )
            trailing()
        }
    }
}

@Composable
private fun MainDrawerFooter(
    state: MainScreenState,
    colors: CmpColors,
    appUpdateActions: MainAppUpdateActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        if (state.appUpdate.installsItself) {
            MainSideUpdateBlock(
                update = state.appUpdate.update,
                actions = appUpdateActions,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            CmpText(
                text = stringResource(Strings.string.main_drawer_label_version, state.appVersionName),
                style = CmpTextStyle.Additional1,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
            state.databaseVersion?.let { databaseVersion ->
                CmpText(
                    text = stringResource(
                        Strings.string.main_drawer_label_database_version,
                        databaseVersion.toString(),
                    ),
                    style = CmpTextStyle.Additional1,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
    }
}

@Composable
private fun MainError.text(fallback: StringResource): String = when (this) {
    is MainError.Common -> error.message()
    MainError.Failed -> stringResource(fallback)
}

@Composable
private fun MainDrawerErrorMessage(text: String, colors: CmpColors, modifier: Modifier = Modifier) {
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        color = colors.errorHighlight,
        contentColor = colors.foregroundOnDarkPrimaryDefault,
    ) {
        CmpText(
            text = text,
            style = CmpTextStyle.Paragraph3,
            modifier = Modifier.padding(CmpSpacing.space12),
            color = colors.foregroundOnDarkPrimaryDefault,
        )
    }
}

private fun mainProfileInitials(name: String): String = name
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
    .joinToString("")
