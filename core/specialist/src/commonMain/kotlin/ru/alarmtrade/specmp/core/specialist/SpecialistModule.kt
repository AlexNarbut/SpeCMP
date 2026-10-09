package ru.alarmtrade.specmp.core.specialist

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.common.core.configuration.CommonConfigurator
import ru.alarmtrade.common.core.configuration.ProvideCommon
import ru.alarmtrade.common.core.configuration.NetworkMonitor as SdkNetworkMonitor
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.core.specialist.account.AccountRepository
import ru.alarmtrade.specmp.core.specialist.account.SpecialistAccountRepository
import ru.alarmtrade.specialist.SpecialistApp
import ru.alarmtrade.specialist.SpecialistConfig
import ru.alarmtrade.specialist.network.NetworkLogLevel

val specialistModule = module {
    single<SpecialistAppVersionProvider> {
        SpecialistAppVersionProvider { CurrentSpecialistAppVersion }
    }

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
            )
        )
    }
    single { get<SpecialistApp>().authInteractor }
    single { get<SpecialistApp>().appDeviceInformationInteractor }
    single { get<SpecialistApp>().appUpdatesInteractor }
    single { get<SpecialistApp>().userInteractor }
    single { get<SpecialistApp>().chatInteractor }
    single { get<SpecialistApp>().filesInteractor }
    singleOf(::SpecialistAccountRepository) bind AccountRepository::class
    single<SpecialistDeviceInformationProvider> {
        SpecialistDeviceInformationProvider { createSpecialistDeviceInformation() }
    }
}
