package ru.alarmtrade.specmp.core.push

/** `userInfo` push от APNs (через FCM): данные сервера — ключи верхнего уровня, кроме `aps`. */
internal object ApsUserInfo {

    private const val KEY_APS = "aps"
    private const val KEY_ALERT = "alert"

    fun data(userInfo: Map<*, *>): Map<String, String> = userInfo.entries
        .mapNotNull { (key, value) -> (key as? String)?.let { k -> value?.toString()?.let { k to it } } }
        .filter { (key, _) -> key != KEY_APS }
        .toMap()

    /** С `aps.alert` уведомление показывает система, второе не нужно. */
    fun hasAlert(userInfo: Map<*, *>): Boolean = (userInfo[KEY_APS] as? Map<*, *>)?.get(KEY_ALERT) != null
}
