package ru.alarmtrade.specmp.core.lifecycle

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification

internal actual fun platformAppLifecycle(): AppLifecycle = UiApplicationLifecycle()

/** Уведомления `UIApplication`: приходят только при смене состояния, стартового события нет. */
private class UiApplicationLifecycle : AppLifecycle {
    override val events: Flow<AppLifecycleEvent> = callbackFlow {
        val center = NSNotificationCenter.defaultCenter
        val queue = NSOperationQueue.mainQueue
        val foreground = center.addObserverForName(UIApplicationWillEnterForegroundNotification, null, queue) {
            trySend(AppLifecycleEvent.FOREGROUND)
        }
        val background = center.addObserverForName(UIApplicationDidEnterBackgroundNotification, null, queue) {
            trySend(AppLifecycleEvent.BACKGROUND)
        }
        awaitClose {
            center.removeObserver(foreground)
            center.removeObserver(background)
        }
    }
}
