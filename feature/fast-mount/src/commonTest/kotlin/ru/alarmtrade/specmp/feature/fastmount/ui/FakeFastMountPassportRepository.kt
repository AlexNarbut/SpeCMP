package ru.alarmtrade.specmp.feature.fastmount.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountPassportRepository

internal class FakeFastMountPassportRepository : FastMountPassportRepository {
    val passport = MutableStateFlow<FastMountPassport?>(null)
    val observed = mutableListOf<String>()
    val blanks = mutableListOf<FastMountResult>()

    override fun observe(passportId: String): Flow<Response<FastMountPassport?>> {
        observed += passportId
        return passport.map { Response.Success(it) }
    }

    override suspend fun createBlank(result: FastMountResult): Response<String> {
        blanks += result
        return Response.Success("blank")
    }
}
