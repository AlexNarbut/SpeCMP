package ru.alarmtrade.specmp.feature.device.settings.data.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.service.SettingsWrite

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceSettingsWritesImplTest {
    @Test
    fun `subscriber gets the writes recorded after it subscribed`() = runTest(UnconfinedTestDispatcher()) {
        val writes = DeviceSettingsWritesImpl()
        val early = SettingsWrite(SettingsSource.Device, listOf("Early"))
        val late = SettingsWrite(SettingsSource.Device, listOf("Siren", "Turbo timer"))
        val received = mutableListOf<Response<SettingsWrite>>()

        writes.record(early)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { writes.writes().collect { received += it } }
        writes.record(late)

        assertEquals(listOf<Response<SettingsWrite>>(Response.Success(late)), received)
    }
}
