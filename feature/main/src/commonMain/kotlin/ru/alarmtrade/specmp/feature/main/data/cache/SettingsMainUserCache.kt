package ru.alarmtrade.specmp.feature.main.data.cache

import com.russhwolf.settings.Settings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

internal class SettingsMainUserCache(private val settings: Settings) : MainUserCache {

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun profile(): MainUserProfile? =
        read(KEY_PROFILE, CachedProfile.serializer())?.let { MainUserProfile(name = it.name, email = it.email) }

    override suspend fun appFeatures(): MainUserAppFeatures? = read(KEY_FEATURES, CachedAppFeatures.serializer())?.let {
        MainUserAppFeatures(
            hasOrdersRequest = it.hasOrdersRequest,
            hasProductSearch = it.hasProductSearch,
        )
    }

    override suspend fun commercial(): MainUserCommercial? =
        read(KEY_COMMERCIAL, CachedCommercial.serializer())?.let { MainUserCommercial(it.warehouse1CUrl) }

    override suspend fun saveProfile(profile: MainUserProfile) =
        write(KEY_PROFILE, CachedProfile.serializer(), CachedProfile(name = profile.name, email = profile.email))

    override suspend fun saveAppFeatures(features: MainUserAppFeatures) = write(
        KEY_FEATURES,
        CachedAppFeatures.serializer(),
        CachedAppFeatures(
            hasOrdersRequest = features.hasOrdersRequest,
            hasProductSearch = features.hasProductSearch,
        ),
    )

    override suspend fun saveCommercial(commercial: MainUserCommercial) =
        write(KEY_COMMERCIAL, CachedCommercial.serializer(), CachedCommercial(commercial.warehouse1CUrl))

    override suspend fun release() {
        mutex.withLock { listOf(KEY_PROFILE, KEY_FEATURES, KEY_COMMERCIAL).forEach(settings::remove) }
    }

    private suspend fun <T> read(key: String, serializer: KSerializer<T>): T? {
        val raw = mutex.withLock { settings.getStringOrNull(key) } ?: return null
        return try {
            json.decodeFromString(serializer, raw)
        } catch (ignored: Exception) {
            // Битый снимок — просто нет кэша: данные придут с сервера.
            null
        }
    }

    private suspend fun <T> write(key: String, serializer: KSerializer<T>, value: T) {
        val raw = json.encodeToString(serializer, value)
        mutex.withLock { settings.putString(key, raw) }
    }

    private companion object {
        const val KEY_PROFILE = "profile"
        const val KEY_FEATURES = "app_features"
        const val KEY_COMMERCIAL = "commercial"
    }
}

@Serializable
@SerialName("main.CachedProfile")
private data class CachedProfile(val name: String, val email: String)

@Serializable
@SerialName("main.CachedAppFeatures")
private data class CachedAppFeatures(val hasOrdersRequest: Boolean = false, val hasProductSearch: Boolean = false)

@Serializable
@SerialName("main.CachedCommercial")
private data class CachedCommercial(val warehouse1CUrl: String? = null)
