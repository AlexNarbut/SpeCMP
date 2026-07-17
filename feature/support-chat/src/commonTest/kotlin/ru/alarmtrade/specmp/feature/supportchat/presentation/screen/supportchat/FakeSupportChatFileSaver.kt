package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver

class FakeSupportChatFileSaver : SupportChatFileSaver {

    var saveResult: String? = "/downloads/Pandora Spec/file"

    val savedFiles = mutableListOf<Pair<String, ByteArray>>()

    override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): String? {
        savedFiles.add(fileName to bytes)
        return saveResult
    }
}
