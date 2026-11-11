package ru.alarmtrade.specmp.feature.mountassistant.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateSummary

/** Факты о подключённом блоке и сети; `null` — блок не в рабочем режиме. */
interface AssistantDeviceRepository {
    fun facts(): Flow<Response<DeviceFacts?>>
}

interface AssistantFirmwareRepository {
    suspend fun check(): Response<FirmwareCheck>
}

interface AssistantAutostartRepository {
    suspend fun status(): Response<AutostartStatus>
}

interface AssistantStateRepository {
    suspend fun summary(): Response<StateSummary>
}

/**
 * Работает в общей сессии настроек блока. Ошибка записи — `AssistantWriteException`; значение, которое блок не
 * принял, остаётся в сессии несохранённым до [save] или [revert].
 */
interface AssistantSettingsRepository {
    suspend fun find(query: String): Response<List<FoundSetting>>

    suspend fun setting(id: Int): Response<FoundSetting>

    /** Сколько несохранённых правок в сессии, кроме [settingId]. */
    suspend fun otherUnsaved(settingId: Int): Response<Int>

    suspend fun write(settingId: Int, value: AssistantSettingValue): Response<Unit>

    suspend fun save(): Response<Unit>

    suspend fun revert(settingId: Int, value: AssistantSettingValue): Response<Unit>
}
