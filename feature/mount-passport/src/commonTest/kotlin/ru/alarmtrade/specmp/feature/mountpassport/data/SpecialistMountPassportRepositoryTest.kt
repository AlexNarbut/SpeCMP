package ru.alarmtrade.specmp.feature.mountpassport.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.MountPassportPhotoLimitException
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStatus
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStorageUsage
import ru.alarmtrade.specialist.domain.mountPassport.PassportWorksSource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistMountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportClient
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportConfiguration
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportEdit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhotoLimitException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageFullException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportWorks

class SpecialistMountPassportRepositoryTest {

    private val source = FakeMountPassportDataSource()
    private val repository = SpecialistMountPassportRepository(source)

    @Test
    fun passportIsMappedWithAbsolutePhotoPaths() = runTest {
        val passport = assertIs<Response.Success<MountPassport>>(repository.get("p1")).value

        assertEquals("20261119-234567-1", passport.number)
        assertEquals(PassportStatus.DRAFT, passport.status)
        assertEquals(Instant.fromEpochMilliseconds(CREATED_MS), passport.createdAt)
        assertEquals("Kia", passport.vehicle.brand)
        assertEquals("1234567", passport.unit.systemId)
        assertTrue(passport.works.fromMaster)
        assertEquals(85, passport.works.durationMinutes)
        val photo = passport.photos.single()
        assertEquals("/data/passports/p1/photos/ph1.jpg", photo.path)
        assertEquals("/data/passports/p1/thumbs/ph1.jpg", photo.thumbnailPath)
        assertTrue(requireNotNull(passport.settingsSnapshot).canFactory)
        assertEquals(PassportReadStatus.NOT_READ, passport.readStages[PassportReadStage.PERIPHERALS])
        assertEquals(false, passport.withoutUnit)
    }

    @Test
    fun sdkErrorIsCommonError() = runTest {
        source.passport = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val error = assertIs<Response.Error>(repository.get("p1"))

        assertEquals(ApiError.NoConnection, error.exception.appError)
    }

    @Test
    fun listItemsAreMapped() = runTest {
        source.all.emit(SpecialistResponse.Success(listOf(sdkItem(status = MountPassportStatus.BLANK))))

        val items = assertIs<Response.Success<List<MountPassportSummary>>>(repository.observeAll().first()).value

        val item = items.single()
        assertEquals(PassportStatus.BLANK, item.status)
        assertEquals("Kia Sportage", item.carTitle)
        assertEquals(Instant.fromEpochMilliseconds(CREATED_MS), item.createdAt)
        assertTrue(item.hasSettings)
    }

    @Test
    fun unitListAsksSdkBySystemId() = runTest {
        source.unit.emit(SpecialistResponse.Success(emptyList()))

        repository.observeUnit("107131").first()

        assertEquals("107131", source.unitRequest)
    }

    @Test
    fun searchSendsQueryAsIsNormalizationIsInSdk() = runTest {
        source.searchResponse = SpecialistResponse.Success(listOf(sdkItem()))

        val found = assertIs<Response.Success<List<MountPassportSummary>>>(repository.search("а 123")).value

        assertEquals("а 123", source.searchQuery)
        assertEquals("p1", found.single().id)
    }

    @Test
    fun storageUsageIsMapped() = runTest {
        source.storage = SpecialistResponse.Success(MountPassportStorageUsage(passports = 3, bytes = 2048))

        assertEquals(
            PassportStorageUsage(3, 2048),
            assertIs<Response.Success<PassportStorageUsage>>(repository.storageUsage()).value,
        )
    }

    @Test
    fun editSendsManualSectionsAndMarksWorksSource() = runTest {
        val edit = PassportEdit(
            vehicle = PassportVehicle(customTitle = "Toyota RAV4", vin = "JTMW43FV60D123456", mileageKm = 1200),
            configuration = PassportConfiguration(name = "Автозапуск", author = null, cloudId = null),
            works = PassportWorks(fromMaster = false, clone = true),
            remarks = "Сирена под капотом",
            client = PassportClient(name = "Сергей", phone = "+79162103308"),
            installer = PassportInstaller(organization = "Автозвук", address = "Москва"),
        )

        assertIs<Response.Success<MountPassport>>(repository.edit("p1", edit))

        val request = requireNotNull(source.editRequest)
        assertEquals("p1", request.id)
        assertEquals("Toyota RAV4", request.edit.vehicle.customTitle)
        assertEquals(1200L, request.edit.vehicle.mileageKm)
        assertEquals("Автозвук", request.edit.installer.organization)
        assertEquals(PassportWorksSource.MANUAL, request.edit.works.source)
        assertTrue(request.edit.works.clone)
        assertEquals("Автозапуск", request.edit.configuration?.name)
    }

    @Test
    fun deleteGoesToSdk() = runTest {
        assertIs<Response.Success<Unit>>(repository.delete("p9"))

        assertEquals("p9", source.deleted)
    }

    @Test
    fun photoIsAddedWithoutCaption() = runTest {
        val jpeg = byteArrayOf(1, 2)
        val thumb = byteArrayOf(3)

        assertIs<Response.Success<MountPassport>>(repository.addPhoto("p1", jpeg, thumb))

        val request = requireNotNull(source.photoRequest)
        assertTrue(request.jpeg.contentEquals(jpeg))
        assertTrue(request.thumbnail.contentEquals(thumb))
        assertNull(request.caption)
    }

    @Test
    fun photoLimitBecomesDomainError() = runTest {
        source.photoResponse = SpecialistResponse.Error.General(MountPassportPhotoLimitException(8))

        val error = assertIs<Response.Error>(repository.addPhoto("p1", byteArrayOf(1), byteArrayOf(1)))

        assertEquals(8, assertIs<PassportPhotoLimitException>(error.exception).limit)
    }

    @Test
    fun noSpaceBecomesStorageFull() = runTest {
        source.photoResponse = SpecialistResponse.Error.General(
            IllegalStateException("write failed", Exception("ENOSPC (No space left on device)")),
        )

        val error = assertIs<Response.Error>(repository.addPhoto("p1", byteArrayOf(1), byteArrayOf(1)))

        assertIs<PassportStorageFullException>(error.exception)
    }

    @Test
    fun otherPhotoErrorStaysAsIs() = runTest {
        source.photoResponse = SpecialistResponse.Error.General(IllegalStateException("broken"))

        val error = assertIs<Response.Error>(repository.addPhoto("p1", byteArrayOf(1), byteArrayOf(1)))

        assertIs<IllegalStateException>(error.exception)
    }

    @Test
    fun deletePhotoAndCaptionGoToSdk() = runTest {
        assertIs<Response.Success<MountPassport>>(repository.deletePhoto("p1", "ph1"))
        assertIs<Response.Success<MountPassport>>(repository.setPhotoCaption("p1", "ph1", "Реле"))

        assertEquals("ph1", source.deletedPhoto?.photoId)
        assertEquals("Реле", source.caption?.caption)
    }

    @Test
    fun settingsFileIsAbsolutePath() = runTest {
        assertEquals(
            "/data/passports/p1/settings.dxs",
            assertIs<Response.Success<String>>(repository.settingsFile("p1")).value,
        )
    }
}
