(async (Z) => {
const L = Z.L, page = Z.page;
page.backgrounds = [{type: 'SOLID', color: Z.rgb('#E8F1FB')}];
const screens = {};
for (const n of [...page.children]) {
  if (n.type === 'SECTION') { for (const c of [...n.children]) if (c.getPluginData('screen')) page.appendChild(c); n.remove(); continue; }
  if (n.getPluginData('screen')) screens[n.getPluginData('screen')] = n; else n.remove();
}
for (const n of page.children) if (n.getPluginData('screen')) screens[n.getPluginData('screen')] = n;
const ROWS = [
  ['A', L('First Launch & Log in', 'پہلی بار اور لاگ اِن'), 'snowflake', L('Splash → language → welcome → phone number → 6-digit SMS code (Firebase Phone Auth) → your details (new accounts only) → Home. Admin numbers go to the Admin Dashboard (Flow G) instead.',
    'سپلیش → زبان → خوش آمدید → فون نمبر → ایس ایم ایس کوڈ → تفصیلات (صرف نیا اکاؤنٹ) → ہوم۔ ایڈمن نمبر ایڈمن ڈیش بورڈ (فلو G) پر۔'), ['01', '02', '03', '04', '05', '06']],
  ['B', L('Home & Discover', 'ہوم'), 'house', L('Hub screen from the design. Area pill → area sheet, bell → notifications (order pushes + reminders), Book a Service → booking form, tiles → services, Call / WhatsApp straight to the shop.',
    'ڈیزائن کی مرکزی سکرین۔ علاقہ، اطلاعات، سروس بک کریں، ٹائلز اور براہِ راست کال / واٹس ایپ۔'), ['10', '11', '12']],
  ['C', L('Services', 'سروسز'), 'layout-grid', L('Live catalogue from Firestore — only services the admin has opened are shown. Every price is "Inspection quote". Details → Book a Visit.',
    'فائرسٹور سے لائیو فہرست — صرف ایڈمن کی کھولی ہوئی سروسز نظر آتی ہیں۔ تفصیل → وزٹ بک کریں۔'), ['20', '21', '22', '23']],
  ['D', L('Book a Visit → Place Order', 'وزٹ بک کریں ← آرڈر دیں'), 'clipboard-list', L('Form → sheets (appliance & service, date & time, photos) → validation → review → Place Order saves to Firestore (CC-1001, CC-1002 …), uploads photos and pushes the order to every admin.',
    'فارم → شیٹس → تصدیق → جائزہ → آرڈر فائرسٹور میں، تصاویر اسٹوریج میں اور ہر ایڈمن کو پش۔'), ['30', '31', '32', '33', '34', '35', '36', '37']],
  ['E', L('Booking Status (live)', 'بکنگ اسٹیٹس (لائیو)'), 'calendar-check', L('Waiting → Confirmed → On the way → Completed, moved by the admin; each step arrives as a push and the screen updates live. Customers can change the time or cancel until the technician leaves.',
    'انتظار → کنفرم → راستے میں → مکمل، ایڈمن آگے بڑھاتا ہے؛ ہر قدم پر پش۔ کسٹمر ٹیکنیشن کے نکلنے تک وقت بدل یا منسوخ کر سکتا ہے۔'), ['40', '41', '42', '43', '44']],
  ['F', L('Bookings, Support & Account', 'بکنگز، مدد اور اکاؤنٹ'), 'headset', L('User dashboard: bookings and support tabs. Settings: language, my details, reminders, privacy, log out, delete account. Pushes and reminders arrive even when the app is closed.',
    'یوزر ڈیش بورڈ: بکنگز اور مدد۔ سیٹنگز: زبان، تفصیلات، یاد دہانیاں، پرائیویسی، لاگ آؤٹ، اکاؤنٹ حذف۔'), ['50', '51', '52', '53', '54', '55', '56', '61']],
  ['G', L('Admin Dashboard', 'ایڈمن ڈیش بورڈ'), 'shield-user', L('Same app, admin role (phones in config/roles). Live orders with push alerts, order detail with call / WhatsApp, status updates that notify the customer, services manager (add, edit, open / close — closed services vanish for customers) and business settings.',
    'وہی ایپ، ایڈمن رول۔ لائیو آرڈر اور پش، آرڈر کی تفصیل، اسٹیٹس اپڈیٹ (کسٹمر کو اطلاع)، سروسز مینیجر (شامل، ترمیم، کھولیں / بند کریں) اور کاروباری سیٹنگز۔'), ['70', '71', '72', '73', '74', '75', '76', '77']]
];
const rowOf = {}; ROWS.forEach((r, ri) => r[4].forEach((id, i) => rowOf[id] = [ri, i]));
const SW = 375, GAP = 150, PADL = 90, TOP = 260, BOT = 330;
const inInst = (n, root) => { let p = n.parent; while (p && p !== root) { if (p.type === 'INSTANCE') return true; p = p.parent; } return false; };
const linkName = n => {
  if (n.name.startsWith('nav/') && Z.NAVL[n.name.slice(4)]) return Z.L(...Z.NAVL[n.name.slice(4)]);
  if (n.name === 'input' && n.parent) { const lb = n.parent.findOne(x => x.type === 'TEXT'); if (lb) return lb.characters; }
  if (n.type === 'TEXT') return n.characters.split('\n')[0];
  const tx = n.findOne && n.findOne(x => x.type === 'TEXT'); if (tx) return tx.characters.split('\n')[0].slice(0, 26);
  return n.name.replace(/^[\w-]+\//, '').slice(0, 24);
};
const titleOf = id => screens[id] ? screens[id].getPluginData('title') : id;
const ORG = {image: ['primary-tint', 'primary', L('From client design', 'کلائنٹ ڈیزائن سے')], added: ['orange-tint', 'orange-text', L('Added', 'اضافہ')]};
const mkArrow = async (parent, pts, o = {}) => {
  const minX = Math.min(...pts.map(p => p[0])), minY = Math.min(...pts.map(p => p[1]));
  const v = figma.createVector(); v.name = o.name || 'arrow';
  await v.setVectorNetworkAsync({vertices: pts.map(([x, y], i) => ({x: x - minX, y: y - minY, strokeCap: i === pts.length - 1 ? 'ARROW_LINES' : 'ROUND', strokeJoin: 'ROUND', cornerRadius: 18})),
    segments: pts.slice(1).map((_, i) => ({start: i, end: i + 1})), regions: []});
  v.strokes = [Z.paint(o.color || 'primary-bright')]; v.strokeWeight = o.w || 3; v.fills = [];
  if (o.dash) v.dashPattern = o.dash;
  parent.appendChild(v); v.x = minX; v.y = minY; return v;
};
const labels = [];
const label = (parent, text, x, y, o = {}) => { const p = Z.row({name: 'label', pad: [5, 12], r: 999, gap: 6, cross: 'CENTER', fill: 'surface', stroke: 'primary-soft', ltr: true}, [o.icon ? Z.ic(o.icon, 14, 'primary', {ltr: true}) : null, Z.t(text, 'Label/M', 'primary', {ltr: true})]); parent.appendChild(p); p.x = x; p.y = y; labels.push([p, x, o.center]); return p; };

// page title + legend
const ttl = Z.col({name: 'Page Title', gap: 10, ltr: true}, [
  Z.row({gap: 18, cross: 'CENTER', ltr: true}, [Z.logo(72), Z.t(L('Cooling Care — App Flow (English)', 'کولنگ کیئر — ایپ فلو (اردو)'), 'Board/H1', 'text', {ltr: true})]),
  Z.t(L('44 screens · 7 flows (customer + admin) · Firebase phone login, Firestore orders, push notifications · ▶ Present starts at Splash, Home, Book a Visit or Admin. Screen 375 × 812; the Urdu page mirrors every layout right-to-left.',
    '44 سکرینز · 7 فلو (کسٹمر + ایڈمن) · فائربیس لاگ اِن، فائرسٹور آرڈر، پش · ▶ پریزنٹ۔ اردو صفحے پر ہر لے آؤٹ دائیں سے بائیں ہے۔'), 'Board/Body', 'text-2', {w: 1800, ltr: true})]);
page.appendChild(ttl); ttl.x = 0; ttl.y = -330;
let y = 0; const sections = [];
for (const [key, name, icon, desc, ids] of ROWS) {
  const rowH = Math.max(...ids.map(id => screens[id].height));
  const W = PADL * 2 + ids.length * SW + (ids.length - 1) * GAP, H = TOP + rowH + BOT;
  const nImg = ids.filter(id => screens[id].getPluginData('origin') === 'image').length;
  const sec = figma.createSection(); page.appendChild(sec); sec.name = key + ' · ' + name; sec.x = 0; sec.y = y; sec.resizeWithoutConstraints(Math.max(W, 2400), H);
  sec.fills = [{type: 'SOLID', color: {r: 1, g: 1, b: 1}, opacity: 0.75}];
  const head = Z.row({name: 'Section title', gap: 18, cross: 'CENTER', ltr: true}, [Z.iconTile(icon, 'dark', 64, 30, {r: 20}),
    Z.col({gap: 6, ltr: true}, [Z.row({gap: 14, cross: 'CENTER', ltr: true}, [Z.t(L('Flow ', 'فلو ') + key + ' · ' + name, 'Board/H2', 'text', {ltr: true}),
      Z.pill(ids.length + L(' screens', ' سکرینز'), {fill: 'surface-2', color: 'text-2', style: 'Board/Label', pad: [6, 14]}),
      nImg ? Z.pill(nImg + ' · ' + ORG.image[2], {fill: 'primary-tint', color: 'primary', style: 'Board/Label', pad: [6, 14]}) : null,
      ids.length - nImg ? Z.pill((ids.length - nImg) + ' · ' + ORG.added[2], {fill: 'orange-tint', color: 'orange-text', style: 'Board/Label', pad: [6, 14]}) : null]),
      Z.t(desc, 'Board/Body', 'text-2', {w: 1900, ltr: true})])]);
  sec.appendChild(head); head.x = PADL; head.y = 46;
  ids.forEach((id, i) => {
    const s = screens[id];
    sec.appendChild(s); s.x = PADL + i * (SW + GAP); s.y = TOP;
    const og = ORG[s.getPluginData('origin') || 'added'];
    const outs = [...new Set(s.findAll(n => n.getPluginData('to') && !inInst(n, s)).map(n => n.getPluginData('to').replace('auto:', '')))].filter(t => screens[t] && t !== id);
    const cross = outs.filter(t => rowOf[t] && rowOf[t][0] !== rowOf[id][0]);
    const cap = Z.col({name: 'Caption · ' + id, gap: 8, w: SW, ltr: true}, [
      Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.t(id + ' · ' + titleOf(id), 'Title/M', 'text', {ltr: true}), Z.pill(og[2], {fill: og[0], color: og[1]})]),
      Z.fw(Z.t(s.description || '', 'Body/S', 'text-2', {ltr: !Z.rtl})),
      cross.length ? Z.fw(Z.row({gap: 6, wrap: true, wgap: 6, ltr: true}, cross.slice(0, 8).map(t => Z.pill('→ ' + t + ' ' + titleOf(t), {fill: 'surface-2', color: 'text-2'})))) : null]);
    sec.appendChild(cap); cap.x = s.x; cap.y = TOP + s.height + 28;
  });
  sections.push({sec, ids, rowH});
  y += H + 120;
}
await Z.done();
let arrows = 0;
for (const {sec, ids, rowH} of sections) {
  const pairs = [];
  ids.forEach((id, i) => {
    const s = screens[id]; const seen = new Set();
    const nodes = s.findAll(n => !!n.getPluginData('to') && !inInst(n, s));
    if (s.getPluginData('to')) nodes.unshift(s);
    for (const n of nodes) {
      let t = n.getPluginData('to'); const auto = t.startsWith('auto:'); if (auto) t = t.slice(5);
      if (!screens[t] || t === id || seen.has(t)) continue;
      const j = ids.indexOf(t); if (j < 0) continue; seen.add(t); pairs.push([i, j, auto ? L('auto', 'خودکار') : linkName(n)]);
    }
  });
  pairs.sort((a, b) => Math.abs(a[0] - a[1]) - Math.abs(b[0] - b[1]));
  let lt = 0, lb = 0;
  for (const [i, j, lbl] of pairs) {
    const sx = PADL + i * (SW + GAP), tx = PADL + j * (SW + GAP);
    if (j === i + 1) { const yy = TOP + 300 + (lt % 2) * 0; await mkArrow(sec, [[sx + SW + 12, yy], [tx - 12, yy]]); label(sec, lbl, sx + SW + 12, yy - 40, {center: GAP - 24}); }
    else if (j > i) { const yy = TOP - 30 - (lt++ % 3) * 16; await mkArrow(sec, [[sx + SW / 2 + 40, TOP - 6], [sx + SW / 2 + 40, yy], [tx + SW / 2 - 40, yy], [tx + SW / 2 - 40, TOP - 8]], {dash: [10, 8]}); label(sec, lbl, (sx + tx + SW) / 2 - 60, yy - 15); }
    else { const yy = TOP + rowH + 200 + (lb++ % 3) * 22; await mkArrow(sec, [[sx + SW / 2, TOP + rowH + 170], [sx + SW / 2, yy], [tx + SW / 2 + 60, yy], [tx + SW / 2 + 60, TOP + rowH + 172]], {dash: [10, 8], color: 'text-3'}); label(sec, lbl, (sx + tx + SW) / 2 - 60, yy - 15); }
    arrows++;
  }
}
await Z.done();
for (const [p, x, center] of labels) if (center) p.x = x + (center - p.width) / 2;
const leg = Z.row({name: 'Legend', gap: 24, pad: [16, 24], r: 20, fill: 'surface', stroke: 'border', cross: 'CENTER', ltr: true}, [
  Z.t(L('Legend', 'علامات'), 'Title/M', 'text', {ltr: true}),
  Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.rect(60, 3, 'primary-bright'), Z.t(L('tap → next screen', 'ٹیپ → اگلی سکرین'), 'Body/M', 'text-2', {ltr: true})]),
  Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.rect(60, 3, 'text-3'), Z.t(L('dashed = jump / back', 'ڈیشڈ = واپس / چھلانگ'), 'Body/M', 'text-2', {ltr: true})]),
  Z.pill(ORG.image[2], {fill: 'primary-tint', color: 'primary'}), Z.pill(ORG.added[2], {fill: 'orange-tint', color: 'orange-text'})]);
page.appendChild(leg); leg.x = 0; leg.y = -110;
await Z.done();
return {sections: sections.length, arrows, height: y};
})
