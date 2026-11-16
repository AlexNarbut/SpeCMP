package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceInComposableTest {
    private val rule = ServiceInComposable(Config.empty)

    @Test
    fun `reports services taken from DI and used as effect keys in composables`() {
        val code = """
            @Composable
            fun ChatNotifications() {
                val repository = koinInject<SupportChatRepository>()
                val manager = koinInject<DownloadManager>()
                val load = koinInject<LoadRecipesUseCase>()
                LaunchedEffect(repository) { repository.session().collect { } }
            }
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `does not report view models, platform helpers and non-composable code`() {
        val code = """
            @Composable
            fun Screen() {
                val viewModel = koinViewModel<RecipesViewModel>()
                val dispatchers = koinInject<AppDispatchers>()
                val picker = koinInject<FirmwareArchivePicker>()
                LaunchedEffect(Unit) { }
                LaunchedEffect(picker) { }
            }

            class Wiring {
                fun build() = koinInject<RecipeRepository>()
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
