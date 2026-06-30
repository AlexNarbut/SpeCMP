Dropdown — pick one value from a list. Read-only field anchor, menu below. Placeholder is 12sp, items 14sp, min height 56dp.

```jsx
<CmpDropdown placeholder="Модель сигнализации" options={["DXL 5000","DX 91","VX 4G"]}
  selectedValue={model} onOptionSelected={(i, v) => setModel(v)} />
```
