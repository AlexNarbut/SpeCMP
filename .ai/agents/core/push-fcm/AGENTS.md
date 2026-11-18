# core/push-fcm

- Область: Firebase Cloud Messaging на Android — Android-only KMP-модуль (`specmp.kmp.android`), подключает только флейвор `google` `androidApp` (`"googleImplementation"`); от него не зависит никто другой (graph-assert).
- `FcmPushTokenSource` — `AndroidPushTokenSource` из `core/push` над `FirebaseMessaging` (`token`/`deleteToken`, `kotlinx-coroutines-play-services`). `di/fcmPushModule()` биндит его как `PushTokenSource` и `AuthCloseable`; `Context` — из платформенных модулей `composeApp`.
- `FcmMessagingService` (манифест модуля, `com.google.firebase.MESSAGING_EVENT`) → `runBlocking { AndroidPushReceiver.show(…) }` на рабочем потоке FCM (`data`, `notification`, ключ — `messageId`/`sentTime`). `onNewToken` пустой: токен уходит при старте главного. meta-data `default_notification_channel_id`/`_icon` — канал и иконка push, показанного самим FCM в фоне.
- Остальное (разбор, показ, каналы, тап) — в `core/push`, см. `core/push/AGENTS.md`.
