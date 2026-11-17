# Оформление (appearance)

`Appearance.dc.html` — экран настройки темы. Переключатель окна (Телефон 412 / Планшет 800 / Desktop 1280) и темы самого экрана — над фреймом.

## Модель темы
```json
{ "format": "pandora-spec-theme", "version": 1,
  "light": { "c": { "primary", "onPrimary", "bg", "bg2", "card", "ink", "ink2", "outline",
                    "success", "warning", "error", "info" },
             "bgType": "solid|gradient", "bgDir": "down|diag|radial",
             "text": 100|115|130|150, "fill": "filled|tonal|outlined",
             "shape": "square|rounded|pill", "radius": 0–28,
             "r": { "buttons", "inputs", "cards", "chips", "sheets" },
             "btnPad": "s|m|l", "chipPad": "s|m|l" },
  "dark": { … } }
```
Маппинг на токены: primary → `--brand-primary-default`/`--primary`, bg → `--background-page-primary`, card → `--background-surface-card`, ink → `--foreground-on-dark-primary-default` (альфа-ступени ink/border выводятся из него), ink2 → `--on-surface-variant`, outline → `--outline`, статусы → `--*-default`, `r.*` → `--r-*-radius`, text → множитель для `--t-*-size/line`. `chips = 28` → pill.

Отступы: кнопки s/m/l = высота 48/56/64, по бокам 16/24/32; чипы s/m/l = 48/48/56, по бокам 12/18/24.

## Ограничения (не отключаются)
- Градиент фона: текст и акцент проверяются на обоих концах (bg и bg2).
- Текст/фон и текст/карточка, вторичный текст, надпись на кнопке — ≥ 4.5:1.
- Акцент, обводки, статусы на карточке — ≥ 3:1; при контурных кнопках акцент ≥ 4.5:1.
- Текст ≥ 100%, высота кнопок/полей/чипов ≥ 48 dp.
- Подбор «ближайшего» цвета — смешивание с чёрным/белым шагом 2% до прохождения всех связанных проверок.

## Раскладка
Compact — одна колонка, предпросмотр сверху. Medium/Expanded — настройки слева, липкий предпросмотр справа; max-width 1040. Редактор цвета: шторка снизу на Compact, диалог на широких окнах.
