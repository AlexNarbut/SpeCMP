package ru.alarmtrade.specmp.feature.fastmount.domain.model

/**
 * Облачная конфигурация для быстрого монтажа: что показать монтажнику и какие шаги будут.
 *
 * @property deviceName модель блока, для которой сделана конфигурация (`systemName`).
 * @property mountingMap карта монтажа (PDF); `null` — нет.
 * @property keyBypass в настройках включён бесключевой обход — будет шаг обхода.
 * @property slave в настройках включён Slave-режим — будет шаг обучения Slave (только Bluetooth).
 * @property questions вопросы проверки системы — плоско, в порядке SDK (курсор идёт по ним).
 */
data class FastMountConfiguration(
    val id: Int,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val deviceName: String,
    val author: String,
    val rating: Int,
    val mountingMap: MountFile?,
    val images: List<MountImage>,
    val keyBypass: Boolean,
    val slave: Boolean,
    val questions: List<MountQuestion>
)

data class MountFile(val name: String, val url: String)

/** Фото «Особенности монтажа». [previewUrl] — `null`, если превью нет. */
data class MountImage(val title: String, val description: String, val previewUrl: String?, val url: String)
