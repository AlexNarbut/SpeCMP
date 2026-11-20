package ru.alarmtrade.specmp.feature.mountpassport.presentation

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDeviceKind
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentText
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.ResourcePassportDocumentLocalizer

class ResourcePassportDocumentLocalizerTest {

    private val localizer = ResourcePassportDocumentLocalizer(TimeZoneSource.fixed(TimeZone.of("Europe/Moscow")))

    private fun <T> inLocale(tag: String, block: suspend () -> T): T {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag(tag))
        return try {
            runBlocking { block() }
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun everyRussianTextIsFilled() = inLocale("ru") {
        val strings = localizer.strings()

        PassportDocumentText.entries.forEach { key -> assertTrue(strings.text(key).isNotBlank(), key.name) }
        assertEquals("Паспорт монтажа", strings.text(PassportDocumentText.TITLE))
        assertEquals(
            "Данные блока внесены вручную, блок не считывался",
            strings.text(PassportDocumentText.MANUAL_UNIT_NOTE),
        )
        assertEquals("Работу выполнил", strings.text(PassportDocumentText.SIGNED_BY))
        assertEquals("Не пройдено", strings.text(PassportDocumentText.CHECK_FAILED))
    }

    @Test
    fun everyEnglishTextIsFilled() = inLocale("en") {
        val strings = localizer.strings()

        PassportDocumentText.entries.forEach { key -> assertTrue(strings.text(key).isNotBlank(), key.name) }
        assertEquals("Installation certificate", strings.text(PassportDocumentText.TITLE))
        assertEquals(
            "Device data entered manually; the device was not read",
            strings.text(PassportDocumentText.MANUAL_UNIT_NOTE),
        )
        assertEquals("Work accepted by", strings.text(PassportDocumentText.ACCEPTED_BY))
        assertEquals("Customer", strings.text(PassportDocumentText.CLIENT))
    }

    @Test
    fun templatesKeepPlaceholders() {
        listOf("ru", "en").forEach { tag ->
            inLocale(tag) {
                val strings = localizer.strings()
                fun has(key: PassportDocumentText, vararg names: String) =
                    names.forEach { assertTrue("{$it}" in strings.text(key), "$tag ${key.name} {$it}") }
                has(PassportDocumentText.CHECK_PASSED_OF_TOTAL, "passed", "total")
                has(PassportDocumentText.MILEAGE_VALUE, "value")
                has(PassportDocumentText.DURATION_HOURS_MINUTES, "h", "m")
                has(PassportDocumentText.DURATION_HOURS, "h")
                has(PassportDocumentText.DURATION_MINUTES, "m")
                has(PassportDocumentText.DEVICE_NUMBER_VALUE, "n")
                has(PassportDocumentText.UNIT_CAN_CODE, "code")
            }
        }
        inLocale("ru") {
            assertEquals(
                "Пройдено {passed} из {total}",
                localizer.strings().text(PassportDocumentText.CHECK_PASSED_OF_TOTAL),
            )
        }
        inLocale("en") {
            assertEquals(
                "Passed {passed} of {total}",
                localizer.strings().text(PassportDocumentText.CHECK_PASSED_OF_TOTAL),
            )
        }
    }

    @Test
    fun russianFormat() = inLocale("ru") {
        val format = localizer.format()

        assertEquals("ru", format.language)
        assertEquals(12, format.months.size)
        assertEquals("ноября", format.months[10])
        assertEquals("{d} {MMMM} {yyyy}, {HH}:{mm}", format.datePattern)
        assertEquals(" ", format.thousandsSeparator)
        assertEquals("Europe/Moscow", format.timeZoneId)
    }

    @Test
    fun englishFormat() = inLocale("en") {
        val format = localizer.format()

        assertEquals("en", format.language)
        assertEquals("November", format.months[10])
        assertEquals("{MMMM} {d}, {yyyy}, {HH}:{mm}", format.datePattern)
        assertEquals(",", format.thousandsSeparator)
    }

    @Test
    fun deviceTypesAreNamed() {
        inLocale("ru") {
            val types = localizer.strings().deviceTypes
            assertEquals(PassportDeviceKind.entries.toSet(), types.keys)
            assertEquals("Метка", types[PassportDeviceKind.TAG])
            assertEquals("Брелок", types[PassportDeviceKind.KEYFOB])
            assertEquals("Замок капота", types[PassportDeviceKind.HOOD_LOCK])
        }
        inLocale("en") {
            assertEquals("Tag", localizer.strings().deviceTypes[PassportDeviceKind.TAG])
        }
    }

    @Test
    fun footerAndFileName() {
        inLocale("ru") {
            val footer = localizer.footer("20261119-234567-1")
            assertEquals("Паспорт монтажа № 20261119-234567-1", footer.start)
            assertEquals("стр. {page} из {pages}", footer.end)
            assertEquals(
                "Паспорт_20261119-234567-1_Kia_Sportage.pdf",
                localizer.fileName("20261119-234567-1", "Kia_Sportage"),
            )
        }
        inLocale("en") {
            val footer = localizer.footer("20261119-234567-1")
            assertEquals("Installation certificate No. 20261119-234567-1", footer.start)
            assertEquals("page {page} of {pages}", footer.end)
            assertEquals(
                "Certificate_20261119-234567-1_1234567.pdf",
                localizer.fileName("20261119-234567-1", "1234567"),
            )
        }
    }
}
