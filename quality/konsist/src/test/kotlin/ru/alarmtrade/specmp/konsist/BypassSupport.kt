package ru.alarmtrade.specmp.konsist

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue

internal const val FOO = "$ROOT.feature.foo"
internal const val BAR = "$ROOT.feature.bar"

/** Файл фикстуры: `src("feature/foo", "A.kt", "package …")` → `feature/foo/src/commonMain/kotlin/A.kt`. */
internal fun src(module: String, file: String, code: String, sourceSet: String = "commonMain"): Pair<String, String> =
    "$module/src/$sourceSet/kotlin/$file" to code

/** Правило [id] падает, и в сообщении есть каждая строка [evidence] (имя декларации, файла, импорта). */
internal fun assertViolation(id: String, vararg evidence: String, rule: () -> Unit) {
    val message = assertThrows(Throwable::class.java) { rule() }.message.orEmpty()
    assertTrue(id in message) { "ожидалось нарушение $id, получено:\n$message" }
    evidence.forEach { assertTrue(it in message) { "в нарушении $id нет «$it»:\n$message" } }
}
