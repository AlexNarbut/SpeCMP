package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceInComposableBypassTest {
    private val rule = ServiceInComposable(Config.empty)

    @Test
    fun `bypass via explicit property type instead of a type argument`() {
        val code = """
            @Composable
            fun Screen() {
                val repository: RecipeRepository = koinInject()
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via aliased koinInject and fully qualified nullable type`() {
        val code = """
            import org.koin.compose.koinInject as inject

            @Composable
            fun Screen() {
                val session = inject<ru.alarmtrade.specmp.feature.device.DeviceSession?>()
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via lambda nested inside a composable`() {
        val code = """
            @Composable
            fun Screen() {
                CompositionLocalProvider(LocalX provides 1) {
                    val starter = koinInject<AppServicesStarter>()
                    DisposableEffect(starter) { onDispose { } }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via qualified service key and produceState`() {
        val code = """
            @Composable
            fun Screen(holder: Holder) {
                val value = produceState(0, holder.chatRepository) { }
                LaunchedEffect(holder.store) { }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via aliased effect`() {
        val code = """
            import androidx.compose.runtime.LaunchedEffect as OnStart

            @Composable
            fun Screen(permissionsProvider: PermissionsProvider) {
                OnStart(permissionsProvider) { }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }
}
