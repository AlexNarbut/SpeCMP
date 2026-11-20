package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.files.rememberImagePicker
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportArchiveEntry
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportCallbacks
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportEditorEntry
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportUnitEntry
import ru.alarmtrade.specmp.filepicker.rememberSettingsFileSaver

/** Считывание, паспорт без блока и PDF — следующий этап: их колбэки пока не подключены (значения по умолчанию). */
@Composable
internal fun MountPassportArchiveHome(onBack: () -> Unit) {
    MountPassportArchiveEntry(callbacks = remember(onBack) { MountPassportCallbacks(onBack = onBack) }, ports = ports())
}

@Composable
internal fun MountPassportUnitHome(systemId: Long, onBack: () -> Unit) {
    MountPassportUnitEntry(
        systemId = systemId,
        callbacks = remember(onBack) { MountPassportCallbacks(onBack = onBack) },
        ports = ports(),
    )
}

@Composable
internal fun MountPassportEditorHome(passportId: String, onBack: () -> Unit) {
    MountPassportEditorEntry(
        passportId = passportId,
        callbacks = remember(onBack) { MountPassportCallbacks(onBack = onBack) },
        ports = ports(),
    )
}

@Composable
private fun ports(): MountPassportPorts {
    val imagePicker = rememberImagePicker(koinInject())
    val saver = rememberSettingsFileSaver()
    return remember(imagePicker, saver) { MountPassportPorts(imagePicker, saver) }
}
