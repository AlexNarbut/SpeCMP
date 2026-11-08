package ru.alarmtrade.specmp.core.specialist

import co.touchlab.kermit.Logger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alarmtrade.common.app.domain.AppLanguage
import ru.alarmtrade.common.app.interactors.appInformation.requests.GetUserPreferenceRequest
import ru.alarmtrade.common.app.interactors.appInformation.requests.SaveAppDeviceInformationRequest
import ru.alarmtrade.common.app.interactors.appInformation.requests.SaveLanguageRequest
import ru.alarmtrade.common.app.interactors.appInformation.requests.SaveUserPreferenceRequest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.appDevice.AppDeviceInformationInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse

/**
 * Начальная настройка Specialist SDK (по образцу `RootViewModel` настольного приложения): информация о клиенте,
 * язык приложения, язык конфигураций. App-scoped: [start] идемпотентен, работает в своём скоупе и переживает экраны;
 * пока он не закончил, [ready] = `false` — корень приложения не показывает UI, чтобы первые запросы уходили
 * с правильным языком и клиентом.
 *
 * - Клиент: актуальная [AppDeviceInformation][ru.alarmtrade.common.app.domain.AppDeviceInformation] сравнивается с
 *   сохранённой и сохраняется, если пусто или отличается (например, после обновления приложения). Идентификатор
 *   клиента — UUID, созданный при первом запуске и сохранённый в пользовательских настройках SDK.
 * - Язык приложения — от него зависят тексты, которые сервер отдаёт на нужном языке (названия модулей, описания
 *   прошивок). Без сохранённого языка SDK запрашивает английский.
 * - Язык конфигураций автомобилей — сохранённый, иначе язык приложения; дальше меняется своей настройкой.
 */
class SpecialistInitializer(
    private val interactor: AppDeviceInformationInteractor,
    private val deviceInformationProvider: SpecialistDeviceInformationProvider,
    private val scope: CoroutineScope,
) {
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val _ready = MutableStateFlow(false)

    /** Начальная настройка закончена (успешно или нет): UI можно показывать. */
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val started = MutableStateFlow(false)

    /**
     * Запустить начальную настройку с языком [languageTag] (`ru` / `en`); повторный вызов ничего не делает. Язык
     * меняется только перезапуском приложения — отдельной синхронизации смены языка нет.
     */
    fun start(languageTag: String) {
        if (!started.compareAndSet(expect = false, update = true)) return
        scope.launch {
            try {
                syncClientInfo()
                val language = applyLanguage(languageTag)
                initConfigurationsLanguage(language)
            } finally {
                // Сбой настройки не должен оставить пользователя на пустом экране.
                _ready.update { true }
            }
        }
    }

    /**
     * Информация о клиенте: сохранить, если пусто или отличается от актуальной. Вызывается при старте и перед входом
     * (логином и повторной отправкой письма активации).
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun syncClientInfo(): Response<Unit> {
        val actual = deviceInformationProvider.create().copy(deviceId = clientId())
        val stored = (interactor.getAppDeviceInformationUseCase.execute() as? SpecialistResponse.Success)?.value
        if (stored == actual) return Response.Success(Unit)
        return interactor.saveAppDeviceInformationUseCase.execute(SaveAppDeviceInformationRequest(actual))
            .toResponse()
            .map { }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun clientId(): String {
        val saved = interactor.getUserPreferenceUseCase.execute(GetUserPreferenceRequest(CLIENT_ID_KEY))
        (saved as? SpecialistResponse.Success)?.value?.takeIf { it.isNotBlank() }?.let { return it }
        val created = Uuid.random().toString()
        interactor.saveUserPreferenceUseCase.execute(SaveUserPreferenceRequest(CLIENT_ID_KEY, created))
        return created
    }

    private suspend fun applyLanguage(languageTag: String): AppLanguage {
        val language = if (languageTag.startsWith(RUSSIAN_TAG)) AppLanguage.RU else AppLanguage.EN
        val response = interactor.saveAppLanguageUseCase.execute(SaveLanguageRequest(language))
        if (response is SpecialistResponse.Error) logger.w(response.exception) { "Failed to save the app language" }
        return language
    }

    private suspend fun initConfigurationsLanguage(appLanguage: AppLanguage) {
        val saved = (interactor.getCarConfigurationLanguageUseCase.execute() as? SpecialistResponse.Success)?.value
        interactor.saveCarConfigurationLanguageUseCase.execute(SaveLanguageRequest(saved ?: appLanguage))
    }

    private companion object {
        const val RUSSIAN_TAG = "ru"
        const val CLIENT_ID_KEY = "SpeCMPClient"
    }
}
