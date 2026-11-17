# TopToast

Тост, выезжающий сверху (вместо снекбара снизу). Позиция: под статус-баром + 64 (топбар) + 16, поля по 48, по центру, ширина по контенту.

| Параметр | Значение |
|---|---|
| Фон / радиус / рамка | `snackbar/bg` (белый), `r-snackbar-radius`, `w-snackbar-width` |
| Паддинг | 12 по бокам, 8 сверху/снизу; зазор 12 |
| Иконка | 24, цвет `snackbar/fg`: Error и Info — `alert-triangle`, Success — `check`, Neutral — без иконки |
| Текст | `headLine3`, `snackbar/fg`, до 3 строк |
| Анимация | въезд/выезд 300ms, висит 3000ms |

### Токены `snackbar/*`

| Переменная | Семантика | Sportlines Dark | 1x2 |
|---|---|---|---|
| `bg` | `background/surface/primary` | `#FFFFFF` | `#F4F1E8` |
| `fg` | `foreground/on-light/primary/default` | `#000000` | `#080B18` |
| `border-color` | `border/transparent` | `#FFFFFF00` | `#F4F1E800` |
| `border-radius` | `snackbar-radius` | `0` | `8` |
| `border-width` | `snackbar-width` | `0` | `0` |
