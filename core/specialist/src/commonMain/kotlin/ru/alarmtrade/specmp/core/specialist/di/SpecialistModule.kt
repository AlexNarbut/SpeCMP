package ru.alarmtrade.specmp.core.specialist.di

import org.koin.dsl.module
import ru.alarmtrade.common.core.configuration.CommonConfigurator
import ru.alarmtrade.common.core.configuration.NetworkMonitor as SdkNetworkMonitor
import ru.alarmtrade.common.core.configuration.ProvideCommon
import ru.alarmtrade.common.core.configuration.ProvideGeo
import ru.alarmtrade.specialist.SpecialistApp
import ru.alarmtrade.specialist.SpecialistConfig
import ru.alarmtrade.specialist.network.NetworkLogLevel
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.AppVersionProvider
import ru.alarmtrade.specmp.core.common.ConfigurationLanguageStore
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.core.specialist.SilentLocationProvider
import ru.alarmtrade.specmp.core.specialist.SpecialistConfigurationLanguageStore
import ru.alarmtrade.specmp.core.specialist.SpecialistDeviceInformationProvider
import ru.alarmtrade.specmp.core.specialist.SpecialistInitializer
import ru.alarmtrade.specmp.core.specialist.createSpecialistDeviceInformation

/** [AppVersionProvider] добавляет [specialistModules]: версию знает только платформа. */
val specialistModule = module {
    single {
        val dispatchers: AppDispatchers = get()
        val appVersionProvider: AppVersionProvider = get()

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
                    logWebSocketFrames = false,
                ),
                mainDispatcher = dispatchers.main,
                backgroundDispatcher = dispatchers.default,
            ),
        )
    }
    single { get<SpecialistApp>().authInteractor }
    single { get<SpecialistApp>().appDeviceInformationInteractor }
    single { get<SpecialistApp>().appUpdatesInteractor }
    single { get<SpecialistApp>().userInteractor }
    single { get<SpecialistApp>().chatInteractor }
    single { get<SpecialistApp>().filesInteractor }
    single { get<SpecialistApp>().bluetoothConnectorInteractor }
    single { get<SpecialistApp>().usbConnectorInteractor }
    // На iOS в SDK — TODO(): резолвить лениво.
    single { get<SpecialistApp>().serialConnectorInteractor }
    single { get<SpecialistApp>().bluetoothAlarmDeviceInteractor }
    single { get<SpecialistApp>().verificationInteractor }
    single { get<SpecialistApp>().deviceInformationInteractor }
    single { get<SpecialistApp>().firmwareFilesInteractor }
    single { get<SpecialistApp>().alarmDeviceSettingsInteractor }
    single { get<SpecialistApp>().carsInteractor }
    single { get<SpecialistApp>().configurationInteractor }
    single { get<SpecialistApp>().localConfigurationInteractor }
    single { get<SpecialistApp>().fastMountInteractor }
    single { SpecialistInitializer(get(), get(), get(appScopeQualifier)) }
    single<ConfigurationLanguageStore> { SpecialistConfigurationLanguageStore(get()) }
    single<SpecialistDeviceInformationProvider> {
        SpecialistDeviceInformationProvider { createSpecialistDeviceInformation() }
    }
}
