package ru.alarmtrade.specmp.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.fastmount.AppFastMountPassportRepositoryTest
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TodayPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus

class AppAssistantPassportRepositoryTest {

    private val passports = AppFastMountPassportRepositoryTest.Passports()
    private val repository = AppAssistantPassportRepository(passports)

    @Test
    fun noPassportToday() = runTest {
        assertEquals(null, assertIs<Response.Success<TodayPassport?>>(repository.today(77)).value)
    }

    @Test
    fun todayPassportIsIssuedByStatus() = runTest {
        passports.lastToday = Response.Success(AppFastMountPassportRepositoryTest.summary("p1", PassportStatus.ISSUED))
        assertEquals(
            TodayPassport("p1", "N-p1", issued = true),
            assertIs<Response.Success<TodayPassport?>>(repository.today(77)).value,
        )

        passports.lastToday = Response.Success(AppFastMountPassportRepositoryTest.summary("p2", PassportStatus.DRAFT))
        assertEquals(false, assertIs<Response.Success<TodayPassport?>>(repository.today(77)).value?.issued)
    }

    @Test
    fun blankIsNotAPassportYet() = runTest {
        passports.lastToday = Response.Success(AppFastMountPassportRepositoryTest.summary("p3", PassportStatus.BLANK))
        assertEquals(null, assertIs<Response.Success<TodayPassport?>>(repository.today(77)).value)
    }
}
