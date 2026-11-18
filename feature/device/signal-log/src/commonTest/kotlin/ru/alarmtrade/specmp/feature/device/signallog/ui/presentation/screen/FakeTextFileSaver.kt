package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

import ru.alarmtrade.specmp.core.files.TextFileSaveResult
import ru.alarmtrade.specmp.core.files.TextFileSaver
import ru.alarmtrade.specmp.core.files.TextFileType

internal class FakeTextFileSaver(var result: TextFileSaveResult? = null) : TextFileSaver {
    val saves = mutableListOf<Triple<String, TextFileType, String>>()

    override suspend fun save(suggestedName: String, type: TextFileType, text: String): TextFileSaveResult {
        saves += Triple(suggestedName, type, text)
        return result ?: TextFileSaveResult.Saved(suggestedName)
    }
}
