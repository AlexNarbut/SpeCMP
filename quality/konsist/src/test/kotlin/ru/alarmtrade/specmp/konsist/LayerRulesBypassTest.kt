package ru.alarmtrade.specmp.konsist

import org.junit.jupiter.api.Test

class LayerRulesBypassTest {

    @Test
    fun `clean feature passes layer rules`() {
        val arch = fixture(
            src(
                "feature/foo",
                "FooRepository.kt",
                "package $FOO.domain.repository\n\nimport $ROOT.core.common.Response\n\ninterface FooRepository",
            ),
            src(
                "feature/foo",
                "FooRepositoryImpl.kt",
                """
                package $FOO.data.repository

                import $FOO.domain.repository.FooRepository
                import $BAR.domain.model.Bar

                internal class FooRepositoryImpl(private val bar: Bar) : FooRepository
                """,
            ),
            src(
                "feature/foo",
                "FooViewModel.kt",
                "package $FOO.presentation\n\nimport $FOO.domain.repository.FooRepository\n\nclass FooViewModel",
            ),
        )
        arch.moduleDirection()
        arch.modulePackage()
        arch.domainIsolation()
        arch.presentationIsolation()
        arch.dataIsolation()
        arch.featureDataIsolation()
        arch.layerPlacement()
        arch.repositoryInDomain()
    }

    @Test
    fun `module-direction bypass via import alias`() = assertViolation("module-direction", "FooState") {
        fixture(
            src(
                "core/common",
                "A.kt",
                """
                package $ROOT.core.common

                import $FOO.presentation.FooState as CoreState

                fun state(): CoreState? = null
                """,
            ),
        ).moduleDirection()
    }

    @Test
    fun `module-direction bypass via star import`() = assertViolation("module-direction", "$FOO.domain") {
        fixture(
            src(
                "core/ui",
                "A.kt",
                """
                package $ROOT.core.ui

                import $FOO.domain.*

                fun model(): Foo? = null
                """,
            ),
        ).moduleDirection()
    }

    @Test
    fun `module-direction bypass via fully qualified name without import`() =
        assertViolation("module-direction", "Holder.kt") {
            fixture(
                src(
                    "core/common",
                    "Holder.kt",
                    """
                    package $ROOT.core.common

                    typealias Foo = $FOO.domain.model.Foo
                    """,
                ),
            ).moduleDirection()
        }

    @Test
    fun `module-direction bypass via app package from feature platform source set`() =
        assertViolation("module-direction", "$ROOT.app.AppGraph") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.presentation

                    import $ROOT.app.AppGraph

                    fun graph(): AppGraph? = null
                    """,
                    sourceSet = "iosMain",
                ),
            ).moduleDirection()
        }

    @Test
    fun `module-package bypass via missing package directive`() = assertViolation("module-package", "A.kt") {
        fixture(src("feature/foo", "A.kt", "class A")).modulePackage()
    }

    @Test
    fun `module-package bypass via sibling module with the same prefix`() = assertViolation("module-package", "A.kt") {
        fixture(src("feature/auth", "A.kt", "package $ROOT.feature.authstate.domain\n\nclass A")).modulePackage()
    }

    @Test
    fun `module-package bypass via flattened ui module package`() = assertViolation("module-package", "A.kt") {
        fixture(
            src("feature/device/settings-ui", "A.kt", "package $ROOT.feature.device.settingsui\n\nclass A"),
        ).modulePackage()
    }

    @Test
    fun `module-package bypass via other feature package in platform source set`() =
        assertViolation("module-package", "A.kt") {
            fixture(src("feature/foo", "A.kt", "package $BAR.data\n\nclass A", sourceSet = "androidMain"))
                .modulePackage()
        }

    @Test
    fun `domain-isolation bypass via aliased data import`() = assertViolation("domain-isolation", "FooDto") {
        fixture(
            src(
                "feature/foo",
                "A.kt",
                """
                package $FOO.domain.model

                import $FOO.data.dto.FooDto as Foo

                fun parse(): Foo? = null
                """,
            ),
        ).domainIsolation()
    }

    @Test
    fun `domain-isolation bypass via star import of presentation`() =
        assertViolation("domain-isolation", "$FOO.presentation") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.domain.usecase

                    import $FOO.presentation.*

                    class A
                    """,
                ),
            ).domainIsolation()
        }

    @Test
    fun `domain-isolation bypass via fully qualified compose annotation`() =
        assertViolation("domain-isolation", "полное имя в коде", "Foo.kt") {
            fixture(
                src(
                    "feature/foo",
                    "Foo.kt",
                    """
                    package $FOO.domain.model

                    @androidx.compose.runtime.Immutable
                    data class Foo(val id: Int)
                    """,
                ),
            ).domainIsolation()
        }

    @Test
    fun `domain-isolation bypass via typealias to SDK type in platform source set`() =
        assertViolation("domain-isolation", "Sdk.kt") {
            fixture(
                src(
                    "feature/foo",
                    "Sdk.kt",
                    """
                    package $FOO.domain

                    typealias Device = ru.alarmtrade.common.device.Device
                    """,
                    sourceSet = "iosMain",
                ),
            ).domainIsolation()
        }

    @Test
    fun `presentation-isolation bypass via aliased data import in view model`() =
        assertViolation("presentation-isolation", "FooRepositoryImpl") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.presentation

                    import $FOO.data.repository.FooRepositoryImpl as Repo

                    class FooViewModel(val repo: Repo)
                    """,
                ),
            ).presentationIsolation()
        }

    @Test
    fun `presentation-isolation bypass via star import in layerless feature package`() =
        assertViolation("presentation-isolation", "$FOO.data") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.ui

                    import $FOO.data.*

                    class A
                    """,
                ),
            ).presentationIsolation()
        }

    @Test
    fun `presentation-isolation bypass via fully qualified name in composition root`() =
        assertViolation("presentation-isolation", "Root.kt") {
            fixture(
                src(
                    "composeApp",
                    "Root.kt",
                    """
                    package $ROOT.app

                    fun create() = $FOO.data.repository.FooRepositoryImpl()
                    """,
                ),
            ).presentationIsolation()
        }

    @Test
    fun `presentation-isolation bypass via core presentation in desktop source set`() =
        assertViolation("presentation-isolation", "SettingsDto") {
            fixture(
                src(
                    "core/ui",
                    "A.kt",
                    """
                    package $ROOT.core.ui.presentation

                    import $ROOT.core.common.data.SettingsDto

                    fun dto(): SettingsDto? = null
                    """,
                    sourceSet = "jvmMain",
                ),
            ).presentationIsolation()
        }

    @Test
    fun `data-isolation bypass via aliased presentation import`() = assertViolation("data-isolation", "FooState") {
        fixture(
            src(
                "feature/foo",
                "A.kt",
                """
                    package $FOO.data.repository

                    import $FOO.presentation.FooState as Snapshot

                    internal fun snapshot(): Snapshot? = null
                    """,
            ),
        ).dataIsolation()
    }

    @Test
    fun `data-isolation bypass via star import of di`() = assertViolation("data-isolation", "$FOO.di") {
        fixture(
            src(
                "feature/foo",
                "A.kt",
                """
                package $FOO.data

                import $FOO.di.*

                internal class A
                """,
            ),
        ).dataIsolation()
    }

    @Test
    fun `data-isolation bypass via fully qualified name without import`() =
        assertViolation("data-isolation", "Mapper.kt") {
            fixture(
                src(
                    "feature/foo",
                    "Mapper.kt",
                    """
                    package $FOO.data.mapper

                    internal fun $FOO.presentation.FooState.toDto(): Int = 0
                    """,
                ),
            ).dataIsolation()
        }

    @Test
    fun `data-isolation bypass via platform source set of core data`() =
        assertViolation("data-isolation", "FooScreen") {
            fixture(
                src(
                    "core/network",
                    "A.kt",
                    """
                    package $ROOT.core.network.data

                    import $ROOT.core.network.presentation.FooScreen

                    internal fun screen(): FooScreen? = null
                    """,
                    sourceSet = "androidMain",
                ),
            ).dataIsolation()
        }

    @Test
    fun `feature-data-isolation bypass via aliased import`() =
        assertViolation("feature-data-isolation", "BarRepositoryImpl") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.di

                    import $BAR.data.repository.BarRepositoryImpl as Bar

                    fun bar(): Bar? = null
                    """,
                ),
            ).featureDataIsolation()
        }

    @Test
    fun `feature-data-isolation bypass via star import`() = assertViolation("feature-data-isolation", "$BAR.data") {
        fixture(
            src(
                "feature/foo",
                "A.kt",
                """
                    package $FOO.data

                    import $BAR.data.*

                    internal class A
                    """,
            ),
        ).featureDataIsolation()
    }

    @Test
    fun `feature-data-isolation bypass via fully qualified name without import`() =
        assertViolation("feature-data-isolation", "Wiring.kt") {
            fixture(
                src(
                    "feature/foo",
                    "Wiring.kt",
                    """
                    package $FOO.di

                    fun bar() = $BAR.data.repository.BarRepositoryImpl()
                    """,
                ),
            ).featureDataIsolation()
        }

    @Test
    fun `feature-data-isolation bypass via module whose package shares the prefix`() =
        assertViolation("feature-data-isolation", "$ROOT.feature.foobar.data.Dto") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.data

                    import $ROOT.feature.foobar.data.Dto

                    internal fun dto(): Dto? = null
                    """,
                ),
            ).featureDataIsolation()
        }

    @Test
    fun `layer-placement bypass via repository class in domain repository package`() =
        assertViolation("layer-placement", "FooRepository") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.domain.repository\n\nclass FooRepository"),
            ).layerPlacement()
        }

    @Test
    fun `layer-placement bypass via data source nested in presentation class`() =
        assertViolation("layer-placement", "FooDataSource") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.presentation

                    class Holder {
                        companion object {
                            object FooDataSource
                        }
                    }
                    """,
                ),
            ).layerPlacement()
        }

    @Test
    fun `layer-placement bypass via abstract coordinator class instead of interface`() =
        assertViolation("layer-placement", "FooCoordinator") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.domain.service\n\nabstract class FooCoordinator"),
            ).layerPlacement()
        }

    @Test
    fun `layer-placement bypass via lowercase source suffix in presentation`() =
        assertViolation("layer-placement", "FooDatasourceImpl") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\ninternal class FooDatasourceImpl",
                    sourceSet = "iosMain",
                ),
            ).layerPlacement()
        }

    @Test
    fun `repository-in-domain bypass via interface nested in presentation class`() =
        assertViolation("repository-in-domain", "FooRepository") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    """
                    package $FOO.presentation

                    class FooScreenContract {
                        interface FooRepository
                    }
                    """,
                ),
            ).repositoryInDomain()
        }

    @Test
    fun `repository-in-domain bypass via fun interface in data`() =
        assertViolation("repository-in-domain", "FooRepository") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.data\n\nfun interface FooRepository {\n    fun load(): Int\n}",
                ),
            ).repositoryInDomain()
        }

    @Test
    fun `repository-in-domain bypass via domain segment inside presentation package`() =
        assertViolation("repository-in-domain", "FooRepository") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.presentation.domain\n\ninterface FooRepository"),
            ).repositoryInDomain()
        }

    @Test
    fun `repository-in-domain bypass via generic interface in platform source set`() =
        assertViolation("repository-in-domain", "FooRepository") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.platform\n\ninterface FooRepository<T>",
                    sourceSet = "androidMain",
                ),
            ).repositoryInDomain()
        }

    @Test
    fun `clean feature passes usage rules`() {
        val arch = fixture(
            src(
                "feature/foo",
                "FooScreen.kt",
                """
                package $FOO.presentation

                import $ROOT.feature.device.presentation.core.DeviceHeader
                import $FOO.presentation.platform.FooPicker
                import $ROOT.core.permissions.DeviceAccessRequirement

                fun screen(header: DeviceHeader, picker: FooPicker, need: DeviceAccessRequirement) = Unit
                """,
            ),
            src(
                "feature/foo",
                "FooRepositoryImpl.kt",
                """
                package $FOO.data.repository

                import com.russhwolf.settings.Settings
                import ru.alarmtrade.common.Sdk
                import $ROOT.core.lifecycle.AppForegroundProvider
                import $ROOT.core.permissions.PermissionsProvider

                internal class FooRepositoryImpl(s: Settings, sdk: Sdk, f: AppForegroundProvider, p: PermissionsProvider)
                """,
            ),
        )
        arch.crossFeaturePresentation()
        arch.sdkConfinement()
        arch.storageUsage()
        arch.capabilityUsage()
        arch.foregroundUsage()
    }

    @Test
    fun `cross-feature-presentation bypass via import alias`() =
        assertViolation("cross-feature-presentation", "BarScreen") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nimport $BAR.presentation.screen.BarScreen as Embedded\n\nval x: Embedded? = null",
                ),
            ).crossFeaturePresentation()
        }

    @Test
    fun `cross-feature-presentation bypass via star import`() =
        assertViolation("cross-feature-presentation", "$BAR.presentation") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.presentation\n\nimport $BAR.presentation.*\n\nval x = 1"),
            ).crossFeaturePresentation()
        }

    @Test
    fun `cross-feature-presentation bypass via typealias with fully qualified name`() =
        assertViolation("cross-feature-presentation", "A.kt") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\ntypealias Bar = $BAR.presentation.BarViewModel",
                ),
            ).crossFeaturePresentation()
        }

    @Test
    fun `cross-feature-presentation bypass via domain file in platform source set`() =
        assertViolation("cross-feature-presentation", "BarPort") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.domain\n\nimport $BAR.presentation.platform.BarPort\n\nfun port(p: BarPort) = p",
                    sourceSet = "iosMain",
                ),
            ).crossFeaturePresentation()
        }

    @Test
    fun `cross-feature-presentation bypass via look-alike of shared presentation package`() =
        assertViolation("cross-feature-presentation", "Header") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nimport $ROOT.feature.device.presentation.coreui.Header\n\nval h: Header? = null",
                ),
            ).crossFeaturePresentation()
        }

    @Test
    fun `sdk-confinement bypass via import alias in presentation`() =
        assertViolation("sdk-confinement", "ru.alarmtrade.common.CarInfo") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nimport ru.alarmtrade.common.CarInfo as Car\n\nval c: Car? = null",
                ),
            ).sdkConfinement()
        }

    @Test
    fun `sdk-confinement bypass via star import in domain`() =
        assertViolation("sdk-confinement", "ru.alarmtrade.devices") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.domain.model\n\nimport ru.alarmtrade.devices.*\n\nval x = 1"),
            ).sdkConfinement()
        }

    @Test
    fun `sdk-confinement bypass via fully qualified name in platform source set`() =
        assertViolation("sdk-confinement", "A.kt") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nfun id(d: ru.alarmtrade.specialist.Device) = d.id",
                    sourceSet = "androidMain",
                ),
            ).sdkConfinement()
        }

    @Test
    fun `sdk-confinement bypass via data segment nested in presentation package`() =
        assertViolation("sdk-confinement", "ru.alarmtrade.common.CarInfo") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation.data\n\nimport ru.alarmtrade.common.CarInfo\n\nval c: CarInfo? = null",
                ),
            ).sdkConfinement()
        }

    @Test
    fun `sdk-confinement bypass via non-SDK core module`() =
        assertViolation("sdk-confinement", "ru.alarmtrade.common.Sdk") {
            fixture(
                src(
                    "core/ui",
                    "A.kt",
                    "package $ROOT.core.ui\n\nimport ru.alarmtrade.common.Sdk\n\nval s: Sdk? = null",
                ),
            ).sdkConfinement()
        }

    @Test
    fun `storage-usage bypass via import alias in presentation`() =
        assertViolation("storage-usage", "com.russhwolf.settings.Settings") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nimport com.russhwolf.settings.Settings as Prefs\n\nval p: Prefs? = null",
                ),
            ).storageUsage()
        }

    @Test
    fun `storage-usage bypass via star import`() = assertViolation("storage-usage", "com.russhwolf.settings") {
        fixture(
            src("feature/foo", "A.kt", "package $FOO.domain\n\nimport com.russhwolf.settings.*\n\nval x = 1"),
        ).storageUsage()
    }

    @Test
    fun `storage-usage bypass via fully qualified name without import`() = assertViolation("storage-usage", "A.kt") {
        fixture(
            src(
                "feature/foo",
                "A.kt",
                "package $FOO.presentation\n\nfun read(s: $ROOT.core.common.settings.AppSettingsFactory) = s",
            ),
        ).storageUsage()
    }

    @Test
    fun `storage-usage bypass via data segment nested in presentation package`() =
        assertViolation("storage-usage", "com.russhwolf.settings.Settings") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation.data\n\nimport com.russhwolf.settings.Settings\n\nval s: Settings? = null",
                    sourceSet = "jvmMain",
                ),
            ).storageUsage()
        }

    @Test
    fun `capability-usage bypass via import alias in composable`() =
        assertViolation("capability-usage", "PermissionsProvider") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nimport $ROOT.core.permissions.PermissionsProvider as Access\n\nval a: Access? = null",
                ),
            ).capabilityUsage()
        }

    @Test
    fun `capability-usage bypass via star import of permissions package`() =
        assertViolation("capability-usage", "$ROOT.core.permissions") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.domain\n\nimport $ROOT.core.permissions.*\n\nval x = 1"),
            ).capabilityUsage()
        }

    @Test
    fun `capability-usage bypass via fully qualified moko name without import`() =
        assertViolation("capability-usage", "A.kt") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nfun bind(c: dev.icerock.moko.permissions.PermissionsController) = c",
                ),
            ).capabilityUsage()
        }

    @Test
    fun `capability-usage bypass via platform port of another feature under alias`() =
        assertViolation("capability-usage", "BarPicker") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation\n\nimport $BAR.presentation.platform.BarPicker as Picker\n\nval p: Picker? = null",
                    sourceSet = "androidMain",
                ),
            ).capabilityUsage()
        }

    @Test
    fun `capability-usage bypass via di segment nested in presentation package`() =
        assertViolation("capability-usage", "PermissionsProvider") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation.di\n\nimport $ROOT.core.permissions.PermissionsProvider\n\nval p: PermissionsProvider? = null",
                ),
            ).capabilityUsage()
        }

    @Test
    fun `foreground-usage bypass via import alias in domain`() =
        assertViolation("foreground-usage", "AppForegroundProvider") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.domain\n\nimport $ROOT.core.lifecycle.AppForegroundProvider as Gate\n\nval g: Gate? = null",
                ),
            ).foregroundUsage()
        }

    @Test
    fun `foreground-usage bypass via star import in composable`() =
        assertViolation("foreground-usage", "$ROOT.core.lifecycle") {
            fixture(
                src("feature/foo", "A.kt", "package $FOO.presentation\n\nimport $ROOT.core.lifecycle.*\n\nval x = 1"),
            ).foregroundUsage()
        }

    @Test
    fun `foreground-usage bypass via fully qualified name without import`() =
        assertViolation("foreground-usage", "A.kt") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.domain\n\nfun gate(a: $ROOT.core.lifecycle.AppLifecycle) = a",
                ),
            ).foregroundUsage()
        }

    @Test
    fun `foreground-usage bypass via data segment nested in presentation package`() =
        assertViolation("foreground-usage", "AppForegroundProvider") {
            fixture(
                src(
                    "feature/foo",
                    "A.kt",
                    "package $FOO.presentation.data\n\nimport $ROOT.core.lifecycle.AppForegroundProvider\n\nval a: AppForegroundProvider? = null",
                    sourceSet = "iosMain",
                ),
            ).foregroundUsage()
        }

    @Test
    fun `foreground-usage bypass via another core module`() =
        assertViolation("foreground-usage", "AppForegroundProvider") {
            fixture(
                src(
                    "core/ui",
                    "A.kt",
                    "package $ROOT.core.ui\n\nimport $ROOT.core.lifecycle.AppForegroundProvider\n\nval a: AppForegroundProvider? = null",
                ),
            ).foregroundUsage()
        }
}
