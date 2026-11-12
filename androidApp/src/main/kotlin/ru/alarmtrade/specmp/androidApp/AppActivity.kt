package ru.alarmtrade.specmp.androidApp

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import ru.alarmtrade.specmp.App
import ru.alarmtrade.specmp.core.localization.reapplyAppLocale
import ru.alarmtrade.specmp.core.localization.withAppLocale

// FragmentActivity (наследник ComponentActivity) нужен для привязки
// PermissionsController из core/permissions (bind → supportFragmentManager).
class AppActivity : FragmentActivity() {
    // Язык приложения — в конфигурации Activity и процессной локали до первой композиции; переживает смену
    // конфигурации (поворот при configChanges). Смена языка — сохранение + перезапуск (docs/localization.md).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withAppLocale())
    }

    // Смена конфигурации без пересоздания (configChanges) возвращает процессную локаль к системной — вернуть свою.
    override fun onConfigurationChanged(newConfig: Configuration) {
        reapplyAppLocale()
        super.onConfigurationChanged(newConfig)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Прошивка и долгий обмен с блоком идут минутами: погасший экран останавливает приложение.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }

        val versionName = packageInfo.versionName.orEmpty()
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }

        setContent {
            Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                App(
                    appVersionName = versionName,
                    appVersionCode = versionCode,
                    onThemeChange = { ThemeChanged(it) },
                )
            }
        }
    }
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isDark
            isAppearanceLightNavigationBars = isDark
        }
    }
}
