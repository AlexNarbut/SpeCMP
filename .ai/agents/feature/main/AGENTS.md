# feature/main

- Авторизованная зона приложения: главный home-экран монтажника с topbar, адаптивным drawer, основными сценариями монтажа и сеткой инструментов.
- Фича содержит `presentation` и узкий `MainRepository` для startup-логики home: профиль пользователя, синхронизация версии базы и проверка обновления приложения. `MainScreenViewModel` зависит от `MainRepository` и `AuthStateRepository`; прямые вызовы Specialist SDK живут только в `data/repository`.
- После успешного выхода экран не навигирует сам — переключение обратно на login выполняет `feature/auth-state` по обновлению состояния сессии; кнопка logout остаётся заблокированной до переключения зоны.
- Дизайн home берётся из `design/features/home` и переносится через существующий `core/ui` UI Kit; реальные SVG-иконки карточек лежат в `composeResources/drawable`.
- Строковые ресурсы модуля — `main_*` (EN/RU) в `composeResources`.
