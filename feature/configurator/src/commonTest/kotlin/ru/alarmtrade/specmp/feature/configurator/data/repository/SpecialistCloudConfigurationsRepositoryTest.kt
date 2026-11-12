package ru.alarmtrade.specmp.feature.configurator.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.AppLanguage
import ru.alarmtrade.specialist.domain.cars.CarFullInformation
import ru.alarmtrade.specialist.domain.configuration.CarConfiguration
import ru.alarmtrade.specialist.domain.configuration.CarConfigurationInfo
import ru.alarmtrade.specialist.domain.configuration.ConfigurationAuthor
import ru.alarmtrade.specialist.domain.configuration.ConfigurationFile
import ru.alarmtrade.specialist.domain.configuration.ConfigurationImage
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationInfoListRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.configurator.data.datasource.CloudConfigurationsDataSource
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationCar

class SpecialistCloudConfigurationsRepositoryTest {
    private var list: SpecialistResponse<List<CarConfigurationInfo>>? = SpecialistResponse.Success(listOf(info()))
    private var card: SpecialistResponse<CarConfiguration>? = SpecialistResponse.Success(configuration())
    private val requests = mutableListOf<Any>()
    private val repository = SpecialistCloudConfigurationsRepository(
        object : CloudConfigurationsDataSource {
            override suspend fun configurationsByCar(
                request: ConfigurationInfoListRequest,
            ): SpecialistResponse<List<CarConfigurationInfo>>? = list.also { requests += request }

            override suspend fun configuration(request: ConfigurationRequest): SpecialistResponse<CarConfiguration>? =
                card.also { requests += request }
        },
    )

    @Test
    fun `list by car sends the car and the model and maps summaries`() = runTest {
        val summary = assertIs<Response.Success<List<CloudConfigurationSummary>>>(repository.byCar(7, "DX-9X"))
            .value.single()

        assertEquals(listOf<Any>(ConfigurationInfoListRequest(carId = 7, alarmDeviceName = "DX-9X")), requests)
        assertEquals(ConfigurationCar(id = 7, brand = "Toyota", model = "Camry", trim = "2.5 AT"), summary.car)
        assertEquals("DX-9X", summary.deviceTitle)
        assertEquals("ivan@mail.ru", summary.author)
        assertNull(summary.rating)
        assertTrue(summary.onlyForAuthor)
    }

    @Test
    fun `card keeps only attachments with links`() = runTest {
        val result = assertIs<Response.Success<CloudConfiguration>>(repository.card(5)).value

        assertEquals(listOf<Any>(ConfigurationRequest(5)), requests)
        assertEquals("<p>Полное</p>", result.fullDescriptionHtml)
        assertEquals(ConfigurationAttachment("Карта", null, "https://files/map.pdf"), result.mountingManual)
        assertEquals(listOf(ConfigurationAttachment("Схема", null, "https://img/1.png")), result.attachments)
    }

    @Test
    fun `network failure becomes a common no-connection error`() = runTest {
        list = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.byCar(7, ""))

        assertEquals(ApiError.NoConnection, result.exception.appError)
    }

    @Test
    fun `unavailable interactor is an error`() = runTest {
        card = null

        assertIs<IllegalStateException>(assertIs<Response.Error>(repository.card(5)).exception)
    }
}

private val date = LocalDateTime(year = 2026, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0)

private fun info() = CarConfigurationInfo(
    id = 5,
    name = "Охрана",
    shortDescription = "Кратко",
    fullDescription = "<p>Полное</p>",
    alarmDeviceId = 1,
    alarmDeviceTitle = "",
    alarmDeviceName = "DX-9X",
    language = AppLanguage.RU,
    author = ConfigurationAuthor(id = 1, login = "ivan@mail.ru", fullName = " "),
    isAvailable = false,
    rating = 0,
    settings = byteArrayOf(),
    settingsFileName = "",
    creationDate = date,
    lastUpdateDate = date,
    mountingManualFile = ConfigurationFile(name = "Карта", url = "https://files/map.pdf"),
    car = CarFullInformation(id = 7, name = "2.5 AT", brand = "Toyota", model = "Camry"),
)

private fun configuration() = CarConfiguration(
    info = info(),
    questions = emptyList(),
    images = listOf(
        ConfigurationImage(1, "Схема", "", previewUrl = "", url = "https://img/1.png"),
        ConfigurationImage(2, "Пустая", "", previewUrl = "https://img/p.png", url = ""),
    ),
)
