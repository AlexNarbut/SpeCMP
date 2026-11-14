package ru.alarmtrade.specmp.feature.mountassistant.domain.model

/**
 * [capabilities] `null` — модели нет в базе; [can] `null` — CAN у устройства нет; [productSearch] — пользователю
 * доступна «Информация о товаре» (функция аккаунта, не блока).
 */
data class DeviceFacts(
    val systemId: Long,
    val title: String,
    val firmwareVersion: String,
    val transport: AssistantTransport,
    val capabilities: Set<AssistantCapability>?,
    val can: CanFacts?,
    val online: Boolean,
    val productSearch: Boolean = false,
) {
    val inDatabase: Boolean get() = capabilities != null

    val factoryCan: Boolean get() = can?.factory == true

    fun has(capability: AssistantCapability): Boolean = capabilities?.contains(capability) == true
}

enum class AssistantTransport { BLE, USB, SERIAL }

enum class AssistantCapability { FIRMWARE, SETTINGS, BYPASS, CLONE, DYNAMIC_DEVICES }

data class CanFacts(val factory: Boolean, val code: String?, val version: String?)
