# InputMain

Поле ввода с плавающим лейблом. Мин. высота 72, во всю ширину, 1–6 строк.

| | InputMain | InputCompact |
|---|---|---|
| Паддинг по бокам | 24 | 16 |
| Нижний паддинг | 9 | 14 |
| Значение | `headLine1` 24 | `headLine2` 19 |

- Лейбл в пустом поле — `headLine1` цвета placeholder. При фокусе или с текстом уезжает вверх со scale 0.67 (≈16).
- Trailing-иконка 24, зазор 12. Если кликабельна — внутри IconButton.
- Курсор `brand-primary-default`, выделение — цвет курсора @ 30%.
- Режим «поле-кнопка» (`onClick`): ripple и scale 0.96, фокус не берёт.

**Состояния:** Default (пусто, без фокуса) → Active (фокус, пусто) → Typing (фокус + текст) → Filled (текст, без фокуса), Error, Disabled. Loading выглядит как текущее, но ввод заблокирован и курсор скрыт.

Под полем — `InputHint` (`headLine3`, по центру, зазор 12): default `foreground-on-dark-secondary-default`, error `info-default`, disabled `foreground-on-dark-tertiary`. `reserveMaxHeight` не даёт вёрстке прыгать при смене текста хинта.

### Токены `input/main/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/fg-placeholder` | `foreground/on-dark/light` | `#FFFFFF33` | `#F4F1E833` |
| `default/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `default/border-color` | `border/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `default/border-width` | `inputs-width` | `1` | `1` |
| `default/border-radius` | `inputs-radius` | `0` | `12` |
| `hover/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/fg-placeholder` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `hover/fg-icon` | `foreground/on-dark/primary/hover` | `#FFFFFFE5` | `#F4F1E8E5` |
| `hover/border-color` | `border/secondary` | `#FFFFFF99` | `#F4F1E899` |
| `hover/border-width` | `inputs-width` | `1` | `1` |
| `hover/border-radius` | `inputs-radius` | `0` | `12` |
| `active/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `active/fg-placeholder` | `foreground/on-dark/medium-highlight` | `#FFFFFF26` | `#F4F1E826` |
| `active/fg-label` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `active/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `active/cursor` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `active/border-color` | `border/primary` | `#FFFFFF` | `#F9F9FB` |
| `active/border-width` | `inputs-width` | `1` | `1` |
| `active/border-radius` | `inputs-radius` | `0` | `12` |
| `typing/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `typing/fg-value` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `typing/fg-label` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `typing/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `typing/cursor` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `typing/border-color` | `border/primary` | `#FFFFFF` | `#F9F9FB` |
| `typing/border-width` | `inputs-width` | `1` | `1` |
| `typing/border-radius` | `inputs-radius` | `0` | `12` |
| `filled/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `filled/fg-value` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `filled/fg-label` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `filled/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `filled/border-color` | `border/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `filled/border-width` | `inputs-width` | `1` | `1` |
| `filled/border-radius` | `inputs-radius` | `0` | `12` |
| `error/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `error/fg-value` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `error/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `error/fg-label` | `info/default` | `#23A3F7` | `#32D6FF` |
| `error/cursor` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `error/border-color` | `info/default` | `#23A3F7` | `#32D6FF` |
| `error/border-width` | `inputs-width` | `1` | `1` |
| `error/border-radius` | `inputs-radius` | `0` | `12` |
| `disabled/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/fg-value` | `foreground/on-dark/secondary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-label` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-icon` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `disabled/border-color` | `border/medium-highlight` | `#FFFFFF26` | `#F4F1E826` |
| `disabled/border-width` | `inputs-width` | `1` | `1` |
| `disabled/border-radius` | `inputs-radius` | `0` | `12` |

### Токены `form-field/hint/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/fg` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `inactive/fg` | `foreground/on-dark/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `success/fg` | `success/default` | `#53F153` | `#C7FF4A` |
| `error/fg` | `info/default` | `#23A3F7` | `#32D6FF` |
