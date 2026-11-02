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
    calculator:          'features/calculator/Calculator.html',
    'calculator-tablet':  'features/calculator/CalculatorTablet.html',
    'calculator-desktop': 'features/calculator/CalculatorDesktop.html',
    auth:                'features/auth/Auth.html',
    'auth-tablet':        'features/auth/AuthTablet.html',
    'auth-desktop':       'features/auth/AuthDesktop.html',
    'auth-states':        'features/auth/AuthStates.html',
    'auth-flow':          'features/auth/AuthFlow.html',
    'support-chat':         'features/support-chat/SupportChat.html',
    'support-chat-tablet':  'features/support-chat/SupportChatTablet.html',
    'support-chat-desktop': 'features/support-chat/SupportChatDesktop.html',
  };

  window.PS_ROOT = ROOT;
  window.PS_LINKS = LINKS;
  window.psHas = function (key) { return !!LINKS[key]; };
  window.psGo = function (key, profile) {
    var p = LINKS[key];
    if (p) location.href = ROOT + p + (profile ? '?profile=' + profile : '');
  };
})();
