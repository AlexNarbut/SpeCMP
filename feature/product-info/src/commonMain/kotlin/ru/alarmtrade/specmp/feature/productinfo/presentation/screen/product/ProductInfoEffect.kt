package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product

sealed interface ProductInfoEffect {
    /** «Ввести вручную» из сканера: курсор в поле номера. */
    data object FocusQuery : ProductInfoEffect
}
