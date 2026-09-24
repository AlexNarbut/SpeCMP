package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver

class FakeSupportChatFileSaver : SupportChatFileSaver {

    var saveResult: SupportChatFileSaveResult = SupportChatFileSaveResult.Saved("/downloads/Pandora Spec/file")

    val savedFiles = mutableListOf<Pair<String, ByteArray>>()

    override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): SupportChatFileSaveResult {
        savedFiles.add(fileName to bytes)
        return saveResult
    }
}
