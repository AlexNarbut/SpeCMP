package ru.alarmtrade.specmp.core.specialist

import org.koin.dsl.module
import ru.alarmtrade.common.core.configuration.CommonConfigurator
import ru.alarmtrade.common.core.configuration.NetworkMonitor as SdkNetworkMonitor
import ru.alarmtrade.common.core.configuration.ProvideCommon
import ru.alarmtrade.common.core.configuration.ProvideGeo
import ru.alarmtrade.specialist.SpecialistApp
import ru.alarmtrade.specialist.SpecialistConfig
import ru.alarmtrade.specialist.network.NetworkLogLevel
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.ConfigurationLanguageStore
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor

/** [SpecialistAppVersionProvider] регистрирует composition root (`App(appVersion)`): версию знает платформа. */
val specialistModule = module {
    single {
        val dispatchers: AppDispatchers = get()
        val appVersionProvider: SpecialistAppVersionProvider = get()

        CommonConfigurator.commonProvider = object : ProvideCommon {
            override fun buildVersion(): Int = appVersionProvider.current().code
        }
        // Монитор сети — до создания SDK: по нему SDK ждёт сеть вместо попыток переподключения
        // (в т. ч. блокировку сети в фоне на Android 15). Нет монитора (desktop) — дефолт SDK.
        getOrNull<NetworkMonitor>()?.let { monitor ->
            CommonConfigurator.networkMonitor = object : SdkNetworkMonitor {
                override val isAvailable = monitor.isAvailable
            }
        }

        // Координаты для отчёта быстрого монтажа — молча, без запроса разрешения; нет провайдера — `0,0` от SDK.
        getOrNull<SilentLocationProvider>()?.let { location ->
            CommonConfigurator.provideGeo = object : ProvideGeo {
                override suspend fun getCurrentLocation(): Pair<Float, Float>? = location.lastKnown()
            }
        }

        SpecialistApp(
            config = SpecialistConfig(
                appName = "SpeCMP",
                // HTTP/WebSocket-логи SDK выключены во всех сборках: тела ответов (переписка) и
                // query (email) не маскируются.
                networkConfig = SpecialistConfig.Network(
                    logLevel = NetworkLogLevel.NONE,
                    logWebSocketFrames = false
                ),
                mainDispatcher = dispatchers.main,
                backgroundDispatcher = dispatchers.default
            )
        )
    }
    single { get<SpecialistApp>().authInteractor }
    single { get<SpecialistApp>().appDeviceInformationInteractor }
    single { get<SpecialistApp>().appUpdatesInteractor }
    single { get<SpecialistApp>().userInteractor }
    single { get<SpecialistApp>().chatInteractor }
    single { get<SpecialistApp>().filesInteractor }
    // Device connection interactors (feature/device/*): скан и сессии BLE/USB,
    // операции с подключённым alarm-устройством.
    single { get<SpecialistApp>().bluetoothConnectorInteractor }
    single { get<SpecialistApp>().usbConnectorInteractor }
    // COM-порт (приложение прошивки): скан портов и serial-сессия блока. На iOS в SDK — TODO(), резолвить лениво.
    single { get<SpecialistApp>().serialConnectorInteractor }
    single { get<SpecialistApp>().bluetoothAlarmDeviceInteractor }
    // Проверка подключённого блока на сервере (feature/advanced-mount).
    single { get<SpecialistApp>().verificationInteractor }
    // Прошивка блока (feature/device/firmware): модули блока и версии прошивок с сервера.
    single { get<SpecialistApp>().deviceInformationInteractor }
    single { get<SpecialistApp>().firmwareFilesInteractor }
    // Расширенные настройки блока (feature/device/settings): сессии настроек по SettingsPath.
    single { get<SpecialistApp>().alarmDeviceSettingsInteractor }
    // Каталог автомобилей (feature/vehicle): марки, модели, карточки, поиск по CAN-коду.
    single { get<SpecialistApp>().carsInteractor }
    // Каталог автомобилей с готовыми конфигурациями (feature/vehicle) и сами конфигурации.
    single { get<SpecialistApp>().configurationInteractor }
    // «Мои конфигурации» настроек в хранилище приложения (feature/configurator).
    single { get<SpecialistApp>().localConfigurationInteractor }
    // Мастер быстрого монтажа (feature/fast-mount): шаги, вопросы проверки системы, отзыв.
    single { get<SpecialistApp>().fastMountInteractor }
    single { SpecialistInitializer(get(), get(), get(appScopeQualifier)) }
    // Язык конфигураций — настройка приложения (feature/app-settings).
    single<ConfigurationLanguageStore> { SpecialistConfigurationLanguageStore(get()) }
    single<SpecialistDeviceInformationProvider> {
        SpecialistDeviceInformationProvider { createSpecialistDeviceInformation() }
    }
}
