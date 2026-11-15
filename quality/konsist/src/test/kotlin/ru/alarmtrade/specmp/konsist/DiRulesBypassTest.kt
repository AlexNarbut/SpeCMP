package ru.alarmtrade.specmp.konsist

import org.junit.jupiter.api.Test

class DiRulesBypassTest {

    @Test
    fun `clean wiring passes di rules`() {
        val arch = fixture(
            impl(),
            src(
                "feature/foo",
                "FooModule.kt",
                """
                package $FOO.di

                import org.koin.dsl.module
                import org.koin.core.component.KoinComponent

                val fooModule = module { single { FooRepositoryImpl() } }
                """,
            ),
            src(
                "feature/foo",
                "FooService.kt",
                """
                package $FOO.domain.service

                class FooService(private val scope: CoroutineScope) {
                    private val started = MutableStateFlow(false)
                    private val job = scope.launch(start = CoroutineStart.LAZY) { }
                    private val cached by lazy { scope.launch { } }

                    fun start() {
                        if (started.compareAndSet(false, true)) job.start()
                    }
                }

                val first = 1
                val second = first + 1
                val reader: Int get() = later
                val later = 2
                """,
            ),
            "feature/foo/build.gradle.kts" to """
                kotlin {
                    sourceSets {
                        commonTest.dependencies { implementation(project(":feature:foo:testing")) }
                        val latestMain by getting
                    }
                }
                dependencies { add("jvmTestImplementation", projects.feature.foo.testing) }
            """,
        )
        arch.koinWiring()
        arch.koinModule()
        arch.noServiceLocator()
        arch.composableInjection()
        arch.noConstructorSideEffects()
        arch.startOnceCas()
        arch.topLevelInitOrder()
        arch.testingModulesTestOnly()
    }

    @Test
    fun `koin-wiring bypass via constructor call inside nested object`() = assertViolation("koin-wiring", "Holder") {
        fixture(
            impl(),
            presentation("Holder.kt", "object Holder {\n    object Inner { val repo = FooRepositoryImpl() }\n}"),
        ).koinWiring()
    }

    @Test
    fun `koin-wiring bypass via constructor reference`() = assertViolation("koin-wiring", "factory") {
        fixture(impl(), presentation("Factory.kt", "val factory: () -> Any = ::FooRepositoryImpl")).koinWiring()
    }

    @Test
    fun `koin-wiring bypass via import alias`() = assertViolation("koin-wiring", "build") {
        fixture(
            impl(),
            presentation(
                "Build.kt",
                "import $FOO.data.repository.FooRepositoryImpl as Repo\n\nfun build(): Any = Repo()",
            ),
        ).koinWiring()
    }

    @Test
    fun `koin-wiring bypass via fully qualified name in platform source set`() =
        assertViolation("koin-wiring", "build") {
            fixture(
                impl(),
                src(
                    "feature/foo",
                    "Build.kt",
                    "package $FOO.presentation\n\nfun build(): Any = $FOO.data.repository.FooRepositoryImpl()",
                    sourceSet = "iosMain",
                ),
            ).koinWiring()
        }

    @Test
    fun `koin-wiring bypass via repository implementation without Impl suffix`() =
        assertViolation("koin-wiring", "build") {
            fixture(
                src(
                    "feature/foo",
                    "SpecialistFooRepository.kt",
                    "package $FOO.data.repository\n\nclass SpecialistFooRepository : FooRepository",
                ),
                presentation("Build.kt", "fun build(): Any = SpecialistFooRepository()"),
            ).koinWiring()
        }

    @Test
    fun `koin-module bypass via name without Module suffix`() = assertViolation("koin-module", "fooBindings") {
        fixture(di("Bindings.kt", "import org.koin.dsl.module\n\nval fooBindings = module { }")).koinModule()
    }

    @Test
    fun `koin-module bypass via module outside di package`() = assertViolation("koin-module", "fooModule") {
        fixture(presentation("Wiring.kt", "import org.koin.dsl.module\n\nval fooModule = module { }")).koinModule()
    }

    @Test
    fun `koin-module bypass via lazy delegate`() = assertViolation("koin-module", "fooBindings") {
        fixture(
            di("Bindings.kt", "import org.koin.dsl.module\n\nval fooBindings by lazy { module { } }"),
        ).koinModule()
    }

    @Test
    fun `koin-module bypass via import alias of module builder`() = assertViolation("koin-module", "fooBindings") {
        fixture(
            di("Bindings.kt", "import org.koin.dsl.module as bindings\n\nval fooBindings = bindings { }"),
        ).koinModule()
    }

    @Test
    fun `koin-module bypass via property nested in object`() = assertViolation("koin-module", "deps") {
        fixture(
            presentation("Wiring.kt", "import org.koin.dsl.module\n\nobject Wiring {\n    val deps = module { }\n}"),
        ).koinModule()
    }

    @Test
    fun `koin-module bypass via fully qualified Module type`() = assertViolation("koin-module", "fooDeps") {
        fixture(
            di("Deps.kt", "val fooDeps: org.koin.core.module.Module = org.koin.dsl.module { }"),
        ).koinModule()
    }

    @Test
    fun `no-service-locator bypass via import alias`() = assertViolation("no-service-locator", "FooHolder") {
        fixture(
            presentation(
                "FooHolder.kt",
                """
                import org.koin.core.component.KoinComponent as Locator
                import org.koin.core.component.inject as lazyDep

                class FooHolder : Locator {
                    private val repo: FooRepository by lazyDep()
                }
                """,
            ),
        ).noServiceLocator()
    }

    @Test
    fun `no-service-locator bypass via fully qualified name`() = assertViolation("no-service-locator", "load") {
        fixture(
            presentation("Load.kt", "fun load(): Any = org.koin.mp.KoinPlatform.getKoin().get<FooRepository>()"),
        ).noServiceLocator()
    }

    @Test
    fun `no-service-locator bypass via star import`() =
        assertViolation("no-service-locator", "org.koin.core.component") {
            fixture(
                presentation("FooHolder.kt", "import org.koin.core.component.*\n\nclass FooHolder : KoinComponent"),
            ).noServiceLocator()
        }

    @Test
    fun `no-service-locator bypass via typealias`() = assertViolation("no-service-locator", "Locator") {
        fixture(
            presentation(
                "FooHolder.kt",
                "typealias Locator = org.koin.core.component.KoinComponent\n\nclass FooHolder : Locator",
            ),
        ).noServiceLocator()
    }

    @Test
    fun `no-service-locator bypass via nested class in platform source set`() =
        assertViolation("no-service-locator", "Outer") {
            fixture(
                src(
                    "feature/foo",
                    "Outer.kt",
                    """
                    package $FOO.presentation

                    import org.koin.java.KoinJavaComponent

                    class Outer {
                        class Inner {
                            val repo = KoinJavaComponent.get<Any>(Any::class.java)
                        }
                    }
                    """,
                    sourceSet = "jvmMain",
                ),
            ).noServiceLocator()
        }

    @Test
    fun `composable-injection bypass via import alias`() = assertViolation("composable-injection", "FooScreen") {
        fixture(
            presentation(
                "FooScreen.kt",
                "import org.koin.compose.koinInject as inject\n\n@Composable\nfun FooScreen(repo: Any = inject()) = Unit",
            ),
        ).composableInjection()
    }

    @Test
    fun `composable-injection bypass via fully qualified call`() =
        assertViolation("composable-injection", "FooScreen") {
            fixture(
                presentation(
                    "FooScreen.kt",
                    "@Composable\nfun FooScreen() {\n    val repo = org.koin.compose.koinInject<Any>()\n}",
                ),
            ).composableInjection()
        }

    @Test
    fun `composable-injection bypass via star import`() = assertViolation("composable-injection", "org.koin.compose") {
        fixture(
            presentation("FooScreen.kt", "import org.koin.compose.*\n\n@Composable\nfun FooScreen() = Unit"),
        ).composableInjection()
    }

    @Test
    fun `composable-injection bypass via koin scope local in nested object`() =
        assertViolation("composable-injection", "FooUi") {
            fixture(
                presentation(
                    "FooUi.kt",
                    """
                    import org.koin.compose.LocalKoinScope

                    object FooUi {
                        @Composable
                        fun Content() {
                            val repo = LocalKoinScope.current.get<Any>()
                        }
                    }
                    """,
                ),
            ).composableInjection()
        }

    @Test
    fun `no-constructor-side-effects bypass via init in nested class`() =
        assertViolation("no-constructor-side-effects", "Inner") {
            fixture(presentation("Outer.kt", "class Outer {\n    class Inner {\n        init { println() }\n    }\n}"))
                .noConstructorSideEffects()
        }

    @Test
    fun `no-constructor-side-effects bypass via import alias of launch`() =
        assertViolation("no-constructor-side-effects", "FooService") {
            fixture(
                domain(
                    "FooService.kt",
                    """
                    import kotlinx.coroutines.launch as go

                    class FooService(scope: CoroutineScope) {
                        private val job = scope.go { }
                    }
                    """,
                ),
            ).noConstructorSideEffects()
        }

    @Test
    fun `no-constructor-side-effects bypass via LAZY mentioned outside start argument`() =
        assertViolation("no-constructor-side-effects", "FooService") {
            fixture(
                domain(
                    "FooService.kt",
                    """
                    class FooService(scope: CoroutineScope) {
                        private val job = scope.launch { }.also { println(CoroutineStart.LAZY) }
                    }
                    """,
                ),
            ).noConstructorSideEffects()
        }

    @Test
    fun `no-constructor-side-effects bypass via object init`() =
        assertViolation("no-constructor-side-effects", "FooTracker") {
            fixture(domain("FooTracker.kt", "object FooTracker {\n    init { println() }\n}"))
                .noConstructorSideEffects()
        }

    @Test
    fun `no-constructor-side-effects bypass via eager stateIn in companion object`() =
        assertViolation("no-constructor-side-effects", "Companion") {
            fixture(
                domain(
                    "FooService.kt",
                    """
                    class FooService {
                        companion object {
                            val state = source.stateIn(scope, SharingStarted.Eagerly, 0)
                        }
                    }
                    """,
                ),
            ).noConstructorSideEffects()
        }

    @Test
    fun `start-once-cas bypass via nullable job guard with fully qualified type`() =
        assertViolation("start-once-cas", "FooService") {
            fixture(
                domain(
                    "FooService.kt",
                    """
                    class FooService {
                        private var job: kotlinx.coroutines.Job? = null

                        fun start() {
                            if (job == null) job = scope.launch { }
                        }
                    }
                    """,
                ),
            ).startOnceCas()
        }

    @Test
    fun `start-once-cas bypass via volatile boolean flag`() = assertViolation("start-once-cas", "FooService") {
        fixture(
            domain(
                "FooService.kt",
                """
                class FooService {
                    @Volatile
                    private var started = false

                    override fun start() {
                        if (!started) started = true
                    }
                }
                """,
            ),
        ).startOnceCas()
    }

    @Test
    fun `start-once-cas bypass via start with default parameter`() = assertViolation("start-once-cas", "FooService") {
        fixture(
            domain(
                "FooService.kt",
                """
                class FooService {
                    private var running: Boolean = false

                    fun start(force: Boolean = false) {
                        if (!running || force) running = true
                    }
                }
                """,
            ),
        ).startOnceCas()
    }

    @Test
    fun `start-once-cas bypass via object`() = assertViolation("start-once-cas", "FooStarter") {
        fixture(
            domain(
                "FooStarter.kt",
                """
                object FooStarter {
                    private var job: Job? = null

                    fun start() {
                        if (job == null) job = scope.launch { }
                    }
                }
                """,
            ),
        ).startOnceCas()
    }

    @Test
    fun `top-level-init-order bypass via reference inside module lambda`() =
        assertViolation("top-level-init-order", "fooModule") {
            fixture(
                di(
                    "FooModule.kt",
                    "val fooModule = module { includes(barModule) }\n\nval barModule = module { }",
                ),
            ).topLevelInitOrder()
        }

    @Test
    fun `top-level-init-order bypass via fully qualified reference`() =
        assertViolation("top-level-init-order", "first") {
            fixture(
                presentation("Order.kt", "val first = $FOO.presentation.second + 1\n\nval second = 1"),
            ).topLevelInitOrder()
        }

    @Test
    fun `top-level-init-order bypass via function reading later property`() =
        assertViolation("top-level-init-order", "first") {
            fixture(
                presentation(
                    "Order.kt",
                    "val first = readSecond()\n\nprivate fun readSecond() = second\n\nval second = 1",
                ),
            ).topLevelInitOrder()
        }

    @Test
    fun `top-level-init-order bypass via private property in platform source set`() =
        assertViolation("top-level-init-order", "first") {
            fixture(
                src(
                    "feature/foo",
                    "Order.kt",
                    "package $FOO.presentation\n\nval first = listOf(second)\n\nprivate val second = 1",
                    sourceSet = "androidMain",
                ),
            ).topLevelInitOrder()
        }

    @Test
    fun `testing-modules-test-only bypass via multiline dependency`() =
        assertViolation("testing-modules-test-only", "feature/foo/build.gradle.kts") {
            buildScript(
                """
                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation(
                                project(":feature:foo:testing")
                            )
                        }
                    }
                }
                """,
            ).testingModulesTestOnly()
        }

    @Test
    fun `testing-modules-test-only bypass via typesafe project accessor`() =
        assertViolation("testing-modules-test-only", "projects.core.testing") {
            buildScript(
                "kotlin {\n    sourceSets {\n        commonMain.dependencies { api(projects.core.testing) }\n    }\n}",
            )
                .testingModulesTestOnly()
        }

    @Test
    fun `testing-modules-test-only bypass via identifier containing test`() =
        assertViolation("testing-modules-test-only", "latestDeps") {
            buildScript(
                """
                val latestDeps = listOf(project(":core:testing"))
                dependencies { latestDeps.forEach { implementation(it) } }
                """,
            ).testingModulesTestOnly()
        }

    @Test
    fun `testing-modules-test-only bypass via add with production configuration`() =
        assertViolation("testing-modules-test-only", "commonMainImplementation") {
            buildScript("dependencies {\n    add(\"commonMainImplementation\", project(\":core:testing\"))\n}")
                .testingModulesTestOnly()
        }

    private fun impl() = src(
        "feature/foo",
        "FooRepositoryImpl.kt",
        "package $FOO.data.repository\n\nclass FooRepositoryImpl : FooRepository",
    )

    private fun presentation(file: String, code: String) = layer("presentation", file, code)

    private fun domain(file: String, code: String) = layer("domain.service", file, code)

    private fun di(file: String, code: String) = layer("di", file, code)

    private fun layer(pkg: String, file: String, code: String) =
        src("feature/foo", file, "package $FOO.$pkg\n\n${code.trimIndent()}")

    private fun buildScript(code: String) = fixture("feature/foo/build.gradle.kts" to code)
}
