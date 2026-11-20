package ru.alarmtrade.specmp.fastmount

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountBypass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountCheck
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountCheckResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResultConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountUnit
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountWorks
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportBypass
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCan
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckTask
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportConfiguration
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportWorks
import ru.alarmtrade.specmp.navigation.toPassportResult

class FastMountPassportMappingTest {

    private fun result(
        transport: ConnectionTransport? = ConnectionTransport.BLE,
        bypass: FastMountBypass = FastMountBypass.CLONE_NEEDED,
        canCode: String? = "0213",
        canFactory: Boolean = false,
    ) = FastMountResult(
        unit = FastMountUnit("DX-9X", 77, "3.57", canCode = canCode, canVersion = "1.2", canFactory = canFactory),
        car = MountCar(id = 5, brand = "Kia", model = "Rio", years = "2017–2020", canCode = "0213"),
        configuration = FastMountResultConfiguration(id = 42, name = "Rio IV", author = "Автор"),
        works = FastMountWorks(true, bypass, clone = true, slave = true, durationMinutes = 40),
        systemCheck = listOf(
            FastMountCheck("Двери", FastMountCheckResult.PASSED),
            FastMountCheck("Капот", FastMountCheckResult.FAILED),
            FastMountCheck("Багажник", FastMountCheckResult.SKIPPED),
        ),
        transport = transport,
    )

    @Test
    fun wizardResultBecomesPassportResult() {
        val passport = result().toPassportResult()

        assertEquals(
            PassportVehicle(brand = "Kia", model = "Rio", years = "2017–2020", catalogCarId = "5"),
            passport.vehicle,
        )
        assertEquals(PassportConfiguration(name = "Rio IV", author = "Автор", cloudId = "42"), passport.configuration)
        assertEquals(
            PassportWorks(
                fromMaster = true,
                settingsWritten = true,
                bypass = PassportBypass.CLONE_NEEDED,
                clone = true,
                slave = true,
                durationMinutes = 40,
            ),
            passport.works,
        )
        assertEquals(
            listOf(
                PassportCheckTask("Двери", PassportCheckResult.PASSED),
                PassportCheckTask("Капот", PassportCheckResult.FAILED),
                PassportCheckTask("Багажник", PassportCheckResult.SKIPPED),
            ),
            passport.systemCheck,
        )
        assertEquals(40, passport.durationMinutes)
        assertEquals(
            PassportUnit(
                systemName = "DX-9X",
                systemId = "77",
                firmwareVersion = "3.57",
                can = PassportCan(version = "1.2", code = "0213", factory = false),
                imei = null,
                bluetoothModule = null,
                transport = PassportTransport.BLUETOOTH,
                manual = false,
            ),
            passport.unit,
        )
    }

    @Test
    fun bypassVariantsAndUsb() {
        assertEquals(PassportBypass.TRAINED, result(bypass = FastMountBypass.TRAINED).toPassportResult().works.bypass)
        assertEquals(PassportBypass.NONE, result(bypass = FastMountBypass.NONE).toPassportResult().works.bypass)
        assertEquals(PassportTransport.USB, result(ConnectionTransport.USB).toPassportResult().unit?.transport)
        assertNull(result(ConnectionTransport.SERIAL).toPassportResult().unit?.transport)
    }

    @Test
    fun unitWithoutCanHasNoCan() {
        assertNull(result(canCode = null).toPassportResult().unit?.can)
        assertEquals(true, result(canCode = "", canFactory = true).toPassportResult().unit?.can?.factory)
    }
}
