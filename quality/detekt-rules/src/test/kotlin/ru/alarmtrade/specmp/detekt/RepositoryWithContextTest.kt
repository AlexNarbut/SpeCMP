package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RepositoryWithContextTest {
    private val rule = RepositoryWithContext(Config.empty)

    @Test
    fun `reports withContext in repositories and data sources, including nested code`() {
        val code = """
            internal class SleepRepositoryImpl(private val dispatchers: AppDispatchers) : SleepRepository {
                override suspend fun save(): Response<Unit> = safeRun { withContext(dispatchers.io) { write() } }
                private val reader = object : Reader {
                    override suspend fun read() = withContext(dispatchers.io) { 1 }
                }
            }
            class EntitlementRepository(private val dispatchers: AppDispatchers) {
                suspend fun refresh() = withContext(dispatchers.io) { }
            }
            internal class SleepRemoteDataSource(private val dispatchers: AppDispatchers) {
                suspend fun load() = withContext(dispatchers.io) { }
            }
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `does not report view models, use cases and top-level code`() {
        val code = """
            class SleepViewModel : BaseViewModel<SleepState, Nothing>() {
                suspend fun save() = withContext(dispatchers.io) { repository.save() }
            }
            class BuildSnapshotUseCase {
                suspend operator fun invoke() = withContext(dispatchers.default) { }
            }
            suspend fun entryPoint() = withContext(dispatchers.io) { }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
