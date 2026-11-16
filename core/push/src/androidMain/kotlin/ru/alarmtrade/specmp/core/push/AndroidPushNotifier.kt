package ru.alarmtrade.specmp.core.push

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import co.touchlab.kermit.Logger
import java.net.URL
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.safeRun

/** Показ push без Koin и SDK: процесс мог быть разбужен FCM без UI (Koin поднимается в композиции). */
internal class AndroidPushNotifier(private val context: Context) {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    suspend fun show(payload: PushPayload, notificationId: Int) {
        if (!canNotify()) {
            logger.d { "Notifications are not allowed, push ${payload.type} is skipped" }
            return
        }
        PushChannel.ensureAll(context)
        val channel = PushChannel.of(payload.type)
        val open = openIntent(payload, notificationId)
        val builder = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(channel.icon)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(open)
        channel.action?.let { builder.addAction(0, getString(it), open) }
        val picture = payload.imageUrl?.let(::loadPicture)
        if (picture != null) {
            builder.setLargeIcon(picture).setStyle(NotificationCompat.BigPictureStyle().bigPicture(picture))
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
        }
        @Suppress("MissingPermission") // проверено в canNotify()
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun canNotify(): Boolean {
        val permitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permitted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Тап — в launcher-активити с данными push в extras: их разбирает `PushOpenRequests.offer(intent)`. */
    private fun openIntent(payload: PushPayload, requestCode: Int): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(PushPayload.KEY_TYPE, payload.type.code.toString())
        payload.actionUrl?.let { intent.putExtra(PushPayload.KEY_ACTION_URL, it) }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    // Вызывается на рабочем потоке FCM-сервиса: блокирующая загрузка допустима, ошибка — уведомление без картинки.
    private fun loadPicture(url: String): Bitmap? {
        if (!url.startsWith("https://", ignoreCase = true)) return null
        return safeRun {
            val connection = URL(url).openConnection().apply {
                connectTimeout = IMAGE_TIMEOUT_MS
                readTimeout = IMAGE_TIMEOUT_MS
            }
            connection.getInputStream().use(BitmapFactory::decodeStream)
        }.getValueIfSuccess()
    }

    private companion object {
        const val IMAGE_TIMEOUT_MS = 5_000
    }
}
