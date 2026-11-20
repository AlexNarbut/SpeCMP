package ru.alarmtrade.specmp.feature.mountpassport.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportDocumentPhotos
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportDocumentStrings
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStatus
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistPassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDeviceKind
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentFormat
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentStrings
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentText
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus

class SpecialistPassportDocumentRepositoryTest {

    private val source = FakePassportDocumentDataSource()
    private val repository = SpecialistPassportDocumentRepository(source)
    private val format = PassportDocumentFormat(
        language = "ru",
        months = List(12) { "m${it + 1}" },
        datePattern = "{d} {MMMM} {yyyy}",
        thousandsSeparator = " ",
        timeZoneId = "Europe/Moscow",
    )

    @Test
    fun everyTextGoesToItsSdkField() = runTest {
        val strings = PassportDocumentStrings(
            texts = PassportDocumentText.entries.associateWith { it.name },
            deviceTypes = mapOf(PassportDeviceKind.TAG to "Метка", PassportDeviceKind.NAVIGATION to "Навигация"),
        )

        repository.buildHtml("p1", strings, format)

        val sdk = requireNotNull(source.htmlRequest).strings
        val fields = Json.encodeToJsonElement(MountPassportDocumentStrings.serializer(), sdk).jsonObject
        val texts = fields.filterValues { it is JsonPrimitive }
        assertEquals(PassportDocumentText.entries.size, texts.size)
        texts.forEach { (field, value) ->
            assertEquals(field.toScreamingSnake(), (value as JsonPrimitive).content, field)
        }
        assertEquals("Метка", sdk.deviceTypes["TAG_760"])
        assertEquals("Метка", sdk.deviceTypes["TAG_760_V3"])
        assertEquals("Навигация", sdk.deviceTypes["NAV_13"])
        assertEquals(null, sdk.deviceTypes["REMOTE_CONTROL"])
        assertIs<JsonObject>(fields.getValue("deviceTypes"))
    }

    @Test
    fun formatAndEmbeddedPhotosGoToSdk() = runTest {
        val html = repository.buildHtml("p1", PassportDocumentStrings(emptyMap()), format)

        assertEquals("<html/>", assertIs<Response.Success<String>>(html).value)
        val request = requireNotNull(source.htmlRequest)
        assertEquals("p1", request.id)
        assertEquals(MountPassportDocumentPhotos.EMBEDDED, request.photos)
        assertEquals("ru", request.format.language)
        assertEquals("m11", request.format.months[10])
        assertEquals("{d} {MMMM} {yyyy}", request.format.datePattern)
        assertEquals(" ", request.format.thousandsSeparator)
        assertEquals("Europe/Moscow", request.format.timeZoneId)
    }

    @Test
    fun htmlIsTakenEvenWithMissingPhotos() = runTest {
        source.missingPhotoIds = listOf("ph2")

        val html = repository.buildHtml("p1", PassportDocumentStrings(emptyMap()), format)

        assertEquals("<html/>", assertIs<Response.Success<String>>(html).value)
    }

    @Test
    fun issuedStatusIsSet() = runTest {
        val passport = assertIs<Response.Success<MountPassport>>(repository.markIssued("p1")).value

        assertEquals(MountPassportStatus.ISSUED, source.statusRequest?.status)
        assertEquals("p1", source.statusRequest?.id)
        assertEquals(PassportStatus.ISSUED, passport.status)
    }

    private fun String.toScreamingSnake(): String = replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").uppercase()
}
