package ru.alarmtrade.specmp.androidApp

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

// FragmentActivity (наследник ComponentActivity) нужен для привязки
// PermissionsController из core/permissions (bind → supportFragmentManager).
class AppActivity : FragmentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            // testTag видны снаружи (uiautomator, ui-test) как resource-id.
            Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                App(
                    appVersionName = versionName,
                    appVersionCode = versionCode,
                    onThemeChange = { ThemeChanged(it) }
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
