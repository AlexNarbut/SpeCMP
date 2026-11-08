package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.combined.KoClassAndInterfaceAndObjectDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

private const val ROOT = "ru.alarmtrade.specmp"

/**
 * Правила раскладки кода из docs/architecture.md и docs/patterns.md (описание — docs/code-quality.md, «Konsist»).
 *
 * `testName` правила — его id: точечное исключение в коде — `@Suppress("konsist.<id>")` (для файла —
 * `@file:Suppress`) с причиной комментарием строкой выше.
 */
class ArchitectureTest {
    private val scope = Konsist.scopeFromProduction().slice {
        it.isCore || it.isFeature || it.moduleName in APP_MODULES
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
    fun `type names use agreed role suffixes`() {
        scope.classesAndInterfacesAndObjects(includeNested = true)
            .assertFalse(
                testName = "forbidden-suffix",
                additionalMessage = "Роль — в имени: *Helper/*Utils запрещены везде (функции-расширения или класс " +
                    "с ролью); *Manager — только платформенная обёртка над нативным API " +
                    "(${PLATFORM_SOURCE_SETS.joinToString()}).",
            ) {
                VAGUE_SUFFIXES.any(it.name::endsWith) ||
                    (it.name.endsWith(MANAGER) && it.containingFile.sourceSetName !in PLATFORM_SOURCE_SETS)
            }
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
            .filterNot { it.name == BASE_VIEW_MODEL || it.isLegacyViewModel() }
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
                    "(до переноса main-safety в репозитории), AppRestarter, AppForegroundProvider, " +
                    "PermissionsProvider, AppLocaleStore, ConfigurationLanguageStore; Kotlin: примитивы, коллекции, " +
                    "TimeSource; Logger. " +
                    "Нельзя: data, SDK, Flow, CoroutineScope, функциональные типы. docs/patterns.md, «BaseViewModel»",
            ) { parameter ->
                val type = parameter.type.text
                "->" !in type && TYPE_REFERENCE.findAll(type)
                    .all { parameter.containingFile.qualifiedTypeName(it.value).isAllowedViewModelDependency() }
            }
    }

    // Имя родителя у Konsist включает аргументы типа: `BaseViewModel<S, E>`; наследники BaseDeviceViewModel — тоже VM.
    private fun KoClassDeclaration.extendsViewModelBase(): Boolean =
        parents(indirectParents = true).any { it.name.substringBefore('<') == BASE_VIEW_MODEL }

    // Миграция ViewModel (этап 2): прежние базы и их прямые наследники пропускаются до подэтапа 2n.
    private fun KoClassDeclaration.isLegacyViewModel(): Boolean = name in LEGACY_VIEW_MODEL_BASES ||
        parents().any { it.name.substringBefore('<') in LEGACY_VIEW_MODEL_BASES }

    private companion object {
        const val MANAGER = "Manager"
        const val VIEW_MODEL = "ViewModel"
        const val BASE_VIEW_MODEL = "BaseViewModel"
        val LEGACY_VIEW_MODEL_BASES = setOf("LegacyBaseViewModel", "LegacyBaseDeviceViewModel")
        val OWNED_FLOW = Regex(
            """\b(MutableStateFlow|MutableSharedFlow|Channel|sideEffectFlow|mutableStateOf|mutableStateListOf|""" +
                """mutableStateMapOf|mutableIntStateOf|mutableLongStateOf|mutableFloatStateOf|mutableDoubleStateOf)""" +
                """\s*(<[^()]*>)?\s*\(""",
        )
        val VAGUE_SUFFIXES = listOf("Helper", "Helpers", "Util", "Utils")
        val PLATFORM_SOURCE_SETS = setOf("androidMain", "iosMain", "jvmMain")
        val GETTER = Regex("""\bget\(\)\s*=""")
        val SERIALIZABLE_PACKAGES = listOf("..data..", "..navigation..")
    }
}

private const val REPOSITORY = "Repository"

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

private val QUALIFIED_NAME = Regex("""\b[A-Za-z_]\w*(?:\.[A-Za-z_]\w*)+""")

// Квалифицированные имена в коде файла: без package/import, комментариев (включая KDoc) и строковых литералов.
private fun KoFileDeclaration.qualifiedReferences(): List<String> = QUALIFIED_NAME
    .findAll(text.stripCommentsAndStrings().lineSequence().filterNot { it.isHeaderLine() }.joinToString("\n"))
    .map { it.value }
    .toList()

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
    "$ROOT.core.lifecycle.AppForegroundProvider",
    "$ROOT.core.permissions.PermissionsProvider",
    "$ROOT.core.localization.AppLocaleStore",
    // Как AppLocaleStore: настройка приложения из core/common, реализация — core/specialist; доменной обёртки нет.
    "$ROOT.core.common.ConfigurationLanguageStore",
    "kotlin.time.TimeSource",
    "co.touchlab.kermit.Logger",
)

private fun String.isAllowedViewModelDependency(): Boolean {
    val segments = split('.')
    val pkg = segments.takeWhile { it.first().isLowerCase() }.joinToString(".")
    val featureLayer = pkg.startsWith("$ROOT.feature.") && ("domain" in segments || "presentation" in segments)
    return this in VIEW_MODEL_DEPENDENCY_TYPES || pkg == "kotlin" || featureLayer
}
