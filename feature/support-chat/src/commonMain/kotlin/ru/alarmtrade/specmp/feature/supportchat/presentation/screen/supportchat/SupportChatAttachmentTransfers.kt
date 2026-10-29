package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import co.touchlab.kermit.Logger
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpenResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver

/**
 * Скачивание и открытие вложений чата (отдельная от ленты ответственность VM). Результат — эффект для экрана,
 * `null` — сообщать нечего (открыто или пользователь отменил системный диалог). Индикатор и запуск — у VM.
 */
internal class SupportChatAttachmentTransfers(
    private val repository: SupportChatRepository,
    private val fileSaver: SupportChatFileSaver,
    private val fileOpener: SupportChatFileOpener,
    private val dispatchers: AppDispatchers
) {
    private val logger = Logger.withTag("SupportChatAttachmentTransfers")

    /** Скачать вложение в «Загрузки». */
    suspend fun download(attachment: SupportChatUiAttachment): SupportChatEffect? {
        val url = attachment.url ?: return null
        return when (val response = withContext(dispatchers.io) { repository.downloadFile(url) }) {
            is Response.Success -> save(attachment.name, response.value)

            is Response.Error -> {
                logger.d(response.exception) { "Failed to download attachment" }
                SupportChatEffect.AttachmentDownloadFailed
            }
        }
    }

    /** Открыть во внешнем приложении: своё неподтверждённое — из локальной копии, остальные — скачать и открыть. */
    suspend fun open(attachment: SupportChatUiAttachment): SupportChatEffect? {
        val localPath = attachment.localPath
        val url = attachment.url
        val result = withContext(dispatchers.io) {
            when {
                localPath != null -> fileOpener.openLocal(localPath, attachment.name)

                url != null -> when (val response = repository.downloadFile(url)) {
                    is Response.Success -> fileOpener.openBytes(attachment.name, response.value)
                    is Response.Error -> SupportChatFileOpenResult.Failed
                }

                else -> SupportChatFileOpenResult.Failed
            }
        }
        return when (result) {
            SupportChatFileOpenResult.Opened -> null
            SupportChatFileOpenResult.NoApp -> SupportChatEffect.AttachmentOpenFailed(noApp = true)
            SupportChatFileOpenResult.Failed -> SupportChatEffect.AttachmentOpenFailed(noApp = false)
        }
    }

    private suspend fun save(fileName: String, bytes: ByteArray): SupportChatEffect? =
        when (val result = withContext(dispatchers.io) { fileSaver.saveToDownloads(fileName, bytes) }) {
            is SupportChatFileSaveResult.Saved -> SupportChatEffect.AttachmentDownloaded(result.path)

            // Пользователь сам отменил системный диалог — не ошибка.
            SupportChatFileSaveResult.Cancelled -> null

            SupportChatFileSaveResult.PermissionDenied -> SupportChatEffect.AttachmentSavePermissionDenied

            SupportChatFileSaveResult.Failed -> SupportChatEffect.AttachmentDownloadFailed
        }
}
