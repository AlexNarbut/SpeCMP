@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.permissions

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController

internal class AndroidPermissionsProvider(private val applicationContext: Context) : PermissionsProvider {
    private val controller = PermissionsController(applicationContext = applicationContext)

    override fun getController(): PermissionsController = controller

    override fun getBlePermission(): List<Permission> = blePermissions()

    override fun isLocationEnabled(): Boolean {
        // Android 12+ : BLUETOOTH_SCAN с neverForLocation — геолокация для скана не нужна.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return true
        val manager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    override fun openLocationSettings() {
        startSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }

    override fun openBluetoothSettings() {
        startSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
    }

    private fun startSettings(action: String) {
        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
    }
}

actual fun permissionsModule(): Module = module {
    single<PermissionsProvider> { AndroidPermissionsProvider(get()) }
}
