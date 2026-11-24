package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FrmFileInfo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareException
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession

class SerialFirmwareViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val bootDevice = AlarmDevice(
        systemId = 67305985,
        systemName = "grc-key",
        firmwareVersion = "VER1.00",
        mode = DeviceOperationMode.BOOT,
        features = null,
        can = null,
    )

    private class FakeSerialRepository(
        var fileInfo: Response<FrmFileInfo> = Response.Success(FrmFileInfo("grc-key", "VER1.01")),
    ) : SerialFirmwareRepository {
        val progress = Channel<Response<SerialFirmwareProgress>>(Channel.UNLIMITED)
        var updatedPath: String? = null
        var updates = 0

        /** Скачанные версии по ссылке: путь временного файла; нет ссылки — ошибка сети. */
        val downloads = mutableMapOf<String, Response<String>>()

        /** Задан — скачивание ждёт его (для отмены «Назад»). */
        var downloadGate: CompletableDeferred<Unit>? = null

        val readPaths = mutableListOf<String>()
        val downloadedUrls = mutableListOf<String>()

        override suspend fun readFileInfo(path: String): Response<FrmFileInfo> = fileInfo.also { readPaths += path }

        override suspend fun download(url: String): Response<String> {
            downloadedUrls += url
            downloadGate?.await()
            return downloads[url] ?: Response.Error(IllegalStateException("no file"))
        }

        override fun update(fileName: String, path: String): Flow<Response<SerialFirmwareProgress>> {
            updatedPath = path
            updates++
            return progress.consumeAsFlow()
        }
    }

    private val server = FakeFirmwareRepository()

    private val serverVersion = FirmwareVersion(
        id = 1,
        version = "1.01",
        createdOn = LocalDate(2026, 11, 1),
        description = "Исправления",
        url = "https://server/grc-key.frm",
    )

    private lateinit var sessionRepository: FakeDeviceSessionRepository
    private lateinit var session: DeviceSession

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) { block() }

    private fun TestScope.viewModel(repository: SerialFirmwareRepository): SerialFirmwareViewModel {
        val dispatchers = testDispatchers(dispatcher)
        sessionRepository = FakeDeviceSessionRepository(bootDevice)
        session =
            testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = ConnectionTransport.SERIAL)
        // Хост держит сессию горячей, пока открыт экран устройства.
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { session.state.collect {} }
        return SerialFirmwareViewModel(repository, server, session, dispatchers).also { vm ->
            // Экран подписан на state, пока открыт.
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        }
    }

    private fun TestScope.pickAndConfirm(viewModel: SerialFirmwareViewModel) {
        viewModel.onFilePicked(FirmwareFilePickResult.Picked("dx.frm", "/tmp/dx.frm"))
        runCurrent()
        viewModel.onConfirmUpdate()
        runCurrent()
    }

    @Test
    fun `device title comes from the session`() = test {
        val viewModel = viewModel(FakeSerialRepository())
        runCurrent()
        assertEquals("grc-key  67305985", viewModel.state.value.deviceTitle)
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `picked file is confirmed and update goes through stages to success`() = test {
        val repository = FakeSerialRepository()
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onFilePicked(FirmwareFilePickResult.Picked("dx.frm", "/tmp/dx.frm"))
        runCurrent()
        assertEquals(
            SerialPhase.Confirm("dx.frm", "/tmp/dx.frm", fileVersion = "VER1.01", older = false),
            viewModel.state.value.phase,
        )
        assertEquals("VER1.00", viewModel.state.value.deviceVersion)

        viewModel.onConfirmUpdate()
        runCurrent()
        assertEquals("/tmp/dx.frm", repository.updatedPath)
        assertEquals(SerialPhase.WaitingBoot, viewModel.state.value.phase)
        assertTrue(viewModel.state.value.locked)

        repository.progress.send(Response.Success(SerialFirmwareProgress.Sending(40)))
        runCurrent()
        assertEquals(SerialPhase.Transfer(40), viewModel.state.value.phase)

        repository.progress.send(Response.Success(SerialFirmwareProgress.Finished))
        repository.progress.close()
        runCurrent()
        assertEquals(SerialPhase.Success("VER1.01"), viewModel.state.value.phase)

        viewModel.onResultOk()
        runCurrent()
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `after the update the session rereads the board mode without reconnect`() = test {
        val repository = FakeSerialRepository()
        val viewModel = viewModel(repository)
        runCurrent()
        assertTrue(session.state.value is DeviceSessionState.FirmwareBoot)
        assertEquals(true, viewModel.state.value.deviceInBoot)

        pickAndConfirm(viewModel)
        // Блок применил прошивку и перезагрузился в рабочий режим; COM-порт при этом не отключался.
        sessionRepository.device = bootDevice.copy(mode = DeviceOperationMode.MAIN, firmwareVersion = "VER1.01")
        repository.progress.send(Response.Success(SerialFirmwareProgress.Finished))
        repository.progress.close()
        runCurrent()

        val ready = session.state.value as DeviceSessionState.Ready
        assertEquals("VER1.01", ready.device.firmwareVersion)
        // Рабочий режим: версия блока — версия прошивки, её и показывает «Готово».
        assertEquals(false, viewModel.state.value.deviceInBoot)
        assertEquals("VER1.01", viewModel.state.value.deviceVersion)
        assertEquals(SerialPhase.Success("VER1.01"), viewModel.state.value.phase)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `failure is shown and back is locked only during the update`() = test {
        val repository = FakeSerialRepository()
        val viewModel = viewModel(repository)
        runCurrent()
        pickAndConfirm(viewModel)

        repository.progress.send(Response.Error(SerialFirmwareException(SerialFirmwareFailure.BootTimeout)))
        repository.progress.close()
        runCurrent()
        assertEquals(SerialPhase.Error(SerialFirmwareFailure.BootTimeout), viewModel.state.value.phase)
        assertTrue(!viewModel.state.value.locked)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `failure in the middle of the transfer ends the update with that error`() = test {
        val repository = FakeSerialRepository()
        val viewModel = viewModel(repository)
        runCurrent()
        pickAndConfirm(viewModel)

        repository.progress.send(Response.Success(SerialFirmwareProgress.Sending(40)))
        runCurrent()
        assertTrue(viewModel.state.value.locked)
        repository.progress.send(Response.Error(SerialFirmwareException(SerialFirmwareFailure.ConnectionLost)))
        repository.progress.close()
        runCurrent()
        assertEquals(SerialPhase.Error(SerialFirmwareFailure.ConnectionLost), viewModel.state.value.phase)
        assertTrue(!viewModel.state.value.locked)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `double tap on update starts it once`() = test {
        val repository = FakeSerialRepository()
        val viewModel = viewModel(repository)
        runCurrent()
        viewModel.onFilePicked(FirmwareFilePickResult.Picked("dx.frm", "/tmp/dx.frm"))
        runCurrent()

        viewModel.onConfirmUpdate()
        viewModel.onConfirmUpdate()
        runCurrent()

        assertEquals(1, repository.updates)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `firmware for another unit is confirmed with a name warning and can be flashed`() = test {
        val repository = FakeSerialRepository(fileInfo = Response.Success(FrmFileInfo("DX-9X", "VER2.10")))
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onFilePicked(FirmwareFilePickResult.Picked("dx.frm", "/tmp/dx.frm"))
        runCurrent()
        val confirm = viewModel.state.value.phase as SerialPhase.Confirm
        assertEquals(SystemNameMismatch("DX-9X", "grc-key"), confirm.nameMismatch)

        viewModel.onConfirmUpdate()
        runCurrent()
        assertEquals("/tmp/dx.frm", repository.updatedPath)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `not a frm file shows banner, older file version is marked`() = test {
        val repository = FakeSerialRepository(fileInfo = Response.Error(IllegalArgumentException("Not a .frm file")))
        val viewModel = viewModel(repository)
        runCurrent()
        viewModel.onFilePicked(FirmwareFilePickResult.Picked("dx.frm", "/tmp/dx.frm"))
        runCurrent()
        assertEquals(SerialFileProblem.NotFrm, viewModel.state.value.fileProblem)

        repository.fileInfo = Response.Success(FrmFileInfo("GRC-KEY", "VER0.90"))
        viewModel.onFilePicked(FirmwareFilePickResult.Picked("old.frm", "/tmp/old.frm"))
        runCurrent()
        assertEquals(
            SerialPhase.Confirm("old.frm", "/tmp/old.frm", fileVersion = "VER0.90", older = true),
            viewModel.state.value.phase,
        )
        assertEquals(null, viewModel.state.value.fileProblem)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `too large file shows banner and stays on ready`() = test {
        val viewModel = viewModel(FakeSerialRepository())
        runCurrent()
        viewModel.onFilePicked(FirmwareFilePickResult.TooLarge)
        runCurrent()
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        assertEquals(FileError.TooLarge, viewModel.state.value.fileError)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `back is ignored during the update and exits otherwise`() = test {
        val repository = FakeSerialRepository()
        val viewModel = viewModel(repository)
        val effects = mutableListOf<SerialFirmwareEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.collect { effects += it } }
        runCurrent()
        pickAndConfirm(viewModel)

        viewModel.onBack()
        runCurrent()
        assertEquals(emptyList(), effects)

        repository.progress.send(Response.Success(SerialFirmwareProgress.Finished))
        repository.progress.close()
        runCurrent()
        viewModel.onBack()
        runCurrent()
        assertEquals(listOf<SerialFirmwareEffect>(SerialFirmwareEffect.Exit), effects)
        viewModel.viewModelScope.cancel()
    }

    private fun withServerVersion(repository: FakeSerialRepository) {
        server.serialModule = Response.Success(MODULE_ID)
        server.latest[MODULE_ID to FirmwareChannel.RELEASE] = Response.Success(serverVersion)
        repository.downloads[serverVersion.url] = Response.Success("/tmp/specmp-serial-firmware.frm")
    }

    @Test
    fun `latest version from the server is downloaded, checked and confirmed`() = test {
        val repository = FakeSerialRepository()
        withServerVersion(repository)
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()

        assertEquals(listOf("grc-key"), server.serialModuleCalls)
        assertEquals(
            SerialPhase.Confirm(
                fileName = "grc-key.frm",
                path = "/tmp/specmp-serial-firmware.frm",
                fileVersion = "VER1.01",
                older = false,
                description = "Исправления",
                fromServer = true,
            ),
            viewModel.state.value.phase,
        )
        viewModel.onConfirmUpdate()
        runCurrent()
        assertEquals("/tmp/specmp-serial-firmware.frm", repository.updatedPath)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `server version that is not a frm file shows its own problem after download`() = test {
        val repository = FakeSerialRepository(fileInfo = Response.Error(IllegalArgumentException("Not a .frm file")))
        withServerVersion(repository)
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()

        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        assertEquals(SerialFileProblem.ServerNotFrm, viewModel.state.value.fileProblem)
        assertEquals(0, repository.updates)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `unit unknown to the server shows no module, the file still works`() = test {
        val repository = FakeSerialRepository()
        server.serialModule = Response.Success(null)
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        assertEquals(SerialSourceError.NoModule, viewModel.state.value.sourceError)

        viewModel.onArchiveClick()
        runCurrent()
        assertEquals(null, viewModel.state.value.archive)
        assertEquals(SerialSourceError.NoModule, viewModel.state.value.sourceError)

        viewModel.onFilePicked(FirmwareFilePickResult.Picked("dx.frm", "/tmp/dx.frm"))
        runCurrent()
        assertIs<SerialPhase.Confirm>(viewModel.state.value.phase)
        assertEquals(null, viewModel.state.value.sourceError)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `network error is shown and the next tap retries`() = test {
        val repository = FakeSerialRepository()
        server.serialModule = Response.Error(IllegalStateException("offline"))
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        assertIs<SerialSourceError.Server>(viewModel.state.value.sourceError)

        withServerVersion(repository)
        viewModel.onDownloadClick()
        runCurrent()
        assertIs<SerialPhase.Confirm>(viewModel.state.value.phase)
        assertEquals(2, server.serialModuleCalls.size)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `failed download keeps the choice with an error`() = test {
        val repository = FakeSerialRepository()
        withServerVersion(repository)
        repository.downloads.clear()
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        assertIs<SerialSourceError.Server>(viewModel.state.value.sourceError)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `version picked in the archive is downloaded and confirmed`() = test {
        val repository = FakeSerialRepository()
        withServerVersion(repository)
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onArchiveClick()
        runCurrent()
        val request = assertNotNull(viewModel.state.value.archive)
        assertEquals(MODULE_ID, request.moduleId)
        assertEquals("grc-key", request.deviceName)
        assertEquals("VER1.00", request.installedVersion)

        viewModel.onArchivePicked(serverVersion)
        runCurrent()
        assertEquals(null, viewModel.state.value.archive)
        val confirm = assertIs<SerialPhase.Confirm>(viewModel.state.value.phase)
        assertEquals("/tmp/specmp-serial-firmware.frm", confirm.path)
        assertTrue(confirm.fromServer)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `back during the download cancels it and stays on the screen`() = test {
        val repository = FakeSerialRepository()
        withServerVersion(repository)
        repository.downloadGate = CompletableDeferred()
        val viewModel = viewModel(repository)
        val effects = mutableListOf<SerialFirmwareEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.collect { effects += it } }
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()
        assertEquals(SerialPhase.Download, viewModel.state.value.phase)

        viewModel.onBack()
        runCurrent()
        repository.downloadGate?.complete(Unit)
        runCurrent()
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        assertEquals(emptyList(), effects)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `server version that is not frm by its link is rejected without download`() = test {
        val repository = FakeSerialRepository()
        withServerVersion(repository)
        val binary = serverVersion.copy(url = "https://server/files/grc-key.bin?t=1")
        server.latest[MODULE_ID to FirmwareChannel.RELEASE] = Response.Success(binary)
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()
        assertEquals(SerialPhase.Ready, viewModel.state.value.phase)
        assertEquals(SerialFileProblem.ServerNotFrm, viewModel.state.value.fileProblem)

        viewModel.onArchiveClick()
        runCurrent()
        viewModel.onArchivePicked(binary)
        runCurrent()
        assertEquals(SerialFileProblem.ServerNotFrm, viewModel.state.value.fileProblem)
        assertEquals(emptyList(), repository.downloadedUrls)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `upper case FRM extension in the link passes`() = test {
        val repository = FakeSerialRepository()
        withServerVersion(repository)
        val upper = serverVersion.copy(url = "https://server/GRC-KEY.FRM?token=1#top")
        server.latest[MODULE_ID to FirmwareChannel.RELEASE] = Response.Success(upper)
        repository.downloads[upper.url] = Response.Success("/tmp/specmp-serial-firmware.frm")
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onDownloadClick()
        runCurrent()
        val confirm = assertIs<SerialPhase.Confirm>(viewModel.state.value.phase)
        assertEquals("GRC-KEY.FRM", confirm.fileName)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `file without frm extension is rejected before reading, upper case passes`() = test {
        val repository = FakeSerialRepository()
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onFilePicked(FirmwareFilePickResult.Picked("dx.bin", "/tmp/dx.bin"))
        runCurrent()
        assertEquals(SerialFileProblem.NotFrm, viewModel.state.value.fileProblem)
        assertEquals(emptyList(), repository.readPaths)

        viewModel.onFilePicked(FirmwareFilePickResult.Picked("DX.FRM", "/tmp/DX.FRM"))
        runCurrent()
        assertIs<SerialPhase.Confirm>(viewModel.state.value.phase)
        viewModel.viewModelScope.cancel()
    }

    private companion object {
        const val MODULE_ID = 7L
    }
}
