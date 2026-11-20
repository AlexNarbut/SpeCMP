package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.files.rememberBinaryFileExporter
import ru.alarmtrade.specmp.core.files.rememberBinaryFilePicker
import ru.alarmtrade.specmp.core.files.rememberHtmlPdfRenderer
import ru.alarmtrade.specmp.core.files.rememberImagePicker
import ru.alarmtrade.specmp.core.files.rememberPdfPageRasterizer
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehiclePick
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportArchiveEntry
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportCallbacks
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportEditorEntry
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportEditorRoute
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportReadEntry
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportReadRoute
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportReadRouteSource
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportUnitEntry
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.PassportVehiclePicker
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectEntry
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest
import ru.alarmtrade.specmp.filepicker.rememberSettingsFileSaver

/** Переходы паспорта наружу: «Новый паспорт», «Дооформить» и «Считать блок» — в считывание блока. */
@Composable
private fun callbacks(backStack: NavBackStack<NavKey>): MountPassportCallbacks = remember(backStack) {
    MountPassportCallbacks(
        onBack = { backStack.popSafe() },
        onNewPassport = { backStack.navigateSafe(MountPassportReadRoute(MountPassportReadRouteSource.AdvancedMount)) },
        onFinish = { id -> backStack.navigateSafe(MountPassportReadRoute(MountPassportReadRouteSource.Complete(id))) },
        onReadUnit = { id ->
            backStack.navigateSafe(MountPassportReadRoute(MountPassportReadRouteSource.Complete(id)))
        },
    )
}

@Composable
internal fun MountPassportArchiveHome(backStack: NavBackStack<NavKey>) {
    MountPassportArchiveEntry(callbacks = callbacks(backStack), ports = ports())
}

@Composable
internal fun MountPassportUnitHome(systemId: Long, backStack: NavBackStack<NavKey>) {
    MountPassportUnitEntry(systemId = systemId, callbacks = callbacks(backStack), ports = ports())
}

@Composable
internal fun MountPassportEditorHome(passportId: String, backStack: NavBackStack<NavKey>) {
    MountPassportEditorEntry(passportId = passportId, callbacks = callbacks(backStack), ports = ports())
}

/** Готовый паспорт заменяет считывание в стеке: «Назад» из предотчёта не возвращает в считывание. */
@Composable
internal fun MountPassportReadHome(route: MountPassportReadRoute, backStack: NavBackStack<NavKey>) {
    MountPassportReadEntry(
        source = route.readSource,
        onDone = { id -> if (backStack.lastOrNull() == route) backStack.replaceCurrent(MountPassportEditorRoute(id)) },
        onCancel = { if (backStack.lastOrNull() == route) backStack.popSafe() },
    )
}

@Composable
private fun ports(): MountPassportPorts = rememberMountPassportPorts()

@Composable
internal fun rememberMountPassportPorts(): MountPassportPorts {
    val dispatchers = koinInject<AppDispatchers>()
    val imagePicker = rememberImagePicker(dispatchers)
    val saver = rememberSettingsFileSaver()
    val renderer = rememberHtmlPdfRenderer(dispatchers)
    val rasterizer = rememberPdfPageRasterizer(dispatchers)
    val exporter = rememberBinaryFileExporter(dispatchers)
    val filePicker = rememberBinaryFilePicker(dispatchers)
    return remember(imagePicker, saver, renderer, rasterizer, exporter, filePicker) {
        MountPassportPorts(
            imagePicker,
            saver,
            renderer,
            rasterizer,
            exporter,
            filePicker,
            vehiclePicker = PassportCatalogPicker,
        )
    }
}

/** Автомобиль предотчёта из каталога `vehicle-ui` (как в быстром монтаже): текстом «марка модель годы». */
private val PassportCatalogPicker: PassportVehiclePicker = { deviceName, onPick, onBack ->
    VehicleSelectEntry(
        request = VehicleSelectRequest(
            mode = VehicleSelectMode.PICK,
            catalog = VehicleCatalog.ALL,
            device = deviceName?.takeIf { it.isNotBlank() }?.let(VehicleDeviceFilter::Device),
        ),
        onPick = { car -> onPick(car.toPassportPick()) },
        onBack = onBack,
    )
}

internal fun CarSummary.toPassportPick(): PassportVehiclePick {
    val years = if (years.first == years.last) "${years.first}" else "${years.first}–${years.last}"
    return PassportVehiclePick(catalogCarId = id.toString(), title = "${fullName()} $years")
}
