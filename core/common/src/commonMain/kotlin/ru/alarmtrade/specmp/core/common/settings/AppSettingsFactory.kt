package ru.alarmtrade.specmp.core.common.settings

import com.russhwolf.settings.Settings

/**
 * Все хранилища — в одном платформенном `Settings`, изоляция префиксом ключа `"<name>."`: [Settings.clear] и
 * [Settings.keys] именованного хранилища затрагивают только его ключи.
 */
class AppSettingsFactory(private val root: Settings) {

    fun create(name: String): Settings {
        require(name.isNotBlank() && NAME_SEPARATOR !in name) { "Invalid settings name: '$name'" }
        return NamespacedSettings(root, "$name$NAME_SEPARATOR")
    }

    private companion object {
        const val NAME_SEPARATOR = "."
    }
}

private class NamespacedSettings(private val root: Settings, private val prefix: String) : Settings {

    override val keys: Set<String>
        get() = root.keys.filter { it.startsWith(prefix) }.map { it.removePrefix(prefix) }.toSet()

    override val size: Int
        get() = keys.size

    override fun clear() {
        root.keys.filter { it.startsWith(prefix) }.forEach(root::remove)
    }

    override fun remove(key: String) = root.remove(prefix + key)
    override fun hasKey(key: String): Boolean = root.hasKey(prefix + key)

    override fun putInt(key: String, value: Int) = root.putInt(prefix + key, value)
    override fun getInt(key: String, defaultValue: Int): Int = root.getInt(prefix + key, defaultValue)
    override fun getIntOrNull(key: String): Int? = root.getIntOrNull(prefix + key)

    override fun putLong(key: String, value: Long) = root.putLong(prefix + key, value)
    override fun getLong(key: String, defaultValue: Long): Long = root.getLong(prefix + key, defaultValue)
    override fun getLongOrNull(key: String): Long? = root.getLongOrNull(prefix + key)

    override fun putString(key: String, value: String) = root.putString(prefix + key, value)
    override fun getString(key: String, defaultValue: String): String = root.getString(prefix + key, defaultValue)
    override fun getStringOrNull(key: String): String? = root.getStringOrNull(prefix + key)

    override fun putFloat(key: String, value: Float) = root.putFloat(prefix + key, value)
    override fun getFloat(key: String, defaultValue: Float): Float = root.getFloat(prefix + key, defaultValue)
    override fun getFloatOrNull(key: String): Float? = root.getFloatOrNull(prefix + key)

    override fun putDouble(key: String, value: Double) = root.putDouble(prefix + key, value)
    override fun getDouble(key: String, defaultValue: Double): Double = root.getDouble(prefix + key, defaultValue)
    override fun getDoubleOrNull(key: String): Double? = root.getDoubleOrNull(prefix + key)

    override fun putBoolean(key: String, value: Boolean) = root.putBoolean(prefix + key, value)
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = root.getBoolean(prefix + key, defaultValue)
    override fun getBooleanOrNull(key: String): Boolean? = root.getBooleanOrNull(prefix + key)
}
