package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UnmanagedCoroutineScopeTest {
    private val rule = UnmanagedCoroutineScope(Config.empty)

    @Test
    fun `reports scopes outside DI without cancel and incomplete contexts`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.data.service

            class Controller(dispatchers: AppDispatchers) {
                private val scope = CoroutineScope(SupervisorJob() + dispatchers.default + handler)
            }

            class Receiver {
                fun onReceive() {
                    CoroutineScope(dispatchers.default).launch { }
                }
            }

            class Managed {
                private val scope = CoroutineScope(dispatchers.default)

                fun stop() = scope.cancel()
            }
        """.trimIndent()

        val findings = rule.lint(code)
        assertEquals(3, findings.size)
    }

    @Test
    fun `reports incomplete contexts in DI`() {
        val code = """
            package ru.alarmtrade.specmp.core.auth.di

            val authModule = module {
                single { Session(scope = CoroutineScope(SupervisorJob() + get<AppDispatchers>().default)) }
                single { Session(scope = CoroutineScope(Job() + exceptionHandler)) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report DI scopes and cancelled scopes with full context`() {
        val code = """
            package ru.alarmtrade.specmp.core.common.di

            fun serviceScope(dispatcher: CoroutineDispatcher): CoroutineScope =
                CoroutineScope(SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, _ -> })
        """.trimIndent()
        val managed = """
            package ru.alarmtrade.specmp.core.notifications.receiver

            class Receiver {
                fun onReceive() {
                    val scope = CoroutineScope(SupervisorJob() + dispatchers.default + exceptionHandler)
                    scope.launch {
                        try { work() } finally { scope.cancel() }
                    }
                }
            }

            class Service {
                private val serviceScope = CoroutineScope(SupervisorJob() + dispatchers.default + handler)

                fun onDestroy() = this.serviceScope.cancel()
            }

            @Composable
            fun Screen() {
                val scope = rememberCoroutineScope()
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
        assertEquals(0, rule.lint(managed).size)
    }
}
