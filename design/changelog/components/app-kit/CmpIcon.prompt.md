Catalog icon — single Material Icons (Filled) glyph; default 24dp, inherits content color. Use for any icon; never hand-roll SVG icons outside the catalog.

```jsx
<CmpIcon icon="ArrowBack" title="Назад" />
<CmpIcon icon="Build" size={32} color="var(--primary)" />
```

- `icon` accepts a CmpIcons catalog name (PascalCase, e.g. `Settings`, `Delete`, `Warning`) or any raw Material Icons ligature.
- Catalog names mirror `CmpIcons` in the codebase (see `CMP_ICONS` export for the full map).
- Decorative icons omit `title` and render `aria-hidden`.
