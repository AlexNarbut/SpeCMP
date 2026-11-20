package ru.alarmtrade.specmp.feature.main.presentation.model

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_advanced_mount
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_cars
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_configurator
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_feedback
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_firmware_archive
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_mount_passports
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_product_info
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_quick_mount
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_tech_requests
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_tech_support_chat
import ru.alarmtrade.specmp.core.localization.resources.main_main_msg_warehouse_1c
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_advanced_mount
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_cars
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_configurator
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_feedback
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_firmware_archive
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_mount_passports
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_product_info
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_quick_mount
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_tech_requests
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_tech_support_chat
import ru.alarmtrade.specmp.core.localization.resources.main_main_title_warehouse_1c
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import specmp.feature.main.generated.resources.Res
import specmp.feature.main.generated.resources.adv_mount
import specmp.feature.main.generated.resources.chat
import specmp.feature.main.generated.resources.fast_mount
import specmp.feature.main.generated.resources.feedback
import specmp.feature.main.generated.resources.firmware_archive
import specmp.feature.main.generated.resources.mount_passports
import specmp.feature.main.generated.resources.orders
import specmp.feature.main.generated.resources.product_search
import specmp.feature.main.generated.resources.settings_configurator
import specmp.feature.main.generated.resources.tech_requests
import specmp.feature.main.generated.resources.vehicle_support

enum class MainHomeActionGroup {
    Primary,
    Tool,
}

enum class MainHomeAction(
    val target: MainNavigationTarget,
    val group: MainHomeActionGroup,
    val icon: DrawableResource,
    val title: StringResource,
    val description: StringResource,
) {
    QuickMount(
        target = MainNavigationTarget.QuickMount,
        group = MainHomeActionGroup.Primary,
        icon = Res.drawable.fast_mount,
        title = Strings.string.main_main_title_quick_mount,
        description = Strings.string.main_main_msg_quick_mount,
    ),
    AdvancedMount(
        target = MainNavigationTarget.AdvancedMount,
        group = MainHomeActionGroup.Primary,
        icon = Res.drawable.adv_mount,
        title = Strings.string.main_main_title_advanced_mount,
        description = Strings.string.main_main_msg_advanced_mount,
    ),
    Configurator(
        target = MainNavigationTarget.Configurator,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.settings_configurator,
        title = Strings.string.main_main_title_configurator,
        description = Strings.string.main_main_msg_configurator,
    ),
    FirmwareArchive(
        target = MainNavigationTarget.FirmwareArchive,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.firmware_archive,
        title = Strings.string.main_main_title_firmware_archive,
        description = Strings.string.main_main_msg_firmware_archive,
    ),
    Cars(
        target = MainNavigationTarget.Cars,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.vehicle_support,
        title = Strings.string.main_main_title_cars,
        description = Strings.string.main_main_msg_cars,
    ),
    TechRequests(
        target = MainNavigationTarget.TechRequests,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.tech_requests,
        title = Strings.string.main_main_title_tech_requests,
        description = Strings.string.main_main_msg_tech_requests,
    ),
    Feedback(
        target = MainNavigationTarget.Feedback,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.feedback,
        title = Strings.string.main_main_title_feedback,
        description = Strings.string.main_main_msg_feedback,
    ),
    ProductInfo(
        target = MainNavigationTarget.ProductInfo,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.product_search,
        title = Strings.string.main_main_title_product_info,
        description = Strings.string.main_main_msg_product_info,
    ),
    MountPassports(
        target = MainNavigationTarget.MountPassports,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.mount_passports,
        title = Strings.string.main_main_title_mount_passports,
        description = Strings.string.main_main_msg_mount_passports,
    ),
    TechSupportChat(
        target = MainNavigationTarget.TechSupportChat,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.chat,
        title = Strings.string.main_main_title_tech_support_chat,
        description = Strings.string.main_main_msg_tech_support_chat,
    ),
    Warehouse1C(
        target = MainNavigationTarget.Warehouse1C,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.orders,
        title = Strings.string.main_main_title_warehouse_1c,
        description = Strings.string.main_main_msg_warehouse_1c,
    ),
    ;

    companion object {
        fun visibleActions(
            features: MainUserAppFeatures = MainUserAppFeatures(),
            commercial: MainUserCommercial = MainUserCommercial(),
            showChat: Boolean = false,
        ): List<MainHomeAction> = entries.filter { action ->
            when (action) {
                ProductInfo -> features.hasProductSearch
                Warehouse1C -> features.hasOrdersRequest && !commercial.warehouse1CUrl.isNullOrBlank()
                TechSupportChat -> showChat
                else -> true
            }
        }
    }
}
