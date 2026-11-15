package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.declaration.KoBaseDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.provider.modifier.KoVisibilityModifierProvider
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue

internal fun ArchScope.repositoryContract() {
    production.interfaces(includeNested = true)
        .filter { it.name.endsWith(REPOSITORY) || it.resideInPackage("..domain.repository..") }
        .assertResultContract(
            testName = "repository-contract",
            role = "репозитория (interface *Repository, интерфейсы domain/repository)",
            docs = "docs/patterns.md, «Domain»",
        )
}

internal fun ArchScope.contractMemberUsage() {
    val contracts = production.interfaces(includeNested = true).filter {
        it.name.endsWith(REPOSITORY) || it.name.isServiceName() ||
            it.resideInPackage("..domain.repository..") || it.resideInPackage("..domain.service..")
    }
    val implementers = production.classesAndObjects(includeNested = true)
        .flatMap { type -> type.parents().map { it.name.substringBefore('<') to type.containingFile.path } }
        .groupBy({ it.first }, { it.second })
    val code = production.files.associate { it.path to it.codeWithoutHeaderCommentsAndStrings() }
    val message = "Член контракта (интерфейс *Repository/*Coordinator/*Controller, domain/repository, " +
        "domain/service) вызывается в production-коде вне интерфейса и его реализаций; мёртвый метод " +
        "удаляется из контракта, реализаций и фейков. docs/patterns.md, «Domain»"
    val unused = contracts.flatMap { contract ->
        val own = implementers[contract.name].orEmpty().toSet() + contract.containingFile.path
        val callers = code.filterKeys { it !in own }.values
        val members: List<KoBaseDeclaration> =
            contract.functions(includeNested = false) + contract.properties(includeNested = false)
        // Функция может вызываться без получателя — в лямбде с ресивером (`updateOutbox { markSent(id) }`).
        members.filterNot { member ->
            val reference = when (member) {
                is KoFunctionDeclaration -> Regex("""(\.|::)${Regex.escape(member.name)}\b|\b${member.name}\s*\(""")
                else -> Regex("""(\.|::)${Regex.escape((member as KoPropertyDeclaration).name)}\b""")
            }
            callers.any { reference.containsMatchIn(it) }
        }
    }
    unused.assertFalse(testName = "contract-member-usage", additionalMessage = message) { true }
}

internal fun ArchScope.serviceContract() {
    production.interfaces(includeNested = true)
        .filter { it.name.isServiceName() || it.resideInPackage("..domain.service..") }
        .assertResultContract(
            testName = "service-contract",
            role = "доменного сервиса (interface *Coordinator/*Controller, интерфейсы domain/service)",
            docs = "docs/patterns.md, «Доменные сервисы»",
            syncCommandTypes = SERVICE_SYNC_COMMAND_TYPES,
            nullableResponse = true,
        )
}

internal fun ArchScope.dataVisibility() {
    libraryFiles.filter { it.inLayer("data") }
        .flatMap { it.declarations(includeNested = false, includeLocal = false) }
        .filterIsInstance<KoVisibilityModifierProvider>()
        .assertTrue(
            testName = "data-visibility",
            additionalMessage = "Декларации верхнего уровня в ..data.. — internal/private: наружу модуля видны " +
                "только контракты domain, привязка — в Koin-модуле. docs/patterns.md, «Data — репозитории»",
        ) { it.hasInternalModifier || it.hasPrivateModifier }
}

internal fun ArchScope.implVisibility() {
    classes.filter { it.isImplementation() && (it.containingFile.isCore || it.containingFile.isFeature) }
        .assertTrue(
            testName = "impl-visibility",
            additionalMessage = "Реализация (*Impl, класс в ..data.., реализующий *Repository, — " +
                "Specialist*Repository) — internal: снаружи видна только абстракция, привязка — в Koin-модуле. " +
                "docs/patterns.md, «Data — репозитории»",
        ) { it.hasInternalModifier || it.hasPrivateModifier }
}

internal fun ArchScope.repositoryImpl() {
    classes.filter { it.name.removeSuffix(IMPL).endsWith(REPOSITORY) }
        .assertTrue(
            testName = "repository-impl",
            additionalMessage = "Класс *Repository реализует свой интерфейс: имя = <источник><Интерфейс> " +
                "(SpecialistFooRepository : FooRepository) или FooRepositoryImpl : FooRepository. " +
                "docs/patterns.md, «Data — репозитории»",
        ) { klass ->
            klass.parents().map { it.simpleName() }
                .any { it != REPOSITORY && it.endsWith(REPOSITORY) && klass.name.removeSuffix(IMPL).endsWith(it) }
        }
}

internal fun ArchScope.repositoryImplTested() {
    val testCodeByModule = project.files
        .filter { it.isTestSource }
        .groupBy({ it.modulePath }, { it.codeWithoutHeaderCommentsAndStrings() })
    classes.filter { it.isRepositoryImplementation() }
        .assertTrue(
            testName = "repository-impl-tested",
            additionalMessage = "У реализации репозитория (Specialist*Repository, Settings*Repository, " +
                "*RepositoryImpl) есть тест в тестовом source set того же модуля, который использует класс по " +
                "имени в коде (не в import, KDoc или строке). docs/patterns.md, «Тестирование репозиториев»",
        ) { impl ->
            val reference = Regex("""\b${impl.name}\b""")
            testCodeByModule[impl.containingFile.modulePath].orEmpty().any { reference.containsMatchIn(it) }
        }
}

internal fun ArchScope.coreHelperUsage() {
    val usageFiles = project.files
        .filter { it.isCore || it.isFeature || it.modulePath in APP_MODULES }
        .map { file ->
            UsageFile(
                path = file.path,
                modulePath = file.modulePath,
                isTest = file.isTestSource,
                packageName = file.packagee?.name.orEmpty(),
                imports = file.imports.map { it.name }.toSet(),
                code = file.codeWithoutHeaderCommentsAndStrings(),
            )
        }
    val message = "Публичная top-level функция/свойство core/common и core/ui/lce вызывается вне своего файла " +
        "(production или тесты других модулей; собственные тесты модуля не считаются). Мёртвый хелпер удаляется " +
        "вместе с его тестом. docs/patterns.md, «Core»"
    val helperFiles = production.files.filter { it.isSharedHelperFile }
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

internal fun ArchScope.mapperPlacement() {
    // Член класса — допустимый приватный маппинг внутри data (репозиторий); вне data фичи — тот же обход размещения.
    val members = libraryFiles.filter { it.isFeature && !it.isInLayer("data") }
        .flatMap { it.functions(includeNested = true, includeLocal = false) }.filterNot { it.isTopLevel }
    (libraryFiles.flatMap { it.functions(includeNested = false, includeLocal = false) } + members)
        .filter { MAPPER_NAME.matches(it.name) }
        .assertTrue(
            testName = "mapper-placement",
            additionalMessage = "Маппинг слоёв (toDomain*, to*Dto) — top-level internal/private функция в пакете " +
                "data.mapper фичи (в core — пакет mapper модуля); presentation ↔ domain — не toDomain, а " +
                "to<Модель>/to*Ui в presentation. " +
                "docs/patterns.md, «Data — репозитории»",
        ) {
            val file = it.containingFile
            val placed = if (file.isFeature) file.isInLayer("data", "mapper") else it.resideInPackage("..mapper..")
            placed && it.isTopLevel && (it.hasInternalModifier || it.hasPrivateModifier)
        }
}

internal fun ArchScope.useCasePackage() {
    classes.filter { it.name.endsWith(USE_CASE) }
        .assertTrue(
            testName = "use-case-package",
            additionalMessage = "*UseCase — в feature/<name>/domain/usecase или в пакете usecase core-модуля. " +
                "docs/patterns.md, «Use case»",
        ) {
            if (it.containingFile.isFeature) {
                it.containingFile.isInLayer("domain", "usecase")
            } else {
                it.resideInPackage("..usecase..")
            }
        }
}

internal fun ArchScope.useCaseContract() {
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

internal fun ArchScope.dataSourceConsumers() {
    classes.filter { klass -> !klass.containingFile.isInLayer("data", "repository") && !klass.name.isDataSourceName() }
        .flatMap { klass -> klass.constructors.flatMap { it.parameters } }
        .assertFalse(
            testName = "data-source-consumers",
            additionalMessage =
                "*DataSource получает только реализация репозитория (data/repository) и составной " +
                    "*DataSource; ViewModel, use case и доменные сервисы ходят через репозиторий. " +
                    "docs/patterns.md, «Data — репозитории»",
        ) { parameter -> parameter.type.text.typeNames().any { it.isDataSourceName() } }
}
