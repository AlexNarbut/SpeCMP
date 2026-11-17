package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.declaration.KoBaseDeclaration
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoParameterDeclaration
import com.lemonappdev.konsist.api.declaration.KoTypeAliasDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue

internal fun ArchScope.viewModel() {
    classes.filter { it.name.endsWith(VIEW_MODEL) || it.isViewModelClass() }
        .filterNot { it.name in VIEW_MODEL_BASES }
        .assertTrue(
            testName = "view-model",
            additionalMessage =
                "ViewModel наследует BaseViewModel (напрямую или через BaseDeviceViewModel), называется " +
                    "*ViewModel, в feature-модуле лежит в presentation. docs/patterns.md, «BaseViewModel»",
        ) {
            it.isViewModelClass() &&
                it.name.endsWith(VIEW_MODEL) &&
                (!it.containingFile.isFeature || it.resideInPackage("..presentation.."))
        }
}

internal fun ArchScope.viewModelOwnsNoFlows() {
    val viewModels = classes.filter { it.isViewModelClass() }
    val ownedInClasses: List<KoBaseDeclaration> = viewModels.filter { viewModel ->
        viewModel.containingFile.ownedFlow().containsMatchIn(viewModel.text.stripCommentsAndStrings()) ||
            viewModel.properties(includeNested = true).any { it.type?.text.isMutableType() }
    }
    val ownedInFiles = viewModels.map { it.containingFile }.distinctBy { it.path }.flatMap { file ->
        file.properties(includeNested = false)
            .filter { file.ownedFlow().containsMatchIn(it.text.stripCommentsAndStrings()) }
    }
    (ownedInClasses + ownedInFiles).assertFalse(
        testName = "view-model-owns-no-flows",
        additionalMessage = "ViewModel не держит своих MutableStateFlow/MutableSharedFlow/Channel/" +
            "mutableStateOf (ни свойством, ни во вложенном классе, ни верхнеуровневым свойством своего файла): " +
            "state — только reduce, эффекты — postSideEffect. Вход подписки (запрос, фильтр) — поле state + " +
            "select { }, «Повторить» — reload(). docs/patterns.md, «BaseViewModel»",
    ) { true }
}

internal fun ArchScope.viewModelDependencies() {
    val typeAliases = project.files.flatMap { it.typeAliases }.associateBy { it.fullyQualifiedName }
    val declared = project.classesAndInterfacesAndObjects(includeNested = true)
        .mapNotNull { it.fullyQualifiedName }.toSet() + typeAliases.keys.filterNotNull()
    classes.filter { it.isViewModelClass() }
        .flatMap { it.constructorParameters() }
        .assertTrue(
            testName = "view-model-dependencies",
            additionalMessage = "Тип параметра конструктора ViewModel (и аргументы его дженериков) — из " +
                "разрешённого списка: domain фич (*Repository, *UseCase, доменные сервисы и модели), " +
                "presentation (аргументы маршрута, порты presentation/platform), core: AppDispatchers " +
                "(до переноса main-safety в репозитории), AppVersionProvider, " +
                "AppForegroundProvider, PermissionsProvider, AppLocaleStore, ConfigurationLanguageStore, " +
                "PaneWidthStore, TextFilePicker/TextFileSaver, CustomThemeStore, AppServicesStarter, " +
                "AppStartup (запуск корнем); " +
                "Kotlin: " +
                "примитивы, коллекции, " +
                "TimeSource; Clock, TimeZoneSource; Logger. " +
                "Нельзя: data, SDK, Flow, CoroutineScope, функциональные типы. docs/patterns.md, «BaseViewModel»",
        ) { parameter ->
            val type = parameter.type.text
            "->" !in type && TYPE_REFERENCE.findAll(type).all { reference ->
                parameter.containingFile.typeCandidates(reference.value, declared, typeAliases)
                    .all(String::isAllowedViewModelDependency)
            }
        }
}

internal fun ArchScope.viewModelConstructor() {
    classes.filter { it.isViewModelClass() }
        .assertFalse(
            testName = "view-model-constructor",
            additionalMessage = "Конструктор ViewModel — не больше $MAX_VIEW_MODEL_PARAMETERS параметров: " +
                "разросшуюся VM делим по поведению (отдельная VM на независимый блок экрана, use case/доменный " +
                "сервис, реально объединяющий зависимости). docs/patterns.md, «BaseViewModel»",
        ) { viewModel ->
            val constructors = listOfNotNull(viewModel.primaryConstructor?.parameters) +
                viewModel.secondaryConstructors.map { it.parameters }
            constructors.any { it.size > MAX_VIEW_MODEL_PARAMETERS }
        }
}

private fun KoClassDeclaration.constructorParameters(): List<KoParameterDeclaration> =
    primaryConstructor?.parameters.orEmpty() + secondaryConstructors.flatMap { it.parameters }

private fun String?.isMutableType(): Boolean = this?.baseTypeName()?.startsWith("Mutable") == true

private val OWNED_FLOW_NAMES = setOf(
    "MutableStateFlow", "MutableSharedFlow", "Channel", "mutableStateOf", "mutableStateListOf", "mutableStateMapOf",
    "mutableIntStateOf", "mutableLongStateOf", "mutableFloatStateOf", "mutableDoubleStateOf",
)

private fun KoFileDeclaration.ownedFlow(): Regex =
    Regex("""\b(${namesWithAliases(OWNED_FLOW_NAMES).joinToString("|")})\s*(<[^()]*>)?\s*\(""")

// Полные имена, на которые может указывать ссылка: typealias раскрывается, а тип без явного импорта, которого нет в
// пакете файла, может прийти из любого импорта со звёздочкой.
private fun KoFileDeclaration.typeCandidates(
    reference: String,
    declared: Set<String>,
    typeAliases: Map<String?, KoTypeAliasDeclaration>,
): List<String> {
    val name = qualifiedTypeName(reference)
    typeAliases[name]?.let { alias ->
        val target = alias.text.substringAfter('=')
        return TYPE_REFERENCE.findAll(target).map { alias.containingFile.qualifiedTypeName(it.value) }.toList()
    }
    val simple = reference.substringBefore('.')
    val (starImports, plainImports) = imports.partition { it.text.trim().endsWith("*") }
    val explicit = reference.first().isLowerCase() || simple in KOTLIN_DEFAULT_TYPES ||
        plainImports.any { (it.alias?.name ?: it.name.substringAfterLast('.')) == simple }
    val stars = starImports.map { it.name.removeSuffix(".*") + "." + reference }
    return if (explicit || name in declared || stars.isEmpty()) listOf(name) else stars
}
