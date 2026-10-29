package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title

/** Фазы экрана прошивки по COM-порту. */
sealed interface SerialPhase {
    /** Блок на связи: «Обновить из файла». */
    data object Ready : SerialPhase

    /**
     * Файл выбран и подходит блоку — подтверждение. [path] — копия файла для SDK (на экран не выводится): живёт в фазе,
     * чтобы подтверждение забрало его атомарно вместе с переходом. [fileVersion] — версия из заголовка файла
     * (`null` — нет); [older] — она старее версии блока.
     */
    data class Confirm(val fileName: String, val path: String, val fileVersion: String?, val older: Boolean) :
        SerialPhase

    /** SDK шлёт GOBOOT: пользователь перезагружает блок по питанию. */
    data object WaitingBoot : SerialPhase

    data object CheckingBoot : SerialPhase

    data class Transfer(val percent: Int?) : SerialPhase

    data object Verify : SerialPhase

    data object Apply : SerialPhase

    /** Файл записан; [writtenVersion] — версия из заголовка записанного файла (`null` — в заголовке её нет). */
    data class Success(val writtenVersion: String?) : SerialPhase

    data class Error(val failure: SerialFirmwareFailure) : SerialPhase
}

/** Выбранный файл не годится (баннер на шаге «Обновить из файла»). */
sealed interface SerialFileProblem {
    /** Не `.frm`: заголовок не разобран. */
    data object NotFrm : SerialFileProblem

    /** Прошивка для другого блока: [fileSystemName] из файла, [deviceSystemName] — подключённый блок. */
    data class OtherDevice(val fileSystemName: String, val deviceSystemName: String) : SerialFileProblem
}

@Immutable
data class SerialFirmwareState(
    /** Блок из сессии (`DeviceSession`); `null` — связи нет или блок ещё не прочитан. */
    val device: AlarmDevice? = null,
    /** Блок в загрузчике: его версия может быть версией загрузчика, а не прошивки. */
    val deviceInBoot: Boolean = false,
    val phase: SerialPhase = SerialPhase.Ready,
    /** Выбранный файл не удалось взять (баннер на шаге «Обновить из файла»). */
    val fileError: FileError? = null,
    val fileProblem: SerialFileProblem? = null
) {
    /** Подзаголовок «имя  systemId». */
    val deviceTitle: String? get() = device?.title

    val deviceVersion: String? get() = device?.firmwareVersion

    /** «Назад» заблокирован (замок): идёт обновление, блок нельзя бросить на полпути. */
    val locked: Boolean
        get() = when (phase) {
            SerialPhase.WaitingBoot,
            SerialPhase.CheckingBoot,
            is SerialPhase.Transfer,
            SerialPhase.Verify,
            SerialPhase.Apply -> true

            SerialPhase.Ready,
            is SerialPhase.Confirm,
            is SerialPhase.Success,
            is SerialPhase.Error -> false
        }
}

/** Действия экрана прошивки по COM (stateless-контент получает их от VM или заглушки в галерее состояний). */
class SerialFirmwareActions(
    val onBack: () -> Unit = {},
    val onFileClick: () -> Unit = {},
    val onConfirmUpdate: () -> Unit = {},
    val onCancelConfirm: () -> Unit = {},
    val onResultOk: () -> Unit = {}
)

/** Одноразовые события экрана прошивки по COM. */
sealed interface SerialFirmwareEffect {
    /** Выйти из flow устройства (сессию закрывает хост). */
    data object Exit : SerialFirmwareEffect
}
