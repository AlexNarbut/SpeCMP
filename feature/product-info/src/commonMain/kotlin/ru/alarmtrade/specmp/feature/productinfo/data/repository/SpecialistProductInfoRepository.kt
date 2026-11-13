package ru.alarmtrade.specmp.feature.productinfo.data.repository

import ru.alarmtrade.common.core.exceptions.ContentParsingException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.response.mapException
import ru.alarmtrade.specialist.domain.errors.alarmDevice.DevicePackageInformationNotFoundException
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.productinfo.data.datasource.ProductInfoDataSource
import ru.alarmtrade.specmp.feature.productinfo.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductNotFoundException
import ru.alarmtrade.specmp.feature.productinfo.domain.repository.ProductInfoRepository

internal class SpecialistProductInfoRepository(private val dataSource: ProductInfoDataSource) : ProductInfoRepository {

    override suspend fun find(id: Long): Response<ProductInfo> =
        when (val response = dataSource.packageInformation(id)) {
            is SpecialistResponse.Success -> Response.Success(response.value.toDomain())
            is SpecialistResponse.Error -> response.toFindError(id)
        }

    /**
     * SDK заворачивает в `DevicePackageInformationNotFoundException` любую ошибку запроса, в том числе обрыв сети.
     * Сеть, таймаут и ответ сервера с ошибкой показываем как есть; «не найден» — всё остальное, включая пустой ответ
     * (на неизвестный номер сервер отдаёт тело без товара — оно не разбирается).
     */
    private fun SpecialistResponse.Error.toFindError(id: Long): Response.Error {
        val sdkError = exception
        if (sdkError !is DevicePackageInformationNotFoundException) return toResponse() as Response.Error
        val cause = sdkError.cause ?: return Response.Error(ProductNotFoundException(id, sdkError))
        val unwrapped = mapException { cause }.toResponse() as Response.Error
        val transport = unwrapped.exception.appError
        val isTransport = transport is ApiError && cause !is ContentParsingException
        return if (isTransport) unwrapped else Response.Error(ProductNotFoundException(id, cause))
    }
}
