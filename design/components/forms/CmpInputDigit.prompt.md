# CmpInputDigit
Ввод PIN / одноразового кода по ячейкам 56×72, цифра `title1`. Одно скрытое поле — работает вставка и автозаполнение кода.
```jsx
<CmpInputDigit value={pin} onChange={setPin} length={4} secure error={wrong} autoFocus />
```
Ошибку поясняйте строкой под полем (`headline3`, `error-default`). Заменяет CmpCodeField и ручные ячейки.
