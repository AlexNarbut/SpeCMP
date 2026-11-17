# InputCenter

Однострочное поле с текстом по центру: суммы, телефон, пароль, промокод. Высота 72, значение `headLine1` по центру, паддинг 16. Опц. префикс (`headLine1`, цвет `fg-prefix`, отступ 4+4).

Под полем колонка с зазором 12: поле → теги быстрого выбора → хинт.
- **Теги:** ряд равных по ширине плашек высотой 40, зазор 8, `headLine3`, scale 0.96. Токены `form-field/tag/*`.
- `tags = null` — спиннер высотой 40 на месте тегов.

**PasswordInput** — то же поле + кнопка-глаз 56×56 справа (иконка 24 `eye-open` / `eye-close`). Текст отбит на 56 с обеих сторон, чтобы оставаться по центру. Показывает последний введённый символ.

Отличие от InputMain: фон `background-surface-highlight` (10%) и рамка `border-light`; лейбла нет, только placeholder.

### Токены `input/center/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `default/fg-placeholder` | `foreground/on-dark/light` | `#FFFFFF33` | `#F4F1E833` |
| `default/fg-icon` | `foreground/on-dark/light` | `#FFFFFF33` | `#F4F1E833` |
| `default/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `default/border-width` | `inputs-width` | `1` | `1` |
| `default/border-radius` | `inputs-radius` | `0` | `12` |
| `hover/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `hover/fg-placeholder` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `hover/fg-icon` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `hover/border-color` | `border/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `hover/border-width` | `inputs-width` | `1` | `1` |
| `hover/border-radius` | `inputs-radius` | `0` | `12` |
| `pressed/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `pressed/fg-placeholder` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `pressed/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `pressed/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `pressed/border-width` | `inputs-width` | `1` | `1` |
| `pressed/border-radius` | `inputs-radius` | `0` | `12` |
| `active/bg` | `background/surface/primary` | `#FFFFFF` | `#F4F1E8` |
| `active/fg-icon` | `foreground/on-light/tertiary` | `#0000004D` | `#080B184D` |
| `active/fg-prefix` | `foreground/on-light/light` | `#00000033` | `#080B1833` |
| `active/cursor` | `brand/primary/default` (1x2: `brand/secondary/default`) | `#53F153` | `#886FFB` |
| `active/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `active/border-width` | `inputs-width` | `1` | `1` |
| `active/border-radius` | `inputs-radius` | `0` | `12` |
| `typing/bg` | `background/surface/primary` | `#FFFFFF` | `#F4F1E8` |
| `typing/fg-icon` | `foreground/on-light/tertiary` | `#0000004D` | `#080B184D` |
| `typing/fg-value` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `typing/fg-prefix` | `foreground/on-light/light` | `#00000033` | `#080B1833` |
| `typing/cursor` | `brand/primary/default` (1x2: `brand/secondary/default`) | `#53F153` | `#886FFB` |
| `typing/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `typing/border-width` | `inputs-width` | `1` | `1` |
| `typing/border-radius` | `inputs-radius` | `0` | `12` |
| `filled/bg` | `background/surface/primary` | `#FFFFFF` | `#F4F1E8` |
| `filled/fg-icon` | `foreground/on-light/tertiary` | `#0000004D` | `#080B184D` |
| `filled/fg-value` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `filled/fg-prefix` | `foreground/on-light/light` | `#00000033` | `#080B1833` |
| `filled/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `filled/border-width` | `inputs-width` | `1` | `1` |
| `filled/border-radius` | `inputs-radius` | `0` | `12` |
| `error/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `error/fg-icon` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `error/fg-value` | `info/default` | `#23A3F7` | `#32D6FF` |
| `error/fg-prefix` | `info/default` | `#23A3F7` | `#32D6FF` |
| `error/cursor` | `brand/primary/default` (1x2: `brand/secondary/default`) | `#53F153` | `#886FFB` |
| `error/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `error/border-width` | `inputs-width` | `1` | `1` |
| `error/border-radius` | `inputs-radius` | `0` | `12` |
| `disabled/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/fg-icon` | `foreground/on-dark/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/fg-value` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-prefix` | `foreground/on-dark/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-width` | `inputs-width` | `1` | `1` |
| `disabled/border-radius` | `inputs-radius` | `0` | `12` |
| `focus/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `focus/fg-placeholder` | `foreground/on-dark/light` | `#FFFFFF33` | `#F4F1E833` |
| `focus/fg-icon` | `foreground/on-dark/light` | `#FFFFFF33` | `#F4F1E833` |
| `focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `focus/border-width` | `focus-width` | `3` | `3` |
| `focus/border-radius` | `inputs-radius` | `0` | `12` |
| `focus/bg 2` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |

### Токены `form-field/tag/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `default/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `default/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/border-width` | `tags-width` | `1` | `1` |
| `default/border-radius` | `tags-radius` | `0` | `8` |
| `hover/bg` | `background/surface/medium-highlight` | `#FFFFFF26` | `#F4F1E826` |
| `hover/fg` | `foreground/on-dark/primary/hover` | `#FFFFFFE5` | `#F4F1E8E5` |
| `hover/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/border-width` | `tags-width` | `1` | `1` |
| `hover/border-radius` | `tags-radius` | `0` | `8` |
| `pressed/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `pressed/fg` | `foreground/on-dark/primary/pressed` | `#FFFFFFCC` | `#F4F1E8CC` |
| `pressed/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `pressed/border-width` | `tags-width` | `1` | `1` |
| `pressed/border-radius` | `tags-radius` | `0` | `8` |
| `focus/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `focus/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `focus/border-width` | `interactive-width` | `1` | `1` |
| `focus/border-radius` | `tags-radius` | `0` | `8` |
| `disabled/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `disabled/fg` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-width` | `tags-width` | `1` | `1` |
| `disabled/border-radius` | `tags-radius` | `0` | `8` |
