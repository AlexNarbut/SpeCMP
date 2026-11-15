package ru.alarmtrade.specmp.konsist

import org.junit.jupiter.api.Test

/**
 * Правила раскладки кода из docs/architecture.md и docs/patterns.md (описание — docs/code-quality.md, «Konsist»);
 * логика правил — *Rules.kt, обходы — *BypassTest.kt.
 *
 * `testName` правила — его id: точечное исключение в коде — `@Suppress("konsist.<id>")` (для файла —
 * `@file:Suppress`) с причиной комментарием строкой выше.
 */
class ArchitectureTest {
    private val arch = ArchScope.production

    @Test
    fun `core depends only on core, features do not depend on the app`() = arch.moduleDirection()

    @Test
    fun `package starts with the module base package`() = arch.modulePackage()

    @Test
    fun `domain does not depend on data, presentation, di, Compose or the SDK`() = arch.domainIsolation()

    @Test
    fun `presentation does not depend on data`() = arch.presentationIsolation()

    @Test
    fun `data does not depend on presentation or di`() = arch.dataIsolation()

    @Test
    fun `features do not import data of other features`() = arch.featureDataIsolation()

    @Test
    fun `roles live in their layer packages`() = arch.layerPlacement()

    @Test
    fun `feature repository interfaces live in domain`() = arch.repositoryInDomain()

    @Test
    fun `repository members return Response`() = arch.repositoryContract()

    @Test
    fun `repository and service members have callers`() = arch.contractMemberUsage()

    @Test
    fun `service members return Response`() = arch.serviceContract()

    @Test
    fun `data declarations are not public`() = arch.dataVisibility()

    @Test
    fun `implementations are not public`() = arch.implVisibility()

    @Test
    fun `repository implementation implements its repository`() = arch.repositoryImpl()

    @Test
    fun `repository implementation is covered by a test of its module`() = arch.repositoryImplTested()

    @Test
    fun `core common and lce helpers have callers`() = arch.coreHelperUsage()

    @Test
    fun `implementations are constructed only in Koin modules`() = arch.koinWiring()

    @Test
    fun `Koin modules are declared in di and named Module`() = arch.koinModule()

    @Test
    fun `domain models are immutable and not serializable`() = arch.domainModelPurity()

    @Test
    fun `domain model classes carry no UI text`() = arch.domainModelUiText()

    @Test
    fun `Serializable is used only for transport, storage formats and routes`() = arch.serializablePlacement()

    @Test
    fun `composables of feature modules live in presentation`() = arch.composableInPresentation()

    @Test
    fun `presentation state classes are immutable`() = arch.stateImmutable()

    @Test
    fun `screen state and effects carry typed errors`() = arch.stateTypedErrors()

    @Test
    fun `AlarmTrade SDK types stay in data, di and SDK-facing core modules`() = arch.sdkConfinement()

    @Test
    fun `fakes and stubs live in tests`() = arch.noFakesInProduction()

    @Test
    fun `testing modules are test-only dependencies`() = arch.testingModulesTestOnly()

    @Test
    fun `type names use agreed role suffixes`() = arch.forbiddenSuffix()

    @Test
    fun `mappers live in data mapper and are not public`() = arch.mapperPlacement()

    @Test
    fun `feature storage access lives in data and di`() = arch.storageUsage()

    @Test
    fun `use cases live in domain usecase`() = arch.useCasePackage()

    @Test
    fun `features do not use presentation of other features`() = arch.crossFeaturePresentation()

    @Test
    fun `use cases orchestrate dependencies and return Response`() = arch.useCaseContract()

    @Test
    fun `platform capabilities are used where the call starts`() = arch.capabilityUsage()

    @Test
    fun `app foreground is gated where the call starts`() = arch.foregroundUsage()

    @Test
    fun `top-level properties do not read properties declared later in the file`() = arch.topLevelInitOrder()

    @Test
    fun `data sources are consumed only by repository implementations`() = arch.dataSourceConsumers()

    @Test
    fun `view models extend BaseViewModel, are named ViewModel and live in presentation`() = arch.viewModel()

    @Test
    fun `view models own no flows - state only through reduce, effects through postSideEffect`() =
        arch.viewModelOwnsNoFlows()

    @Test
    fun `view models depend only on domain contracts, presentation helpers and approved infrastructure`() =
        arch.viewModelDependencies()

    @Test
    fun `view model constructors take at most 12 parameters`() = arch.viewModelConstructor()

    @Test
    fun `service locator is used only in di, composition roots and platform entry points`() = arch.noServiceLocator()

    @Test
    fun `composables get dependencies from parameters, view models or composition locals`() = arch.composableInjection()

    @Test
    fun `composables do not receive view models`() = arch.composableNoViewModel()

    @Test
    fun `classes have no constructor side effects`() = arch.noConstructorSideEffects()

    @Test
    fun `start-once services guard start with CAS`() = arch.startOnceCas()
}
