package ru.alarmtrade.specmp.core.network.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * [NetworkMonitor] поверх `ConnectivityManager` (нужен `ACCESS_NETWORK_STATE`).
 *
 * Учитывает блокировку сети процессу в фоне (Android 15+, `onBlockedStatusChanged`): сокет в фоне
 * рвёт ОС, и без этого сигнала переподключение крутилось бы впустую. Колбэк регистрируется на всё
 * время жизни процесса — экземпляр должен быть синглтоном.
 */
class AndroidNetworkMonitor(context: Context) : NetworkMonitor {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isAvailable = MutableStateFlow(connectivityManager.activeNetwork != null)
    override val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isAvailable.update { true }
        }

        override fun onLost(network: Network) {
            // При смене сети по умолчанию потерянная может ещё числиться активной.
            val active = connectivityManager.activeNetwork
            _isAvailable.update { active != null && active != network }
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            _isAvailable.update { !blocked }
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }
}
