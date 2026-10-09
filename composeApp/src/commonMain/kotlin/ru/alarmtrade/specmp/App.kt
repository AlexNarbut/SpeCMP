package ru.alarmtrade.specmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.network.di.networkModule
import ru.alarmtrade.specmp.core.localization.LocalAppLocale
import ru.alarmtrade.specmp.core.localization.applyAppLocale
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersion
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersionProvider
import ru.alarmtrade.specmp.core.specialist.specialistModule
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.view.image.ProvideCmpImageLoader
import ru.alarmtrade.specmp.core.ui.view.image.rememberCmpImageLoader
import ru.alarmtrade.specmp.di.rememberPlatformModule
import ru.alarmtrade.specmp.feature.authstate.di.authStateModule
import ru.alarmtrade.specmp.feature.login.di.loginModule
import ru.alarmtrade.specmp.feature.main.di.mainModule
import ru.alarmtrade.specmp.feature.supportchat.di.supportChatModule
import ru.alarmtrade.specmp.navigation.AppNavigation

@Preview
@Composable
fun App(
    appVersionName: String? = null,
    appVersionCode: Int? = null,
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) {
    var appLocale by remember { mutableStateOf(prepareAppLocale()) }

    val overrideAppVersionModule = remember(appVersionName, appVersionCode) {
        if (appVersionName != null && appVersionCode != null) {
            module {
                single<SpecialistAppVersionProvider> {
                    SpecialistAppVersionProvider {
                        SpecialistAppVersion(
                            name = appVersionName,
                            code = appVersionCode,
                        )
                    }
                }
            }
        } else {
            null
        }
    }

    val platformModule = rememberPlatformModule()

    KoinApplication(application = {
        modules(
            buildList {
                add(platformModule)
                add(commonModule)
                add(networkModule)
                add(specialistModule)
                overrideAppVersionModule?.let { add(it) }
                add(authStateModule)
                add(loginModule)
                add(mainModule)
                add(supportChatModule)
            }
        )
    }) {
        CompositionLocalProvider(LocalAppLocale provides appLocale) {
            // Единый ImageLoader приложения: memory/disk-кэш Coil общий для
            // всех экранов (U-1).
            ProvideCmpImageLoader(rememberCmpImageLoader()) {
                AppTheme(onThemeChanged) {
                    AppNavigation(
                        onLocaleChanged = { locale ->
                            applyAppLocale(locale)
                            appLocale = locale
                        },
                    )
                }
            }
        }
    }
}
