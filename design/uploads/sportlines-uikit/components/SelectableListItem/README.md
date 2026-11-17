# SelectableListItem

Строка выбора (язык, страна, модель). Мин. высота 72, паддинг 4 по бокам и 16 сверху/снизу. Опц. картинка слева 32, зазор 16. Заголовок `headLine1` без подзаголовка или `headLine2` с ним, 2 строки; подзаголовок `additional0`, зазор 4. Выбранная строка: заголовок `brand-primary-default` и галочка `check` 32 справа (зазор 16), высота не меняется. Снизу разделитель во всю ширину (`border-light`, 1). Нажатие — scale 0.96 + ripple.

### Токены `selectable-list-item/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `unselected/default/fg-title` | `foreground/on-dark/primary/default` | `#FFFFFF` | `#F4F1E8` |
| `unselected/default/fg-subtitle` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `unselected/default/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `unselected/default/border-width` | `list-item-width` | `1` | `1` |
| `unselected/hover/fg-title` | `foreground/on-dark/primary/hover` | `#FFFFFFE5` | `#F4F1E8E5` |
| `unselected/hover/fg-subtitle` | `foreground/on-dark/secondary/hover` | `#FFFFFF80` | `#F4F1E880` |
| `unselected/hover/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `unselected/hover/border-width` | `list-item-width` | `1` | `1` |
| `unselected/pressed/fg-title` | `foreground/on-dark/primary/pressed` | `#FFFFFFCC` | `#F4F1E8CC` |
| `unselected/pressed/fg-subtitle` | `foreground/on-dark/secondary/pressed` | `#FFFFFF66` | `#F4F1E866` |
| `unselected/pressed/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `unselected/pressed/border-width` | `list-item-width` | `1` | `1` |
| `unselected/focus/fg-title` | `foreground/on-dark/primary/pressed` | `#FFFFFFCC` | `#F4F1E8CC` |
| `unselected/focus/fg-subtitle` | `foreground/on-dark/secondary/pressed` | `#FFFFFF66` | `#F4F1E866` |
| `unselected/focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `unselected/focus/border-width` | `focus-width` | `3` | `3` |
| `unselected/disabled/fg-title` | `foreground/on-dark/primary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `unselected/disabled/fg-subtitle` | `foreground/on-dark/secondary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `unselected/disabled/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `unselected/disabled/border-width` | `list-item-width` | `1` | `1` |
| `selected/default/fg-title` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `selected/default/fg-icon` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `selected/default/fg-subtitle` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `selected/default/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `selected/default/border-width` | `list-item-width` | `1` | `1` |
| `selected/hover/fg-title` | `brand/primary/hover` | `#3DE93D` | `#B7FF17` |
| `selected/hover/fg-icon` | `brand/primary/hover` | `#3DE93D` | `#B7FF17` |
| `selected/hover/fg-subtitle` | `foreground/on-dark/secondary/hover` | `#FFFFFF80` | `#F4F1E880` |
| `selected/hover/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `selected/hover/border-width` | `list-item-width` | `1` | `1` |
| `selected/pressed/fg-title` | `brand/primary/pressed` | `#2ACD2A` | `#93D305` |
| `selected/pressed/fg-icon` | `brand/primary/pressed` | `#2ACD2A` | `#93D305` |
| `selected/pressed/fg-subtitle` | `foreground/on-dark/secondary/pressed` | `#FFFFFF66` | `#F4F1E866` |
| `selected/pressed/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `selected/pressed/border-width` | `list-item-width` | `1` | `1` |
| `selected/focus/fg-title` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `selected/focus/fg-icon` | `brand/primary/default` | `#53F153` | `#C7FF4A` |
| `selected/focus/fg-subtitle` | `foreground/on-dark/secondary/default` | `#FFFFFF99` | `#F4F1E899` |
| `selected/focus/border-color` | `border/focus` | `#4FB1F5` | `#55D9FB` |
| `selected/focus/border-width` | `focus-width` | `3` | `3` |
| `selected/disabled/fg-title` | `brand/primary/disabled` | `#1CBA1C` | `#304109` |
| `selected/disabled/fg-icon` | `brand/primary/disabled` | `#1CBA1C` | `#304109` |
| `selected/disabled/fg-subtitle` | `foreground/on-dark/secondary/disabled` | `#FFFFFF33` | `#F4F1E833` |
| `selected/disabled/border-color` | `border/light` | `#FFFFFF33` | `#F4F1E833` |
| `selected/disabled/border-width` | `list-item-width` | `1` | `1` |
