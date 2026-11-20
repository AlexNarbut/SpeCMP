package ru.alarmtrade.specmp.feature.device.presentation.core.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.HandsFreeVoiceViewModel

val handsFreeVoiceModule = module {
    viewModelOf(::HandsFreeVoiceViewModel)
}
