package ru.alarmtrade.specmp.core.specialist.account

import co.touchlab.kermit.Logger
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.user.UserInteractor
import ru.alarmtrade.specmp.core.common.Response

class SpecialistAccountRepository(private val userInteractor: UserInteractor) : AccountRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun currentProfile(): Response<AccountProfile> =
        when (val response = userInteractor.getUserUseCase.execute()) {
            is SpecialistResponse.Success -> {
                val user = response.value
                Response.Success(
                    AccountProfile(
                        name = user.userContactInformation.fullName,
                        email = user.login
                    )
                )
            }

            is SpecialistResponse.Error -> {
                logger.d(response.exception) { "Failed to fetch current account profile" }
                Response.Error(
                    exception = response.exception,
                    message = response.message ?: response.exception.message
                )
            }
        }
}
