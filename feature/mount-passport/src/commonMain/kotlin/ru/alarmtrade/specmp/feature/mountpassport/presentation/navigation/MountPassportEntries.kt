package ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.core.files.BinaryFileExporter
import ru.alarmtrade.specmp.core.files.BinaryFilePicker
import ru.alarmtrade.specmp.core.files.HtmlPdfRenderer
import ru.alarmtrade.specmp.core.files.ImagePicker
import ru.alarmtrade.specmp.core.files.PdfPageRasterizer
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehiclePick
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadPane

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

/** Считывание блока в паспорт; по готовности маршрут заменяется предотчётом ([MountPassportEditorRoute]). */
@Serializable
@SerialName("mount-passport.MountPassportReadRoute")
data class MountPassportReadRoute(val source: MountPassportReadRouteSource) : NavKey {
    val readSource: PassportReadSource
        get() = when (source) {
            MountPassportReadRouteSource.AdvancedMount -> PassportReadSource.AdvancedMount
            is MountPassportReadRouteSource.Complete -> PassportReadSource.Complete(source.passportId)
        }
}

/**
 * Источник считывания в маршруте: расширенный монтаж или дооформление паспорта. Итог мастера быстрого монтажа в
 * маршрут не кладётся — мастер встраивает [MountPassportReadEntry] слотом с `PassportReadSource.FastMount`.
 */
@Serializable
@SerialName("mount-passport.MountPassportReadRouteSource")
sealed interface MountPassportReadRouteSource {
    @Serializable
    @SerialName("mount-passport.MountPassportReadRouteSource.AdvancedMount")
    data object AdvancedMount : MountPassportReadRouteSource

    @Serializable
    @SerialName("mount-passport.MountPassportReadRouteSource.Complete")
    data class Complete(val passportId: String) : MountPassportReadRouteSource
}

/**
 * Переходы наружу фичи — в считывание блока ([MountPassportReadRoute]): [onNewPassport] — «Новый паспорт» блока,
 * [onFinish] — «Дооформить» заготовку, [onReadUnit] — «Считать блок» у паспорта без блока. PDF, «Перечитать» и
 * паспорт без блока — внутри фичи.
 */
@Stable
class MountPassportCallbacks(
    val onBack: () -> Unit,
    val onNewPassport: (systemId: Long) -> Unit = {},
    val onFinish: (passportId: String) -> Unit = {},
    val onReadUnit: (passportId: String) -> Unit = {},
)

/**
 * Порты платформы, их создаёт корень приложения: выбор фото, сохранение `.dxs` снимка настроек, HTML → PDF,
 * листы PDF картинками, сохранение / отправка готового файла и выбор архива для импорта; [vehiclePicker] — каталог
 * автомобилей (`deviceName` — модель блока для фильтра), `null` — автомобиль только текстом.
 */
@Stable
class MountPassportPorts(
    val imagePicker: ImagePicker,
    val settingsFileSaver: SettingsFileSaver,
    val pdfRenderer: HtmlPdfRenderer,
    val pageRasterizer: PdfPageRasterizer,
    val fileExporter: BinaryFileExporter,
    val filePicker: BinaryFilePicker,
    val vehiclePicker: PassportVehiclePicker? = null,
)

typealias PassportVehiclePicker =
    @Composable (deviceName: String?, onPick: (PassportVehiclePick) -> Unit, onBack: () -> Unit) -> Unit

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

/** С чего открыть паспорт: предотчёт, «Паспорт готов» или «Паспорт готов» с отправкой документа. */
enum class MountPassportDocumentStart { EDITOR, READY, SHARE }

/**
 * Паспорт внутри мастера быстрого монтажа: [MountPassportCallbacks.onBack] — назад на финал мастера. «Изменить» на
 * «Паспорт готов», открытом первым, заменяет его предотчётом.
 */
@Composable
fun MountPassportDocumentEntry(
    passportId: String,
    start: MountPassportDocumentStart,
    callbacks: MountPassportCallbacks,
    ports: MountPassportPorts,
) {
    val key = when (start) {
        MountPassportDocumentStart.EDITOR -> PassportEditorKey(passportId)
        MountPassportDocumentStart.READY -> PassportReadyKey(passportId)
        MountPassportDocumentStart.SHARE -> PassportReadyKey(passportId, share = true)
    }
    MountPassportNavGraph(start = key, callbacks = callbacks, ports = ports)
}

/**
 * Считывание блока: [onDone] — паспорт сохранён (открыть предотчёт с заменой в стеке), [onCancel] — «Отмена» или
 * «Назад», паспорт не создаётся. Быстрый монтаж передаёт `PassportReadSource.FastMount` с итогом мастера.
 */
@Composable
fun MountPassportReadEntry(source: PassportReadSource, onDone: (passportId: String) -> Unit, onCancel: () -> Unit) {
    PassportReadPane(source = source, onDone = onDone, onCancel = onCancel)
}
