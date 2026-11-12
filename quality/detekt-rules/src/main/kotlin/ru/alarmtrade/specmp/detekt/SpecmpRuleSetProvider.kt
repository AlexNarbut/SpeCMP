package ru.alarmtrade.specmp.detekt

import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class SpecmpRuleSetProvider : RuleSetProvider {
    override val ruleSetId = RuleSetId(RULE_SET_ID)

    override fun instance() = RuleSet(
        ruleSetId,
        mapOf(
            RuleName("ForbiddenRunCatching") to ::ForbiddenRunCatching,
            RuleName("ForbiddenPrintln") to ::ForbiddenPrintln,
            RuleName("StateFlowValueMutation") to ::StateFlowValueMutation,
            RuleName("ViewModelScopeLaunch") to ::ViewModelScopeLaunch,
            RuleName("ViewModelInitBlock") to ::ViewModelInitBlock,
            RuleName("ViewModelFileContents") to ::ViewModelFileContents,
            RuleName("ViewModelContractNames") to ::ViewModelContractNames,
            RuleName("ViewModelPublicApi") to ::ViewModelPublicApi,
            RuleName("ViewModelMutableProperty") to ::ViewModelMutableProperty,
            RuleName("ReduceOutsideIntent") to ::ReduceOutsideIntent,
            RuleName("RepositoryWithContext") to ::RepositoryWithContext,
            RuleName("RedundantSafeRun") to ::RedundantSafeRun,
            RuleName("SettingsInstantiation") to ::SettingsInstantiation,
            RuleName("SafeRunInViewModel") to ::SafeRunInViewModel,
            RuleName("RawFlowFromRepository") to ::RawFlowFromRepository,
            RuleName("ResponseGetOrThrow") to ::ResponseGetOrThrow,
            RuleName("ResponseIgnored") to ::ResponseIgnored,
            RuleName("DirectClockUsage") to ::DirectClockUsage,
            RuleName("ThrowInRepository") to ::ThrowInRepository,
            RuleName("UpdateLambdaSideEffects") to ::UpdateLambdaSideEffects,
            RuleName("CatchSwallowsCancellation") to ::CatchSwallowsCancellation,
            RuleName("MutexIsLockedCheck") to ::MutexIsLockedCheck,
            RuleName("NestedSameMutexLock") to ::NestedSameMutexLock,
            RuleName("SuspendInsideSynchronized") to ::SuspendInsideSynchronized,
            RuleName("SharedMutableStateInSingleton") to ::SharedMutableStateInSingleton,
            RuleName("UnmanagedCoroutineScope") to ::UnmanagedCoroutineScope,
            RuleName("SharingInsideFunction") to ::SharingInsideFunction,
            RuleName("UnawaitedAsync") to ::UnawaitedAsync,
            RuleName("NestedCollect") to ::NestedCollect,
            RuleName("SideEffectAwaitsUi") to ::SideEffectAwaitsUi,
            RuleName("HardcodedUiText") to ::HardcodedUiText,
            RuleName("DomainSentinelDefault") to ::DomainSentinelDefault,
            RuleName("LoopWithoutCancellation") to ::LoopWithoutCancellation,
            RuleName("LoggerTagLiteral") to ::LoggerTagLiteral,
        ),
    )

    companion object {
        const val RULE_SET_ID = "specmp"
    }
}
