// Checks firestore.rules against the local emulator (no extra packages: Node 22 fetch + unsigned emulator tokens).
//   firebase emulators:start   (in firebase/)
//   node tests/rules-check.js
// Every case says whether the write/read must be ALLOWED or DENIED.
const PROJECT = 'cooling-care-pasrur';
const BASE = `http://127.0.0.1:8085/v1/projects/${PROJECT}/databases/(default)/documents`;

const b64 = (o) => Buffer.from(JSON.stringify(o)).toString('base64url');
function token(uid, phone) {
  const now = Math.floor(Date.now() / 1000);
  return b64({ alg: 'none', kid: 'fakekid', typ: 'JWT' }) + '.' + b64({
    iss: `https://securetoken.google.com/${PROJECT}`, aud: PROJECT, auth_time: now, iat: now, exp: now + 3600,
    sub: uid, user_id: uid, phone_number: phone, firebase: { identities: { phone: [phone] }, sign_in_provider: 'phone' },
  }) + '.';
}
const OWNER = 'owner';
const CUSTOMER = token('custA', '+923001234567');
const OTHER = token('custB', '+923007654321');
const ADMIN = token('admin1', '+923000000001');

const enc = (v) => {
  if (v === null || v === undefined) return { nullValue: null };
  if (typeof v === 'boolean') return { booleanValue: v };
  if (typeof v === 'number') return Number.isInteger(v) ? { integerValue: String(v) } : { doubleValue: v };
  if (typeof v === 'string') return { stringValue: v };
  if (Array.isArray(v)) return { arrayValue: { values: v.map(enc) } };
  return { mapValue: { fields: Object.fromEntries(Object.entries(v).map(([k, x]) => [k, enc(x)])) } };
};
const fields = (o) => Object.fromEntries(Object.entries(o).map(([k, v]) => [k, enc(v)]));

async function call(method, path, auth, body) {
  const res = await fetch(BASE + path, { method, headers: { Authorization: `Bearer ${auth}`, 'Content-Type': 'application/json' }, body: body && JSON.stringify(body) });
  return res.status;
}
const create = (doc, data, auth) => call('PATCH', `/${doc}?currentDocument.exists=false`, auth, { fields: fields(data) });
const update = (doc, data, auth) => call('PATCH', `/${doc}?` + Object.keys(data).map((k) => `updateMask.fieldPaths=${k}`).join('&') + '&currentDocument.exists=true', auth, { fields: fields(data) });
const read = (doc, auth) => call('GET', `/${doc}`, auth);
async function query(auth, structuredQuery) {
  const res = await fetch(BASE + ':runQuery', { method: 'POST', headers: { Authorization: `Bearer ${auth}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ structuredQuery }) });
  return res.status;
}

let failed = 0;
async function expect(name, allowed, statusPromise) {
  const status = await statusPromise;
  const ok = allowed ? status === 200 : status === 403;
  if (!ok) failed++;
  console.log(`${ok ? 'PASS' : 'FAIL'}  ${allowed ? 'allow' : 'deny '}  ${name}  (${status})`);
}

const txt = (en, ur) => ({ en, ur });
const service = (active) => ({ category: 'ac', sort: 10, active, title: txt('AC Deep Cleaning', 'اے سی ڈیپ کلیننگ'), short: txt('Cleaning', 'صفائی'), included: [txt('a', 'ا')], duration: txt('1 h', '1 گھنٹہ') });
const order = (code, uid, serviceId, extra = {}) => ({
  code, uid, serviceId, service: txt('AC Deep Cleaning', 'اے سی ڈیپ کلیننگ'), applianceId: 'split_ac', appliance: txt('Split AC', 'اسپلٹ اے سی'),
  date: '2026-10-08', dateLabel: txt('Thu, 08 Oct', 'جمعرات، 08 اکتوبر'), slotId: '10-12', slot: txt('10 AM – 12 PM', 'صبح 10 تا دوپہر 12'),
  issue: 'Not cooling', photos: [], name: 'Ali Raza', phone: '03001234567', address: 'House 12, Pasrur', status: 'requested',
  history: [{ status: 'requested', at: 1, by: 'customer' }], lang: 'en', createdAt: 1, updatedAt: 1, ...extra,
});

(async () => {
  // fresh database
  await fetch(`http://127.0.0.1:8085/emulator/v1/projects/${PROJECT}/databases/(default)/documents`, { method: 'DELETE' });
  await create('config/roles', { adminPhones: ['+923000000001'] }, OWNER);
  await create('config/business', { phoneDisplay: '0300 1234567', whatsapp: '923001234567' }, OWNER);
  await create('counters/orders', { next: 1001 }, OWNER);
  await create('services/ac_deep', service(true), OWNER);
  await create('services/vrf', { ...service(false), category: 'commercial' }, OWNER);

  console.log('— services & config');
  await expect('anyone reads an open service', true, read('services/ac_deep', CUSTOMER));
  await expect('customer cannot read a closed service', false, read('services/vrf', CUSTOMER));
  await expect('customer lists open services', true, query(CUSTOMER, { from: [{ collectionId: 'services' }], where: { fieldFilter: { field: { fieldPath: 'active' }, op: 'EQUAL', value: { booleanValue: true } } } }));
  await expect('customer cannot list every service', false, query(CUSTOMER, { from: [{ collectionId: 'services' }] }));
  await expect('admin lists every service', true, query(ADMIN, { from: [{ collectionId: 'services' }] }));
  await expect('customer cannot change a service', false, update('services/ac_deep', { active: false }, CUSTOMER));
  await expect('admin closes a service', true, update('services/ac_deep', { active: false, updatedAt: 2 }, ADMIN));
  await expect('admin opens it again', true, update('services/ac_deep', { active: true, updatedAt: 3 }, ADMIN));
  await expect('admin cannot save a service without Urdu', false, create('services/bad', { ...service(true), title: { en: 'x' } }, ADMIN));
  await expect('admin adds a service', true, create('services/svc_new', service(true), ADMIN));
  await expect('anyone reads business info', true, read('config/business', OTHER));
  await expect('customer cannot edit business info', false, update('config/business', { phoneDisplay: '0300 0000000' }, CUSTOMER));
  await expect('admin edits business info', true, update('config/business', { phoneDisplay: '0301 1111111' }, ADMIN));
  await expect('nobody edits roles from the app', false, update('config/roles', { adminPhones: ['+923001234567'] }, ADMIN));

  console.log('— profiles');
  await expect('customer creates own profile', true, create('users/custA', { phone: '+923001234567', name: 'Ali', tokens: [] }, CUSTOMER));
  await expect('profile must carry the login number', false, create('users/custB', { phone: '+923001234567', name: 'B', tokens: [] }, OTHER));
  await expect('customer cannot read another profile', false, read('users/custA', OTHER));
  await expect('admin reads a customer profile', true, read('users/custA', ADMIN));

  console.log('— order numbers');
  await expect('counter goes up by one', true, update('counters/orders', { next: 1002 }, CUSTOMER));
  await expect('counter cannot jump', false, update('counters/orders', { next: 1010 }, CUSTOMER));

  console.log('— orders');
  await expect('customer places an order', true, create('orders/CC-1001', order('CC-1001', 'custA', 'ac_deep'), CUSTOMER));
  await expect('order for a closed service is refused', false, create('orders/CC-1002', order('CC-1002', 'custA', 'vrf'), CUSTOMER));
  await expect('order in someone else\'s name is refused', false, create('orders/CC-1003', order('CC-1003', 'custB', 'ac_deep'), CUSTOMER));
  await expect('order must start as requested', false, create('orders/CC-1004', order('CC-1004', 'custA', 'ac_deep', { status: 'confirmed' }), CUSTOMER));
  await expect('order id must be CC-number', false, create('orders/hello', order('hello', 'custA', 'ac_deep'), CUSTOMER));
  await expect('customer reads own order', true, read('orders/CC-1001', CUSTOMER));
  await expect('other customer cannot read it', false, read('orders/CC-1001', OTHER));
  await expect('customer lists own orders', true, query(CUSTOMER, { from: [{ collectionId: 'orders' }], where: { fieldFilter: { field: { fieldPath: 'uid' }, op: 'EQUAL', value: { stringValue: 'custA' } } } }));
  await expect('customer cannot list all orders', false, query(CUSTOMER, { from: [{ collectionId: 'orders' }] }));
  await expect('admin lists all orders', true, query(ADMIN, { from: [{ collectionId: 'orders' }] }));
  await expect('customer cannot confirm own order', false, update('orders/CC-1001', { status: 'confirmed' }, CUSTOMER));
  await expect('customer asks for another time', true, update('orders/CC-1001', {
    date: '2026-10-09', dateLabel: txt('Fri, 09 Oct', 'جمعہ، 09 اکتوبر'), slotId: '12-14', slot: txt('12 PM – 2 PM', 'دوپہر 12 تا 2'), status: 'requested',
    history: [{ status: 'requested', at: 1, by: 'customer' }, { status: 'requested', at: 2, by: 'customer' }], updatedAt: 2,
  }, CUSTOMER));
  await expect('customer cannot rate before completion', false, update('orders/CC-1001', { rating: 5, updatedAt: 3 }, CUSTOMER));
  await expect('admin confirms', true, update('orders/CC-1001', { status: 'confirmed', updatedAt: 4 }, ADMIN));
  await expect('admin: technician on the way', true, update('orders/CC-1001', { status: 'on_the_way', updatedAt: 5 }, ADMIN));
  await expect('customer cannot cancel once on the way', false, update('orders/CC-1001', { status: 'cancelled', cancelReason: 'other', updatedAt: 6 }, CUSTOMER));
  await expect('admin cannot set an unknown status', false, update('orders/CC-1001', { status: 'paid' }, ADMIN));
  await expect('admin completes', true, update('orders/CC-1001', { status: 'completed', completedAt: 7, updatedAt: 7 }, ADMIN));
  await expect('customer rates the visit', true, update('orders/CC-1001', { rating: 4, updatedAt: 8 }, CUSTOMER));
  await expect('customer cannot rewrite the address', false, update('orders/CC-1001', { address: 'x' }, CUSTOMER));
  await create('counters/orders', { next: 1 }, OWNER); // no-op if it exists
  await expect('second order', true, create('orders/CC-1002', order('CC-1002', 'custA', 'ac_deep'), CUSTOMER));
  await expect('customer cancels a new order', true, update('orders/CC-1002', {
    status: 'cancelled', cancelReason: 'not_needed', history: [{ status: 'requested', at: 1, by: 'customer' }, { status: 'cancelled', at: 2, by: 'customer' }], updatedAt: 2,
  }, CUSTOMER));
  await expect('nobody deletes orders', false, call('DELETE', '/orders/CC-1002', ADMIN));

  console.log(failed ? `\n${failed} FAILED` : '\nALL RULES CHECKS PASSED');
  process.exit(failed ? 1 : 0);
})().catch((e) => { console.error(e); process.exit(2); });
