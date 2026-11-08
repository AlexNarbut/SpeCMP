package ru.alarmtrade.specmp.detekt

import org.jetbrains.kotlin.psi.KtClassOrObject

// Миграция ViewModel (этап 2): прежние базы и их прямые наследники правила ViewModel пропускают; снимается в 2n.
private val LEGACY_VIEW_MODEL_BASES = setOf("LegacyBaseViewModel", "LegacyBaseDeviceViewModel")

internal fun KtClassOrObject.isLegacyViewModel(): Boolean = name in LEGACY_VIEW_MODEL_BASES ||
    superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.') in LEGACY_VIEW_MODEL_BASES
    }
