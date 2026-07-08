Top app bar — screen header, 64dp tall. Build it only through this component (never a hand-rolled row). Back button goes in `navigationIcon`, page actions in `actions`.

```jsx
<CmpTopAppBar
  title="Калькулятор"
  navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={back} />}
  actions={<CmpIconButton icon="MoreVert" contentDescription="Ещё" />}
/>
```
