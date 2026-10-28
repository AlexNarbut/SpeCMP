package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.error.RequestError
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

@Immutable
data class AdvancedMountRootState(
    /** Последний прочитанный блок; `null` — ещё не прочитан (экран перекрыт обёрткой «Подключение…»). */
    val device: AlarmDevice? = null,
    val transport: ConnectionTransport? = null,
    /** IMEI GSM-модема; `null` — не прочитан (строка не показывается). */
    val imei: String? = null,
    val content: MountContent = MountContent.Loading,
    val unreadCount: Int = 0,
    /** Доступен удалённый монтаж — иконка синхронизации в верхней панели. */
    val remoteAvailable: Boolean = false,
    /** Кнопка «Информация о товаре» (у пользователя включён поиск товара). */
    val productInfo: Boolean = false,
    val dialog: MountDialog? = null
) {
    /** Заводские CAN-настройки: разделы с [MountSection.needsCan] заблокированы до CAN-прошивки. */
    val canLocked: Boolean
        get() = device?.can?.isFactory == true && content is MountContent.Ready
}

/** Содержимое области разделов (шапка блока видна всегда). */
@Immutable
sealed interface MountContent {
    /** Читаем возможности и проверяем блок на сервере. */
    data object Loading : MountContent

    /** Проверка на сервере не удалась (сеть / сервер) — «Исправить». */
    data class VerifyError(val error: RequestError) : MountContent

    /** Сервер ограничил доступ к блоку; [message] — причина от сервера. */
    data class Denied(val message: String) : MountContent

    /**
     * Разделы; [unsupported] — возможности блока не прочитаны, показаны только базовые разделы. [modelNotInDatabase] —
     * причина: модели блока нет в базе устройств телефона (база не скачана/устарела), а не неподдерживаемый блок.
     */
    data class Ready(
        val sections: List<MountSection>,
        val unsupported: Boolean,
        val modelNotInDatabase: Boolean = false
    ) : MountContent
}

enum class MountDialog {
    /** «Внимание»: нужна CAN-прошивка → «Перейти к обновлению». */
    CAN_UPDATE,

    /** Подсказка у CAN-кода в шапке. */
    CAN_HINT
}
