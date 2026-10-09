package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver

@Composable
expect fun rememberSupportChatFileSaver(): SupportChatFileSaver
