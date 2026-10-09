(async (Z) => {
// Customer-side booking management now that the shop updates the status from the Admin app.
const L = Z.L, out = [], B = Z.bk();
const P = (s, n) => { s.x = (n % 10) * 455; s.y = Math.floor(n / 10) * 980; Z.S[s.getPluginData('screen')] = s; out.push(s); return s; };

// ---------- 42 Manage Booking (sheet) ----------
{
  const s = Z.screen('42', 'Manage Booking', {flow: 'E', noStatus: true, desc: L('Customers can ask for a different time (goes back to "Waiting for confirmation" and notifies the shop) or cancel before the technician leaves. Both are blocked by the rules once the visit is on the way.',
    'کسٹمر دوسرا وقت مانگ سکتا ہے (دکان کو اطلاع جاتی ہے) یا ٹیکنیشن کے نکلنے سے پہلے منسوخ کر سکتا ہے۔')});
  const row = (icon, tone, title, sub, to, o = {}) => Z.listRow({icon, tone, ir: 12, isz: 40, title, sub, chevron: true, to, pad: [12, 12], r: 14, stroke: 'border', tcol: o.tcol});
  const sh = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.fw(Z.t(L('Manage booking', 'بکنگ کا انتظام') + ' ' + B.id, 'Title/M', 'text')), Z.iconBtn('x', 'Grey', {size: 34, isz: 18, to: 'back'})])),
    row('calendar-sync', 'blue', L('Change date or time', 'تاریخ یا وقت بدلیں'), L('Cooling Care confirms the new time', 'نیا وقت کولنگ کیئر کنفرم کرے گی'), '32'),
    row('whatsapp', 'green', L('Message Cooling Care', 'کولنگ کیئر کو پیغام'), L('WhatsApp with your order number', 'آرڈر نمبر کے ساتھ واٹس ایپ'), 'whatsapp'),
    row('ban', 'red', L('Cancel booking', 'بکنگ منسوخ کریں'), L('Allowed until the technician leaves', 'ٹیکنیشن کے نکلنے تک ممکن'), '44', {tcol: 'danger'}),
  ], {gap: 10});
  Z.overlay(s, '41', sh); await Z.done(); Z.dock(sh);
  P(s, 32);
}

// ---------- 44 Cancel Booking (dialog) ----------
{
  const s = Z.screen('44', 'Cancel Booking', {flow: 'E', noStatus: true, desc: L('Saves status "cancelled" with the reason; the Cloud Function pushes it to every admin and the local reminders are removed.',
    'وجہ کے ساتھ "منسوخ" محفوظ؛ کلاؤڈ فنکشن ہر ایڈمن کو پش بھیجتا ہے اور یاد دہانیاں ختم۔')});
  const reasons = [L('Not needed anymore', 'اب ضرورت نہیں'), L('Want a different time', 'کوئی اور وقت چاہیے'), L('Issue fixed itself', 'مسئلہ خود ٹھیک ہو گیا'), L('Other', 'دیگر')];
  const dlg = Z.col({name: 'Dialog', w: 327, fill: 'surface', r: 24, pad: [22, 20, 18, 20], gap: 12, cross: 'CENTER', fx: 'Shadow/Float'}, [
    Z.iconTile('calendar-x', 'red', 60, 28, {r: 18}),
    Z.t(L('Cancel this booking?', 'یہ بکنگ منسوخ کریں؟'), 'Title/M', 'text', {align: 'CENTER'}),
    Z.t(L('Cooling Care will be notified right away.', 'کولنگ کیئر کو فوراً اطلاع ہو جائے گی۔'), 'Body/M', 'text-2', {align: 'CENTER', w: 287}),
    Z.fw(Z.col({gap: 0, r: 14, stroke: 'border', pad: [2, 12]}, reasons.map((r, i) => Z.fw(Z.col({gap: 0}, [Z.listRow({title: r, tstyle: i === 0 ? 'Title/S' : 'Body/M', right: Z.radio(i === 0), pad: [8, 0]}), i < 3 ? Z.fw(Z.divider(263)) : null]))))),
    Z.fw(Z.btn(L('Cancel Booking', 'بکنگ منسوخ کریں'), 'Danger', {to: '50'})),
    Z.fw(Z.btn(L('Keep Booking', 'بکنگ برقرار رکھیں'), 'Light', {to: 'back'})),
    Z.t(L('Change the time instead', 'اس کے بجائے وقت تبدیل کریں'), 'Label/M', 'primary', {to: '32'})]);
  Z.overlay(s, '41', dlg); await Z.done(); Z.dock(dlg, 'center');
  P(s, 34);
}
await Z.done();
return out.map(s => s.name);
})
