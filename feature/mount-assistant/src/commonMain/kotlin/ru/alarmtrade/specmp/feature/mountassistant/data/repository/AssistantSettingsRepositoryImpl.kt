package ru.alarmtrade.specmp.feature.mountassistant.data.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfError
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsWrongPinException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.pathTo
import ru.alarmtrade.specmp.feature.device.settings.domain.model.search
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.mountassistant.data.mapper.toFoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.data.mapper.toSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantWriteException
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantSettingsRepository

/**
 * Сессия настроек блока одна на приложение: открываем без перечитывания (не сбрасывая чужих правок) и не закрываем —
 * закрывает экран. Дерево не кэшируем: после редактора значения могли измениться.
 */
internal class AssistantSettingsRepositoryImpl(private val repository: DeviceSettingsRepository) :
    AssistantSettingsRepository {

    override suspend fun find(query: String): Response<List<FoundSetting>> =
        repository.open(SettingsSource.Device, reload = false).mapIfSuccess { root ->
            val matches = repository.search(SettingsSource.Device, query).getValueIfSuccess().orEmpty()
            Response.Success(
                root.search(query, matches).mapNotNull { hit ->
                    (hit.node as? Setting)?.toFoundSetting(hit.path, hit.highlights.name)
                },
            )
        }

    override suspend fun setting(id: Int): Response<FoundSetting> =
        repository.open(SettingsSource.Device, reload = false).mapIfSuccess { root ->
            val setting = root.findNode(id) as? Setting
            val path = root.pathTo(id)
            if (setting == null || path == null) {
                Response.Error(NoSuchElementException("Setting $id is not in the device menu"))
            } else {
                Response.Success(setting.toFoundSetting(path, emptyList()))
            }
        }

    override suspend fun otherUnsaved(settingId: Int): Response<Int> =
        repository.unsavedIds(SettingsSource.Device).map { (it - settingId).size }

    override suspend fun write(settingId: Int, value: AssistantSettingValue): Response<Unit> =
        repository.open(SettingsSource.Device, reload = false)
            .mapIfSuccess { repository.setValue(SettingsSource.Device, settingId, value.toSettingValue()) }
            .mapIfSuccess { save() }

    override suspend fun save(): Response<Unit> = repository.save(SettingsSource.Device).mapIfError { error ->
        val failure = if (error.exception is SettingsWrongPinException) WriteFailure.WRONG_PIN else WriteFailure.FAILED
        Response.Error(AssistantWriteException(failure, error.exception))
    }

    override suspend fun revert(settingId: Int, value: AssistantSettingValue): Response<Unit> =
        repository.setValue(SettingsSource.Device, settingId, value.toSettingValue()).map { }
}
