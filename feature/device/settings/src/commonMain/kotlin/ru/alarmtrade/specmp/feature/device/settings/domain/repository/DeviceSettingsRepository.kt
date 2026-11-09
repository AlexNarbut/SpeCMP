package ru.alarmtrade.specmp.feature.device.settings.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsMismatchException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/**
 * Сессия настроек SDK для [SettingsSource]. Тонкий: без состояния; снимок «как было», повторы, перечитывание
 * при переподключении — в ViewModel.
 *
 * Правки ([setValue]) живут в сессии SDK и уходят в источник только по [save].
 */
interface DeviceSettingsRepository {
    /** Открыть сессию и прочитать меню; [reload] — перечитать из источника, а не из кэша сессии SDK. */
    suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection>

    /** Закрыть сессию без сохранения правок. */
    suspend fun close(source: SettingsSource): Response<Unit>

    /** Изменить значение настройки в сессии; ответ — настройка с новым значением. */
    suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting>

    /** Id настроек, изменённых в сессии и ещё не сохранённых. */
    suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>>

    /** Сохранить правки сессии в источник (для блока — записать в устройство). */
    suspend fun save(source: SettingsSource): Response<Unit>

    /**
     * Скопировать настройки из [from] в [to]; [saveChanges] — сразу сохранить в [to]. Ответ — меню [to].
     * Другой набор настроек (размер) — ошибка с [SettingsMismatchException].
     * Изменённые настройки [to] SDK после копирования не отмечает — [unsavedIds] их не содержит.
     */
    suspend fun copy(from: SettingsSource, to: SettingsSource, saveChanges: Boolean): Response<SettingsSection>

    /**
     * Снимок настроек [from] во временном файле, прочитанный моделью [deviceName] — так их увидит блок этой модели
     * (сравнение «Что изменится» перед загрузкой в блок, в том числе другой модели). Сессия [from] не закрывается.
     * Снимок открывается как обычный источник; после — `DeviceSettingsSessionController.discardSnapshot`.
     */
    suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File>

    /** Сбросить подключённый блок к заводским настройкам (только [SettingsSource.Device]). */
    suspend fun resetDeviceToFactory(): Response<Unit>
}
