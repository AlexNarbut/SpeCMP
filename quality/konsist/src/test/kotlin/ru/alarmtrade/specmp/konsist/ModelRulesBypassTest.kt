package ru.alarmtrade.specmp.konsist

import org.junit.jupiter.api.Test

class ModelRulesBypassTest {

    private fun model(code: String, file: String = "Model.kt", sourceSet: String = "commonMain") =
        fixture(src("feature/foo", file, "package $FOO.domain.model\n\n$code", sourceSet))

    private fun presentation(code: String, file: String = "FooScreen.kt", sourceSet: String = "commonMain") =
        fixture(src("feature/foo", file, "package $FOO.presentation\n\n$code", sourceSet))

    @Test
    fun `clean fixture passes model rules`() {
        val arch = fixture(
            src(
                "feature/foo",
                "Foo.kt",
                "package $FOO.domain.model\n\ndata class Foo(val id: Int, val items: List<String>, val title: String)",
            ),
            src(
                "feature/foo",
                "FooState.kt",
                """
                package $FOO.presentation

                import androidx.compose.runtime.Immutable
                import $FOO.domain.model.Foo

                @Immutable
                data class FooState(val foo: Foo? = null, val error: FooError? = null)

                enum class FooError { Network }
                """,
            ),
            src(
                "feature/foo",
                "FooRoute.kt",
                """
                package $FOO.presentation.navigation

                import androidx.navigation3.runtime.NavKey
                import kotlinx.serialization.Serializable

                @Serializable
                @SerialName("foo.FooRoute")
                data class FooRoute(val mode: FooMode) : NavKey

                @Serializable
                @SerialName("foo.FooMode")
                enum class FooMode { A }
                """,
            ),
            src(
                "feature/foo",
                "FooScreen.kt",
                """
                package $FOO.presentation

                @Composable
                fun FooScreen(state: FooState, onBack: () -> Unit) = Unit
                """,
            ),
        )
        arch.domainModelPurity()
        arch.domainModelUiText()
        arch.serializablePlacement()
        arch.stateImmutable()
        arch.stateTypedErrors()
        arch.noFakesInProduction()
        arch.forbiddenSuffix()
        arch.composableInPresentation()
        arch.composableNoViewModel()
    }

    @Test
    fun `domain-model-purity bypass via var in nested class`() = assertViolation("domain-model-purity", "count") {
        model(
            "data class Foo(val id: Int) {\n    class Counter {\n        var count: Int = 0\n    }\n}",
        ).domainModelPurity()
    }

    @Test
    fun `domain-model-purity bypass via fully qualified mutable collection`() =
        assertViolation("domain-model-purity", "items") {
            model("class Foo {\n    val items: kotlin.collections.MutableList<Int> = mutableListOf()\n}")
                .domainModelPurity()
        }

    @Test
    fun `domain-model-purity bypass via var constructor parameter`() = assertViolation("domain-model-purity", "id") {
        model("data class Foo(var id: Int)").domainModelPurity()
    }

    @Test
    fun `domain-model-purity bypass via import alias of mutable collection`() =
        assertViolation("domain-model-purity", "ids") {
            model("import kotlin.collections.MutableList as Items\n\ndata class Foo(val ids: Items<Int>)")
                .domainModelPurity()
        }

    @Test
    fun `domain-model-purity bypass via typealias to mutable collection`() =
        assertViolation("domain-model-purity", "Ids") {
            model("typealias Ids = ArrayList<Int>\n\ndata class Foo(val ids: Ids)").domainModelPurity()
        }

    @Test
    fun `domain-model-purity bypass via fully qualified Serializable on object`() =
        assertViolation("domain-model-purity", "Empty") {
            model("@kotlinx.serialization.Serializable\nobject Empty").domainModelPurity()
        }

    @Test
    fun `domain-model-ui-text bypass via nested class property`() =
        assertViolation("domain-model-ui-text", "formattedPrice") {
            model(
                "data class Foo(val id: Int) {\n    data class Price(val formattedPrice: String)\n}",
            ).domainModelUiText()
        }

    @Test
    fun `domain-model-ui-text bypass via fully qualified String`() =
        assertViolation("domain-model-ui-text", "labelText") {
            model("data class Foo(val labelText: kotlin.String)").domainModelUiText()
        }

    @Test
    fun `domain-model-ui-text bypass via sealed interface property`() =
        assertViolation("domain-model-ui-text", "displayName") {
            model("sealed interface Foo {\n    val displayName: String\n}").domainModelUiText()
        }

    @Test
    fun `domain-model-ui-text bypass via object property in jvmMain`() =
        assertViolation("domain-model-ui-text", "titleText") {
            model("object Defaults {\n    val titleText: String? = null\n}", sourceSet = "jvmMain").domainModelUiText()
        }

    @Test
    fun `serializable-placement bypass via data segment under presentation`() =
        assertViolation("serializable-placement", "FooUi") {
            fixture(
                src(
                    "feature/foo",
                    "FooUi.kt",
                    "package $FOO.presentation.data\n\n@kotlinx.serialization.Serializable\ndata class FooUi(val id: Int)",
                ),
            ).serializablePlacement()
        }

    @Test
    fun `serializable-placement bypass via domain model pretending to be a route`() =
        assertViolation("serializable-placement", "Foo") {
            model("import kotlinx.serialization.Serializable\n\n@Serializable\ndata class Foo(val id: Int) : NavKey")
                .serializablePlacement()
        }

    @Test
    fun `serializable-placement bypass via name clash with route argument type`() =
        assertViolation("serializable-placement", "Mode.kt") {
            fixture(
                src(
                    "feature/foo",
                    "FooRoute.kt",
                    """
                    package $FOO.presentation.navigation

                    @Serializable
                    @SerialName("foo.FooRoute")
                    data class FooRoute(val mode: Mode) : NavKey

                    @Serializable
                    @SerialName("foo.Mode")
                    enum class Mode { A }
                    """,
                ),
                src(
                    "feature/foo",
                    "Mode.kt",
                    "package $FOO.domain.model\n\n@Serializable\ndata class Mode(val id: Int)",
                ),
            ).serializablePlacement()
        }

    @Test
    fun `serializable-placement bypass via import alias of annotation`() =
        assertViolation("serializable-placement", "Foo") {
            model("import kotlinx.serialization.Serializable as Wire\n\n@Wire\ndata class Foo(val id: Int)")
                .serializablePlacement()
        }

    @Test
    fun `state-immutable bypass via plain class`() = assertViolation("state-immutable", "FooState") {
        presentation("class FooState(val items: List<Int>)").stateImmutable()
    }

    @Test
    fun `state-immutable bypass via own annotation named Immutable`() = assertViolation("state-immutable", "FooState") {
        presentation("annotation class Immutable\n\n@Immutable\ndata class FooState(val items: List<Int>)")
            .stateImmutable()
    }

    @Test
    fun `state-immutable bypass via nested state in object`() = assertViolation("state-immutable", "PageState") {
        presentation("object Screens {\n    data class PageState(val page: Int)\n}").stateImmutable()
    }

    @Test
    fun `state-immutable bypass via other source set and sub package`() =
        assertViolation("state-immutable", "RowState") {
            fixture(
                src(
                    "feature/foo",
                    "Row.kt",
                    "package $FOO.presentation.screen.row\n\ndata class RowState(val selected: Boolean)",
                    sourceSet = "androidMain",
                ),
            ).stateImmutable()
        }

    @Test
    fun `state-typed-errors bypass via state class outside contract file`() =
        assertViolation("state-typed-errors", "error") {
            presentation("data class FooState(val error: Throwable? = null)", file = "FooScreen.kt").stateTypedErrors()
        }

    @Test
    fun `state-typed-errors bypass via import alias of exception`() = assertViolation("state-typed-errors", "problem") {
        presentation(
            "import java.io.IOException as Problem\n\ndata class FooState(val problem: Problem? = null)",
            file = "FooState.kt",
        ).stateTypedErrors()
    }

    @Test
    fun `state-typed-errors bypass via kotlin Result`() = assertViolation("state-typed-errors", "result") {
        presentation("data class FooState(val result: Result<Int>? = null)", file = "FooState.kt").stateTypedErrors()
    }

    @Test
    fun `state-typed-errors bypass via nested effect with generic exception`() =
        assertViolation("state-typed-errors", "errors") {
            presentation(
                "sealed interface FooEffect {\n    data class Failed(val errors: List<kotlin.Exception>) : FooEffect\n}",
                file = "FooEffect.kt",
            ).stateTypedErrors()
        }

    @Test
    fun `no-fakes-in-production bypass via nested object`() = assertViolation("no-fakes-in-production", "FakeClock") {
        presentation("object Clocks {\n    object FakeClock\n}").noFakesInProduction()
    }

    @Test
    fun `no-fakes-in-production bypass via Fake suffix`() =
        assertViolation("no-fakes-in-production", "RepositoryFake") {
            fixture(src("feature/foo", "R.kt", "package $FOO.data\n\nclass RepositoryFake")).noFakesInProduction()
        }

    @Test
    fun `no-fakes-in-production bypass via factory of anonymous object`() =
        assertViolation("no-fakes-in-production", "fakeRepository") {
            fixture(
                src("feature/foo", "R.kt", "package $FOO.di\n\nfun fakeRepository(): Repo = object : Repo {}"),
            ).noFakesInProduction()
        }

    @Test
    fun `no-fakes-in-production bypass via stub property in iosMain`() =
        assertViolation("no-fakes-in-production", "stubSession") {
            fixture(
                src("feature/foo", "S.kt", "package $FOO.data\n\nval stubSession = object : Session {}", "iosMain"),
            ).noFakesInProduction()
        }

    @Test
    fun `forbidden-suffix bypass via Impl after the suffix`() =
        assertViolation("forbidden-suffix", "FormatHelperImpl") {
            presentation("class FormatHelperImpl").forbiddenSuffix()
        }

    @Test
    fun `forbidden-suffix bypass via utils file of top-level functions`() =
        assertViolation("forbidden-suffix", "DateUtils") {
            presentation("fun format(value: Long): String = value.toString()", file = "DateUtils.kt").forbiddenSuffix()
        }

    @Test
    fun `forbidden-suffix bypass via nested Manager in commonMain`() =
        assertViolation("forbidden-suffix", "SessionManager") {
            presentation("class Foo {\n    private object SessionManager\n}").forbiddenSuffix()
        }

    @Test
    fun `forbidden-suffix bypass via feature storage interface implementation`() =
        assertViolation("forbidden-suffix", "FooStoreImpl") {
            fixture(src("feature/foo", "S.kt", "package $FOO.data\n\ninternal class FooStoreImpl", "androidMain"))
                .forbiddenSuffix()
        }

    @Test
    fun `composable-in-presentation bypass via member of object in data`() =
        assertViolation("composable-in-presentation", "Row") {
            fixture(
                src(
                    "feature/foo",
                    "Rows.kt",
                    "package $FOO.data\n\nobject Rows {\n    @Composable\n    fun Row() = Unit\n}",
                ),
            ).composableInPresentation()
        }

    @Test
    fun `composable-in-presentation bypass via fully qualified annotation`() =
        assertViolation("composable-in-presentation", "Title") {
            fixture(
                src(
                    "feature/foo",
                    "T.kt",
                    "package $FOO.domain\n\n@androidx.compose.runtime.Composable\nfun Title() = Unit",
                ),
            ).composableInPresentation()
        }

    @Test
    fun `composable-in-presentation bypass via presentation sub package of domain`() =
        assertViolation("composable-in-presentation", "Badge") {
            fixture(
                src("feature/foo", "B.kt", "package $FOO.domain.presentation\n\n@Composable\nfun Badge() = Unit"),
            ).composableInPresentation()
        }

    @Test
    fun `composable-in-presentation bypass via platform source set`() =
        assertViolation("composable-in-presentation", "Picker") {
            fixture(
                src(
                    "feature/foo",
                    "P.kt",
                    "package $FOO.data.platform\n\n@Composable\nfun Picker() = Unit",
                    "androidMain",
                ),
            ).composableInPresentation()
        }

    @Test
    fun `composable-no-view-model bypass via lambda returning view model`() =
        assertViolation("composable-no-view-model", "provider") {
            presentation("@Composable\nfun Foo(provider: () -> FooViewModel) = Unit").composableNoViewModel()
        }

    @Test
    fun `composable-no-view-model bypass via import alias`() = assertViolation("composable-no-view-model", "model") {
        presentation(
            "import $FOO.presentation.vm.FooViewModel as Model\n\n@Composable\nfun Foo(model: Model) = Unit",
        ).composableNoViewModel()
    }

    @Test
    fun `composable-no-view-model bypass via extension receiver`() =
        assertViolation("composable-no-view-model", "Content") {
            presentation("@Composable\nfun FooViewModel.Content() = Unit").composableNoViewModel()
        }

    @Test
    fun `composable-no-view-model bypass via generic holder in member function`() =
        assertViolation("composable-no-view-model", "holder") {
            presentation("object Screens {\n    @Composable\n    fun Foo(holder: Lazy<FooViewModel>) = Unit\n}")
                .composableNoViewModel()
        }
}
