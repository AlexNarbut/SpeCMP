package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SettingsInstantiationBypassTest {
    private val rule = SettingsInstantiation(Config.empty)

    @Test
    fun `bypass via import alias`() {
        val code = """
            import com.russhwolf.settings.Settings as Prefs
            import com.russhwolf.settings.SharedPreferencesSettings as AndroidPrefs

            val module = module {
                single { Store(Prefs()) }
                single { Store(AndroidPrefs(prefs)) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via fully qualified name`() {
        val code = """
            val module = module {
                single { Store(com.russhwolf.settings.Settings()) }
                single { Store(com.russhwolf.settings.NSUserDefaultsSettings(defaults)) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via platform settings factory`() {
        val code = """
            val module = module {
                single { SharedPreferencesSettings.Factory(context).create("owner") }
                single { NSUserDefaultsSettings.Factory().create("owner") }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via constructor reference`() {
        val code = """
            val module = module {
                single(::PreferencesSettings)
                factory { listOf(node).map(::SharedPreferencesSettings) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via default parameter and lazy property`() {
        val code = """
            class Store(private val settings: Settings = run { Settings() }) {
                private val backup by lazy { PreferencesSettings(node) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report other factories and references`() {
        val code = """
            val module = module {
                single { get<AppSettingsFactory>().create(OWNER) }
                single { Store(MapSettings()) }
                factory(::Store)
                single { Settings.Factory }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
