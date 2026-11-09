package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SettingsInstantiationTest {
    private val rule = SettingsInstantiation(Config.empty)

    @Test
    fun `reports the shared no-arg Settings and platform settings constructors`() {
        val code = """
            class Store(private val settings: Settings = Settings())

            val module = module {
                single { Store(Settings()) }
                single { SharedPreferencesSettings(prefs) }
                single { NSUserDefaultsSettings(defaults) }
                single { PreferencesSettings(node) }
            }
        """.trimIndent()

        assertEquals(5, rule.lint(code).size)
    }

    @Test
    fun `allows owner files from the factory, in-memory settings and other Settings calls`() {
        val code = """
            val module = module {
                single<Settings>(named(PREFS)) { get<KeyValueStoreFactory>().create(PREFS) }
                single { Store(MapSettings()) }
                single { AppSettings(language = "ru") }
                single { Settings(delegate) }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
