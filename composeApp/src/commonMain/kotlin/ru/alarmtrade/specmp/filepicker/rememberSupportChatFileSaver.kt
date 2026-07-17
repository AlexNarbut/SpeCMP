package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver

@Composable
expect fun rememberSupportChatFileSaver(): SupportChatFileSaver
