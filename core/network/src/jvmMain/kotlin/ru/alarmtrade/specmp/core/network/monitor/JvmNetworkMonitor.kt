package ru.alarmtrade.specmp.core.network.monitor

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive

/**
 * На JVM нет уведомлений о смене сети — интерфейсы опрашиваются, пока есть подписчики; без них значение не
 * обновляется до следующей подписки. Ложное «сети нет» остановило бы переподключение чата SDK, поэтому пропажа
 * подтверждается вторым опросом, а сбой опроса считается наличием сети.
 */
class JvmNetworkMonitor(
    scope: CoroutineScope,
    pollPeriod: Duration = 3.seconds,
    hasNetwork: () -> Boolean = ::hasConnectedInterface,
) : NetworkMonitor {
    override val isAvailable: StateFlow<Boolean> = flow {
        var misses = 0
        while (currentCoroutineContext().isActive) {
            if (hasNetwork()) {
                misses = 0
                emit(true)
            } else if (++misses >= LOST_CONFIRMATIONS) {
                emit(false)
            }
            delay(pollPeriod)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), hasNetwork())
}

private const val LOST_CONFIRMATIONS = 2

private fun hasConnectedInterface(): Boolean = try {
    NetworkInterface.getNetworkInterfaces()?.asSequence().orEmpty().any { network ->
        network.isUp && !network.isLoopback && network.inetAddresses.asSequence().any(InetAddress::isRoutable)
    }
} catch (_: SocketException) {
    true
}

// Link-local (169.254/16, fe80::/10) интерфейс получает и без подключения к сети.
private fun InetAddress.isRoutable(): Boolean = !isLoopbackAddress && !isLinkLocalAddress
