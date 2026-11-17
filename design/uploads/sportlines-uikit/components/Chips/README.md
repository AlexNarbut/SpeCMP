# Chips

Ряд чипов-фильтров с одиночным выбором. Высота 40, паддинг 12, зазор между чипами 4, ряд скроллится. Текст `headLine3`, опц. счётчик (показывается при > 0) с зазором 4. Радиус `r-chips-radius` (пилюля), рамка `w-chips-width`.

Фон и рамка — **вертикальные градиенты** (сверху → вниз):

| Состояние | Фон | Рамка | Текст / счётчик |
|---|---|---|---|
| Не выбран | `background-surface-transparent` → `background-surface-ultra-highlight` | `border-highlight` → `border-transparent` | `foreground-on-dark-primary-default` / `foreground-on-dark-tertiary` |
| Не выбран, нажат | `background-surface-ultra-highlight` → `background-surface-highlight` | `border-light` → `border-highlight` | `foreground-on-dark-primary-pressed` / `foreground-on-dark-light` |
| Выбран | `background-surface-primary` | нет | `foreground-on-light-primary-default` / `foreground-on-light-tertiary` |
| Выбран, нажат | `background-surface-secondary` | нет | `foreground-on-light-primary-pressed` / `foreground-on-light-light` |
| Disabled | `background-surface-ultra-highlight` | нет | `foreground-on-dark-primary-disabled` / `foreground-on-dark-highlight` |

Переходы цвета 300ms. Макет просит ещё блюр 64 под невыбранным чипом (стекло) — в коде пока не сделан.
