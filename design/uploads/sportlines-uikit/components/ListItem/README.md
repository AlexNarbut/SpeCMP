# ListItem

Навигационная строка — основа экранов-списков. Мин. высота 80, паддинги: сверху/снизу 12, справа 20, слева 24 (16, если слева большая иконка или логотип). Нажатие — scale 0.96 + ripple по форме строки (если есть `onClick`).

**Слева** (что-то одно, по приоритету): логотип 56 (круглая подложка цвета `fg-icon`, картинка с отступом 3) → большая иконка 44 → иконка 24. Зазор до текста 16 (для иконки 24 — 12).

**Текст** сверху вниз: `topSubtitle` (`headLine3`, 1 строка) → `bigTitle` (`headLine1`, 2 строки) или `smallTitle` (`headLine2`, 2 строки) → `bottomSubtitle` (`additional0`, 2 строки). Всё с многоточием.

**Справа:** иконка 32 после зазора 16 (обычно `pyromid-right`). Ниже строки — опц. слот.

**Состояния:** Default, Highlighted (рамка и стрелка `brand-primary-default`), Warning (заголовок и подзаголовок `warning-default`, рамка `warning-highlight`), Disabled.

**StatusListItem** — та же геометрия, статус Success / Info / Warning: заголовок и иконка `{status}-default` (pressed `-pressed`), рамка `{status}-highlight`, подзаголовок нейтральный.

Рамка 1 `border-light`, радиус `r-list-item-radius`. Соседние строки сливают рамки в одну линию.

### Токены `navigational-list-item/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `default/fg-title` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `default/fg-subtitle` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `default/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `default/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `default/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `default/border-radius` | `list-item-radius` | `0` | `12` |
| `default/border-width` | `list-item-width` | `1` | `1` |
| `hover/fg-title` | `foreground/on-dark/primary/hover` | `#FFFFFFE5` | `#F4F1E8E5` |
| `hover/fg-subtitle` | `foreground/on-dark/secondary/hover` | `#FFFFFF80` | `#F4F1E880` |
| `hover/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `hover/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `hover/border-color` | `border/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `hover/border-radius` | `list-item-radius` | `0` | `12` |
| `hover/border-width` | `list-item-width` | `1` | `1` |
| `pressed/fg-title` | `foreground/on-dark/primary/pressed` | `#FFFFFFCC` | `#F4F1E8CC` |
| `pressed/fg-subtitle` | `foreground/on-dark/secondary/pressed` | `#FFFFFF66` | `#F4F1E866` |
| `pressed/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `pressed/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `pressed/border-color` | `border/primary` | `#FFFFFF` | `#F9F9FB` |
| `pressed/border-radius` | `list-item-radius` | `0` | `12` |
| `pressed/border-width` | `list-item-width` | `1` | `1` |
| `focus/fg-title` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `loading/fg` | `background/surface/ultra-highlight` | `#FFFFFF0D` | `#F4F1E80D` |
| `loading/border-color` | `border/tertiary` | `#FFFFFF4D` | `#F4F1E84D` |
| `loading/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `loading/border-radius` | `list-item-radius` | `0` | `12` |
| `loading/border-width` | `list-item-width` | `1` | `1` |
| `focus/fg-subtitle` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `focus/fg-icon` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `focus/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `focus/border-radius` | `list-item-radius` | `0` | `12` |
| `focus/border-width` | `focus-width` | `3` | `3` |
| `disabled/fg-title` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-subtitle` | `foreground/on-dark/secondary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/fg-icon` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `disabled/bg` | `background/surface/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `disabled/border-color` | `border/medium-highlight` | `#FFFFFF26` | `#F4F1E826` |
| `disabled/border-radius` | `list-item-radius` | `0` | `12` |
| `disabled/border-width` | `list-item-width` | `1` | `1` |
