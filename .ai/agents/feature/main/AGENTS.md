# feature/main

- Авторизованная зона приложения: главный home-экран монтажника с topbar, адаптивным drawer, основными сценариями монтажа и сеткой инструментов.
- Фича содержит `presentation` и узкий `MainRepository` для startup-логики home: профиль пользователя, синхронизация версии базы и проверка обновления приложения. `MainScreenViewModel` зависит от `MainRepository` и `AuthStateRepository`; прямые вызовы Specialist SDK живут только в `data/repository`.
- Скелетон home держится только до профиля, возможностей и коммерческих данных пользователя (`initialRefresh`, `awaitAll`, таймаут 10 с на запрос). Синхронизация базы устройств и проверка обновления приложения идут параллельно и скелетон не держат: у них свои индикаторы вверху экрана.
- После успешного выхода экран не навигирует сам — переключение обратно на login выполняет `feature/auth-state` по обновлению состояния сессии; кнопка logout остаётся заблокированной до переключения зоны.
- Дизайн home берётся из `design/features/home` и переносится через существующий `core/ui` UI Kit; иконки карточек лежат в `composeResources/drawable` в формате Android Vector Drawable XML. Compose Multiplatform на Android не поддерживает SVG-ресурсы через `painterResource`, поэтому SVG нельзя класть в `composeResources/drawable`.
- Карточки `Requests` и `TechSupportChat` эмитят навигационное действие в чат; сам `SupportChatRoute` подключается только в `composeApp`. В drawer перехода в чат нет.
- Строковые ресурсы модуля — `main_*` (EN/RU) в `composeResources`.
