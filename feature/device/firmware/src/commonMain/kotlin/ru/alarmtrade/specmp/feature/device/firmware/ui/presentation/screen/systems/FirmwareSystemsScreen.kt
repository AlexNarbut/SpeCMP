package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_msg
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_title_screen
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_a11y_collapse
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_a11y_expand
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_a11y_order
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_error_title
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_msg_not_found
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_placeholder_search
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_subtitle
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_value_order_added
import ru.alarmtrade.specmp.core.localization.resources.firmware_systems_value_order_alphabet
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.titleRes
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

internal const val SYSTEMS_SEARCH_TAG = "firmware_systems_search"

internal fun systemTag(id: Long): String = "firmware_system_$id"

internal fun subsystemTag(id: Long): String = "firmware_subsystem_$id"

private val ListMaxWidth = 760.dp

@Composable
fun FirmwareSystemsEntry(onPick: (FirmwareArchiveRequest) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<FirmwareSystemsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FirmwareSystemsContent(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onToggle = viewModel::onToggle,
        onPick = { system, subsystem -> onPick(archiveRequestOf(system, subsystem)) },
        onRetry = viewModel::onRetry,
        onOrderSelect = viewModel::onOrderSelect,
    )
}

fun archiveRequestOf(system: FirmwareSystem, subsystem: FirmwareSubsystem): FirmwareArchiveRequest =
    FirmwareArchiveRequest(
        moduleId = subsystem.id,
        channel = FirmwareChannel.RELEASE,
        installedVersion = null,
        deviceTitle = subsystem.title,
        profile = if (subsystem.kind ==
            FirmwareModuleKind.BluetoothModule
        ) {
            UpdateProfile.Bluetooth
        } else {
            UpdateProfile.Main
        },
        deviceName = system.name,
        noUnit = true,
        canFilterAllowed = subsystem.kind == FirmwareModuleKind.AlarmMain,
        moduleName = subsystem.systemName,
    )

@Composable
internal fun FirmwareSystemsContent(
    state: FirmwareSystemsState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggle: (Long) -> Unit,
    onPick: (FirmwareSystem, FirmwareSubsystem) -> Unit,
    onRetry: () -> Unit,
    onOrderSelect: (SystemsOrder) -> Unit = {},
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = {
                    CmpText(
                        text = stringResource(Strings.string.firmware_archive_title_screen),
                        style = CmpTextStyle.Paragraph2,
                    )
                },
                subtitle = {
                    CmpText(
                        text = stringResource(Strings.string.firmware_systems_subtitle),
                        style = CmpTextStyle.Paragraph3,
                    )
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.firmware_common_a11y_back),
                    )
                },
                actions = { OrderMenu(state.order, onOrderSelect) },
            )
        },
    ) { padding ->
        CmpColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CmpSearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(Strings.string.firmware_systems_placeholder_search),
                clearContentDescription = stringResource(Strings.string.firmware_systems_a11y_clear),
                modifier = Modifier.widthIn(max = ListMaxWidth).fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp).testTag(SYSTEMS_SEARCH_TAG),
            )
            when (val content = state.content) {
                LCEState.None, LCEState.Loading -> CmpBox(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpCircularProgressIndicator()
                }

                is LCEState.Error -> CmpStateBox(
                    icon = CmpIcons.CloudOff,
                    title = content.error.text(Strings.string.firmware_systems_error_title),
                    text = stringResource(Strings.string.firmware_archive_error_msg),
                    tone = CmpStateTone.Error,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CmpButton(text = stringResource(Strings.string.firmware_common_btn_retry), onClick = onRetry)
                }

                is LCEState.Content -> if (content.content.isEmpty()) {
                    CmpStateBox(
                        icon = if (state.query.isBlank()) CmpIcons.Inventory2 else CmpIcons.SearchOff,
                        title = if (state.query.isBlank()) {
                            stringResource(Strings.string.firmware_systems_msg_empty)
                        } else {
                            stringResource(Strings.string.firmware_systems_msg_not_found, state.query.trim())
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    SystemsList(content.content, searching = state.query.isNotBlank(), onToggle, onPick)
                }
            }
        }
    }
}

@Composable
private fun SystemsList(
    rows: List<SystemRowUi>,
    searching: Boolean,
    onToggle: (Long) -> Unit,
    onPick: (FirmwareSystem, FirmwareSubsystem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.widthIn(max = ListMaxWidth).fillMaxSize().hideKeyboardOnScroll(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(rows, key = { it.system.id }) { row ->
            SystemCard(row, searching, onToggle, onPick)
        }
    }
}

@Composable
private fun SystemCard(
    row: SystemRowUi,
    searching: Boolean,
    onToggle: (Long) -> Unit,
    onPick: (FirmwareSystem, FirmwareSubsystem) -> Unit,
) {
    val colors = cmpThemeColors()
    val single = row.system.subsystems.singleOrNull()
    CmpClickableSurface(
        onClick = { if (single != null) onPick(row.system, single) else onToggle(row.system.id) },
        enabled = !searching || single != null,
        modifier = Modifier.fillMaxWidth().testTag(systemTag(row.system.id)),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn {
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(
                    icon = CmpIcons.Memory,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(26.dp),
                )
                CmpColumn(modifier = Modifier.weight(1f)) {
                    CmpText(text = row.system.name, style = CmpTextStyle.Headline3)
                    if (!row.expanded || single != null) {
                        CmpText(
                            text = row.system.subsystems.joinToString { it.title },
                            style = CmpTextStyle.Additional1,
                            color = colors.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
                CmpIcon(
                    icon = when {
                        single != null -> CmpIcons.ChevronRight
                        row.expanded -> CmpIcons.KeyboardArrowUp
                        else -> CmpIcons.KeyboardArrowDown
                    },
                    contentDescription = if (single != null) {
                        null
                    } else if (row.expanded) {
                        stringResource(Strings.string.firmware_systems_a11y_collapse)
                    } else {
                        stringResource(Strings.string.firmware_systems_a11y_expand)
                    },
                    tint = colors.onSurfaceVariant,
                )
            }
            if (row.expanded && single == null) {
                row.subsystems.forEach { subsystem -> SubsystemRow(subsystem) { onPick(row.system, subsystem) } }
            }
        }
    }
}

@Composable
private fun SubsystemRow(subsystem: FirmwareSubsystem, onClick: () -> Unit) {
    val colors = cmpThemeColors()
    CmpClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(subsystemTag(subsystem.id)),
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(
                min = 56.dp,
            ).padding(start = 56.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(text = subsystem.title, style = CmpTextStyle.Paragraph2)
                CmpText(
                    text = subsystem.systemName + " · " + stringResource(subsystem.kind.titleRes()),
                    style = CmpTextStyle.Additional1,
                    color = colors.onSurfaceVariant,
                )
            }
            CmpIcon(icon = CmpIcons.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun OrderMenu(order: SystemsOrder, onSelect: (SystemsOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    CmpBox {
        CmpIconButton(
            onClick = { open = true },
            icon = CmpIcons.FilterList,
            contentDescription = stringResource(Strings.string.firmware_systems_a11y_order),
            tint = cmpThemeColors().primary,
            modifier = Modifier.testTag(SYSTEMS_ORDER_TAG),
        )
        CmpDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SystemsOrder.entries.forEach { option ->
                CmpDropdownMenuItem(
                    text = stringResource(
                        when (option) {
                            SystemsOrder.Added -> Strings.string.firmware_systems_value_order_added
                            SystemsOrder.Alphabet -> Strings.string.firmware_systems_value_order_alphabet
                        },
                    ),
                    onClick = {
                        open = false
                        onSelect(option)
                    },
                    selected = option == order,
                )
            }
        }
    }
}

internal const val SYSTEMS_ORDER_TAG = "firmware_systems_order"
