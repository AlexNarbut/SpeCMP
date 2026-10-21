package ru.alarmtrade.specmp.feature.device.firmware.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester

class FirmwareBootRequesterTest {

    private val requester = FirmwareBootRequester()

    @Test
    fun `request is taken once`() {
        requester.request(FirmwareBootRequest(moduleId = 7, channel = FirmwareChannel.BETA))

        assertEquals(FirmwareBootRequest(7, FirmwareChannel.BETA), requester.take())
        assertNull(requester.take())
    }

    @Test
    fun `the last request wins`() {
        requester.request(FirmwareBootRequest(1, FirmwareChannel.RELEASE))
        requester.request(FirmwareBootRequest(2, FirmwareChannel.ALPHA))

        assertEquals(FirmwareBootRequest(2, FirmwareChannel.ALPHA), requester.take())
    }

    @Test
    fun `release drops a pending request on logout`() = runTest {
        requester.request(FirmwareBootRequest(1, FirmwareChannel.RELEASE))
        requester.release()

        assertNull(requester.take())
    }
}
