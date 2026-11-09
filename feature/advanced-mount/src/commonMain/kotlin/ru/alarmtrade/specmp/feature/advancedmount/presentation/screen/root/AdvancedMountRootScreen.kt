package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_a11y_can_hint
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_a11y_remote_sync
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_btn_fix
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_btn_go_update
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_btn_product_info
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_error_subtitle
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_error_title
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_loading
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_title
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_denied_title
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_dialog_msg_can_hint
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_dialog_msg_can_update
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_dialog_title_attention
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_dialog_title_can_hint
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_label_can_code
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_label_can_firmware
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_label_firmware
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_label_id
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_label_imei
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_label_mode
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_value_mode_boot
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_value_mode_main
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_value_transport_ble
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_header_value_transport_usb
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_root_subtitle_no_model
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_root_title_no_model
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_msg_locked
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_support_chat_unread
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_support_chat_unread
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_unsupported_subtitle
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_unsupported_title
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountError
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountExtra
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionPainter
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionSubtitle
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionTitle
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import specmp.feature.advanced_mount.generated.resources.Res
import specmp.feature.advanced_mount.generated.resources.mount_dialog_warning
import specmp.feature.advanced_mount.generated.resources.mount_remote_sync

internal enum class MountLayout { Compact, Medium, Expanded }

internal class AdvancedMountRootActions(
    val onBack: () -> Unit = {},
    val onSectionClick: (MountSection) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onCanHintClick: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onCanUpdateConfirm: () -> Unit = {},
    val onRemoteSyncClick: () -> Unit = {},
    val onProductInfoClick: () -> Unit = {},
)

internal fun AlarmDevice.title(): String = "$systemName  $systemId"

@Composable
internal fun AdvancedMountRootScreen(
    onSectionClick: (MountSection, String) -> Unit,
    onExtraClick: (MountExtra, String) -> Unit,
    onOpenSupportChat: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<AdvancedMountRootViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.sideEffect.collectSideEffect { effect ->
        val deviceTitle = viewModel.state.value.device?.title().orEmpty()
        when (effect) {
            is AdvancedMountRootEffect.OpenSection -> onSectionClick(effect.section, deviceTitle)
            AdvancedMountRootEffect.OpenSupportChat -> onOpenSupportChat()
            AdvancedMountRootEffect.OpenProductInfo -> onExtraClick(MountExtra.PRODUCT_INFO, deviceTitle)
            AdvancedMountRootEffect.OpenRemoteSync -> onExtraClick(MountExtra.REMOTE_SYNC, deviceTitle)
        }
    }

    AdvancedMountRootContent(
        state = state,
        actions = AdvancedMountRootActions(
            onBack = onBack,
            onSectionClick = viewModel::onSectionClick,
            onRetry = viewModel::onRetry,
            onCanHintClick = viewModel::onCanHintClick,
            onDialogDismiss = viewModel::onDialogDismiss,
            onCanUpdateConfirm = viewModel::onCanUpdateConfirm,
            onRemoteSyncClick = viewModel::onRemoteSyncClick,
            onProductInfoClick = viewModel::onProductInfoClick,
        ),
    )
}

@Composable
internal fun AdvancedMountRootContent(state: AdvancedMountRootState, actions: AdvancedMountRootActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.advancedmount_common_title)) },
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.advancedmount_common_a11y_back),
                    )
                },
                actions = { if (state.remoteAvailable) RemoteSyncAction(actions.onRemoteSyncClick) },
            )
        },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = when {
                maxWidth < 600.dp -> MountLayout.Compact
                maxWidth < 840.dp -> MountLayout.Medium
                else -> MountLayout.Expanded
            }
            val header: @Composable () -> Unit = {
                state.device?.let { DeviceHeaderCard(it, state, layout, actions) }
            }
            if (layout == MountLayout.Expanded) {
                CmpRow(
                    modifier = Modifier.fillMaxSize().widthIn(max = 1180.dp).padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    CmpBox(modifier = Modifier.width(340.dp).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        header()
                    }
                    CmpColumn(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        SectionsArea(state, layout, actions)
                    }
                }
            } else {
                CmpColumn(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CmpColumn(
                        modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth()
                            .padding(if (layout == MountLayout.Compact) 14.dp else 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        header()
                        SectionsArea(state, layout, actions)
                    }
                }
            }
        }
    }

    when (state.dialog) {
        MountDialog.CAN_UPDATE -> WarningDialog(
            title = stringResource(Strings.string.advancedmount_dialog_title_attention),
            text = stringResource(Strings.string.advancedmount_dialog_msg_can_update),
            confirmText = stringResource(Strings.string.advancedmount_common_btn_go_update),
            onConfirm = actions.onCanUpdateConfirm,
            dismissText = stringResource(Strings.string.advancedmount_common_btn_cancel),
            onDismiss = actions.onDialogDismiss,
        )

        MountDialog.CAN_HINT -> WarningDialog(
            title = stringResource(Strings.string.advancedmount_dialog_title_can_hint),
            text = stringResource(Strings.string.advancedmount_dialog_msg_can_hint),
            confirmText = stringResource(Strings.string.advancedmount_common_btn_ok),
            onConfirm = actions.onDialogDismiss,
            dismissText = null,
            onDismiss = actions.onDialogDismiss,
        )

        null -> Unit
    }
}

@Composable
private fun pulseAlpha(periodMillis: Int): Float {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(periodMillis / 2, easing = LinearEasing), RepeatMode.Reverse),
    )
    return alpha
}

@Composable
private fun RemoteSyncAction(onClick: () -> Unit) {
    val colors = cmpThemeColors()
    CmpBox(
        modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(
            painter = painterResource(Res.drawable.mount_remote_sync),
            contentDescription = stringResource(Strings.string.advancedmount_common_a11y_remote_sync),
            modifier = Modifier.size(30.dp).alpha(pulseAlpha(periodMillis = 1_800)),
            tint = colors.primary,
        )
    }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun DeviceHeaderCard(
    device: AlarmDevice,
    state: AdvancedMountRootState,
    layout: MountLayout,
    actions: AdvancedMountRootActions,
) {
    val colors = cmpThemeColors()
    val wide = layout == MountLayout.Expanded
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(if (wide) 24.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val tile = if (wide) 72.dp else 56.dp
                CmpSurface(
                    modifier = Modifier.size(tile),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceVariant,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                ) {
                    CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CmpIcon(
                            icon = if (state.transport ==
                                ConnectionTransport.BLE
                            ) {
                                CmpIcons.Sensors
                            } else {
                                CmpIcons.Memory
                            },
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }
                CmpColumn(modifier = Modifier.weight(1f)) {
                    CmpText(
                        text = device.systemName,
                        fontSize = if (wide) 24.sp else 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CmpText(
                        text = stringResource(Strings.string.advancedmount_header_label_id, device.systemId.toString()),
                        fontSize = 14.sp,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.transport?.let { transport ->
                    HeaderChip(
                        icon = if (transport == ConnectionTransport.BLE) CmpIcons.Bluetooth else CmpIcons.Usb,
                        text = stringResource(
                            if (transport ==
                                ConnectionTransport.BLE
                            ) {
                                Strings.string.advancedmount_header_value_transport_ble
                            } else {
                                Strings.string.advancedmount_header_value_transport_usb
                            },
                        ),
                        bg = colors.secondaryContainer,
                        fg = colors.onSecondaryContainer,
                    )
                }
                val boot = device.mode == DeviceOperationMode.BOOT
                HeaderChip(
                    icon = if (boot) CmpIcons.Download else CmpIcons.CheckCircle,
                    text = stringResource(
                        Strings.string.advancedmount_header_label_mode,
                        stringResource(
                            if (boot) {
                                Strings.string.advancedmount_header_value_mode_boot
                            } else {
                                Strings.string.advancedmount_header_value_mode_main
                            },
                        ),
                    ),
                    bg = if (boot) colors.tertiaryContainer else colors.primaryContainer,
                    fg = if (boot) colors.onTertiaryContainer else colors.onPrimaryContainer,
                )
            }
            CmpColumn {
                CmpDivider()
                HeaderRow(
                    stringResource(Strings.string.advancedmount_header_label_firmware),
                    device.displayFirmwareVersion,
                )
                device.can?.let { can ->
                    CmpDivider()
                    HeaderRow(
                        stringResource(Strings.string.advancedmount_header_label_can_firmware),
                        can.firmwareVersion,
                    )
                    CmpDivider()
                    HeaderRow(stringResource(Strings.string.advancedmount_header_label_can_code), can.code) {
                        CmpBox(
                            modifier = Modifier.size(
                                40.dp,
                            ).clip(CircleShape).clickable(onClick = actions.onCanHintClick),
                            contentAlignment = Alignment.Center,
                        ) {
                            CmpIcon(
                                icon = CmpIcons.Warning,
                                contentDescription = stringResource(Strings.string.advancedmount_common_a11y_can_hint),
                                modifier = Modifier.size(22.dp).alpha(pulseAlpha(periodMillis = 1_600)),
                                tint = colors.error,
                            )
                        }
                    }
                }
                state.imei?.let {
                    CmpDivider()
                    HeaderRow(stringResource(Strings.string.advancedmount_header_label_imei), it)
                }
            }
            if (state.productInfo) {
                CmpOutlinedButton(onClick = actions.onProductInfoClick, modifier = Modifier.fillMaxWidth()) {
                    CmpIcon(
                        icon = CmpIcons.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.primary,
                    )
                    CmpText(
                        text = stringResource(Strings.string.advancedmount_common_btn_product_info),
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Medium,
                        color = colors.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(
    icon: CmpIcons,
    text: String,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
) {
    CmpSurface(shape = CircleShape, color = bg, contentColor = fg) {
        CmpRow(
            modifier = Modifier.height(28.dp).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = fg)
            CmpText(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = fg, maxLines = 1)
        }
    }
}

@Composable
private fun HeaderRow(label: String, value: String, trailing: (@Composable () -> Unit)? = null) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(text = label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = colors.onSurfaceVariant)
        CmpText(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            textAlign = TextAlign.End,
        )
        trailing?.invoke()
    }
}

@Composable
private fun SectionsArea(state: AdvancedMountRootState, layout: MountLayout, actions: AdvancedMountRootActions) {
    when (val content = state.content) {
        LCEState.None, LCEState.Loading -> CenterBlock {
            CmpCircularProgressIndicator()
            CmpText(
                text = stringResource(Strings.string.advancedmount_common_loading),
                fontSize = 16.sp,
                color = cmpThemeColors().onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        is LCEState.Error -> CenterBlock {
            Hero(CmpIcons.WifiOff, error = false)
            CmpText(
                text = when (val error = content.error) {
                    is MountError.Common -> error.error.message()
                    MountError.VerifyFailed -> stringResource(Strings.string.advancedmount_common_error_title)
                },
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Strings.string.advancedmount_common_error_subtitle),
                fontSize = 16.sp,
                color = cmpThemeColors().onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            RefreshButton(stringResource(Strings.string.advancedmount_common_btn_fix), actions.onRetry)
        }

        is LCEState.Content -> AccessArea(content.content, state, layout, actions)
    }
}

@Composable
private fun AccessArea(
    access: MountAccess,
    state: AdvancedMountRootState,
    layout: MountLayout,
    actions: AdvancedMountRootActions,
) {
    when (access) {
        is MountAccess.Denied -> CenterBlock {
            Hero(CmpIcons.Lock, error = true)
            CmpText(
                text = stringResource(Strings.string.advancedmount_denied_title),
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
            if (access.message.isNotBlank()) {
                CmpText(
                    text = access.message,
                    fontSize = 16.sp,
                    color = cmpThemeColors().onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            RefreshButton(stringResource(Strings.string.advancedmount_common_btn_retry), actions.onRetry)
        }

        is MountAccess.Ready -> CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (access.unsupported) UnsupportedBanner(modelNotInDatabase = access.modelNotInDatabase)
            SectionsGrid(access.sections, layout, state.canLocked, state.unreadCount, actions.onSectionClick)
        }
    }
}

@Composable
private fun CenterBlock(content: @Composable () -> Unit) {
    CmpBox(
        modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp).padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}

@Composable
private fun Hero(icon: CmpIcons, error: Boolean) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.size(88.dp),
        shape = CircleShape,
        color = if (error) colors.errorContainer else colors.surfaceVariant,
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = if (error) colors.onErrorContainer else colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RefreshButton(text: String, onClick: () -> Unit) {
    CmpButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        CmpIcon(icon = CmpIcons.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        CmpText(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun UnsupportedBanner(modelNotInDatabase: Boolean) {
    val colors = cmpThemeColors()
    CmpSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.errorContainer) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = CmpIcons.Warning,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = colors.onErrorContainer,
            )
            CmpColumn {
                CmpText(
                    text = stringResource(
                        if (modelNotInDatabase) {
                            Strings.string.advancedmount_root_title_no_model
                        } else {
                            Strings.string.advancedmount_unsupported_title
                        },
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onErrorContainer,
                )
                CmpText(
                    text = stringResource(
                        if (modelNotInDatabase) {
                            Strings.string.advancedmount_root_subtitle_no_model
                        } else {
                            Strings.string.advancedmount_unsupported_subtitle
                        },
                    ),
                    fontSize = 14.sp,
                    color = colors.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun SectionsGrid(
    sections: List<MountSection>,
    layout: MountLayout,
    canLocked: Boolean,
    unreadCount: Int,
    onClick: (MountSection) -> Unit,
) {
    val columns = if (layout == MountLayout.Compact) 1 else 2
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.chunked(columns).forEachIndexed { rowIndex, row ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEachIndexed { columnIndex, section ->
                    SectionTile(
                        section = section,
                        locked = canLocked && section.needsCan,
                        unreadCount = if (section == MountSection.SUPPORT_CHAT) unreadCount else 0,
                        big = layout != MountLayout.Compact,
                        appearIndex = rowIndex * columns + columnIndex,
                        onClick = { onClick(section) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                repeat(columns - row.size) { CmpBox(modifier = Modifier.weight(1f)) {} }
            }
        }
    }
}

private const val FIRMWARE_CHECK_TAG = "advanced_mount_firmware_check"

@Composable
private fun SectionTile(
    section: MountSection,
    locked: Boolean,
    unreadCount: Int,
    big: Boolean,
    appearIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(durationMillis = 260, delayMillis = appearIndex * 35)) }
    val shape = RoundedCornerShape(18.dp)
    CmpSurface(
        modifier = modifier
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 6.dp.toPx()
            }
            .then(if (section == MountSection.FIRMWARE_UPDATE) Modifier.testTag(FIRMWARE_CHECK_TAG) else Modifier)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = if (big) 96.dp else 84.dp)
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionIconBox(section, locked, unreadCount)
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val unread = section == MountSection.SUPPORT_CHAT && unreadCount > 0
                CmpText(
                    text = if (unread) {
                        stringResource(
                            Strings.string.advancedmount_sections_title_support_chat_unread,
                        )
                    } else {
                        sectionTitle(section)
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (locked) {
                    CmpText(
                        text = stringResource(Strings.string.advancedmount_sections_msg_locked),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onTertiaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    CmpText(
                        text = if (unread) {
                            stringResource(
                                Strings.string.advancedmount_sections_subtitle_support_chat_unread,
                            )
                        } else {
                            sectionSubtitle(section)
                        },
                        fontSize = 14.sp,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            CmpIcon(icon = CmpIcons.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionIconBox(section: MountSection, locked: Boolean, unreadCount: Int) {
    val colors = cmpThemeColors()
    CmpBox(modifier = Modifier.size(52.dp)) {
        CmpSurface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(14.dp),
            color = if (locked) colors.tertiaryContainer else colors.primaryContainer,
        ) {
            CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CmpIcon(
                    painter = sectionPainter(section),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (locked) colors.onTertiaryContainer else colors.onPrimaryContainer,
                )
            }
        }
        if (locked) {
            Badge(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = 7.dp, y = 7.dp).size(26.dp),
                color = colors.tertiary,
            ) {
                CmpIcon(
                    icon = CmpIcons.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = colors.onTertiary,
                )
            }
        }
        if (unreadCount > 0) {
            Badge(
                modifier = Modifier.align(
                    Alignment.TopStart,
                ).offset(x = (-6).dp, y = (-6).dp).heightIn(min = 24.dp).widthIn(min = 24.dp),
                color = colors.error,
            ) {
                CmpText(
                    text = unreadCount.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onError,
                )
            }
        }
    }
}

@Composable
private fun Badge(modifier: Modifier, color: androidx.compose.ui.graphics.Color, content: @Composable () -> Unit) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier,
        shape = CircleShape,
        color = color,
        border = BorderStroke(2.dp, colors.surfaceContainerLow),
    ) {
        CmpBox(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun WarningDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String?,
    onDismiss: () -> Unit,
) {
    val colors = cmpThemeColors()
    CmpDialog(
        onDismissRequest = onDismiss,
        title = {
            CmpColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CmpIcon(
                    painter = painterResource(Res.drawable.mount_dialog_warning),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = colors.error,
                )
                CmpText(text = title)
            }
        },
        text = { CmpText(text = text) },
        confirmButton = { CmpTextButton(text = confirmText, onClick = onConfirm) },
        dismissButton = dismissText?.let { { CmpTextButton(text = it, onClick = onDismiss) } },
    )
}
