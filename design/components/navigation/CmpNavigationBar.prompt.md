Bottom navigation — switch between top-level sections (Compact width class). 80dp tall, selected item gets a primary pill behind its icon. Labels always shown for clarity.

```jsx
<CmpNavigationBar items={[
  { icon: "Home", label: "Главная", selected: tab===0, onClick: () => setTab(0) },
  { icon: "List", label: "Объекты", selected: tab===1, onClick: () => setTab(1), badge: 3 },
  { icon: "Settings", label: "Настройки", selected: tab===2, onClick: () => setTab(2) },
]} />
```
