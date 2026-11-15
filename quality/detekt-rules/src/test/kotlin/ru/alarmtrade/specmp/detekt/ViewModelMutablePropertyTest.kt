package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelMutablePropertyTest {
    private val rule = ViewModelMutableProperty(Config.empty)

    @Test
    fun `reports var, lateinit, delegated var and constructor var in a ViewModel`() {
        val code = """
            class FooViewModel(var mode: Int) : BaseViewModel<FooState, Nothing>(FooState()) {
                private var counter = 0
                private lateinit var name: String
                var delegated: Int by Delegates.notNull()
            }
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `reports var in nested classes and objects of a ViewModel`() {
        val code = """
            class AppShell : BaseViewModel<ShellState, Nothing>(ShellState()) {
                private class Form(var text: String) { var touched = false }
                private companion object { var cache = 0 }
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `reports val with a mutable collection by initializer or explicit type`() {
        val code = """
            class FooViewModel(private val ids: MutableList<String>) : BaseViewModel<FooState, Nothing>(FooState()) {
                private val seen = mutableSetOf<String>()
                private val cache = kotlin.collections.mutableMapOf<String, Int>()
                private val buffer = ArrayList<Int>()
                private val index: HashMap<String, Int> = build()
                private val pending: MutableCollection<Int>? = null
            }
        """.trimIndent()

        assertEquals(6, rule.lint(code).size)
    }

    @Test
    fun `does not report immutable collections, locals or mutable collections of regular classes`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val ids = setOf("a")
                private val names: List<String> = listOf()
                private val flow = MutableStateFlow(0)
                fun onClick() {
                    val local = mutableListOf<Int>()
                }
            }

            class Holder(val items: MutableList<Int>) {
                val other = mutableMapOf<String, Int>()
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not report val, local var or vars of regular classes`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private val fine = 1
                fun onClick() {
                    var attempts = 0
                    attempts++
                }
            }

            class Holder(var value: Int) {
                var other = 0
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `bypass via scope function around a mutable factory`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val seen = mutableSetOf<String>().also { it += "a" }
                private val cache = hashMapOf<String, Int>().apply { put("a", 1) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via fully qualified nullable mutable type`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val cache: kotlin.collections.MutableMap<String, Int>? = null
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via arrays and snapshot collections`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val ids = IntArray(3)
                private val names: Array<String> = emptyArray()
                private val rows = mutableStateListOf<Int>()
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `bypass via companion object var`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private companion object {
                    var attempts = 0
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via intermediate base without ViewModel suffix`() {
        val code = """
            abstract class ScreenModel<S : Any>(initial: S) : BaseViewModel<S, Nothing>(initial)

            class FooViewModel : ScreenModel<FooState>(FooState()) {
                private var job: Job? = null
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report read-only results of mutable builders`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val ids = mutableListOf(1, 2).toList()
                private val names = buildList { add("a") }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
