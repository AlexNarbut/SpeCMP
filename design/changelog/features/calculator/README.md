# Калькулятор — фича

Экран **«Калькулятор»** Pandora Spec — инженерный калькулятор монтажника с таймером установки.
Порт `feature/calculator/.../CalculatorScreen.kt` из кодовой базы SpeCMP (единственный feature-экран).

## Файлы
- `Calculator.jsx` — компонент `CalculatorScreen` (экспортируется в `window.CalculatorScreen`):
  верхняя панель, таймер (Старт/Стоп/Сброс), дисплей справа, клавиатура 4×N. Реальная логика
  калькулятора и таймера; деление на ноль поднимает снэкбар «Деление на ноль невозможно».
- `Calculator.html` — mobile-демо: экран во фрейме Android (`components/app-kit/PhoneFrame.jsx`)
  с переключателем светлой/тёмной темы.
- `CalculatorDesktop.html` — desktop-демо: тот же экран в опорном окне **800 × 600** (титул-бар + окно).

## Как собрано
Только из компонентов дизайн-системы через `window.PandoraSpecDesignSystem_b8d6e1`:
`CmpTopAppBar`, `CmpIconButton`, `CmpSurface` (кликабельные клавиши), `CmpText`, `CmpSnackbar`.
Цвета — через роли, поэтому экран корректен в обеих темах. Спека: `design-system/handoff/calculator.md`.

## Источник
`feature/calculator` + `desktopApp` — AlexNarbut/SpeCMP @ develop.
