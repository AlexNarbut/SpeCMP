package ru.alarmtrade.specmp.feature.appupdate.domain.service

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateInstallResult
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePackage
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform

/**
 * Платформа: desktop ставит установщик сам ([AppUpdatePlatform.installsItself]), Android и iOS — через магазин.
 * Реализации — в composition root.
 */
interface AppUpdateInstaller {
    // Константы платформы, не результат операции.
    @Suppress("konsist.service-contract")
    val platform: AppUpdatePlatform

    @Suppress("konsist.service-contract")
    val storeUrls: List<String>

    /** Установщик подходит этой ОС (Windows — `.exe`/`.msi`, macOS — `.dmg`). */
    fun accepts(fileName: String): Boolean

    /** Пустой файл под загрузку; прежние загрузки удаляются. */
    suspend fun prepare(fileName: String): Response<AppUpdatePackage>

    suspend fun append(target: AppUpdatePackage, chunk: ByteArray): Response<Unit>

    /** Запускает установку и завершает приложение; возвращается только при неудаче. */
    suspend fun install(target: AppUpdatePackage): Response<AppUpdateInstallResult>

    /** Копия установщика в «Загрузки» и открытая папка. */
    suspend fun export(target: AppUpdatePackage): Response<Unit>
}
