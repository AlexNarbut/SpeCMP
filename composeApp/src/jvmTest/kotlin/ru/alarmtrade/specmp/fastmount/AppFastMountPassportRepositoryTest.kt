package ru.alarmtrade.specmp.fastmount

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountBypass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountUnit
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountWorks
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportEdit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportFastMountResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository

class AppFastMountPassportRepositoryTest {

    internal class Passports : MountPassportRepository {
        val all = MutableStateFlow<Response<List<MountPassportSummary>>>(Response.Success(emptyList()))
        val blanks = mutableListOf<PassportFastMountResult>()
        var lastToday: Response<MountPassportSummary?> = Response.Success(null)

        override fun observeAll(): Flow<Response<List<MountPassportSummary>>> = all
        override fun observeUnit(systemId: String): Flow<Response<List<MountPassportSummary>>> = all
        override suspend fun search(query: String): Response<List<MountPassportSummary>> = error("not used")
        override suspend fun storageUsage(): Response<PassportStorageUsage> = error("not used")
        override suspend fun createBlank(result: PassportFastMountResult): Response<String> {
            blanks += result
            return Response.Success("blank-1")
        }

        override suspend fun lastOfUnitToday(systemId: String): Response<MountPassportSummary?> = lastToday
        override suspend fun get(id: String): Response<MountPassport> = error("not used")
        override suspend fun edit(id: String, edit: PassportEdit): Response<MountPassport> = error("not used")
        override suspend fun delete(id: String): Response<Unit> = error("not used")
        override suspend fun addPhoto(id: String, jpeg: ByteArray, thumbnail: ByteArray): Response<MountPassport> =
            error("not used")

        override suspend fun deletePhoto(id: String, photoId: String): Response<MountPassport> = error("not used")
        override suspend fun setPhotoCaption(id: String, photoId: String, caption: String?): Response<MountPassport> =
            error("not used")

        override suspend fun settingsFile(id: String): Response<String> = error("not used")
    }

    private val passports = Passports()
    private val repository = AppFastMountPassportRepository(passports)

    @Test
    fun observedPassportIsFoundByIdAndIssuedByStatus() = runTest {
        passports.all.value = Response.Success(
            listOf(summary("p0", PassportStatus.ISSUED), summary("p1", PassportStatus.DRAFT)),
        )
        assertEquals(
            FastMountPassport("p1", "N-p1", issued = false),
            assertIs<Response.Success<FastMountPassport?>>(repository.observe("p1").first()).value,
        )

        passports.all.value = Response.Success(listOf(summary("p1", PassportStatus.ISSUED)))
        assertEquals(
            true,
            assertIs<Response.Success<FastMountPassport?>>(repository.observe("p1").first()).value?.issued,
        )

        passports.all.value = Response.Success(emptyList())
        assertEquals(null, assertIs<Response.Success<FastMountPassport?>>(repository.observe("p1").first()).value)
    }

    @Test
    fun blankIsCreatedFromWizardResult() = runTest {
        val result = FastMountResult(
            unit = FastMountUnit("DX-9X", 77, "3.57", null, null, false),
            car = null,
            configuration = null,
            works = FastMountWorks(true, FastMountBypass.NONE, clone = false, slave = false, durationMinutes = 12),
            systemCheck = emptyList(),
            transport = ConnectionTransport.USB,
        )

        assertEquals("blank-1", assertIs<Response.Success<String>>(repository.createBlank(result)).value)
        assertEquals("77", passports.blanks.single().unit?.systemId)
    }

    internal companion object {
        fun summary(id: String, status: PassportStatus, systemId: String = "77") = MountPassportSummary(
            id = id,
            number = "N-$id",
            status = status,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
            systemId = systemId,
            unitModel = "DX-9X",
            carTitle = null,
            plate = null,
            vin = null,
            clientName = null,
            clientPhone = null,
            withoutUnit = false,
            hasSettings = false,
        )
    }
}
