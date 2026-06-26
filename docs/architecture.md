# Архитектура — Модули и структура

## Граф зависимостей модулей

```
core/common ──┬── core/network
              └── core/ui
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
- `core/*` — переиспользуемая инфраструктура, без логики фич
- `composeApp` — общая Compose UI библиотека (зависит от всех core-модулей), точка входа: `App()`
- Платформенные приложения (`androidApp`, `desktopApp`, `iosApp`) — только тонкие обёртки

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
    ├── presentation/     # ViewModels, экраны, Compose-компоненты
    │   ├── *State.kt     # @Immutable data-классы
    │   └── *StateMapper.kt  # (опционально) маппинг domain → UI state
    └── di/               # Определение Koin-модуля
```

> **`domain/model/`** — только чистый Kotlin. Без Compose, без `@Serializable`, без зависимостей от фреймворков.
>
> **`presentation/*State.kt`** — всегда `@Immutable` (или `@Stable`) для оптимизации skip в Compose.
>
> **`presentation/*StateMapper.kt`** — опционально; инкапсулирует сложное преобразование domain → UI.
> Регистрируется в `di/` и внедряется в ViewModel через Koin.

Feature-модули:
- Являются Kotlin Multiplatform библиотеками
- Подключаются в `:composeApp` для интеграции в UI

Текущие feature-модули:
- `feature/calculator` — UI-фича калькулятора.
- `feature/login` — авторизация специалиста. Общие `domain`, `presentation` и use case-слои живут в `commonMain`; адаптер `SpecialistApp` подключён в `androidMain` и `jvmMain`, а iOS использует явную unavailable-реализацию репозитория до появления совместимой iOS-интеграции auth SDK.

**Зависимости feature-модулей от core:**

| Зависимость | Что даёт | Когда нужна |
|-------------|----------|-------------|
| `:core:common` | `Response<T>`, `ResponseExt`, `AppDispatchers` | **Всегда** — каждый feature-модуль обязан зависеть от `core:common` |
| `:core:ui` | `LCEState<T>`, `LCEStateExt`, `AppTheme`, Cmp*-компоненты | Только если у фичи есть UI-слой (presentation) |
| `:core:network` | `HttpClientFactory`, Ktor-клиент | Только если фича работает с сетью |

Примеры `build.gradle.kts`:

```kotlin
// Feature с UI
commonMain.dependencies {
    implementation(project(":core:common"))  // Response, AppDispatchers — обязательно
    implementation(project(":core:ui"))      // LCEState, Cmp*, тема — есть UI
}

// Feature без UI (только domain + data)
commonMain.dependencies {
    implementation(project(":core:common"))  // Response, AppDispatchers — обязательно
}
```

> **Примечание:** `core:ui` зависит от `core:common` через `implementation`, поэтому `Response` и `AppDispatchers` **не** доступны транзитивно. Feature-модули **обязаны** явно подключать `:core:common`.

---

## Пространства имён

| Модуль       | Пакет                              |
|--------------|------------------------------------|
| core/common  | `ru.alarmtrade.specmp.core.common`      |
| core/network | `ru.alarmtrade.specmp.core.network`     |
| core/ui      | `ru.alarmtrade.specmp.core.ui`          |
| composeApp   | `ru.alarmtrade.specmp`                  |
| androidApp   | `ru.alarmtrade.specmp.androidApp`       |
| desktopApp   | `ru.alarmtrade.specmp.desktopApp`       |

JVM target: **Java 17**
