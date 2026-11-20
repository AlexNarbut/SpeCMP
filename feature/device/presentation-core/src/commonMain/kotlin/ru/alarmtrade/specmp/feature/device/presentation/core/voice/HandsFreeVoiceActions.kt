package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import androidx.compose.runtime.Immutable

@Immutable
data class HandsFreeVoiceActions(
    val onToggle: () -> Unit = {},
    val onTipDismiss: () -> Unit = {},
    val onNoVoiceDismiss: () -> Unit = {},
    val onInstallVoice: () -> Unit = {},
)

/** Кнопка «Озвучка» в шапке экрана шага: состояние и действия общей VM ([rememberHandsFreeVoice]). */
@Immutable
data class HandsFreeVoice(val state: HandsFreeVoiceState, val actions: HandsFreeVoiceActions = HandsFreeVoiceActions())
