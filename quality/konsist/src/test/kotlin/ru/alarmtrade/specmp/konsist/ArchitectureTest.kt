package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoBaseDeclaration
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.KoParentDeclaration
import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.declaration.combined.KoClassAndInterfaceAndObjectDeclaration
import com.lemonappdev.konsist.api.provider.modifier.KoVisibilityModifierProvider
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import java.io.File
import org.junit.jupiter.api.Test

private const val ROOT = "ru.alarmtrade.specmp"

/**
 * Правила раскладки кода из docs/architecture.md и docs/patterns.md (описание — docs/code-quality.md, «Konsist»).
 *
 * `testName` правила — его id: точечное исключение в коде — `@Suppress("konsist.<id>")` (для файла —
 * `@file:Suppress`) с причиной комментарием строкой выше.
 */
class ArchitectureTest {
    // Модули тестовых дублёров `*/testing` — тестовый код в commonMain: правила продакшена к ним не применяются.
    private val scope = Konsist.scopeFromProduction().slice {
        (it.isCore || it.isFeature || it.moduleName in APP_MODULES) && !it.isTestingModule
    }
    private val libraryFiles = scope.files.filter { it.isCore || it.isFeature }
    private val classes = scope.classes(includeNested = true)

    @Test
    fun `core depends only on core, features do not depend on the app`() {
        libraryFiles.flatMap { file -> file.imports.filter { it.name.isForbiddenProjectImport(file) } }
            .assertFalse(
                testName = "module-direction",
                additionalMessage = "core/* импортирует только core/*, feature/* — core/* и feature/*; " +
                    "пакеты composition roots (composeApp, flasherApp) не импортируются. " +
                    "docs/architecture.md, «Граф зависимостей модулей»",
            ) { true }
    }

    @Test
    fun `package starts with the module base package`() {
        libraryFiles.assertTrue(
            testName = "module-package",
            additionalMessage = "Пакет файла начинается с базового пакета модуля: feature/auth-state → " +
                "$ROOT.feature.authstate, feature/device/settings-ui → $ROOT.feature.device.settings.ui, " +
                "feature/device/presentation-core → $ROOT.feature.device.presentation.core. " +
                "docs/architecture.md, «Пространства имён»",
        ) { file ->
            val pkg = file.packagee?.name.orEmpty()
            pkg == file.basePackage || pkg.startsWith(file.basePackage + ".")
        }
    }

    @Test
    fun `domain does not depend on data, presentation, di, Compose or the SDK`() {
        val domainFiles = libraryFiles.filter { it.inLayer("domain") }
        val message = "domain — чистый Kotlin: без data/presentation/di, без Compose и ресурсов, без core/ui, " +
            "хранилищ (multiplatform-settings), сериализации и типов AlarmTrade SDK. " +
            "docs/architecture.md, «Структура feature-модуля»; docs/patterns.md, «Domain»"
        domainFiles.flatMap { file -> file.imports.filter { it.name.isForbiddenInDomain() } }
            .assertFalse(testName = "domain-isolation", additionalMessage = message) { true }
        // Полное имя в коде без импорта обходит проверку импортов — смотрим текст без комментариев и строк.
        domainFiles.assertFalse(testName = "domain-isolation", additionalMessage = "$message (полное имя в коде)") {
            it.qualifiedReferences().any { name -> name.isForbiddenInDomain() }
        }
    }

    @Test
    fun `presentation does not depend on data`() {
        val message = "presentation и composition roots зависят только от domain (своей и чужих фич): в фиче пакет " +
            "..data.. импортируют только data и di, в core — не presentation, в приложениях — только di " +
            "(графы Koin). docs/patterns.md, «Feature-модули»"
        val files = scope.files.filter { it.mayNotUseDataLayer }
        files.flatMap { file -> file.imports.filter { it.name.isProjectLayer("data") } }
            .assertFalse(testName = "presentation-isolation", additionalMessage = message) { true }
        files.assertFalse(testName = "presentation-isolation", additionalMessage = "$message (полное имя в коде)") {
            it.qualifiedReferences().any { name -> name.isProjectLayer("data") }
        }
    }

    @Test
    fun `data does not depend on presentation or di`() {
        libraryFiles.filter { it.inLayer("data") }
            .flatMap { file ->
                file.imports.filter { it.name.isProjectLayer("presentation") || it.name.isProjectLayer("di") }
            }
            .assertFalse(
                testName = "data-isolation",
                additionalMessage = "data реализует контракты domain и не знает об UI и Koin-модулях. " +
                    "docs/patterns.md, «Data — репозитории»",
            ) { true }
    }

    @Test
    fun `features do not import data of other features`() {
        libraryFiles.filter { it.isFeature }
            .flatMap { file -> file.imports.filter { it.name.isOtherFeatureData(file) } }
            .assertFalse(
                testName = "feature-data-isolation",
                additionalMessage = "Чужая фича доступна только через domain (контракты domain/repository, модели); " +
                    "её data — реализация, привязывается в её Koin-модуле. docs/architecture.md, " +
                    "«Граф зависимостей модулей»",
            ) { true }
    }

    @Test
    fun `roles live in their layer packages`() {
        scope.classesAndInterfacesAndObjects(includeNested = true)
            .filter { it.containingFile.isFeature }
            .assertTrue(
                testName = "layer-placement",
                additionalMessage = "Раскладка ролей фичи: реализация репозитория (класс *Repository, " +
                    "Specialist*Repository) — data/repository; *DataSource(Impl) — data/datasource; " +
                    "*CoordinatorImpl/*ControllerImpl — data/service или platform; *Coordinator/*Controller — " +
                    "только interface в domain/service. docs/patterns.md, «Data — репозитории»",
            ) { it.isPlacedByRole() }
    }

    @Test
    fun `feature repository interfaces live in domain`() {
        scope.interfaces(includeNested = true)
            .filter { it.name.endsWith(REPOSITORY) && it.containingFile.isFeature }
            .assertTrue(
                testName = "repository-in-domain",
                additionalMessage = "Интерфейс *Repository фичи — контракт domain: feature/<name>/domain/repository, " +
                    "реализация — в data/repository. docs/patterns.md, «Domain»",
            ) { it.resideInPackage("..domain..") }
    }

    @Test
    fun `repository members return Response`() {
        scope.interfaces(includeNested = true)
            .filter { it.name.endsWith(REPOSITORY) || it.resideInPackage("..domain.repository..") }
            .assertResultContract(
                testName = "repository-contract",
                role = "репозитория (interface *Repository, интерфейсы domain/repository)",
                docs = "docs/patterns.md, «Domain»",
            )
    }

    @Test
    fun `service members return Response`() {
        scope.interfaces(includeNested = true)
            .filter { it.name.isServiceName() || it.resideInPackage("..domain.service..") }
            .assertResultContract(
                testName = "service-contract",
                role = "доменного сервиса (interface *Coordinator/*Controller, интерфейсы domain/service)",
                docs = "docs/patterns.md, «Доменные сервисы»",
                syncCommandTypes = SERVICE_SYNC_COMMAND_TYPES,
            )
    }

    @Test
    fun `data declarations are not public`() {
        libraryFiles.filter { it.inLayer("data") }
            .flatMap { it.declarations(includeNested = false, includeLocal = false) }
            .filterIsInstance<KoVisibilityModifierProvider>()
            .assertTrue(
                testName = "data-visibility",
                additionalMessage = "Декларации верхнего уровня в ..data.. — internal/private: наружу модуля видны " +
                    "только контракты domain, привязка — в Koin-модуле. docs/patterns.md, «Data — репозитории»",
            ) { it.hasInternalModifier || it.hasPrivateModifier }
    }

    @Test
    fun `implementations are not public`() {
        classes.filter { it.isImplementation() && (it.containingFile.isCore || it.containingFile.isFeature) }
            .assertTrue(
                testName = "impl-visibility",
                additionalMessage = "Реализация (*Impl, класс в ..data.., реализующий *Repository, — " +
                    "Specialist*Repository) — internal: снаружи видна только абстракция, привязка — в Koin-модуле. " +
                    "docs/patterns.md, «Data — репозитории»",
            ) { it.hasInternalModifier || it.hasPrivateModifier }
    }

    @Test
    fun `repository implementation implements its repository`() {
        classes.filter { it.name.removeSuffix(IMPL).endsWith(REPOSITORY) }
            .assertTrue(
                testName = "repository-impl",
                additionalMessage = "Класс *Repository реализует свой интерфейс: имя = <источник><Интерфейс> " +
                    "(SpecialistFooRepository : FooRepository) или FooRepositoryImpl : FooRepository. " +
                    "docs/patterns.md, «Data — репозитории»",
            ) { klass ->
                klass.parents().map { it.simpleName() }
                    .any { it.endsWith(REPOSITORY) && klass.name.removeSuffix(IMPL).endsWith(it) }
            }
    }

    @Test
    fun `repository implementation is covered by a test of its module`() {
        val testCodeByModule = Konsist.scopeFromProject().files
            .filter { it.isTestSource }
            .groupBy({ it.moduleName }, { it.codeWithoutHeaderCommentsAndStrings() })
        classes.filter { it.isRepositoryImplementation() }
            .assertTrue(
                testName = "repository-impl-tested",
                additionalMessage = "У реализации репозитория (Specialist*Repository, Settings*Repository, " +
                    "*RepositoryImpl) есть тест в тестовом source set того же модуля, который использует класс по " +
                    "имени в коде (не в import, KDoc или строке). docs/patterns.md, «Тестирование репозиториев»",
            ) { impl ->
                val reference = Regex("""\b${impl.name}\b""")
                testCodeByModule[impl.containingFile.moduleName].orEmpty().any { reference.containsMatchIn(it) }
            }
    }

    @Test
    fun `core common and lce helpers have callers`() {
        val usageFiles = Konsist.scopeFromProject().files
            .filter { it.isCore || it.isFeature || it.moduleName in APP_MODULES }
            .map { file ->
                UsageFile(
                    path = file.path,
                    moduleName = file.moduleName,
                    isTest = file.isTestSource,
                    packageName = file.packagee?.name.orEmpty(),
                    imports = file.imports.map { it.name }.toSet(),
                    code = file.codeWithoutHeaderCommentsAndStrings(),
                )
            }
        val message = "Публичная top-level функция/свойство core/common и core/ui/lce вызывается вне своего файла " +
            "(production или тесты других модулей; собственные тесты модуля не считаются). Мёртвый хелпер удаляется " +
            "вместе с его тестом. docs/patterns.md, «Core»"
        val helperFiles = scope.files.filter { it.isSharedHelperFile }
        helperFiles.flatMap { it.functions(includeNested = false, includeLocal = false) }
            .filter { it.hasPublicOrDefaultModifier && !it.hasActualModifier }
            .assertTrue(testName = "core-helper-usage", additionalMessage = message) {
                usageFiles.references(it.name, it.containingFile)
            }
        helperFiles.flatMap { it.properties(includeNested = false) }
            .filter { it.hasPublicOrDefaultModifier && !it.hasActualModifier }
            .assertTrue(testName = "core-helper-usage", additionalMessage = message) {
                usageFiles.references(it.name, it.containingFile)
            }
    }

    @Test
    fun `implementations are constructed only in Koin modules`() {
        val implementations = classes.filter { it.isImplementation() }.map { it.name }.toSet()
        scope.files.filterNot { it.inLayer("di") }
            .flatMap { file ->
                file.classesAndInterfacesAndObjects(includeNested = false) +
                    file.functions(includeNested = false, includeLocal = false) +
                    file.properties(includeNested = false)
            }
            .assertFalse(
                testName = "koin-wiring",
                additionalMessage =
                    "Реализацию (*Impl, Specialist*Repository) создаёт только Koin-модуль (пакет di): " +
                        "Foo(…) или ::Foo вне di — ручная сборка графа в обход DI. Остальной код получает абстракцию " +
                        "конструктором. docs/patterns.md, «Внедрение зависимостей»",
            ) { declaration ->
                val code = declaration.text.stripCommentsAndStrings()
                (CONSTRUCTOR_CALL.findAll(code) + CONSTRUCTOR_REFERENCE.findAll(code))
                    .map { it.groupValues[1] }
                    .any { it.endsWith(IMPL) || it in implementations }
            }
    }

    @Test
    fun `Koin modules are declared in di and named Module`() {
        scope.files.flatMap { it.properties(includeNested = false) }
            .filter { it.type?.name == "Module" || KOIN_MODULE_INITIALIZER.containsMatchIn(it.text) }
            .assertTrue(
                testName = "koin-module",
                additionalMessage = "Koin-модуль — val <name>Module в пакете di. docs/patterns.md, " +
                    "«Внедрение зависимостей»",
            ) { it.name.endsWith("Module") && it.resideInPackage("..di..") }
    }

    @Test
    fun `domain models are immutable and not serializable`() {
        val modelFiles = libraryFiles.filter { ".domain.model." in ".${it.packagee?.name}." }
        val types = modelFiles.flatMap { it.classesAndInterfacesAndObjects(includeNested = true) }
        val properties = modelFiles.flatMap { it.properties(includeNested = false) } +
            types.flatMap { it.properties(includeNested = false) }
        val message = "Модель domain/model — неизменяемое значение: свойства только val, коллекции read-only " +
            "(List/Set/Map, не Mutable*/ArrayList/HashMap…), без @Serializable (формат передачи — DTO в data). " +
            "docs/patterns.md, «Domain»"
        properties.assertFalse(testName = "domain-model-purity", additionalMessage = message) {
            it.isVar || it.type?.text.isMutableCollectionType()
        }
        types.assertFalse(testName = "domain-model-purity", additionalMessage = message) {
            it.hasAnnotationWithName("Serializable")
        }
    }

    @Test
    fun `Serializable is used only for transport, storage formats and routes`() {
        val serializable = scope.classesAndInterfacesAndObjects(includeNested = true)
            .filter { it.hasAnnotationWithName("Serializable") }
        val routes = serializable.filter { it.isNavKey() }
        val routeArgumentTypes = routes.flatMap { route ->
            route.properties(includeNested = false).mapNotNull { it.type?.text?.baseTypeName() }
        }.toSet()
        serializable.assertTrue(
            testName = "serializable-placement",
            additionalMessage = "@Serializable — только форматы передачи и хранения (пакеты ..data..), маршруты " +
                "навигации (..navigation.. или NavKey) и типы их аргументов. Доменные и UI-модели не сериализуются. " +
                "docs/patterns.md, «Сериализация»",
        ) { declaration ->
            SERIALIZABLE_PACKAGES.any { declaration.resideInPackage(it) } ||
                declaration in routes ||
                declaration.name in routeArgumentTypes
        }
    }

    @Test
    fun `composables of feature modules live in presentation`() {
        libraryFiles.filter { it.isFeature }
            .flatMap { it.functions(includeNested = false, includeLocal = false) }
            .filter { it.hasAnnotationWithName("Composable") }
            .assertTrue(
                testName = "composable-in-presentation",
                additionalMessage = "@Composable в feature-модуле — только в пакете presentation, включая форматтеры " +
                    "на строках. docs/architecture.md, «Раскладка `presentation/`»",
            ) { it.resideInPackage("..presentation..") }
    }

    @Test
    fun `presentation state classes are immutable`() {
        classes.filter { it.hasDataModifier && it.name.endsWith("State") && it.resideInPackage("..presentation..") }
            .assertTrue(
                testName = "state-immutable",
                additionalMessage = "data class *State в presentation помечается @Immutable или @Stable; коллекции — " +
                    "поля такого класса или ImmutableList. docs/compose-best-prac.md, §3.2",
            ) { it.hasAnnotationWithName("Immutable", "Stable") }
    }

    @Test
    fun `AlarmTrade SDK types stay in data, di and SDK-facing core modules`() {
        val message =
            "Типы AlarmTrade SDK (ru.alarmtrade.common/specialist/devices/permissions/bluetoothControl/…) — " +
                "только data и di фичи, модули $SDK_CORE_MODULES и composition roots; domain и presentation работают " +
                "с моделями фичи. docs/architecture.md, «core/specialist»"
        val files = scope.files.filterNot { it.mayUseSdk }
        files.flatMap { file -> file.imports.filter { it.name.isSdkName() } }
            .assertFalse(testName = "sdk-confinement", additionalMessage = message) { true }
        files.assertFalse(testName = "sdk-confinement", additionalMessage = "$message (полное имя в коде)") {
            it.qualifiedReferences().any { name -> name.isSdkName() }
        }
    }

    @Test
    fun `fakes and stubs live in tests`() {
        scope.classesAndInterfacesAndObjects(includeNested = true)
            .assertFalse(
                testName = "no-fakes-in-production",
                additionalMessage = "Fake*/Stub* — тестовые дублёры: только в тестовых source set'ах. Рабочая " +
                    "заглушка в проде — явный NoOp* (поведение «ничего не делать» — часть контракта).",
            ) { it.name.startsWith("Fake") || it.name.startsWith("Stub") }
    }

    @Test
    fun `testing modules are test-only dependencies`() {
        val violations = buildScripts().flatMap { script ->
            script.testingDependenciesOutsideTests().map { "${script.relativeTo(projectRoot)}:$it" }
        }
        check(violations.isEmpty()) {
            "testing-modules-test-only: модуль тестовых дублёров `*:testing` подключается только в тестовых source " +
                "set'ах (commonTest/jvmTest… .dependencies, *TestImplementation): production-код не зависит от " +
                "фейков. docs/patterns.md, «Тестовые дублёры»\n" + violations.joinToString("\n")
        }
    }

    @Test
    fun `type names use agreed role suffixes`() {
        scope.classesAndInterfacesAndObjects(includeNested = true)
            .assertFalse(
                testName = "forbidden-suffix",
                additionalMessage = "Роль — в имени: *Helper/*Utils запрещены везде (функции-расширения или класс " +
                    "с ролью); *Manager — только платформенная обёртка над нативным API " +
                    "(${PLATFORM_SOURCE_SETS.joinToString()}); *Store/*Storage в фичах — это *Repository " +
                    "(контракт в domain/repository, реализация Settings*Repository в data/repository). " +
                    "docs/patterns.md, «Хранение настроек»",
            ) {
                VAGUE_SUFFIXES.any(it.name::endsWith) ||
                    (it.name.endsWith(MANAGER) && it.containingFile.sourceSetName !in PLATFORM_SOURCE_SETS) ||
                    (STORAGE_SUFFIXES.any(it.name::endsWith) && it.containingFile.isFeature)
            }
    }

    @Test
    fun `mappers live in data mapper and are not public`() {
        libraryFiles.flatMap { it.functions(includeNested = false, includeLocal = false) }
            .filter { MAPPER_NAME.matches(it.name) }
            .assertTrue(
                testName = "mapper-placement",
                additionalMessage = "Маппинг слоёв (toDomain*, to*Dto) — top-level internal/private функция в пакете " +
                    "data.mapper фичи (в core — пакет mapper модуля); presentation ↔ domain — не toDomain, а " +
                    "to<Модель>/to*Ui в presentation. " +
                    "docs/patterns.md, «Data — репозитории»",
            ) {
                val pkg = if (it.containingFile.isFeature) "..data.mapper.." else "..mapper.."
                it.resideInPackage(pkg) && (it.hasInternalModifier || it.hasPrivateModifier)
            }
    }

    @Test
    fun `feature storage access lives in data and di`() {
        libraryFiles.filter { it.isFeature && !it.inLayer("data") && !it.inLayer("di") }
            .flatMap { file -> file.imports.filter { it.name.isStorageImport() } }
            .assertFalse(
                testName = "storage-usage",
                additionalMessage =
                    "Хранилища (multiplatform-settings, AppSettingsFactory из core.common.settings) в " +
                        "фиче — только data и di; domain и presentation получают данные через репозиторий. " +
                        "docs/patterns.md, «Хранение настроек»",
            ) { true }
    }

    @Test
    fun `use cases live in domain usecase`() {
        classes.filter { it.name.endsWith(USE_CASE) }
            .assertTrue(
                testName = "use-case-package",
                additionalMessage = "*UseCase — в feature/<name>/domain/usecase или в пакете usecase core-модуля. " +
                    "docs/patterns.md, «Use case»",
            ) {
                if (it.containingFile.isFeature) {
                    it.resideInPackage(
                        "..domain.usecase..",
                    )
                } else {
                    it.resideInPackage("..usecase..")
                }
            }
    }

    @Test
    fun `features do not use presentation of other features`() {
        val message = "presentation чужого модуля фичи (экраны, VM, компоненты, порты) не используется: общий UI — " +
            "в core/ui, общий presentation фич устройства — в feature/device/presentation-core, экраны другой " +
            "фичи — слотом (@Composable-колбэком) из composition root. docs/patterns.md, «Feature-модули»"
        val files = libraryFiles.filter { it.isFeature }
        files.flatMap { file -> file.imports.filter { it.name.isOtherFeaturePresentation(file) } }
            .assertFalse(testName = "cross-feature-presentation", additionalMessage = message) { true }
        files.assertFalse(
            testName = "cross-feature-presentation",
            additionalMessage = "$message (полное имя в коде)",
        ) { file ->
            file.qualifiedReferences().any { it.isOtherFeaturePresentation(file) }
        }
    }

    @Test
    fun `use cases orchestrate dependencies and return Response`() {
        classes.filter { it.name.endsWith(USE_CASE) }
            .assertTrue(
                testName = "use-case-contract",
                additionalMessage = "Use case — оркестрация ≥ 2 зависимостей (*Repository/*UseCase/*Coordinator/" +
                    "*Controller/*Service/*Session/*Closeable в первичном конструкторе, в том числе List<…> из " +
                    "getAll(); диспетчеры, часы, логгер не считаются) с единственным публичным operator fun invoke: " +
                    "suspend → Response<…>, иначе Flow/StateFlow<Response<…>>. Одна зависимость — VM зовёт " +
                    "репозиторий; чистое правило — класс с ролью (*Validator/*Resolver/*Calculator/…), не *UseCase. " +
                    "docs/patterns.md, «Use case»",
            ) { it.orchestratesDependencies() && it.hasResponseInvoke() }
    }

    @Test
    fun `platform capabilities are used where the call starts`() {
        scope.files.flatMap { file -> file.imports.filter { it.name.isForbiddenCapabilityImport(file) } }
            .assertFalse(
                testName = "capability-usage",
                additionalMessage = "Платформенные возможности: API core.permissions (PermissionsProvider, " +
                    "BindEffect, permissionsModule, blePermissions) и moko-permissions в фиче — только data, di и " +
                    "ViewModel (значения DeviceAccessRequirement/BlePermissionResult — всем); порты " +
                    "presentation/platform фичи — только presentation своего модуля, di и composition roots; " +
                    "общие порты feature/device/presentation-core — presentation любой фичи. " +
                    "docs/patterns.md, «Платформенные возможности»",
            ) { true }
    }

    @Test
    fun `app foreground is gated where the call starts`() {
        scope.files
            .filterNot { file ->
                file.inLayer("data") || file.inLayer("di") || file.moduleName == LIFECYCLE_MODULE ||
                    file.classes(includeNested = false).any { it.extendsViewModelBase() }
            }
            .flatMap { file -> file.imports.filter { it.name.isLifecycleImport() } }
            .assertFalse(
                testName = "foreground-usage",
                additionalMessage = "AppForegroundProvider/AppLifecycle (core.lifecycle) — гейт там, где " +
                    "инициируется вызов: data (репозиторий, сервис) или ViewModel (опрос, привязанный к экрану); не " +
                    "domain и не composable. Koin-модуль core.lifecycle.di подключается где угодно. " +
                    "docs/patterns.md, «Жизненный цикл ViewModel и сессии»",
            ) { true }
    }

    @Test
    fun `top-level properties do not read properties declared later in the file`() {
        scope.files.flatMap { file ->
            val properties = file.properties(includeNested = false)
            properties.mapIndexedNotNull { index, property ->
                val initializer = property.text.stripCommentsAndStrings()
                if (" by lazy" in initializer || GETTER.containsMatchIn(initializer)) return@mapIndexedNotNull null
                val later = properties.drop(index + 1).filterNot { it.hasConstModifier }.map { it.name }
                property.takeIf { later.any { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(initializer) } }
            }
        }.assertFalse(
            testName = "top-level-init-order",
            additionalMessage = "Верхнеуровневое свойство читает свойство, объявленное ниже в том же файле: при " +
                "инициализации файла оно ещё null (на iOS — падение Koin в includes). Перенеси зависимость выше",
        ) { true }
    }

    @Test
    fun `data sources are consumed only by repository implementations`() {
        classes.filter { klass -> !klass.resideInPackage("..data.repository..") && !klass.name.isDataSourceName() }
            .flatMap { it.primaryConstructor?.parameters.orEmpty() }
            .assertFalse(
                testName = "data-source-consumers",
                additionalMessage =
                    "*DataSource получает только реализация репозитория (data/repository) и составной " +
                        "*DataSource; ViewModel, use case и доменные сервисы ходят через репозиторий. " +
                        "docs/patterns.md, «Data — репозитории»",
            ) { it.type.text.baseTypeName().isDataSourceName() }
    }

    @Test
    fun `view models extend BaseViewModel, are named ViewModel and live in presentation`() {
        classes.filter { it.name.endsWith(VIEW_MODEL) || it.extendsViewModelBase() }
            .filterNot { it.name == BASE_VIEW_MODEL }
            .assertTrue(
                testName = "view-model",
                additionalMessage =
                    "ViewModel наследует BaseViewModel (напрямую или через BaseDeviceViewModel), называется " +
                        "*ViewModel, в feature-модуле лежит в presentation. docs/patterns.md, «BaseViewModel»",
            ) {
                it.extendsViewModelBase() &&
                    it.name.endsWith(VIEW_MODEL) &&
                    (!it.containingFile.isFeature || it.resideInPackage("..presentation.."))
            }
    }

    @Test
    fun `view models own no flows - state only through reduce, effects through postSideEffect`() {
        classes.filter { it.extendsViewModelBase() }
            .assertFalse(
                testName = "view-model-owns-no-flows",
                additionalMessage = "ViewModel не держит своих MutableStateFlow/MutableSharedFlow/Channel/" +
                    "mutableStateOf (ни свойством, ни во вложенном классе): state — только reduce, эффекты — " +
                    "postSideEffect. Вход подписки (запрос, фильтр) — поле state + select { }, «Повторить» — " +
                    "reload(). docs/patterns.md, «BaseViewModel»",
            ) { viewModel ->
                OWNED_FLOW.containsMatchIn(viewModel.text.stripCommentsAndStrings()) ||
                    viewModel.properties(includeNested = true).any { it.type?.name?.startsWith("Mutable") == true }
            }
    }

    @Test
    fun `view models depend only on domain contracts, presentation helpers and approved infrastructure`() {
        classes.filter { it.extendsViewModelBase() }
            .flatMap { it.primaryConstructor?.parameters.orEmpty() }
            .assertTrue(
                testName = "view-model-dependencies",
                additionalMessage = "Тип параметра конструктора ViewModel (и аргументы его дженериков) — из " +
                    "разрешённого списка: domain фич (*Repository, *UseCase, доменные сервисы и модели), " +
                    "presentation (аргументы маршрута, порты presentation/platform), core: AppDispatchers " +
                    "(до переноса main-safety в репозитории), AppRestarter, AppVersionProvider, " +
                    "AppForegroundProvider, PermissionsProvider, AppLocaleStore, ConfigurationLanguageStore; Kotlin: " +
                    "примитивы, коллекции, " +
                    "TimeSource; Clock, TimeZoneSource; Logger. " +
                    "Нельзя: data, SDK, Flow, CoroutineScope, функциональные типы. docs/patterns.md, «BaseViewModel»",
            ) { parameter ->
                val type = parameter.type.text
                "->" !in type && TYPE_REFERENCE.findAll(type)
                    .all { parameter.containingFile.qualifiedTypeName(it.value).isAllowedViewModelDependency() }
            }
    }

    @Test
    fun `view model constructors take at most 12 parameters`() {
        classes.filter { it.extendsViewModelBase() }
            .assertFalse(
                testName = "view-model-constructor",
                additionalMessage = "Конструктор ViewModel — не больше $MAX_VIEW_MODEL_PARAMETERS параметров: " +
                    "разросшуюся VM делим по поведению (отдельная VM на независимый блок экрана, use case/доменный " +
                    "сервис, реально объединяющий зависимости). docs/patterns.md, «BaseViewModel»",
            ) { (it.primaryConstructor?.parameters?.size ?: 0) > MAX_VIEW_MODEL_PARAMETERS }
    }

    @Test
    fun `service locator is used only in di, composition roots and platform entry points`() {
        scope.files.filterNot { it.mayUseServiceLocator }
            .flatMap { it.serviceLocatorUsages() }
            .assertFalse(
                testName = "no-service-locator",
                additionalMessage = "Koin как сервис-локатор (KoinComponent/inject/get, GlobalContext, " +
                    "KoinPlatformTools, KoinPlatform, startKoin/loadKoinModules, KoinJavaComponent, getKoin) — " +
                    "только пакеты di, composition roots и платформенные обёртки (${APP_MODULES.joinToString()}) и " +
                    "Android-компоненты, которые создаёт система (Activity/Application/Service/BroadcastReceiver/" +
                    "Worker/Initializer). Остальным зависимости приходят конструктором из Koin-модуля. " +
                    "docs/patterns.md, «Внедрение зависимостей»",
            ) { true }
    }

    @Test
    fun `composables get dependencies from parameters, view models or composition locals`() {
        scope.files.filterNot { it.isCompositionRoot }
            .flatMap { it.apiUsages(String::isComposableInjectionName) }
            .assertFalse(
                testName = "composable-injection",
                additionalMessage = "Composable не достаёт зависимости из Koin (koinInject, currentKoinScope, " +
                    "LocalKoinScope/LocalKoinApplication): данные и действия — через ViewModel (koinViewModel " +
                    "разрешён), параметр или CompositionLocal, заданный в корне композиции " +
                    "(${COMPOSITION_ROOTS.joinToString()}). docs/patterns.md, «Внедрение зависимостей»",
            ) { true }
    }

    @Test
    fun `classes have no constructor side effects`() {
        classes.assertFalse(
            testName = "no-constructor-side-effects",
            additionalMessage = "Создание объекта (Koin-резолв) без побочных эффектов: нет init { } и запуска " +
                "корутин в инициализаторах свойств (launch/async/launchIn без CoroutineStart.LAZY, shareIn/stateIn " +
                "c SharingStarted.Eagerly). Данные — лениво при подписке (WhileSubscribed, onStart), фоновые " +
                "реакции — start() доменного сервиса из AppServicesStarter. init разрешён только платформенным " +
                "мониторам (${INIT_ALLOWED_CLASSES.joinToString()}) для синхронной регистрации в ОС, корутины — " +
                "никому. docs/patterns.md, «Без побочных эффектов в конструкторе»",
        ) { klass ->
            (klass.initBlocks.isNotEmpty() && klass.name !in INIT_ALLOWED_CLASSES) ||
                klass.properties(includeNested = false).any { it.launchesOnConstruction() }
        }
    }

    @Test
    fun `start-once services guard start with CAS`() {
        classes.filter { klass ->
            klass.functions(includeNested = false, includeLocal = false)
                .any { it.name == "start" && it.parameters.isEmpty() }
        }.assertFalse(
            testName = "start-once-cas",
            additionalMessage = "Класс с start() не держит guard в var-свойстве (Job, Boolean, инициализатор " +
                "true/false/launch): check-then-set на var при параллельном start() запускает работу дважды. Guard — " +
                "MutableStateFlow(false).compareAndSet(false, true); с stop() — MutableStateFlow<Job?> + " +
                "compareAndSet(null, job). docs/patterns.md, «Без побочных эффектов в конструкторе»",
        ) { klass -> klass.properties(includeNested = false).any { it.isVar && it.isStartGuard() } }
    }

    // Имя родителя у Konsist включает аргументы типа: `BaseViewModel<S, E>`; наследники BaseDeviceViewModel — тоже VM.
    private fun KoClassDeclaration.extendsViewModelBase(): Boolean =
        parents(indirectParents = true).any { it.name.substringBefore('<') == BASE_VIEW_MODEL }

    private companion object {
        const val MANAGER = "Manager"
        const val VIEW_MODEL = "ViewModel"
        const val BASE_VIEW_MODEL = "BaseViewModel"
        const val MAX_VIEW_MODEL_PARAMETERS = 12
        val OWNED_FLOW = Regex(
            """\b(MutableStateFlow|MutableSharedFlow|Channel|sideEffectFlow|mutableStateOf|mutableStateListOf|""" +
                """mutableStateMapOf|mutableIntStateOf|mutableLongStateOf|mutableFloatStateOf|mutableDoubleStateOf)""" +
                """\s*(<[^()]*>)?\s*\(""",
        )
        const val USE_CASE = "UseCase"
        val VAGUE_SUFFIXES = listOf("Helper", "Helpers", "Util", "Utils")
        val STORAGE_SUFFIXES = listOf("Store", "Storage")
        val PLATFORM_SOURCE_SETS = setOf("androidMain", "iosMain", "jvmMain")
        val GETTER = Regex("""\bget\(\)\s*=""")
        val SERIALIZABLE_PACKAGES = listOf("..data..", "..navigation..")
    }
}

private const val REPOSITORY = "Repository"
private const val IMPL = "Impl"

private val KOIN_MODULE_INITIALIZER = Regex("""=\s*(lazyModule|module)\s*[({]""")

// Вызов конструктора или ссылка на него; объявление `class Foo(` — не создание.
private val CONSTRUCTOR_CALL = Regex("""(?<!\bclass )\b([A-Z]\w*)\s*\(""")
private val CONSTRUCTOR_REFERENCE = Regex("""::([A-Z]\w*)\b""")

// Имя супертипа у Konsist — как написано: с пакетом и аргументами типа.
private fun KoParentDeclaration.simpleName(): String =
    name.substringBefore('<').substringBefore('(').trim().substringAfterLast('.')

// Р8: реализации репозиториев названы по источнику (Specialist*Repository), поэтому ищем по супертипу.
private fun KoClassDeclaration.isImplementation(): Boolean = name.endsWith(IMPL) || isRepositoryImplementation()

private fun KoClassDeclaration.isRepositoryImplementation(): Boolean =
    resideInPackage("..data..") && parents().any { it.simpleName().endsWith(REPOSITORY) }

// Composition roots и платформенные обёртки: имя модуля Konsist — путь от корня репозитория до src/.
private val APP_MODULES = setOf(
    "composeApp",
    "flasherApp",
    "androidApp",
    "desktopApp",
    "flasherAndroidApp",
    "flasherDesktopApp",
)

private val KoFileDeclaration.isCore get() = moduleName.startsWith("core/")
private val KoFileDeclaration.isFeature get() = moduleName.startsWith("feature/")
private val KoFileDeclaration.isTestingModule get() = moduleName.endsWith("/testing")

private val KoFileDeclaration.isTestSource get() = sourceSetName.endsWith("test", ignoreCase = true)

private val KoFileDeclaration.isSharedHelperFile
    get() = moduleName == "core/common" ||
        (moduleName == "core/ui" && packagee?.name?.startsWith("$ROOT.core.ui.lce") == true)

private class UsageFile(
    val path: String,
    val moduleName: String,
    val isTest: Boolean,
    val packageName: String,
    val imports: Set<String>,
    val code: String,
)

// Имя целым словом в коде без import, комментариев и строк, и файл видит хелпер (тот же пакет или import): иначе
// общие имена (`isLoading`, `map`) засчитываются чужими объявлениями. Свой файл и тесты своего модуля — не в счёт.
private fun List<UsageFile>.references(name: String, declaringFile: KoFileDeclaration): Boolean {
    val reference = Regex("""\b${Regex.escape(name)}\b""")
    val helperPackage = declaringFile.packagee?.name.orEmpty()
    return any { file ->
        file.path != declaringFile.path &&
            !(file.isTest && file.moduleName == declaringFile.moduleName) &&
            (
                file.packageName == helperPackage || "$helperPackage.$name" in file.imports ||
                    "$helperPackage.*" in file.imports
                ) &&
            reference.containsMatchIn(file.code)
    }
}

private val projectRoot: File = generateSequence(File("").absoluteFile) { it.parentFile }
    .first { File(it, "settings.gradle.kts").isFile }

private fun buildScripts(): List<File> = (listOf("core", "feature") + APP_MODULES)
    .map { File(projectRoot, it) }
    .filter(File::isDirectory)
    .flatMap { dir ->
        dir.walkTopDown().onEnter { it.name != "build" && it.name != "src" }.filter { it.name == "build.gradle.kts" }
    }

private val TESTING_PROJECT_DEPENDENCY = Regex("""project\(":[\w:-]*:testing"\)""")
private val TEST_SCOPE = Regex("(?i)test")

// Строки (номер: текст) с project(":…:testing") вне тестового блока или тестовой конфигурации: блок — по заголовкам
// открытых `{`, тестовый — в заголовке или в самой строке есть «test» (commonTest.dependencies, testImplementation).
private fun File.testingDependenciesOutsideTests(): List<String> {
    val blocks = ArrayDeque<String>()
    return readLines().withIndex().mapNotNull { (index, line) ->
        val code = line.substringBefore("//")
        val violation = TESTING_PROJECT_DEPENDENCY.containsMatchIn(code) &&
            blocks.none(TEST_SCOPE::containsMatchIn) && !TEST_SCOPE.containsMatchIn(code.substringBefore("project("))
        var rest = code
        while (rest.isNotEmpty()) {
            val open = rest.indexOf('{')
            val close = rest.indexOf('}')
            when {
                open >= 0 && (close < 0 || open < close) -> {
                    blocks.addLast(rest.substring(0, open).trim())
                    rest = rest.substring(open + 1)
                }

                close >= 0 -> {
                    blocks.removeLastOrNull()
                    rest = rest.substring(close + 1)
                }

                else -> rest = ""
            }
        }
        if (violation) "${index + 1}: ${line.trim()}" else null
    }
}

// feature/auth-state → …feature.authstate; суффиксы модуля -ui и -core — подпакет: feature/device/settings-ui →
// …settings.ui, feature/device/presentation-core → …presentation.core
private val KoFileDeclaration.basePackage
    get() = "$ROOT." + moduleName.replace('/', '.').replace(Regex("-(ui|core)$"), ".$1").replace("-", "")

private fun KoFileDeclaration.inLayer(layer: String) = ".$layer." in ".${packagee?.name}."

// Слой проекта в имени: ru.alarmtrade.specmp.<core|feature>.<module>.….<layer>.…
private fun String.isProjectLayer(layer: String) = startsWith("$ROOT.") && ".$layer." in "$this."

private fun String.isForbiddenProjectImport(file: KoFileDeclaration): Boolean {
    if (!startsWith("$ROOT.")) return false
    val allowed = if (file.isCore) listOf("$ROOT.core.") else listOf("$ROOT.core.", "$ROOT.feature.")
    return allowed.none { startsWith(it) }
}

private fun String.isForbiddenInDomain(): Boolean = isProjectLayer("data") ||
    isProjectLayer("presentation") ||
    isProjectLayer("di") ||
    isSdkName() ||
    startsWith("androidx.compose.") ||
    startsWith("org.jetbrains.compose.") ||
    ".generated.resources." in this ||
    DOMAIN_FORBIDDEN_PREFIXES.any(::startsWith)

private val DOMAIN_FORBIDDEN_PREFIXES = listOf(
    "$ROOT.core.ui.",
    "com.russhwolf.",
    "kotlinx.serialization.",
    "io.ktor.",
)

// Где пакет ..data.. недоступен: фича — вне data/di, core — presentation, приложения — вне di.
private val KoFileDeclaration.mayNotUseDataLayer
    get() = when {
        isFeature -> !inLayer("data") && !inLayer("di")
        isCore -> inLayer("presentation")
        else -> !inLayer("di")
    }

// ru.alarmtrade.specmp.feature.<other>.….data.… из файла фичи <this>
private fun String.isOtherFeatureData(file: KoFileDeclaration): Boolean =
    startsWith("$ROOT.feature.") && !startsWith(file.basePackage + ".") && isProjectLayer("data")

private fun String.isDataSourceName(): Boolean = endsWith("DataSource") || endsWith("DataSourceImpl")

private fun String.isServiceName(): Boolean = endsWith("Coordinator") || endsWith("Controller")

private fun KoClassAndInterfaceAndObjectDeclaration.isPlacedByRole(): Boolean {
    val name = name
    return when {
        name.endsWith(REPOSITORY) && this !is KoInterfaceDeclaration -> resideInPackage("..data.repository..")

        name.isDataSourceName() -> resideInPackage("..data.datasource..")

        name.endsWith("CoordinatorImpl") || name.endsWith("ControllerImpl") ->
            resideInPackage("..data.service..") || resideInPackage("..platform..")

        name.isServiceName() -> this is KoInterfaceDeclaration && resideInPackage("..domain.service..")

        else -> true
    }
}

private val MUTABLE_COLLECTION_TYPE =
    Regex("""\b(Mutable\w*|ArrayList|HashMap|HashSet|LinkedHashMap|LinkedHashSet|ArrayDeque)\b""")

private fun String?.isMutableCollectionType(): Boolean = this != null && MUTABLE_COLLECTION_TYPE.containsMatchIn(this)

// Корни композиции: здесь composable собирают граф и передают зависимости дальше параметрами.
private val COMPOSITION_ROOTS = setOf("composeApp", "flasherApp")

// SDK AlarmTrade — всё под ru.alarmtrade.*, кроме пакетов самого приложения.
private fun String.isSdkName(): Boolean = startsWith("ru.alarmtrade.") && !startsWith("$ROOT.") && this != ROOT

// core-модули, которые оборачивают SDK (сессия, разрешения, сеть) и отдают остальным свои типы.
private val SDK_CORE_MODULES = setOf("core/specialist", "core/permissions", "core/network")

private val KoFileDeclaration.mayUseSdk
    get() = moduleName in SDK_CORE_MODULES ||
        moduleName in APP_MODULES ||
        (isFeature && (inLayer("data") || inLayer("di")))

// Маршрут Navigation 3: имя супертипа у Konsist — как написано (`NavKey`, без аргументов типа).
private fun KoClassAndInterfaceAndObjectDeclaration.isNavKey(): Boolean =
    parents().any { it.name.substringBefore('<').substringAfterLast('.') == "NavKey" }

// `kotlinx.coroutines.flow.StateFlow<Foo>?` → `StateFlow`.
private fun String.baseTypeName(): String = trim().removeSuffix("?").substringBefore('<').substringAfterLast('.').trim()

// Контракт (docs/patterns.md, «Domain»): suspend → Response<…>, Flow/StateFlow/SharedFlow → …<Response<…>>.
// Синхронный член (не suspend, не поток) — нарушение, кроме функции-команды с результатом из syncCommandTypes
// (у сервисов — Unit/Boolean: start, «закрыть в фоне»); у репозиториев команд нет.
private fun List<KoInterfaceDeclaration>.assertResultContract(
    testName: String,
    role: String,
    docs: String,
    syncCommandTypes: Set<String> = emptySet(),
) {
    val functions: List<KoBaseDeclaration> = flatMap { it.functions(includeNested = false, includeLocal = false) }
    val properties: List<KoBaseDeclaration> = flatMap { it.properties(includeNested = false) }
    val syncRule = if (syncCommandTypes.isEmpty()) {
        " Иных членов нет: синхронный снимок — StateFlow<Response<T>>.value, чтение/запись — suspend … Response<T>."
    } else {
        " Синхронная функция — только команда с результатом ${syncCommandTypes.joinToString("/")}; " +
            "данные — suspend … Response<T> или поток, синхронных свойств-не-потоков нет."
    }
    (functions + properties).assertTrue(
        testName = testName,
        additionalMessage = "Члены $role: suspend → Response<…>, Flow/StateFlow/SharedFlow → …<Response<…>>." +
            "$syncRule $docs",
    ) {
        when (it) {
            is KoFunctionDeclaration -> when {
                it.hasSuspendModifier -> it.returnType?.text?.baseTypeName() == RESPONSE
                it.returnType?.text.isFlowType() -> it.returnType?.text.isResponseFlow()
                else -> it.syncResultType() in syncCommandTypes
            }

            is KoPropertyDeclaration -> it.type?.text.isFlowType() && it.type?.text.isResponseFlow()

            else -> true
        }
    }
}

// Результат синхронной функции как написан (без `kotlin.`); без явного типа — Unit, кроме выводимого из
// expression body (тип неизвестен — не команда). `Boolean?` — не команда.
private fun KoFunctionDeclaration.syncResultType(): String? =
    returnType?.text?.trim()?.removePrefix("kotlin.") ?: UNIT.takeUnless { hasExpressionBody }

private const val UNIT = "Unit"
private val SERVICE_SYNC_COMMAND_TYPES = setOf(UNIT, "Boolean")

// Платформенные мониторы сети регистрируют колбэк ОС синхронно при создании.
private val INIT_ALLOWED_CLASSES = setOf("AndroidNetworkMonitor", "IosNetworkMonitor")

// Инициализатор свойства исполняется в конструкторе; геттер, `by lazy` и LAZY-корутина до start() — нет.
private fun KoPropertyDeclaration.launchesOnConstruction(): Boolean {
    if (hasGetter || delegateName == "lazy") return false
    val code = text.stripCommentsAndStrings()
    return CONSTRUCTOR_LAUNCH.containsMatchIn(code) && LAZY_START !in code
}

private val CONSTRUCTOR_LAUNCH = Regex("""\b(launch|async|launchIn)\s*[({]|SharingStarted\.Eagerly\b""")
private const val LAZY_START = "CoroutineStart.LAZY"

private val START_GUARD_TYPES = setOf("Job", "Boolean")
private val START_GUARD_INITIALIZER = Regex("""^(true|false)$|\b(launch|launchIn)\b""")

private fun KoPropertyDeclaration.isStartGuard(): Boolean = type?.text?.baseTypeName() in START_GUARD_TYPES ||
    value?.stripCommentsAndStrings()?.trim()?.let(START_GUARD_INITIALIZER::containsMatchIn) == true

private const val RESPONSE = "Response"
private val FLOW_TYPES = setOf("Flow", "StateFlow", "SharedFlow")

private fun String?.isFlowType(): Boolean = this != null && baseTypeName() in FLOW_TYPES

private fun String?.isResponseFlow(): Boolean =
    this != null && substringAfter('<').substringBeforeLast('>').baseTypeName() == RESPONSE

private val QUALIFIED_NAME = Regex("""\b[A-Za-z_]\w*(?:\.[A-Za-z_]\w*)+""")

// Квалифицированные имена в коде файла: без package/import, комментариев (включая KDoc) и строковых литералов.
private fun KoFileDeclaration.qualifiedReferences(): List<String> = QUALIFIED_NAME
    .findAll(codeWithoutHeaderCommentsAndStrings())
    .map { it.value }
    .toList()

private fun KoFileDeclaration.codeWithoutHeaderCommentsAndStrings(): String =
    text.stripCommentsAndStrings().lineSequence().filterNot { it.isHeaderLine() }.joinToString("\n")

private fun String.isHeaderLine(): Boolean = trimStart().let { it.startsWith("package ") || it.startsWith("import ") }

// Комментарии и литералы заменяются пробелом (переводы строк внутри блочных комментариев сохраняются,
// чтобы package/import оставались на своих строках). Выражения в шаблонах строк выпадают вместе со строкой.
private fun String.stripCommentsAndStrings(): String {
    val out = StringBuilder(length)
    var i = 0
    while (i < length) {
        val end = when {
            startsWith("//", i) -> indexOf('\n', i).takeIf { it >= 0 } ?: length

            startsWith("/*", i) -> indexOf("*/", i + 2).takeIf { it >= 0 }?.plus(2) ?: length

            startsWith("\"\"\"", i) -> indexOf("\"\"\"", i + 3).takeIf { it >= 0 }?.plus(3) ?: length

            this[i] == '"' -> stringLiteralEnd(i)

            this[i] == '\'' && i + 2 < length && (this[i + 1] == '\\' || this[i + 2] == '\'') ->
                indexOf('\'', if (this[i + 1] == '\\') i + 3 else i + 2).takeIf { it >= 0 }?.plus(1) ?: length

            else -> -1
        }
        if (end < 0) {
            out.append(this[i++])
        } else {
            out.append(' ').append(substring(i, end).filter { it == '\n' })
            i = end
        }
    }
    return out.toString()
}

private fun String.stringLiteralEnd(start: Int): Int {
    var i = start + 1
    while (i < length && this[i] != '"' && this[i] != '\n') i += if (this[i] == '\\') 2 else 1
    return minOf(i + 1, length)
}

private val TYPE_REFERENCE = Regex("""\b(?:[a-z_]\w*\.)*[A-Z]\w*(?:\.[A-Z]\w*)*""")

// Типы, видимые без импорта (пакеты kotlin, kotlin.collections).
private val KOTLIN_DEFAULT_TYPES = setOf(
    "Any", "Boolean", "Byte", "Char", "Double", "Float", "Int", "Long", "Number", "Short", "String", "Unit",
    "Array", "Collection", "Iterable", "List", "Map", "Pair", "Sequence", "Set", "Triple",
)

// Полное имя типа по ссылке из файла: полное имя в коде, импорт (с алиасом), тип Kotlin по умолчанию,
// иначе — тип из пакета файла (в том числе вложенный в класс этого файла).
private fun KoFileDeclaration.qualifiedTypeName(reference: String): String {
    if (reference.first().isLowerCase()) return reference
    val simple = reference.substringBefore('.')
    val nested = reference.removePrefix(simple)
    val import = imports.firstOrNull {
        !it.name.endsWith(".*") && (it.alias?.name ?: it.name.substringAfterLast('.')) == simple
    }
    return when {
        import != null -> import.name + nested
        simple in KOTLIN_DEFAULT_TYPES -> "kotlin.$reference"
        else -> listOfNotNull(packagee?.name, reference).joinToString(".")
    }
}

// Зависимости ViewModel вне domain/presentation фич: инфраструктура, которую VM получает из DI напрямую.
private val VIEW_MODEL_DEPENDENCY_TYPES = setOf(
    "$ROOT.core.common.AppDispatchers",
    "$ROOT.core.common.AppRestarter",
    "$ROOT.core.common.AppVersionProvider",
    "$ROOT.core.lifecycle.AppForegroundProvider",
    "$ROOT.core.permissions.PermissionsProvider",
    "$ROOT.core.localization.AppLocaleStore",
    // Как AppLocaleStore: настройка приложения из core/common, реализация — core/specialist; доменной обёртки нет.
    "$ROOT.core.common.ConfigurationLanguageStore",
    "kotlin.time.TimeSource",
    "kotlinx.datetime.Clock",
    "$ROOT.core.common.time.TimeZoneSource",
    "co.touchlab.kermit.Logger",
)

private fun String.isAllowedViewModelDependency(): Boolean {
    val segments = split('.')
    val pkg = segments.takeWhile { it.first().isLowerCase() }.joinToString(".")
    val featureLayer = pkg.startsWith("$ROOT.feature.") && ("domain" in segments || "presentation" in segments)
    return this in VIEW_MODEL_DEPENDENCY_TYPES || pkg == "kotlin" || featureLayer
}

// API Koin для доставания зависимостей вне графа: пакеты (`pkg.`) и отдельные декларации.
private val SERVICE_LOCATOR_PACKAGES = listOf(
    "org.koin.core.component.",
    "org.koin.core.context.",
    "org.koin.android.ext.android.",
)
private val SERVICE_LOCATOR_NAMES = setOf(
    "org.koin.mp.KoinPlatformTools",
    "org.koin.mp.KoinPlatform",
    "org.koin.java.KoinJavaComponent",
    "org.koin.compose.getKoin",
    "org.koin.core.Koin",
)

private fun String.isServiceLocatorName(): Boolean {
    val name = removeSuffix(".*")
    return SERVICE_LOCATOR_PACKAGES.any { name.startsWith(it) || "$name." == it } ||
        SERVICE_LOCATOR_NAMES.any { name == it || name.startsWith("$it.") }
}

// Компоненты, которые создаёт система: конструктора для DI у них нет.
private val ANDROID_ENTRY_POINTS = setOf(
    "Activity",
    "ComponentActivity",
    "AppCompatActivity",
    "FragmentActivity",
    "Application",
    "Service",
    "LifecycleService",
    "BroadcastReceiver",
    "ListenableWorker",
    "CoroutineWorker",
    "Worker",
    "Initializer",
)

private val KoFileDeclaration.mayUseServiceLocator: Boolean
    get() = inLayer("di") ||
        moduleName in APP_MODULES ||
        classes(includeNested = true).any { type ->
            type.parents().any { it.name.substringBefore('<').substringBefore('(').trim() in ANDROID_ENTRY_POINTS }
        }

private val KoFileDeclaration.isCompositionRoot: Boolean
    get() = moduleName in COMPOSITION_ROOTS

private fun KoFileDeclaration.serviceLocatorUsages(): List<KoBaseDeclaration> =
    apiUsages(String::isServiceLocatorName) { name -> "$name." in SERVICE_LOCATOR_PACKAGES }

// Koin-API для зависимостей прямо в composable; `koinViewModel` (org.koin.compose.viewmodel) сюда не входит.
private val COMPOSABLE_INJECTION_NAMES = setOf(
    "org.koin.compose.koinInject",
    "org.koin.compose.currentKoinScope",
    "org.koin.compose.LocalKoinScope",
    "org.koin.compose.LocalKoinApplication",
)

private fun String.isComposableInjectionName(): Boolean = removeSuffix(".*") == "org.koin.compose" ||
    COMPOSABLE_INJECTION_NAMES.any { this == it || startsWith("$it.") }

/**
 * Декларации верхнего уровня, упоминающие API (по простому имени/алиасу импорта или полным именем), — чтобы
 * `@Suppress` ставился на объект, а не на файл; импорт со звёздочкой или пакета целиком ([isPackageImport]) и
 * неиспользованный импорт возвращаются самим импортом.
 */
private fun KoFileDeclaration.apiUsages(
    isApiName: (String) -> Boolean,
    isPackageImport: (String) -> Boolean = { false },
): List<KoBaseDeclaration> {
    val apiImports = imports.filter { isApiName(it.name) }
    val starImports = apiImports.filter { it.name.endsWith(".*") || isPackageImport(it.name) }
    val names = (apiImports - starImports.toSet()).map { it.alias?.name ?: it.name.substringAfterLast('.') }.toSet()
    val usages = (
        classesAndInterfacesAndObjects(includeNested = false) +
            functions(includeNested = false, includeLocal = false) +
            properties(includeNested = false)
        ).filter { declaration ->
        val code = declaration.text.stripCommentsAndStrings()
        QUALIFIED_NAME.findAll(code).any { isApiName(it.value) } ||
            IDENTIFIER.findAll(code).any { it.value in names }
    }
    return starImports + usages.ifEmpty { apiImports - starImports.toSet() }
}

private val IDENTIFIER = Regex("""\b[A-Za-z_]\w*\b""")

private val MAPPER_NAME = Regex("""toDomain\w*|toDto|to\w*Dto""")

private fun String.isStorageImport(): Boolean =
    startsWith("com.russhwolf.settings.") || startsWith("$ROOT.core.common.settings.")

private val USE_CASE_DEPENDENCY_SUFFIXES =
    listOf("Repository", "UseCase", "Coordinator", "Controller", "Service", "Session", "Closeable")

// `List<AuthCloseable>` из getAll() — тоже зависимость: считаем по типу элемента.
private fun KoClassDeclaration.orchestratesDependencies(): Boolean =
    primaryConstructor?.parameters.orEmpty().count { parameter ->
        val type = parameter.type.text
        val element = if (type.baseTypeName() in
            COLLECTION_TYPES
        ) {
            type.substringAfter('<').substringBeforeLast('>')
        } else {
            type
        }
        USE_CASE_DEPENDENCY_SUFFIXES.any(element.baseTypeName()::endsWith)
    } >= 2

private val COLLECTION_TYPES = setOf("List", "Set", "Collection")

private fun KoClassDeclaration.hasResponseInvoke(): Boolean {
    val functions = functions(includeNested = false, includeLocal = false)
    val invokes = functions.filter { it.name == "invoke" && it.hasOperatorModifier }
    val otherPublic = functions.filter { it.name != "invoke" && !it.hasPrivateModifier && !it.hasInternalModifier }
    return invokes.isNotEmpty() && otherPublic.isEmpty() && invokes.all {
        val returnType = it.returnType?.text
        when {
            it.hasSuspendModifier -> returnType?.baseTypeName() == RESPONSE
            else -> returnType.isFlowType() && returnType.isResponseFlow()
        }
    }
}

private const val LIFECYCLE_MODULE = "core/lifecycle"
private const val LIFECYCLE_PACKAGE = "$ROOT.core.lifecycle"

// Импорт со звёздочкой Konsist отдаёт по-разному (`pkg.*` или `pkg`) — ловим оба.
private fun String.isLifecycleImport(): Boolean = removeSuffix(".*").let { name ->
    (name == LIFECYCLE_PACKAGE || name.startsWith("$LIFECYCLE_PACKAGE.")) && !name.startsWith("$LIFECYCLE_PACKAGE.di")
}

// API core.permissions, которое обращается к ОС; значения (DeviceAccessRequirement, BlePermissionResult) — не оно.
private val PERMISSIONS_API = setOf("PermissionsProvider", "BindEffect", "permissionsModule", "blePermissions")
    .map { "$ROOT.core.permissions.$it" }
private const val MOKO_PERMISSIONS = "dev.icerock.moko.permissions"

// Общий presentation фич устройства: компоненты и порты, которые подключают несколько фич.
private const val SHARED_PRESENTATION_PACKAGE = "$ROOT.feature.device.presentation.core"

private fun String.isOtherFeaturePresentation(file: KoFileDeclaration): Boolean = startsWith("$ROOT.feature.") &&
    !startsWith(file.basePackage + ".") &&
    isProjectLayer("presentation") &&
    !startsWith("$SHARED_PRESENTATION_PACKAGE.")

private fun String.isForbiddenCapabilityImport(file: KoFileDeclaration): Boolean {
    if (!file.isFeature) return false
    val name = removeSuffix(".*")
    val inViewModelFile = file.classes(includeNested = false).any { klass ->
        klass.parents(indirectParents = true).any { it.name.substringBefore('<') == "BaseViewModel" }
    }
    val permissionsApi = name in PERMISSIONS_API || name == MOKO_PERMISSIONS || name.startsWith("$MOKO_PERMISSIONS.")
    if (permissionsApi) return !file.inLayer("data") && !file.inLayer("di") && !inViewModelFile
    val port = name.substringBefore(".platform.", missingDelimiterValue = "").takeIf { ".presentation" in it }
        ?.plus(".platform") ?: return false
    if (!port.startsWith("$ROOT.feature.") || file.inLayer("di")) return false
    val ownPresentation = port.startsWith(file.basePackage + ".") && file.inLayer("presentation")
    val sharedPort = port.startsWith("$SHARED_PRESENTATION_PACKAGE.") && file.inLayer("presentation")
    return !ownPresentation && !sharedPort
}
