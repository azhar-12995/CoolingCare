(async (Z) => {
const L = Z.L, out = [];
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };

// ---------- 01 Splash ----------
{
  const s = Z.screen('01', 'Splash', {light: true, flow: 'A', fill: [Z.G.splash()],
    desc: L('Cold start (~1.2 s). First launch → Choose Language (02). Returning user → Home (10).', 'ایپ کھلتے ہی (~1.2 سیکنڈ)۔ پہلی بار → زبان (02)۔ دوبارہ → ہوم (10)۔')});
  Z.link(s, 'auto:02');
  for (const [x, y, sz, op] of [[-70, 520, 280, 0.07], [250, 70, 190, 0.07], [270, 640, 120, 0.09]]) { const l = Z.ic('snowflake', sz, 'on-primary', {ltr: true, sw: 1.2}); l.opacity = op; Z.put(s, Z.abs(l, x, y, {ltr: true})); }
  Z.put(s, Z.fwh(Z.col({name: 'Body', gap: 16, cross: 'CENTER', main: 'CENTER', pad: [0, 24]}, [
    Z.logo(104, {fill: 'surface', fg: 'primary', fx: 'Shadow/Float', sw: 1.8}),
    Z.col({gap: 4, cross: 'CENTER'}, [Z.t('Cooling Care', 'Logo/XL', 'on-primary', {align: 'CENTER', ltr: true}),
      Z.t(L('AC & Refrigeration Services', 'اے سی اور ریفریجریشن سروسز'), 'Body/L', 'on-primary', {align: 'CENTER', op: 0.88})])
  ])));
  Z.put(s, Z.fw(Z.col({name: 'Footer', pad: [0, 24, 10, 24], gap: 14, cross: 'CENTER'}, [
    Z.row({gap: 7, cross: 'CENTER', name: 'loader', ltr: true}, [Z.dot(8, 'on-primary'), Z.dot(8, 'on-primary', {op: 0.6}), Z.dot(8, 'on-primary', {op: 0.3})]),
    Z.t(L('Serving Pasrur & nearby areas', 'پسرور اور قریبی علاقوں میں سروس'), 'Caption', 'on-primary', {op: 0.8, align: 'CENTER'}), Z.homeBar(true)])));
  P(s, 0);
}

// ---------- 02 Choose Language ----------
{
  const en = Z.lang === 'en';
  const s = Z.screen('02', 'Choose Language', {flow: 'A', desc: L('Shown once on first launch (also in Settings). Switches every string and the layout direction (Urdu = right-to-left). Continue → Welcome (03).',
    'پہلی بار ایک دفعہ (سیٹنگز میں بھی)۔ تمام الفاظ اور سمت بدل جاتی ہے (اردو = دائیں سے بائیں)۔ جاری رکھیں → خوش آمدید (03)۔')});
  const opt = (badge, title, sub, sel, ur) => Z.fw(Z.row({name: 'option/' + title, pad: [16, 16], gap: 14, r: 18, cross: 'CENTER', fill: sel ? 'primary-tint' : 'surface', stroke: sel ? 'primary' : 'border', sw: sel ? 2 : 1, fx: sel ? null : 'Shadow/Card', to: '03'}, [
    Z.row({w: 52, h: 52, r: 14, fill: sel ? 'primary' : 'surface-2', main: 'CENTER', cross: 'CENTER'}, [Z.t(badge, ur ? 'Title/L' : 'Title/L', sel ? 'on-primary' : 'text', {align: 'CENTER'})]),
    Z.fw(Z.col({gap: 2}, [Z.fw(Z.t(title, 'Title/M', 'text')), Z.fw(Z.t(sub, 'Body/S', 'text-2'))])), Z.radio(sel)]));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 18, cross: 'CENTER', pad: [36, 0, 8, 0]}, [Z.logo(72), Z.col({gap: 6, cross: 'CENTER'}, [
      Z.t('Choose your language', 'Title/L', 'text', {align: 'CENTER'}), Z.t('اپنی زبان منتخب کریں', 'Title/M', 'text-2', {align: 'CENTER'})])])),
    opt('Aa', 'English', 'Continue in English', en, false),
    opt('اُ', 'اردو', 'اردو میں جاری رکھیں', !en, true),
    Z.note('languages', L('You can change the language anytime in Settings.', 'آپ کسی بھی وقت سیٹنگز سے زبان بدل سکتے ہیں۔'), 'grey', {fill: 'bg-2'})
  ], {gap: 16}));
  Z.put(s, Z.footer([Z.btn(L('Continue', 'جاری رکھیں'), 'Primary', {arrow: true, to: '03'})], {flat: true}));
  P(s, 1);
}

// ---------- 03 Welcome ----------
{
  const s = Z.screen('03', 'Welcome', {flow: 'A', noStatus: true, desc: L('Brand intro built from the client poster: four promises + service area. Get Started → Log in (04).',
    'کلائنٹ پوسٹر سے برانڈ تعارف: چار وعدے + سروس ایریا۔ شروع کریں → ہوم (10)۔ کوئی اکاؤنٹ یا لاگ اِن نہیں۔')});
  const hero = Z.box(375, 380, {name: 'Hero', clip: true});
  const im = Z.img('welcome_scene', 375, 380, 0, {top: 0.05}); hero.appendChild(im);
  const fade = Z.rect(375, 380, Z.grad([['#FFFFFF', 0, 0], ['#FFFFFF', 0.62, 0], ['#FFFFFF', 1, 1]], 'down'), 0, {name: 'fade'}); hero.appendChild(fade);
  const sb = Z.statusBar(false).n; hero.appendChild(sb); sb.primaryAxisSizingMode = 'FIXED'; sb.resize(375, 44); sb.x = 0; sb.y = 0;
  Z.put(s, hero);
  const feat = (icon, text) => Z.fw(Z.row({gap: 14, cross: 'CENTER'}, [Z.iconTile(icon, 'blue', 40, 22, {r: 12}), Z.fw(Z.t(text, 'Body/L', 'text'))]));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 6}, [Z.t('Cooling Care', 'Display/L', 'navy', {ltr: true, align: Z.rtl ? 'RIGHT' : null}), Z.fw(Z.t(L('AC & Refrigeration Services', 'اے سی اور ریفریجریشن سروسز'), 'Title/M', 'text-2')), Z.rect(44, 4, 'primary-bright', 2, {name: 'accent'})])),
    feat('snowflake', L('Residential & Commercial', 'گھریلو اور کمرشل')),
    feat('shield-check', L('Professional Technicians', 'پروفیشنل ٹیکنیشنز')),
    feat('settings', L('Reliable & On-Time Service', 'قابلِ اعتماد اور بروقت سروس')),
    feat('map-pin', L('Serving Pasrur and Nearby Areas', 'پسرور اور قریبی علاقوں میں سروس'))
  ], {gap: 14, pad: [0, 24, 8, 24]}));
  Z.put(s, Z.footer([Z.btn(L('Get Started', 'شروع کریں'), 'Primary', {arrow: true, to: '04'})], {flat: true}));
  P(s, 2);
}

// ---------- 10 Home (from design) ----------
{
  const s = Z.screen('10', 'Home', {origin: 'image', flow: 'B', desc: L('Hub. Area pill → Select Area (11); bell → Notifications (12); Book a Service → Book a Visit (30); tiles → Services filtered by appliance (20/21/22); Call Now dials the shop; WhatsApp opens a chat.',
    'مرکزی سکرین۔ علاقہ → (11)؛ گھنٹی → اطلاعات (12)؛ سروس بک کریں → (30)؛ ٹائلز → سروسز (20/21/22)؛ کال اور واٹس ایپ براہِ راست۔')});
  Z.put(s, Z.fw(Z.row({name: 'Top Bar', h: 50, pad: [0, 14, 0, Z.G_], cross: 'CENTER', main: 'SPACE_BETWEEN'}, [
    Z.row({name: 'Area', gap: 6, cross: 'CENTER', to: '11', pad: [6, 0]}, [Z.ic('map-pin', 19, 'primary'), Z.t(L('Pasrur', 'پسرور'), 'Title/S', 'text'), Z.ic('chevron-down', 17, 'text-2')]),
    Z.iconBtn('bell', 'Plain', {badge: true, to: '12'})])));
  const hero = Z.box(335, 238, {name: 'Hero Card', r: 20, fill: 'primary-tint', clip: true, fx: 'Shadow/Card'});
  hero.fills = [Z.G.hero()];
  // image sits on the trailing side; a fade on its inner edge melts it into the card behind the text
  const img = Z.img('hero_technician', 184, 188, 0, {left: 1, top: 0.1});
  const fadeR = Z.rect(70, 188, Z.grad([['#E5F1FD', 0, 1], ['#E5F1FD', 1, 0]], Z.rtl ? 'left' : 'right'), 0, {name: 'fade'});
  const title = Z.t(L('Expert cooling\ncare at your\ndoorstep', 'ماہر کولنگ سروس\nآپ کی دہلیز پر'), 'Display/M', 'navy', {w: 168});
  const sub = Z.t(L('Professional AC & refrigeration service in Pasrur.', 'پسرور میں پروفیشنل اے سی اور ریفریجریشن سروس۔'), 'Body/S', 'text-2', {w: 150});
  const book = Z.btn(L('Book a Service', 'سروس بک کریں'), 'Primary', {arrow: true, h: 46, to: '30'}); book.resize(303, 46); book.layoutSizingHorizontal = 'FIXED';
  for (const [n, x, y] of [[img, 151, 0], [fadeR, 151, 0]]) { hero.appendChild(n); n.x = Z.rtl ? 335 - x - n.width : x; n.y = y; }
  hero.appendChild(title); hero.appendChild(sub); hero.appendChild(book);
  await Z.done();
  title.x = Z.rtl ? 335 - 18 - title.width : 18; title.y = 18; sub.x = Z.rtl ? 335 - 18 - sub.width : 18; sub.y = title.y + title.height + 8; book.x = 16; book.y = 238 - 46 - 14;
  const tile = (key, label, to) => Z.col({name: 'tile/' + label, w: 105, h: 88, r: 14, fill: 'tile', pad: [7, 4, 7, 4], gap: 2, cross: 'CENTER', main: 'SPACE_BETWEEN', to}, [
    Z.img(key, 90, 48, 0, {fit: true}), Z.t(label, 'Label/S', 'text', {align: 'CENTER', w: 97, trunc: 2})]);
  const contact = (icon, title, sub, to) => Z.fw(Z.row({name: 'contact/' + title, pad: [10, 12], gap: 10, r: 16, fill: 'green-tint', stroke: '#BFE3CC', cross: 'CENTER', to}, [
    Z.iconTile(icon, 'green', 38, 20, {fill: 'green', color: 'on-primary'}), Z.fw(Z.col({gap: 0}, [Z.t(title, 'Title/S', 'green-text'), Z.t(sub, 'Caption', 'text-2')]))]));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: Z.rtl ? 0 : 3}, [Z.t(L('Good morning!', 'صبح بخیر!'), 'Title/L', 'text'), Z.fw(Z.t(L('Keep your space cool and comfortable all year round.', 'سارا سال اپنے گھر اور دفتر کو ٹھنڈا اور آرام دہ رکھیں۔'), 'Body/S', 'text-2'))])),
    hero,
    Z.sec(L('Our Services', 'ہماری سروسز'), L('See all', 'سب دیکھیں'), {to: '20'}),
    Z.fw(Z.col({gap: 10, name: 'Category Grid'}, [
      Z.row({gap: 10}, [tile('cat_split_ac', L('Split AC', 'اسپلٹ اے سی'), '20'), tile('cat_refrigerator', L('Refrigerator', 'فریج'), '21'), tile('cat_cassette', L('Ceiling Cassette', 'سیلنگ کیسٹ'), '22')]),
      Z.row({gap: 10}, [tile('cat_vrf', L('VRF / VRV', 'وی آر ایف / وی آر وی'), '22'), tile('cat_commercial', L('Commercial Cooling', 'کمرشل کولنگ'), '22'), tile('cat_installation', L('Installation', 'انسٹالیشن'), '20')])])),
    Z.fw(Z.row({gap: 11}, [contact('phone', L('Call Now', 'ابھی کال کریں'), '0300 1234567', 'call'), contact('whatsapp', L('WhatsApp', 'واٹس ایپ'), L('Chat with us', 'ہم سے بات کریں'), 'whatsapp')]))
  ], {gap: Z.rtl ? 10 : 14, pad: [2, Z.G_, 10, Z.G_]}));
  Z.put(s, Z.nav('home'));
  P(s, 3);
}

// ---------- 11 Select Area (sheet) ----------
{
  const s = Z.screen('11', 'Select Area', {flow: 'B', noStatus: true, desc: L('Bottom sheet from the area pill. Area is saved and pre-filled in the booking address. List comes from app config so the client can edit it.',
    'علاقہ منتخب کرنے کی شیٹ۔ محفوظ ہو کر بکنگ ایڈریس میں خود آ جاتا ہے۔ فہرست ایپ کنفیگ سے آتی ہے۔')});
  const areas = [[L('Pasrur City', 'پسرور شہر'), true], [L('Chawinda', 'چونڈہ')], [L('Qila Kalar Wala', 'قلعہ کالر والا')], [L('Badiana', 'بڈیانہ')], [L('Sialkot Road', 'سیالکوٹ روڈ')], [L('Narowal Road', 'نارووال روڈ')], [L('Other nearby area', 'دیگر قریبی علاقہ')]];
  const sh = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.fw(Z.col({gap: 2}, [Z.t(L('Select your area', 'اپنا علاقہ منتخب کریں'), 'Title/M', 'text'), Z.fw(Z.t(L('We serve Pasrur and nearby areas.', 'ہم پسرور اور قریبی علاقوں میں سروس دیتے ہیں۔'), 'Body/S', 'text-2'))])), Z.iconBtn('x', 'Grey', {size: 34, isz: 18, to: 'back'})])),
    Z.field(null, L('Search area', 'علاقہ تلاش کریں'), {icon: 'search', ph: true}),
    Z.fw(Z.col({gap: 0, name: 'Areas'}, areas.map(([a, on], i) => Z.fw(Z.col({gap: 0}, [Z.listRow({lead: Z.iconTile(i === areas.length - 1 ? 'map-pinned' : 'map-pin', on ? 'blue' : 'grey', 34, 17, {r: 10}), title: a, tstyle: on ? 'Title/S' : 'Body/L', right: Z.radio(!!on), pad: [9, 0], to: 'back'}),
      i < areas.length - 1 ? Z.fw(Z.divider()) : null]))))),
    Z.note('phone', L('Area not listed? Call us — we may still be able to come.', 'آپ کا علاقہ فہرست میں نہیں؟ کال کریں، شاید ہم پھر بھی آ سکیں۔'), 'blue'),
    Z.fw(Z.btn(L('Confirm Area', 'علاقہ کنفرم کریں'), 'Primary', {to: '10'}))
  ], {gap: 12});
  Z.overlay(s, '10', sh);
  await Z.done(); Z.dock(sh);
  P(s, 4);
}

// ---------- 12 Notifications ----------
{
  const s = Z.screen('12', 'Notifications', {flow: 'B', desc: L('Order updates pushed by the shop (confirmed, on the way, completed, cancelled) plus on-device reminders: day before, 1 h before, maintenance after 90 days. Tap → related booking.',
    'صرف فون پر مقامی اطلاعات — کوئی سرور نہیں۔ وزٹ سے ایک دن اور ایک گھنٹہ پہلے یاد دہانی، 90 دن بعد مینٹیننس یاد دہانی۔')});
  Z.put(s, Z.appBar(L('Notifications', 'اطلاعات'), {actions: [Z.t(L('Mark all read', 'سب پڑھ لیا'), 'Label/M', 'primary')]}));
  const item = (icon, tone, title, sub, time, unread, to) => Z.fw(Z.row({name: 'notif/' + title, pad: [14, 14], gap: 12, r: 16, fill: unread ? 'primary-tint' : 'surface', stroke: unread ? null : 'border', cross: 'MIN', to}, [
    Z.iconTile(icon, tone, 42, 21, {r: 12}),
    Z.fw(Z.col({gap: 3}, [Z.fw(Z.row({gap: 8, cross: 'CENTER'}, [Z.fw(Z.t(title, 'Title/S', 'text')), unread ? Z.dot(8, 'primary') : null])), Z.fw(Z.t(sub, 'Body/S', 'text-2')), Z.t(time, 'Caption', 'text-3')]))]));
  Z.put(s, Z.body([
    Z.t(L('Today', 'آج'), 'Label/M', 'text-3', {upper: true}),
    item('calendar-clock', 'blue', L('Visit tomorrow at 10:00 AM', 'کل صبح 10 بجے وزٹ ہے'), L('AC Deep Cleaning · Thu, 08 Oct 2026. Please keep the AC area accessible.', 'اے سی ڈیپ کلیننگ · جمعرات 08 اکتوبر 2026۔ براہِ کرم اے سی تک رسائی آسان رکھیں۔'), L('2 h ago', '2 گھنٹے پہلے'), true, '41'),
    item('calendar-check', 'green', L('Visit confirmed ✓ · CC-1004', 'وزٹ کنفرم ہو گیا ✓ · CC-1004'), L('Cooling Care confirmed AC Deep Cleaning for Thu, 08 Oct, 10 AM – 12 PM.', 'کولنگ کیئر نے جمعرات 08 اکتوبر صبح 10 تا 12 اے سی ڈیپ کلیننگ کنفرم کر دی۔'), L('5 h ago', '5 گھنٹے پہلے'), true, '41'),
    Z.t(L('Earlier', 'پہلے'), 'Label/M', 'text-3', {upper: true}),
    item('snowflake', 'amber', L('Time for your AC service', 'اے سی سروس کا وقت ہو گیا'), L('It has been 3 months since your last AC General Service.', 'آپ کی پچھلی اے سی سروس کو 3 ماہ ہو گئے ہیں۔'), L('12 Sep', '12 ستمبر'), false, '30'),
    item('circle-check', 'green', L('Service completed', 'سروس مکمل ہو گئی'), L('AC General Service · Split AC. Thank you for choosing Cooling Care!', 'اے سی جنرل سروس · اسپلٹ اے سی۔ کولنگ کیئر کا شکریہ!'), L('12 Jun', '12 جون'), false, '43')
  ], {gap: 12}));
  P(s, 5);
}

await Z.done();
return out.map(s => s.name);
})
