package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.combined.KoClassAndInterfaceAndObjectDeclaration
import com.lemonappdev.konsist.api.provider.KoAnnotationProvider
import com.lemonappdev.konsist.api.provider.KoContainingFileProvider
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue

internal fun ArchScope.domainModelPurity() {
    val modelFiles = libraryFiles.filter { ".domain.model." in ".${it.packagee?.name}." }
    val types = modelFiles.flatMap { it.classesAndInterfacesAndObjects(includeNested = true) }
    val properties = modelFiles.flatMap { it.properties(includeNested = false) } +
        types.flatMap { it.properties(includeNested = false) }
    val message = "Модель domain/model — неизменяемое значение: свойства только val, коллекции read-only " +
        "(List/Set/Map, не Mutable*/ArrayList/HashMap…), без @Serializable (формат передачи — DTO в data). " +
        "docs/patterns.md, «Domain»"
    properties.assertFalse(testName = "domain-model-purity", additionalMessage = message) {
        it.isVar || it.type?.text.isMutableCollectionType() ||
            it.type?.text.mentionsAny(it.containingFile.aliasesOf { name -> name.isMutableCollectionType() })
    }
    modelFiles.flatMap { it.typeAliases }
        .assertFalse(testName = "domain-model-purity", additionalMessage = message) {
            it.text.substringAfter('=').isMutableCollectionType()
        }
    types.assertFalse(testName = "domain-model-purity", additionalMessage = message) { it.isSerializable() }
}

internal fun ArchScope.domainModelUiText() {
    val types = libraryFiles.filter { ".domain.model." in ".${it.packagee?.name}." }
        .flatMap { it.classesAndInterfacesAndObjects(includeNested = true) }
    val message = "Поле модели domain/model — данные, не UI-текст: без display*/formatted* и строковых *Text; " +
        "подпись собирает presentation из stringResource. Текст, который присылает SDK, — @Suppress с причиной. " +
        "docs/patterns.md, «Domain»"
    types.filterIsInstance<KoClassDeclaration>().flatMap { it.primaryConstructor?.parameters.orEmpty() }
        .assertFalse(testName = "domain-model-ui-text", additionalMessage = message) {
            isUiTextField(it.name, it.type.text.withoutKotlinPrefix())
        }
    types.flatMap { it.properties(includeNested = false) }
        .assertFalse(testName = "domain-model-ui-text", additionalMessage = message) {
            isUiTextField(it.name, it.type?.text.orEmpty().withoutKotlinPrefix())
        }
}

internal fun ArchScope.serializablePlacement() {
    val serializable = production.classesAndInterfacesAndObjects(includeNested = true).filter { it.isSerializable() }
    val routes = serializable.filter { it.isNavKey() && !it.containingFile.inModelLayer() }
    val routeArgumentTypes = routes.flatMap { route -> route.argumentTypeCandidates() }.toSet()
    serializable.assertTrue(
        testName = "serializable-placement",
        additionalMessage = "@Serializable — только форматы передачи и хранения (пакеты ..data..), маршруты " +
            "навигации (..navigation.. или NavKey) и типы их аргументов. Доменные и UI-модели не сериализуются. " +
            "docs/patterns.md, «Сериализация»",
    ) { declaration ->
        declaration.inSerializablePackage() ||
            declaration in routes ||
            declaration.fullyQualifiedName in routeArgumentTypes
    }
}

internal fun ArchScope.composableInPresentation() {
    libraryFiles.filter { it.isFeature }
        .flatMap { it.functions(includeNested = true, includeLocal = false) }
        .filter { it.hasAnnotationWithName("Composable") }
        .assertTrue(
            testName = "composable-in-presentation",
            additionalMessage = "@Composable в feature-модуле — только в пакете presentation, включая форматтеры " +
                "на строках. docs/architecture.md, «Раскладка `presentation/`»",
        ) { it.resideInPackage("..presentation..") && !it.containingFile.inModelLayer() }
}

internal fun ArchScope.stateImmutable() {
    classes.filter { it.name.endsWith("State") && it.resideInPackage("..presentation..") && it.isStateValue() }
        .assertTrue(
            testName = "state-immutable",
            additionalMessage = "data class *State в presentation помечается @Immutable или @Stable; коллекции — " +
                "поля такого класса или ImmutableList. docs/compose-best-prac.md, §3.2",
        ) { state -> state.annotations.any { it.fullyQualifiedName in STABILITY_ANNOTATIONS } }
}

internal fun ArchScope.stateTypedErrors() {
    val contracts = production.files
        .filter { it.inLayer("presentation") }
        .flatMap { file ->
            val isContractFile = CONTRACT_FILE.matches(file.name)
            file.classesAndInterfacesAndObjects(includeNested = false)
                .filter { isContractFile || CONTRACT_FILE.matches(it.name) }
                .flatMap { listOfNotNull(it as? KoClassDeclaration) + it.classes(includeNested = true) }
        }
    val message = "State и Effect экрана несут ошибку типом фичи (enum/sealed, AppError), не " +
        "Throwable/Exception/Result и не Response: UI не разбирает исключения. docs/patterns.md, «Ошибки фичи»"
    fun String?.isRawError(file: KoFileDeclaration) =
        this != null && (RAW_STATE_ERROR.containsMatchIn(this) || mentionsAny(file.aliasesOf(RAW_STATE_ERROR::matches)))
    contracts.flatMap { it.primaryConstructor?.parameters.orEmpty() }
        .assertFalse(testName = "state-typed-errors", additionalMessage = message) {
            it.type.text.isRawError(it.containingFile)
        }
    contracts.flatMap { it.properties(includeNested = false) }
        .assertFalse(testName = "state-typed-errors", additionalMessage = message) {
            it.type?.text.isRawError(it.containingFile)
        }
}

internal fun ArchScope.noFakesInProduction() {
    val message = "Fake*/Stub* — тестовые дублёры: только в тестовых source set'ах. Рабочая " +
        "заглушка в проде — явный NoOp* (поведение «ничего не делать» — часть контракта)."
    production.classesAndInterfacesAndObjects(includeNested = true)
        .assertFalse(testName = "no-fakes-in-production", additionalMessage = message) {
            DOUBLE_WORDS.any { word -> it.name.startsWith(word) || it.name.endsWith(word) }
        }
    (production.functions(includeNested = true) + production.properties(includeNested = true))
        .assertFalse(testName = "no-fakes-in-production", additionalMessage = message) {
            DOUBLE_FACTORY.containsMatchIn(it.name)
        }
}

internal fun ArchScope.forbiddenSuffix() {
    val message = "Роль — в имени: *Helper/*Utils запрещены везде (функции-расширения или класс " +
        "с ролью); *Manager — только платформенная обёртка над нативным API " +
        "(${PLATFORM_SOURCE_SETS.joinToString()}); *Store/*Storage в фичах — это *Repository " +
        "(контракт в domain/repository, реализация Settings*Repository в data/repository). " +
        "docs/patterns.md, «Хранение настроек»"
    production.classesAndInterfacesAndObjects(includeNested = true)
        .assertFalse(testName = "forbidden-suffix", additionalMessage = message) {
            val name = it.name.removeSuffix(IMPL)
            VAGUE_SUFFIXES.any(name::endsWith) ||
                (name.endsWith(MANAGER) && it.containingFile.sourceSetName !in PLATFORM_SOURCE_SETS) ||
                (STORAGE_SUFFIXES.any(name::endsWith) && it.containingFile.isFeature)
        }
    production.files.assertFalse(testName = "forbidden-suffix", additionalMessage = message) { file ->
        VAGUE_SUFFIXES.any(file.name::endsWith)
    }
}

internal fun ArchScope.composableNoViewModel() {
    val message = "ViewModel не передаётся в composable параметром: её получает точка входа экрана " +
        "(koinViewModel), дальше идут state и actions (лямбды, *Actions). docs/compose-best-prac.md"
    val composables = production.functions(includeNested = true, includeLocal = true)
        .filter { it.hasAnnotationWithName("Composable") }
    fun String?.isViewModel(file: KoFileDeclaration) = this != null &&
        (VIEW_MODEL_TYPE.containsMatchIn(this) || mentionsAny(file.aliasesOf(VIEW_MODEL_TYPE::containsMatchIn)))
    composables.flatMap { it.parameters }
        .assertFalse(testName = "composable-no-view-model", additionalMessage = message) {
            it.type.text.isViewModel(it.containingFile)
        }
    composables.filter { it.receiverType?.text.isViewModel(it.containingFile) }
        .assertFalse(testName = "composable-no-view-model", additionalMessage = message) { true }
}

private const val SERIALIZABLE = "Serializable"
private val STABILITY_ANNOTATIONS = setOf("androidx.compose.runtime.Immutable", "androidx.compose.runtime.Stable")
private val RAW_STATE_ERROR = Regex("""\b(Throwable|\w*Exception|Response|Result)\b""")
private val VIEW_MODEL_TYPE = Regex("""\b\w*ViewModel\b""")
private val DOUBLE_WORDS = listOf("Fake", "Stub")
private val DOUBLE_FACTORY = Regex("""^(fake|stub)([A-Z]|$)""")

private fun String.withoutKotlinPrefix() = trim().removePrefix("kotlin.")

private fun KoFileDeclaration.inModelLayer() = inLayer("domain") || inLayer("data")

// `import a.b.MutableList as Items` → `Items`: имя, под которым запрещённый тип виден в файле.
private fun KoFileDeclaration.aliasesOf(predicate: (String) -> Boolean): Set<String> = imports
    .filter { " as " in it.text && predicate(it.name.substringAfterLast('.')) }
    .map { it.text.substringAfter(" as ").trim() }
    .toSet()

private fun String?.mentionsAny(names: Set<String>): Boolean =
    this != null && names.any { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(this) }

private fun <T> T.isSerializable(): Boolean where T : KoAnnotationProvider, T : KoContainingFileProvider {
    val aliases = containingFile.aliasesOf { it == SERIALIZABLE }
    return annotations.any { it.name == SERIALIZABLE || it.name in aliases }
}

private fun KoClassAndInterfaceAndObjectDeclaration.inSerializablePackage(): Boolean {
    val file = containingFile
    return (resideInPackage("..data..") && !file.inLayer("presentation") && !file.inLayer("domain")) ||
        (resideInPackage("..navigation..") && !file.inModelLayer())
}

// Тип аргумента маршрута по имени: импорт файла маршрута, тот же пакет или вложенный в маршрут класс.
private fun KoClassAndInterfaceAndObjectDeclaration.argumentTypeCandidates(): List<String> {
    val imports = containingFile.imports.map { it.name }
    val pkg = containingFile.packagee?.name.orEmpty()
    return properties(includeNested = false).mapNotNull { it.type?.text?.baseTypeName() }.flatMap { name ->
        imports.filter { it.endsWith(".$name") } + "$pkg.$name" + "$fullyQualifiedName.$name"
    }
}

private fun KoClassDeclaration.isStateValue(): Boolean =
    hasDataModifier || !(hasSealedModifier || hasAbstractModifier || hasEnumModifier || hasAnnotationModifier)
