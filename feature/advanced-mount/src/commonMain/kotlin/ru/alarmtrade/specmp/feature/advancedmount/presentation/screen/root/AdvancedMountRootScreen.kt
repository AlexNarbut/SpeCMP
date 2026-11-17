package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_btn_product_info_short
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
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_unsupported_subtitle
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_unsupported_title
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButtonVariant
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountError
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountExtra
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
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

private val ContentMaxWidth = 760.dp
private val ExpandedMaxWidth = 1180.dp
private val ExpandedHeaderWidth = 340.dp
private val ExpandedDeviceTile = 72.dp
private val ProductTileWidth = 64.dp

@Composable
internal fun AdvancedMountRootScreen(
    onSectionClick: (MountSection, String) -> Unit,
    onExtraClick: (MountExtra, String) -> Unit,
    onOpenSupportChat: () -> Unit,
    onOpenProductInfo: (systemId: Long) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<AdvancedMountRootViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.sideEffect.collectSideEffect { effect ->
        val deviceTitle = viewModel.state.value.device?.title().orEmpty()
        when (effect) {
            is AdvancedMountRootEffect.OpenSection -> onSectionClick(effect.section, deviceTitle)
            AdvancedMountRootEffect.OpenSupportChat -> onOpenSupportChat()
            is AdvancedMountRootEffect.OpenProductInfo -> onOpenProductInfo(effect.systemId)
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
                title = stringResource(Strings.string.advancedmount_common_title),
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize().horizontalInsets(padding)) {
            val layout = when {
                maxWidth < CmpBreakpoints.Medium -> MountLayout.Compact
                maxWidth < CmpBreakpoints.Expanded -> MountLayout.Medium
                else -> MountLayout.Expanded
            }
            val side = if (layout == MountLayout.Compact) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium
            val scrollPadding = Modifier.padding(
                top = padding.calculateTopPadding() + CmpSpacing.screenPaddingTop,
                bottom = padding.calculateBottomPadding() + CmpSpacing.screenPaddingBottom,
            )
            val header: @Composable () -> Unit = {
                state.device?.let { DeviceHeaderCard(it, state, layout, actions) }
            }
            if (layout == MountLayout.Expanded) {
                CmpRow(
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxHeight().widthIn(max = ExpandedMaxWidth)
                        .fillMaxWidth().padding(horizontal = side),
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24),
                ) {
                    CmpBox(
                        modifier = Modifier.width(ExpandedHeaderWidth).fillMaxHeight()
                            .verticalScroll(rememberScrollState()).then(scrollPadding),
                    ) {
                        header()
                    }
                    CmpColumn(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .verticalScroll(rememberScrollState()).then(scrollPadding),
                    ) {
                        SectionsArea(state, layout, actions)
                    }
                }
            } else {
                CmpColumn(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CmpColumn(
                        modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth()
                            .then(scrollPadding).padding(horizontal = side),
                        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
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
private fun Modifier.horizontalInsets(padding: PaddingValues): Modifier {
    val direction = LocalLayoutDirection.current
    return padding(start = padding.calculateStartPadding(direction), end = padding.calculateEndPadding(direction))
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
    CmpIconButton(
        onClick = onClick,
        icon = painterResource(Res.drawable.mount_remote_sync),
        contentDescription = stringResource(Strings.string.advancedmount_common_a11y_remote_sync),
        variant = CmpIconButtonVariant.Glass,
        tint = CmpTheme.colors.brandPrimaryDefault.copy(alpha = pulseAlpha(periodMillis = 1_800)),
    )
}

@Composable
private fun DeviceHeaderCard(
    device: AlarmDevice,
    state: AdvancedMountRootState,
    layout: MountLayout,
    actions: AdvancedMountRootActions,
) {
    val wide = layout == MountLayout.Expanded
    CmpCard(modifier = Modifier.fillMaxWidth()) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(if (wide) CmpSpacing.space24 else CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DeviceTile(transport = state.transport, size = if (wide) ExpandedDeviceTile else CmpSize.iconXxl)
                CmpColumn(modifier = Modifier.weight(1f)) {
                    CmpText(
                        text = device.systemName,
                        style = if (wide) CmpTextStyle.Headline1 else CmpTextStyle.Headline2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CmpText(
                        text = stringResource(Strings.string.advancedmount_header_label_id, device.systemId.toString()),
                        style = CmpTextStyle.Additional0,
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                if (state.productInfo) {
                    ProductInfoButton(tile = layout != MountLayout.Medium, onClick = actions.onProductInfoClick)
                }
            }
            HeaderTags(device, state.transport)
            HeaderRows(device, state.imei, actions.onCanHintClick)
        }
    }
}

@Composable
private fun DeviceTile(transport: ConnectionTransport?, size: Dp) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpCard(
        modifier = Modifier.size(size),
        shape = shape,
        containerColor = colors.backgroundSurfaceHighlight,
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = if (transport == ConnectionTransport.BLE) CmpIcons.Sensors else CmpIcons.Memory,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconL),
                tint = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun HeaderTags(device: AlarmDevice, transport: ConnectionTransport?) {
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        transport?.let {
            val ble = it == ConnectionTransport.BLE
            CmpTag(
                text = stringResource(
                    if (ble) {
                        Strings.string.advancedmount_header_value_transport_ble
                    } else {
                        Strings.string.advancedmount_header_value_transport_usb
                    },
                ),
                icon = if (ble) CmpIcons.Bluetooth else CmpIcons.Usb,
            )
        }
        val boot = device.mode == DeviceOperationMode.BOOT
        CmpTag(
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
            tone = if (boot) CmpTagTone.Warning else CmpTagTone.Success,
            icon = if (boot) CmpIcons.Download else CmpIcons.CheckCircle,
        )
    }
}

@Composable
private fun HeaderRows(device: AlarmDevice, imei: String?, onCanHintClick: () -> Unit) {
    CmpColumn {
        CmpDivider()
        HeaderRow(stringResource(Strings.string.advancedmount_header_label_firmware), device.displayFirmwareVersion)
        device.can?.let { can ->
            CmpDivider()
            HeaderRow(stringResource(Strings.string.advancedmount_header_label_can_firmware), can.firmwareVersion)
            CmpDivider()
            HeaderRow(stringResource(Strings.string.advancedmount_header_label_can_code), can.code) {
                CmpIconButton(
                    onClick = onCanHintClick,
                    icon = CmpIcons.Warning,
                    contentDescription = stringResource(Strings.string.advancedmount_common_a11y_can_hint),
                    tint = CmpTheme.colors.errorDefault.copy(alpha = pulseAlpha(periodMillis = 1_600)),
                )
            }
        }
        imei?.let {
            CmpDivider()
            HeaderRow(stringResource(Strings.string.advancedmount_header_label_imei), it)
        }
    }
}

@Composable
private fun HeaderRow(label: String, value: String, trailing: (@Composable () -> Unit)? = null) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = label,
            style = CmpTextStyle.Paragraph2,
            modifier = Modifier.weight(1f),
            color = colors.foregroundOnDarkSecondaryDefault,
        )
        CmpText(text = value, style = CmpTextStyle.Headline3, textAlign = TextAlign.End)
        trailing?.invoke()
    }
}

@Composable
private fun SectionsArea(state: AdvancedMountRootState, layout: MountLayout, actions: AdvancedMountRootActions) {
    when (val content = state.content) {
        LCEState.None, LCEState.Loading -> LoadingBlock()

        is LCEState.Error -> CmpStateBox(
            icon = CmpIcons.WifiOff,
            title = when (val error = content.error) {
                is MountError.Common -> error.error.message()
                MountError.VerifyFailed -> stringResource(Strings.string.advancedmount_common_error_title)
            },
            text = stringResource(Strings.string.advancedmount_common_error_subtitle),
        ) {
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
        is MountAccess.Denied -> CmpStateBox(
            icon = CmpIcons.Lock,
            title = stringResource(Strings.string.advancedmount_denied_title),
            text = access.message.takeIf { it.isNotBlank() },
            tone = CmpStateTone.Error,
        ) {
            RefreshButton(stringResource(Strings.string.advancedmount_common_btn_retry), actions.onRetry)
        }

        is MountAccess.Ready -> CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            if (access.unsupported) UnsupportedBanner(modelNotInDatabase = access.modelNotInDatabase)
            SectionsGrid(
                sections = access.sections,
                layout = layout,
                canLocked = state.canLocked,
                unreadCount = state.unreadCount,
                limited = access.unsupported,
                onClick = actions.onSectionClick,
            )
        }
    }
}

@Composable
private fun LoadingBlock() {
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(vertical = CmpSpacing.space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        CmpCircularProgressIndicator()
        CmpText(
            text = stringResource(Strings.string.advancedmount_common_loading),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RefreshButton(text: String, onClick: () -> Unit) {
    CmpButton(text = text, onClick = onClick, icon = CmpIcons.Refresh, fullWidth = true)
}

@Composable
private fun UnsupportedBanner(modelNotInDatabase: Boolean) {
    CmpBanner(
        title = stringResource(
            if (modelNotInDatabase) {
                Strings.string.advancedmount_root_title_no_model
            } else {
                Strings.string.advancedmount_unsupported_title
            },
        ),
        text = stringResource(
            if (modelNotInDatabase) {
                Strings.string.advancedmount_root_subtitle_no_model
            } else {
                Strings.string.advancedmount_unsupported_subtitle
            },
        ),
        tone = CmpBannerTone.Error,
        icon = CmpIcons.Warning,
        modifier = Modifier.fillMaxWidth(),
    )
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
    CmpDialog(
        onDismissRequest = onDismiss,
        title = {
            CmpColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpIcon(
                    painter = painterResource(Res.drawable.mount_dialog_warning),
                    contentDescription = null,
                    modifier = Modifier.size(CmpSize.iconL),
                    tint = CmpTheme.colors.errorDefault,
                )
                CmpText(text = title, style = CmpTextStyle.Headline2)
            }
        },
        text = { CmpText(text = text, style = CmpTextStyle.Paragraph2) },
        confirmButton = {
            CmpButton(type = CmpButtonType.Tertiary, size = CmpButtonSize.S, text = confirmText, onClick = onConfirm)
        },
        dismissButton = dismissText?.let {
            { CmpButton(type = CmpButtonType.Tertiary, size = CmpButtonSize.S, text = it, onClick = onDismiss) }
        },
    )
}

@Composable
private fun ProductInfoButton(tile: Boolean, onClick: () -> Unit) {
    val colors = CmpTheme.colors
    if (tile) {
        CmpClickableCard(
            onClick = onClick,
            modifier = Modifier.width(ProductTileWidth).heightIn(min = CmpSize.iconXxl).testTag(PRODUCT_INFO_TAG),
            shape = RoundedCornerShape(CmpRadius.cardSmall),
            border = BorderStroke(CmpBorderWidth.listItem, colors.borderLight),
            containerColor = colors.backgroundSurfaceHighlight,
        ) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.iconXxl).padding(CmpSpacing.space4),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CmpIcon(icon = CmpIcons.Inventory2, contentDescription = null)
                CmpText(
                    text = stringResource(Strings.string.advancedmount_common_btn_product_info_short),
                    style = CmpTextStyle.Additional1,
                    maxLines = 1,
                    uppercase = true,
                )
            }
        }
    } else {
        CmpButton(
            text = stringResource(Strings.string.advancedmount_common_btn_product_info_short),
            onClick = onClick,
            modifier = Modifier.testTag(PRODUCT_INFO_TAG),
            type = CmpButtonType.Secondary,
            size = CmpButtonSize.S,
            icon = CmpIcons.Inventory2,
        )
    }
}

private const val PRODUCT_INFO_TAG = "advancedmount_product_info"
