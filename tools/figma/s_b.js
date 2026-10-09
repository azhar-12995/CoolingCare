(async (Z) => {
const L = Z.L, out = [];
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };
const CAT = await (await fetch('http://localhost:9230/cc/catalog.json?' + Date.now())).json();
Z.CAT = CAT;
const tr = o => o ? L(o.en, o.ur) : '';
const FOCUS = {big_fridge: {fit: true, bg: 'tile'}, big_vrf: {top: 0.45}, big_cassette: {top: 0.4}, cat_refrigerator: {fit: true, bg: 'tile'}, cat_commercial: {fit: true, bg: 'tile'}};

const svcRow = (sv, to) => Z.fw(Z.col({name: 'service/' + sv.id, gap: 0}, [Z.fw(Z.row({gap: 14, cross: 'CENTER', pad: [12, 0], to}, [
  Z.img(sv.img, 96, 76, 12, FOCUS[sv.img] || {}),
  Z.fw(Z.col({gap: 4}, [
    Z.fw(Z.t(tr(sv.listTitle || sv.title), 'Title/S', 'text')),
    Z.fw(Z.t(tr(sv.short), 'Body/S', 'text-2', {trunc: 2})),
    Z.fw(Z.pill(L('Inspection quote', 'معائنے کے بعد قیمت'), {icon: 'tag', pad: [5, 10], r: 8}))])),
  Z.ic('chevron-right', 18, 'text-3')])), Z.fw(Z.divider())]));
const header = () => Z.fw(Z.col({name: 'Header', pad: [8, Z.G_, 4, Z.G_], gap: 2}, [
  Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Our Services', 'ہماری سروسز'), 'Title/L', 'text')), Z.iconBtn('search', 'Soft', {size: 38, isz: 19})])),
  Z.fw(Z.t(L('Choose a service for your appliance', 'اپنے اپلائنس کے لیے سروس منتخب کریں'), 'Body/S', 'text-2'))]));
const chips = active => Z.fw(Z.row({name: 'Filters', pad: [10, Z.G_, 6, Z.G_], gap: 8}, [['all', L('All', 'سب'), '20'], ['ac', L('AC', 'اے سی'), '20'], ['refrigeration', L('Refrigeration', 'ریفریجریشن'), '21'], ['commercial', L('Commercial', 'کمرشل'), '22']]
  .map(([k, l, to]) => Z.chip(l, k === active, {to, h: 34}))));

const lists = [['20', 'Services', 'all', CAT.services.filter(s => s.category === 'ac'), L('All services. Chips filter by category (All · AC · Refrigeration · Commercial). Row → Service Details (23). Prices are quoted after inspection, so every row shows "Inspection quote".', 'تمام سروسز۔ چپس سے کیٹیگری فلٹر۔ قطار → تفصیل (23)۔ قیمت معائنے کے بعد۔'), 'image'],
  ['21', 'Services · Refrigeration', 'refrigeration', CAT.services.filter(s => s.category === 'refrigeration'), L('Refrigeration filter (from the Refrigerator tile).', 'ریفریجریشن فلٹر (فریج ٹائل سے)۔'), 'added'],
  ['22', 'Services · Commercial', 'commercial', CAT.services.filter(s => s.category === 'commercial'), L('Commercial filter (from Ceiling Cassette, VRF / VRV and Commercial Cooling tiles).', 'کمرشل فلٹر (کیسٹ، وی آر ایف اور کمرشل ٹائلز سے)۔'), 'added']];
lists.forEach(([id, title, active, items, desc, origin], i) => {
  const s = Z.screen(id, title, {origin, flow: 'C', desc});
  Z.put(s, header()); Z.put(s, chips(active));
  const kids = items.map(sv => svcRow(sv, '23'));
  if (id !== '20') kids.push(Z.note('phone', L('Need something not listed? Call or WhatsApp us and we\'ll help.', 'جو سروس فہرست میں نہیں؟ کال یا واٹس ایپ کریں، ہم مدد کریں گے۔'), 'blue', {pad: [12, 14]}));
  Z.put(s, Z.body(kids, {gap: 0, pad: [0, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.nav('services'));
  P(s, 10 + i);
});

// ---------- 23 Service Details (from design) ----------
{
  const sv = CAT.services.find(s => s.id === 'ac_deep');
  const s = Z.screen('23', 'Service Details', {origin: 'image', flow: 'C', desc: L('Template for every service (content from catalog.json). Book a Visit → form (30) with appliance + service pre-selected.',
    'ہر سروس کے لیے ایک ہی ٹیمپلیٹ (catalog.json سے)۔ وزٹ بک کریں → فارم (30) جس میں اپلائنس اور سروس پہلے سے منتخب۔')});
  Z.put(s, Z.appBar(L('Service Details', 'سروس کی تفصیل'), {center: true}));
  Z.put(s, Z.fw(Z.img(sv.header, 375, 180, 0, {top: 0.5})));
  const inc = Z.fw(Z.col({name: 'Included', fill: 'primary-tint', r: 16, pad: [14, 16], gap: 10}, [
    Z.row({gap: 10, cross: 'CENTER'}, [Z.row({w: 24, h: 24, r: 12, fill: 'primary', main: 'CENTER', cross: 'CENTER'}, [Z.ic('check', 15, 'on-primary', {sw: 3})]), Z.t(L("What's included?", 'اس میں کیا شامل ہے؟'), 'Title/S', 'text')]),
    ...sv.included.map(it => Z.fw(Z.row({gap: 10, cross: 'MIN'}, [Z.ic('check', 18, 'green', {sw: 2.6}), Z.fw(Z.t(tr(it), 'Body/S', 'text'))])))]));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 4}, [Z.fw(Z.t(tr(sv.title), 'Display/M', 'text')), Z.fw(Z.t(tr(sv.about), 'Body/M', 'text-2'))])),
    inc,
    Z.fw(Z.col({gap: 0}, [
      Z.kv('clock', L('Estimated duration', 'متوقع دورانیہ'), tr(sv.duration), {pad: [10, 4]}), Z.fw(Z.divider()),
      Z.kvFill('settings', L('Parts and repairs', 'پرزے اور مرمت'), L('Quoted separately if required', 'ضرورت ہو تو الگ تخمینہ'), {pad: [10, 4]})]))
  ], {gap: 14, pad: [14, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.footer([Z.btn(L('Book a Visit', 'وزٹ بک کریں'), 'Primary', {arrow: true, to: '30'})]));
  P(s, 13);
}
await Z.done();
return out.map(s => s.name);
})
