package ru.alarmtrade.specmp.core.files

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.alarmtrade.specmp.core.ui.theme.DocumentFontFile
import ru.alarmtrade.specmp.core.ui.theme.readDocumentFontFiles

internal class DocumentFontCache(private val load: suspend () -> List<DocumentFontFile> = ::readDocumentFontFiles) {
    private val mutex = Mutex()
    private var fonts: List<DocumentFontFile>? = null

    suspend fun get(): List<DocumentFontFile> = mutex.withLock { fonts ?: load().also { fonts = it } }
}
