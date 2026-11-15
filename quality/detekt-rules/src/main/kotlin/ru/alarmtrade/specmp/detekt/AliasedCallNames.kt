package ru.alarmtrade.specmp.detekt

import org.jetbrains.kotlin.psi.KtFile

/** Имена [names] вместе с их алиасами из импортов файла (`import kotlin.error as fail` → `fail`). */
internal fun KtFile.namesWithAliases(names: Set<String>): Set<String> = names + importDirectives
    .filter { it.importedFqName?.shortName()?.asString() in names }
    .mapNotNull { it.aliasName }
