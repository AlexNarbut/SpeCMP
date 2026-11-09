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
- `feature/*` — доменная и UI-логика фич, включая модели, репозитории и экраны. Сюда же относятся «сервисные» модули с доменными моделями (например, `feature/account` — профиль текущего аккаунта, общий для нескольких фич).
- `composeApp` — общая Compose UI библиотека (зависит от всех core и feature модулей), точка входа: `App()`
- Платформенные приложения (`androidApp`, `desktopApp`, `iosApp`) — только тонкие обёртки

**Правило границ feature-модулей:**
- `feature/*` не владеют app-level инфраструктурой: глобальной локалью, темой приложения, SDK-контейнерами, платформенной инициализацией сторонних библиотек, глобальными настройками, навигационным composition root и DI-модулями всего приложения.
- Если функциональность может понадобиться двум и более фичам или описывает состояние всего приложения, она должна жить в `core/*` или подключаться в `composeApp`, а feature-модуль должен только зависеть от узкого публичного API.
- Feature-репозитории не принимают "большие" SDK/application-контейнеры вроде `SpecialistApp`. В конструктор передаются только конкретные interactors/data sources/provider-ы, реально используемые классом.
- Feature DI-модуль регистрирует только зависимости своей фичи. Создание singleton-ов внешних SDK и platform/app-wide объектов выполняется в профильном `core/*` модуле или в `composeApp`.
- Обёртки над SDK-методами (репозитории) живут в атомарных фичах, владеющих соответствующей областью (например, session-операции — в `feature/auth-state`). Экранная фича не заводит собственный репозиторий ради одного-двух вызовов — её ViewModel подключает репозиторий профильной фичи как узкую зависимость (пример: `MainScreenViewModel` → `AuthStateRepository.logout()`).

---

## Типы feature-модулей — классифицируй фичу до создания

Перед созданием фичи явно определи, к какому типу она относится. Тип задаёт состав модуля,
его зависимости и то, как фичу переиспользуют другие. Классификация — часть постановки
задачи на фичу, а не постфактум-рефакторинг.

| Тип | Состав | Кто подключает | Пример в проекте |
|---|---|---|---|
| **Логическая** | `domain` + `data` + DI, без UI (или с минимальным служебным presentation) | ViewModel других фич — как узкий API (repository/interactor) | `feature/auth-state` (session-операции; presentation — только переключатель зон) |
| **Обособленная** | `domain` + `data` + `presentation` в одном модуле | Только `composeApp` (публичный вход — nav-граф/экран) | `feature/login` |
| **Экранная (композиционная)** | Только `presentation`; бизнес-логику подключает из логических фич | Только `composeApp` | `feature/main` (logout через `AuthStateRepository` из `feature/auth-state`) |
| **Крупная → пара модулей** | `feature/<name>` — только логика; `feature/<name>-ui` — presentation, зависящий от `feature/<name>` | Логику — любые фичи; `-ui` — только `composeApp` | — (появится по мере роста) |

### Правила зависимостей между типами

- Направление всегда одно: **presentation → логика**. `feature/<name>-ui` зависит от
  `feature/<name>`; логический модуль никогда не зависит от UI-модуля и не тянет
  Compose-зависимости — потребители подключают его легковесно, без транзитивного UI.
- **Межфичевое переиспользование — только через логические модули.** Чужой presentation
  (`*-ui`, экраны, ViewModel) фича не подключает никогда: экраны интегрирует `composeApp`
  через навигацию, логику — ViewModel через узкий API (см. «Правило границ feature-модулей»
  выше).
- Исключение — общий presentation фич устройства `feature/device/presentation-core` (база VM блока
  `BaseDeviceViewModel`, диалог PIN, компоненты и порты файлов конфигураций, запрос выбора конфигурации): его
  подключают фичи устройства и конфигуратор; сам он `*-ui` не подключает, экранов и DI в нём нет. Экран другой фичи
  (редактор настроек в конфигураторе, экраны конфигуратора в редакторе) — слот, который заполняет `composeApp`
  (graph-assert `:feature:.* -X> :feature:.*-ui`, Konsist `cross-feature-presentation`).
- «Только UI-компоненты» — это не фича: доменно-нейтральные компоненты живут в `core/ui`.
  Feature-модулю принадлежат лишь доменные UI-блоки (виджет конкретной бизнес-сущности) —
  и тогда это `presentation` соответствующей фичи или её `-ui` модуль.

### Когда разделять на `<name>` + `<name>-ui`

Начинай с обособленной фичи — **не создавай пару модулей «на будущее»**. Переиспользование логики
уточняется у владельца при планировании (`docs/patterns.md`, «Планирование фичи: один модуль или пара»).
Дели при первом реальном триггере:

1. Логику фичи захотела переиспользовать другая фича, а модуль тянет за собой
   presentation/Compose.
2. Presentation вырос: больше 3–4 экранов или заметный слой UI-логики/компонентов —
   модуль перестал быть обозримым.
3. Логика нужна в окружении без UI (фоновая синхронизация, платформенный сервис, тесты
   без Compose-рантайма).

Механика разделения: `domain` + `data` + их DI переезжают в `feature/<name>` без смены
пакетов; `presentation` и его DI — в `feature/<name>-ui`. У каждого модуля свой
Koin-модуль (`<name>Module` — repository/interactors, `<name>UiModule` — ViewModel);
`composeApp` подключает оба. Публичный вход UI-модуля — его nav-граф.

### Чек-лист классификации

- Кто потребитель фичи: другие фичи (→ логическая / пара модулей) или только пользователь
  через экраны (→ обособленная / экранная)?
- Есть ли у фичи собственная бизнес-логика, или она компонует чужую (→ экранная)?
- Сколько экранов и UI-логики ожидается: умещается в 3–4 экрана → один модуль; больше или
  логика уже переиспользуется → пара `<name>` + `<name>-ui`.

---

## Зона ответственности модулей

### core/common
- Пакет: `ru.alarmtrade.specmp.core.common`
- `Response<T>` — результат бизнес-операции: `Success<T>` | `Error`
- `ResponseExt` — функции-расширения
- `AppDispatchers` — обёртка над корутин-диспетчерами
- `AuthCloseable` — контракт компонентов, обязанных чистить состояние при смене аккаунта (`release()` вызывает `LogoutUseCase`)
- `appScopeQualifier` — application-scoped `CoroutineScope` для работы, переживающей инициатора
- `AppSettingsFactory` — именованные key-value хранилища (multiplatform-settings, `api`-зависимость)
- Без UI-зависимостей

### core/network
- Пакет: `ru.alarmtrade.specmp.core.network`
- HTTP-движки Ktor для SDK и Coil (OkHttp на Android/JVM, Darwin на iOS); `NetworkMonitor` — доступность сети

### core/localization
- Пакет: `ru.alarmtrade.specmp.core.localization`
- `AppLocale` — поддерживаемые языки приложения
- `currentAppLocale()` / `saveAppLocale()` / `AppLocaleStore` — язык приложения (применяется на старте: `prepareAppLocale()`, Android — `Context.withAppLocale()` в `attachBaseContext`); см. `docs/localization.md`

### core/specialist
- Пакет: `ru.alarmtrade.specmp.core.specialist`
- `di/specialistModule` — Koin-модуль инициализации `SpecialistApp`
- Регистрирует app-level SDK singleton и конкретные SDK interactors для feature-модулей
- Содержит platform-specific сборку `AppDeviceInformation` для Specialist SDK
- Связывает `AppVersionProvider` (`core/common`) с версией от платформы — её читают Specialist SDK и экраны
- `feature/*` не создают `SpecialistApp` самостоятельно и зависят только от нужных SDK interactors

### core/ui
- Пакет: `ru.alarmtrade.specmp.core.ui`
- `LCEState<T, E>` — UI-состояние: `None` | `Loading` | `Content<T>` (+ `isUpdating`/`updateError`) | `Error<E>`, `E` — ошибка фичи с подтипом `Common(AppError)` (`docs/patterns.md`, «Ошибки фичи»)
- `AppError` (`core/common`) — общие ошибки `ApiError` / `PlatformError`; текст — `AppError.message()` в `core/ui`
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
- На convention-плагинах `specmp.kmp.library` / `specmp.kmp.compose` (`build-logic`, `docs/build.md`): таргеты, JVM,
  SDK, namespace и `androidResources` задаёт конвенция; `flasherApp` без iOS — `specmp.kmp.android-jvm`
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

Все feature-модули живут в `feature/*` (каждый — отдельный Gradle-модуль `:feature:<name>`).

> **Описание конкретного модуля — в его `AGENTS.md`** (источник: `.ai/agents/feature/<name>/AGENTS.md` → `.ai/generate.sh`). `architecture.md` описывает только общие правила и границы, отдельные фичи здесь не перечисляются и не описываются — чтобы документ не распухал. Добавил фичу — заведи ей `AGENTS.md`, а не абзац здесь.

**Зависимости feature-модулей от core:**

| Зависимость | Что даёт | Когда нужна |
|-------------|----------|-------------|
| `:core:common` | `Response<T>`, `ResponseExt`, `AppDispatchers` | **Всегда** — каждый feature-модуль обязан зависеть от `core:common` |
| `:core:localization` | Строки `Strings`, `AppLocale` / `AppLocaleStore` | Всем фичам с UI (строки); выбор языка — только `feature/app-settings` |
| `:core:ui` | `LCEState<T, E>`, `LCEStateExt`, `AppError.message()`, `AppTheme`, Cmp*-компоненты | Только если у фичи есть UI-слой (presentation) |
| `:core:network` | `NetworkMonitor`, HTTP-движки Ktor процесса | Фичам обычно не нужен — сеть идёт через Specialist SDK |

Примеры `build.gradle.kts`:

```kotlin
// Feature с UI и compose resources
plugins {
    id("specmp.kmp.compose")
}

compose.resources {
    publicResClass = false
    generateResClass = always
}

kotlin {
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

> **Важно:** `androidResources.enable = true` для модуля с `composeResources/` ставит `specmp.kmp.compose`. Без него ресурсы не будут упакованы в Android APK и приложение упадёт с `MissingResourceException` в runtime.

> **Примечание:** `core:ui` зависит от `core:common` через `implementation`, поэтому `Response` и `AppDispatchers` **не** доступны транзитивно. Feature-модули **обязаны** явно подключать `:core:common`.

---

## Пространства имён

| Модуль            | Пакет                                     |
|-------------------|-------------------------------------------|
| core/common       | `ru.alarmtrade.specmp.core.common`        |
| core/localization | `ru.alarmtrade.specmp.core.localization`  |
| core/network      | `ru.alarmtrade.specmp.core.network`       |
| core/specialist   | `ru.alarmtrade.specmp.core.specialist`    |
| core/ui           | `ru.alarmtrade.specmp.core.ui`            |
| feature/`<name>`  | `ru.alarmtrade.specmp.feature.<name>` (дефис в имени модуля схлопывается: `auth-state` → `authstate`) |
| feature/device/presentation-core | `ru.alarmtrade.specmp.feature.device.presentation.core` (суффиксы `-ui`/`-core` — подпакет) |
| composeApp        | `ru.alarmtrade.specmp`                    |
| androidApp        | `ru.alarmtrade.specmp.androidApp`         |

JVM target: **Java 17**
