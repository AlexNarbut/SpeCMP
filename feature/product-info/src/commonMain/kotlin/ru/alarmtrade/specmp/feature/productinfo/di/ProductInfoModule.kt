package ru.alarmtrade.specmp.feature.productinfo.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.productinfo.data.datasource.ProductInfoDataSource
import ru.alarmtrade.specmp.feature.productinfo.data.datasource.SpecialistProductInfoDataSource
import ru.alarmtrade.specmp.feature.productinfo.data.repository.SpecialistProductInfoRepository
import ru.alarmtrade.specmp.feature.productinfo.domain.repository.ProductInfoRepository
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoArgs
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoViewModel

val productInfoModule = module {
    singleOf(::SpecialistProductInfoDataSource) bind ProductInfoDataSource::class
    singleOf(::SpecialistProductInfoRepository) bind ProductInfoRepository::class
    viewModel { params ->
        ProductInfoViewModel(
            args = params.get<ProductInfoArgs>(),
            repository = get(),
            permissions = get(),
            dispatchers = get(),
        )
    }
}
