package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.files.ImageCropRect
import ru.alarmtrade.specmp.core.files.ImageSource

/** Смена аватара в шапке шторки: тап по аватару, выбор источника, обрезка, закрытие ошибки. */
@Immutable
data class MainAvatarActions(
    val onAvatarClick: () -> Unit,
    val onSourceSelect: (ImageSource) -> Unit,
    val onSourceDismiss: () -> Unit,
    val onErrorDismiss: () -> Unit,
    val onCropConfirm: (ImageCropRect) -> Unit,
    val onCropCancel: () -> Unit,
)
