# CmpContentError
Центрированное состояние экрана: ошибка, пусто, нет сети. Заголовок — одна строка, пояснение — одна-две.
```jsx
<CmpContentError tone="offline" title="Нет связи с блоком" text="Проверьте Bluetooth и подойдите ближе." action={<CmpButton text="Повторить" fullWidth />} />
```
Заменяет CmpStateBox.
