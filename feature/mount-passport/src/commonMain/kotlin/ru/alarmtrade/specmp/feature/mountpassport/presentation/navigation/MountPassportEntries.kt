package ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.core.files.ImagePicker
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage

/** Архив паспортов (плитка главного экрана). */
@Serializable
@SerialName("mount-passport.MountPassportArchiveRoute")
data object MountPassportArchiveRoute : NavKey

/** Паспорта подключённого блока (раздел расширенного монтажа). */
@Serializable
@SerialName("mount-passport.MountPassportUnitRoute")
data class MountPassportUnitRoute(val systemId: Long) : NavKey

/** Предотчёт одного паспорта (после считывания, из ассистента). */
@Serializable
@SerialName("mount-passport.MountPassportEditorRoute")
data class MountPassportEditorRoute(val passportId: String) : NavKey

/**
 * Переходы наружу фичи (считывание, паспорт без блока, PDF — следующий этап):
 * [onNewPassport] — «Новый паспорт» блока, [onNewWithoutUnit] — «Новый паспорт без блока» из архива,
 * [onFinish] — «Дооформить» заготовку, [onReread] — «Перечитать» этап, [onReadUnit] — «Считать блок» у паспорта
 * без блока, [onCreatePdf] — «Сформировать PDF».
 */
@Stable
class MountPassportCallbacks(
    val onBack: () -> Unit,
    val onNewPassport: (systemId: Long) -> Unit = {},
    val onNewWithoutUnit: () -> Unit = {},
    val onFinish: (passportId: String) -> Unit = {},
    val onReread: (passportId: String, stage: PassportReadStage) -> Unit = { _, _ -> },
    val onReadUnit: (passportId: String) -> Unit = {},
    val onCreatePdf: (passportId: String) -> Unit = {},
)

/** Порты платформы, их создаёт корень приложения: выбор фото и сохранение `.dxs` снимка настроек. */
@Stable
class MountPassportPorts(val imagePicker: ImagePicker, val settingsFileSaver: SettingsFileSaver)

@Composable
fun MountPassportArchiveEntry(callbacks: MountPassportCallbacks, ports: MountPassportPorts) {
    MountPassportNavGraph(start = PassportArchiveKey, callbacks = callbacks, ports = ports)
}

@Composable
fun MountPassportUnitEntry(systemId: Long, callbacks: MountPassportCallbacks, ports: MountPassportPorts) {
    MountPassportNavGraph(start = PassportUnitKey(systemId), callbacks = callbacks, ports = ports)
}

@Composable
fun MountPassportEditorEntry(passportId: String, callbacks: MountPassportCallbacks, ports: MountPassportPorts) {
    MountPassportNavGraph(start = PassportEditorKey(passportId), callbacks = callbacks, ports = ports)
}
