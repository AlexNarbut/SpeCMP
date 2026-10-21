package ru.alarmtrade.specmp.core.specialist

import ru.alarmtrade.common.app.domain.AppLanguage
import ru.alarmtrade.common.app.interactors.appInformation.AppDeviceInformationInteractor
import ru.alarmtrade.common.app.interactors.appInformation.requests.SaveLanguageRequest

/**
 * Язык приложения для Specialist SDK. От него зависят тексты, которые сервер отдаёт на нужном языке (названия модулей,
 * описания прошивок, справочники); пока язык не сохранён, SDK запрашивает английский. Применяется при старте и при
 * смене языка в приложении. Язык, отличный от русского, — английский (как в `AppLocale`).
 */
class SpecialistLanguage(private val appDeviceInformationInteractor: AppDeviceInformationInteractor) {

    suspend fun apply(languageTag: String) {
        val language = if (languageTag.startsWith(RUSSIAN_TAG)) AppLanguage.RU else AppLanguage.EN
        appDeviceInformationInteractor.saveAppLanguageUseCase.execute(SaveLanguageRequest(language))
    }

    private companion object {
        const val RUSSIAN_TAG = "ru"
    }
}
