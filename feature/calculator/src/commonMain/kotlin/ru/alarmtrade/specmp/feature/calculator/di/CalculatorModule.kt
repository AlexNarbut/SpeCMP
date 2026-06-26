package ru.alarmtrade.specmp.feature.calculator.di

import ru.alarmtrade.specmp.feature.calculator.presentation.CalculatorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val calculatorModule = module {
    viewModelOf(::CalculatorViewModel)
}
