package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles

class FakeSupportChatAttachmentFiles : SupportChatAttachmentFiles {

    val discarded = mutableListOf<String>()

    override fun discard(path: String) {
        discarded.add(path)
    }
}
