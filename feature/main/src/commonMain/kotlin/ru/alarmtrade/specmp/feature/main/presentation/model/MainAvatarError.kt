package ru.alarmtrade.specmp.feature.main.presentation.model

sealed interface MainAvatarError {
    data object ImageNotRead : MainAvatarError

    data object CameraDenied : MainAvatarError

    data class UploadFailed(val error: MainError) : MainAvatarError
}
