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
import specmp.feature.advanced_mount.generated.resources.advanced_mount_a11y_back
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_bypass_learning
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_bypass_learning_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_clone
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_clone_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_community_maps
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_community_maps_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_dynamic_devices
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_dynamic_devices_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_firmware_update
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_firmware_update_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_settings
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_settings_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_stub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_support_chat
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_support_chat_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_tech_requests
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_tech_requests_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_vehicles
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_vehicles_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_voice_firmware
import specmp.feature.advanced_mount.generated.resources.advanced_mount_section_voice_firmware_sub
import specmp.feature.advanced_mount.generated.resources.advanced_mount_stub_product_info
import specmp.feature.advanced_mount.generated.resources.advanced_mount_stub_remote_sync
import specmp.feature.advanced_mount.generated.resources.mount_remote_sync
import specmp.feature.advanced_mount.generated.resources.mount_section_bypass_learning
import specmp.feature.advanced_mount.generated.resources.mount_section_clone
import specmp.feature.advanced_mount.generated.resources.mount_section_community_maps
import specmp.feature.advanced_mount.generated.resources.mount_section_dynamic_devices
import specmp.feature.advanced_mount.generated.resources.mount_section_firmware_update
import specmp.feature.advanced_mount.generated.resources.mount_section_settings
import specmp.feature.advanced_mount.generated.resources.mount_section_support_chat
import specmp.feature.advanced_mount.generated.resources.mount_section_tech_requests
import specmp.feature.advanced_mount.generated.resources.mount_section_vehicles
import specmp.feature.advanced_mount.generated.resources.mount_section_voice_firmware

private class SectionRes(val title: StringResource, val subtitle: StringResource, val icon: DrawableResource)

private val MountSection.res: SectionRes
    get() = when (this) {
        MountSection.FIRMWARE_UPDATE -> SectionRes(
            Res.string.advanced_mount_section_firmware_update,
            Res.string.advanced_mount_section_firmware_update_sub,
            Res.drawable.mount_section_firmware_update,
        )
        MountSection.SETTINGS -> SectionRes(
            Res.string.advanced_mount_section_settings,
            Res.string.advanced_mount_section_settings_sub,
            Res.drawable.mount_section_settings,
        )
        MountSection.BYPASS_LEARNING -> SectionRes(
            Res.string.advanced_mount_section_bypass_learning,
            Res.string.advanced_mount_section_bypass_learning_sub,
            Res.drawable.mount_section_bypass_learning,
        )
        MountSection.CLONE -> SectionRes(
            Res.string.advanced_mount_section_clone,
            Res.string.advanced_mount_section_clone_sub,
            Res.drawable.mount_section_clone,
        )
        MountSection.DYNAMIC_DEVICES -> SectionRes(
            Res.string.advanced_mount_section_dynamic_devices,
            Res.string.advanced_mount_section_dynamic_devices_sub,
            Res.drawable.mount_section_dynamic_devices,
        )
        MountSection.TECH_REQUESTS -> SectionRes(
            Res.string.advanced_mount_section_tech_requests,
            Res.string.advanced_mount_section_tech_requests_sub,
            Res.drawable.mount_section_tech_requests,
        )
        MountSection.SUPPORT_CHAT -> SectionRes(
            Res.string.advanced_mount_section_support_chat,
            Res.string.advanced_mount_section_support_chat_sub,
            Res.drawable.mount_section_support_chat,
        )
        MountSection.VOICE_FIRMWARE -> SectionRes(
            Res.string.advanced_mount_section_voice_firmware,
            Res.string.advanced_mount_section_voice_firmware_sub,
            Res.drawable.mount_section_voice_firmware,
        )
        MountSection.COMMUNITY_MAPS -> SectionRes(
            Res.string.advanced_mount_section_community_maps,
            Res.string.advanced_mount_section_community_maps_sub,
            Res.drawable.mount_section_community_maps,
        )
        MountSection.VEHICLES -> SectionRes(
            Res.string.advanced_mount_section_vehicles,
            Res.string.advanced_mount_section_vehicles_sub,
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
        MountExtra.PRODUCT_INFO -> Res.string.advanced_mount_stub_product_info
        MountExtra.REMOTE_SYNC -> Res.string.advanced_mount_stub_remote_sync
    },
)

/** Заглушка раздела: экраны разделов — отдельные задачи. [deviceTitle] — «Название  ID» блока. */
@Composable
internal fun SectionStubScreen(section: MountSection, deviceTitle: String, onBack: () -> Unit) {
    StubScreen(title = sectionTitle(section), deviceTitle = deviceTitle, icon = sectionPainter(section), onBack = onBack)
}

/** Заглушка экранов, ещё не проработанных в дизайне («Информация о товаре», «Удалённая синхронизация»). */
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
            CmpSurface(modifier = Modifier.size(88.dp), shape = CircleShape, color = colors.primaryContainer) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (icon != null) {
                        CmpIcon(painter = icon, contentDescription = null, modifier = Modifier.size(44.dp), tint = colors.onPrimaryContainer)
                    } else {
                        CmpIcon(icon = CmpIcons.Info, contentDescription = null, tint = colors.onPrimaryContainer)
                    }
                }
            }
            CmpText(
                text = stringResource(Res.string.advanced_mount_section_stub),
                fontSize = 16.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
