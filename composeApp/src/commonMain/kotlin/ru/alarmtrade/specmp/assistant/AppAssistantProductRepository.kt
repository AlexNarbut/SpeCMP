package ru.alarmtrade.specmp.assistant

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantProduct
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantProductKit
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantProductNotFoundException
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantProductRepository
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductNotFoundException
import ru.alarmtrade.specmp.feature.productinfo.domain.repository.ProductInfoRepository

/** Ассистент не зависит от `feature/product-info`: связывает корень приложения. */
internal class AppAssistantProductRepository(private val products: ProductInfoRepository) : AssistantProductRepository {

    override fun enabled(): Flow<Response<Boolean>> = flow { emit(products.isEnabled()) }

    override suspend fun summary(id: Long): Response<AssistantProduct> = when (val response = products.find(id)) {
        is Response.Success -> response.map { it.toAssistant() }

        is Response.Error -> if (response.exception is ProductNotFoundException) {
            Response.Error(AssistantProductNotFoundException(response.exception))
        } else {
            response
        }
    }
}

private fun ProductInfo.toAssistant(): AssistantProduct = AssistantProduct(
    id = product?.id,
    name = product?.name ?: kit?.name.orEmpty(),
    serial = product?.serial ?: kit?.serial,
    factoryDate = product?.factoryDate,
    kit = kit?.let { AssistantProductKit(it.name, it.components.size) },
)
