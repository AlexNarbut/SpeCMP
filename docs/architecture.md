# Архитектура — Модули и структура

## Граф зависимостей модулей

```
core/common ──┬── core/network
              ├── core/ui
              ├── core/localization
              └── core/specialist
                    ▲
                    │
              feature/* (будущие)
                    ▲
                    │
              composeApp (KMP Library)
                    ▲
                    ├────────┐
                    │        │
              androidApp  desktopApp  iosApp
              (Android)     (JVM)   (Swift wrapper)
```

**Ключевые принципы:**
- `core/*` — атомарная, широко переиспользуемая инфраструктура без бизнес-логики фич. Модули в `core/` минимально зависят от других модулей проекта и не содержат доменных моделей конкретных фич (аккаунты, дети, дневник и т.д.). Если модуль завязан на доменную логику или зависит от нескольких других модулей — он принадлежит `feature/*`.
- `feature/*` — доменная и UI-логика фич, включая модели, репозитории и экраны. Сюда же относятся «сервисные» модули с доменными моделями (например, `feature/account` для Parent/Child).
- `composeApp` — общая Compose UI библиотека (зависит от всех core и feature модулей), точка входа: `App()`
- Платформенные приложения (`androidApp`, `desktopApp`, `iosApp`) — только тонкие обёртки

**Правило границ feature-модулей:**
- `feature/*` не владеют app-level инфраструктурой: глобальной локалью, темой приложения, SDK-контейнерами, платформенной инициализацией сторонних библиотек, глобальными настройками, навигационным composition root и DI-модулями всего приложения.
- Если функциональность может понадобиться двум и более фичам или описывает состояние всего приложения, она должна жить в `core/*` или подключаться в `composeApp`, а feature-модуль должен только зависеть от узкого публичного API.
- Feature-репозитории не принимают "большие" SDK/application-контейнеры вроде `SpecialistApp`. В конструктор передаются только конкретные interactors/use cases/data sources, реально используемые классом.
- Feature DI-модуль регистрирует только зависимости своей фичи. Создание singleton-ов внешних SDK и platform/app-wide объектов выполняется в профильном `core/*` модуле или в `composeApp`.

---

## Зона ответственности модулей

### core/common
- Пакет: `ru.alarmtrade.specmp.core.common`
- `Response<T>` — результат бизнес-операции: `Success<T>` | `Error`
- `ResponseExt` — функции-расширения
- `AppDispatchers` — обёртка над корутин-диспетчерами
- Без UI-зависимостей

### core/network
- Пакет: `ru.alarmtrade.specmp.core.network`
- `HttpClientFactory` — Ktor HttpClient (OkHttp на Android, Darwin на iOS)

### core/localization
- Пакет: `ru.alarmtrade.specmp.core.localization`
- `AppLocale` — поддерживаемые языки приложения
- `currentAppLocale()` / `applyAppLocale()` — platform-specific чтение и применение локали приложения

### core/specialist
- Пакет: `ru.alarmtrade.specmp.core.specialist`
- `specialistModule` — Koin-модуль инициализации `SpecialistApp`
- Регистрирует app-level SDK singleton и конкретные SDK interactors для feature-модулей
- Содержит platform-specific сборку `AppDeviceInformation` для Specialist SDK
- `feature/*` не создают `SpecialistApp` самостоятельно и зависят только от нужных SDK interactors

### core/ui
- Пакет: `ru.alarmtrade.specmp.core.ui`
- `LCEState<T>` — UI-состояние: `None` | `Loading` | `Content<T>` | `Error`
- `AppTheme` — Material3 тема, светлая/тёмная
- **UI Kit** — все Cmp* компоненты дизайн-системы (см. `docs/ui-kit.md`)

### composeApp
- Пакет: `ru.alarmtrade.specmp`
- Плагин: `com.android.kotlin.multiplatform.library`
- Ресурсы: `composeApp/src/commonMain/composeResources/`
- Пакет генерируемых ресурсов: `specmp.composeapp.generated.resources`
- `App()` — composition root, все Koin-модули (включая платформенные) подключаются внутри common-кода, без параметров от платформы
- `composeApp/src/iosMain/kotlin/.../MainViewController.kt` — iOS entry point, создаёт `ComposeUIViewController`

### Все KMP-модули (core/*, feature/* и composeApp)
- Плагин: `alias(libs.plugins.android.kmp.library)`
- Android-конфигурация внутри `kotlin { android { } }` — НЕ в top-level `android { }`
- Нет `androidTarget { }` — заменён на `android { }` внутри `kotlin { }`
- JVM target используется платформенной обёрткой `desktopApp`

---

## Структура feature-модуля

Новые фичи создаются в `feature/<name>/` в корне проекта:

```
feature/
└── feature_name/
    ├── domain/           # Чистая бизнес-логика — без Compose, без @Serializable, без Android
    │   ├── model/        # Kotlin-сущности: sealed-классы, enum, value objects
    │   └── repository/   # Интерфейсы репозиториев, возвращающих Response<T>
    ├── data/             # DTO, mapper-ы, реализации репозиториев
    │   ├── dto/          # @Serializable DTO
    │   ├── mapper/       # Функции-расширения toDomain() / toDto()
    │   └── repository/   # Реализации репозиториев
    ├── presentation/     # ViewModels, экраны, Compose-компоненты (подробная раскладка ниже)
    │   ├── screen/<screen_name>/
    │   ├── widgets/<widget_name>/
    │   └── components/
    └── di/               # Определение Koin-модуля
```

> **`domain/model/`** — только чистый Kotlin. Без Compose, без `@Serializable`, без зависимостей от фреймворков.

### Раскладка `presentation/`

Каждый «presentation business unit» (отдельный экран или виджет) получает свою корневую папку. В корне живут только root-файлы; все вложенные sub-composables лежат в `subcomponents/` этого же юнита.

```
presentation/
├── screen/<screen_name>/            # Экран — обычно по одному на feature-модуль
│   ├── <Name>Screen.kt              # Корневая @Composable функция экрана
│   ├── <Name>ScreenState.kt         # @Immutable state (+ sealed actions / enum'ы state-слоя)
│   ├── <Name>ScreenViewModel.kt     # ViewModel — регистрируется в di/
│   ├── <Name>StateMapper.kt         # (опционально) domain → UI state
│   └── subcomponents/               # Все прочие @Composable этого экрана
│       └── *.kt                     # Header, секции, карточки, BottomSheet'ы, чарты и т.п.
├── widgets/<widget_name>/           # Встраиваемый виджет — отображается на других экранах
│   ├── <Name>Widget.kt              # Корневая @Composable функция виджета
│   ├── <Name>State.kt               # @Immutable state виджета
│   ├── <Name>ViewModel.kt           # Отдельная VM (если есть своё состояние)
│   └── subcomponents/               # Sub-composables виджета (active/idle/loading состояния и т.п.)
└── components/                      # Общие переиспользуемые composables и helpers фичи
    └── *.kt                         # BigStatCard, NormBadge, форматтеры, date-helpers и т.п.
```

**Правила размещения:**

- В корне `screen/<name>/` или `widgets/<name>/` — только `root composable + state + ViewModel + (опционально) mapper`. Ничего другого.
- Любая дополнительная @Composable функция для конкретного экрана/виджета → `<unit>/subcomponents/`.
- Composable или helper, который реально переиспользуется между разными screen/widget внутри той же фичи → `presentation/components/`.
- Composable, нужный сразу нескольким features, — поднимать в `core/ui` (`docs/ui-kit.md`), а не в `feature/*/presentation/components/`.
- Имя корневой папки юнита = имя бизнес-единицы в нижнем регистре (`screen/sleep/`, `widgets/sleep/`). Несколько экранов в одном feature-модуле — каждый в своей подпапке.

**State-файлы:**

- `*State.kt` — всегда `@Immutable` (или `@Stable`) для skip-оптимизации Compose.
- `*StateMapper.kt` — опционально; инкапсулирует сложное преобразование domain → UI и внедряется в ViewModel через Koin.

Feature-модули:
- Являются Kotlin Multiplatform библиотеками
- Подключаются в `:composeApp` для интеграции в UI
- **НЕ импортируют** `androidx.compose.material.icons` напрямую — иконки берутся из `CmpIcons` enum в `core/ui`, либо подключаются как compose resources модуля
- **НЕ объявляют** app-level типы и сервисы (`AppLocale`, глобальные settings, SDK app-контейнеры, root navigation/DI). Такие типы создаются в `core/*` или `composeApp`.

Текущие feature-модули:
- `feature/login` — авторизация специалиста. `domain`, `data`, `presentation`, use case-слои и DI фичи живут в `commonMain`; app-level инициализация `SpecialistApp` и platform-specific `AppDeviceInformation` живут в `core/specialist`. Репозиторий фичи получает только нужные SDK interactors и provider device information, не создаёт `SpecialistApp` и не содержит platform-specific DI.
  `LoginRepository` описывает auth-операции (`login`, `signUp`, `resetPassword`, `resendActivationEmail`, `isLoggedIn`, `logout`) и возвращает результаты через `Response<T>`.

**Зависимости feature-модулей от core:**

| Зависимость | Что даёт | Когда нужна |
|-------------|----------|-------------|
| `:core:common` | `Response<T>`, `ResponseExt`, `AppDispatchers` | **Всегда** — каждый feature-модуль обязан зависеть от `core:common` |
| `:core:localization` | `AppLocale`, чтение/применение локали приложения | Если фича показывает переключение языка или зависит от текущей локали |
| `:core:ui` | `LCEState<T>`, `LCEStateExt`, `AppTheme`, Cmp*-компоненты | Только если у фичи есть UI-слой (presentation) |
| `:core:network` | `HttpClientFactory`, Ktor-клиент | Только если фича работает с сетью |

Примеры `build.gradle.kts`:

```kotlin
// Feature с UI и compose resources
compose.resources {
    publicResClass = false
    generateResClass = always
}

kotlin {
    android {
        namespace = "org.evolyta.babysteps.feature.<name>"
        // ...
        androidResources.enable = true  // обязательно если есть composeResources/
    }
    // ...
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))  // Response, AppDispatchers — обязательно
            implementation(project(":core:ui"))      // LCEState, Cmp*, тема — есть UI
        }
    }
}

// Feature без UI (только domain + data)
commonMain.dependencies {
    implementation(project(":core:common"))  // Response, AppDispatchers — обязательно
}
```

> **Важно:** Если feature-модуль содержит `composeResources/`, в `kotlin { android { } }` необходимо добавить `androidResources.enable = true`. Без этого ресурсы не будут упакованы в Android APK и приложение упадёт с `MissingResourceException` в runtime.

> **Примечание:** `core:ui` зависит от `core:common` через `implementation`, поэтому `Response` и `AppDispatchers` **не** доступны транзитивно. Feature-модули **обязаны** явно подключать `:core:common`.

---

## Пространства имён

| Модуль                 | Пакет                                        |
|------------------------|----------------------------------------------|
| core/common            | `org.evolyta.babysteps.core.common`          |
| core/network           | `org.evolyta.babysteps.core.network`         |
| core/ui                | `org.evolyta.babysteps.core.ui`              |
| core/live-notification | `org.evolyta.babysteps.core.livenotification`|
| composeApp             | `org.evolyta.babysteps`                      |
| androidApp             | `org.evolyta.babysteps.androidApp`           |

JVM target: **Java 17**
