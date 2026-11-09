package ru.alarmtrade.specmp.detekt

import dev.detekt.api.RuleSetProvider
import java.io.File
import java.util.ServiceLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SpecmpRuleSetProviderTest {
    @Test
    fun `provider is discoverable and exposes all rules`() {
        val provider = ServiceLoader.load(RuleSetProvider::class.java)
            .single { it is SpecmpRuleSetProvider }
        val ruleSet = provider.instance()

        assertEquals("specmp", ruleSet.id.value)
        assertEquals(
            setOf(
                "ForbiddenRunCatching",
                "ForbiddenPrintln",
                "StateFlowValueMutation",
                "ViewModelScopeLaunch",
                "ViewModelInitBlock",
                "ViewModelFileContents",
                "ViewModelContractNames",
                "ViewModelPublicApi",
                "ViewModelMutableProperty",
                "RepositoryWithContext",
                "RedundantSafeRun",
                "SettingsInstantiation",
                "SafeRunInViewModel",
                "RawFlowFromRepository",
                "ResponseGetOrThrow",
                "ResponseIgnored",
                "DirectClockUsage",
                "ThrowInRepository",
                "UpdateLambdaSideEffects",
                "CatchSwallowsCancellation",
                "MutexIsLockedCheck",
                "NestedSameMutexLock",
                "SuspendInsideSynchronized",
                "SharedMutableStateInSingleton",
                "UnmanagedCoroutineScope",
                "SharingInsideFunction",
                "UnawaitedAsync",
                "NestedCollect",
            ),
            ruleSet.rules.keys.map { it.value }.toSet(),
        )
    }

    @Test
    fun `every rule is configured in detekt yml`() {
        val config = File(System.getProperty("specmp.detektConfig")).readLines()
        val start = config.indexOf("specmp:")
        val section = config.drop(start + 1).takeWhile { it.isBlank() || it.startsWith(" ") || it.startsWith("#") }
        val configured = section.mapNotNull { Regex("""^ {2}(\w+):$""").find(it)?.groupValues?.get(1) }.toSet()
        val rules = SpecmpRuleSetProvider().instance().rules.keys.map { it.value }.toSet()

        assertEquals(rules, configured)
    }
}
