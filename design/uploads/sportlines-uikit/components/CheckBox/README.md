# CheckBox

Чекбокс с подписью. Квадрат 24, радиус `r-checkbox-radius`, рамка 1. Галочка — иконка `check` 24. Подпись `additional0`, зазор 16. Кликабелен только квадрат: ripple радиусом 20, scale 0.96.

| Состояние | Фон | Рамка | Галочка |
|---|---|---|---|
| Выкл | `background-surface-highlight` | `border-light` | — |
| Выкл, нажат | `background-surface-light` | `border-secondary` | — |
| Вкл | `background-surface-primary` | — | `foreground-on-light-primary-default` |
| Disabled вкл | `background-surface-ultra-highlight` | — | `foreground-on-light-primary-disabled` |
| Disabled выкл | прозрачный | `border-ultra-highlight` | — |
