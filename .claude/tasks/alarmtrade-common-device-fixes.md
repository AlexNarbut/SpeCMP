# alarmtrade-common: доработки подключения к устройству (BLE / USB)

**Версия:** `ru.alarmtrade.common` 2.0.93 (`bluetooth-common`, `alarmtrade-bluetooth`, `usb-common`,
`alarmtrade-devices`), `specialist` 1.2.0.

**Источник:** прогон поиска и подключения SpeCMP (`feature/device/*`) на Pixel 8 (Android 16) с блоками
DX4G (BLE и USB-OTG), X-1911BT, DX95FDS_AR, 2026-10-12. Разбор по исходникам `alarmtrade-common3`
(`095935726`): совпадают с опубликованными sources-jar 2.0.93.

**Для кого:** владельцы SDK. У каждого пункта: проблема → доказательство → предложение → чем обходим сейчас.

**Статус:**
- P0-1 и P0-2 реализованы в `alarmtrade-common3` (не закоммичено);
- `bluetooth-common` и `usb-common` 2.0.93 опубликованы в mavenLocal;
- проверено на Pixel 8 2026-10-12 (см. «Результат P0»);
- P1 и P2 — предложение.

Приоритет:
- **P0** — ломает сценарий у пользователя;
- **P1** — вынуждает клиента писать обходы;
- **P2** — качество / удобство API.

---

## P0-1. USB (Android): закрытие сессии убирает устройство из списка подключённых

**Где:** `usb-common/src/androidMain/.../usb/core/UsbManagerImpl.kt`, `disconnect()` (стр. 106–119).

**Проблема.** `disconnect(device)` закрывает `UsbDeviceConnection` и в `finally` **всегда** вызывает
`delegate.onDeviceDetached(device)`. `MainUsbDelegate.onDeviceDetached` удаляет устройство из
`attachedDevices`, а `scanUsbDevicesUseCase` отдаёт именно этот список. То есть любое закрытие сессии
(`closeUsbDeviceSessionUseCase`, `closeBluetoothDeviceSessionUseCase` (P2-3), `PandoraUsbDevice.disconnect()`)
выглядит для поиска как физическое отключение кабеля.

Вернуть устройство в `attachedDevices` может только одно из трёх:
- системный `ACTION_USB_DEVICE_ATTACHED` (переподключить кабель);
- `ACTION_USB_PERMISSION`;
- `usbEvents.register()` при инициализации, то есть перезапуск приложения.

Публичного способа пересканировать нет.

**Доказательство (Pixel 8, DX4G по USB):**
1. Поиск → DX4G (USB) в списке → подключение → `Session created`.
2. Закрытие сессии: в логе `UsbManagerImpl disconnect device …`, `Usb disconnect`, `connected devices keys }`.
3. Поиск: USB-устройства нет больше 20 с, кабель при этом подключён. BLE-устройства находятся.

На десктопе ошибки нет: `JvmUsbManager.disconnect()` только освобождает устройство, а
`onDeviceDetached` вызывается из `detachDevice()` по событию отключения.

**Предложение.** Разделить закрытие соединения и физическое отключение, как в `JvmUsbManager`:

```kotlin
override fun disconnect(device: UsbDevice) {
    while (!connectionMutex.tryLock()) { }
    try {
        connections.remove(device.usbDeviceInformation.id)?.close()
    } finally {
        connectionMutex.unlock()
    }
}

// init(): событие отключения кабеля — закрыть соединение и убрать из attached
usbEvents.detached
    .onEach {
        val information = deviceIdMapping.remove(it.stringDeviceId)
        val device = UsbDeviceImpl(it, information)
        disconnect(device)
        delegate.onDeviceDetached(device)
    }
```

Попутно: `while (!connectionMutex.tryLock()) { }` — активное ожидание. `connect()` держит mutex на всё
время сбора потока, так что `disconnect` из другого потока крутит CPU. Стоит заменить на `runBlocking { lock() }`
либо на отдельный lock только для `connections`.

**Чем обходим сейчас:** никак. После выхода из flow устройства USB-блок пропадает из поиска до
переподключения кабеля.

---

## P0-2. BLE: отмена `connect` после установки GATT не закрывает соединение

**Где:** `bluetooth-common/src/commonMain/.../bluetooth/repository/impl/BluetoothDeviceRepositoryImpl.kt`,
`connect(peripheral, parameters)` (стр. 52–145).

**Проблема.** Тело flow обёрнуто в `try { … } catch (e: Exception)`, и `CancellationException` там
**проглатывается**:

```kotlin
} catch (e: Exception) {
    Log.d { "Main connect exeption ${e.stackTraceToString()}" }
    if (e !is CancellationException) {
        emit(Pair(bleDevice, BluetoothConnectingStatus.FAIL))
        throw CancellationException(e.message, e)
    }
}
}.onCompletion { e ->
    if (e != null) {
        bleDevice?.dispose()
        bleDevice?.disconnect(true)
    }
}
```

При отмене сбора (пользователь ушёл с экрана, отмена, таймаут клиента) исключение ловится, тело
завершается «нормально», `onCompletion` получает `e == null`, и GATT не закрывается. Колбэки
`BluetoothManager` продолжают работу: MTU → `discoverServices` → `setCharacteristicNotification`.
Блок остаётся подключённым к телефону и не рекламируется. Сессия при этом не создана, поэтому
`closeBluetoothDeviceSessionUseCase` ничего не делает. Отпускает только завершение процесса.

**Доказательство (Pixel 8, DX4G):**
- 14:01:43.337 — `onClientConnectionState() connected=true`;
- 14:01:43.706 — отмена: `Main connect exeption JobCancellationException`;
- далее нет `BluetoothGatt: close()`;
- 14:01:44.849 — `discoverServices()`, затем `setCharacteristicNotification`;
- `dumpsys bluetooth_manager`: `ACL LE:Y` больше 80 с, поиск DX4G не находит;
- после `am force-stop` блок снова находится.

**Важно для фикса.** На этом же «проглатывании» держится успешный путь. Клиент обычно берёт первый
финальный статус (`first { … }`), и `AbortFlowException` от `emit(CONNECTED)` ловится тем же `catch`.
Если просто пробросить отмену, текущий `onCompletion` получит `e != null` и сделает `dispose()` +
`disconnect(true)` устройству, на котором уже создана сессия. Очистка нужна **только если соединение
не состоялось**:

```kotlin
var connected = false
return flow {
    …
    connected = true
    emit(Pair(bleDevice, BluetoothConnectingStatus.CONNECTED))
} … catch (e: CancellationException) {
    throw e // не глотать
} catch (e: Exception) {
    emit(Pair(bleDevice, BluetoothConnectingStatus.FAIL))
    throw CancellationException(e.message, e)
}
}.onCompletion { e ->
    if (e != null && !connected) withContext(NonCancellable) {
        bleDevice?.dispose()
        bleDevice?.disconnect(true)
    }
}
```

Так же стоит проверить отмену во время `createBond()`: сейчас ожидание bond до 60 с не прерывается
отменой сопряжения на стороне системы.

**Чем обходим сейчас:** в SpeCMP убрали кнопку «Отмена» и запретили «Назад» во время подключения.
Попытка всегда идёт до финального статуса. Остаётся страховочный таймаут клиента 60 с, на котором
ошибка воспроизведётся.

---

## P1-1. BLE: `connect` запускает свой поиск и не останавливает его

**Где:** `BluetoothDeviceRepositoryImpl.connect` (стр. 65–69).

**Проблема.** Перед подключением SDK ищет устройство: `scanDevices(true).first { … }` с таймаутом
8 с, чтобы получить manufacturer data. В отличие от `BluetoothConnectionRepositoryImpl.scanDevices`, в
`BluetoothDeviceRepositoryImpl.scanDevices` нет `onCompletion { stopScan() }`. Поиск остаётся
включённым на всё время сессии: батарея, лишняя нагрузка на радио во время обмена с блоком. Кроме того,
он занимает общий `ScanCallback` (P1-2).

**Предложение.**

```kotlin
val scanInfo = withTimeoutOrNull(WAITING_CONNECT_SCAN) {
    try {
        scanDevices(true).first { … }.firstOrNull { … }
    } finally {
        withContext(NonCancellable) { scanner.stopScan() }
    }
}
```

**Чем обходим сейчас:** `SpecialistDeviceConnectionRepository.connectBle()` в `onCompletion` вызывает
`stopBluetoothScanUseCase`.

---

## P1-2. BLE (Android): повторный `startScan` молча игнорируется

**Где:**
- `bluetooth-common/.../bluetooth/scan/BluetoothPeripheralScanner.kt`, `startScan()` (стр. 16);
- `bluetooth-common/src/androidMain/.../core/BluetoothManager.kt`, `startScan()`.

**Проблема.**
- У сканера один `ScanCallback` на приложение (`_bluetoothScanCallBack`).
- Если поиск уже идёт (например, внутренний из P1-1 или незавершённый прошлый), то
  `bluetoothLeScanner.startScan(filters, settings, callback)` с тем же callback Android не выполняет: он
  отдаёт `SCAN_FAILED_ALREADY_STARTED` в `onScanFailed`, а он пустой (`BluetoothManager.kt:344`).
- При этом `startScan` перед вызовом делает `scanDelegate.clearDevices()`, и клиент получает пустой
  список, который больше не пополняется (или пополняется по чужим фильтрам).

**Доказательство:**
- после отменённого подключения подписка на `scanBluetoothDevicesUseCase` не порождала новый
  `RegisterScanner` в логе, и поиск ничего не находил;
- с `stopBluetoothScanUseCase` перед подпиской `RegisterScanner` появляется сразу.

**Предложение.**
- В `BluetoothPeripheralScanner.startScan` сначала останавливать текущий поиск:
  `bluetoothManager.value.stopScan()`, затем `startScan` с новыми фильтрами.
- В `onScanFailed` логировать код ошибки (и пробрасывать в поток как ошибку поиска).
- Если разные потребители поиска должны жить одновременно (UI и `connect`), нужен счётчик подписчиков
  или общий поток поверх одного поиска, а не прямые `start` / `stop` от каждого.

**Чем обходим сейчас:** `SpecialistDeviceScanRepository.scanBluetooth()` вызывает
`stopBluetoothScanUseCase` перед `scanBluetoothDevicesUseCase`.

---

## P2-1. BLE: бесконечный цикл переподключения в `connect`

**Где:** `BluetoothDeviceRepositoryImpl.connect`, `while (!isConnected.value)` (стр. 116–129).

**Проблема.** По таймауту `parameters.timeout` SDK эмитит `CONNECTING_FAIL`, делает `disconnect(false)` и
пробует снова, без ограничения. Поток не завершается сам. Клиент вынужден сам решать, считать ли
`CONNECTING_FAIL` финалом, и держать внешний таймаут.

**Предложение.** `ConnectionParameters.maxAttempts` (по умолчанию 1–3), после исчерпания — `FAIL` и
завершение потока. Либо явно описать в KDoc, что поток бесконечен, а `CONNECTING_FAIL` — промежуточный
статус.

**Чем обходим сейчас:** первый `CONNECTING_FAIL` считаем финалом, плюс общий таймаут 60 с.

---

## P2-2. iOS: флаг сопряжения с телефоном всегда `false`

**Где:**
- `bluetooth-common/src/iosMain/.../core/BluetoothPeripheralScanInformation.kt` (`isBonded = false`);
- `iosMain/.../core/BluetoothManager.kt`, `isDeviceBonded()` → `false`.

**Проблема.**
- `BleDeviceScanResult.hasPhoneBond` на iOS всегда `false`, а `hasDeviceBond` (`hasContext`) — реальный.
- Клиент, который проверяет рассинхрон сопряжения (как в старом приложении), на iOS ложно блокирует
  каждый ранее сопряжённый блок подсказкой «снимите сопряжение на устройстве».

**Предложение.** `hasPhoneBond: Boolean?`, где `null` означает, что платформа не сообщает. Либо явно
задокументировать, что поле значимо только на Android.

**Чем обходим сейчас:** `ScannedDevice.Ble.hasPhoneBond = null` при `platformType() != ANDROID`, проверка
сопряжения пропускается.

---

## P2-3. Одна сессия на BLE и USB, а закрытие названо по транспорту

**Где:**
- `alarmtrade-bluetooth/.../usecases/CloseBluetoothDeviceSessionUseCase.kt` →
  `PandoraDeviceSessionStorage.clearSession()`;
- `alarmtrade-devices/.../PandoraDeviceSessionStorageImpl.kt`.

**Проблема.** Хранилище сессии одно на все транспорты. `closeBluetoothDeviceSessionUseCase` закрывает и
USB-сессию: в логе три `UsbManagerImpl disconnect` подряд от вызовов `closeBluetooth…` + `closeUsb…`. С
P0-1 это превращается в «закрыли BLE — пропал USB-блок». Клиент из названий этого не видит.

**Предложение.**
- Единый `closeDeviceSessionUseCase` (без транспорта в имени), а старые объявить устаревшими.
- Плюс поток состояния текущей сессии (`currentSession: StateFlow<SessionInfo?>`: транспорт, id), чтобы
  клиенту не нужно было закрывать вслепую.

---

## Что убрать у нас после фиксов SDK

| Фикс SDK | Что убрать / вернуть в SpeCMP |
|---|---|
| P0-1 | Ничего не убирать. Перепроверить: выход из flow → повторный поиск находит USB-блок |
| P0-2 | Можно вернуть кнопку «Отмена» и «Назад» во время подключения (`ScanDeviceViewModel.onBack`) |
| P1-1 | `onCompletion { stopBluetoothScanUseCase }` в `SpecialistDeviceConnectionRepository.connectBle` |
| P1-2 | `stopBluetoothScanUseCase` перед поиском в `SpecialistDeviceScanRepository.scanBluetooth` |
| P2-1 | Отношение к `CONNECTING_FAIL` и, возможно, общий таймаут подключения |
| P2-2 | `platformType()` в `SpecialistDeviceScanRepository` (если SDK отдаст `null` сам) |

## Проверка после фиксов (Pixel 8)

1. **USB.** Подключить → выйти → войти: USB-блок в списке без переподключения кабеля. Выдернуть кабель → пропал; вставить → появился.
2. **BLE.** Тап → отмена после `onClientConnectionState connected=true` → в логе `BluetoothGatt: close()`, `ACL LE:N`, блок снова в поиске.
3. **BLE, успешный путь.** Тап → `Session created` → данные блока читаются (отмена не рвёт успешную сессию).
4. **BLE, внутренний поиск.** После `Session created` в логе нет активного поиска (нет новых результатов скана).
5. **Поиск после неудачного подключения** без `stopBluetoothScanUseCase` у клиента: сразу новый `RegisterScanner`, блоки находятся.

## Результат P0 (2026-10-12, Pixel 8)

- **P0-1.** `UsbManagerDelegate.onDeviceConnectionClosed`. По умолчанию это `onDeviceDetached`, так что `serialport` ведёт себя как прежде. `MainUsbDelegate` переводит состояние в `DETACHED`, но не убирает устройство из `attachedDevices`. `UsbManagerImpl`:
  - `disconnect()` вызывает `onDeviceConnectionClosed`;
  - `onDeviceDetached` вызывается только по `usbEvents.detached`.

  Вариант с `onDeviceAttached` отвергнут: `PandoraUsbDeviceImpl` на `ATTACHED` сам вызывает `connect()`.

  Проверка: USB-подключение → выход → повторный вход: DX4G (USB) в списке; повторное подключение по USB — `Session created`.
- **P0-2.** Отмена пробрасывается (`catch (e: CancellationException) { throw e }`). `onCompletion` чистит соединение (`dispose` + `disconnect(true)` под `NonCancellable`) только если `CONNECTED` ещё не было.
  - Путь очистки: GATT 133 → таймаут 20 с → `CONNECTING_FAIL` → клиент прервал подписку → `Main connect cancelled`, `BluetoothGatt: close()`. До фикса `close()` не было.
  - Успешный путь: `Session created`, после `CONNECTED` очистки нет, соединение живо.
  - Повторный вход: оба блока (BLE и USB) в списке.
