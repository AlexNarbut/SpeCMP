package ru.alarmtrade.specmp.core.specialist

import ru.alarmtrade.common.app.domain.AppLanguage
import ru.alarmtrade.common.app.interactors.appInformation.requests.SaveLanguageRequest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.appDevice.AppDeviceInformationInteractor
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.common.ConfigurationLanguageStore
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse

/**
 * [ConfigurationLanguageStore] над хранилищем SDK: SDK сам подставляет сохранённый язык во все запросы каталога
 * и конфигураций. Начальное значение — язык приложения ([SpecialistInitializer]).
 */
internal class SpecialistConfigurationLanguageStore(private val interactor: AppDeviceInformationInteractor) :
    ConfigurationLanguageStore {

    override suspend fun current(): ConfigurationLanguage? =
        (interactor.getCarConfigurationLanguageUseCase.execute() as? SpecialistResponse.Success)?.value?.toDomain()

    override suspend fun save(language: ConfigurationLanguage): Response<Unit> =
        interactor.saveCarConfigurationLanguageUseCase.execute(SaveLanguageRequest(language.toSdk()))
            .toResponse()
            .map { }
}

private fun AppLanguage.toDomain(): ConfigurationLanguage = when (this) {
    AppLanguage.RU -> ConfigurationLanguage.RU
    AppLanguage.EN -> ConfigurationLanguage.EN
}

private fun ConfigurationLanguage.toSdk(): AppLanguage = when (this) {
    ConfigurationLanguage.RU -> AppLanguage.RU
    ConfigurationLanguage.EN -> AppLanguage.EN
}
