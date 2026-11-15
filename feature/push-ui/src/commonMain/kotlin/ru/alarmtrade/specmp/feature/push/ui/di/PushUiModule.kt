package ru.alarmtrade.specmp.feature.push.ui.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules.PushRulesViewModel

val pushUiModule = module {
    viewModelOf(::PushRulesViewModel)
}
