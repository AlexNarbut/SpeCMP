package ru.alarmtrade.specmp.feature.mountpassport.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCan
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckTask
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportClient
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportConfiguration
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportSettingsSnapshot
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportSection
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.SettingsSnapshotStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.VinError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.WorksForm
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.mileageInvalid
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.phoneInvalid
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.sections
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.settingsStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.toEdit
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.toForm
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.vinError

class PassportEditorTransitionsTest {

    @Test
    fun vinIsSeventeenCharsWithoutIOQ() {
        assertNull(vinError(""))
        assertNull(vinError("XWEPH81ADN0012345"))
        assertNull(vinError("xweph81adn0012345"))
        assertEquals(VinError.LENGTH, vinError("XWEPH81ADN001234"))
        assertEquals(VinError.INVALID_CHARS, vinError("XWEPH81ADN001234O"))
        assertEquals(VinError.INVALID_CHARS, vinError("XWEPH81ADN00123Ж"))
    }

    @Test
    fun mileageIsDigitsOnly() {
        assertFalse(mileageInvalid(""))
        assertFalse(mileageInvalid("120 500"))
        assertTrue(mileageInvalid("12к"))
    }

    @Test
    fun phoneIsValidatedByPhoneNumbers() {
        assertFalse(phoneInvalid(""))
        assertFalse(phoneInvalid("79035551240"))
        assertTrue(phoneInvalid("7903"))
    }

    @Test
    fun formIsFilledFromPassport() {
        val passport = testPassport().copy(
            vehicle = PassportVehicle(customTitle = "Toyota RAV4", vin = "JTMW43FV60D123456", mileageKm = 120500),
            client = PassportClient(name = "Сергей", phone = "+7 916 210-33-08"),
            configuration = PassportConfiguration(name = "Автозапуск", author = null, cloudId = null),
            remarks = "Сирена под капотом",
        )

        val form = passport.toForm()

        assertEquals("Toyota RAV4", form.carTitle)
        assertEquals("120500", form.mileage)
        assertEquals("79162103308", form.clientPhone)
        assertEquals("Автозапуск", form.configurationName)
        assertEquals("Автозвук", form.organization)
        assertEquals("Сирена под капотом", form.remarks)
        assertTrue(form.works.settingsWritten)
    }

    @Test
    fun editTakesValidFieldsAndKeepsPreviousForInvalid() {
        val passport = testPassport().copy(
            vehicle = PassportVehicle(brand = "Kia", model = "Rio", vin = "XWEPH81ADN0012345", mileageKm = 10),
            client = PassportClient(phone = "+79035551240"),
        )
        val form = passport.toForm().copy(
            plate = "  В 310 НЕ 77 ",
            vin = "BAD",
            mileage = "12 000",
            clientName = " ",
            clientPhone = "7903",
            organization = "Автозвук-2",
            remarks = "",
        )

        val edit = form.toEdit(passport)

        assertEquals("Kia", edit.vehicle.brand)
        assertEquals("В 310 НЕ 77", edit.vehicle.plate)
        assertEquals("XWEPH81ADN0012345", edit.vehicle.vin)
        assertEquals(12_000L, edit.vehicle.mileageKm)
        assertNull(edit.client.name)
        assertEquals("+79035551240", edit.client.phone)
        assertEquals("Автозвук-2", edit.installer.organization)
        assertEquals("Алексей Смирнов", edit.installer.name)
        assertNull(edit.remarks)
    }

    @Test
    fun editWritesPhoneInternationalAndVinUppercase() {
        val passport = testPassport()
        val edit = passport.toForm().copy(clientPhone = "79162103308", vin = "jtmw43fv60d123456").toEdit(passport)

        assertEquals("+79162103308", edit.client.phone)
        assertEquals("JTMW43FV60D123456", edit.vehicle.vin)
    }

    @Test
    fun worksFromFormOnlyWhenEditable() {
        val manual = testPassport(origin = PassportOrigin.ADVANCED_MOUNT)
        val master = testPassport(origin = PassportOrigin.FAST_MOUNT)
        val form = WorksForm(settingsWritten = false, bypass = true, clone = true, slave = false)

        val manualWorks = manual.toForm().copy(works = form).toEdit(manual).works
        val masterWorks = master.toForm().copy(works = form).toEdit(master).works

        assertTrue(manualWorks.clone)
        assertFalse(manualWorks.settingsWritten)
        assertEquals(master.works, masterWorks)
    }

    @Test
    fun configurationNameIsManualOnlyWithoutMaster() {
        val master = testPassport(origin = PassportOrigin.FAST_MOUNT)
            .copy(configuration = PassportConfiguration("Из каталога", "Pandora", "42"))
        val manual = testPassport(origin = PassportOrigin.ADVANCED_MOUNT)

        assertEquals("Из каталога", master.toForm().copy(configurationName = "x").toEdit(master).configuration?.name)
        assertEquals("Своя", manual.toForm().copy(configurationName = "Своя").toEdit(manual).configuration?.name)
        assertNull(manual.toForm().copy(configurationName = " ").toEdit(manual).configuration)
    }

    @Test
    fun sectionsFollowDocumentOrder() {
        val withCheck = testPassport().copy(
            systemCheck = listOf(PassportCheckTask("Охрана", PassportCheckResult.PASSED)),
        )

        assertEquals(
            listOf(
                PassportSection.VEHICLE,
                PassportSection.EQUIPMENT,
                PassportSection.CONFIGURATION,
                PassportSection.WORKS,
                PassportSection.CHECK,
                PassportSection.DEVICES,
                PassportSection.PHOTOS,
                PassportSection.REMARKS,
                PassportSection.CLIENT,
                PassportSection.INSTALLER,
            ),
            withCheck.sections(),
        )
        assertFalse(PassportSection.CHECK in testPassport().sections())
        assertFalse(PassportSection.DEVICES in testPassport(manual = true).sections())
    }

    @Test
    fun settingsStatusCoversSnapshotAndStages() {
        val snapshot = PassportSettingsSnapshot("Pandora DX-91", CREATED, canFactory = false)
        val base = testPassport()

        assertEquals(SettingsSnapshotStatus.SAVED, base.copy(settingsSnapshot = snapshot).settingsStatus())
        assertEquals(
            SettingsSnapshotStatus.NOT_READ,
            base.copy(readStages = mapOf(PassportReadStage.SETTINGS to PassportReadStatus.NOT_READ)).settingsStatus(),
        )
        val factory = base.copy(
            unit = base.unit.copy(can = PassportCan("12.05", "1", factory = true)),
            readStages = mapOf(PassportReadStage.SETTINGS to PassportReadStatus.UNAVAILABLE),
        )
        assertEquals(SettingsSnapshotStatus.FACTORY_CAN, factory.settingsStatus())
        assertEquals(
            SettingsSnapshotStatus.UNAVAILABLE,
            base.copy(
                readStages = mapOf(PassportReadStage.SETTINGS to PassportReadStatus.UNAVAILABLE),
            ).settingsStatus(),
        )
        assertEquals(
            SettingsSnapshotStatus.NOT_READ,
            base.copy(readStages = mapOf(PassportReadStage.SETTINGS to PassportReadStatus.SKIPPED)).settingsStatus(),
        )
        assertEquals(SettingsSnapshotStatus.NO_UNIT, testPassport(manual = true).settingsStatus())
        assertEquals(SettingsSnapshotStatus.MISSING, base.settingsStatus())
    }
}
