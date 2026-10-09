(async (Z) => {
const L = Z.L, out = [], B = Z.bk();
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };
const CAT = Z.CAT || await (await fetch('http://localhost:9230/cc/catalog.json?' + Date.now())).json();
const tr = o => o ? L(o.en, o.ur) : '';

// shared form builder (o.err = show validation state)
const form = (o = {}) => {
  const half = (k) => k;
  const photos = Z.fw(Z.col({name: 'field/photos', gap: 6}, [
    Z.row({gap: 4, cross: 'CENTER'}, [Z.t(L('Add Photos', 'تصاویر شامل کریں'), 'Label/M', 'text'), Z.t(L('(optional)', '(اختیاری)'), 'Caption', 'text-3')]),
    Z.fw(Z.row({name: 'Add Photos', h: 58, r: 12, fill: 'bg-2', stroke: 'primary', sop: 0.45, dash: [6, 4], sw: 1.2, gap: 12, main: 'CENTER', cross: 'CENTER', to: '33'}, [
      Z.iconTile('camera', 'blue', 36, 19, {r: 10}),
      Z.col({gap: 0}, [Z.t(L('Add Photos', 'تصاویر شامل کریں'), 'Title/S', 'primary'), Z.t(L('Helps us understand the issue better', 'مسئلہ سمجھنے میں مدد ملتی ہے'), 'Caption', 'text-3')])]))]));
  return [
    Z.field(L('Select Appliance', 'اپلائنس منتخب کریں'), B.app, {trail: 'chevron-down', to: '31'}),
    Z.field(L('Service Type', 'سروس کی قسم'), B.svc, {trail: 'chevron-down', to: '31'}),
    Z.field(L('Describe the issue', 'مسئلہ بیان کریں'), o.err ? B.issue : L('e.g. not cooling well, unusual noise, or any other details...', 'مثلاً ٹھنڈک کم ہے، عجیب آواز، یا کوئی اور تفصیل...'), {opt: L('(optional)', '(اختیاری)'), ph: !o.err, multi: true, h: 62}),
    photos,
    Z.fw(Z.row({gap: 12, cross: 'MIN'}, [
      half(Z.field(L('Preferred Date', 'پسندیدہ تاریخ'), o.err ? L('Select date', 'تاریخ منتخب کریں') : B.dateShort, {icon: 'calendar', ph: !!o.err, to: '32', error: o.err ? L('Choose a date', 'تاریخ منتخب کریں') : null})),
      half(Z.field(L('Time Slot', 'وقت'), o.err ? L('Select time', 'وقت منتخب کریں') : B.slot, {icon: 'clock', ph: !!o.err, to: '32'}))])),
    Z.fw(Z.row({gap: 12, cross: 'MIN'}, [
      half(Z.field(L('Your Name', 'آپ کا نام'), o.err ? L('Full name', 'پورا نام') : B.name, {icon: 'user', ph: !!o.err, error: o.err ? L('Enter your name', 'اپنا نام لکھیں') : null})),
      half(Z.field(L('Contact Number', 'رابطہ نمبر'), o.err ? '0300 12345' : B.phone, {icon: 'phone', focus: !!o.err, error: o.err ? L('Use 03XX XXXXXXX', '03XX XXXXXXX لکھیں') : null}))])),
    Z.field(L('Service Address', 'سروس کا پتہ'), B.addr, {icon: 'map-pin', trail: 'pencil', trailColor: 'primary'})
  ];
};

// ---------- 30 Book a Visit (from design) ----------
{
  const s = Z.screen('30', 'Book a Visit', {origin: 'image', flow: 'D', h: Z.rtl ? 880 : 812, desc: L('Booking form. Appliance + service come pre-filled from Service Details. Name, phone and address are remembered on the phone after the first booking. Request Booking → Review & Send (35).',
    'بکنگ فارم۔ سروس کی تفصیل سے اپلائنس اور سروس پہلے سے بھرے ہوتے ہیں۔ نام، فون اور پتہ پہلی بکنگ کے بعد فون میں محفوظ۔ بکنگ کی درخواست → جائزہ (35)۔')});
  Z.put(s, Z.appBar(L('Book a Service Visit', 'سروس وزٹ بک کریں'), {center: true}));
  Z.put(s, Z.body(form(), {gap: 11, pad: [4, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.footer([Z.btn(L('Request Booking', 'بکنگ کی درخواست'), 'Primary', {arrow: true, to: '35'})]));
  P(s, 20);
}

// ---------- 31 Appliance & Service (sheet) ----------
{
  const s = Z.screen('31', 'Select Appliance & Service', {flow: 'D', noStatus: true, desc: L('One sheet for both dropdowns. Appliance changes the service list (AC → 5 AC services, Refrigerator → 2, Commercial → 3).',
    'دونوں ڈراپ ڈاؤن کے لیے ایک شیٹ۔ اپلائنس بدلنے سے سروس کی فہرست بدلتی ہے۔')});
  const ap = CAT.appliances.slice(0, 6);
  const tile = (a, on) => Z.col({name: 'appliance/' + a.en, w: 103, h: 82, r: 14, fill: on ? 'primary-tint' : 'tile', stroke: on ? 'primary' : null, sw: 2, pad: [6, 4, 6, 4], gap: 2, cross: 'CENTER', main: 'SPACE_BETWEEN'}, [
    Z.img(a.img, 84, 44, 0, {fit: true}), Z.t(tr(a), 'Label/S', on ? 'primary' : 'text', {align: 'CENTER', w: 95, trunc: 1})]);
  const svcs = CAT.services.filter(x => x.category === 'ac');
  const sh = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.fw(Z.t(L('Appliance & service', 'اپلائنس اور سروس'), 'Title/M', 'text')), Z.iconBtn('x', 'Grey', {size: 34, isz: 18, to: 'back'})])),
    Z.fw(Z.col({gap: 8}, [Z.row({gap: 8}, ap.slice(0, 3).map((a, i) => tile(a, i === 0))), Z.row({gap: 8}, ap.slice(3).map(a => tile(a, false)))])),
    Z.t(L('Service type', 'سروس کی قسم'), 'Label/M', 'text-2'),
    Z.fw(Z.col({gap: 0}, svcs.map((sv, i) => Z.fw(Z.col({gap: 0}, [Z.listRow({title: tr(sv.title), sub: tr(sv.duration), tstyle: sv.id === 'ac_deep' ? 'Title/S' : 'Body/L', right: Z.radio(sv.id === 'ac_deep'), pad: [8, 0], to: '30'}), i < svcs.length - 1 ? Z.fw(Z.divider()) : null]))))),
    Z.fw(Z.btn(L('Done', 'ہو گیا'), 'Primary', {to: '30'}))
  ], {gap: 12});
  Z.overlay(s, '30', sh); await Z.done(); Z.dock(sh);
  P(s, 21);
}

// ---------- 32 Date & Time (sheet) ----------
{
  const s = Z.screen('32', 'Choose Date & Time', {flow: 'D', noStatus: true, desc: L('Next 14 days. Sundays are greyed (emergency calls only). Past slots of today are disabled. This is the preferred time — the team confirms the final time on WhatsApp. Also used for Reschedule.',
    'اگلے 14 دن۔ اتوار غیر فعال (صرف ایمرجنسی)۔ آج کے گزرے ہوئے وقت غیر فعال۔ یہ پسندیدہ وقت ہے — حتمی وقت ٹیم واٹس ایپ پر کنفرم کرے گی۔')});
  const days = [[L('Sat', 'ہفتہ'), '03', L('Today', 'آج')], [L('Sun', 'اتوار'), '04', null, true], [L('Mon', 'پیر'), '05'], [L('Tue', 'منگل'), '06'], [L('Wed', 'بدھ'), '07'], [L('Thu', 'جمعرات'), '08', null, false, true]];
  const day = ([d, n, tag, off, on]) => Z.col({name: 'day/' + n, w: 50, h: 76, r: 14, fill: on ? 'primary' : off ? 'surface-2' : 'surface', stroke: on || off ? null : 'border', fx: on ? 'Shadow/Button' : null, gap: 2, main: 'CENTER', cross: 'CENTER'}, [
    Z.t(d, 'Caption', on ? 'on-primary' : off ? 'placeholder' : 'text-2', {align: 'CENTER'}), Z.t(n, 'Title/M', on ? 'on-primary' : off ? 'placeholder' : 'text', {ltr: true}), tag ? Z.t(tag, 'Label/S', on ? 'on-primary' : 'primary') : Z.box(4, 4, {name: 'sp'})]);
  const slot = (t, on, off) => Z.fw(Z.row({name: 'slot/' + t.en, h: 46, r: 12, fill: on ? 'primary-tint' : off ? 'surface-2' : 'surface', stroke: on ? 'primary' : off ? null : 'border', sw: on ? 2 : 1, main: 'CENTER', cross: 'CENTER', gap: 6}, [
    on ? Z.ic('circle-check', 16, 'primary') : null, Z.t(tr(t), 'Label/M', on ? 'primary' : off ? 'placeholder' : 'text')]));
  const sl = CAT.timeSlots;
  const sh = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.fw(Z.t(L('Choose date & time', 'تاریخ اور وقت منتخب کریں'), 'Title/M', 'text')), Z.iconBtn('x', 'Grey', {size: 34, isz: 18, to: 'back'})])),
    Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('October 2026', 'اکتوبر 2026'), 'Label/M', 'text-2')), Z.ic('chevron-left', 18, 'text-3'), Z.box(10, 10, {name: 'sp'}), Z.ic('chevron-right', 18, 'primary')])),
    Z.fw(Z.row({gap: 7, name: 'Days'}, days.map(day))),
    Z.t(L('Time slot', 'وقت'), 'Label/M', 'text-2'),
    Z.fw(Z.col({gap: 10}, [Z.fw(Z.row({gap: 10}, [slot(sl[0], true), slot(sl[1])])), Z.fw(Z.row({gap: 10}, [slot(sl[2]), slot(sl[3])])), Z.fw(Z.row({gap: 10}, [slot(sl[4]), Z.fw(Z.box(10, 46, {name: 'sp'}))]))])),
    Z.note('info', L('This is your preferred time. Our team will confirm the final time on WhatsApp.', 'یہ آپ کا پسندیدہ وقت ہے۔ حتمی وقت ہماری ٹیم واٹس ایپ پر کنفرم کرے گی۔'), 'blue'),
    Z.fw(Z.btn(L('Confirm', 'کنفرم کریں'), 'Primary', {to: '30'}))
  ], {gap: 12});
  Z.overlay(s, '30', sh); await Z.done(); Z.dock(sh);
  P(s, 22);
}

// ---------- 33 Add Photos (sheet) ----------
{
  const s = Z.screen('33', 'Add Photos', {flow: 'D', noStatus: true, desc: L('Camera or gallery, up to 3 photos, compressed to ~1 MB. Android: attached to the WhatsApp message automatically. iPhone: shared right after the text via the share sheet.',
    'کیمرہ یا گیلری، زیادہ سے زیادہ 3 تصاویر۔ اینڈرائیڈ پر خود بخود واٹس ایپ پیغام کے ساتھ؛ آئی فون پر شیئر شیٹ سے۔')});
  const ph = (key) => { const b = Z.box(96, 96, {r: 14, name: 'photo', clip: false}); const im = Z.img(key, 96, 96, 14); b.appendChild(im);
    const x = Z.row({w: 24, h: 24, r: 12, fill: '#0B1F45', main: 'CENTER', cross: 'CENTER', name: 'remove'}, [Z.ic('x', 14, 'on-primary', {sw: 2.6})]); b.appendChild(x); x.x = Z.rtl ? -6 : 78; x.y = -6; return b; };
  const sh = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.fw(Z.t(L('Add photos', 'تصاویر شامل کریں'), 'Title/M', 'text')), Z.iconBtn('x', 'Grey', {size: 34, isz: 18, to: 'back'})])),
    Z.fw(Z.row({gap: 12}, [
      Z.fw(Z.col({h: 92, r: 16, fill: 'primary-tint', gap: 8, main: 'CENTER', cross: 'CENTER', to: '30'}, [Z.ic('camera', 26, 'primary'), Z.t(L('Take photo', 'تصویر کھینچیں'), 'Title/S', 'primary')])),
      Z.fw(Z.col({h: 92, r: 16, fill: 'primary-tint', gap: 8, main: 'CENTER', cross: 'CENTER', to: '30'}, [Z.ic('images', 26, 'primary'), Z.t(L('From gallery', 'گیلری سے'), 'Title/S', 'primary')]))])),
    Z.t(L('Selected (2 of 3)', 'منتخب (3 میں سے 2)'), 'Label/M', 'text-2'),
    Z.row({gap: 14, pad: [6, 6, 0, 0], name: 'Selected'}, [ph('svc_gas'), ph('detail_deep_clean'),
      Z.col({w: 96, h: 96, r: 14, stroke: 'border', dash: [5, 4], sw: 1.5, main: 'CENTER', cross: 'CENTER', gap: 4}, [Z.ic('image-plus', 24, 'text-3'), Z.t(L('Add', 'شامل'), 'Caption', 'text-3')])]),
    Z.note('whatsapp', L('Photos are shared with your request on WhatsApp. They are not uploaded anywhere else.', 'تصاویر صرف واٹس ایپ پر آپ کی درخواست کے ساتھ جاتی ہیں، کہیں اور اپ لوڈ نہیں ہوتیں۔'), 'green'),
    Z.fw(Z.btn(L('Done', 'ہو گیا'), 'Primary', {to: '30'}))
  ], {gap: 14});
  Z.overlay(s, '30', sh); await Z.done(); Z.dock(sh);
  P(s, 23);
}

// ---------- 34 Form errors ----------
{
  const s = Z.screen('34', 'Book a Visit · Errors', {flow: 'D', h: Z.rtl ? 880 : 812, desc: L('Validation on Request Booking: date required, name required, Pakistani mobile 03XXXXXXXXX (11 digits) or +92. The first invalid field is focused and the page scrolls to it.',
    'تصدیق: تاریخ، نام لازمی؛ موبائل 03XXXXXXXXX (11 ہندسے) یا +92۔ پہلی غلط فیلڈ پر فوکس۔')});
  Z.put(s, Z.appBar(L('Book a Service Visit', 'سروس وزٹ بک کریں'), {center: true}));
  const f = form({err: true});
  Z.put(s, Z.body([Z.note('circle-alert', L('Please fix 3 fields to continue.', 'آگے بڑھنے کے لیے 3 فیلڈز درست کریں۔'), 'red', {pad: [9, 12], color: 'danger'}), f[0], f[2], f[4], f[5], f[6]], {gap: 11, pad: [4, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.footer([Z.btn(L('Request Booking', 'بکنگ کی درخواست'), 'Primary', {arrow: true, to: '35'})]));
  P(s, 24);
}

// ---------- 35 Review & Place Order ----------
{
  const s = Z.screen('35', 'Review & Place Order', {flow: 'D', h: Z.rtl ? 900 : 812, desc: L('Last check. Place Order saves the order in Firestore (number CC-1001, CC-1002 … from a transaction), uploads photos to Storage and notifies every admin by push. Needs internet → otherwise 37.',
    'آخری جائزہ۔ آرڈر دیں سے آرڈر فائرسٹور میں محفوظ، تصاویر اسٹوریج میں اور ہر ایڈمن کو پش۔ انٹرنیٹ ضروری → ورنہ 37۔')});
  Z.put(s, Z.appBar(L('Review & Confirm', 'جائزہ لیں اور کنفرم کریں'), {center: true}));
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 12, cross: 'CENTER', pad: [12, 12], r: 16, fill: 'surface', stroke: 'border', fx: 'Shadow/Card'}, [Z.thumb('svc_deep', 60), Z.fw(Z.col({gap: 2}, [Z.t(B.svc, 'Title/S', 'text'), Z.t(B.app, 'Body/S', 'text-2')])), Z.t(L('Edit', 'ترمیم'), 'Label/M', 'primary', {to: '30'})])),
    Z.fw(Z.col({gap: 0, pad: [4, 14], r: 16, stroke: 'border'}, [
      Z.info('calendar', B.date + '  ·  ' + B.slot, {pad: [9, 0]}), Z.fw(Z.divider()), Z.info('map-pin', B.addr, {pad: [9, 0]}), Z.fw(Z.divider()),
      Z.info('user', B.name + '  ·  ' + B.phone, {pad: [9, 0]}), Z.fw(Z.divider()), Z.info('message-square-text', B.issue, {pad: [9, 0]}), Z.fw(Z.divider()),
      Z.info('camera', L('2 photos attached', '2 تصاویر منسلک'), {pad: [9, 0]})])),
    Z.fw(Z.col({gap: 10, pad: [12, 14], r: 16, fill: 'primary-tint'}, [Z.t(L('What happens next', 'آگے کیا ہوگا'), 'Title/S', 'text'),
      Z.info('bell-ring', L('Cooling Care gets your order instantly.', 'کولنگ کیئر کو آرڈر فوراً مل جاتا ہے۔'), {style: 'Body/S', icol: 'primary', pad: [0, 0]}),
      Z.info('calendar-check', L('You get a notification when your visit is confirmed.', 'وزٹ کنفرم ہونے پر آپ کو اطلاع ملتی ہے۔'), {style: 'Body/S', icol: 'primary', pad: [0, 0]}),
      Z.info('tag', L('Price is quoted after inspection — nothing to pay now.', 'قیمت معائنے کے بعد — ابھی کچھ ادا نہیں کرنا۔'), {style: 'Body/S', icol: 'primary', pad: [0, 0]})])),
  ], {gap: 12, pad: [4, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.footer([Z.btn(L('Place Order', 'آرڈر دیں'), 'Primary', {lead: 'check', to: '36'})]));
  P(s, 25);
}

// ---------- 36 Order Placed ----------
{
  const s = Z.screen('36', 'Order Placed', {flow: 'D', desc: L('Success. The order number is the one the shop sees. Track → Booking Status (40). Notification permission is asked here (first order only).',
    'کامیابی۔ یہی آرڈر نمبر دکان بھی دیکھتی ہے۔ ٹریک → (40)۔ اطلاع کی اجازت یہیں مانگی جاتی ہے۔')});
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 14, cross: 'CENTER', pad: [80, 8, 0, 8]}, [
      Z.row({w: 120, h: 120, r: 60, fill: 'green-tint', main: 'CENTER', cross: 'CENTER'}, [Z.row({w: 84, h: 84, r: 42, fill: 'green', main: 'CENTER', cross: 'CENTER'}, [Z.ic('check', 46, 'on-primary', {sw: 3})])]),
      Z.t(L('Order placed!', 'آرڈر ہو گیا!'), 'Display/L', 'text', {align: 'CENTER'}),
      Z.t(L('Order number', 'آرڈر نمبر'), 'Body/S', 'text-2', {align: 'CENTER'}),
      Z.pill('CC-1004', {fill: 'primary-tint', color: 'primary', style: 'Title/M', pad: [6, 16]}),
      Z.t(L("Cooling Care has received your order. We'll notify you as soon as your visit is confirmed.", 'کولنگ کیئر کو آپ کا آرڈر مل گیا ہے۔ وزٹ کنفرم ہوتے ہی ہم آپ کو اطلاع دیں گے۔'), 'Body/M', 'text-2', {align: 'CENTER', w: 300}),
    ])),
    Z.note('bell-ring', L('Allow notifications so you don\'t miss the confirmation.', 'اطلاعات کی اجازت دیں تاکہ تصدیق نظر سے نہ رہ جائے۔'), 'amber'),
  ], {gap: 20, pad: [0, 24, 8, 24]}));
  Z.put(s, Z.footer([Z.btn(L('Track my order', 'آرڈر دیکھیں'), 'Primary', {arrow: true, to: '40'}), Z.fw(Z.row({main: 'CENTER'}, [Z.t(L('Back to Home', 'ہوم پر جائیں'), 'Label/M', 'primary', {to: '10'})]))], {flat: true}));
  P(s, 26);
}

// ---------- 37 No connection ----------
{
  const s = Z.screen('37', 'No Connection', {flow: 'D', noStatus: true, desc: L('Placing an order needs internet (order number + photo upload). The form is kept; Try again retries; Call reaches the shop directly.',
    'آرڈر دینے کے لیے انٹرنیٹ ضروری ہے۔ فارم محفوظ رہتا ہے؛ دوبارہ کوشش کریں یا دکان کو کال کریں۔')});
  const dlg = Z.col({name: 'Dialog', w: 327, fill: 'surface', r: 24, pad: [24, 20, 20, 20], gap: 12, cross: 'CENTER', fx: 'Shadow/Float'}, [
    Z.iconTile('wifi', 'grey', 64, 32, {r: 20}),
    Z.t(L('No internet connection', 'انٹرنیٹ کنکشن نہیں'), 'Title/M', 'text', {align: 'CENTER'}),
    Z.t(L("We couldn't send your order. Your details are saved — try again when you're online, or call us.", 'آپ کا آرڈر نہیں جا سکا۔ آپ کی تفصیلات محفوظ ہیں — آن لائن ہو کر دوبارہ کوشش کریں یا ہمیں کال کریں۔'), 'Body/M', 'text-2', {align: 'CENTER', w: 287}),
    Z.fw(Z.btn(L('Try again', 'دوبارہ کوشش کریں'), 'Primary', {lead: 'refresh-cw', to: '36'})),
    Z.fw(Z.btn(L('Call 0300 1234567', 'کال کریں 0300 1234567'), 'Light', {lead: 'phone', leadColor: 'green', to: 'back'}))]);
  Z.overlay(s, '35', dlg); await Z.done(); Z.dock(dlg, 'center');
  P(s, 27);
}
await Z.done();
return out.map(s => s.name);
})
