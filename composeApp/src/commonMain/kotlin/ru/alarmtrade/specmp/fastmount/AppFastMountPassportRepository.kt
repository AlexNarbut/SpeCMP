package ru.alarmtrade.specmp.fastmount

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.navigation.toPassportResult

/** Паспорт для финала быстрого монтажа — над репозиторием паспорта монтажа. */
internal class AppFastMountPassportRepository(private val passports: MountPassportRepository) :
    FastMountPassportRepository {

    override fun observe(passportId: String): Flow<Response<FastMountPassport?>> =
        passports.observeAll().map { response ->
            response.map { list ->
                list.firstOrNull { it.id == passportId }?.let {
                    FastMountPassport(id = it.id, number = it.number, issued = it.status == PassportStatus.ISSUED)
                }
            }
        }

    override suspend fun createBlank(result: FastMountResult): Response<String> =
        passports.createBlank(result.toPassportResult())
}
