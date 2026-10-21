package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener

@Composable
expect fun rememberSupportChatFileOpener(): SupportChatFileOpener
