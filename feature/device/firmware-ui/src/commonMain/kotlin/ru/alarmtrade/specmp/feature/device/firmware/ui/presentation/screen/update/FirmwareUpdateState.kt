package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveArgs

/** Фазы экрана «Обновление прошивки» (блок в загрузчике). Часть выводится из связи, а не из действий пользователя. */
sealed interface UpdatePhase {
    /** Читаем данные блока и подсистемы после входа в загрузчик. */
    data object Loading : UpdatePhase

    /** Блок перезагружается в загрузчик (связь рвётся намеренно). */
    data object Entering : UpdatePhase

    /** Выбор подсистемы, канала и источника прошивки. */
    data object Source : UpdatePhase

    /** Подсистемы блока определить не удалось. */
    data object DataError : UpdatePhase

    /** Запрос последней версии прошивки на сервере. */
    data object Download : UpdatePhase

    /** Подтверждение версии перед обновлением. */
    data object Confirm : UpdatePhase

    data object Prepare : UpdatePhase

    data class Transfer(val percent: Int?) : UpdatePhase

    data object Verify : UpdatePhase

    data object Apply : UpdatePhase

    data object Success : UpdatePhase

    /** После «OK»: ждём возврата блока в рабочий режим. */
    data object Finishing : UpdatePhase

    data class Error(val failure: FailureKind) : UpdatePhase

    /** Связь с блоком прервана; [updating] — обновление шло. */
    data class Lost(val updating: Boolean) : UpdatePhase
}

/** Причина неудачи обновления на языке экрана (под текст ошибки). */
enum class FailureKind {
    Crc,
    Timeout,
    LostPacket,
    WrongPacketLength,
    DeviceNotReady,
    ApplyingError,
    Preparation,
    Unknown
}

/** Что обновляем: основная программа блока или Bluetooth-модуль; от этого зависят подзаголовок и данные. */
enum class UpdateProfile { Main, Bluetooth }

@Immutable
data class FirmwareUpdateState(
    val phase: UpdatePhase = UpdatePhase.Loading,
    val profile: UpdateProfile = UpdateProfile.Main,
    /** «Название  ID» блока или «имя Bluetooth-модуля» (по профилю). */
    val deviceTitle: String = "",
    val moduleTitles: List<String> = emptyList(),
    val selectedModule: Int = 0,
    val currentVersion: String? = null,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val confirm: ConfirmUi? = null,
    /** Последняя версия с сервера не получена (баннер на выборе источника). */
    val sourceError: Boolean = false,
    val exitDialog: Boolean = false,
    /** Блок сейчас в загрузчике (на экране успеха это значит «после прошивки блок остался в загрузчике»). */
    val inBoot: Boolean = false,
    /** Команда выхода из загрузчика (перезагрузка) доступна: только BLE. */
    val canExitBoot: Boolean = false,
    /** Команда выхода из загрузчика не прошла (баннер на выборе источника). */
    val exitError: Boolean = false
) {
    /** «Назад» заблокирован (замок): процесс идёт, и блок нельзя бросить на полпути. */
    val locked: Boolean
        get() = when (phase) {
            UpdatePhase.Entering,
            UpdatePhase.Download,
            UpdatePhase.Prepare,
            is UpdatePhase.Transfer,
            UpdatePhase.Verify,
            UpdatePhase.Apply,
            UpdatePhase.Success,
            UpdatePhase.Finishing -> true

            UpdatePhase.Loading,
            UpdatePhase.Source,
            UpdatePhase.DataError,
            UpdatePhase.Confirm,
            is UpdatePhase.Error,
            is UpdatePhase.Lost -> false
        }
}

/** Данные шага подтверждения. [older] — выбрана версия старее установленной. */
@Immutable
data class ConfirmUi(val currentVersion: String?, val newVersion: String, val description: String, val older: Boolean)

/** Действия экрана обновления (stateless-контент получает их от VM или заглушки в галерее состояний). */
class FirmwareUpdateActions(
    val onBack: () -> Unit = {},
    val onModuleSelected: (Int) -> Unit = {},
    val onChannelSelected: (FirmwareChannel) -> Unit = {},
    val onDownloadClick: () -> Unit = {},
    val onArchiveClick: () -> Unit = {},
    val onConfirmUpdate: () -> Unit = {},
    val onCancelConfirm: () -> Unit = {},
    val onResultOk: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSupportClick: () -> Unit = {},
    val onReconnect: () -> Unit = {},
    val onDisconnect: () -> Unit = {},
    val onExitBootClick: () -> Unit = {},
    val onExitDialogDismiss: () -> Unit = {},
    val onExitConfirm: () -> Unit = {}
)

/** Одноразовые события экрана обновления. */
sealed interface FirmwareUpdateEffect {
    /** Открыть чат техподдержки («Написать в поддержку» на ошибке). */
    data object OpenSupportChat : FirmwareUpdateEffect

    /** Открыть архив прошивок выбранного модуля и канала. */
    data class OpenArchive(val args: FirmwareArchiveArgs) : FirmwareUpdateEffect

    /** Выйти из режима обновления (закрыть сессию и flow). */
    data object Exit : FirmwareUpdateEffect
}
