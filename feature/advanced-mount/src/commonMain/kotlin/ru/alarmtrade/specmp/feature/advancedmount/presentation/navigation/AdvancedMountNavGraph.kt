package ru.alarmtrade.specmp.feature.advancedmount.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountExtra
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.AdvancedMountRootScreen
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.ExtraStubScreen
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.SectionStubScreen

/** App-level ключ расширенного монтажа (регистрируется в графе `composeApp`). */
@Serializable
@SerialName("advanced-mount.AdvancedMountRoute")
data object AdvancedMountRoute : NavKey

@Serializable
@SerialName("advanced-mount.AdvancedMountRootRoute")
internal data object AdvancedMountRootRoute : NavKey

/**
 * Раздел; [deviceTitle] — «Название  ID» блока для подзаголовка; [fromBypass] — Clone открыт из бесключевого обхода
 * (поверх него: «Назад» возвращает в обход).
 */
@Serializable
@SerialName("advanced-mount.AdvancedMountSectionRoute")
internal data class AdvancedMountSectionRoute(
    val section: MountSection,
    val deviceTitle: String,
    val fromBypass: Boolean = false
) : NavKey

@Serializable
@SerialName("advanced-mount.AdvancedMountExtraRoute")
internal data class AdvancedMountExtraRoute(val extra: MountExtra, val deviceTitle: String) : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AdvancedMountRootRoute::class, AdvancedMountRootRoute.serializer())
            subclass(AdvancedMountSectionRoute::class, AdvancedMountSectionRoute.serializer())
            subclass(AdvancedMountExtraRoute::class, AdvancedMountExtraRoute.serializer())
        }
    }
}

/**
 * Граф расширенного монтажа: свой back stack (корень → разделы). Оборачивается в
 * `DeviceInteractionHost` в `composeApp`: при потере связи граф остаётся в композиции под
 * экраном дисконнекта, после переподключения — тот же экран.
 *
 * @param onExit выход из монтажа (с корня) — закрывает сессию через обёртку.
 * @param onOpenSupportChat открыть чат техподдержки (фича `support-chat`, app-level маршрут).
 * @param firmwareCheck экран раздела «Проверка прошивок» (фича `device/firmware-ui`, подключается в `composeApp`);
 *   получает выход на корень монтажа.
 * @param settingsEditor раздел «Настройки» (фича `device/settings-ui`, подключается в `composeApp`); получает выход на
 *   корень монтажа и переход в «Проверку прошивок» (заменяет раздел настроек: CAN-код блока с единой CAN-прошивкой
 *   меняется прошивкой).
 * @param configurator раздел «Конфигуратор» (фича `configurator-ui`, подключается в `composeApp`): тот же конфигуратор,
 *   что с главной, но при активной сессии — с «Записать в блок»; получает выход на корень монтажа и переход
 *   в «Проверку прошивок» (из редактора блока, единая CAN-прошивка).
 * @param bypassLearning раздел «Бесключевой обход» (фича `device/autostart`, подключается в `composeApp`): получает
 *   выход, переход в Clone (отдельный экран поверх обхода) и переход в чат техподдержки.
 * @param clone раздел «Процедура Clone» (фича `device/autostart`, подключается в `composeApp`): отдельный экран;
 *   получает, открыт ли он из бесключевого обхода, выход и переход в чат техподдержки.
 * @param checkState раздел «Проверка состояния» (фича `device/state`, подключается в `composeApp`): получает выход
 *   на корень монтажа. Плитка есть только при подключении по Bluetooth.
 * @param vehicles раздел «Автомобили» (фича `vehicle-ui`, подключается в `composeApp` с моделью и CAN-кодом блока);
 *   получает выход на корень монтажа.
 */
@Composable
fun AdvancedMountNavGraph(
    onExit: () -> Unit,
    onOpenSupportChat: () -> Unit,
    firmwareCheck: @Composable (onBack: () -> Unit) -> Unit,
    settingsEditor: @Composable (onBack: () -> Unit, onOpenFirmwareUpdate: () -> Unit) -> Unit,
    configurator: @Composable (onBack: () -> Unit, onOpenFirmwareUpdate: () -> Unit) -> Unit,
    bypassLearning: @Composable (onBack: () -> Unit, onOpenClone: () -> Unit, onOpenSupportChat: () -> Unit) -> Unit,
    clone: @Composable (fromBypass: Boolean, onBack: () -> Unit, onOpenSupportChat: () -> Unit) -> Unit,
    checkState: @Composable (onBack: () -> Unit) -> Unit,
    vehicles: @Composable (onBack: () -> Unit) -> Unit
) {
    val backStack = rememberNavBackStack(navConfig, AdvancedMountRootRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = {
            if (!backStack.popSafe()) onExit()
        },
        entryProvider = entryProvider<NavKey> {
            entry<AdvancedMountRootRoute> { _ ->
                AdvancedMountRootScreen(
                    onSectionClick = { section, title ->
                        backStack.navigateSafe(AdvancedMountSectionRoute(section, title))
                    },
                    onExtraClick = { extra, title -> backStack.navigateSafe(AdvancedMountExtraRoute(extra, title)) },
                    onOpenSupportChat = onOpenSupportChat,
                    onBack = onExit
                )
            }
            entry<AdvancedMountSectionRoute> { key ->
                when (key.section) {
                    MountSection.FIRMWARE_UPDATE -> firmwareCheck { backStack.popSafe() }

                    MountSection.SETTINGS -> settingsEditor({ backStack.popSafe() }) {
                        backStack.replaceCurrent(
                            AdvancedMountSectionRoute(MountSection.FIRMWARE_UPDATE, key.deviceTitle)
                        )
                    }

                    MountSection.CONFIGURATOR -> configurator({ backStack.popSafe() }) {
                        backStack.replaceCurrent(
                            AdvancedMountSectionRoute(MountSection.FIRMWARE_UPDATE, key.deviceTitle)
                        )
                    }

                    MountSection.BYPASS_LEARNING -> bypassLearning(
                        { backStack.popSafe() },
                        {
                            backStack.navigateSafe(
                                AdvancedMountSectionRoute(MountSection.CLONE, key.deviceTitle, fromBypass = true)
                            )
                        },
                        onOpenSupportChat
                    )

                    MountSection.CLONE -> clone(key.fromBypass, { backStack.popSafe() }, onOpenSupportChat)

                    MountSection.CHECK_STATE -> checkState { backStack.popSafe() }

                    MountSection.VEHICLES -> vehicles { backStack.popSafe() }

                    else -> SectionStubScreen(section = key.section, deviceTitle = key.deviceTitle, onBack = {
                        backStack.popSafe()
                    })
                }
            }
            entry<AdvancedMountExtraRoute> { key ->
                ExtraStubScreen(extra = key.extra, deviceTitle = key.deviceTitle, onBack = { backStack.popSafe() })
            }
        }
    )
}
