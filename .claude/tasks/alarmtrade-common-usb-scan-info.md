# alarmtrade-common: прошивка и режим блока в результате USB-поиска

**Версия:** `ru.alarmtrade.common` 2.0.94 → **2.0.95**, `specialist` 1.2.0 (перепубликовать).

**Зачем:** экран поиска SpeCMP (`feature/device/selection-ui`) показывает для USB-блока версию прошивки и метку
«Загрузчик» ещё до подключения. Пользователь может отличить блок, который нужно прошить, и в списке видно, что он
подключает.

**Статус:** реализовано в `alarmtrade-common3` (2.0.95), публикация в mavenLocal; SpeCMP использует поля в поиске.

---

## Как сейчас

- При подключении кабеля SDK опрашивает блок по PC-base протоколу:
  `PcBaseInformationExtractor.getUsbInformation` → `PcBaseProtocol.readDeviceInformation()` →
  `PcBaseDeviceInformation(systemName, firmwareVersion, systemId, productId, operationMode, …)`.
- Наружу из ответа уходят только два поля: `UsbDeviceInformation.copy(id = systemId, productName = systemName)`.
  Версия прошивки и режим работы теряются.
- `scanUsbDevicesUseCase` отдаёт `AttachedUsbDeviceInformation(id, vendorId, productId, manufacturerName,
  productName)`.

## Изменения

1. **`usb-common` / `UsbDeviceInformation`**: добавить поля, значения по умолчанию — `null` (блок не опрошен):
   ```kotlin
   val firmwareVersion: String? = null,
   val isBootMode: Boolean? = null,
   ```
   Позиционные конструкторы в `UsbDeviceImpl` (Android) и `JvmUsbDevice` (desktop) не меняются.
2. **`usb-common` / `PcBaseInformationExtractor`**: заполнять новые поля из ответа блока:
   ```kotlin
   usbDevice.usbDeviceInformation.copy(
       id = pcBaseInformation.systemId.toString(),
       productName = pcBaseInformation.systemName,
       firmwareVersion = pcBaseInformation.firmwareVersion,
       isBootMode = when (pcBaseInformation.operationMode) {
           MAIN -> false
           BOOT_OLD, BOOT_MLD, BOOT_FLD -> true
           UNKNOWN -> null // fromByte отдаёт UNKNOWN для неизвестного байта — не помечаем «загрузчиком»
       },
   )
   ```
3. **`alarmtrade-usb` / `AttachedUsbDeviceInformation`**: те же поля, по умолчанию `null`.
   `UsbDeviceInformation.toAttachedUsbDeviceInformation()` их передаёт.
4. **`AppVersions.version` = 2.0.95.** Опубликовать в mavenLocal: `deployLibsLocal.sh`, затем `:nav-cpu`, затем
   `deploySpecLocal.sh`.

**Совместимость.** Новые поля добавлены в конец и имеют значения по умолчанию, так что исходники клиентов
собираются без изменений. Меняются сигнатуры `copy` / `componentN` у data-классов, поэтому нужна новая версия, а не
перезапись 2.0.94. `equals` в `UsbDeviceImpl` и `JvmUsbDevice` сравнивает по `id`, новые поля на него не влияют.

## Чего не делаем

- **BLE.** Версии прошивки и `systemId` в рекламном пакете нет: manufacturer data содержит только тип устройства и
  флаги BOOT / PAIRING / context. Уровень сигнала, тип и режим уже есть в `BleDeviceScanResult`, правка SDK для них
  не нужна.
- Остальные поля `PcBaseDeviceInformation` (`productId` блока, CAN, `processorPlatformId`, MLD) — по запросу.

## Проверка

- Сборка `:usb-common`, `:alarmtrade-usb` (android, jvm, ios), публикация.
- SpeCMP: на Pixel 8 и desktop (x86_64-JDK) у DX4G по USB в поиске версия прошивки, у блока в загрузчике — метка
  «Загрузчик».
