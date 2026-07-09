# feature/main

- Авторизованная зона приложения: главный home-экран монтажника с topbar, адаптивным drawer, основными сценариями монтажа и сеткой инструментов.
- Чисто экранная фича без собственных `domain`/`data`: `MainScreenViewModel` управляет только UI-состоянием home и подключает `AuthStateRepository` из `feature/auth-state` для `logout()`.
- После успешного выхода экран не навигирует сам — переключение обратно на login выполняет `feature/auth-state` по обновлению состояния сессии; кнопка logout остаётся заблокированной до переключения зоны.
- Дизайн home берётся из `design/features/home` и переносится через существующий `core/ui` UI Kit; реальные SVG-иконки карточек лежат в `composeResources/drawable`.
- Строковые ресурсы модуля — `main_*` (EN/RU) в `composeResources`.
