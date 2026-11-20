package ru.alarmtrade.specmp.assistant

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TodayPassport
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository

/** Заготовка `BLANK` — ещё не паспорт: ассистент предлагает его оформить. */
internal class AppAssistantPassportRepository(private val passports: MountPassportRepository) :
    AssistantPassportRepository {

    override suspend fun today(systemId: Long): Response<TodayPassport?> =
        passports.lastOfUnitToday(systemId.toString()).map { item ->
            item?.takeIf { it.status != PassportStatus.BLANK }
                ?.let { TodayPassport(id = it.id, number = it.number, issued = it.status == PassportStatus.ISSUED) }
        }
}
