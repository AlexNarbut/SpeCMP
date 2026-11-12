# Сборка — Команды / Зависимости / Ресурсы / Настройка платформ

## Возможности по платформам

| Возможность | macOS | Windows | Ubuntu |
|---|---|---|---|
| Сборка Android APK | ✅ | ✅ | ✅ |
| Сборка iOS framework | ✅ | ❌ | ❌ |
| Запуск Desktop приложения | ✅ | ✅ | ✅ |
| Нативный Desktop пакет | DMG | MSI | DEB |
| Android Emulator (аппаратная) | ✅ Hypervisor | ✅ WHPX/HAXM | ✅ KVM |

---

## Команды сборки

| Задача | Команда |
|---|---|
| Сборка для проверки изменений (оба composition root, Android + desktop) | `./gradlew :androidApp:assembleDebug :flasherAndroidApp:assembleDebug :desktopApp:compileKotlin :flasherDesktopApp:compileKotlin` |
| Собрать всё (вкл. iOS, долго) | `./gradlew build` |
| Android debug APK | `./gradlew :androidApp:assembleDebug` (flasher — `:flasherAndroidApp:assembleDebug`) |
| Компиляция iOS (правки `iosMain`, `expect`/`actual`, перед PR) | `./gradlew :composeApp:compileKotlinIosArm64` |
| iOS framework | `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` |
| Запустить Desktop | `./gradlew :desktopApp:run` |
| Собрать Desktop пакет | `./gradlew :desktopApp:packageDistributionForCurrentOS` |
| Тесты (commonTest + jvmTest на JVM) | `./gradlew jvmTest` / модуля — `./gradlew :feature:login:jvmTest`; `allTests`/`test` не использовать — гоняют iOS, Android host-тестов нет |
| Стиль и импорты (ktlint + compose-rules) | `./gradlew spotlessCheck` / автофикс — `./gradlew spotlessApply` ([`code-quality.md`](code-quality.md)) |
| Смысловой анализ (detekt) | `./gradlew detektCheck --continue` / модуля — `./gradlew :feature:login:detektCheck` |
| Архитектура (Konsist) / свои правила detekt | `./gradlew :quality:konsist:test` / `./gradlew :quality:detekt-rules:test` |
| Скопированный код (CPD) / граф модулей | `./gradlew cpdCheck` / `./gradlew assertModuleGraph` |
| Очистить | `./gradlew clean` |
| Очистить Konan-кеш | `rm -rf ~/.konan/cache` |

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

Desktop-пакет: `desktopApp/build/compose/binaries/main/`

## Протокол верификации

Изменение кода считается готовым, когда:

1. **Сборка проходит** — оба composition root (`composeApp` и `flasherApp` на тех же `feature/device/*`), Android и
   desktop:

   ```bash
   ./gradlew :androidApp:assembleDebug :flasherAndroidApp:assembleDebug :desktopApp:compileKotlin :flasherDesktopApp:compileKotlin
   ```

   Правки `iosMain` или `expect`/`actual` — ещё `./gradlew :composeApp:compileKotlinIosArm64`; перед PR — всегда.
2. **Качество кода ⚠️ ОБЯЗАТЕЛЬНАЯ СТАДИЯ:**

   ```bash
   ./gradlew spotlessApply   # автофикс стиля и импортов (всё дерево)
   ./gradlew spotlessCheck detektCheck cpdCheck assertModuleGraph :quality:konsist:test :quality:detekt-rules:test --continue
   ```

   Все прогоны должны проходить без замечаний: стиль, небезопасный код, ошибки Compose, архитектура слоёв и модулей,
   копии кода. Что именно проверяется, как это устроено и как подавлять — [`code-quality.md`](code-quality.md).
   `spotlessCheck` запускается git-хуком pre-commit, `detektCheck` — pre-push (активация: `pre-commit install`).
3. **Тесты затронутых модулей зелёные:** `./gradlew :<module>:jvmTest` (все — `./gradlew jvmTest`).

**Desktop + USB на macOS (Apple Silicon).** USB-слой SDK на JVM (`usb4java` 1.3.0) содержит нативную библиотеку
только для `darwin-x86-64`: на arm64-JDK USB-устройства молча не находятся. Запускать на x86_64-JDK через
`compose.jvmHome` — в `local.properties` (`compose.jvmHome=/path/to/x86_64-jdk-17/Contents/Home`) или
`./gradlew :desktopApp:run -Pcompose.jvmHome=…`. Без ключа на macOS arm64 `desktopApp/build.gradle.kts` сам берёт
установленный x86_64-JDK 17 (`/usr/libexec/java_home -a x86_64 -v 17`); не нашёл — JDK Gradle (USB не заработает).
Явный `compose.jvmHome` приоритетнее; нет x86_64-JDK — `run` предупреждает об этом при запуске.
В репозитории есть общая Run-конфигурация `desktopApp` (`.run/`, Gradle `:desktopApp:run`): IDE подхватывает её при
открытии проекта — запускать её, а не `main()` из гуттера (гуттер берёт JDK проекта и обходит этот выбор). Если USB не находится — проверить, на каком JDK запущено приложение (запуск
из IDE берёт JDK из настроек Run/Gradle, а не из `desktopApp`).

---

## Добавление зависимостей

Все зависимости добавляются в `gradle/libs.versions.toml`:

```toml
[versions]
my-lib = "1.0.0"

[libraries]
my-lib = { module = "com.example:my-lib", version.ref = "my-lib" }
```

Использование в `build.gradle.kts`:

```kotlin
commonMain.dependencies {
    implementation(libs.my.lib)   // все платформы
}
androidMain.dependencies {
    implementation(libs.my.lib)   // только Android
}
jvmMain.dependencies {
    implementation(libs.my.lib)   // Desktop/JVM
}
iosMain.dependencies {
    implementation(libs.my.lib)   // только iOS
}
```

---

## Convention-плагины (`build-logic`)

`build-logic` — included build (`settings.gradle.kts` → `pluginManagement { includeBuild("build-logic") }`) с
плагинами в `build-logic/src/main/kotlin`; версии — из общего `gradle/libs.versions.toml`.

| Плагин | Что делает |
|---|---|
| `specmp.kmp.android-jvm` | KMP + `com.android.kotlin.multiplatform.library`; таргеты android и `jvm()`, без iOS; JVM 17; `compileSdk`/`minSdk` из каталога; `namespace` по умолчанию из пути модуля без дефисов (`:feature:auth-state` → `ru.alarmtrade.specmp.feature.authstate`). Напрямую — только модулям без iOS (`flasherApp`) |
| `specmp.kmp.library` | `specmp.kmp.android-jvm` + `iosArm64()` |
| `specmp.kmp.compose` | `org.jetbrains.compose` + Compose-компилятор поверх `specmp.kmp.library` (или поверх `specmp.kmp.android-jvm`, если он объявлен раньше); `androidResources.enable = true`, если у модуля есть `src/commonMain/composeResources` |
| `specmp.detekt` | detekt и задача `detektCheck` модуля ([`code-quality.md`](code-quality.md)); применяется из корня, в модулях не указывается |

Модуль на конвенции объявляет только своё: `plugins { id("specmp.kmp.compose") }`, при необходимости другой
`namespace` (`kotlin { android { namespace = … } }`), iOS-framework (`kotlin { iosArm64 { binaries.framework { … } } }`),
`compose.resources` и зависимости. Плагины AGP/KGP в `build-logic` — `compileOnly`: их классы приходят из корневого
`build.gradle.kts` (`alias(...).apply(false)`), поэтому новые плагины, которые применяет конвенция, тоже объявляются
там. На конвенциях — все KMP-модули: `core/*`, `feature/*`, `composeApp`, `flasherApp` (`plugins { id("specmp.kmp.android-jvm");
id("specmp.kmp.compose") }`). Явный `namespace` — у `composeApp`, `flasherApp` и `-ui`-модулей (`…feature.vehicle.ui`).

Модули тестовых дублёров (`core/testing`, `feature/<x>/testing`) — тоже `specmp.kmp.library`, код в `commonMain`
(KMP не даёт `testFixtures`); подключаются только в тестовых source set'ах —
`commonTest.dependencies { implementation(project(":core:testing")) }` (Konsist `testing-modules-test-only`,
`docs/patterns.md` → «Тестовые дублёры»). Новый модуль — `include` в `settings.gradle.kts` и `.ai/agents/<путь>/AGENTS.md`.

## Compose-компилятор: конфиг стабильности

Корневой `build.gradle.kts` подключает `config/compose/stability.conf` ко всем модулям с плагином Compose-компилятора
(`plugins.withId("org.jetbrains.kotlin.plugin.compose")`) — в модулях ничего настраивать не нужно. Что и почему там
объявлено стабильным — `docs/compose-best-prac.md`, §3.5 «Конфиг стабильности».

## Ресурсы

- **Строки** — только в `core/localization` (`composeResources/values*/<префикс фичи>.xml`, публичный класс `Strings`).
  Правила — `docs/localization.md`. Модулю для строк нужна лишь зависимость `implementation(project(":core:localization"))`.
- **Картинки и шрифты** — в `composeResources/` своего модуля (`drawable/`, `font/`), класс `Res` модуля.

```kotlin
stringResource(Strings.string.login_login_title_screen)   // core/localization
painterResource(Res.drawable.ic_logo)                      // ресурс модуля
```

### Модуль со своими ресурсами (картинки)

- `androidResources.enable = true` включает `specmp.kmp.compose` по наличию `src/commonMain/composeResources/`, руками
  не задаётся; без него ресурсы не попадут в Android APK.
- Блок `compose.resources`:

  ```kotlin
  // Ресурсы фичи — только картинки (drawable); строки — в core/localization (docs/localization.md)
  compose.resources {
      publicResClass = false
      generateResClass = always
  }
  ```

- Пакет `Res`-класса формируется из `rootProject.name` + путь модуля: `feature/login` →
  `specmp.feature.login.generated.resources`. У `core/localization` пакет и имя заданы явно:
  `ru.alarmtrade.specmp.core.localization.resources.Strings` (`packageOfResClass`, `nameOfResClass`).
- Модулю без своих ресурсов блок `compose.resources` не нужен.

---

## Платформо-специфичный код (expect/actual)

Предпочитать `commonMain`. Использовать `expect`/`actual` только для по-настоящему платформо-специфичного API.

```kotlin
// commonMain
expect fun platformName(): String

// androidMain
actual fun platformName(): String = "Android ${Build.VERSION.SDK_INT}"

// iosMain
actual fun platformName(): String = UIDevice.currentDevice.systemName()
```

Наборы источников: `commonMain`, `androidMain`, `jvmMain`, `iosMain`

---

## Версии ключевых библиотек

| Библиотека | Версия | Назначение |
|---|---|---|
| Compose Multiplatform | 1.10.0-rc02 | UI-фреймворк |
| Material3 | 1.10.0-alpha05 | Дизайн-компоненты |
| Ktor | 3.3.3 | HTTP-клиент |
| AndroidX Lifecycle | 2.10.0-alpha07 | ViewModel + Compose |
| AndroidX Navigation3 | 1.1.0-alpha01 | Навигация |
| Koin | 4.0.0 | Внедрение зависимостей |
| Kermit | 2.0.8 | Мультиплатформенное логирование |
| Kotlinx Serialization | 1.9.0 | JSON |
| Kotlinx Coroutines | 1.10.2 | Async |
| Coil | 3.3.0 | Загрузка изображений |
| Specialist SDK (`ru.alarmtrade.common:specialist`) | 1.2.2 | SDK специалиста; common-либы `ru.alarmtrade.common:*` — 2.0.114 (одна линия с SDK) |

Актуальные версии — только в `gradle/libs.versions.toml`; таблица — ориентир. `usb-serial-for-android` (транзитивно из
`serialport-android` SDK) приходит с JitPack — репозиторий подключён в корневом `build.gradle.kts` только для группы
`com.github.mik3y`.

## Версия приложения

Единый источник — `app-versionName` / `app-versionCode` в `gradle/libs.versions.toml`: из него Android
(`versionName`/`versionCode`), desktop (`packageVersion` и генерируемый задачей `generateVersionProperties`
`version.properties`). iOS берёт версию из `iosApp/iosApp/Info.plist` (её ведёт Xcode) — при выпуске обновлять и его.
Платформа передаёт версию в `App(appVersionName, appVersionCode)`; в коде (`core/specialist`, обёртки) номеров версии нет.

---

## Настройка платформ

### Общие требования (все платформы)

- **JDK 17** — Eclipse Temurin 17 LTS или Azul Zulu 17 (JDK 21+ работает, 23+ не тестировался)
- Всегда использовать **Gradle wrapper**: `./gradlew` (Unix/macOS) или `gradlew.bat` (Windows)
- Первая сборка скачивает Kotlin/Native toolchain (~300–500 MB в `~/.konan/`), занимает 10–20 мин

### Android SDK levels

| Параметр | Значение |
|---|---|
| compileSdk | 36 |
| minSdk | 24 |
| targetSdk | 36 |

`minSdk = 24` требуется Android-артефактом `ru.alarmtrade.common:specialist-android`.
Android wrapper также объявляет `ACCESS_COARSE_LOCATION`, потому что транзитивный
`bluetooth-common` manifest добавляет `ACCESS_FINE_LOCATION`, а Android 12+ lint
требует запрашивать оба location-разрешения парой.

---

### Экран и фоновая работа

Прошивка блока по BLE идёт 15–20 минут; если приложение приостановлено дольше ~5 с, загрузчик блока сбрасывает приём
(`ERROR_TIMEOUT`) и прошивка начинается заново.

| Платформа | Экран | Фон |
|---|---|---|
| Android (`androidApp`, `flasherAndroidApp`) | `FLAG_KEEP_SCREEN_ON` в Activity, пока приложение на переднем плане | нет (foreground service не сделан) |
| iOS (`iosApp`) | `isIdleTimerDisabled = true` в `iosApp.swift` | `UIBackgroundModes = bluetooth-central` в `Info.plist` |
| Desktop | не держим | не нужен |

### macOS

Единственная платформа для обоих целей: Android + iOS.

**1. Xcode**
```bash
xcode-select --install
sudo xcodebuild -license accept
# Минимум Xcode 15.x для iosArm64 / iosX64 / iosSimulatorArm64
```

**2. JDK 17**
```bash
brew install --cask temurin@17
# или: curl -s "https://get.sdkman.io" | bash && sdk install java 17.0.12-tem
```

**3. Android SDK** — через Android Studio (SDK Manager → Platform 36, Build-Tools 36) или:
```bash
brew install --cask android-commandlinetools
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
```

**4. `~/.zshrc`**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

**5. `local.properties`** (не коммитить)
```properties
sdk.dir=/Users/<username>/Library/Android/sdk
```

**6. iOS в Xcode**
```bash
open iosApp/iosApp.xcodeproj
# Build Phase автоматически вызывает:
# ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Основные настройки iOS находятся в `iosApp/Configuration/Config.xcconfig`:

```xcconfig
PRODUCT_NAME = SpeCMP
PRODUCT_BUNDLE_IDENTIFIER = ru.alarmtrade.specmp.iosApp
IPHONEOS_DEPLOYMENT_TARGET = 17.2
TEAM_ID =
```

Для симулятора `TEAM_ID` можно оставить пустым. Для запуска на физическом
устройстве укажите Apple Developer Team ID или выберите Team в Signing &
Capabilities внутри Xcode.

**Известные проблемы macOS**

| Проблема | Решение |
|---|---|
| iOS Release build OOM | Уже `-Xmx6G`; если мало — поднять до `-Xmx8G` |
| Xcode не находит gradlew | Build Phase должна выполняться из `$SRCROOT/..` |
| Konan-кеш несовместим с новым Xcode/SDK (undefined symbols из `*-cache.a`) | `rm -rf ~/.konan/cache`, затем пересобрать framework |

---

### Windows

Цель: Android APK. iOS — недоступен.

**1. JDK 17** — скачать `.msi` с https://adoptium.net/temurin/releases/?version=17

System Properties → Advanced → Environment Variables:
```
JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot
PATH     += %JAVA_HOME%\bin
```

**2. Android SDK** — через Android Studio (`%LOCALAPPDATA%\Android\Sdk`) или вручную:
```cmd
sdkmanager.bat "platform-tools" "platforms;android-36" "build-tools;36.0.0"
```

**3. Environment Variables** (PowerShell, Admin)
```powershell
[System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot", "User")
$p = [System.Environment]::GetEnvironmentVariable("PATH", "User")
[System.Environment]::SetEnvironmentVariable("PATH", "$p;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin", "User")
```

**4. `local.properties`**
```properties
sdk.dir=C\:\\Users\\<username>\\AppData\\Local\\Android\\Sdk
```
Обратные слеши экранируются как `\\`.

**Известные проблемы Windows**

| Проблема | Решение |
|---|---|
| ⚠️ MAX_PATH = 260 символов (критично) | `Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -Value 1`; держать проект в `C:\dev\` |
| | `git config --system core.longpaths true` |
| Зависший Gradle daemon | `.\gradlew --stop` |
| Медленная сборка | Добавить `%USERPROFILE%\.gradle\` и `%USERPROFILE%\.konan\` в исключения Windows Defender |
| `'\r': command not found` в Git Bash | `git config core.autocrlf false && git checkout -- gradlew && chmod +x gradlew` |
| Android Emulator (Intel) | HAXM: https://github.com/intel/haxm/releases |
| Android Emulator (AMD) | Включить Windows Hypervisor Platform в "Windows Features" |

---

### Ubuntu Linux

Цель: Android APK. iOS — недоступен.

**1. JDK 17**
```bash
sudo apt update && sudo apt install -y openjdk-17-jdk
# или SDKMAN: curl -s "https://get.sdkman.io" | bash && sdk install java 17.0.12-tem
```

**2. Android SDK**
```bash
mkdir -p "$HOME/Android/sdk"
cd /tmp
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip -d "$HOME/Android/sdk/cmdline-tools"
mv "$HOME/Android/sdk/cmdline-tools/cmdline-tools" "$HOME/Android/sdk/cmdline-tools/latest"

"$HOME/Android/sdk/cmdline-tools/latest/bin/sdkmanager" \
    "platform-tools" "platforms;android-36" "build-tools;36.0.0"
yes | "$HOME/Android/sdk/cmdline-tools/latest/bin/sdkmanager" --licenses
```

**3. `~/.bashrc`**
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME="$HOME/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$PATH"
```

**4. `local.properties`**
```properties
sdk.dir=/home/<username>/Android/sdk
```

**5. Android Emulator — KVM**
```bash
egrep -c '(vmx|svm)' /proc/cpuinfo   # > 0 — виртуализация есть
sudo apt install -y qemu-kvm libvirt-daemon-system cpu-checker
sudo kvm-ok
sudo usermod -aG kvm $USER && newgrp kvm
```

KVM в GitHub Actions:
```yaml
- name: Enable KVM
  run: |
    echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | \
      sudo tee /etc/udev/rules.d/99-kvm4all.rules
    sudo udevadm control --reload-rules && sudo udevadm trigger --name-match=kvm
```

**Известные проблемы Ubuntu**

| Проблема | Решение |
|---|---|
| `gradlew: Permission denied` | `chmod +x gradlew` |
| Gradle daemon OOM (8 GB RAM) | В `gradle.properties`: `-Xmx4G` вместо `-Xmx6G` |
| KVM недоступен в Docker | `docker run --device=/dev/kvm ...` |

---

## Heap-настройки

`gradle.properties` уже содержит оптимальные значения для 16 GB. При необходимости:

| RAM | `org.gradle.jvmargs` | `kotlin.daemon.jvmargs` |
|---|---|---|
| 8 GB | `-Xmx4G` | `-Xmx2G` |
| 16 GB | `-Xmx6G` ← текущее | `-Xmx4G` ← текущее |
| 32 GB+ | `-Xmx8G` | `-Xmx6G` |
