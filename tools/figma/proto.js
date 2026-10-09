(async (Z) => {
const page = Z.page;
const screens = {};
for (const n of page.findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) screens[n.getPluginData('screen')] = n;
const inInst = (n, root) => { let p = n.parent; while (p && p !== root) { if (p.type === 'INSTANCE') return true; p = p.parent; } return false; };
const tr = {type: 'DISSOLVE', easing: {type: 'EASE_OUT'}, duration: 0.25};
const SHEETS = {'11': 1, '31': 1, '32': 1, '33': 1, '42': 1, '73': 1};
const DIALOG = {'37': 1, '44': 1};
let links = 0, backs = 0, autos = 0; const missing = [];
for (const [id, s] of Object.entries(screens)) {
  const nodes = s.findAll(n => !!n.getPluginData('to') && !inInst(n, s));
  for (const n of nodes) {
    const t = n.getPluginData('to');
    if (t === id || t === 'call' || t === 'whatsapp') { await n.setReactionsAsync([]); continue; }
    let r = null;
    if (t === 'back') { r = [{trigger: {type: 'ON_CLICK'}, actions: [{type: 'BACK'}]}]; backs++; }
    else if (screens[t]) {
      const trans = SHEETS[t] ? {type: 'MOVE_IN', direction: 'TOP', matchLayers: false, easing: {type: 'EASE_OUT'}, duration: 0.3} : DIALOG[t] ? {type: 'DISSOLVE', easing: {type: 'EASE_OUT'}, duration: 0.2} : tr;
      r = [{trigger: {type: 'ON_CLICK'}, actions: [{type: 'NODE', destinationId: screens[t].id, navigation: 'NAVIGATE', transition: trans, preserveScrollPosition: false}]}]; links++;
    } else { missing.push(id + '→' + t); continue; }
    await n.setReactionsAsync(r);
  }
  const st = s.getPluginData('to');
  if (st && st.startsWith('auto:') && screens[st.slice(5)]) {
    await s.setReactionsAsync([{trigger: {type: 'AFTER_TIMEOUT', timeout: 1.4}, actions: [{type: 'NODE', destinationId: screens[st.slice(5)].id, navigation: 'NAVIGATE', transition: tr, preserveScrollPosition: false}]}]);
    autos++;
  }
}
const L = Z.L;
page.flowStartingPoints = [{nodeId: screens['01'].id, name: L('1 · First launch & log in', '1 · پہلی بار اور لاگ اِن')}, {nodeId: screens['10'].id, name: L('2 · Home hub', '2 · ہوم')}, {nodeId: screens['30'].id, name: L('3 · Book a visit → order', '3 · وزٹ بک کریں → آرڈر')}, {nodeId: screens['70'].id, name: L('4 · Admin dashboard', '4 · ایڈمن ڈیش بورڈ')}];
return {links, backs, autos, missing};
})
