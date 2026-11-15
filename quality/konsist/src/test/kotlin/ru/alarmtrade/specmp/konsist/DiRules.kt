package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue

internal fun ArchScope.koinWiring() {
    val implementations = classes.filter { it.isImplementation() }.map { it.name }.toSet()
    production.files.filterNot { it.inLayer("di") }
        .flatMap { file ->
            (
                file.classesAndInterfacesAndObjects(includeNested = false) +
                    file.functions(includeNested = false, includeLocal = false) +
                    file.properties(includeNested = false)
                ).filter { declaration ->
                val code = declaration.text.stripCommentsAndStrings()
                (CONSTRUCTOR_CALL.findAll(code) + CONSTRUCTOR_REFERENCE.findAll(code))
                    .map { file.originalSimpleName(it.groupValues[1]) }
                    .any { it.endsWith(IMPL) || it in implementations }
            }
        }
        .assertFalse(
            testName = "koin-wiring",
            additionalMessage =
                "Реализацию (*Impl, Specialist*Repository) создаёт только Koin-модуль (пакет di): " +
                    "Foo(…) или ::Foo вне di — ручная сборка графа в обход DI. Остальной код получает абстракцию " +
                    "конструктором. docs/patterns.md, «Внедрение зависимостей»",
        ) { true }
}

internal fun ArchScope.koinModule() {
    production.files.flatMap { file ->
        val builders = file.namesWithAliases(KOIN_MODULE_BUILDERS).joinToString("|")
        val initializer = Regex("""(=|\bby\s+lazy\s*\{)\s*(?:org\.koin\.dsl\.)?($builders)\s*[({]""")
        file.properties(includeNested = true).filter {
            it.type?.text?.baseTypeName() == "Module" ||
                initializer.containsMatchIn(it.text.stripCommentsAndStrings())
        }
    }
        .assertTrue(
            testName = "koin-module",
            additionalMessage = "Koin-модуль — val <name>Module в пакете di. docs/patterns.md, " +
                "«Внедрение зависимостей»",
        ) { it.name.endsWith("Module") && it.resideInPackage("..di..") }
}

internal fun ArchScope.testingModulesTestOnly() {
    val violations = buildScripts(root).flatMap { script ->
        script.testingDependenciesOutsideTests().map { "${script.relativeTo(root)}:$it" }
    }
    check(violations.isEmpty()) {
        "testing-modules-test-only: модуль тестовых дублёров `*:testing` подключается только в тестовых source " +
            "set'ах (commonTest/jvmTest… .dependencies, *TestImplementation): production-код не зависит от " +
            "фейков. docs/patterns.md, «Тестовые дублёры»\n" + violations.joinToString("\n")
    }
}

internal fun ArchScope.topLevelInitOrder() {
    production.files.flatMap { file ->
        val properties = file.properties(includeNested = false)
        val functions = file.functions(includeNested = false, includeLocal = false)
            .associate { it.name to it.text.stripCommentsAndStrings() }
        properties.mapIndexedNotNull { index, property ->
            val initializer = property.text.stripCommentsAndStrings()
            if (" by lazy" in initializer || GETTER.containsMatchIn(initializer)) return@mapIndexedNotNull null
            val later = properties.drop(index + 1).filterNot { it.hasConstModifier }.map { it.name }
            // Функция файла, вызванная из инициализатора, читает поле в момент вызова — как прямое обращение.
            val called = functions.filterKeys { Regex("""\b${Regex.escape(it)}\s*\(""").containsMatchIn(initializer) }
            val reads = listOf(initializer) + called.values
            property.takeIf {
                later.any { name -> reads.any { Regex("""\b${Regex.escape(name)}\b""").containsMatchIn(it) } }
            }
        }
    }.assertFalse(
        testName = "top-level-init-order",
        additionalMessage = "Верхнеуровневое свойство читает свойство, объявленное ниже в том же файле: при " +
            "инициализации файла оно ещё null (на iOS — падение Koin в includes). Перенеси зависимость выше",
    ) { true }
}

internal fun ArchScope.noServiceLocator() {
    production.files.filterNot { it.mayUseServiceLocator }
        .flatMap { file ->
            file.serviceLocatorUsages() + file.typeAliases.filter { alias ->
                QUALIFIED_NAME.findAll(alias.text.stripCommentsAndStrings()).any { it.value.isServiceLocatorName() }
            }
        }
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

internal fun ArchScope.composableInjection() {
    production.files.filterNot { it.isCompositionRoot }
        .flatMap { it.apiUsages(String::isComposableInjectionName) }
        .assertFalse(
            testName = "composable-injection",
            additionalMessage = "Composable не достаёт зависимости из Koin (koinInject, currentKoinScope, " +
                "LocalKoinScope/LocalKoinApplication): данные и действия — через ViewModel (koinViewModel " +
                "разрешён), параметр или CompositionLocal, заданный в корне композиции " +
                "(${COMPOSITION_ROOTS.joinToString()}). docs/patterns.md, «Внедрение зависимостей»",
        ) { true }
}

internal fun ArchScope.noConstructorSideEffects() {
    production.classesAndObjects(includeNested = true).assertFalse(
        testName = "no-constructor-side-effects",
        additionalMessage = "Создание объекта (Koin-резолв) без побочных эффектов: нет init { } и запуска " +
            "корутин в инициализаторах свойств (launch/async/launchIn без CoroutineStart.LAZY, shareIn/stateIn " +
            "c SharingStarted.Eagerly). Данные — лениво при подписке (WhileSubscribed, onStart), фоновые " +
            "реакции — start() доменного сервиса из AppServicesStarter. init разрешён только платформенным " +
            "мониторам (${INIT_ALLOWED_CLASSES.joinToString()}) для синхронной регистрации в ОС, корутины — " +
            "никому. docs/patterns.md, «Без побочных эффектов в конструкторе»",
    ) { klass ->
        (klass.initBlocks.isNotEmpty() && klass.name !in INIT_ALLOWED_CLASSES) ||
            klass.properties(includeNested = false).any { it.launchesEagerly() }
    }
}

internal fun ArchScope.startOnceCas() {
    production.classesAndObjects(includeNested = true).filter { klass ->
        klass.functions(includeNested = false, includeLocal = false)
            .any { function -> function.name == "start" && function.parameters.all { it.defaultValue != null } }
    }.assertFalse(
        testName = "start-once-cas",
        additionalMessage = "Класс с start() не держит guard в var-свойстве (Job, Boolean, инициализатор " +
            "true/false/launch): check-then-set на var при параллельном start() запускает работу дважды. Guard — " +
            "MutableStateFlow(false).compareAndSet(false, true); с stop() — MutableStateFlow<Job?> + " +
            "compareAndSet(null, job). docs/patterns.md, «Без побочных эффектов в конструкторе»",
    ) { klass -> klass.properties(includeNested = false).any { it.isVar && it.isStartGuard() } }
}

private val KOIN_MODULE_BUILDERS = setOf("module", "lazyModule")
private val LAUNCHERS = setOf("launch", "async")
private val EAGER_SHARING = Regex("""\blaunchIn\s*\(|SharingStarted\.Eagerly\b""")

// Корутина из инициализатора свойства: launch/async (и их алиасы) без start = CoroutineStart.LAZY в своих
// аргументах, launchIn, shareIn/stateIn c Eagerly. Геттер и `by lazy` — не в конструкторе.
private fun KoPropertyDeclaration.launchesEagerly(): Boolean {
    if (hasGetter || delegateName == "lazy") return false
    val code = text.stripCommentsAndStrings()
    if (EAGER_SHARING.containsMatchIn(code)) return true
    val launchers = containingFile.namesWithAliases(LAUNCHERS).joinToString("|")
    return Regex("""\b($launchers)\s*([({])""").findAll(code).any { call ->
        val open = call.groups[2]!!.range.first
        code[open] == '{' || LAZY_START !in code.substring(open, code.closingParenthesis(open))
    }
}

private fun String.closingParenthesis(open: Int): Int {
    var depth = 0
    for (i in open until length) {
        when (this[i]) {
            '(' -> depth++
            ')' -> if (--depth == 0) return i
        }
    }
    return length
}
