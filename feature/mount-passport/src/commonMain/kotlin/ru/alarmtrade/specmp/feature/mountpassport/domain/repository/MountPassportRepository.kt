package ru.alarmtrade.specmp.feature.mountpassport.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportEdit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportFastMountResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage

/** Паспорта на устройстве; поиск (нормализация госномера, VIN, телефона) — в SDK. */
interface MountPassportRepository {
    /** Все паспорта, новые сверху; эмитит заново при каждом изменении. */
    fun observeAll(): Flow<Response<List<MountPassportSummary>>>

    fun observeUnit(systemId: String): Flow<Response<List<MountPassportSummary>>>

    suspend fun search(query: String): Response<List<MountPassportSummary>>

    suspend fun storageUsage(): Response<PassportStorageUsage>

    /** Заготовка `BLANK` из итога быстрого монтажа без считывания блока (дооформить позже); id паспорта. */
    suspend fun createBlank(result: PassportFastMountResult): Response<String>

    /** Последний паспорт блока, созданный сегодня, или `null`. */
    suspend fun lastOfUnitToday(systemId: String): Response<MountPassportSummary?>

    suspend fun get(id: String): Response<MountPassport>

    suspend fun edit(id: String, edit: PassportEdit): Response<MountPassport>

    suspend fun delete(id: String): Response<Unit>

    /** Лимит фото — `PassportPhotoLimitException`, нет места — `PassportStorageFullException`. */
    suspend fun addPhoto(id: String, jpeg: ByteArray, thumbnail: ByteArray): Response<MountPassport>

    suspend fun deletePhoto(id: String, photoId: String): Response<MountPassport>

    suspend fun setPhotoCaption(id: String, photoId: String, caption: String?): Response<MountPassport>

    /** Абсолютный путь `settings.dxs` снимка настроек. */
    suspend fun settingsFile(id: String): Response<String>
}
