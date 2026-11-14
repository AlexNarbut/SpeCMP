package ru.alarmtrade.specmp.feature.productinfo.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo

interface ProductInfoRepository {
    /** Ошибка «нет такого номера» — `ProductNotFoundException`, сеть и сервер — `AppException`. */
    suspend fun find(id: Long): Response<ProductInfo>

    /** Функция включена пользователю сервером (`UserAppFeature.PRODUCT_SEARCH`). */
    suspend fun isEnabled(): Response<Boolean>
}
