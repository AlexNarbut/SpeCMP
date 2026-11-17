# Button

Основная кнопка: одна на все типы и размеры. Текст всегда UPPERCASE, нажатие — scale 0.96 + белый ripple 10%.

**Типы** (`ButtonType`): `Primary` (белая — главное действие экрана), `Secondary` (белый 10% — второстепенное), `Tertiary` (без фона — «Отмена», «Позже»), `AccentPrimary` (брендовый зелёный), `AccentSecondary` (брендовый синий), `StaticOnLight` / `StaticOnDark` (поверх промо-картинок, цвета не зависят от темы).

**Размеры**

| Size | Высота | Паддинг по бокам | Текст |
|---|---|---|---|
| S | 40 | 12 | `headLine3` 16 |
| M | 44 | 16 | `headLine2` 19 |
| L | 48 | 16 | `headLine1` 24 |
| XL | 56 | 16 | `headLine1` 24 |

**Слоты** (ряд с зазором 8): leftIcon 24 → текст → `secondText` (тот же стиль, цвет `fg-second-text`, например сумма) → rightIcon 24. Иконки красятся цветом текста.

**Состояния:** Default, Pressed, Loading, Disabled, Focus (+ Hover для десктопа).
- Loading: фон прозрачный, рамка `border-highlight` 1px, контент прячется (ширина сохраняется), по центру спиннер 30.
- Радиус `r-buttons-radius` (Sportlines 0, 1x2 12), рамка `w-buttons-width` (0).
- `autoSizeText`: шрифт уменьшается ступенями до `headLine3`, одна строка. Без него текст переносится.
- Static-типы: OnLight — фон `static-black`, текст `static-white`; OnDark — наоборот; disabled — `static-*-ultra-highlight`.

Одна Primary на экран, обычно размера L внизу экрана во всю ширину (32 от краёв).

### Токены `button/primary/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `background/surface/primary` | `#FFFFFF` | `#F4F1E8` |
| `hover/bg` | `background/surface/semi` | `#FFFFFFE5` | `#F4F1E8E5` |
| `pressed/bg` | `background/surface/primary` | `#FFFFFF` | `#F4F1E8` |
| `focus/bg` | `background/surface/primary` | `#FFFFFF` | `#F4F1E8` |
| `focus/fg` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `focus/fg-second-text` | `foreground/on-light/secondary/default` | `#00000099` | `#080B1899` |
| `focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `focus/border-width` | `focus-width` | `3` | `3` |
| `focus/border-radius` | `buttons-radius` | `0` | `12` |
| `pressed/fg` | `foreground/on-light/primary/pressed` | `#000000CC` | `#080B18CC` |
| `pressed/fg-second-text` | `foreground/on-light/secondary/pressed` | `#00000066` | `#080B1866` |
| `pressed/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `pressed/border-width` | `buttons-width` | `0` | `0` |
| `pressed/border-radius` | `buttons-radius` | `0` | `12` |
| `hover/fg` | `foreground/on-light/primary/hover` | `#000000E5` | `#080B18E5` |
| `hover/fg-second-text` | `foreground/on-light/secondary/hover` | `#00000080` | `#080B1880` |
| `hover/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/border-width` | `buttons-width` | `0` | `0` |
| `hover/border-radius` | `buttons-radius` | `0` | `12` |
| `default/fg` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `default/fg-second-text` | `foreground/on-light/secondary/default` | `#00000099` | `#080B1899` |
| `default/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/border-width` | `buttons-width` | `0` | `0` |
| `default/border-radius` | `buttons-radius` | `0` | `12` |
| `loading/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `loading/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `loading/border-color` | `border/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `loading/border-width` | `interactive-width` | `1` | `1` |
| `loading/border-radius` | `buttons-radius` | `0` | `12` |
| `disabled/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `disabled/fg` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-second-text` | `foreground/on-dark/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-width` | `buttons-width` | `0` | `0` |
| `disabled/border-radius` | `buttons-radius` | `0` | `12` |

### Токены `button/secondary/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `hover/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `pressed/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `focus/bg` | `background/surface/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `focus/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `focus/fg-second-text` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `focus/border-width` | `focus-width` | `3` | `3` |
| `focus/border-radius` | `buttons-radius` | `0` | `12` |
| `pressed/fg` | `foreground/on-dark/primary/pressed` | `#FFFFFFCC` | `#F4F1E8CC` |
| `pressed/fg-second-text` | `foreground/on-dark/secondary/pressed` | `#FFFFFF66` | `#F4F1E866` |
| `pressed/border-color` | `border/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `pressed/border-width` | `buttons-width` | `0` | `0` |
| `pressed/border-radius` | `buttons-radius` | `0` | `12` |
| `hover/fg` | `foreground/on-dark/primary/hover` | `#FFFFFFE5` | `#F4F1E8E5` |
| `hover/fg-second-text` | `foreground/on-dark/secondary/hover` | `#FFFFFF80` | `#F4F1E880` |
| `hover/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/border-width` | `buttons-width` | `0` | `0` |
| `hover/border-radius` | `buttons-radius` | `0` | `12` |
| `default/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `default/fg-second-text` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `default/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/border-width` | `buttons-width` | `0` | `0` |
| `default/border-radius` | `buttons-radius` | `0` | `12` |
| `loading/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `loading/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `loading/border-color` | `border/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `loading/border-width` | `interactive-width` | `1` | `1` |
| `loading/border-radius` | `buttons-radius` | `0` | `12` |
| `disabled/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `disabled/fg` | `foreground/on-dark/secondary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-second-text` | `foreground/on-dark/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-width` | `buttons-width` | `0` | `0` |
| `disabled/border-radius` | `buttons-radius` | `0` | `12` |

### Токены `button/tertiary/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `pressed/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `focus/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `focus/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `focus/fg-second-text` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `focus/border-width` | `focus-width` | `3` | `3` |
| `focus/border-radius` | `buttons-radius` | `0` | `12` |
| `pressed/fg` | `foreground/on-dark/primary/pressed` | `#FFFFFFCC` | `#F4F1E8CC` |
| `pressed/fg-second-text` | `foreground/on-dark/secondary/pressed` | `#FFFFFF66` | `#F4F1E866` |
| `pressed/border-color` | `border/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `pressed/border-width` | `buttons-width` | `0` | `0` |
| `pressed/border-radius` | `buttons-radius` | `0` | `12` |
| `hover/fg` | `foreground/on-dark/primary/hover` | `#FFFFFFE5` | `#F4F1E8E5` |
| `hover/fg-second-text` | `foreground/on-dark/secondary/hover` | `#FFFFFF80` | `#F4F1E880` |
| `hover/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/border-width` | `buttons-width` | `0` | `0` |
| `hover/border-radius` | `buttons-radius` | `0` | `12` |
| `default/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `default/fg-second-text` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `default/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/border-width` | `buttons-width` | `0` | `0` |
| `default/border-radius` | `buttons-radius` | `0` | `12` |
| `loading/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `loading/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `loading/border-color` | `border/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `loading/border-width` | `interactive-width` | `1` | `1` |
| `loading/border-radius` | `buttons-radius` | `0` | `12` |
| `disabled/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/fg` | `foreground/on-dark/secondary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-second-text` | `foreground/on-dark/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-width` | `buttons-width` | `0` | `0` |
| `disabled/border-radius` | `buttons-radius` | `0` | `12` |

### Токены `button/accent-primary/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `hover/bg` | `brand/primary/hover` | `#3DE93D` | `#B7FF17` |
| `pressed/bg` | `brand/primary/pressed` | `#2ACD2A` | `#93D305` |
| `focus/bg` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `focus/fg` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `focus/fg-second-text` | `foreground/on-light/secondary/default` | `#00000099` | `#080B1899` |
| `focus/border-color` | `border/primary` | `#FFFFFF` | `#F9F9FB` |
| `focus/border-width` | `focus-width` | `3` | `3` |
| `focus/border-radius` | `buttons-radius` | `0` | `12` |
| `pressed/fg` | `foreground/on-light/primary/pressed` | `#000000CC` | `#080B18CC` |
| `pressed/fg-second-text` | `foreground/on-light/secondary/pressed` | `#00000066` | `#080B1866` |
| `pressed/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `pressed/border-width` | `buttons-width` | `0` | `0` |
| `pressed/border-radius` | `buttons-radius` | `0` | `12` |
| `hover/fg` | `foreground/on-light/primary/hover` | `#000000E5` | `#080B18E5` |
| `hover/fg-second-text` | `foreground/on-light/secondary/hover` | `#00000080` | `#080B1880` |
| `hover/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/border-width` | `buttons-width` | `0` | `0` |
| `hover/border-radius` | `buttons-radius` | `0` | `12` |
| `default/fg` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `default/fg-second-text` | `foreground/on-light/secondary/default` | `#00000099` | `#080B1899` |
| `default/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/border-width` | `buttons-width` | `0` | `0` |
| `default/border-radius` | `buttons-radius` | `0` | `12` |
| `loading/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `loading/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `loading/border-color` | `border/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `loading/border-width` | `interactive-width` | `1` | `1` |
| `loading/border-radius` | `buttons-radius` | `0` | `12` |
| `disabled/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `disabled/fg` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-second-text` | `foreground/on-dark/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-width` | `buttons-width` | `0` | `0` |
| `disabled/border-radius` | `buttons-radius` | `0` | `12` |

### Токены `button/accent-secondary/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/bg` | `brand/secondary/default` | `#23A3F7` | `#886FFB` |
| `hover/bg` | `brand/secondary/hover` | `#2098E9` | `#6C4DFF` |
| `pressed/bg` | `brand/secondary/pressed` | `#1C8CD6` | `#421AFF` |
| `focus/bg` | `brand/secondary/default` | `#23A3F7` | `#886FFB` |
| `focus/fg` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `focus/fg-second-text` | `foreground/on-light/secondary/default` | `#00000099` | `#080B1899` |
| `focus/border-color` | `border/primary` | `#FFFFFF` | `#F9F9FB` |
| `focus/border-width` | `focus-width` | `3` | `3` |
| `focus/border-radius` | `buttons-radius` | `0` | `12` |
| `pressed/fg` | `foreground/on-light/primary/pressed` | `#000000CC` | `#080B18CC` |
| `pressed/fg-second-text` | `foreground/on-light/secondary/pressed` | `#00000066` | `#080B1866` |
| `pressed/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `pressed/border-width` | `buttons-width` | `0` | `0` |
| `pressed/border-radius` | `buttons-radius` | `0` | `12` |
| `hover/fg` | `foreground/on-light/primary/hover` | `#000000E5` | `#080B18E5` |
| `hover/fg-second-text` | `foreground/on-light/secondary/hover` | `#00000080` | `#080B1880` |
| `hover/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/border-width` | `buttons-width` | `0` | `0` |
| `hover/border-radius` | `buttons-radius` | `0` | `12` |
| `default/fg` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `default/fg-second-text` | `foreground/on-light/secondary/default` | `#00000099` | `#080B1899` |
| `default/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/border-width` | `buttons-width` | `0` | `0` |
| `default/border-radius` | `buttons-radius` | `0` | `12` |
| `loading/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `loading/fg` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `loading/border-color` | `border/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `loading/border-width` | `interactive-width` | `1` | `1` |
| `loading/border-radius` | `buttons-radius` | `0` | `12` |
| `disabled/bg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `disabled/fg` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-second-text` | `foreground/on-dark/highlight` | `#FFFFFF1A` | `#F4F1E81A` |
| `disabled/border-color` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-width` | `buttons-width` | `0` | `0` |
| `disabled/border-radius` | `buttons-radius` | `0` | `12` |
