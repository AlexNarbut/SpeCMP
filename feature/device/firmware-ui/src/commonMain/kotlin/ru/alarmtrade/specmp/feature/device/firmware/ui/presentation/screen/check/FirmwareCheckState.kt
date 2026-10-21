package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind

@Immutable
data class FirmwareCheckState(
    val deviceName: String = "",
    val deviceId: Long? = null,
    val content: CheckContent = CheckContent.Loading,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val dialog: CheckDialog? = null,
    /** Команда входа в загрузчик не удалась (нет связи, блок не ответил) — баннер над списком. */
    val bootFailed: Boolean = false
)

sealed interface CheckContent {
    data object Loading : CheckContent

    /** Список модулей блока не получен. */
    data object Error : CheckContent

    @Immutable
    data class Ready(val modules: List<ModuleCardUi>) : CheckContent
}

/** Карточка модуля: только примитивы и enum — устойчива для Compose. */
@Immutable
data class ModuleCardUi(
    val id: Long,
    val kind: FirmwareModuleKind,
    val title: String,
    val currentVersion: CurrentVersionUi,
    val status: CardStatusUi
)

sealed interface CurrentVersionUi {
    /** Версия ещё читается с блока. */
    data object Reading : CurrentVersionUi

    data class Known(val version: String) : CurrentVersionUi

    /** Прочитать не удалось или у модуля нет версии — строка не показывается. */
    data object Absent : CurrentVersionUi
}

/** Что известно про серверную версию модуля. */
sealed interface CardStatusUi {
    data object Searching : CardStatusUi

    /** На сервере новее установленной (или установленная неизвестна). */
    data class UpdateAvailable(val version: String, val description: String) : CardStatusUi

    /** Установленная версия актуальна. */
    data class Actual(val version: String) : CardStatusUi

    /** Версию с сервера получить не удалось. */
    data object Failed : CardStatusUi

    /** Режим обновления модуля приложение не поддерживает. */
    data object NotSupported : CardStatusUi
}

sealed interface CheckDialog {
    data class Description(val moduleId: Long, val version: String, val description: String) : CheckDialog

    /** [sending] — команда входа в загрузчик отправляется (кнопки недоступны). */
    data class ConfirmBoot(val moduleId: Long, val sending: Boolean = false) : CheckDialog

    /**
     * PIN программирования (только BLE). [wrongCount] растёт с каждым неверным вводом — по нему экран очищает поле;
     * [wrong] — подсвечивать ошибку (молчаливая первая попытка PIN у пользователя не спрашивала); [lockedFor] —
     * блок не принимает ввод ещё столько времени (`null` — ввод разрешён).
     */
    data class Pin(
        val moduleId: Long,
        val sending: Boolean = false,
        val wrong: Boolean = false,
        val wrongCount: Int = 0,
        val lockedFor: Duration? = null
    ) : CheckDialog
}

/** Действия экрана проверки (stateless-контент получает их от VM или заглушки в галерее состояний). */
class FirmwareCheckActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onChannelSelected: (FirmwareChannel) -> Unit = {},
    val onRefreshModule: (Long) -> Unit = {},
    val onDescriptionClick: (Long) -> Unit = {},
    val onUpdateClick: (Long) -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onConfirmBoot: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {}
)
