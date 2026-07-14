package ru.alarmtrade.specmp.feature.main.presentation.model

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import specmp.feature.main.generated.resources.Res
import specmp.feature.main.generated.resources.adv_mount
import specmp.feature.main.generated.resources.can
import specmp.feature.main.generated.resources.chat
import specmp.feature.main.generated.resources.fast_mount
import specmp.feature.main.generated.resources.feedback
import specmp.feature.main.generated.resources.firmware_archive
import specmp.feature.main.generated.resources.main_main_msg_advanced_mount
import specmp.feature.main.generated.resources.main_main_msg_can_scanner
import specmp.feature.main.generated.resources.main_main_msg_cars
import specmp.feature.main.generated.resources.main_main_msg_configurator
import specmp.feature.main.generated.resources.main_main_msg_feedback
import specmp.feature.main.generated.resources.main_main_msg_firmware_archive
import specmp.feature.main.generated.resources.main_main_msg_product_info
import specmp.feature.main.generated.resources.main_main_msg_quick_mount
import specmp.feature.main.generated.resources.main_main_msg_requests
import specmp.feature.main.generated.resources.main_main_msg_tech_support_chat
import specmp.feature.main.generated.resources.main_main_msg_warehouse_1c
import specmp.feature.main.generated.resources.main_main_title_advanced_mount
import specmp.feature.main.generated.resources.main_main_title_can_scanner
import specmp.feature.main.generated.resources.main_main_title_cars
import specmp.feature.main.generated.resources.main_main_title_configurator
import specmp.feature.main.generated.resources.main_main_title_feedback
import specmp.feature.main.generated.resources.main_main_title_firmware_archive
import specmp.feature.main.generated.resources.main_main_title_product_info
import specmp.feature.main.generated.resources.main_main_title_quick_mount
import specmp.feature.main.generated.resources.main_main_title_requests
import specmp.feature.main.generated.resources.main_main_title_tech_support_chat
import specmp.feature.main.generated.resources.main_main_title_warehouse_1c
import specmp.feature.main.generated.resources.orders
import specmp.feature.main.generated.resources.product_search
import specmp.feature.main.generated.resources.settings_configurator
import specmp.feature.main.generated.resources.user_manual_request
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
    val isVisibleByDefault: Boolean = true,
) {
    QuickMount(
        target = MainNavigationTarget.QuickMount,
        group = MainHomeActionGroup.Primary,
        icon = Res.drawable.fast_mount,
        title = Res.string.main_main_title_quick_mount,
        description = Res.string.main_main_msg_quick_mount,
    ),
    AdvancedMount(
        target = MainNavigationTarget.AdvancedMount,
        group = MainHomeActionGroup.Primary,
        icon = Res.drawable.adv_mount,
        title = Res.string.main_main_title_advanced_mount,
        description = Res.string.main_main_msg_advanced_mount,
    ),
    Configurator(
        target = MainNavigationTarget.Configurator,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.settings_configurator,
        title = Res.string.main_main_title_configurator,
        description = Res.string.main_main_msg_configurator,
    ),
    FirmwareArchive(
        target = MainNavigationTarget.FirmwareArchive,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.firmware_archive,
        title = Res.string.main_main_title_firmware_archive,
        description = Res.string.main_main_msg_firmware_archive,
    ),
    Cars(
        target = MainNavigationTarget.Cars,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.vehicle_support,
        title = Res.string.main_main_title_cars,
        description = Res.string.main_main_msg_cars,
    ),
    CanScanner(
        target = MainNavigationTarget.CanScanner,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.can,
        title = Res.string.main_main_title_can_scanner,
        description = Res.string.main_main_msg_can_scanner,
    ),
    Requests(
        target = MainNavigationTarget.Requests,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.user_manual_request,
        title = Res.string.main_main_title_requests,
        description = Res.string.main_main_msg_requests,
    ),
    Feedback(
        target = MainNavigationTarget.Feedback,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.feedback,
        title = Res.string.main_main_title_feedback,
        description = Res.string.main_main_msg_feedback,
    ),
    ProductInfo(
        target = MainNavigationTarget.ProductInfo,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.product_search,
        title = Res.string.main_main_title_product_info,
        description = Res.string.main_main_msg_product_info,
    ),
    TechSupportChat(
        target = MainNavigationTarget.TechSupportChat,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.chat,
        title = Res.string.main_main_title_tech_support_chat,
        description = Res.string.main_main_msg_tech_support_chat,
    ),
    Warehouse1C(
        target = MainNavigationTarget.Warehouse1C,
        group = MainHomeActionGroup.Tool,
        icon = Res.drawable.orders,
        title = Res.string.main_main_title_warehouse_1c,
        description = Res.string.main_main_msg_warehouse_1c,
    );

    companion object {
        fun visibleActions(
            features: MainUserAppFeatures = MainUserAppFeatures(),
            commercial: MainUserCommercial = MainUserCommercial(),
            showChat: Boolean = false,
        ): List<MainHomeAction> {
            return entries.filter { action ->
                if (!action.isVisibleByDefault) return@filter false

                when (action) {
                    CanScanner -> features.hasCanScanner
                    ProductInfo -> features.hasProductSearch
                    Warehouse1C -> features.hasOrdersRequest && !commercial.warehouse1CUrl.isNullOrBlank()
                    TechSupportChat -> showChat
                    else -> true
                }
            }
        }
    }
}
