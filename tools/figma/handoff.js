(async (Z) => {
const page = figma.root.children.find(p => p.name === 'Handoff');
for (const n of [...page.children]) n.remove();
const W = 1040;
const card = (title, sub, kids, w = W) => Z.col({name: title, w, pad: 44, gap: 20, fill: 'surface', r: 28, stroke: 'border'}, [
  Z.col({gap: 6}, [Z.t(title, 'Board/H2', 'text'), sub ? Z.t(sub, 'Board/Body', 'text-2', {w: w - 88}) : null]), ...kids]);
const mono = (txt, w = W - 88) => Z.fw(Z.col({pad: [18, 22], r: 16, fill: '#0B1F45', gap: 0}, [Z.t(txt, 'Body/M', '#DDE9FA', {w: w - 44, lh: 22})]));
const table = (head, rows, widths) => Z.fw(Z.col({r: 16, stroke: 'border', gap: 0, clip: true}, [
  Z.fw(Z.row({fill: 'primary-tint', pad: [12, 16], gap: 16}, head.map((h, i) => Z.t(h, 'Label/M', 'primary', {w: widths[i]})))),
  ...rows.map((r, j) => Z.fw(Z.row({pad: [12, 16], gap: 16, fill: j % 2 ? 'bg-2' : 'surface', cross: 'MIN'}, r.map((c, i) => Z.t(c, i === 0 ? 'Title/S' : 'Body/M', i === 0 ? 'text' : 'text-2', {w: widths[i]})))))]));
const box = (icon, t, s, tone = 'blue') => Z.col({w: 210, pad: 18, gap: 10, r: 18, fill: Z.TONES[tone][0], cross: 'CENTER'}, [Z.iconTile(icon, tone, 48, 24, {fill: 'surface', r: 14}), Z.t(t, 'Title/S', 'text', {align: 'CENTER', w: 174}), Z.t(s, 'Body/S', 'text-2', {align: 'CENTER', w: 174})]);
const arrow = (t) => Z.col({gap: 4, cross: 'CENTER', w: 52}, [Z.ic('arrow-right', 28, 'primary-bright'), t ? Z.t(t, 'Caption', 'text-3', {align: 'CENTER', w: 52}) : null]);

const c1 = card('1 · How an order travels (Firebase)', 'Customers log in with their mobile number (SMS code). Orders live in Firestore; Cloud Functions send the pushes; the same app shows the Admin Dashboard to the numbers listed in config/roles.', [
  Z.row({gap: 6, cross: 'CENTER'}, [box('smartphone', 'Customer app', 'Place Order → orders/CC-1004 (number from a transaction)', 'blue'), arrow(), box('zap', 'onOrderCreated', 'Cloud Function, asia-south1', 'amber'), arrow('push'), box('shield-user', 'Admin phones', '"New order CC-1004" even with the app closed', 'green')]),
  Z.row({gap: 6, cross: 'CENTER'}, [box('calendar-check', 'Admin confirms', 'Confirm / On the way / Completed / Cancel / new time', 'green'), arrow(), box('zap', 'onOrderUpdated', 'Picks the customer\'s language', 'amber'), arrow('push'), box('bell-ring', 'Customer phone', 'Status screen updates live; reminders re-planned', 'blue')]),
  Z.note('info', 'Customer cancels or asks for another time → the same function pushes it to every admin. WhatsApp stays for support chats and invoices only.', 'blue')]);

const c2 = card('2 · Firestore data', 'Region asia-south1 (Mumbai). Field names below are exactly what the app, rules and functions use.', [
  mono('config/roles      { adminPhones: ["+923000000001", …] }          ← edit in the Firebase console only\nconfig/business   { phoneDisplay, phoneDial, whatsapp, hours{en,ur}, hoursNote{en,ur}, areas[] }\ncounters/orders   { next: 1001 }                                     ← +1 per order (transaction)\nservices/{id}     { category: ac|refrigeration|commercial, active, sort, title{en,ur}, listTitle?,\n                    short{en,ur}, about{en,ur}, included[{en,ur}] ≤ 12, duration{en,ur},\n                    image (bundled key) | imagePath (Storage), createdAt, updatedAt }\nusers/{uid}       { phone (+92…, = login), name, area, house, landmark, lang: en|ur,\n                    tokens[≤10] (FCM), adminAlerts, createdAt, updatedAt }\norders/{CC-1004}  { code, uid, serviceId, service{en,ur}, serviceImage, applianceId, appliance{en,ur},\n                    date "2026-10-08", dateLabel{en,ur}, slotId "10-12", slot{en,ur}, issue, photos[≤3],\n                    name, phone, address, status, history[{status, at, by: customer|admin, note, date, slotId}],\n                    lang, createdAt, updatedAt, completedAt?, rating?, cancelReason, adminNote, clientRef }\n\nStorage: orders/{uid}/{folder}/{1..3}.jpg (customer + admins) · services/{id}_{time}.jpg (public)')]);

const c3 = card('3 · Order states', 'Every change appends to history (who + when) so both sides see the same story.', [
  table(['State', 'Set by', 'What happens'], [
    ['requested', 'Customer (Place Order / new time)', 'Admins get a push · customer sees "Waiting for confirmation"'],
    ['confirmed', 'Admin (Confirm, or Propose a different time)', 'Customer push "Visit confirmed ✓" · day-before + 1-hour reminders'],
    ['on_the_way', 'Admin (Technician left)', 'Customer push · cancel / reschedule locked'],
    ['completed', 'Admin (Mark completed)', 'Customer push · rating saved on the order · 90-day maintenance reminder'],
    ['cancelled', 'Customer (before on the way) or Admin (with reason)', 'Push to the other side · reminders removed']], [150, 320, 418])]);

const c4 = card('4 · Security rules (firestore.rules · storage.rules)', 'Checked by firebase/tests/rules-check.js against the emulator — 43 allow / deny cases, all passing.', [
  table(['Who', 'Can', 'Cannot'], [
    ['Anyone', 'Read open services and business info', 'Read closed services, profiles or orders'],
    ['Customer', 'Own profile (phone must equal the login number) · create an order for an open service · cancel / change time until on the way · rate when completed', 'Confirm, edit someone else\'s data, jump the order counter, delete orders'],
    ['Admin', 'Read all orders and profiles · move any order between valid states · add / edit / open / close / delete services · edit business info', 'Change admin numbers (console only) · delete orders']], [130, 450, 308])]);

const c5 = card('5 · Push notifications', 'FCM, high priority. Android channels: "visits" (customer) and "orders" (admin). iOS needs the APNs key uploaded in Firebase.', [
  table(['Event', 'Goes to', 'Example'], [
    ['New order', 'Every admin with alerts on', 'New order CC-1004 — AC Deep Cleaning · Thu, 08 Oct · 10 AM – 12 PM · Ali Raza · 0300 1234567'],
    ['Confirmed', 'Customer, in their language', 'Visit confirmed ✓ · CC-1004 / وزٹ کنفرم ہو گیا ✓'],
    ['On the way / Completed', 'Customer', 'Technician on the way · Service completed — tap to rate'],
    ['Time changed by shop', 'Customer', 'Visit time changed · CC-1004'],
    ['Cancelled / new time by customer', 'Admins', 'Order CC-1004 cancelled by customer'],
    ['Reminders', 'Customer (on the phone)', 'Day before 7 PM · 1 h before · maintenance after 90 days']], [210, 230, 448])]);

const c6 = card('6 · Before going live', 'Project: cooling-care-pasrur (Firebase). Items marked ★ need the client / owner.', [
  table(['Item', 'Status / action'], [
    ['★ Blaze plan', 'Required for Cloud Functions (push) and the Storage bucket (photos). Then: firebase deploy --only storage,functions'],
    ['★ Admin numbers', 'Firebase console › Firestore › config/roles › adminPhones (+92… format). Seed has +923000000001 for testing'],
    ['★ Shop phone / WhatsApp / hours', 'Admin app › Settings › Business (stored in config/business — no app update needed)'],
    ['Test numbers', '+92 300 0000001 → 123456 (admin) · +92 300 0000002 → 654321 — no real SMS'],
    ['SMS region', 'Allow-list = Pakistan only (stops SMS fraud)'],
    ['★ Android release', 'Add the release + Play App Signing SHA-1/SHA-256 to the Firebase Android app (debug keys already added)'],
    ['★ iOS', 'Apple Developer account · APNs auth key in Firebase › Cloud Messaging · open iosApp in Xcode (Firebase SPM already referenced)'],
    ['★ Privacy policy URL', 'Host store/privacy_policy.html; Data safety: phone number, name, address, photos, order history']], [230, 674])]);

const c7 = card('7 · Platform notes', 'Compose Multiplatform + GitLive Firebase SDK (one Kotlin codebase, Android + iOS).', [
  mono('Android\n• Phone Auth: PhoneAuthOptions with the visible Activity; SMS auto-retrieval signs in by itself\n• FCM: CcMessagingService (foreground display + token refresh) · channels visits / orders / maintenance\n• Debug builds can use the Emulator Suite: ./gradlew installDebug -Pcc.useEmulators=true (10.0.2.2)\n\niOS\n• FirebaseApp.configure() in AppDelegate · APNs token → Auth (silent verification) + Messaging\n• Background mode remote-notification · URL scheme app-1-834596151541-ios-… for reCAPTCHA fallback\n• Push Notifications entitlement (iosApp.entitlements)\n\nBoth\n• Offline: Firestore cache shows orders and services; placing / changing an order needs internet (clear message + retry)\n• A lost answer never doubles an order: each form carries clientRef, retried orders are found first')]);

const c8 = card('8 · QA checklist before release', null, [
  Z.fw(Z.col({gap: 8}, ['Log in with a new number → Your Details → Home; returning number → straight to Home; admin number → Dashboard', 'Place an order with 0 and 3 photos → admin phone gets the push (app closed) → photos visible in Order Detail',
    'Confirm / Technician left / Completed from admin → customer push in English and اردو, status screen updates by itself', 'Customer cancels and asks for another time → admins get the push; both blocked once on the way',
    'Close a service → it disappears from the customer app; ordering it from an open screen shows "service closed"', 'Airplane mode on Review → "No internet" dialog, form kept, Try again works',
    'Log out removes the push token; Delete account removes profile + login', 'Urdu: every new screen mirrors, Nastaliq not clipped, numbers stay left-to-right']
    .map(t => Z.fw(Z.row({gap: 10, cross: 'MIN'}, [Z.ic('square-pen', 18, 'primary'), Z.fw(Z.t(t, 'Body/L', 'text'))])))))]);
await Z.done();
const cols = [[c1, c3, c5, c7], [c2, c4, c6, c8]];
cols.forEach((list, ci) => { let y = 0; for (const c of list) { page.appendChild(c); c.x = ci * (W + 80); c.y = y; y += c.height + 80; } });
return 'ok';
})
