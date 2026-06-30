Text input — outlined by default, filled optional. 56dp tall with floating label. Border goes primary on focus, error red on `isError`.

```jsx
<CmpTextField label="VIN автомобиля" value={vin} onChange={setVin} singleLine />
<CmpTextField label="Ток" value={a} onChange={setA} isError supportingText="Введите число" trailingIcon={<CmpIcon icon="AlertCircle" />} />
```
