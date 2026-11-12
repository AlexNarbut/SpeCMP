package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_extras_title_product_info
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_extras_title_remote_sync
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_msg_stub
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_bypass_learning
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_check_state
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_clone
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_community_maps
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_configurator
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_dynamic_devices
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_firmware_update
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_settings
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_support_chat
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_system_devices
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_tech_requests
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_vehicles
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_subtitle_voice_firmware
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_bypass_learning
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_check_state
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_clone
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_community_maps
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_configurator
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_dynamic_devices
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_firmware_update
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_settings
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_support_chat
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_system_devices
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_tech_requests
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_vehicles
import ru.alarmtrade.specmp.core.localization.resources.advancedmount_sections_title_voice_firmware
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountExtra
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import specmp.feature.advanced_mount.generated.resources.Res
import specmp.feature.advanced_mount.generated.resources.mount_remote_sync
import specmp.feature.advanced_mount.generated.resources.mount_section_bypass_learning
import specmp.feature.advanced_mount.generated.resources.mount_section_check_state
import specmp.feature.advanced_mount.generated.resources.mount_section_clone
import specmp.feature.advanced_mount.generated.resources.mount_section_community_maps
import specmp.feature.advanced_mount.generated.resources.mount_section_configurator
import specmp.feature.advanced_mount.generated.resources.mount_section_dynamic_devices
import specmp.feature.advanced_mount.generated.resources.mount_section_firmware_update
import specmp.feature.advanced_mount.generated.resources.mount_section_settings
import specmp.feature.advanced_mount.generated.resources.mount_section_support_chat
import specmp.feature.advanced_mount.generated.resources.mount_section_system_devices
import specmp.feature.advanced_mount.generated.resources.mount_section_tech_requests
import specmp.feature.advanced_mount.generated.resources.mount_section_vehicles
import specmp.feature.advanced_mount.generated.resources.mount_section_voice_firmware

private class SectionRes(val title: StringResource, val subtitle: StringResource, val icon: DrawableResource)

private val MountSection.res: SectionRes
    get() = when (this) {
        MountSection.FIRMWARE_UPDATE -> SectionRes(
            Strings.string.advancedmount_sections_title_firmware_update,
            Strings.string.advancedmount_sections_subtitle_firmware_update,
            Res.drawable.mount_section_firmware_update,
        )

        MountSection.SETTINGS -> SectionRes(
            Strings.string.advancedmount_sections_title_settings,
            Strings.string.advancedmount_sections_subtitle_settings,
            Res.drawable.mount_section_settings,
        )

        MountSection.CONFIGURATOR -> SectionRes(
            Strings.string.advancedmount_sections_title_configurator,
            Strings.string.advancedmount_sections_subtitle_configurator,
            Res.drawable.mount_section_configurator,
        )

        MountSection.BYPASS_LEARNING -> SectionRes(
            Strings.string.advancedmount_sections_title_bypass_learning,
            Strings.string.advancedmount_sections_subtitle_bypass_learning,
            Res.drawable.mount_section_bypass_learning,
        )

        MountSection.CLONE -> SectionRes(
            Strings.string.advancedmount_sections_title_clone,
            Strings.string.advancedmount_sections_subtitle_clone,
            Res.drawable.mount_section_clone,
        )

        MountSection.SYSTEM_DEVICES -> SectionRes(
            Strings.string.advancedmount_sections_title_system_devices,
            Strings.string.advancedmount_sections_subtitle_system_devices,
            Res.drawable.mount_section_system_devices,
        )

        MountSection.DYNAMIC_DEVICES -> SectionRes(
            Strings.string.advancedmount_sections_title_dynamic_devices,
            Strings.string.advancedmount_sections_subtitle_dynamic_devices,
            Res.drawable.mount_section_dynamic_devices,
        )

        MountSection.CHECK_STATE -> SectionRes(
            Strings.string.advancedmount_sections_title_check_state,
            Strings.string.advancedmount_sections_subtitle_check_state,
            Res.drawable.mount_section_check_state,
        )

        MountSection.TECH_REQUESTS -> SectionRes(
            Strings.string.advancedmount_sections_title_tech_requests,
            Strings.string.advancedmount_sections_subtitle_tech_requests,
            Res.drawable.mount_section_tech_requests,
        )

        MountSection.SUPPORT_CHAT -> SectionRes(
            Strings.string.advancedmount_sections_title_support_chat,
            Strings.string.advancedmount_sections_subtitle_support_chat,
            Res.drawable.mount_section_support_chat,
        )

        MountSection.VOICE_FIRMWARE -> SectionRes(
            Strings.string.advancedmount_sections_title_voice_firmware,
            Strings.string.advancedmount_sections_subtitle_voice_firmware,
            Res.drawable.mount_section_voice_firmware,
        )

        MountSection.COMMUNITY_MAPS -> SectionRes(
            Strings.string.advancedmount_sections_title_community_maps,
            Strings.string.advancedmount_sections_subtitle_community_maps,
            Res.drawable.mount_section_community_maps,
        )

        MountSection.VEHICLES -> SectionRes(
            Strings.string.advancedmount_sections_title_vehicles,
            Strings.string.advancedmount_sections_subtitle_vehicles,
            Res.drawable.mount_section_vehicles,
        )
    }

@Composable
internal fun sectionTitle(section: MountSection): String = stringResource(section.res.title)

@Composable
internal fun sectionSubtitle(section: MountSection): String = stringResource(section.res.subtitle)

@Composable
internal fun sectionPainter(section: MountSection): Painter = painterResource(section.res.icon)

@Composable
private fun extraTitle(extra: MountExtra): String = stringResource(
    when (extra) {
        MountExtra.PRODUCT_INFO -> Strings.string.advancedmount_extras_title_product_info
        MountExtra.REMOTE_SYNC -> Strings.string.advancedmount_extras_title_remote_sync
    },
)

@Composable
internal fun SectionStubScreen(section: MountSection, deviceTitle: String, onBack: () -> Unit) {
    StubScreen(
        title = sectionTitle(section),
        deviceTitle = deviceTitle,
        icon = sectionPainter(section),
        onBack = onBack,
    )
}

@Composable
internal fun ExtraStubScreen(extra: MountExtra, deviceTitle: String, onBack: () -> Unit) {
    val icon = when (extra) {
        MountExtra.PRODUCT_INFO -> null
        MountExtra.REMOTE_SYNC -> painterResource(Res.drawable.mount_remote_sync)
    }
    StubScreen(title = extraTitle(extra), deviceTitle = deviceTitle, icon = icon, onBack = onBack)
}

@Composable
private fun StubScreen(title: String, deviceTitle: String, icon: Painter?, onBack: () -> Unit) {
    val colors = cmpThemeColors()
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = title) },
                subtitle = deviceTitle.takeIf { it.isNotBlank() }?.let {
                    { CmpText(text = it, fontSize = 14.sp, color = colors.onSurfaceVariant, maxLines = 1) }
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.advancedmount_common_a11y_back),
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
            CmpSurface(modifier = Modifier.size(88.dp), shape = CircleShape, color = colors.primaryContainer) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (icon != null) {
                        CmpIcon(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = colors.onPrimaryContainer,
                        )
                    } else {
                        CmpIcon(icon = CmpIcons.Info, contentDescription = null, tint = colors.onPrimaryContainer)
                    }
                }
            }
            CmpText(
                text = stringResource(Strings.string.advancedmount_sections_msg_stub),
                fontSize = 16.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
