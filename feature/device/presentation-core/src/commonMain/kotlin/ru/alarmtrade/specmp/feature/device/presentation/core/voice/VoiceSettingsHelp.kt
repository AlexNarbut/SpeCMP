package ru.alarmtrade.specmp.feature.device.presentation.core.voice

/** Где ставить голос: тексты «Нет голоса» разные на телефоне Android, iPhone и компьютере. */
enum class VoiceSettingsHelp { ANDROID, IOS, DESKTOP }

internal expect val platformVoiceSettingsHelp: VoiceSettingsHelp
