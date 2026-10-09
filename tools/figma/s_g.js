(async (Z) => {
// ---------- Flow G: Admin (same app, admin role from config/roles) ----------
const L = Z.L, out = [];
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980 + 2000; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };
const CAT = Z.CAT || await (await fetch('http://localhost:9230/cc/catalog.json?' + Date.now())).json();
const tr = o => o ? L(o.en, o.ur) : '';

const NAV = [['dashboard', 'layout-dashboard', '70', ['Dashboard', 'ڈیش بورڈ']], ['orders', 'clipboard-list', '71', ['Orders', 'آرڈرز']], ['services', 'wrench', '74', ['Services', 'سروسز']], ['settings', 'settings', '76', ['Settings', 'سیٹنگز']]];
const nav = (active) => Z.fw(Z.col({name: 'Admin Nav', fill: 'surface', fx: 'Shadow/Nav'}, [Z.fw(Z.row({pad: [4, 14, 0, 14], main: 'SPACE_BETWEEN', cross: 'MIN'}, NAV.map(([k, icon, to, l]) => {
  const on = k === active, col = on ? 'primary' : 'text-3';
  return Z.col({name: 'nav/' + k, w: 76, gap: 3, cross: 'CENTER', pad: [6, 0, 0, 0], to}, [Z.row({w: 52, h: 28, r: 14, fill: on ? 'primary-tint' : null, main: 'CENTER', cross: 'CENTER'}, [Z.ic(icon, 21, col)]), Z.t(L(...l), on ? 'Label/S' : 'Caption', col)]);
}))), Z.homeBar()]));
const ST = {
  requested: [L('New', 'نیا'), 'blue', 'primary-tint', 'primary'], confirmed: [L('Confirmed', 'کنفرم'), 'green', 'green-tint', 'green-text'],
  on_the_way: [L('On the way', 'راستے میں'), 'amber', 'warning-tint', 'warning'], completed: [L('Completed', 'مکمل'), 'grey', 'surface-2', 'text-2'], cancelled: [L('Cancelled', 'منسوخ'), 'red', 'danger-tint', 'danger'],
};
const badge = st => Z.pill(ST[st][0], {fill: ST[st][2], color: ST[st][3], pad: [3, 9]});
const ORDERS = [
  {code: 'CC-1004', st: 'requested', name: L('Ali Raza', 'علی رضا'), phone: '0300 1234567', svc: 'ac_deep', app: L('Split AC', 'اسپلٹ اے سی'), date: L('Thu, 08 Oct', 'جمعرات، 08 اکتوبر'), slot: L('10 AM – 12 PM', 'صبح 10 تا دوپہر 12'), ago: L('12 min ago', '12 منٹ پہلے'), area: L('Main Street, Pasrur', 'مین اسٹریٹ، پسرور')},
  {code: 'CC-1003', st: 'requested', name: L('Sana Ahmed', 'ثناء احمد'), phone: '0312 7654321', svc: 'fridge_repair', app: L('Refrigerator', 'فریج'), date: L('Thu, 08 Oct', 'جمعرات، 08 اکتوبر'), slot: L('2 PM – 4 PM', 'دوپہر 2 تا شام 4'), ago: L('40 min ago', '40 منٹ پہلے'), area: L('Chawinda', 'چونڈہ')},
  {code: 'CC-1002', st: 'confirmed', name: L('Usman Tariq', 'عثمان طارق'), phone: '0333 2468101', svc: 'ac_install', app: L('Split AC', 'اسپلٹ اے سی'), date: L('Fri, 09 Oct', 'جمعہ، 09 اکتوبر'), slot: L('12 PM – 2 PM', 'دوپہر 12 تا 2'), ago: L('Yesterday', 'کل'), area: L('Sialkot Road', 'سیالکوٹ روڈ')},
  {code: 'CC-1001', st: 'on_the_way', name: L('Hamza Iqbal', 'حمزہ اقبال'), phone: '0301 1357913', svc: 'ac_general', app: L('Split AC', 'اسپلٹ اے سی'), date: L('Today', 'آج'), slot: L('4 PM – 6 PM', 'شام 4 تا 6'), ago: L('2 days ago', '2 دن پہلے'), area: L('Qila Kalar Wala', 'قلعہ کالر والا')},
];
const svcOf = id => CAT.services.find(s => s.id === id);
const FOCUS = {big_fridge: {fit: true, bg: 'tile'}, cat_refrigerator: {fit: true, bg: 'tile'}, cat_commercial: {fit: true, bg: 'tile'}, big_vrf: {top: 0.45}, big_cassette: {top: 0.4}};
const orderCard = (o, actions) => Z.fw(Z.col({name: 'order/' + o.code, r: 16, fill: 'surface', stroke: 'border', fx: 'Shadow/Card', pad: [12, 12], gap: 10, to: '72'}, [
  Z.fw(Z.row({gap: 8, cross: 'CENTER'}, [Z.t(o.code, 'Title/S', 'text', {ltr: true}), badge(o.st), Z.fw(Z.box(4, 4, {name: 'sp'})), Z.t(o.ago, 'Caption', 'text-3')])),
  Z.fw(Z.row({gap: 12, cross: 'CENTER'}, [Z.img(svcOf(o.svc).img, 56, 48, 10, FOCUS[svcOf(o.svc).img] || {}),
    Z.fw(Z.col({gap: 1}, [Z.fw(Z.t(tr(svcOf(o.svc).title), 'Title/S', 'text')), Z.t(o.name + ' · ' + o.phone, 'Body/S', 'text-2'),
      Z.row({gap: 6, cross: 'CENTER'}, [Z.ic('calendar', 14, 'text-3'), Z.t(o.date + ' | ' + o.slot, 'Caption', 'text-2')])]))])),
  actions ? Z.fw(Z.row({gap: 8}, actions)) : null]));
const stat = (icon, tone, n, label, to) => Z.fw(Z.col({name: 'stat/' + label, r: 16, fill: 'surface', stroke: 'border', fx: 'Shadow/Card', pad: [12, 14], gap: 8, to}, [
  Z.iconTile(icon, tone, 36, 18, {r: 10}), Z.t(String(n), 'Display/M', 'text', {ltr: true}), Z.t(label, 'Label/M', 'text-2')]));

// ---------- 70 Admin Dashboard ----------
{
  const s = Z.screen('70', 'Admin Dashboard', {flow: 'G', desc: L('Admins land here after log-in. Live counts from Firestore; new orders arrive with a push + a badge. Confirm sends the customer a push. Admins can switch to the customer view in Settings (76).',
    'لاگ اِن کے بعد ایڈمن یہاں آتا ہے۔ فائرسٹور سے لائیو گنتی؛ نیا آرڈر پش کے ساتھ آتا ہے۔ کنفرم کرنے پر کسٹمر کو پش جاتا ہے۔')});
  Z.put(s, Z.fw(Z.row({name: 'Top Bar', h: 56, pad: [0, 14, 0, Z.G_], cross: 'CENTER', gap: 10}, [
    Z.logo(36, {fx: null, sw: 2}), Z.fw(Z.col({gap: 0}, [Z.t('Cooling Care', 'Title/S', 'text', {ltr: !Z.rtl, align: Z.rtl ? 'RIGHT' : null}), Z.pill(L('Admin', 'ایڈمن'), {fill: 'primary', color: 'on-primary', pad: [2, 8], icon: 'shield-user', isz: 12})])),
    Z.iconBtn('bell', 'Plain', {badge: true, to: '77'})])));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 2}, [Z.t(L('Good morning, Admin!', 'صبح بخیر، ایڈمن!'), 'Title/L', 'text'), Z.t(L('Thursday, 08 October', 'جمعرات، 08 اکتوبر'), 'Body/S', 'text-2')])),
    Z.fw(Z.col({gap: 10, name: 'Stats'}, [Z.fw(Z.row({gap: 10}, [stat('inbox', 'blue', 2, L('New orders', 'نئے آرڈر'), '71'), stat('calendar-check', 'green', 5, L('Visits today', 'آج کے وزٹ'), '71')])),
      Z.fw(Z.row({gap: 10}, [stat('truck', 'amber', 1, L('On the way', 'راستے میں'), '71'), stat('circle-check', 'grey', 12, L('Done this week', 'اس ہفتے مکمل'), '71')]))])),
    Z.sec(L('New orders', 'نئے آرڈر'), L('See all', 'سب دیکھیں'), {to: '71'}),
    orderCard(ORDERS[0], [Z.fw(Z.btn(L('View', 'دیکھیں'), 'Light', {small: true, h: 38, to: '72'})), Z.fw(Z.btn(L('Confirm', 'کنفرم کریں'), 'Green', {small: true, h: 38, lead: 'check', to: '72'}))]),
    orderCard(ORDERS[1], [Z.fw(Z.btn(L('View', 'دیکھیں'), 'Light', {small: true, h: 38, to: '72'})), Z.fw(Z.btn(L('Confirm', 'کنفرم کریں'), 'Green', {small: true, h: 38, lead: 'check', to: '72'}))]),
  ], {gap: 14, pad: [4, Z.G_, 10, Z.G_]}));
  Z.put(s, nav('dashboard'));
  P(s, 0);
}

// ---------- 71 Orders ----------
{
  const s = Z.screen('71', 'Orders', {flow: 'G', desc: L('All orders, newest first, filtered by status; search by order number, name or phone. Tap → Order Detail (72).', 'تمام آرڈر، نئے پہلے، اسٹیٹس کے مطابق فلٹر؛ نمبر، نام یا فون سے تلاش۔')});
  Z.put(s, Z.fw(Z.col({name: 'Header', pad: [8, Z.G_, 4, Z.G_], gap: 10}, [
    Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Orders', 'آرڈرز'), 'Title/L', 'text')), Z.iconBtn('search', 'Soft', {size: 38, isz: 19})])),
  ])));
  Z.put(s, Z.fw(Z.row({name: 'Filters', pad: [4, Z.G_, 6, Z.G_], gap: 8}, [Z.chip(L('New · 2', 'نئے · 2'), true, {h: 34}), Z.chip(L('Confirmed · 5', 'کنفرم · 5'), false, {h: 34}), Z.chip(L('On the way · 1', 'راستے میں · 1'), false, {h: 34}), Z.chip(L('Done', 'مکمل'), false, {h: 34})])));
  Z.put(s, Z.body(ORDERS.map(o => orderCard(o)), {gap: 10, pad: [6, Z.G_, 10, Z.G_]}));
  Z.put(s, nav('orders'));
  P(s, 1);
}

// ---------- 72 Order Detail (admin) ----------
{
  const o = ORDERS[0], sv = svcOf(o.svc);
  const s = Z.screen('72', 'Order Detail', {flow: 'G', h: 1000, desc: L('Everything the technician needs: customer (call / WhatsApp), service, time, address, issue, photos and history. Actions follow the status: New → Confirm / Propose new time / Cancel; Confirmed → On the way; On the way → Completed. Every change sends the customer a push.',
    'ٹیکنیشن کی ہر ضرورت: کسٹمر (کال / واٹس ایپ)، سروس، وقت، پتہ، مسئلہ، تصاویر اور ہسٹری۔ ہر تبدیلی پر کسٹمر کو پش جاتا ہے۔')});
  Z.put(s, Z.appBar(L('Order', 'آرڈر') + ' ' + o.code, {center: true, actions: [Z.iconBtn('ellipsis-vertical', 'Plain', {color: 'text', to: '73'})]}));
  const sec = (title, kids, o2 = {}) => Z.fw(Z.col({name: 'sec/' + title, r: 16, fill: 'surface', stroke: 'border', pad: [12, 14], gap: 10}, [Z.t(title, 'Label/M', 'text-3', {upper: true}), ...kids]));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'Banner', pad: [12, 14], r: 16, fill: 'primary-tint', gap: 12, cross: 'CENTER'}, [Z.iconTile('inbox', 'dark', 40, 20, {fill: 'primary'}),
      Z.fw(Z.col({gap: 1}, [Z.t(L('New order — waiting for you', 'نیا آرڈر — آپ کا انتظار'), 'Title/S', 'primary'), Z.t(L('Placed 12 min ago · Thu, 08 Oct', '12 منٹ پہلے · جمعرات، 08 اکتوبر'), 'Body/S', 'text-2')]))])),
    sec(L('Customer', 'کسٹمر'), [Z.fw(Z.row({gap: 12, cross: 'CENTER'}, [Z.row({w: 44, h: 44, r: 22, fill: 'primary', main: 'CENTER', cross: 'CENTER'}, [Z.t('AR', 'Title/S', 'on-primary', {ltr: true})]),
      Z.fw(Z.col({gap: 0}, [Z.t(o.name, 'Title/S', 'text'), Z.t(o.phone, 'Body/S', 'text-2', {ltr: true})])),
      Z.iconTile('phone', 'green', 40, 19, {fill: 'green', color: 'on-primary'}), Z.iconTile('whatsapp', 'green', 40, 19, {fill: 'green-tint', color: 'green'})]))]),
    sec(L('Service', 'سروس'), [Z.fw(Z.row({gap: 12, cross: 'CENTER'}, [Z.img(sv.img, 60, 50, 10), Z.fw(Z.col({gap: 1}, [Z.t(tr(sv.title), 'Title/S', 'text'), Z.t(o.app, 'Body/S', 'text-2')]))])),
      Z.info('calendar', o.date + ' 2026  ·  ' + o.slot, {style: 'Body/S', pad: [0, 0]}),
      Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.ic('map-pin', 18, 'text-2'), Z.fw(Z.t(L('House No. 12, Main Street (near Jamia Masjid), Pasrur', 'مکان نمبر 12، مین اسٹریٹ (جامع مسجد کے قریب)، پسرور'), 'Body/S', 'text')), Z.ic('copy', 16, 'primary')]))]),
    sec(L('Issue & photos', 'مسئلہ اور تصاویر'), [Z.fw(Z.t(L('Not cooling well, unusual noise from the indoor unit.', 'ٹھنڈک کم ہے، اندرونی یونٹ سے عجیب آواز۔'), 'Body/M', 'text')),
      Z.row({gap: 10}, [Z.img('svc_gas', 72, 72, 12), Z.img('detail_deep_clean', 72, 72, 12)])]),
    sec(L('History', 'ہسٹری'), [Z.info('send', L('Requested by customer · 10:05 AM', 'کسٹمر کی درخواست · صبح 10:05'), {style: 'Body/S', pad: [0, 0], icol: 'primary'})]),
    Z.field(L('Note for the technician', 'ٹیکنیشن کے لیے نوٹ'), L('e.g. bring gas cylinder', 'مثلاً گیس سلنڈر ساتھ لائیں'), {ph: true, opt: L('(optional)', '(اختیاری)'), icon: 'notebook-pen'}),
  ], {gap: 12, pad: [4, Z.G_, 10, Z.G_]}));
  Z.put(s, Z.footer([Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Cancel', 'منسوخ'), 'DangerSoft', {lead: 'ban', h: 48, small: true, to: '73'})), Z.fw(Z.btn(L('Confirm visit', 'وزٹ کنفرم کریں'), 'Green', {lead: 'check', h: 48, to: '73'}))])),
    Z.fw(Z.row({main: 'CENTER'}, [Z.t(L('Propose a different time', 'دوسرا وقت تجویز کریں'), 'Label/M', 'primary')]))]));
  P(s, 2);
}

// ---------- 73 Update Status (admin sheet) ----------
{
  const s = Z.screen('73', 'Update Status', {flow: 'G', noStatus: true, desc: L('Admin changes the status; the customer gets a push in their language (Cloud Function onOrderUpdated). Cancel asks for a reason that the customer sees.',
    'ایڈمن اسٹیٹس بدلتا ہے؛ کسٹمر کو اس کی زبان میں پش جاتا ہے۔ منسوخی پر وجہ پوچھی جاتی ہے۔')});
  const opt = (icon, title, sub, on) => Z.fw(Z.row({name: 'opt/' + title, pad: [11, 12], r: 14, gap: 12, cross: 'CENTER', fill: on ? 'primary-tint' : 'surface', stroke: on ? 'primary' : 'border', sw: on ? 1.5 : 1}, [
    Z.iconTile(icon, on ? 'blue' : 'grey', 36, 18, {r: 10}), Z.fw(Z.col({gap: 0}, [Z.t(title, 'Title/S', 'text'), Z.fw(Z.t(sub, 'Caption', 'text-2'))])), Z.radio(on)]));
  const sh = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.fw(Z.t(L('Update order', 'آرڈر اپڈیٹ کریں') + ' CC-1004', 'Title/M', 'text')), Z.iconBtn('x', 'Grey', {size: 34, isz: 18, to: 'back'})])),
    opt('calendar-check', L('Confirmed', 'کنفرم'), L('Date & time agreed with the customer', 'کسٹمر سے تاریخ اور وقت طے'), true),
    opt('truck', L('On the way', 'راستے میں'), L('Technician has left for the visit', 'ٹیکنیشن روانہ ہو گیا'), false),
    opt('circle-check', L('Completed', 'مکمل'), L('Work done — customer can rate it', 'کام مکمل — کسٹمر ریٹ کر سکتا ہے'), false),
    opt('ban', L('Cancelled', 'منسوخ'), L('Tell the customer why', 'کسٹمر کو وجہ بتائیں'), false),
    Z.note('bell-ring', L('Ali Raza gets a notification as soon as you save.', 'محفوظ کرتے ہی علی رضا کو اطلاع جائے گی۔'), 'green'),
    Z.fw(Z.btn(L('Save & notify customer', 'محفوظ کریں اور اطلاع دیں'), 'Primary', {to: '72'}))
  ], {gap: 10});
  Z.overlay(s, '72', sh); await Z.done(); Z.dock(sh);
  P(s, 3);
}

// ---------- 74 Services Manager ----------
{
  const s = Z.screen('74', 'Services Manager', {flow: 'G', h: 900, desc: L('Every service in Firestore. The switch opens / closes a service instantly — closed services disappear from the customer app. Tap a row to edit; Add creates a new one.',
    'فائرسٹور کی تمام سروسز۔ سوئچ سے سروس فوراً کھلتی / بند ہوتی ہے — بند سروس کسٹمر کو نظر نہیں آتی۔ ترمیم کے لیے ٹیپ کریں۔')});
  Z.put(s, Z.fw(Z.col({name: 'Header', pad: [8, Z.G_, 4, Z.G_], gap: 2}, [
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.fw(Z.t(L('Services', 'سروسز'), 'Title/L', 'text')), Z.btn(L('Add', 'شامل کریں'), 'Primary', {lead: 'plus', small: true, h: 38, px: 14, to: '75'})])),
    Z.t(L('8 open · 2 closed', '8 کھلی · 2 بند'), 'Body/S', 'text-2')])));
  Z.put(s, Z.fw(Z.row({name: 'Filters', pad: [8, Z.G_, 6, Z.G_], gap: 8}, [Z.chip(L('All', 'سب'), true, {h: 34}), Z.chip(L('AC', 'اے سی'), false, {h: 34}), Z.chip(L('Refrigeration', 'ریفریجریشن'), false, {h: 34}), Z.chip(L('Commercial', 'کمرشل'), false, {h: 34})])));
  const closed = new Set(['fridge_gas', 'vrf']);
  const catName = {ac: L('AC', 'اے سی'), refrigeration: L('Refrigeration', 'ریفریجریشن'), commercial: L('Commercial', 'کمرشل')};
  const rows = CAT.services.slice(0, 8).map(sv => {
    const off = closed.has(sv.id);
    return Z.fw(Z.col({gap: 0}, [Z.fw(Z.row({name: 'svc/' + sv.id, gap: 12, cross: 'CENTER', pad: [10, 0], to: '75', op: off ? 0.6 : 1}, [
      Z.img(sv.img, 56, 48, 10, FOCUS[sv.img] || {}),
      Z.fw(Z.col({gap: 2}, [Z.fw(Z.t(tr(sv.listTitle || sv.title), 'Title/S', 'text')), Z.row({gap: 6, cross: 'CENTER'}, [Z.t(catName[sv.category] + ' · ' + tr(sv.duration), 'Caption', 'text-2')]),
        off ? Z.pill(L('Closed — hidden from customers', 'بند — کسٹمرز سے پوشیدہ'), {fill: 'surface-2', color: 'text-2', icon: 'eye-off', pad: [2, 8]}) : null])),
      Z.col({gap: 2, cross: 'CENTER'}, [Z.toggle(!off), Z.t(off ? L('Closed', 'بند') : L('Open', 'کھلی'), 'Caption', off ? 'text-3' : 'green-text')])])), Z.fw(Z.divider())]));
  });
  Z.put(s, Z.body(rows, {gap: 0, pad: [0, Z.G_, 10, Z.G_]}));
  Z.put(s, nav('services'));
  P(s, 4);
}

// ---------- 75 Add / Edit Service ----------
{
  const sv = svcOf('ac_deep');
  const s = Z.screen('75', 'Edit Service', {flow: 'G', h: 1320, desc: L('Bilingual form (English + Urdu) so customers see their language. Photo uploads to Firebase Storage (services/…). Saving updates the customer app immediately; Delete asks for confirmation.',
    'دو زبانوں میں فارم۔ تصویر فائربیس اسٹوریج میں۔ محفوظ کرتے ہی کسٹمر ایپ میں تبدیلی؛ حذف سے پہلے تصدیق۔')});
  Z.put(s, Z.appBar(L('Edit service', 'سروس میں ترمیم'), {center: true, actions: [Z.iconBtn('trash-2', 'Plain', {color: 'danger'})]}));
  const pair = (label, en, ur, o2 = {}) => Z.fw(Z.col({gap: 6, name: 'pair/' + label}, [Z.t(label, 'Label/M', 'text'),
    Z.fw(Z.row({h: o2.multi ? 62 : 46, r: 12, fill: 'surface', stroke: 'border', pad: o2.multi ? [10, 14] : [0, 14], gap: 10, cross: o2.multi ? 'MIN' : 'CENTER', ltr: true}, [Z.pill('EN', {fill: 'surface-2', color: 'text-2', pad: [2, 6]}), Z.fw(Z.t(en, 'Body/M', 'text', {ltr: true, align: 'LEFT', trunc: 2}))])),
    Z.fw(Z.row({h: o2.multi ? 70 : 50, r: 12, fill: 'surface', stroke: 'border', pad: o2.multi ? [6, 14] : [0, 14], gap: 10, cross: o2.multi ? 'MIN' : 'CENTER', ltr: true}, [Z.fw(Z.t(ur, 'Body/M', 'text', {ltr: true, align: 'RIGHT', trunc: 2})), Z.pill('اردو', {fill: 'surface-2', color: 'text-2', pad: [0, 6]})]))]));
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 14, cross: 'CENTER'}, [Z.img(sv.img, 110, 84, 14), Z.fw(Z.col({gap: 8}, [Z.t(L('Service photo', 'سروس کی تصویر'), 'Label/M', 'text'), Z.btn(L('Change photo', 'تصویر بدلیں'), 'Soft', {lead: 'image-up', small: true, h: 38, px: 12})]))])),
    Z.fw(Z.col({gap: 8}, [Z.t(L('Category', 'کیٹیگری'), 'Label/M', 'text'), Z.row({gap: 8}, [Z.chip(L('AC', 'اے سی'), true, {h: 34, flat: true}), Z.chip(L('Refrigeration', 'ریفریجریشن'), false, {h: 34}), Z.chip(L('Commercial', 'کمرشل'), false, {h: 34})])])),
    pair(L('Title', 'عنوان'), sv.title.en, sv.title.ur),
    pair(L('Short description', 'مختصر تفصیل'), sv.short.en, sv.short.ur, {multi: true}),
    Z.fw(Z.col({gap: 8}, [Z.t(L("What's included", 'کیا شامل ہے'), 'Label/M', 'text'), ...sv.included.slice(0, 3).map(it => Z.fw(Z.row({gap: 10, cross: 'CENTER', pad: [8, 12], r: 12, stroke: 'border'}, [Z.ic('grip-vertical', 16, 'text-3'), Z.fw(Z.t(tr(it), 'Body/S', 'text', {trunc: 1})), Z.ic('x', 16, 'text-3')]))),
      Z.t(L('+ Add item', '+ آئٹم شامل کریں'), 'Label/M', 'primary')])),
    pair(L('Duration', 'دورانیہ'), sv.duration.en, sv.duration.ur),
    Z.listRow({icon: 'eye', title: L('Open for booking', 'بکنگ کے لیے کھلی'), sub: L('Customers can see and book this service', 'کسٹمرز یہ سروس دیکھ اور بک کر سکتے ہیں'), right: Z.toggle(true), fill: 'bg-2', r: 14, pad: [10, 12]}),
  ], {gap: 16, pad: [6, Z.G_, 12, Z.G_]}));
  Z.put(s, Z.footer([Z.btn(L('Save service', 'سروس محفوظ کریں'), 'Primary', {to: '74'})]));
  P(s, 5);
}

// ---------- 76 Admin Settings / Business info ----------
{
  const s = Z.screen('76', 'Admin Settings', {flow: 'G', h: 900, desc: L('Business details shown to customers (Call / WhatsApp buttons, hours) live in Firestore config/business, so the real number can be set without a new app release. Admin numbers are managed in the Firebase console (config/roles).',
    'کسٹمرز کو دکھائی جانے والی کاروباری تفصیل فائرسٹور میں ہے، اس لیے نیا ایپ ورژن بنائے بغیر اصل نمبر لگ سکتا ہے۔ ایڈمن نمبر فائربیس کنسول سے۔')});
  Z.put(s, Z.fw(Z.col({name: 'Header', pad: [8, Z.G_, 4, Z.G_]}, [Z.t(L('Settings', 'سیٹنگز'), 'Title/L', 'text')])));
  const grp = (title, rows) => Z.fw(Z.col({gap: 6}, [Z.t(title, 'Label/M', 'text-3', {upper: true}), Z.fw(Z.col({r: 16, stroke: 'border', pad: [2, 14], gap: 0}, rows.flatMap((r, i) => i ? [Z.fw(Z.divider(305)), r] : [r])))]));
  const row = (icon, title, o2 = {}) => Z.listRow(Object.assign({icon, ir: 10, isz: 34, title, tstyle: 'Body/L', pad: [9, 0]}, o2));
  Z.put(s, Z.body([
    grp(L('Business', 'کاروبار'), [
      row('phone', L('Phone for calls', 'کال کے لیے نمبر'), {right: Z.t('0300 1234567', 'Label/M', 'text-2', {ltr: true}), chevron: true}),
      row('whatsapp', L('WhatsApp number', 'واٹس ایپ نمبر'), {right: Z.t('+92 300 1234567', 'Label/M', 'text-2', {ltr: true}), chevron: true}),
      row('clock', L('Working hours', 'کام کے اوقات'), {sub: L('Mon – Sat · 10 AM – 8 PM', 'پیر تا ہفتہ · صبح 10 تا رات 8'), chevron: true})]),
    grp(L('Admins', 'ایڈمنز'), [
      row('shield-user', L('Admin numbers', 'ایڈمن نمبر'), {sub: '+92 300 0000001', right: Z.pill(L('1 admin', '1 ایڈمن'), {fill: 'surface-2', color: 'text-2'})}),
      Z.note('info', L('Add or remove admins in Firebase console › Firestore › config/roles.', 'ایڈمن فائربیس کنسول › فائرسٹور › config/roles میں شامل یا ختم کریں۔'), 'grey', {fill: 'bg-2'})]),
    grp(L('App', 'ایپ'), [
      row('languages', L('Language', 'زبان'), {right: Z.t(L('English', 'اردو'), 'Label/M', 'primary'), chevron: true, to: '02'}),
      row('smartphone', L('Switch to customer view', 'کسٹمر ویو پر جائیں'), {chevron: true, to: '10'}),
      row('bell-ring', L('New order alerts', 'نئے آرڈر کی اطلاع'), {right: Z.toggle(true)})]),
    Z.listRow({icon: 'log-out', tone: 'red', ir: 10, isz: 34, title: L('Log out', 'لاگ آؤٹ'), tcol: 'danger', tstyle: 'Title/S', pad: [10, 14], r: 16, stroke: 'border', to: '04'}),
  ], {gap: 14, pad: [6, Z.G_, 10, Z.G_]}));
  Z.put(s, nav('settings'));
  P(s, 6);
}

// ---------- 77 Admin push (lock screen) ----------
{
  const s = Z.screen('77', 'New Order Push', {flow: 'G', light: true, fill: [Z.grad([['#0B3D91', 0], ['#0A66C8', 0.5], ['#1E88E5', 1]], 'down')],
    desc: L('Cloud Function onOrderCreated sends every admin device a high-priority push the moment a customer places an order — even if the app is closed. Tap → Order Detail (72).',
      'جیسے ہی کسٹمر آرڈر دیتا ہے، کلاؤڈ فنکشن ہر ایڈمن فون کو پش بھیجتا ہے — ایپ بند ہو تب بھی۔ ٹیپ → (72)۔')});
  const nt = (title, body, time, to) => Z.fw(Z.row({name: 'notification', r: 22, fill: 'surface', fop: 0.92, pad: [12, 14], gap: 12, cross: 'MIN', to}, [Z.logo(38, {fx: null}),
    Z.fw(Z.col({gap: 2}, [Z.fw(Z.row({gap: 6, cross: 'CENTER'}, [Z.fw(Z.t('Cooling Care', 'Label/M', 'text-2', {ltr: true, align: Z.rtl ? 'RIGHT' : null})), Z.t(time, 'Caption', 'text-3')])), Z.fw(Z.t(title, 'Title/S', 'text', {ltr: true, align: Z.rtl ? 'RIGHT' : null})), Z.fw(Z.t(body, 'Body/S', 'text-2'))]))]));
  Z.put(s, Z.fw(Z.col({gap: 0, cross: 'CENTER', pad: [26, 0, 26, 0], ltr: true}, [Z.t(L('Thursday, 8 October', 'جمعرات، 8 اکتوبر'), 'Title/S', 'on-primary', {op: 0.9}), Z.t('10:05', 'Display/L', 'on-primary', {size: 76, lh: 88, ltr: true})])));
  Z.put(s, Z.body([
    nt('New order CC-1004', L('AC Deep Cleaning · Thu, 08 Oct · 10 AM – 12 PM\nAli Raza · 0300 1234567', 'اے سی ڈیپ کلیننگ · جمعرات، 08 اکتوبر · صبح 10 تا دوپہر 12\nعلی رضا · 0300 1234567'), L('now', 'ابھی'), '72'),
    nt('Order CC-1002 — new time requested', L('Installation & Removal · Sat, 10 Oct · 2 PM – 4 PM\nUsman Tariq · 0333 2468101', 'انسٹالیشن اور ریموول · ہفتہ، 10 اکتوبر · دوپہر 2 تا شام 4\nعثمان طارق · 0333 2468101'), L('8 min', '8 منٹ'), '72'),
  ], {gap: 10, pad: [10, 12, 10, 12]}));
  Z.put(s, Z.homeBar(true));
  P(s, 7);
}
await Z.done();
return out.map(s => s.name);
})
