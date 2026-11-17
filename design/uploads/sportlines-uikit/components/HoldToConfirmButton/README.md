# HoldToConfirmButton

Подтверждение удержанием — для необратимых действий. Пока палец на кнопке, слева направо идёт заливка, которая инвертирует цвета. `onClick` срабатывает через 1500ms (linear) плюс пауза 200ms на 100%. Отпустил раньше — прогресс сбрасывается. Обычный тап игнорируется.

| Параметр | Значение |
|---|---|
| Высота | 60, ширина по контенту, паддинг 20 |
| Иконка | 24 (по умолчанию `alert-triangle`), зазор 8 |
| Текст | `headLine2`, UPPERCASE, 1 строка |
| Радиус / рамка | `r-buttons-radius` / `w-buttons-width` |
| Default | фон `background-surface-light`, текст `foreground-on-dark-primary-default` |
| Holding | + рамка `border-primary`; заливка `foreground-on-dark-primary-default`, текст на ней `foreground-on-light-primary-default` |
| Disabled | фон `background-surface-ultra-highlight`, текст `foreground-on-dark-primary-disabled` |

Без ripple и scale. Вариант `HoldToConfirmProgress` — тот же вид, прогресс приходит снаружи.

Для SpeCMP: сброс устройства, стирание, запуск прошивки.
