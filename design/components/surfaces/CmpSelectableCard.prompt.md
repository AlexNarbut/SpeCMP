Selectable card — a tappable choice with a clear selected state (primary border + primaryContainer fill). Use for picking a model, mode, or option. Large hit area suits mechanics.

```jsx
<CmpSelectableCard selected={sel === id} onClick={() => setSel(id)}
  icon={<CmpRadioButton selected={sel === id} />}>
  <CmpText variant="titleMedium">Pandora DXL 5000</CmpText>
</CmpSelectableCard>
```
