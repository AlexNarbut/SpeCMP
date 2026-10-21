package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpenResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener

class FakeSupportChatFileOpener : SupportChatFileOpener {

    var result: SupportChatFileOpenResult = SupportChatFileOpenResult.Opened
    val openedBytes = mutableListOf<String>()
    val openedLocal = mutableListOf<String>()

    override suspend fun openBytes(fileName: String, bytes: ByteArray): SupportChatFileOpenResult {
        openedBytes.add(fileName)
        return result
    }

    override suspend fun openLocal(path: String, fileName: String): SupportChatFileOpenResult {
        openedLocal.add(path)
        return result
    }
}
