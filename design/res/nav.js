// Pandora Spec — cross-mockup navigation.
// Lets one standalone layout link to another regardless of folder depth.
// Usage: window.psGo('calculator')  → opens that screen in the same tab.
(function () {
  // Find this script reliably (document.currentScript is unreliable in the
  // preview environment — it can point at a later-evaluated script).
  var me = document.currentScript;
  if (!me || !/res\/nav\.js/.test(me.src || '')) {
    me = [].slice.call(document.scripts).filter(function (s) {
      return /res\/nav\.js(?:\?|$)/.test(s.src || '');
    }).pop() || me;
  }
  // Project root = location of this file minus "res/nav.js"
  var ROOT = me && me.src
    ? me.src.replace(/res\/nav\.js(?:\?.*)?$/, '')
    : new URL('.', location.href).href;

  // Section key → file, relative to project root.
  var LINKS = {
    home:                'features/home/Home.html',
    'home-tablet':        'features/home/HomeTablet.html',
    'home-desktop':       'features/home/HomeDesktop.html',
    'home-parts':         'features/home/HomeParts.html',
    'app-update':         'features/app-update/AppUpdate.html',
    index:               'index.html',
    'index-tablet':       'index-tablet.html',
    'index-desktop':      'index-desktop.html',
    'device-select':          'features/device-select/DeviceSelect.html',
    'device-select-tablet':   'features/device-select/DeviceSelectTablet.html',
    'device-select-desktop':  'features/device-select/DeviceSelectDesktop.html',
    'device-select-states':   'features/device-select/DeviceSelectStates.html',
    'advanced-mount':          'features/advanced-mount/AdvancedMount.html',
    'advanced-mount-tablet':   'features/advanced-mount/AdvancedMountTablet.html',
    'advanced-mount-desktop':  'features/advanced-mount/AdvancedMountDesktop.html',
    'device-settings':          'features/device-settings/DeviceSettings.html',
    'device-settings-tablet':   'features/device-settings/DeviceSettingsTablet.html',
    'device-settings-desktop':  'features/device-settings/DeviceSettingsDesktop.html',
    'device-settings-parts':    'features/device-settings/DeviceSettingsParts.html',
    firmware:            'features/firmware/Firmware.html',
    'firmware-tablet':    'features/firmware/FirmwareTablet.html',
    'firmware-desktop':   'features/firmware/FirmwareDesktop.html',
    'firmware-foldable':  'features/firmware/FirmwareFoldable.html',
    'vehicle-select':          'features/vehicle-select/VehicleSelect.html',
    'vehicle-select-tablet':   'features/vehicle-select/VehicleSelectTablet.html',
    'vehicle-select-desktop':  'features/vehicle-select/VehicleSelectDesktop.html',
    'vehicle-select-states':   'features/vehicle-select/VehicleSelectStates.html',
    'vehicle-select-parts':    'features/vehicle-select/VehicleSelectParts.html',
    configurator:          'features/configurator/Configurator.html',
    'configurator-tablet':   'features/configurator/ConfiguratorTablet.html',
    'configurator-desktop':  'features/configurator/ConfiguratorDesktop.html',
    'configurator-states':   'features/configurator/ConfiguratorStates.html',
    'configurator-parts':    'features/configurator/ConfiguratorParts.html',
    'bypass-clone':          'features/bypass-clone/BypassClone.html',
    'bypass-clone-tablet':   'features/bypass-clone/BypassCloneTablet.html',
    'bypass-clone-desktop':  'features/bypass-clone/BypassCloneDesktop.html',
    'check-state':          'features/check-state/CheckState.html',
    'check-state-tablet':   'features/check-state/CheckStateTablet.html',
    'check-state-parts':    'features/check-state/CheckStateParts.html',
    'check-state-layouts':  'features/check-state/CheckStateLayouts.html',
    'check-state-foldable': 'features/check-state/CheckStateFoldable.html',
    'peripheral-devices':         'features/peripheral-devices/PeripheralDevices.html',
    'peripheral-devices-tablet':  'features/peripheral-devices/PeripheralDevicesTablet.html',
    'peripheral-devices-foldable': 'features/peripheral-devices/PeripheralDevicesFoldable.html',
    'peripheral-devices-states':  'features/peripheral-devices/PeripheralDevicesStates.html',
    'peripheral-devices-parts':   'features/peripheral-devices/PeripheralDevicesParts.html',
    'fast-mount':          'features/fast-mount/FastMount.html',
    'fast-mount-tablet':   'features/fast-mount/FastMountTablet.html',
    'fast-mount-desktop':  'features/fast-mount/FastMountDesktop.html',
    'fast-mount-states':   'features/fast-mount/FastMountStates.html',
    calculator:          'features/calculator/Calculator.html',
    'calculator-tablet':  'features/calculator/CalculatorTablet.html',
    'calculator-desktop': 'features/calculator/CalculatorDesktop.html',
    auth:                'features/auth/Auth.html',
    'auth-tablet':        'features/auth/AuthTablet.html',
    'auth-desktop':       'features/auth/AuthDesktop.html',
    'auth-states':        'features/auth/AuthStates.html',
    'auth-flow':          'features/auth/AuthFlow.html',
    assistant:             'features/assistant/Assistant.dc.html',
    'assistant-wide':      'features/assistant/AssistantWide.dc.html',
    'support-chat':         'features/support-chat/SupportChat.html',
    'support-chat-tablet':  'features/support-chat/SupportChatTablet.html',
    'support-chat-desktop': 'features/support-chat/SupportChatDesktop.html',
    'tech-requests':         'features/tech-requests/TechRequests.html',
    'tech-requests-tablet':  'features/tech-requests/TechRequestsTablet.html',
    'tech-requests-desktop': 'features/tech-requests/TechRequestsDesktop.html',
    'tech-requests-states':  'features/tech-requests/TechRequestsStates.html',
    'product-info':          'features/product-info/ProductInfo.dc.html',
    'product-info-wide':     'features/product-info/ProductInfoWide.dc.html',
    'product-info-states':   'features/product-info/ProductInfoStates.dc.html',
  };

  // «Информация о товаре»: from = home | mount | assistant; n — номер (из монтажа/ассистента — ID блока).
  // Вариант экрана — по текущему макету: Desktop → окно 1280×800, Tablet/Wide → планшет, иначе телефон.
  window.psProduct = function (from, n) {
    var f = location.pathname;
    var wide = /(Desktop|Tablet|Wide|Foldable)/i.test(f);
    var q = '?from=' + encodeURIComponent(from || 'home') + (n ? '&n=' + encodeURIComponent(n) : '') + (/Desktop/i.test(f) ? '&win=dk' : '');
    location.href = ROOT + LINKS[wide ? 'product-info-wide' : 'product-info'] + q;
  };

  // Заявка на техпомощь из сценария (обход, Clone, ассистент): открывает форму «Новая заявка»
  // с описанием проблемы. Вариант экрана — по текущему макету (Desktop / Tablet / mobile).
  window.psTicket = function (from, text) {
    var f = location.pathname;
    var key = /Desktop/i.test(f) ? 'tech-requests-desktop' : /(Tablet|Wide|Foldable)/i.test(f) ? 'tech-requests-tablet' : 'tech-requests';
    location.href = ROOT + LINKS[key] + '?new=1&from=' + encodeURIComponent(from || '') + '&text=' + encodeURIComponent(text || '');
  };

  window.PS_ROOT = ROOT;
  window.PS_LINKS = LINKS;
  window.psHas = function (key) { return !!LINKS[key]; };
  window.psGo = function (key, profile) {
    var p = LINKS[key];
    if (p) location.href = ROOT + p + (profile ? '?profile=' + profile : '');
  };
})();
