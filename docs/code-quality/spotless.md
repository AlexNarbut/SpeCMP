# Spotless

Gradle-плагин [`com.diffplug.spotless`](https://github.com/diffplug/spotless). Сам ничего не «понимает» в Kotlin: он запускает форматтеры (ktlint, ktfmt, license header и др.) и умеет **проверять** и **применять** их результат.

## Зачем он нам

- Одна пара команд на весь репозиторий: `spotlessCheck` (падает, если код не по формату) и `spotlessApply` (чинит).
- **Автофикс** неиспользуемых импортов, порядка импортов, отступов: агентам и людям не нужно исправлять это руками.
- **`ratchetFrom`**: проверять только файлы, изменённые относительно ветки. Позволяет ввести правила без массового коммита по всему репо.
- Поддерживает конфигурационный кэш Gradle и работает в мультимодульных проектах.

## Основные задачи

| Задача | Что делает |
|---|---|
| `./gradlew spotlessCheck` | Проверка. Подходит для CI и pre-commit |
| `./gradlew spotlessApply` | Переписывает файлы по правилам |
| `./gradlew spotlessCheck -PspotlessRatchetFrom=main` | Проверка всего, что изменено относительно `main` |

`spotlessCheck` подключается к `check` корневого проекта, поэтому `./gradlew check` тоже упадёт на плохом форматировании.

## Минимальная настройка

Общая схема с отдельным Spotless на каждый подпроект (у нас вместо этого один корневой блок `spotless { }` с `fileTree` на весь репозиторий — см. [`../code-quality.md`](../code-quality.md)):

```kotlin
// build.gradle.kts (root)
plugins {
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**", "**/generated/**")
            ktlint(libs.versions.ktlint.get())
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(libs.versions.ktlint.get())
        }
    }
}
```

Стиль правил берётся из [`.editorconfig`](ktlint.md#editorconfig) в корне, поэтому в Gradle его не дублируем.

### Ratchet: только изменённые файлы

```kotlin
spotless {
    ratchetFrom("main")
    kotlin { /* ... */ }
}
```

Файлы, не тронутые относительно `main`, не проверяются. Старый код не блокирует, новый должен быть чистым. У нас по умолчанию ratchet от `HEAD` (ровно коммитимое), база переопределяется свойством `-PspotlessRatchetFrom=<ref>`; в CI ветка должна быть доступна (`git fetch`).

### Подключение правил compose-rules через ktlint

```kotlin
ktlint(libs.versions.ktlint.get())
    .customRuleSets(listOf("io.nlopez.compose.rules:ktlint:<version>"))
```

Подробнее в [`compose-rules.md`](compose-rules.md).

## Спорные места

- **Скорость:** ktlint внутри Spotless на всём проекте работает десятки секунд. Для pre-commit используйте `ratchetFrom` или запуск по изменённым файлам.
- **Версия ktlint** задаётся в Spotless явно. Если не указать, будет взята версия по умолчанию плагина, и она может отличаться от версии в IDE-плагине ktlint.
- **Генерируемый код** (`build/`, `generated/`, Compose Resources) обязательно исключать из целей.
- **Двойная работа с detekt.** Не включайте в detekt форматирующие правила (`detekt-formatting`), иначе ktlint запускается дважды.

## Ссылки

- [Spotless Gradle plugin: README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)
- [Changelog плагина](https://github.com/diffplug/spotless/blob/main/plugin-gradle/CHANGES.md)
