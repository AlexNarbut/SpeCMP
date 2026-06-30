Modal dialog — confirm or choose. 28dp corners, scrim behind. Title + body + text-button actions; optional centered icon. Wrap the screen in `position:relative` so the scrim fills it.

```jsx
<CmpDialog open={ask} onDismissRequest={close}
  icon="Delete" title="Удалить объект?" text="Это действие необратимо."
  dismissButton={<CmpTextButton text="Отмена" onClick={close} />}
  confirmButton={<CmpTextButton text="Удалить" onClick={del} />} />
```
