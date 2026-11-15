package ru.alarmtrade.specmp.konsist

import org.junit.jupiter.api.Test

class ContractRulesBypassTest {

    @Test
    fun `clean contracts pass contract rules`() {
        val arch = usage(
            src(
                "feature/foo",
                "FooCoordinator.kt",
                """
                package $FOO.domain.service

                interface FooCoordinator {
                    fun start(): Boolean

                    fun update(transform: (Int) -> Int)
                }
                """,
            ),
            src("feature/foo", "Use.kt", "package $FOO.presentation\n\nfun use(c: FooCoordinator) = c.start()"),
            src(
                "feature/foo",
                "Calls.kt",
                "package $FOO.presentation\n\nfun more(c: FooCoordinator) = c.update { it }",
            ),
            src(
                "feature/foo",
                "Unused.kt",
                "package $FOO.presentation\n\nsuspend fun all(r: FooRepository) = r.unused()",
            ),
            src("feature/foo", "Dto.kt", "package $FOO.data.dto\n\ninternal class FooDto\n\nprivate const val X = 1"),
        )
        arch.repositoryContract()
        arch.contractMemberUsage()
        arch.serviceContract()
        arch.dataVisibility()
    }

    private fun repository(body: String, sourceSet: String = "commonMain") = fixture(
        src(
            "feature/foo",
            "FooRepository.kt",
            """
            package $FOO.domain.repository

            import $ROOT.core.common.Response
            import kotlinx.coroutines.flow.Flow

            interface FooRepository {
            $body
            }
            """,
            sourceSet,
        ),
    )

    private fun service(name: String, body: String) = fixture(
        src(
            "feature/foo",
            "$name.kt",
            """
            package $FOO.domain.service

            import $ROOT.core.common.Response

            interface $name {
            $body
            }
            """,
        ),
    )

    @Test
    fun `repository-contract bypass via nullable Response`() = assertViolation("repository-contract", "load") {
        repository("    suspend fun load(): Response<Int>?").repositoryContract()
    }

    @Test
    fun `repository-contract bypass via blocking function returning Response`() =
        assertViolation("repository-contract", "load") {
            repository("    fun load(): Response<Int>").repositoryContract()
        }

    @Test
    fun `repository-contract bypass via Response nested inside flow element`() =
        assertViolation("repository-contract", "items") {
            repository("    fun items(): Flow<List<Response<Int>>>").repositoryContract()
        }

    @Test
    fun `repository-contract bypass via synchronous property in contract without Repository suffix`() =
        assertViolation("repository-contract", "cached") {
            fixture(
                src(
                    "feature/foo",
                    "FooSource.kt",
                    """
                    package $FOO.domain.repository

                    interface FooSource {
                        val cached: Int
                    }
                    """,
                    sourceSet = "iosMain",
                ),
            ).repositoryContract()
        }

    @Test
    fun `repository-contract bypass via default implementation in interface`() =
        assertViolation("repository-contract", "count") {
            repository("    fun count(): Int = 0").repositoryContract()
        }

    private fun usage(vararg extra: Pair<String, String>) = fixture(
        src(
            "feature/foo",
            "FooRepository.kt",
            """
            package $FOO.domain.repository

            import $ROOT.core.common.Response

            interface FooRepository {
                suspend fun load(): Response<Int>

                suspend fun unused(): Response<Int>
            }
            """,
        ),
        src(
            "feature/foo",
            "FooViewModel.kt",
            """
            package $FOO.presentation

            class FooViewModel(private val repository: FooRepository) {
                suspend fun onStart() {
                    repository.load()
                }
            }
            """,
        ),
        *extra,
    )

    @Test
    fun `contract-member-usage bypass via mention in comment`() = assertViolation("contract-member-usage", "unused") {
        usage(
            src(
                "feature/foo",
                "Docs.kt",
                "package $FOO.presentation\n\n// repository.unused() вызывается позже\nclass Docs",
            ),
        ).contractMemberUsage()
    }

    @Test
    fun `contract-member-usage bypass via mention in string literal`() =
        assertViolation("contract-member-usage", "unused") {
            usage(
                src("feature/foo", "Log.kt", "package $FOO.presentation\n\nval trace = \"repository.unused()\""),
            ).contractMemberUsage()
        }

    @Test
    fun `contract-member-usage bypass via call from tests only`() = assertViolation("contract-member-usage", "unused") {
        usage(
            src(
                "feature/foo",
                "FooTest.kt",
                "package $FOO\n\nsuspend fun check(repository: FooRepository) = repository.unused()",
                sourceSet = "commonTest",
            ),
        ).contractMemberUsage()
    }

    @Test
    fun `contract-member-usage bypass via call from testing module fake`() =
        assertViolation("contract-member-usage", "unused") {
            usage(
                src(
                    "feature/foo/testing",
                    "FakeFoo.kt",
                    "package $FOO.testing\n\nsuspend fun warmUp(repository: FooRepository) = repository.unused()",
                ),
            ).contractMemberUsage()
        }

    @Test
    fun `contract-member-usage bypass via call from own implementation`() =
        assertViolation("contract-member-usage", "unused") {
            usage(
                src(
                    "feature/foo",
                    "FooRepositoryImpl.kt",
                    """
                    package $FOO.data.repository

                    internal class FooRepositoryImpl : FooRepository {
                        override suspend fun load() = unused()

                        override suspend fun unused() = TODO()
                    }
                    """,
                ),
            ).contractMemberUsage()
        }

    @Test
    fun `service-contract bypass via nullable Boolean command`() = assertViolation("service-contract", "start") {
        service("FooCoordinator", "    fun start(): Boolean?").serviceContract()
    }

    @Test
    fun `service-contract bypass via expression body without type`() = assertViolation("service-contract", "start") {
        service("FooController", "    fun start() = 1").serviceContract()
    }

    @Test
    fun `service-contract bypass via synchronous Boolean property`() = assertViolation("service-contract", "running") {
        service("FooSessionGate", "    val running: Boolean").serviceContract()
    }

    @Test
    fun `service-contract bypass via Unit command returning data through callback`() =
        assertViolation("service-contract", "load") {
            service("FooCoordinator", "    fun load(onResult: (Int) -> Unit)").serviceContract()
        }

    @Test
    fun `service-contract bypass via function interface named by role`() =
        assertViolation("service-contract", "snapshot") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nfun interface FooController {\n    fun snapshot(): List<Int>\n}",
                    sourceSet = "androidMain",
                ),
            ).serviceContract()
        }

    private fun data(code: String, sourceSet: String = "commonMain") =
        fixture(src("feature/foo", "A.kt", "package $FOO.data.repository\n\n$code", sourceSet))

    @Test
    fun `data-visibility bypass via public typealias`() = assertViolation("data-visibility", "Raw") {
        data("typealias Raw = Map<String, Int>").dataVisibility()
    }

    @Test
    fun `data-visibility bypass via explicit public extension`() = assertViolation("data-visibility", "parse") {
        data("public fun String.parse(): Int = length").dataVisibility()
    }

    @Test
    fun `data-visibility bypass via actual class in platform source set`() =
        assertViolation("data-visibility", "Storage") {
            data("actual class Storage", sourceSet = "iosMain").dataVisibility()
        }

    @Test
    fun `data-visibility bypass via top-level const and object`() =
        assertViolation("data-visibility", "LIMIT", "Defaults") {
            data("const val LIMIT = 10\n\nobject Defaults").dataVisibility()
        }

    @Test
    fun `clean implementations, use cases and mappers pass contract rules`() {
        val arch = fixture(
            src("feature/foo", "FooRepository.kt", "package $FOO.domain.repository\n\ninterface FooRepository"),
            src("feature/foo", "BarRepository.kt", "package $FOO.domain.repository\n\ninterface BarRepository"),
            src(
                "feature/foo",
                "SpecialistFooRepository.kt",
                """
                package $FOO.data.repository

                import $FOO.data.datasource.FooDataSource

                internal class SpecialistFooRepository(private val source: FooDataSource) : FooRepository
                """,
            ),
            src(
                "feature/foo",
                "SpecialistFooRepositoryTest.kt",
                "package $FOO.data.repository\n\nclass T {\n    val r = SpecialistFooRepository(FakeSource())\n}",
                sourceSet = "commonTest",
            ),
            src(
                "feature/foo",
                "LoadFooUseCase.kt",
                """
                package $FOO.domain.usecase

                class LoadFooUseCase(private val foo: FooRepository, private val bar: BarRepository) {
                    suspend operator fun invoke(): Response<Int> = TODO()
                }
                """,
            ),
            src(
                "feature/foo",
                "FooMappers.kt",
                "package $FOO.data.mapper\n\ninternal fun FooDto.toDomain(): Foo = Foo()",
            ),
            src("core/common", "Helpers.kt", "package $ROOT.core.common\n\nfun clamp(x: Int) = x"),
            src(
                "feature/foo",
                "Use.kt",
                "package $FOO.presentation\n\nimport $ROOT.core.common.clamp\n\nval y = clamp(1)",
            ),
        )
        arch.implVisibility()
        arch.repositoryImpl()
        arch.repositoryImplTested()
        arch.coreHelperUsage()
        arch.useCasePackage()
        arch.useCaseContract()
        arch.dataSourceConsumers()
        arch.mapperPlacement()
    }

    @Test
    fun `impl-visibility bypass via explicit public modifier`() = assertViolation("impl-visibility", "FooServiceImpl") {
        fixture(
            src("feature/foo", "A.kt", "package $FOO.data.service\n\npublic class FooServiceImpl : FooService"),
        ).implVisibility()
    }

    @Test
    fun `impl-visibility bypass via source-named repository without Impl suffix`() =
        assertViolation("impl-visibility", "SettingsFooRepository") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.data.repository\n\nclass SettingsFooRepository : $FOO.domain.repository.FooRepository",
                ),
            ).implVisibility()
        }

    @Test
    fun `impl-visibility bypass via nested class in public object`() =
        assertViolation("impl-visibility", "FooClockImpl") {
            fixture(
                src(
                    "core/common",
                    "A.kt",
                    "package $ROOT.core.common\n\nobject Clocks {\n    class FooClockImpl : Clock\n}",
                ),
            ).implVisibility()
        }

    @Test
    fun `impl-visibility bypass via actual class in platform source set`() =
        assertViolation("impl-visibility", "FooPickerImpl") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.platform\n\nactual class FooPickerImpl : FooPicker",
                    sourceSet = "iosMain",
                ),
            ).implVisibility()
        }

    @Test
    fun `repository-impl bypass via implementing another repository`() =
        assertViolation("repository-impl", "FooRepositoryImpl") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.data.repository\n\ninternal class FooRepositoryImpl : BarRepository",
                ),
            ).repositoryImpl()
        }

    @Test
    fun `repository-impl bypass via generic marker interface named Repository`() =
        assertViolation("repository-impl", "SpecialistFooRepository") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.data.repository\n\ninternal class SpecialistFooRepository : Repository<Foo>",
                ),
            ).repositoryImpl()
        }

    @Test
    fun `repository-impl bypass via nested class without supertypes`() =
        assertViolation("repository-impl", "CacheRepository") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.data.repository\n\ninternal object Holder {\n    class CacheRepository\n}",
                ),
            ).repositoryImpl()
        }

    @Test
    fun `repository-impl bypass via supertype differing only by prefix`() =
        assertViolation("repository-impl", "FooRepositoryImpl") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.data.repository\n\ninternal class FooRepositoryImpl : BaseFooRepository()",
                    sourceSet = "jvmMain",
                ),
            ).repositoryImpl()
        }

    private fun tested(test: String, testModule: String = "feature/foo", sourceSet: String = "commonTest") = fixture(
        src(
            "feature/foo",
            "A.kt",
            "package $FOO.data.repository\n\ninternal class SpecialistFooRepository : FooRepository",
        ),
        src(testModule, "T.kt", "package $FOO.data.repository\n\n$test", sourceSet),
    )

    @Test
    fun `repository-impl-tested bypass via import only`() =
        assertViolation("repository-impl-tested", "SpecialistFooRepository") {
            tested("import $FOO.data.repository.SpecialistFooRepository\n\nclass T").repositoryImplTested()
        }

    @Test
    fun `repository-impl-tested bypass via comment and string mentions`() =
        assertViolation("repository-impl-tested", "SpecialistFooRepository") {
            tested(
                "// SpecialistFooRepository\nclass T {\n    val name = \"SpecialistFooRepository\"\n}",
            ).repositoryImplTested()
        }

    @Test
    fun `repository-impl-tested bypass via test in another module`() =
        assertViolation("repository-impl-tested", "SpecialistFooRepository") {
            tested("class T {\n    val r = SpecialistFooRepository()\n}", testModule = "feature/bar")
                .repositoryImplTested()
        }

    @Test
    fun `repository-impl-tested bypass via testing module production source set`() =
        assertViolation("repository-impl-tested", "SpecialistFooRepository") {
            tested(
                "class FakeHolder {\n    val r = SpecialistFooRepository()\n}",
                testModule = "feature/foo/testing",
                sourceSet = "commonMain",
            ).repositoryImplTested()
        }

    @Test
    fun `repository-impl-tested bypass via longer name sharing the prefix`() =
        assertViolation("repository-impl-tested", "SpecialistFooRepository") {
            tested("class T {\n    val r = SpecialistFooRepositoryFake()\n}").repositoryImplTested()
        }

    private fun helper(vararg usages: Pair<String, String>) = fixture(
        src("core/common", "Helpers.kt", "package $ROOT.core.common\n\nfun clamp(x: Int) = x"),
        *usages,
    )

    @Test
    fun `core-helper-usage bypass via own module test`() = assertViolation("core-helper-usage", "clamp") {
        helper(
            src("core/common", "T.kt", "package $ROOT.core.common\n\nval y = clamp(1)", sourceSet = "commonTest"),
        ).coreHelperUsage()
    }

    @Test
    fun `core-helper-usage bypass via same-named local function without import`() =
        assertViolation("core-helper-usage", "clamp") {
            helper(
                src("feature/foo", "U.kt", "package $FOO.presentation\n\nfun clamp(x: Int) = x\n\nval y = clamp(1)"),
            ).coreHelperUsage()
        }

    @Test
    fun `core-helper-usage bypass via comment and string mentions`() = assertViolation("core-helper-usage", "clamp") {
        helper(
            src(
                "feature/foo",
                "U.kt",
                "package $FOO.presentation\n\nimport $ROOT.core.common.clamp\n\n// clamp(1)\nval y = \"clamp\"",
            ),
        ).coreHelperUsage()
    }

    @Test
    fun `core-helper-usage bypass via unused import only`() = assertViolation("core-helper-usage", "clamp") {
        helper(
            src("feature/foo", "U.kt", "package $FOO.presentation\n\nimport $ROOT.core.common.clamp\n\nval y = 1"),
        ).coreHelperUsage()
    }

    @Test
    fun `core-helper-usage bypass via usage only in the declaring file`() =
        assertViolation("core-helper-usage", "clamp") {
            fixture(
                src(
                    "core/common",
                    "Helpers.kt",
                    "package $ROOT.core.common\n\nfun clamp(x: Int) = x\n\nval y = clamp(1)",
                ),
            ).coreHelperUsage()
        }

    @Test
    fun `use-case-package bypass via domain usecase segment nested in presentation`() =
        assertViolation("use-case-package", "LoadFooUseCase") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.presentation.domain.usecase\n\nclass LoadFooUseCase"),
            ).useCasePackage()
        }

    @Test
    fun `use-case-package bypass via nested class in presentation`() =
        assertViolation("use-case-package", "LoadFooUseCase") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.presentation\n\nobject Cases {\n    class LoadFooUseCase\n}"),
            ).useCasePackage()
        }

    @Test
    fun `use-case-package bypass via domain service package in platform source set`() =
        assertViolation("use-case-package", "LoadFooUseCase") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.domain.service\n\nclass LoadFooUseCase",
                    sourceSet = "androidMain",
                ),
            ).useCasePackage()
        }

    @Test
    fun `use-case-package bypass via core module util package`() = assertViolation("use-case-package", "SyncUseCase") {
        fixture(src("core/common", "A.kt", "package $ROOT.core.common.util\n\nclass SyncUseCase")).useCasePackage()
    }

    private fun useCase(
        body: String,
        params: String = "private val foo: FooRepository, private val bar: BarRepository",
    ) = fixture(
        src(
            "feature/foo",
            "LoadFooUseCase.kt",
            "package $FOO.domain.usecase\n\nclass LoadFooUseCase($params) {\n$body\n}",
        ),
    )

    @Test
    fun `use-case-contract bypass via nullable Response`() = assertViolation("use-case-contract", "LoadFooUseCase") {
        useCase("    suspend operator fun invoke(): Response<Int>? = null").useCaseContract()
    }

    @Test
    fun `use-case-contract bypass via flow of nullable Response`() =
        assertViolation("use-case-contract", "LoadFooUseCase") {
            useCase("    operator fun invoke(): Flow<Response<Int>?> = TODO()").useCaseContract()
        }

    @Test
    fun `use-case-contract bypass via public property next to invoke`() =
        assertViolation("use-case-contract", "LoadFooUseCase") {
            useCase(
                "    val cache: Map<Int, Int> = emptyMap()\n\n    suspend operator fun invoke(): Response<Int> = TODO()",
            ).useCaseContract()
        }

    @Test
    fun `use-case-contract bypass via public constructor property`() =
        assertViolation("use-case-contract", "LoadFooUseCase") {
            useCase(
                "    suspend operator fun invoke(): Response<Int> = TODO()",
                params = "val foo: FooRepository, private val bar: BarRepository",
            ).useCaseContract()
        }

    @Test
    fun `use-case-contract bypass via dependency hidden in a dispatcher-like wrapper`() =
        assertViolation("use-case-contract", "LoadFooUseCase") {
            useCase(
                "    suspend operator fun invoke(): Response<Int> = TODO()",
                params = "private val foo: FooRepository, private val dispatchers: AppDispatchers",
            ).useCaseContract()
        }

    private fun consumer(code: String, sourceSet: String = "commonMain") =
        fixture(src("feature/foo", "A.kt", code, sourceSet))

    @Test
    fun `data-source-consumers bypass via list of data sources`() = assertViolation("data-source-consumers", "A.kt") {
        consumer("package $FOO.presentation\n\nclass FooViewModel(private val all: List<FooDataSource>)")
            .dataSourceConsumers()
    }

    @Test
    fun `data-source-consumers bypass via Lazy wrapper`() = assertViolation("data-source-consumers", "A.kt") {
        consumer("package $FOO.domain.usecase\n\nclass LoadFooUseCase(private val source: Lazy<FooDataSource>)")
            .dataSourceConsumers()
    }

    @Test
    fun `data-source-consumers bypass via data repository segment nested in presentation`() =
        assertViolation("data-source-consumers", "A.kt") {
            consumer("package $FOO.presentation.data.repository\n\nclass FooViewModel(private val s: FooDataSource)")
                .dataSourceConsumers()
        }

    @Test
    fun `data-source-consumers bypass via secondary constructor`() = assertViolation("data-source-consumers", "A.kt") {
        consumer(
            "package $FOO.presentation\n\nclass FooViewModel() {\n    constructor(s: FooDataSource) : this()\n}",
            sourceSet = "jvmMain",
        ).dataSourceConsumers()
    }

    @Test
    fun `data-source-consumers bypass via fully qualified nullable type`() =
        assertViolation("data-source-consumers", "Parameter s") {
            consumer(
                "package $FOO.domain.service\n\nclass FooService(private val s: $FOO.data.datasource.FooDataSource?)",
            ).dataSourceConsumers()
        }

    private fun mapper(code: String, pkg: String = "$FOO.presentation", sourceSet: String = "commonMain") =
        fixture(src("feature/foo", "A.kt", "package $pkg\n\n$code", sourceSet))

    @Test
    fun `mapper-placement bypass via data mapper segment nested in presentation`() =
        assertViolation("mapper-placement", "toDomain") {
            mapper(
                "internal fun FooDto.toDomain(): Foo = Foo()",
                pkg = "$FOO.presentation.data.mapper",
            ).mapperPlacement()
        }

    @Test
    fun `mapper-placement bypass via member of object in presentation`() =
        assertViolation("mapper-placement", "toFooDto") {
            mapper("object FooMapping {\n    fun Foo.toFooDto(): FooDto = FooDto()\n}").mapperPlacement()
        }

    @Test
    fun `mapper-placement bypass via prefixed mapper name`() = assertViolation("mapper-placement", "mapToDomain") {
        mapper("fun FooDto.mapToDomain(): Foo = Foo()", pkg = "$FOO.domain").mapperPlacement()
    }

    @Test
    fun `mapper-placement bypass via public mapper in platform source set`() =
        assertViolation("mapper-placement", "toDomainModel") {
            mapper("fun FooDto.toDomainModel(): Foo = Foo()", pkg = "$FOO.data.mapper", sourceSet = "iosMain")
                .mapperPlacement()
        }
}
