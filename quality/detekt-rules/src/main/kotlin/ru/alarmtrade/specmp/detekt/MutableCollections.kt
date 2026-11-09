package ru.alarmtrade.specmp.detekt

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtTypeReference

/** Изменяемая коллекция по имени типа или фабрики — общее для ViewModelMutableProperty и SharedMutableStateInSingleton. */
internal fun KtTypeReference?.isMutableCollection(): Boolean =
    this?.text?.substringBefore('<')?.removeSuffix("?")?.substringAfterLast('.') in MUTABLE_TYPES

internal fun KtExpression?.isMutableCollectionCall(): Boolean = callName() in MUTABLE_FACTORIES

internal fun KtExpression?.callName(): String? {
    val call = when (this) {
        is KtCallExpression -> this
        is KtQualifiedExpression -> selectorExpression as? KtCallExpression
        else -> null
    }
    return call?.calleeExpression?.text
}

private val MUTABLE_TYPES = setOf(
    "MutableList", "MutableSet", "MutableMap", "MutableCollection",
    "ArrayList", "HashMap", "HashSet", "LinkedHashMap", "LinkedHashSet",
)

private val MUTABLE_FACTORIES = setOf(
    "mutableListOf", "mutableSetOf", "mutableMapOf", "arrayListOf", "hashMapOf", "hashSetOf",
    "linkedMapOf", "linkedSetOf", "ArrayList", "HashMap", "HashSet", "LinkedHashMap", "LinkedHashSet",
)
