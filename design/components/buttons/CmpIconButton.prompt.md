Icon button — a single catalog icon as an action. 48dp touch target (critical for mechanics' gloved/low-sensitivity fingers), 24dp icon. Content is always an icon — never arbitrary UI.

```jsx
<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={back} />
<CmpIconButton icon="Favorite" selected contentDescription="В избранном" />
```
