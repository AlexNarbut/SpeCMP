Bottom sheet — panel sliding up from the bottom for secondary content/actions. 16dp top corners, drag handle, scrim. Wrap the screen in `position:relative`.

```jsx
<CmpBottomSheet open={sheet} onDismissRequest={close}>
  <CmpText variant="titleLarge">Выберите точку</CmpText>
  {/* list … */}
</CmpBottomSheet>
```
