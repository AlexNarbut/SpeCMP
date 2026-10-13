package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionIcon
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.sectionTitle
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import specmp.feature.advanced_mount.generated.resources.Res
import specmp.feature.advanced_mount.generated.resources.advanced_mount_a11y_back
import specmp.feature.advanced_mount.generated.resources.advanced_mount_device_firmware
import specmp.feature.advanced_mount.generated.resources.advanced_mount_title

/** Корневой экран расширенного монтажа. */
@Composable
internal fun AdvancedMountRootScreen(
    onSectionClick: (MountSection) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<AdvancedMountRootViewModel>()
    val state by viewModel.state.collectAsState()

    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Res.string.advanced_mount_title)) },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.advanced_mount_a11y_back),
                    )
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 200.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.device?.let { device ->
                item(span = { GridItemSpan(maxLineSpan) }) { DeviceHeader(device) }
            }
            items(state.sections) { section ->
                SectionCard(section = section, onClick = { onSectionClick(section) })
            }
        }
    }
}

@Composable
private fun DeviceHeader(device: AlarmDevice) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(icon = CmpIcons.Memory, contentDescription = null, tint = colors.onPrimaryContainer)
            CmpColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CmpText(text = device.systemName, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    CmpText(text = device.systemId.toString(), fontSize = 14.sp, color = colors.onPrimaryContainer)
                }
                CmpText(
                    text = stringResource(Res.string.advanced_mount_device_firmware, device.firmwareVersion),
                    fontSize = 14.sp,
                    color = colors.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(section: MountSection, onClick: () -> Unit) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CmpSurface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = colors.secondaryContainer) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpIcon(icon = section.sectionIcon(), contentDescription = null, tint = colors.onSecondaryContainer)
                }
            }
            CmpText(text = sectionTitle(section), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
