package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NestedCollectTest {
    private val rule = NestedCollect(Config.empty)

    @Test
    fun `reports collect inside collect lambdas`() {
        val code = """
            suspend fun a(first: Flow<Int>, second: Flow<Int>, scope: CoroutineScope) {
                first.collect { second.collect { } }
                first.collectLatest { value -> second.collectLatest { } }
                first.collect {
                    scope.launch { second.collect { } }
                }
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `does not report sequential collects and combined flows`() {
        val code = """
            suspend fun a(first: Flow<Int>, second: Flow<Int>) {
                first.collect { }
                second.collect()
                first.flatMapLatest { second }.collect { }
                first.collect {
                    fun local(flow: Flow<Int>) = Unit
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
