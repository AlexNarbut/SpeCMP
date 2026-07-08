Base surface — the lowest-level container. Square and flat by default; configure fill, content color, radius, shadow, border. Used for custom tiles like calculator keys.

```jsx
<CmpSurface onClick={onKey} color="var(--secondary-container)" contentColor="var(--on-secondary-container)" radius={16} style={{ height: 72, display: "grid", placeItems: "center" }}>
  <CmpText variant="headlineSmall">÷</CmpText>
</CmpSurface>
```
