package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import specmp.feature.advanced_mount.generated.resources.Res
import specmp.feature.advanced_mount.generated.resources.advanced_mount_a11y_back
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_bypass_learning
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_clone
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_dynamic_devices
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_firmware_update
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_settings
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_stub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_voice_firmware

@Composable
internal fun sectionTitle(section: MountSection): String = stringResource(
    when (section) {
        MountSection.SETTINGS -> Res.string.advanced_mount_section_settings
        MountSection.FIRMWARE_UPDATE -> Res.string.advanced_mount_section_firmware_update
        MountSection.VOICE_FIRMWARE -> Res.string.advanced_mount_section_voice_firmware
        MountSection.BYPASS_LEARNING -> Res.string.advanced_mount_section_bypass_learning
        MountSection.CLONE -> Res.string.advanced_mount_section_clone
        MountSection.DYNAMIC_DEVICES -> Res.string.advanced_mount_section_dynamic_devices
    },
)

internal fun MountSection.sectionIcon(): CmpIcons = when (this) {
    MountSection.SETTINGS -> CmpIcons.Settings
    MountSection.FIRMWARE_UPDATE -> CmpIcons.Download
    MountSection.VOICE_FIRMWARE -> CmpIcons.Mic
    MountSection.BYPASS_LEARNING -> CmpIcons.VpnKey
    MountSection.CLONE -> CmpIcons.Share
    MountSection.DYNAMIC_DEVICES -> CmpIcons.Sensors
}

/** Заглушка раздела: экраны разделов — отдельные задачи. */
@Composable
internal fun SectionStubScreen(section: MountSection, onBack: () -> Unit) {
    val colors = cmpThemeColors()
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = sectionTitle(section)) },
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
        CmpColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            CmpIcon(icon = section.sectionIcon(), contentDescription = null, tint = colors.onSurfaceVariant)
            CmpText(
                text = stringResource(Res.string.advanced_mount_section_stub),
                fontSize = 16.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
