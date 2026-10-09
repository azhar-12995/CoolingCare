(async (Z) => {
const L = Z.L, out = [], B = Z.bk();
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };
const STEPS = [L('Requested', 'درخواست'), L('Confirmed', 'کنفرم'), L('On the way', 'راستے میں'), L('Completed', 'مکمل')];
const menu = () => Z.iconBtn('ellipsis-vertical', 'Plain', {color: 'text', to: '42'});
const bookingCard = (o = {}) => Z.fw(Z.col({name: 'Booking Card', r: 18, fill: 'surface', stroke: 'border', fx: 'Shadow/Card', pad: [14, 14], gap: 10}, [
  Z.fw(Z.row({gap: 12, cross: 'MIN'}, [
    Z.fw(Z.col({gap: 10}, [
      Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.thumb('cat_split_ac', 54, {w: 64, h: 46, r: 10}), Z.fw(Z.col({gap: 1}, [Z.fw(Z.t(B.svc, 'Title/S', 'text')), Z.t(B.app, 'Body/S', 'text-2')]))])),
      Z.info('calendar', B.date, {style: 'Body/S', pad: [0, 0]}), Z.info('clock', B.slot, {style: 'Body/S', pad: [0, 0]}), Z.info('map-pin', B.addr, {style: 'Body/S', pad: [0, 0]})])),
    o.team ? Z.col({gap: 4, cross: 'CENTER', w: 100}, [Z.img('tech_portrait', 96, 104, 14, {top: 0.2}), Z.t(L('Cooling Care', 'کولنگ کیئر'), 'Label/M', 'text', {align: 'CENTER'}), Z.t(L('Service Technician', 'سروس ٹیکنیشن'), 'Caption', 'text-2', {align: 'CENTER'})]) : null])),
  Z.fw(Z.divider(307)),
  Z.fw(Z.row({gap: 8, cross: 'CENTER'}, [Z.t(L('Booking ID', 'بکنگ آئی ڈی'), 'Caption', 'text-3'), Z.t(B.id, 'Label/M', 'text', {ltr: true}), Z.box(4, 4, {name: 'sp'}), Z.fw(Z.box(4, 4, {name: 'sp'})), Z.ic('copy', 15, 'text-3')]))]));
const actions = (o = {}) => Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Call Us', 'کال کریں'), 'Green', {lead: 'phone', h: 48, to: 'call'})), Z.fw(Z.btn(L('WhatsApp Support', 'واٹس ایپ سپورٹ'), 'GreenOutline', {lead: 'whatsapp', isz: 18, small: true, gap: 6, h: 48, px: 8, to: 'whatsapp'}))]));

// ---------- 40 Request Sent ----------
{
  const s = Z.screen('40', 'Waiting for Confirmation', {flow: 'E', desc: L('Live from Firestore. Cooling Care confirms from the Admin app and the customer gets a push; this screen updates by itself. ⋮ → Manage booking (42).',
    'فائرسٹور سے لائیو۔ کولنگ کیئر ایڈمن ایپ سے کنفرم کرتی ہے اور کسٹمر کو پش ملتا ہے؛ یہ سکرین خود اپڈیٹ ہوتی ہے۔ ⋮ → (42)۔')});
  Z.put(s, Z.appBar(L('Booking Status', 'بکنگ اسٹیٹس'), {center: true, backTo: '10', backIcon: 'x', actions: [menu()]}));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'Banner', pad: [14, 14], r: 16, fill: 'primary-tint', gap: 12, cross: 'CENTER'}, [Z.iconTile('send', 'dark', 40, 20, {fill: 'primary'}),
      Z.fw(Z.col({gap: 2}, [Z.t(L('Waiting for confirmation', 'تصدیق کا انتظار'), 'Title/S', 'primary'), Z.fw(Z.t(L('Cooling Care has your order. You will get a notification as soon as it is confirmed.', 'کولنگ کیئر کو آپ کا آرڈر مل گیا ہے۔ کنفرم ہوتے ہی آپ کو اطلاع ملے گی۔'), 'Body/S', 'text-2'))]))])),
    bookingCard(),
    Z.fw(Z.col({pad: [6, 0, 2, 0]}, [Z.stepper(STEPS, 1)])),
    Z.note('bell-ring', L('Usually confirmed within an hour during working hours.', 'کام کے اوقات میں عموماً ایک گھنٹے میں کنفرم ہو جاتا ہے۔'), 'blue'),
    actions()
  ], {gap: 14}));
  P(s, 30);
}

// ---------- 41 Booking Status · Confirmed (from design) ----------
{
  const s = Z.screen('41', 'Booking Status', {origin: 'image', flow: 'E', desc: L('Confirmed state from the design. The admin moves the stepper (Requested → Confirmed → On the way → Completed) and every step arrives as a push. ⋮ → Manage booking (42). Call Us dials the shop; WhatsApp Support opens a chat with the order number.',
    'ڈیزائن سے کنفرم حالت۔ مراحل: درخواست → کنفرم → راستے میں → مکمل۔ ⋮ → 42۔ کال اور واٹس ایپ بکنگ آئی ڈی کے ساتھ۔')});
  Z.put(s, Z.appBar(L('Booking Status', 'بکنگ اسٹیٹس'), {center: true, actions: [menu()]}));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'Banner', pad: [14, 14], r: 16, fill: 'green-tint', gap: 12, cross: 'CENTER'}, [Z.iconTile('check', 'green', 40, 22, {fill: 'green', color: 'on-primary'}),
      Z.fw(Z.col({gap: 2}, [Z.t(L('Visit Confirmed', 'وزٹ کنفرم ہو گیا'), 'Title/S', 'green-text'), Z.fw(Z.t(L('Your service visit has been confirmed.', 'آپ کا سروس وزٹ کنفرم ہو گیا ہے۔'), 'Body/S', 'text-2'))]))])),
    bookingCard({team: true}),
    Z.fw(Z.col({pad: [6, 0, 2, 0]}, [Z.stepper(STEPS, 2)])),
    actions(),
    Z.note('info', L('Our technician will contact you before arrival.', 'ہمارا ٹیکنیشن آنے سے پہلے آپ سے رابطہ کرے گا۔'), 'blue', {right: Z.t(L('Update', 'اپڈیٹ'), 'Label/M', 'primary', {to: '42'})})
  ], {gap: 14}));
  P(s, 31);
}

// ---------- 43 Service Completed ----------
{
  const s = Z.screen('43', 'Service Completed', {flow: 'E', desc: L('After the admin marks it Completed (push). Stars are saved on the order so the shop sees them. Get Invoice asks the shop on WhatsApp. Maintenance reminder defaults to 90 days and can be switched off.',
    'مکمل ہونے کے بعد۔ ریٹنگ واٹس ایپ پر۔ انوائس واٹس ایپ پر منگوائیں۔ مینٹیننس یاد دہانی 90 دن بعد (بند کی جا سکتی ہے)۔')});
  Z.put(s, Z.appBar(L('Booking Status', 'بکنگ اسٹیٹس'), {center: true, backTo: '50'}));
  const star = on => Z.ic('star', 30, on ? '#F5A524' : 'border', {solid: on});
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 8, cross: 'CENTER', pad: [6, 0, 0, 0]}, [
      Z.row({w: 76, h: 76, r: 38, fill: 'green-tint', main: 'CENTER', cross: 'CENTER'}, [Z.row({w: 54, h: 54, r: 27, fill: 'green', main: 'CENTER', cross: 'CENTER'}, [Z.ic('check', 30, 'on-primary', {sw: 3})])]),
      Z.t(L('Service Completed', 'سروس مکمل ہو گئی'), 'Title/L', 'text', {align: 'CENTER'}),
      Z.t(B.svc + ' · ' + B.date, 'Body/S', 'text-2', {align: 'CENTER'})])),
    Z.fw(Z.col({pad: [4, 0, 0, 0]}, [Z.stepper(STEPS, 4, {color: 'green'})])),
    Z.fw(Z.col({name: 'Rating', r: 16, stroke: 'border', pad: [14, 16], gap: 10, cross: 'CENTER'}, [Z.t(L('How was your service?', 'آپ کی سروس کیسی رہی؟'), 'Title/S', 'text'),
      Z.row({gap: 8, ltr: true}, [star(true), star(true), star(true), star(true), star(false)]),
      Z.t(L('Thanks! Your rating was sent to Cooling Care.', 'شکریہ! آپ کی ریٹنگ کولنگ کیئر کو بھیج دی گئی۔'), 'Caption', 'green-text')])),
    Z.fw(Z.row({name: 'Reminder', r: 16, fill: 'primary-tint', pad: [12, 14], gap: 12, cross: 'CENTER'}, [Z.iconTile('bell-ring', 'blue', 40, 20, {fill: 'surface', r: 12}),
      Z.fw(Z.col({gap: 1}, [Z.t(L('Next service reminder', 'اگلی سروس کی یاد دہانی'), 'Title/S', 'text'), Z.fw(Z.t(L("We'll remind you on 06 Jan 2027 (in 3 months).", 'ہم آپ کو 06 جنوری 2027 کو یاد دلائیں گے (3 ماہ بعد)۔'), 'Body/S', 'text-2'))])), Z.toggle(true)]))
  ], {gap: 14}));
  Z.put(s, Z.footer([Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Get Invoice', 'انوائس منگوائیں'), 'Secondary', {lead: 'receipt', to: 'whatsapp'})), Z.fw(Z.btn(L('Book Again', 'دوبارہ بک کریں'), 'Primary', {to: '30'}))]))]));
  P(s, 33);
}

await Z.done();
return out.map(s => s.name);
})
