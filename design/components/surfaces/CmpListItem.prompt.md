# CmpListItem / CmpInfoRow
`CmpListItem` — строка списка 80 (настройки, устройства, периферия): иконка в круге 48, заголовок, подзаголовок, шеврон при onClick, `status` — цветная точка.
`CmpInfoRow` — пара «подпись / значение» 88 для карточек информации; `mono` для ID и серийников, `trailing` — кнопка копирования.
```jsx
<CmpListItem icon="bluetooth" title="Метка BT-760" subtitle="Подключена" status="ok" onClick={open} />
<CmpInfoRow label="Серийный номер" value="4G24 1003 5871" mono trailing={<CmpIconButton icon="content_copy" />} />
```
Заменяют ручные сборки из CmpSurface + CmpRow.
