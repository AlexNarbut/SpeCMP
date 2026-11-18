package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.declaration.KoBaseDeclaration
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.KoParentDeclaration
import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.declaration.combined.KoClassAndInterfaceAndObjectDeclaration
import com.lemonappdev.konsist.api.verify.assertTrue
import java.io.File

internal const val ROOT = "ru.alarmtrade.specmp"

// Имя родителя у Konsist включает аргументы типа: `BaseViewModel<S, E>`; наследники BaseDeviceViewModel — тоже VM.
internal fun KoClassDeclaration.extendsViewModelBase(): Boolean =
    parents(indirectParents = true).any { it.name.substringBefore('<') == BASE_VIEW_MODEL }

internal const val MANAGER = "Manager"
internal const val VIEW_MODEL = "ViewModel"
internal const val BASE_VIEW_MODEL = "BaseViewModel"
internal const val MAX_VIEW_MODEL_PARAMETERS = 12
internal val OWNED_FLOW = Regex(
    """\b(MutableStateFlow|MutableSharedFlow|Channel|mutableStateOf|mutableStateListOf|""" +
        """mutableStateMapOf|mutableIntStateOf|mutableLongStateOf|mutableFloatStateOf|mutableDoubleStateOf)""" +
        """\s*(<[^()]*>)?\s*\(""",
)
internal const val USE_CASE = "UseCase"
internal val VAGUE_SUFFIXES = listOf("Helper", "Helpers", "Util", "Utils")
internal val STORAGE_SUFFIXES = listOf("Store", "Storage")
internal val PLATFORM_SOURCE_SETS = setOf("androidMain", "iosMain", "jvmMain")
internal val GETTER = Regex("""\bget\(\)\s*=""")
internal val SERIALIZABLE_PACKAGES = listOf("..data..", "..navigation..")

internal const val REPOSITORY = "Repository"
internal const val IMPL = "Impl"

internal val KOIN_MODULE_INITIALIZER = Regex("""=\s*(lazyModule|module)\s*[({]""")

// Вызов конструктора или ссылка на него; объявление `class Foo(` — не создание.
internal val CONSTRUCTOR_CALL = Regex("""(?<!\bclass )\b([A-Z]\w*)\s*\(""")
internal val CONSTRUCTOR_REFERENCE = Regex("""::([A-Z]\w*)\b""")

// Имя супертипа у Konsist — как написано: с пакетом и аргументами типа.
internal fun KoParentDeclaration.simpleName(): String =
    name.substringBefore('<').substringBefore('(').trim().substringAfterLast('.')

// Р8: реализации репозиториев названы по источнику (Specialist*Repository), поэтому ищем по супертипу.
internal fun KoClassDeclaration.isImplementation(): Boolean = name.endsWith(IMPL) || isRepositoryImplementation()

internal fun KoClassDeclaration.isRepositoryImplementation(): Boolean =
    resideInPackage("..data..") && parents().any { it.simpleName().endsWith(REPOSITORY) }

// Composition roots и платформенные обёртки: имя модуля Konsist — путь от корня репозитория до src/.
internal val APP_MODULES = setOf(
    "composeApp",
    "flasherApp",
    "androidApp",
    "desktopApp",
    "flasherAndroidApp",
    "flasherDesktopApp",
)

internal val KoFileDeclaration.isCore get() = modulePath.startsWith("core/")
internal val KoFileDeclaration.isFeature get() = modulePath.startsWith("feature/")
internal val KoFileDeclaration.isTestingModule get() = modulePath.endsWith("/testing")

internal val KoFileDeclaration.isTestSource get() = sourceSetName.endsWith("test", ignoreCase = true)

internal val KoFileDeclaration.isSharedHelperFile
    get() = modulePath == "core/common" ||
        (modulePath == "core/ui" && packagee?.name?.startsWith("$ROOT.core.ui.lce") == true)

internal class UsageFile(
    val path: String,
    val modulePath: String,
    val isTest: Boolean,
    val packageName: String,
    val imports: Set<String>,
    val code: String,
)

// Имя целым словом в коде без import, комментариев и строк, и файл видит хелпер (тот же пакет или import): иначе
// общие имена (`isLoading`, `map`) засчитываются чужими объявлениями. Свой файл и тесты своего модуля — не в счёт.
internal fun List<UsageFile>.references(name: String, declaringFile: KoFileDeclaration): Boolean {
    val reference = Regex("""\b${Regex.escape(name)}\b""")
    val helperPackage = declaringFile.packagee?.name.orEmpty()
    return any { file ->
        file.path != declaringFile.path &&
            !(file.isTest && file.modulePath == declaringFile.modulePath) &&
            (
                file.packageName == helperPackage || "$helperPackage.$name" in file.imports ||
                    "$helperPackage.*" in file.imports
                ) &&
            reference.containsMatchIn(file.code)
    }
}

internal fun buildScripts(root: File): List<File> = (listOf("core", "feature") + APP_MODULES)
    .map { File(root, it) }
    .filter(File::isDirectory)
    .flatMap { dir ->
        dir.walkTopDown().onEnter { it.name != "build" && it.name != "src" }.filter { it.name == "build.gradle.kts" }
    }

internal val TESTING_PROJECT_DEPENDENCY = Regex("""project\(":[\w:-]*:testing"\)|\bprojects(?:\.\w+)*\.testing\b""")

// «test» в начале слова или «Test» внутри (commonTest, testImplementation, androidUnitTest), не «latest».
internal val TEST_SCOPE = Regex("""(?<![A-Za-z])test|Test""")

// Строки (номер: текст) с project(":…:testing") вне тестового блока или тестовой конфигурации: блок — по заголовкам
// открытых `{`, тестовый — в заголовке или в самой строке есть «test» (commonTest.dependencies, testImplementation).
internal fun File.testingDependenciesOutsideTests(): List<String> {
    val blocks = ArrayDeque<String>()
    return readLines().withIndex().mapNotNull { (index, line) ->
        val code = line.substringBefore("//")
        val dependency = TESTING_PROJECT_DEPENDENCY.find(code)
        val violation = dependency != null &&
            blocks.none(TEST_SCOPE::containsMatchIn) && !TEST_SCOPE.containsMatchIn(code.take(dependency.range.first))
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
internal val KoFileDeclaration.basePackage
    get() = "$ROOT." + modulePath.replace('/', '.').replace(Regex("-(ui|core)$"), ".$1").replace("-", "")

internal val KoFileDeclaration.domainPackage get() = "$basePackage.domain."

internal fun KoFileDeclaration.inLayer(layer: String) = ".$layer." in ".${packagee?.name}."

internal val LAYERS = setOf("data", "domain", "presentation", "di")

// Пакет от слоя файла: слой — первый сегмент-слой после базового пакета (`….presentation.data` — presentation, а не
// data); нет его — первый в пакете целиком (feature/device/presentation-core: слой в самом базовом пакете).
internal val KoFileDeclaration.layerPath: List<String>
    get() {
        val pkg = packagee?.name.orEmpty()
        val own = pkg.removePrefix(basePackage).split('.').dropWhile { it !in LAYERS }
        return own.ifEmpty { pkg.split('.').dropWhile { it !in LAYERS } }
    }

internal fun KoFileDeclaration.isInLayer(vararg path: String) = layerPath.take(path.size) == path.toList()

// Слой проекта в имени: ru.alarmtrade.specmp.<core|feature>.<module>.….<layer>.…
internal fun String.isProjectLayer(layer: String) = startsWith("$ROOT.") && ".$layer." in "$this."

internal fun String.isForbiddenProjectImport(file: KoFileDeclaration): Boolean {
    if (!startsWith("$ROOT.")) return false
    val allowed = if (file.isCore) listOf("$ROOT.core.") else listOf("$ROOT.core.", "$ROOT.feature.")
    return allowed.none { startsWith(it) }
}

internal fun String.isForbiddenInDomain(): Boolean = isProjectLayer("data") ||
    isProjectLayer("presentation") ||
    isProjectLayer("di") ||
    isSdkName() ||
    startsWith("androidx.compose.") ||
    startsWith("org.jetbrains.compose.") ||
    ".generated.resources." in this ||
    DOMAIN_FORBIDDEN_PREFIXES.any(::startsWith)

internal val DOMAIN_FORBIDDEN_PREFIXES = listOf(
    "$ROOT.core.ui.",
    "com.russhwolf.",
    "kotlinx.serialization.",
    "io.ktor.",
)

// Где пакет ..data.. недоступен: фича — вне data/di, core — presentation, приложения — вне di.
internal val KoFileDeclaration.mayNotUseDataLayer
    get() = when {
        isFeature -> !inLayer("data") && !inLayer("di")
        isCore -> inLayer("presentation")
        else -> !inLayer("di")
    }

// ru.alarmtrade.specmp.feature.<other>.….data.… из файла фичи <this>
internal fun String.isOtherFeatureData(file: KoFileDeclaration): Boolean =
    startsWith("$ROOT.feature.") && !startsWith(file.basePackage + ".") && isProjectLayer("data")

internal fun String.isDataSourceName(): Boolean =
    endsWith("DataSource", ignoreCase = true) || endsWith("DataSourceImpl", ignoreCase = true)

internal fun String.isServiceName(): Boolean = endsWith("Coordinator") || endsWith("Controller")

internal fun KoClassAndInterfaceAndObjectDeclaration.isPlacedByRole(): Boolean {
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

internal val MUTABLE_COLLECTION_TYPE =
    Regex("""\b(Mutable\w*|ArrayList|HashMap|HashSet|LinkedHashMap|LinkedHashSet|ArrayDeque)\b""")

internal fun String?.isMutableCollectionType(): Boolean = this != null && MUTABLE_COLLECTION_TYPE.containsMatchIn(this)

// Корни композиции: здесь composable собирают граф и передают зависимости дальше параметрами.
internal val COMPOSITION_ROOTS = setOf("composeApp", "flasherApp")

internal val CONTRACT_FILE = Regex("""\w+(State|Effect)""")
internal val RAW_ERROR_TYPE = Regex("""\b(Throwable|\w*Exception|Response)\b""")

internal val UI_TEXT_PREFIX = Regex("""^(display|formatted)[A-Z]""")
internal val STRING_TYPE = Regex("""^String\??$""")

internal fun isUiTextField(name: String, type: String): Boolean =
    UI_TEXT_PREFIX.containsMatchIn(name) || (name.endsWith("Text") && STRING_TYPE.matches(type.trim()))

// SDK AlarmTrade — всё под ru.alarmtrade.*, кроме пакетов самого приложения.
internal fun String.isSdkName(): Boolean = startsWith("ru.alarmtrade.") && !startsWith("$ROOT.") && this != ROOT

// core-модули, которые оборачивают SDK (сессия, разрешения, сеть) и отдают остальным свои типы.
internal val SDK_CORE_MODULES = setOf("core/specialist", "core/permissions", "core/network")

internal val KoFileDeclaration.mayUseSdk
    get() = modulePath in SDK_CORE_MODULES ||
        modulePath in APP_MODULES ||
        (isFeature && (isInLayer("data") || isInLayer("di")))

// Маршрут Navigation 3: имя супертипа у Konsist — как написано (`NavKey`, без аргументов типа).
internal fun KoClassAndInterfaceAndObjectDeclaration.isNavKey(): Boolean =
    parents().any { it.name.substringBefore('<').substringAfterLast('.') == "NavKey" }

// `kotlinx.coroutines.flow.StateFlow<Foo>?` → `StateFlow`.
internal fun String.baseTypeName(): String =
    trim().removeSuffix("?").substringBefore('<').substringAfterLast('.').trim()

// Контракт (docs/patterns.md, «Domain»): suspend → Response<…>, Flow/StateFlow/SharedFlow → …<Response<…>>.
// Синхронный член (не suspend, не поток) — нарушение, кроме функции-команды с результатом из syncCommandTypes
// (у сервисов — Unit/Boolean: start, «закрыть в фоне»); у репозиториев команд нет.
internal fun List<KoInterfaceDeclaration>.assertResultContract(
    testName: String,
    role: String,
    docs: String,
    syncCommandTypes: Set<String> = emptySet(),
    nullableResponse: Boolean = false,
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
                it.hasSuspendModifier -> it.returnType?.text.isResponseType(nullableResponse)
                it.returnType?.text.isFlowType() -> it.returnType?.text.isResponseFlow()
                else -> it.syncResultType() in syncCommandTypes && it.parameters.none { p -> p.type.text.isDataSink() }
            }

            is KoPropertyDeclaration -> it.type?.text.isFlowType() && it.type?.text.isResponseFlow()

            else -> true
        }
    }
}

// Результат синхронной функции как написан (без `kotlin.`); без явного типа — Unit, кроме выводимого из
// expression body (тип неизвестен — не команда). `Boolean?` — не команда.
internal fun KoFunctionDeclaration.syncResultType(): String? =
    returnType?.text?.trim()?.removePrefix("kotlin.") ?: UNIT.takeUnless { hasExpressionBody }

internal const val UNIT = "Unit"
internal val SERVICE_SYNC_COMMAND_TYPES = setOf(UNIT, "Boolean")

// Платформенные мониторы сети регистрируют колбэк ОС синхронно при создании.
internal val INIT_ALLOWED_CLASSES = setOf("AndroidNetworkMonitor", "IosNetworkMonitor")

// Инициализатор свойства исполняется в конструкторе; геттер, `by lazy` и LAZY-корутина до start() — нет.
internal fun KoPropertyDeclaration.launchesOnConstruction(): Boolean {
    if (hasGetter || delegateName == "lazy") return false
    val code = text.stripCommentsAndStrings()
    return CONSTRUCTOR_LAUNCH.containsMatchIn(code) && LAZY_START !in code
}

internal val CONSTRUCTOR_LAUNCH = Regex("""\b(launch|async|launchIn)\s*[({]|SharingStarted\.Eagerly\b""")
internal const val LAZY_START = "CoroutineStart.LAZY"

internal val START_GUARD_TYPES = setOf("Job", "Boolean")
internal val START_GUARD_INITIALIZER = Regex("""^(true|false)$|\b(launch|launchIn)\b""")

internal fun KoPropertyDeclaration.isStartGuard(): Boolean = type?.text?.baseTypeName() in START_GUARD_TYPES ||
    value?.stripCommentsAndStrings()?.trim()?.let(START_GUARD_INITIALIZER::containsMatchIn) == true

internal const val RESPONSE = "Response"

// «Нет результата» — Response.Error, а не null; сервисам null разрешён (тайм-аут попытки, DeviceSession.reconnectNow).
internal fun String?.isResponseType(nullable: Boolean): Boolean =
    this != null && (nullable || !trim().endsWith("?")) && baseTypeName() == RESPONSE

// Колбэк `(T) -> Unit` в синхронной команде — данные в обход Response; преобразование `(T) -> T` — не он.
private val DATA_SINK = Regex("""\(\s*[^)\s][^)]*\)\s*->\s*(kotlin\.)?Unit\b""")

internal fun String.isDataSink(): Boolean = DATA_SINK.containsMatchIn(this)
internal val FLOW_TYPES = setOf("Flow", "StateFlow", "SharedFlow")

internal fun String?.isFlowType(): Boolean = this != null && baseTypeName() in FLOW_TYPES

internal fun String?.isResponseFlow(): Boolean =
    this != null && substringAfter('<').substringBeforeLast('>').baseTypeName() == RESPONSE

internal val QUALIFIED_NAME = Regex("""\b[A-Za-z_]\w*(?:\.[A-Za-z_]\w*)+""")

// Квалифицированные имена в коде файла: без package/import, комментариев (включая KDoc) и строковых литералов.
internal fun KoFileDeclaration.qualifiedReferences(): List<String> = QUALIFIED_NAME
    .findAll(codeWithoutHeaderCommentsAndStrings())
    .map { it.value }
    .toList()

internal fun KoFileDeclaration.codeWithoutHeaderCommentsAndStrings(): String =
    text.stripCommentsAndStrings().lineSequence().filterNot { it.isHeaderLine() }.joinToString("\n")

internal fun String.isHeaderLine(): Boolean = trimStart().let { it.startsWith("package ") || it.startsWith("import ") }

// Комментарии и литералы заменяются пробелом (переводы строк внутри блочных комментариев сохраняются,
// чтобы package/import оставались на своих строках). Выражения в шаблонах строк выпадают вместе со строкой.
internal fun String.stripCommentsAndStrings(): String {
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

internal fun String.stringLiteralEnd(start: Int): Int {
    var i = start + 1
    while (i < length && this[i] != '"' && this[i] != '\n') i += if (this[i] == '\\') 2 else 1
    return minOf(i + 1, length)
}

internal val TYPE_REFERENCE = Regex("""\b(?:[a-z_]\w*\.)*[A-Z]\w*(?:\.[A-Z]\w*)*""")

// Типы, видимые без импорта (пакеты kotlin, kotlin.collections).
internal val KOTLIN_DEFAULT_TYPES = setOf(
    "Any", "Boolean", "Byte", "Char", "Double", "Float", "Int", "Long", "Number", "Short", "String", "Unit",
    "Array", "Collection", "Iterable", "List", "Map", "Pair", "Sequence", "Set", "Triple",
)

// Полное имя типа по ссылке из файла: полное имя в коде, импорт (с алиасом), тип Kotlin по умолчанию,
// иначе — тип из пакета файла (в том числе вложенный в класс этого файла).
internal fun KoFileDeclaration.qualifiedTypeName(reference: String): String {
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
internal val VIEW_MODEL_DEPENDENCY_TYPES = setOf(
    "$ROOT.core.common.AppDispatchers",
    "$ROOT.core.common.AppVersionProvider",
    "$ROOT.core.lifecycle.AppForegroundProvider",
    "$ROOT.core.permissions.PermissionsProvider",
    "$ROOT.core.localization.AppLocaleStore",
    // Как AppLocaleStore: настройка приложения из core/common, реализация — core/specialist; доменной обёртки нет.
    "$ROOT.core.common.ConfigurationLanguageStore",
    // Ширины перетаскиваемых панелей: UI-настройка приложения, как ConfigurationLanguageStore.
    "$ROOT.core.common.layout.PaneWidthStore",
    // Порты импорта/экспорта текстовых файлов: создаёт composable (rememberTextFile*), передаёт во VM через parametersOf.
    "$ROOT.core.files.TextFilePicker",
    "$ROOT.core.files.TextFileSaver",
    // Порт выбора изображения (аватар): как TextFilePicker — rememberImagePicker в composable, parametersOf во VM.
    "$ROOT.core.files.ImagePicker",
    // Пользовательская тема (экран «Оформление»): хранилище core/ui, как AppLocaleStore у языка.
    "$ROOT.core.ui.theme.CustomThemeStore",
    // Запуск приложения корнем (AppStartViewModel, core/ui): process-wide сервисы и начальная настройка SDK.
    "$ROOT.core.common.AppServicesStarter",
    "$ROOT.core.common.AppStartup",
    "kotlin.time.TimeSource",
    "kotlinx.datetime.Clock",
    "$ROOT.core.common.time.TimeZoneSource",
    "co.touchlab.kermit.Logger",
)

internal fun String.isAllowedViewModelDependency(): Boolean {
    val segments = split('.')
    val pkg = segments.takeWhile { it.first().isLowerCase() }.joinToString(".")
    val featureLayer = pkg.startsWith("$ROOT.feature.") && ("domain" in segments || "presentation" in segments)
    return this in VIEW_MODEL_DEPENDENCY_TYPES || pkg == "kotlin" || featureLayer
}

// API Koin для доставания зависимостей вне графа: пакеты (`pkg.`) и отдельные декларации.
internal val SERVICE_LOCATOR_PACKAGES = listOf(
    "org.koin.core.component.",
    "org.koin.core.context.",
    "org.koin.android.ext.android.",
)
internal val SERVICE_LOCATOR_NAMES = setOf(
    "org.koin.mp.KoinPlatformTools",
    "org.koin.mp.KoinPlatform",
    "org.koin.java.KoinJavaComponent",
    "org.koin.compose.getKoin",
    "org.koin.core.Koin",
)

internal fun String.isServiceLocatorName(): Boolean {
    val name = removeSuffix(".*")
    return SERVICE_LOCATOR_PACKAGES.any { name.startsWith(it) || "$name." == it } ||
        SERVICE_LOCATOR_NAMES.any { name == it || name.startsWith("$it.") }
}

// Компоненты, которые создаёт система: конструктора для DI у них нет.
internal val ANDROID_ENTRY_POINTS = setOf(
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

internal val KoFileDeclaration.mayUseServiceLocator: Boolean
    get() = inLayer("di") ||
        modulePath in APP_MODULES ||
        classes(includeNested = true).any { type ->
            type.parents().any { it.name.substringBefore('<').substringBefore('(').trim() in ANDROID_ENTRY_POINTS }
        }

internal val KoFileDeclaration.isCompositionRoot: Boolean
    get() = modulePath in COMPOSITION_ROOTS

internal fun KoFileDeclaration.serviceLocatorUsages(): List<KoBaseDeclaration> =
    apiUsages(String::isServiceLocatorName) { name -> "$name." in SERVICE_LOCATOR_PACKAGES }

// Koin-API для зависимостей прямо в composable; `koinViewModel` (org.koin.compose.viewmodel) сюда не входит.
internal val COMPOSABLE_INJECTION_NAMES = setOf(
    "org.koin.compose.koinInject",
    "org.koin.compose.currentKoinScope",
    "org.koin.compose.LocalKoinScope",
    "org.koin.compose.LocalKoinApplication",
)

internal fun String.isComposableInjectionName(): Boolean = removeSuffix(".*") == "org.koin.compose" ||
    COMPOSABLE_INJECTION_NAMES.any { this == it || startsWith("$it.") }

/**
 * Декларации верхнего уровня, упоминающие API (по простому имени/алиасу импорта или полным именем), — чтобы
 * `@Suppress` ставился на объект, а не на файл; импорт со звёздочкой или пакета целиком ([isPackageImport]) и
 * неиспользованный импорт возвращаются самим импортом.
 */
internal fun KoFileDeclaration.apiUsages(
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

internal val IDENTIFIER = Regex("""\b[A-Za-z_]\w*\b""")

internal val MAPPER_NAME = Regex("""(?:\w*To|to)Domain\w*|(?:\w*To|to)\w*Dto""")

internal val STORAGE_PACKAGES = listOf("com.russhwolf.settings", "$ROOT.core.common.settings")

// Импорт со звёздочкой Konsist отдаёт как имя пакета — он тоже доступ к хранилищу.
internal fun String.isStorageImport(): Boolean =
    removeSuffix(".*").let { name -> STORAGE_PACKAGES.any { name == it || name.startsWith("$it.") } }

internal val USE_CASE_DEPENDENCY_SUFFIXES =
    listOf("Repository", "UseCase", "Coordinator", "Controller", "Service", "Session", "Closeable")

// `List<AuthCloseable>` из getAll() — тоже зависимость: считаем по типу элемента.
internal fun KoClassDeclaration.orchestratesDependencies(): Boolean =
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

internal val COLLECTION_TYPES = setOf("List", "Set", "Collection")

internal fun KoClassDeclaration.hasResponseInvoke(): Boolean {
    val functions = functions(includeNested = false, includeLocal = false)
    val invokes = functions.filter { it.name == "invoke" && it.hasOperatorModifier }
    val otherPublic = functions.filter { it.name != "invoke" && !it.hasPrivateModifier && !it.hasInternalModifier }
    val publicState = properties(includeNested = false).any { !it.hasPrivateModifier && !it.hasInternalModifier } ||
        primaryConstructor?.parameters.orEmpty().any { PUBLIC_CONSTRUCTOR_PROPERTY.containsMatchIn(it.text) }
    return invokes.isNotEmpty() && otherPublic.isEmpty() && !publicState && invokes.all {
        val returnType = it.returnType?.text?.trim()
        when {
            returnType == null || returnType.endsWith(
                "?",
            ) || NULLABLE_RESPONSE_ARGUMENT.containsMatchIn(returnType) -> false

            it.hasSuspendModifier -> returnType.baseTypeName() == RESPONSE

            else -> returnType.isFlowType() && returnType.isResponseFlow()
        }
    }
}

// `val foo: FooRepository` без модификатора видимости в первичном конструкторе — публичное свойство.
internal val NULLABLE_RESPONSE_ARGUMENT = Regex("""Response<.*>\?>""")

internal val PUBLIC_CONSTRUCTOR_PROPERTY = Regex("""^\s*(?:public\s+)?(?:override\s+)?(?:val|var)\b""")

// Все простые имена типов в записи `List<Lazy<foo.FooDataSource>>?` — для поиска обёрнутых зависимостей.
internal fun String.typeNames(): List<String> =
    TYPE_NAME.findAll(this).map { it.value.substringAfterLast('.') }.toList()

internal val TYPE_NAME = Regex("""[A-Za-z_][\w.]*""")

internal const val LIFECYCLE_MODULE = "core/lifecycle"
internal const val LIFECYCLE_PACKAGE = "$ROOT.core.lifecycle"

// Импорт со звёздочкой Konsist отдаёт по-разному (`pkg.*` или `pkg`) — ловим оба.
internal fun String.isLifecycleImport(): Boolean = removeSuffix(".*").let { name ->
    (name == LIFECYCLE_PACKAGE || name.startsWith("$LIFECYCLE_PACKAGE.")) && !name.startsWith("$LIFECYCLE_PACKAGE.di")
}

// API core.permissions, которое обращается к ОС; значения (DeviceAccessRequirement, PermissionResult) — не оно.
internal val PERMISSIONS_API = setOf("PermissionsProvider", "BindEffect", "permissionsModule", "blePermissions")
    .map { "$ROOT.core.permissions.$it" }
internal const val MOKO_PERMISSIONS = "dev.icerock.moko.permissions"
internal const val PERMISSIONS_PACKAGE = "$ROOT.core.permissions"

// Общий presentation фич устройства: компоненты и порты, которые подключают несколько фич.
internal const val SHARED_PRESENTATION_PACKAGE = "$ROOT.feature.device.presentation.core"

internal fun String.isOtherFeaturePresentation(file: KoFileDeclaration): Boolean = startsWith("$ROOT.feature.") &&
    !startsWith(file.basePackage + ".") &&
    isProjectLayer("presentation") &&
    !startsWith("$SHARED_PRESENTATION_PACKAGE.")

internal fun String.isForbiddenCapabilityImport(file: KoFileDeclaration): Boolean {
    if (!file.isFeature) return false
    val name = removeSuffix(".*").let { if (it == PERMISSIONS_PACKAGE) PERMISSIONS_API.first() else it }
    val inViewModelFile = file.classes(includeNested = false).any { klass ->
        klass.parents(indirectParents = true).any { it.name.substringBefore('<') == "BaseViewModel" }
    }
    val permissionsApi = PERMISSIONS_API.any { name == it || name.startsWith("$it.") } ||
        name == MOKO_PERMISSIONS || name.startsWith("$MOKO_PERMISSIONS.")
    if (permissionsApi) return !file.isInLayer("data") && !file.isInLayer("di") && !inViewModelFile
    val port = name.substringBefore(".platform.", missingDelimiterValue = "").takeIf { ".presentation" in it }
        ?.plus(".platform") ?: return false
    if (!port.startsWith("$ROOT.feature.") || file.isInLayer("di")) return false
    val ownPresentation = port.startsWith(file.basePackage + ".") && file.isInLayer("presentation")
    val sharedPort = port.startsWith("$SHARED_PRESENTATION_PACKAGE.") && file.isInLayer("presentation")
    return !ownPresentation && !sharedPort
}

// Имена, под которыми декларации [simpleNames] видны в файле: сами имена и алиасы их импортов.
internal fun KoFileDeclaration.namesWithAliases(simpleNames: Set<String>): Set<String> = simpleNames +
    imports.mapNotNull { import -> import.alias?.name?.takeIf { import.name.substringAfterLast('.') in simpleNames } }

// Простое имя, на которое указывает [name] в файле: алиас импорта раскрывается, полное имя укорачивается.
internal fun KoFileDeclaration.originalSimpleName(name: String): String {
    val simple = name.substringBefore('<').substringBefore('(').trim().substringAfterLast('.')
    return imports.firstOrNull { it.alias?.name == simple }?.name?.substringAfterLast('.') ?: simple
}

internal val VIEW_MODEL_BASES = setOf(BASE_VIEW_MODEL, "BaseDeviceViewModel")

// Как extendsViewModelBase, но и через алиас импорта или полное имя базы.
internal fun KoClassDeclaration.isViewModelClass(): Boolean = extendsViewModelBase() ||
    parents().any { containingFile.originalSimpleName(it.name) in VIEW_MODEL_BASES }
