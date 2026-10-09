(async (Z) => {
const L = Z.L, out = [], B = Z.bk();
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };
const CAT = Z.CAT || await (await fetch('http://localhost:9230/cc/catalog.json?' + Date.now())).json();
const tr = o => o ? L(o.en, o.ur) : '';
const TABS = [L('My Bookings', 'میری بکنگز'), L('Support', 'مدد')];
const head = () => Z.fw(Z.col({name: 'Header', pad: [6, Z.G_, 6, Z.G_], gap: 12}, [Z.t(L('Support & My Bookings', 'مدد اور میری بکنگز'), 'Title/L', 'text')]));
const urgent = () => Z.fw(Z.col({name: 'Urgent Help', r: 16, fill: 'orange-tint', stroke: '#F8D2B4', pad: [12, 14], gap: 10}, [
  Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.ic('triangle-alert', 20, 'orange'), Z.fw(Z.col({gap: 0}, [Z.t(L('Need urgent help?', 'فوری مدد چاہیے؟'), 'Title/S', 'orange-text'), Z.fw(Z.t(L('Call or WhatsApp us for immediate assistance.', 'فوری مدد کے لیے کال یا واٹس ایپ کریں۔'), 'Body/S', 'text-2'))]))])),
  Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Call Now', 'ابھی کال کریں'), 'Orange', {lead: 'phone', sub: '0300 1234567', h: 52, px: 12, to: 'call'})), Z.fw(Z.btn(L('WhatsApp', 'واٹس ایپ'), 'Green', {lead: 'whatsapp', sub: L('Chat with us', 'ہم سے بات کریں'), h: 52, px: 12, to: 'whatsapp'}))]))]));

// ---------- 50 My Bookings (from design) ----------
{
  const s = Z.screen('50', 'My Bookings', {origin: 'image', flow: 'F', h: Z.rtl ? 900 : 812, desc: L('Bookings tab. Upcoming card → Booking Status (41). Completed: Get Invoice (WhatsApp) / Book Again (30). Maintenance reminder appears 90 days after the last completed service.',
    'بکنگز ٹیب۔ آنے والی بکنگ → (41)۔ مکمل: انوائس / دوبارہ بک۔ آخری سروس کے 90 دن بعد مینٹیننس یاد دہانی۔')});
  Z.put(s, head());
  Z.put(s, Z.fw(Z.col({pad: [0, Z.G_, 4, Z.G_]}, [Z.tabs(TABS, 0, {to: ['50', '52']})])));
  Z.put(s, Z.body([
    Z.t(L('Upcoming Appointment', 'آنے والا وزٹ'), 'Title/S', 'text'),
    Z.fw(Z.row({name: 'Upcoming', r: 16, fill: 'surface', stroke: 'border', fx: 'Shadow/Card', pad: [12, 12], gap: 12, cross: 'CENTER', to: '41'}, [
      Z.thumb('cat_split_ac', 60, {w: 60, h: 60, r: 12}),
      Z.fw(Z.col({gap: 3}, [Z.fw(Z.t(B.svc, 'Title/S', 'text')), Z.fw(Z.row({gap: 8, cross: 'CENTER'}, [Z.t(B.app, 'Body/S', 'text-2'), Z.badge(L('Confirmed', 'کنفرم'))])),
        Z.fw(Z.row({gap: 6, cross: 'CENTER'}, [Z.ic('calendar', 14, 'text-2'), Z.fw(Z.t(B.date + '  |  ' + B.slot, 'Caption', 'text', {trunc: 1}))])), Z.fw(Z.row({gap: 6, cross: 'CENTER'}, [Z.ic('map-pin', 14, 'text-2'), Z.fw(Z.t(B.addr, 'Caption', 'text', {trunc: 1}))]))])),
      Z.ic('chevron-right', 18, 'text-3')])),
    Z.t(L('Completed Services', 'مکمل سروسز'), 'Title/S', 'text'),
    Z.fw(Z.row({name: 'Completed', r: 16, fill: 'surface', stroke: 'border', fx: 'Shadow/Card', pad: [12, 12], gap: 12, cross: 'CENTER'}, [
      Z.thumb('svc_gas', 60, {w: 60, h: 66, r: 12}),
      Z.fw(Z.col({gap: 3}, [Z.t(B.past, 'Title/S', 'text'), Z.t(B.app, 'Body/S', 'text-2'), Z.info('calendar', B.pastDate, {style: 'Caption', pad: [0, 0]}),
        Z.fw(Z.row({gap: 8, pad: [4, 0, 0, 0]}, [Z.fw(Z.btn(L('Get Invoice', 'انوائس'), 'Secondary', {small: true, h: 34, px: 8, to: 'whatsapp'})), Z.fw(Z.btn(L('Book Again', 'دوبارہ بک'), 'Primary', {small: true, h: 34, px: 8, flat: true, to: '30'}))]))]))])),
    urgent(),
    Z.fw(Z.row({name: 'Maintenance Reminder', r: 16, fill: 'primary-tint', pad: [12, 14], gap: 12, cross: 'CENTER'}, [Z.iconTile('calendar-clock', 'blue', 40, 20, {fill: 'surface', r: 12}),
      Z.fw(Z.col({gap: 1}, [Z.fw(Z.t(L("It's time for your next AC service", 'اگلی اے سی سروس کا وقت ہو گیا'), 'Title/S', 'text')), Z.fw(Z.t(L('Regular maintenance keeps your AC efficient.', 'باقاعدہ مینٹیننس سے اے سی بہتر چلتا ہے۔'), 'Caption', 'text-2'))])),
      Z.btn(L('Book Now', 'ابھی بک کریں'), 'Secondary', {small: true, h: 34, px: 12, to: '30'})]))
  ], {gap: 10, pad: [8, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.nav('bookings'));
  P(s, 40);
}

// ---------- 51 My Bookings · Empty ----------
{
  const s = Z.screen('51', 'My Bookings · Empty', {flow: 'F', desc: L('First-time state before any booking.', 'پہلی بکنگ سے پہلے کی حالت۔')});
  Z.put(s, head());
  Z.put(s, Z.fw(Z.col({pad: [0, Z.G_, 4, Z.G_]}, [Z.tabs(TABS, 0, {to: ['50', '52']})])));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 12, cross: 'CENTER', pad: [70, 10, 0, 10]}, [
      Z.row({w: 132, h: 132, r: 66, fill: 'tile', main: 'CENTER', cross: 'CENTER'}, [Z.img('cat_split_ac', 104, 68, 0, {fit: true})]),
      Z.t(L('No bookings yet', 'ابھی کوئی بکنگ نہیں'), 'Title/L', 'text', {align: 'CENTER'}),
      Z.t(L('Book your first visit — it takes less than a minute.', 'اپنا پہلا وزٹ بک کریں — ایک منٹ سے بھی کم وقت لگتا ہے۔'), 'Body/M', 'text-2', {align: 'CENTER', w: 280}),
      Z.btn(L('Book a Service', 'سروس بک کریں'), 'Primary', {arrow: true, to: '30'})])),
  ], {main: 'MIN'}));
  Z.put(s, Z.nav('bookings'));
  P(s, 41);
}

// ---------- 52 Support ----------
{
  const s = Z.screen('52', 'Support', {flow: 'F', h: Z.rtl ? 880 : 812, desc: L('Support tab: call / WhatsApp, working hours, service area, FAQs (from catalog.json), Settings.', 'مدد ٹیب: کال/واٹس ایپ، اوقات، سروس ایریا، سوالات، سیٹنگز۔')});
  Z.put(s, head());
  Z.put(s, Z.fw(Z.col({pad: [0, Z.G_, 4, Z.G_]}, [Z.tabs(TABS, 1, {to: ['50', '52']})])));
  const faq = (f, open) => Z.fw(Z.col({name: 'faq', gap: 6, pad: [11, 0]}, [Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.fw(Z.t(tr(f.q), 'Title/S', 'text')), Z.ic(open ? 'chevron-up' : 'chevron-down', 18, 'text-3')])), open ? Z.fw(Z.t(tr(f.a), 'Body/S', 'text-2')) : null]));
  Z.put(s, Z.body([
    urgent(),
    Z.fw(Z.col({r: 16, stroke: 'border', pad: [4, 14], gap: 0}, [
      Z.listRow({icon: 'clock', ir: 10, isz: 36, title: L('Working hours', 'کام کے اوقات'), sub: L('Mon – Sat · 10:00 AM – 8:00 PM\nSunday: emergency calls only', 'پیر تا ہفتہ · صبح 10 تا رات 8 بجے\nاتوار: صرف ایمرجنسی کالز'), pad: [10, 0]}), Z.fw(Z.divider(305)),
      Z.listRow({icon: 'map-pin', ir: 10, isz: 36, title: L('Service area', 'سروس ایریا'), sub: L('Pasrur and nearby areas', 'پسرور اور قریبی علاقے'), right: Z.t(L('Change', 'تبدیل'), 'Label/M', 'primary'), pad: [10, 0], to: '11'})])),
    Z.t(L('Frequently asked questions', 'عام سوالات'), 'Title/S', 'text'),
    Z.fw(Z.col({gap: 0}, CAT.faqs.slice(0, 2).map((f, i) => Z.fw(Z.col({gap: 0}, [faq(f, i === 0), Z.fw(Z.divider())]))))),
    Z.listRow({icon: 'settings', ir: 10, isz: 36, title: L('Settings', 'سیٹنگز'), sub: L('Language, my details, reminders', 'زبان، میری تفصیلات، یاد دہانیاں'), chevron: true, to: '53', fill: 'bg-2', r: 14, pad: [10, 12]})
  ], {gap: 10, pad: [8, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.nav('support'));
  P(s, 42);
}

// ---------- 53 Settings ----------
{
  const s = Z.screen('53', 'Settings', {flow: 'F', desc: L('Account settings. Log out keeps the data in Firebase; Delete account removes the profile and the login (required by Google Play / App Store); past orders stay with the shop for its records.',
    'سب کچھ صرف اسی فون میں۔ زبان بدلنے سے سمت بدلتی ہے۔ میرا ڈیٹا حذف کریں سب کچھ مٹا دیتا ہے۔')});
  Z.put(s, Z.appBar(L('Settings', 'سیٹنگز'), {center: true}));
  const grp = (title, rows) => Z.fw(Z.col({gap: 6}, [Z.t(title, 'Label/M', 'text-3', {upper: true}), Z.fw(Z.col({r: 16, stroke: 'border', pad: [2, 14], gap: 0}, rows.flatMap((r, i) => i ? [Z.fw(Z.divider(305)), r] : [r])))]));
  const row = (icon, title, o = {}) => Z.listRow(Object.assign({icon, ir: 10, isz: 34, title, tstyle: 'Body/L', pad: [9, 0]}, o));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'Profile', r: 16, fill: 'primary-tint', pad: [14, 14], gap: 12, cross: 'CENTER', to: '54'}, [
      Z.row({w: 50, h: 50, r: 25, fill: 'primary', main: 'CENTER', cross: 'CENTER'}, [Z.t(L('AR', 'ع ر'), 'Title/M', 'on-primary')]),
      Z.fw(Z.col({gap: 1}, [Z.t(B.name, 'Title/S', 'text'), Z.t(B.phone, 'Body/S', 'text-2'), Z.t(B.addr, 'Caption', 'text-3')])), Z.ic('pencil', 18, 'primary')])),
    grp(L('Preferences', 'ترجیحات'), [
      row('languages', L('Language', 'زبان'), {right: Z.t(L('English', 'اردو'), 'Label/M', 'primary'), chevron: true, to: '02'}),
      row('calendar-clock', L('Visit reminders', 'وزٹ کی یاد دہانی'), {right: Z.toggle(true)}),
      row('bell-ring', L('Maintenance reminders', 'مینٹیننس یاد دہانی'), {right: Z.toggle(true)})]),
    grp(L('About', 'متعلق'), [
      row('info', L('About Cooling Care', 'کولنگ کیئر کے بارے میں'), {chevron: true, to: '55'}),
      row('shield-check', L('Privacy policy', 'پرائیویسی پالیسی'), {chevron: true, to: '56'}),
      row('share-2', L('Share the app', 'ایپ شیئر کریں'), {chevron: true}),
      row('star', L('Rate the app', 'ایپ کو ریٹ کریں'), {chevron: true})]),
    Z.fw(Z.col({gap: 0, r: 16, stroke: 'border', pad: [2, 14]}, [
      Z.listRow({icon: 'log-out', tone: 'grey', ir: 10, isz: 34, title: L('Log out', 'لاگ آؤٹ'), tstyle: 'Title/S', pad: [10, 0], to: '04'}), Z.fw(Z.divider(305)),
      Z.listRow({icon: 'user-x', tone: 'red', ir: 10, isz: 34, title: L('Delete account', 'اکاؤنٹ حذف کریں'), tcol: 'danger', tstyle: 'Title/S', pad: [10, 0]})])),
    Z.fw(Z.t(L('Version 1.0.0', 'ورژن 1.0.0'), 'Caption', 'text-3', {align: 'CENTER'}))
  ], {gap: 14, pad: [6, Z.G_, 8, Z.G_]}));
  P(s, 43);
}

// ---------- 54 Edit My Details ----------
{
  const s = Z.screen('54', 'My Details', {flow: 'F', desc: L('Saved in Firestore (users/{uid}) and pre-filled in every booking. The login number is verified and cannot be edited here; a different contact number can still be typed per booking.', 'فائرسٹور میں محفوظ اور ہر بکنگ میں خود بھرتا ہے۔ لاگ اِن نمبر تصدیق شدہ ہے۔')});
  Z.put(s, Z.appBar(L('My Details', 'میری تفصیلات'), {center: true}));
  Z.put(s, Z.body([
    Z.note('lock', L('Only you and Cooling Care can see these details.', 'یہ تفصیلات صرف آپ اور کولنگ کیئر دیکھ سکتے ہیں۔'), 'blue'),
    Z.field(L('Full name', 'پورا نام'), B.name, {icon: 'user'}),
    Z.field(L('Mobile number', 'موبائل نمبر'), '+92 300 1234567', {icon: 'phone', fill: 'bg-2', trail: Z.pill(L('Verified', 'تصدیق شدہ'), {fill: 'green-tint', color: 'green-text', icon: 'badge-check', pad: [3, 8]}), help: L('This is your login number.', 'یہ آپ کا لاگ اِن نمبر ہے۔')}),
    Z.field(L('Area', 'علاقہ'), L('Pasrur City', 'پسرور شہر'), {icon: 'map-pin', trail: 'chevron-down', to: '11'}),
    Z.field(L('House / street', 'مکان / گلی'), L('House No. 12, Main Street', 'مکان نمبر 12، مین اسٹریٹ'), {icon: 'house'}),
    Z.field(L('Landmark', 'نشانی'), L('e.g. near Jamia Masjid', 'مثلاً جامع مسجد کے قریب'), {icon: 'navigation', ph: true, opt: L('(optional)', '(اختیاری)')})
  ], {gap: 14}));
  Z.put(s, Z.footer([Z.btn(L('Save', 'محفوظ کریں'), 'Primary', {to: '53'})]));
  P(s, 44);
}

// ---------- 55 About ----------
{
  const s = Z.screen('55', 'About Cooling Care', {flow: 'F', desc: L('Brand page: promises from the poster + "how it works" strip from the poster footer.', 'برانڈ صفحہ: پوسٹر کے وعدے اور طریقہ کار۔')});
  Z.put(s, Z.appBar(L('About', 'متعلق'), {center: true}));
  const step = (icon, n, t) => Z.fw(Z.col({gap: 6, cross: 'CENTER'}, [Z.row({w: 44, h: 44, r: 22, fill: 'primary-tint', main: 'CENTER', cross: 'CENTER'}, [Z.ic(icon, 21, 'primary')]), Z.t(n, 'Caption', 'text-3'), Z.t(t, 'Label/S', 'text', {align: 'CENTER', w: 74})]));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 10, cross: 'CENTER', pad: [8, 0, 4, 0]}, [Z.logo(72), Z.wordmark({center: true, style: 'Title/L'})])),
    Z.fw(Z.t(L('Cooling Care keeps homes, shops and offices in Pasrur cool all year — from a quick AC service to full VRF and commercial maintenance.', 'کولنگ کیئر پسرور کے گھروں، دکانوں اور دفاتر کو سارا سال ٹھنڈا رکھتی ہے — فوری اے سی سروس سے لے کر وی آر ایف اور کمرشل مینٹیننس تک۔'), 'Body/M', 'text-2', {align: 'CENTER'})),
    Z.fw(Z.col({r: 16, fill: 'bg-2', pad: [6, 14], gap: 0}, [
      Z.info('snowflake', L('Residential & Commercial', 'گھریلو اور کمرشل'), {icol: 'primary', pad: [8, 0]}), Z.info('shield-check', L('Professional Technicians', 'پروفیشنل ٹیکنیشنز'), {icol: 'primary', pad: [8, 0]}),
      Z.info('settings', L('Reliable & On-Time Service', 'قابلِ اعتماد اور بروقت سروس'), {icol: 'primary', pad: [8, 0]}), Z.info('map-pin', L('Serving Pasrur and Nearby Areas', 'پسرور اور قریبی علاقوں میں سروس'), {icol: 'primary', pad: [8, 0]})])),
    Z.t(L('How it works', 'طریقہ کار'), 'Title/S', 'text'),
    Z.fw(Z.row({gap: 4, cross: 'MIN'}, [step('search', '1', L('Browse services', 'سروس دیکھیں')), step('calendar-days', '2', L('Request visit', 'وزٹ کی درخواست')), step('circle-check', '3', L('Get confirmation', 'تصدیق حاصل کریں')), step('receipt', '4', L('Service & invoice', 'سروس اور انوائس'))])),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Call', 'کال'), 'Light', {lead: 'phone', leadColor: 'green', h: 46, to: 'call'})), Z.fw(Z.btn(L('WhatsApp', 'واٹس ایپ'), 'Light', {lead: 'whatsapp', leadColor: 'green', h: 46, to: 'whatsapp'}))]))
  ], {gap: 14}));
  P(s, 45);
}

// ---------- 56 Privacy ----------
{
  const s = Z.screen('56', 'Privacy Policy', {flow: 'F', desc: L('Plain-language policy. Also hosted as a web page for the Play Store / App Store listing.', 'آسان زبان میں پالیسی۔ پلے اسٹور / ایپ اسٹور کے لیے ویب صفحہ بھی۔')});
  Z.put(s, Z.appBar(L('Privacy Policy', 'پرائیویسی پالیسی'), {center: true}));
  const sec = (icon, t, b) => Z.fw(Z.row({gap: 12, cross: 'MIN'}, [Z.iconTile(icon, 'blue', 36, 18, {r: 10}), Z.fw(Z.col({gap: 3}, [Z.fw(Z.t(t, 'Title/S', 'text')), Z.fw(Z.t(b, 'Body/S', 'text-2'))]))]));
  Z.put(s, Z.body([
    Z.t(L('Last updated: 03 Oct 2026', 'آخری تجدید: 03 اکتوبر 2026'), 'Caption', 'text-3'),
    sec('smartphone', L('What we store', 'ہم کیا محفوظ کرتے ہیں'), L('Your phone number (for log-in), name, address and orders are stored securely in Google Firebase so Cooling Care can serve you.', 'آپ کا فون نمبر (لاگ اِن کے لیے)، نام، پتہ اور آرڈرز گوگل فائربیس میں محفوظ ہوتے ہیں تاکہ کولنگ کیئر آپ کی خدمت کر سکے۔')),
    sec('shield-check', L('Who can see it', 'کون دیکھ سکتا ہے'), L('Only you and the Cooling Care team. We never sell or share your data.', 'صرف آپ اور کولنگ کیئر کی ٹیم۔ ہم آپ کا ڈیٹا کبھی فروخت یا شیئر نہیں کرتے۔')),
    sec('camera', L('Camera & photos', 'کیمرہ اور تصاویر'), L('Used only to attach photos of the problem. Nothing is uploaded by this app.', 'صرف مسئلے کی تصاویر لگانے کے لیے۔ یہ ایپ کچھ اپ لوڈ نہیں کرتی۔')),
    sec('bell', L('Reminders', 'یاد دہانیاں'), L('Visit and maintenance reminders are scheduled on your phone. Turn them off in Settings.', 'وزٹ اور مینٹیننس یاد دہانیاں فون پر شیڈول ہوتی ہیں۔ سیٹنگز سے بند کریں۔')),
    sec('user-x', L('Delete your account', 'اکاؤنٹ حذف کریں'), L('Settings › Delete account removes your profile and login. Order records are kept by the shop for accounting.', 'سیٹنگز › اکاؤنٹ حذف کریں آپ کا پروفائل اور لاگ اِن مٹا دیتا ہے۔ آرڈر ریکارڈ دکان حساب کے لیے رکھتی ہے۔'))
  ], {gap: 16}));
  P(s, 46);
}

// ---------- 61 Lock-screen reminders (system) ----------
{
  const s = Z.screen('61', 'Reminder Notifications', {flow: 'F', light: true, fill: [Z.grad([['#0B3D91', 0], ['#0A66C8', 0.5], ['#1E88E5', 1]], 'down')],
    desc: L('Push from Cloud Functions when the shop changes the order (Firebase Cloud Messaging) plus on-device visit / maintenance reminders. Both arrive with the app closed. Tap → the related booking.', 'دکان کی ہر تبدیلی پر پش اور فون پر یاد دہانیاں؛ ایپ بند ہو تب بھی۔ ٹیپ → متعلقہ بکنگ۔')});
  const nt = (title, body, time, to) => Z.fw(Z.row({name: 'notification', r: 22, fill: 'surface', fop: 0.92, pad: [12, 14], gap: 12, cross: 'MIN', to}, [Z.logo(38, {fx: null}),
    Z.fw(Z.col({gap: 2}, [Z.fw(Z.row({gap: 6, cross: 'CENTER'}, [Z.fw(Z.t('Cooling Care', 'Label/M', 'text-2', {ltr: true, align: Z.rtl ? 'RIGHT' : null})), Z.t(time, 'Caption', 'text-3')])), Z.fw(Z.t(title, 'Title/S', 'text')), Z.fw(Z.t(body, 'Body/S', 'text-2'))]))]));
  Z.put(s, Z.fw(Z.col({gap: 0, cross: 'CENTER', pad: [26, 0, 26, 0], ltr: true}, [Z.t(L('Thursday, 8 October', 'جمعرات، 8 اکتوبر'), 'Title/S', 'on-primary', {op: 0.9}), Z.t('9:41', 'Display/L', 'on-primary', {size: 76, lh: 88, ltr: true})])));
  Z.put(s, Z.body([
    nt(L('Visit confirmed ✓ · CC-1004', 'وزٹ کنفرم ہو گیا ✓ · CC-1004'), L('AC Deep Cleaning · Thu, 08 Oct · 10 AM – 12 PM', 'اے سی ڈیپ کلیننگ · جمعرات، 08 اکتوبر · صبح 10 تا دوپہر 12'), L('now', 'ابھی'), '41'),
    nt(L('Visit today at 10 AM', 'آج صبح 10 بجے وزٹ'), L('AC Deep Cleaning — our technician will call before arrival.', 'اے سی ڈیپ کلیننگ — ٹیکنیشن آنے سے پہلے کال کرے گا۔'), L('9:00', '9:00'), '41'),
    nt(L('Time for your AC service', 'اے سی سروس کا وقت'), L("It's been 3 months since your last service. Tap to book.", 'پچھلی سروس کو 3 ماہ ہو گئے۔ بک کرنے کے لیے ٹیپ کریں۔'), L('Jan 6', '6 جنوری'), '30')
  ], {gap: 10, pad: [10, 12, 10, 12]}));
  Z.put(s, Z.homeBar(true));
  P(s, 47);
}
await Z.done();
return out.map(s => s.name);
})
