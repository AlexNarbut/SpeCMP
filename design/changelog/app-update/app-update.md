# Экран обновления приложения (desktop)

**Экраны:** app-update
**Файлы:** features/app-update/AppUpdateScreen.jsx, features/app-update/AppUpdate.html, features/app-update/README.md, features/home/HomeScreens.jsx (route=update), features/home/HomeDesktop.html (подключение), res/nav.js

## Что изменилось
- Новая фича `features/app-update/` — desktop-возможность проверки и загрузки более свежей версии
  приложения (сборка `.dmg` и т.п.). Отдельно от «Обновить базу».
- `AppUpdateScreen.jsx` — единый источник правды, только Cmp*-компоненты:
  - `UpdateScreen` — презентационное тело экрана, рендерится по `state` (топбар даёт хост).
  - `UpdateFlow` — контроллер полного потока: `checking → available → downloading` (детерминированный
    прогресс) `→ downloaded → install → uptodate`, плюс `error`.
  - `AppUpdateApp` — standalone-экран (топбар + поток) либо закреплённое состояние (`pinnedState`).
  - Экспорт `window.AppUpdate`.
- `AppUpdate.html` — desktop-окно: тумблер темы, «Прототип» (живой поток загрузки) / «Состояния»
  (галерея: available / checking / downloading / downloaded / uptodate / error).
- Состояния: версия-пилюли (установлено→доступно), «что нового», кнопки «Скачать .dmg» / «Позже»,
  детерминированный прогресс с «Отменить», «Установить и перезапустить», ошибка проверки.

## Связь с главным экраном
- В боковом меню главного экрана (desktop) — строка «Проверить обновления» и блок «Доступна новая
  версия» (Telegram-style) внизу бара. По нажатию — route `update`, где `HomeScreen` рендерит
  `window.AppUpdate.UpdateFlow` (топбар с «Назад» даёт главный экран).
- `HomeDesktop.html` подгружает `../app-update/AppUpdateScreen.jsx`. Ключ nav `app-update`.

## Зачем
Проверка/загрузка новой версии приложения — отдельный от синхронизации базы сценарий; вынесен
в самостоятельную фичу с единым UI, переиспользуемым главным экраном и собственным хостом.

## Заметки
- Поток — прототип (демо-данные версий 2.4.1 → 2.5.0, размер 84 МБ). Версии/состояние — пропсы
  `current` / `latest` / `sizeMB` / `initialState` / `pinnedState`.
- Возможность только desktop; на mobile/tablet UI обновления не показывается (`appUpdate=null`).
