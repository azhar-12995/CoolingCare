(async (Z) => {
// ---------- Flow A additions: phone login (Firebase Phone Auth) ----------
const L = Z.L, out = [];
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };
const phoneField = (value, o = {}) => Z.fw(Z.col({name: 'field/phone', gap: 6}, [
  Z.t(L('Mobile number', 'موبائل نمبر'), 'Label/M', 'text'),
  Z.fw(Z.row({name: 'input', h: 54, r: 14, fill: 'surface', stroke: o.focus ? 'primary' : 'border', sw: o.focus ? 1.5 : 1, pad: [0, 14, 0, 6], gap: 10, cross: 'CENTER', ltr: true}, [
    Z.row({h: 42, r: 10, fill: 'primary-tint', pad: [0, 12], gap: 6, cross: 'CENTER', ltr: true}, [Z.t('+92', 'Title/S', 'primary', {ltr: true})]),
    Z.fw(Z.t(value, 'Title/M', value.startsWith('3') ? 'text' : 'placeholder', {ltr: true, align: 'LEFT'}))])),
  o.error ? Z.row({gap: 5, cross: 'CENTER'}, [Z.ic('circle-alert', 14, 'danger'), Z.t(o.error, 'Caption', 'danger')]) : Z.t(L('We\'ll send a 6-digit code by SMS to verify it\'s you.', 'تصدیق کے لیے ہم ایس ایم ایس پر 6 ہندسوں کا کوڈ بھیجیں گے۔'), 'Caption', 'text-3')]));

// ---------- 04 Phone Login ----------
{
  const s = Z.screen('04', 'Log in', {flow: 'A', desc: L('Firebase Phone Auth. Pakistani mobiles only (SMS region allow-list = PK). One screen for log-in and sign-up: a new number creates the account. Admin phones (config/roles) land on the Admin Dashboard (70).',
    'فائربیس فون لاگ اِن۔ صرف پاکستانی نمبر۔ لاگ اِن اور سائن اپ ایک ہی سکرین: نیا نمبر اکاؤنٹ بنا دیتا ہے۔ ایڈمن نمبر ایڈمن ڈیش بورڈ (70) پر جاتے ہیں۔')});
  Z.put(s, Z.appBar('', {}));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 16, pad: [8, 0, 6, 0]}, [Z.logo(64), Z.col({gap: 6}, [
      Z.t(L('Log in or sign up', 'لاگ اِن یا سائن اپ'), 'Display/L', 'text'),
      Z.fw(Z.t(L('Enter your mobile number to book visits and track them from any phone.', 'وزٹ بک کرنے اور کسی بھی فون سے دیکھنے کے لیے اپنا موبائل نمبر لکھیں۔'), 'Body/M', 'text-2'))])])),
    phoneField('300 1234567', {focus: true}),
    Z.note('shield-check', L('Your number is only used for your bookings. No spam, ever.', 'آپ کا نمبر صرف بکنگ کے لیے استعمال ہوگا۔ کوئی اسپیم نہیں۔'), 'blue'),
  ], {gap: 18, pad: [0, 24, 8, 24]}));
  Z.put(s, Z.footer([Z.btn(L('Send code', 'کوڈ بھیجیں'), 'Primary', {arrow: true, to: '05'}),
    Z.fw(Z.t(L('By continuing you agree to the Privacy Policy.', 'جاری رکھ کر آپ پرائیویسی پالیسی سے اتفاق کرتے ہیں۔'), 'Caption', 'text-3', {align: 'CENTER', to: '56'}))], {flat: true}));
  P(s, 3);
}

// ---------- 05 Verify OTP ----------
{
  const s = Z.screen('05', 'Verify Code', {flow: 'A', desc: L('6-digit SMS code. Android reads the SMS automatically when it can. Resend after 60 s. Wrong code → shake + error. New users → Your Details (06); returning → Home (10); admin → Admin Dashboard (70).',
    '6 ہندسوں کا کوڈ۔ اینڈرائیڈ خود ایس ایم ایس پڑھ لیتا ہے۔ 60 سیکنڈ بعد دوبارہ بھیجیں۔ نیا صارف → (06)؛ پرانا → ہوم (10)؛ ایڈمن → (70)۔')});
  Z.put(s, Z.appBar('', {}));
  const box = (d, on) => Z.row({name: 'digit', w: 46, h: 56, r: 12, fill: on ? 'primary-tint' : 'surface', stroke: on ? 'primary' : d ? 'text-3' : 'border', sw: on ? 2 : 1, main: 'CENTER', cross: 'CENTER', ltr: true},
    [d ? Z.t(d, 'Title/L', 'text', {ltr: true}) : (on ? Z.rect(2, 24, 'primary', 1) : Z.box(2, 2, {name: 'sp'}))]);
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 6, pad: [8, 0, 6, 0]}, [Z.iconTile('message-square-text', 'blue', 56, 28, {r: 16}), Z.t(L('Verify your number', 'اپنا نمبر تصدیق کریں'), 'Display/L', 'text'),
      Z.fw(Z.row({gap: 6, cross: 'CENTER', wrap: true, wgap: 2}, [Z.t(L('Code sent to', 'کوڈ بھیجا گیا'), 'Body/M', 'text-2'), Z.t('+92 300 1234567', 'Title/S', 'text', {ltr: true}), Z.t(L('Change', 'تبدیل'), 'Label/M', 'primary', {to: '04'})]))])),
    Z.fw(Z.row({gap: 8, main: 'CENTER', ltr: true, name: 'OTP'}, [box('4', false), box('8', false), box('2', false), box('1', false), box('', true), box('', false)])),
    Z.fw(Z.row({gap: 6, main: 'CENTER', cross: 'CENTER'}, [Z.ic('clock', 16, 'text-3'), Z.t(L('Resend code in 0:42', '0:42 میں دوبارہ بھیجیں'), 'Label/M', 'text-3')])),
    Z.note('smartphone', L('On Android the code is filled in automatically when the SMS arrives.', 'اینڈرائیڈ پر ایس ایم ایس آتے ہی کوڈ خود بھر جاتا ہے۔'), 'grey', {fill: 'bg-2'}),
  ], {gap: 22, pad: [0, 24, 8, 24]}));
  Z.put(s, Z.footer([Z.btn(L('Verify & Continue', 'تصدیق کریں'), 'Primary', {arrow: true, to: '06'})], {flat: true}));
  P(s, 4);
}

// ---------- 06 Your Details (new account) ----------
{
  const s = Z.screen('06', 'Your Details', {flow: 'A', desc: L('Only for new accounts. Saved to users/{uid} in Firestore and pre-filled in every booking; editable later in Settings › My Details.',
    'صرف نئے اکاؤنٹ کے لیے۔ فائرسٹور میں محفوظ اور ہر بکنگ میں خود بھر جاتا ہے؛ بعد میں سیٹنگز سے تبدیل کریں۔')});
  Z.put(s, Z.appBar('', {back: false}));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 6, pad: [0, 0, 4, 0]}, [Z.t(L('Almost done!', 'بس تھوڑا سا اور!'), 'Display/L', 'text'),
      Z.fw(Z.t(L('Tell us where to send the technician. You can change this anytime.', 'بتائیں ٹیکنیشن کہاں آئے۔ آپ یہ کسی بھی وقت بدل سکتے ہیں۔'), 'Body/M', 'text-2'))])),
    Z.fw(Z.row({gap: 10, cross: 'CENTER', pad: [10, 12], r: 14, fill: 'green-tint'}, [Z.ic('badge-check', 20, 'green'), Z.fw(Z.t(L('+92 300 1234567 verified', '+92 300 1234567 تصدیق شدہ'), 'Label/M', 'green-text'))])),
    Z.field(L('Full name', 'پورا نام'), L('Ali Raza', 'علی رضا'), {icon: 'user', focus: true}),
    Z.field(L('Area', 'علاقہ'), L('Pasrur City', 'پسرور شہر'), {icon: 'map-pin', trail: 'chevron-down', to: '11'}),
    Z.field(L('House / street', 'مکان / گلی'), L('House No. 12, Main Street', 'مکان نمبر 12، مین اسٹریٹ'), {icon: 'house'}),
    Z.field(L('Landmark', 'نشانی'), L('e.g. near Jamia Masjid', 'مثلاً جامع مسجد کے قریب'), {icon: 'navigation', ph: true, opt: L('(optional)', '(اختیاری)')}),
  ], {gap: 14, pad: [0, 24, 8, 24]}));
  Z.put(s, Z.footer([Z.btn(L('Save & Continue', 'محفوظ کریں اور آگے بڑھیں'), 'Primary', {arrow: true, to: '10'})]));
  P(s, 5);
}
await Z.done();
return out.map(s => s.name);
})
