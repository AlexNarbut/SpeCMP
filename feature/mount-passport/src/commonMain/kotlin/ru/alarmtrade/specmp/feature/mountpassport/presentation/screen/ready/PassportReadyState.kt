package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/** [shareOnReady] — «Поделиться» с финала быстрого монтажа: отправить первый собранный документ. */
@Immutable
internal data class PassportReadyArgs(val passportId: String, val shareOnReady: Boolean = false)

/** Сформированный PDF: [builtFor] — `updatedAt` паспорта, из которого он собран (кэш до следующей правки). */
@Immutable
internal class PassportPdf(val bytes: ByteArray, val pageCount: Int, val fileName: String, val builtFor: Instant)

@Immutable
internal sealed interface DocumentStatus {
    data object Forming : DocumentStatus

    data object Ready : DocumentStatus

    data class Failed(val error: PassportError) : DocumentStatus
}

internal enum class DocumentAction { SHARE, SAVE, OPEN }

/**
 * «Паспорт готов» и живой лист широкого окна. [refreshing] — PDF пересобирается после правки, старые листы на
 * экране; [pages] — листы картинками по мере прокрутки, шириной [pageWidthPx]; [busy] — действие с файлом идёт.
 */
@Immutable
internal data class PassportReadyState(
    val passportId: String,
    val canShare: Boolean,
    val passport: MountPassport? = null,
    val document: DocumentStatus = DocumentStatus.Forming,
    val pdf: PassportPdf? = null,
    val refreshing: Boolean = false,
    val pages: PersistentMap<Int, ImageBitmap> = persistentMapOf(),
    val pageWidthPx: Int = 0,
    val busy: DocumentAction? = null,
    val shareOnReady: Boolean = false,
) {
    val pageCount: Int get() = pdf?.pageCount ?: 0

    val hasSettings: Boolean get() = passport?.settingsSnapshot != null
}
