package ru.alarmtrade.specmp.feature.mountpassport.data

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.MountPassportArchiveException
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportDecision
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportFailure
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportInspection
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportItem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportProblem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportResult
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistPassportTransferRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportExportOptions
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportDecision
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportFailure
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportInspection
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportItem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportProblem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportNotArchiveException

class SpecialistPassportTransferRepositoryTest {

    private val source = FakePassportTransferDataSource()
    private val repository = SpecialistPassportTransferRepository(source)

    @Test
    fun exportPassesIdsOptionsAndBackupFlag() = runTest {
        val archive = repository.export(
            ids = listOf("p1", "p2"),
            options = PassportExportOptions(includeSettings = false, includePhotos = true),
            fullBackup = true,
            pdf = { byteArrayOf(1) },
        )

        val request = requireNotNull(source.exportRequest)
        assertEquals(listOf("p1", "p2"), request.ids)
        assertEquals(false, request.options.includeSettings)
        assertEquals(true, request.options.includePhotos)
        assertEquals(true, request.fullBackup)
        assertEquals(Response.Success(PassportArchive("/tmp/export/a.zip", "a.zip", 2048, 1)), archive)
    }

    @Test
    fun exportPdfIsBuiltByPassportId() = runTest {
        val asked = mutableListOf<String>()
        repository.export(listOf("p1"), PassportExportOptions(), fullBackup = false) { id ->
            asked += id
            byteArrayOf(7)
        }

        val pdf = requireNotNull(source.exportRequest).pdf.pdf(sdkPassport())

        assertEquals(listOf("p1"), asked)
        assertContentEquals(byteArrayOf(7), pdf)
    }

    @Test
    fun exportErrorIsReturned() = runTest {
        source.exportResponse = SpecialistResponse.Error.General(IllegalStateException("disk"))

        assertIs<Response.Error>(repository.export(listOf("p1"), PassportExportOptions(), false) { byteArrayOf() })
    }

    @Test
    fun inspectionItemsAreMapped() = runTest {
        source.inspection = SpecialistResponse.Success(
            MountPassportImportInspection.Ready(
                passports = listOf(sdkImportItem(existingId = "local-1"), sdkImportItem(key = "b", existingId = null)),
                problems = listOf(
                    MountPassportImportProblem("c", "N-3", MountPassportImportFailure.UNSUPPORTED_SCHEMA),
                ),
            ),
        )

        val inspection = repository.inspect("/tmp/import/a.zip")

        assertEquals("/tmp/import/a.zip", source.inspectRequest?.path)
        val ready = assertIs<PassportImportInspection.Ready>(assertIs<Response.Success<*>>(inspection).value)
        assertEquals(
            PassportImportItem(
                key = "a",
                number = "20261105-771204-1",
                carTitle = "Nissan X-Trail",
                plate = "С112АВ199",
                createdAt = Instant.fromEpochMilliseconds(CREATED_MS),
                systemId = "7712040",
                unitModel = "Pandora DX-91",
                hasSettings = true,
                photos = 3,
                exists = true,
            ),
            ready.items.first(),
        )
        assertEquals(false, ready.items[1].exists)
        assertEquals(
            listOf(PassportImportProblem("c", "N-3", PassportImportFailure.UNSUPPORTED_SCHEMA)),
            ready.problems,
        )
    }

    @Test
    fun notArchiveAndNothingToImportAreMapped() = runTest {
        assertEquals(Response.Success(PassportImportInspection.NotArchive), repository.inspect("/a.zip"))

        source.inspection = SpecialistResponse.Success(
            MountPassportImportInspection.NothingToImport(
                listOf(MountPassportImportProblem("", null, MountPassportImportFailure.UNREADABLE)),
            ),
        )

        assertEquals(
            Response.Success(
                PassportImportInspection.NothingToImport(
                    listOf(PassportImportProblem("", null, PassportImportFailure.UNREADABLE)),
                ),
            ),
            repository.inspect("/a.zip"),
        )
    }

    @Test
    fun importPassesDecisionsAndThumbnailer() = runTest {
        repository.import(
            path = "/a.zip",
            decisions = mapOf("a" to PassportImportDecision.REPLACE, "b" to PassportImportDecision.SKIP),
            thumbnailer = { jpeg -> jpeg.copyOf(1) },
        )

        val request = requireNotNull(source.importRequest)
        assertEquals("/a.zip", request.path)
        assertEquals(
            mapOf("a" to MountPassportImportDecision.REPLACE, "b" to MountPassportImportDecision.SKIP),
            request.decisions,
        )
        assertContentEquals(byteArrayOf(1), requireNotNull(request.thumbnailer).thumbnail(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun importResultIsCounted() = runTest {
        source.importResponse = SpecialistResponse.Success(
            MountPassportImportResult(
                imported = listOf("n1", "n2"),
                replaced = listOf("r1"),
                skipped = 2,
                problems = listOf(MountPassportImportProblem("x", "N-9", MountPassportImportFailure.WRITE_FAILED)),
            ),
        )

        val result = repository.import("/a.zip", emptyMap()) { it }

        assertEquals(
            Response.Success(
                PassportImportResult(
                    imported = 2,
                    replaced = 1,
                    skipped = 2,
                    problems = listOf(PassportImportProblem("x", "N-9", PassportImportFailure.WRITE_FAILED)),
                ),
            ),
            result,
        )
    }

    @Test
    fun notArchiveOnImportBecomesDomainError() = runTest {
        source.importResponse = SpecialistResponse.Error.General(MountPassportArchiveException("/a.zip"))

        val error = assertIs<Response.Error>(repository.import("/a.zip", emptyMap()) { it })

        assertIs<PassportNotArchiveException>(error.exception)
    }

    @Test
    fun backupReminderIsDelegated() = runTest {
        assertEquals(Response.Success(true), repository.shouldRemindBackup())

        repository.snoozeBackupReminder()

        assertEquals(1, source.snoozes)
    }

    private fun sdkImportItem(key: String = "a", existingId: String?) = MountPassportImportItem(
        key = key,
        id = "remote-$key",
        number = "20261105-771204-1",
        carTitle = "Nissan X-Trail",
        plate = "С112АВ199",
        createdAtMs = CREATED_MS,
        systemId = "7712040",
        unitModel = "Pandora DX-91",
        hasSettings = true,
        photos = 3,
        existingId = existingId,
    )
}
