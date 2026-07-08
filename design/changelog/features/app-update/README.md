# Обновление приложения (app-update)

Desktop-возможность: проверка доступности более свежей версии приложения (сборка `.dmg` и т.п.),
загрузка и установка. Оформлено отдельной фичей, но переиспользуется главным экраном.

## Файлы

- **AppUpdateScreen.jsx** — единственный источник правды по UI обновления, только из Cmp*-компонентов:
  - `UpdateScreen` — презентационное ТЕЛО экрана, рендерится по `state`. Топбар даёт хост,
    поэтому одинаково встраивается в главный экран (route=update) и в свой хост.
  - `UpdateFlow` — контроллер полного потока: `checking → available → downloading` (детерминированный
    прогресс) `→ downloaded → (install) → uptodate`. Рендерит только тело.
  - `AppUpdateApp` — полный standalone-экран (топбар + `UpdateFlow`) либо закреплённое состояние
    (`pinnedState`) для галереи. Экспортируется как `window.AppUpdate`.
- **AppUpdate.html** — desktop-окно: тумблер темы, «Прототип» (живой поток) / «Состояния»
  (галерея: available / checking / downloading / downloaded / uptodate / error).

## Состояния

`available` (версия-пилюли + «что нового» + «Скачать .dmg»/«Позже») · `checking` (спиннер) ·
`downloading` (детерминированный прогресс + «Отменить») · `downloaded` («Установить и перезапустить») ·
`uptodate` (последняя версия) · `error` (ошибка проверки).

## Связь с главным экраном

Главный экран (`features/home/`) на desktop показывает в боковом меню строку «Проверить обновления»
и внизу — блок «Доступна новая версия» (Telegram-style). По нажатию — route `update`, где
`HomeScreen` рендерит `window.AppUpdate.UpdateFlow` (хост главного экрана даёт топбар с «Назад»).
Для этого `HomeDesktop.html` подгружает `../app-update/AppUpdateScreen.jsx`.

## Навигация

Ключ `res/nav.js`: `app-update` → `features/app-update/AppUpdate.html`.
Версии/состояние — пропсы `current` / `latest` / `sizeMB` / `pinnedState` / `initialState`.
