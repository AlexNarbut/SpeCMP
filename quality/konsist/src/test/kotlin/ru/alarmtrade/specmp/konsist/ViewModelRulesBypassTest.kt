package ru.alarmtrade.specmp.konsist

import org.junit.jupiter.api.Test

class ViewModelRulesBypassTest {

    @Test
    fun `clean view model passes view model rules`() {
        val arch = vm(
            """
            import $FOO.domain.repository.FooRepository

            class FooViewModel(private val repository: FooRepository, private val ids: List<Int>) :
                BaseDeviceViewModel<FooState, Nothing>(FooState()) {
                fun onSave() {
                    intent { save() }
                }

                private suspend fun save() = subIntent { reduce { copy(saved = true) } }
            }
            """,
        )
        arch.viewModel()
        arch.viewModelOwnsNoFlows()
        arch.viewModelDependencies()
        arch.viewModelConstructor()
    }

    @Test
    fun `view-model bypass via androidx ViewModel`() = assertViolation("view-model", "FooViewModel") {
        vm("class FooViewModel : androidx.lifecycle.ViewModel()").viewModel()
    }

    @Test
    fun `view-model bypass via name without ViewModel suffix`() = assertViolation("view-model", "FooScreenLogic") {
        vm("class FooScreenLogic : BaseViewModel<FooState, Nothing>(FooState())").viewModel()
    }

    @Test
    fun `view-model bypass via fully qualified base`() = assertViolation("view-model", "FooScreenLogic") {
        vm(
            "class FooScreenLogic :\n    $ROOT.core.ui.viewmodel.BaseViewModel<FooState, Nothing>(FooState())",
        ).viewModel()
    }

    @Test
    fun `view-model bypass via import alias of base`() = assertViolation("view-model", "FooScreenLogic") {
        vm(
            """
            import $ROOT.core.ui.viewmodel.BaseViewModel as Logic

            class FooScreenLogic : Logic<FooState, Nothing>(FooState())
            """,
        ).viewModel()
    }

    @Test
    fun `view-model bypass via device base in domain package`() = assertViolation("view-model", "FooViewModel") {
        vm(
            "class FooViewModel : BaseDeviceViewModel<FooState, Nothing>(FooState())",
            pkg = "$FOO.domain.service",
        ).viewModel()
    }

    @Test
    fun `view-model bypass via nested class in object`() = assertViolation("view-model", "FooViewModel") {
        vm(
            "object Holder {\n    class FooViewModel : BaseViewModel<FooState, Nothing>(FooState())\n}",
            pkg = "$FOO.domain",
        ).viewModel()
    }

    @Test
    fun `view-model-owns-no-flows bypass via nested class`() =
        assertViolation("view-model-owns-no-flows", "FooViewModel") {
            vm(
                """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private class Box {
                    val query = MutableStateFlow("")
                }
            }
            """,
            ).viewModelOwnsNoFlows()
        }

    @Test
    fun `view-model-owns-no-flows bypass via import alias`() =
        assertViolation("view-model-owns-no-flows", "FooViewModel") {
            vm(
                """
            import kotlinx.coroutines.flow.MutableSharedFlow as Events

            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val events = Events<Int>()
            }
            """,
            ).viewModelOwnsNoFlows()
        }

    @Test
    fun `view-model-owns-no-flows bypass via fully qualified name in companion`() =
        assertViolation("view-model-owns-no-flows", "FooViewModel") {
            vm(
                """
                class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                    private companion object {
                        val ticks = kotlinx.coroutines.channels.Channel<Int>(capacity = 1)
                    }
                }
                """,
            ).viewModelOwnsNoFlows()
        }

    @Test
    fun `view-model-owns-no-flows bypass via top-level property in view model file`() =
        assertViolation("view-model-owns-no-flows", "query") {
            vm(
                """
                private val query = MutableStateFlow("")

                class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                    fun onQuery(text: String) {
                        query.value = text
                    }
                }
                """,
            ).viewModelOwnsNoFlows()
        }

    @Test
    fun `view-model-owns-no-flows bypass via compose state with explicit type`() =
        assertViolation("view-model-owns-no-flows", "FooViewModel") {
            vm(
                """
                class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                    private val selected: MutableState<Int> = mutableIntStateOf(0)
                }
                """,
            ).viewModelOwnsNoFlows()
        }

    @Test
    fun `view-model-dependencies bypass via import alias of data type`() =
        assertViolation("view-model-dependencies", "repository") {
            vm(
                """
                import $FOO.data.repository.FooRepositoryImpl as Repo

                class FooViewModel(private val repository: Repo) : BaseViewModel<FooState, Nothing>(FooState())
                """,
            ).viewModelDependencies()
        }

    @Test
    fun `view-model-dependencies bypass via generic argument`() = assertViolation("view-model-dependencies", "dtos") {
        vm(
            """
            import $FOO.data.dto.FooDto

            class FooViewModel(private val dtos: List<FooDto>) : BaseViewModel<FooState, Nothing>(FooState())
            """,
        ).viewModelDependencies()
    }

    @Test
    fun `view-model-dependencies bypass via suspend function type`() =
        assertViolation("view-model-dependencies", "load") {
            vm(
                "class FooViewModel(private val load: suspend () -> Int) :\n" +
                    "    BaseViewModel<FooState, Nothing>(FooState())",
            ).viewModelDependencies()
        }

    @Test
    fun `view-model-dependencies bypass via typealias of data type`() =
        assertViolation("view-model-dependencies", "repository") {
            vm(
                """
                typealias Repo = $FOO.data.repository.FooRepositoryImpl

                class FooViewModel(private val repository: Repo) : BaseViewModel<FooState, Nothing>(FooState())
                """,
            ).viewModelDependencies()
        }

    @Test
    fun `view-model-dependencies bypass via star import of data package`() =
        assertViolation("view-model-dependencies", "api") {
            vm(
                """
                import $FOO.data.api.*

                class FooViewModel(private val api: FooApi) : BaseViewModel<FooState, Nothing>(FooState())
                """,
            ).viewModelDependencies()
        }

    @Test
    fun `view-model-dependencies bypass via secondary constructor`() =
        assertViolation("view-model-dependencies", "scope") {
            vm(
                """
                class FooViewModel(private val ids: List<Int>) : BaseViewModel<FooState, Nothing>(FooState()) {
                    constructor(scope: kotlinx.coroutines.CoroutineScope) : this(emptyList())
                }
                """,
            ).viewModelDependencies()
        }

    @Test
    fun `view-model-constructor bypass via device base`() = assertViolation("view-model-constructor", "FooViewModel") {
        vm(
            "class FooViewModel(${params(13)}) :\n    BaseDeviceViewModel<FooState, Nothing>(FooState())",
        ).viewModelConstructor()
    }

    @Test
    fun `view-model-constructor bypass via secondary constructor`() =
        assertViolation("view-model-constructor", "FooViewModel") {
            vm(
                """
                class FooViewModel(private val a: Int) : BaseViewModel<FooState, Nothing>(FooState()) {
                    constructor(${params(13)}) : this(p0)
                }
                """,
            ).viewModelConstructor()
        }

    @Test
    fun `view-model-constructor bypass via nested class`() = assertViolation("view-model-constructor", "FooViewModel") {
        vm(
            "object Screens {\n    class FooViewModel(${params(13)}) : BaseViewModel<FooState, Nothing>(FooState())\n}",
        ).viewModelConstructor()
    }

    @Test
    fun `view-model-constructor bypass via import alias of base`() =
        assertViolation("view-model-constructor", "FooScreenLogic") {
            vm(
                """
                import $ROOT.core.ui.viewmodel.BaseViewModel as Logic

                class FooScreenLogic(${params(13)}) : Logic<FooState, Nothing>(FooState())
                """,
            ).viewModelConstructor()
        }

    private fun params(count: Int) = (0 until count).joinToString { "p$it: Int" }

    private fun vm(code: String, pkg: String = "$FOO.presentation") = fixture(
        src(
            "core/ui",
            "BaseViewModel.kt",
            "package $ROOT.core.ui.viewmodel\n\nabstract class BaseViewModel<S : Any, E : Any>(initialState: S)",
        ),
        src(
            "feature/device/presentation-core",
            "BaseDeviceViewModel.kt",
            """
            package $ROOT.feature.device.presentation.core.viewmodel

            import $ROOT.core.ui.viewmodel.BaseViewModel

            abstract class BaseDeviceViewModel<S : Any, E : Any>(initialState: S) : BaseViewModel<S, E>(initialState)
            """,
        ),
        src(
            "feature/foo",
            "FooViewModel.kt",
            """
            |package $pkg
            |
            |import $ROOT.core.ui.viewmodel.BaseViewModel
            |import $ROOT.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
            |
            |${code.trimIndent()}
            """.trimMargin(),
        ),
    )
}
