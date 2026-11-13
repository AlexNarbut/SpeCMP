package ru.alarmtrade.specmp.feature.techrequests.data.mapper

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.alarmtrade.specialist.domain.userRequests.CreateUserRequestProgress
import ru.alarmtrade.specialist.domain.userRequests.TechSupportDeviceReadException
import ru.alarmtrade.specialist.domain.userRequests.TechSupportRequestLimitException
import ru.alarmtrade.specialist.domain.userRequests.TechSupportRequestNotFoundException
import ru.alarmtrade.specialist.domain.userRequests.UserRequest
import ru.alarmtrade.specialist.domain.userRequests.UserRequestStatus
import ru.alarmtrade.specialist.domain.userRequests.UserRequestsPage
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestException
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestFailure
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestPage
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestSubmitStep

// Сервер отдаёт дату без пояса; считаем её UTC: старый desktop форматировал её в UTC без сдвига.
internal fun UserRequest.toDomain(): TechRequest = TechRequest(
    id = id,
    status = status.toDomain(),
    userName = userName,
    userPhone = userPhone,
    issue = issue,
    createdAt = creationDate.toInstant(TimeZone.UTC),
)

internal fun UserRequestStatus.toDomain(): TechRequestStatus = when (this) {
    UserRequestStatus.OPEN -> TechRequestStatus.OPEN
    UserRequestStatus.IN_PROCESS -> TechRequestStatus.IN_PROCESS
    UserRequestStatus.SUSPENDED -> TechRequestStatus.SUSPENDED
    UserRequestStatus.FINISHED -> TechRequestStatus.FINISHED
    UserRequestStatus.FINISHED_BY_USER -> TechRequestStatus.FINISHED_BY_USER
    UserRequestStatus.CANCELED_BY_USER -> TechRequestStatus.CANCELED_BY_USER
}

internal fun UserRequestsPage.toDomain(): TechRequestPage = TechRequestPage(items.map { it.toDomain() }, hasMore)

internal fun CreateUserRequestProgress.toDomain(): TechRequestSubmitStep = when (this) {
    CreateUserRequestProgress.ReadingDevice -> TechRequestSubmitStep.ReadingDevice
    CreateUserRequestProgress.Sending -> TechRequestSubmitStep.Sending
    is CreateUserRequestProgress.Created -> TechRequestSubmitStep.Created(request.toDomain())
}

/** Ошибки SDK, которые различает фича; прочие (сеть, Bluetooth) уже приведены `toResponse()` к `AppException`. */
internal fun Throwable.toTechRequestException(): Throwable {
    val failure = when (this) {
        is TechSupportRequestLimitException -> TechRequestFailure.Limit
        is TechSupportRequestNotFoundException -> TechRequestFailure.NotFound
        is TechSupportDeviceReadException -> TechRequestFailure.DeviceRead
        else -> return this
    }
    return TechRequestException(failure, this)
}
