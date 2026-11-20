package ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation

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
import ru.alarmtrade.specmp.core.ui.navigation.cmpBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpForwardTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpPredictiveBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorPane
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListMode
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListPane
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupPane
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyPane

@Serializable
@SerialName("mount-passport.PassportArchiveKey")
internal data object PassportArchiveKey : NavKey

@Serializable
@SerialName("mount-passport.PassportUnitKey")
internal data class PassportUnitKey(val systemId: Long) : NavKey

@Serializable
@SerialName("mount-passport.PassportEditorKey")
internal data class PassportEditorKey(val passportId: String) : NavKey

@Serializable
@SerialName("mount-passport.PassportReadyKey")
internal data class PassportReadyKey(val passportId: String) : NavKey

@Serializable
@SerialName("mount-passport.PassportLookupKey")
internal data object PassportLookupKey : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(PassportArchiveKey::class, PassportArchiveKey.serializer())
            subclass(PassportUnitKey::class, PassportUnitKey.serializer())
            subclass(PassportEditorKey::class, PassportEditorKey.serializer())
            subclass(PassportReadyKey::class, PassportReadyKey.serializer())
            subclass(PassportLookupKey::class, PassportLookupKey.serializer())
        }
    }
}

/**
 * Внутри фичи: список → предотчёт → «Паспорт готов»; архив → «Блок» (паспорт без блока) → предотчёт вместо него.
 * Выход с первого экрана — [MountPassportCallbacks.onBack].
 */
@Composable
internal fun MountPassportNavGraph(start: NavKey, callbacks: MountPassportCallbacks, ports: MountPassportPorts) {
    val backStack = rememberNavBackStack(navConfig, start)
    val back: () -> Unit = { if (!backStack.popSafe()) callbacks.onBack() }
    NavDisplay(
        transitionSpec = cmpForwardTransition(),
        popTransitionSpec = cmpBackTransition(),
        predictivePopTransitionSpec = cmpPredictiveBackTransition(),
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { back() },
        entryProvider = entryProvider<NavKey> {
            entry<PassportArchiveKey> {
                PassportListPane(
                    mode = PassportListMode.Archive,
                    callbacks = callbacks,
                    ports = ports,
                    onBack = { back() },
                    onOpen = { id -> backStack.navigateSafe(PassportEditorKey(id)) },
                    onNewWithoutUnit = { backStack.navigateSafe(PassportLookupKey) },
                    onCreatePdf = { id -> backStack.navigateSafe(PassportReadyKey(id)) },
                )
            }
            entry<PassportUnitKey> { key ->
                PassportListPane(
                    mode = PassportListMode.Unit(key.systemId),
                    callbacks = callbacks,
                    ports = ports,
                    onBack = { back() },
                    onOpen = { id -> backStack.navigateSafe(PassportEditorKey(id)) },
                    onNewWithoutUnit = {},
                    onCreatePdf = { id -> backStack.navigateSafe(PassportReadyKey(id)) },
                )
            }
            entry<PassportEditorKey> { key ->
                PassportEditorPane(
                    passportId = key.passportId,
                    callbacks = callbacks,
                    ports = ports,
                    embedded = false,
                    onClose = { back() },
                    onCreatePdf = { backStack.navigateSafe(PassportReadyKey(key.passportId)) },
                )
            }
            entry<PassportReadyKey> { key ->
                PassportReadyPane(passportId = key.passportId, ports = ports, preview = false, onEdit = { back() })
            }
            entry<PassportLookupKey> {
                PassportLookupPane(
                    onBack = { back() },
                    onCreate = { id -> backStack.replaceCurrent(PassportEditorKey(id)) },
                )
            }
        },
    )
}
