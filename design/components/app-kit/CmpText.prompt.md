Text primitive — renders text at a Material 3 type-scale role. Designer chooses a role (`titleLarge`, `bodyMedium`, …), not a raw px size.

```jsx
<CmpText variant="headlineSmall">Установка сигнализации</CmpText>
<CmpText variant="bodyMedium" color="var(--on-surface-variant)">Шаг 2 из 6</CmpText>
```

- Default variant `bodyLarge`; default color `currentColor`.
- `maxLines` truncates with ellipsis; `weight` overrides the role's default weight.
- `as` sets the element tag (e.g. `as="h1"`).
