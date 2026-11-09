package ru.alarmtrade.specmp.feature.fastmount.domain.model

/** @property questions вопросы проверки системы — плоско, в порядке SDK (курсор идёт по ним). */
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
    val questions: List<MountQuestion>,
)

data class MountFile(val name: String, val url: String)

data class MountImage(val title: String, val description: String, val previewUrl: String?, val url: String)
