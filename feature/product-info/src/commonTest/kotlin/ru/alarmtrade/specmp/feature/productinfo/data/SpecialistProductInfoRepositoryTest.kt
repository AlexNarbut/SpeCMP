package ru.alarmtrade.specmp.feature.productinfo.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.exceptions.ContentParsingException
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageComponent
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageConfigurationInfo
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageFirmwareInfo
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageInformation
import ru.alarmtrade.specialist.domain.errors.alarmDevice.DevicePackageInformationNotFoundException
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.productinfo.data.datasource.ProductInfoDataSource
import ru.alarmtrade.specmp.feature.productinfo.data.repository.SpecialistProductInfoRepository
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductNotFoundException

class SpecialistProductInfoRepositoryTest {

    private val source = FakeProductInfoDataSource()
    private val repository = SpecialistProductInfoRepository(source)

    @Test
    fun packageIsMappedBlankStringsBecomeNull() = runTest {
        source.response = SpecialistResponse.Success(sdkPackage())

        val info = assertIs<Response.Success<ProductInfo>>(repository.find(107131)).value

        assertEquals(107131L, source.requestedId)
        assertEquals("Pandora DX-4GS", info.product.name)
        assertEquals("4G24 1003 5871", info.product.serial)
        assertEquals(LocalDate(2024, 10, 14), info.product.factoryDate)
        assertEquals("3.57", info.product.firmwares.single().version)
        val kit = requireNotNull(info.kit)
        assertNull(kit.serial)
        assertEquals(LocalDate(2024, 10, 21), kit.packageDate)
        val component = kit.components.single()
        assertNull(component.serial)
        assertNull(component.firmwares.single().version)
    }

    @Test
    fun withoutKitKitIsNull() = runTest {
        source.response = SpecialistResponse.Success(sdkPackage().copy(configuration = null))

        assertNull(assertIs<Response.Success<ProductInfo>>(repository.find(1)).value.kit)
    }

    @Test
    fun wrappedNetworkFailureIsCommonError() = runTest {
        source.response = SpecialistResponse.Error.Network(
            DevicePackageInformationNotFoundException(cause = NetworkIsNotAvailableException()),
        )

        val error = assertIs<Response.Error>(repository.find(1))

        assertEquals(ApiError.NoConnection, error.exception.appError)
    }

    @Test
    fun unparsedBodyIsNotFound() = runTest {
        source.response = SpecialistResponse.Error.Network(
            DevicePackageInformationNotFoundException(cause = ContentParsingException("Find", IllegalStateException())),
        )

        val error = assertIs<Response.Error>(repository.find(42))

        assertEquals(42L, assertIs<ProductNotFoundException>(error.exception).id)
    }

    @Test
    fun notFoundWithoutCauseIsNotFound() = runTest {
        source.response = SpecialistResponse.Error.General(DevicePackageInformationNotFoundException())

        assertIs<ProductNotFoundException>(assertIs<Response.Error>(repository.find(7)).exception)
    }
}

private class FakeProductInfoDataSource : ProductInfoDataSource {
    var response: SpecialistResponse<DevicePackageInformation> = SpecialistResponse.Success(sdkPackage())
    var requestedId: Long? = null

    override suspend fun packageInformation(id: Long): SpecialistResponse<DevicePackageInformation> {
        requestedId = id
        return response
    }
}

private fun sdkPackage(): DevicePackageInformation = DevicePackageInformation(
    main = DevicePackageComponent(
        deviceId = 107131,
        name = "Pandora DX-4GS",
        serial = "4G24 1003 5871",
        factoryDate = LocalDateTime(2024, 10, 14, 10, 0),
        firmwares = listOf(DevicePackageFirmwareInfo("Основной", "3.57", LocalDateTime(2024, 10, 14, 10, 0))),
    ),
    configuration = DevicePackageConfigurationInfo(
        name = "Pandora DX-4GS · базовый",
        serial = " ",
        packageDate = LocalDateTime(2024, 10, 21, 9, 30),
        components = listOf(
            DevicePackageComponent(
                deviceId = null,
                name = "Сирена Pandora PS-331",
                serial = "",
                firmwares = listOf(DevicePackageFirmwareInfo("Основной", "", null)),
            ),
        ),
    ),
)
