package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue

internal fun ArchScope.moduleDirection() {
    val message = "core/* импортирует только core/*, feature/* — core/* и feature/*; " +
        "пакеты composition roots (composeApp, flasherApp) не импортируются. " +
        "docs/architecture.md, «Граф зависимостей модулей»"
    libraryFiles.flatMap { file -> file.imports.filter { it.name.isForbiddenProjectImport(file) } }
        .assertFalse(testName = "module-direction", additionalMessage = message) { true }
    libraryFiles.assertFalse(
        testName = "module-direction",
        additionalMessage = "$message (полное имя в коде)",
    ) { file ->
        file.qualifiedReferences().any { it.isForbiddenProjectImport(file) }
    }
}

internal fun ArchScope.modulePackage() {
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

internal fun ArchScope.domainIsolation() {
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

internal fun ArchScope.presentationIsolation() {
    val message = "presentation и composition roots зависят только от domain (своей и чужих фич): в фиче пакет " +
        "..data.. импортируют только data и di, в core — не presentation, в приложениях — только di " +
        "(графы Koin). docs/patterns.md, «Feature-модули»"
    val files = production.files.filter { it.mayNotUseDataLayer }
    files.flatMap { file -> file.imports.filter { it.name.isProjectLayer("data") } }
        .assertFalse(testName = "presentation-isolation", additionalMessage = message) { true }
    files.assertFalse(testName = "presentation-isolation", additionalMessage = "$message (полное имя в коде)") {
        it.qualifiedReferences().any { name -> name.isProjectLayer("data") }
    }
}

internal fun ArchScope.dataIsolation() {
    val message = "data реализует контракты domain и не знает об UI и Koin-модулях. " +
        "docs/patterns.md, «Data — репозитории»"
    val files = libraryFiles.filter { it.inLayer("data") }
    files.flatMap { file -> file.imports.filter { it.name.isForbiddenInData() } }
        .assertFalse(testName = "data-isolation", additionalMessage = message) { true }
    files.assertFalse(testName = "data-isolation", additionalMessage = "$message (полное имя в коде)") {
        it.qualifiedReferences().any { name -> name.isForbiddenInData() }
    }
}

private fun String.isForbiddenInData() = isProjectLayer("presentation") || isProjectLayer("di")

internal fun ArchScope.featureDataIsolation() {
    val message = "Чужая фича доступна только через domain (контракты domain/repository, модели); " +
        "её data — реализация, привязывается в её Koin-модуле. docs/architecture.md, «Граф зависимостей модулей»"
    val files = libraryFiles.filter { it.isFeature }
    files.flatMap { file -> file.imports.filter { it.name.isOtherFeatureData(file) } }
        .assertFalse(testName = "feature-data-isolation", additionalMessage = message) { true }
    files.assertFalse(testName = "feature-data-isolation", additionalMessage = "$message (полное имя в коде)") { file ->
        file.qualifiedReferences().any { it.isOtherFeatureData(file) }
    }
}

internal fun ArchScope.layerPlacement() {
    production.classesAndInterfacesAndObjects(includeNested = true)
        .filter { it.containingFile.isFeature }
        .assertTrue(
            testName = "layer-placement",
            additionalMessage = "Раскладка ролей фичи: реализация репозитория (класс *Repository, " +
                "Specialist*Repository) — data/repository; *DataSource(Impl) — data/datasource; " +
                "*CoordinatorImpl/*ControllerImpl — data/service или platform; *Coordinator/*Controller — " +
                "только interface в domain/service. docs/patterns.md, «Data — репозитории»",
        ) { it.isPlacedByRole() }
}

internal fun ArchScope.repositoryInDomain() {
    production.interfaces(includeNested = true)
        .filter { it.name.endsWith(REPOSITORY) && it.containingFile.isFeature }
        .assertTrue(
            testName = "repository-in-domain",
            additionalMessage = "Интерфейс *Repository фичи — контракт domain: feature/<name>/domain/repository, " +
                "реализация — в data/repository. docs/patterns.md, «Domain»",
        ) { "${it.containingFile.packagee?.name}.".startsWith(it.containingFile.domainPackage) }
}

internal fun ArchScope.sdkConfinement() {
    val message =
        "Типы AlarmTrade SDK (ru.alarmtrade.common/specialist/devices/permissions/bluetoothControl/…) — " +
            "только data и di фичи, модули $SDK_CORE_MODULES и composition roots; domain и presentation работают " +
            "с моделями фичи. docs/architecture.md, «core/specialist»"
    val files = production.files.filterNot { it.mayUseSdk }
    files.flatMap { file -> file.imports.filter { it.name.isSdkName() } }
        .assertFalse(testName = "sdk-confinement", additionalMessage = message) { true }
    files.assertFalse(testName = "sdk-confinement", additionalMessage = "$message (полное имя в коде)") {
        it.qualifiedReferences().any { name -> name.isSdkName() }
    }
}

internal fun ArchScope.storageUsage() {
    val message = "Хранилища (multiplatform-settings, AppSettingsFactory из core.common.settings) в " +
        "фиче — только data и di; domain и presentation получают данные через репозиторий. " +
        "docs/patterns.md, «Хранение настроек»"
    val files = libraryFiles.filter { it.isFeature && !it.isInLayer("data") && !it.isInLayer("di") }
    files.flatMap { file -> file.imports.filter { it.name.isStorageImport() } }
        .assertFalse(testName = "storage-usage", additionalMessage = message) { true }
    files.assertFalse(testName = "storage-usage", additionalMessage = "$message (полное имя в коде)") { file ->
        file.qualifiedReferences().any { it.isStorageImport() }
    }
}

internal fun ArchScope.crossFeaturePresentation() {
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

internal fun ArchScope.capabilityUsage() {
    production.files.assertFalse(testName = "capability-usage", additionalMessage = CAPABILITY_MESSAGE) { file ->
        file.qualifiedReferences().any { it.isForbiddenCapabilityImport(file) }
    }
    production.files.flatMap { file -> file.imports.filter { it.name.isForbiddenCapabilityImport(file) } }
        .assertFalse(
            testName = "capability-usage",
            additionalMessage = CAPABILITY_MESSAGE,
        ) { true }
}

private const val CAPABILITY_MESSAGE = "Платформенные возможности: API core.permissions (PermissionsProvider, " +
    "BindEffect, permissionsModule, blePermissions) и moko-permissions в фиче — только data, di и " +
    "ViewModel (значения DeviceAccessRequirement/PermissionResult — всем); порты " +
    "presentation/platform фичи — только presentation своего модуля, di и composition roots; " +
    "общие порты feature/device/presentation-core — presentation любой фичи. " +
    "docs/patterns.md, «Платформенные возможности»"

internal fun ArchScope.foregroundUsage() {
    val files = production.files.filterNot { file ->
        file.isInLayer("data") || file.isInLayer("di") || file.modulePath == LIFECYCLE_MODULE ||
            file.classes(includeNested = false).any { it.extendsViewModelBase() }
    }
    files.assertFalse(testName = "foreground-usage", additionalMessage = "$FOREGROUND_MESSAGE (полное имя в коде)") {
        it.qualifiedReferences().any { name -> name.isLifecycleImport() }
    }
    files.flatMap { file -> file.imports.filter { it.name.isLifecycleImport() } }
        .assertFalse(testName = "foreground-usage", additionalMessage = FOREGROUND_MESSAGE) { true }
}

private const val FOREGROUND_MESSAGE = "AppForegroundProvider/AppLifecycle (core.lifecycle) — гейт там, где " +
    "инициируется вызов: data (репозиторий, сервис) или ViewModel (опрос, привязанный к экрану); не " +
    "domain и не composable. Koin-модуль core.lifecycle.di подключается где угодно. " +
    "docs/patterns.md, «Жизненный цикл ViewModel и сессии»"
