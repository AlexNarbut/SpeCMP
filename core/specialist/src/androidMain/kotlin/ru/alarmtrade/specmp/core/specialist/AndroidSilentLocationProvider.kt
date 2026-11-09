package ru.alarmtrade.specmp.core.specialist

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager

/** Только при уже выданном разрешении; активного запроса координат нет — монтаж не ждёт GPS. */
class AndroidSilentLocationProvider(private val context: Context) : SilentLocationProvider {

    override suspend fun lastKnown(): Pair<Float, Float>? {
        if (!granted()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val best = try {
            manager.getProviders(true)
                .mapNotNull { provider -> manager.getLastKnownLocation(provider) }
                .maxByOrNull(Location::getTime)
        } catch (ignored: SecurityException) {
            null
        }
        return best?.let { it.latitude.toFloat() to it.longitude.toFloat() }
    }

    private fun granted(): Boolean = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ).any { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
}
