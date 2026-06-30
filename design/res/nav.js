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
    home:                'index.html',
    calculator:          'features/calculator/Calculator.html',
    'calculator-tablet':  'features/calculator/CalculatorTablet.html',
    'calculator-desktop': 'features/calculator/CalculatorDesktop.html',
  };

  window.PS_ROOT = ROOT;
  window.PS_LINKS = LINKS;
  window.psHas = function (key) { return !!LINKS[key]; };
  window.psGo = function (key) {
    var p = LINKS[key];
    if (p) location.href = ROOT + p;
  };
})();
