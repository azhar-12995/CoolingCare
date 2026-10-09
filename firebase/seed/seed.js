// Seeds Cooling Care's Firestore: 10 services (from tools/catalog.json), business info, admin roles, order counter.
// Existing documents are never overwritten (so admin edits survive a re-run).
//   node seed.js --emulator          → Firestore emulator on localhost:8085 (8080 is often taken by a local web server)
//   node seed.js --project <id>       → real project, using the Firebase CLI login (NODE_OPTIONS=--use-system-ca)
//   add --admin +923001234567 to add an admin phone (real projects: add the owner's number this way or in the console)
const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);
const emulator = args.includes('--emulator');
const project = args.includes('--project') ? args[args.indexOf('--project') + 1] : 'cooling-care-pasrur';
const extraAdmins = args.flatMap((a, i) => (a === '--admin' ? [args[i + 1]] : []));
const base = emulator
  ? `http://127.0.0.1:8085/v1/projects/${project}/databases/(default)/documents`
  : `https://firestore.googleapis.com/v1/projects/${project}/databases/(default)/documents`;

async function token() {
  if (emulator) return 'owner';
  const lib = path.join(process.env.APPDATA, 'npm', 'node_modules', 'firebase-tools', 'lib', 'auth.js');
  const auth = require(lib);
  const acc = auth.getGlobalDefaultAccount();
  return (await auth.getAccessToken(acc.tokens.refresh_token, [])).access_token;
}

const enc = (v) => {
  if (v === null || v === undefined) return { nullValue: null };
  if (typeof v === 'boolean') return { booleanValue: v };
  if (typeof v === 'number') return Number.isInteger(v) ? { integerValue: String(v) } : { doubleValue: v };
  if (typeof v === 'string') return { stringValue: v };
  if (Array.isArray(v)) return { arrayValue: { values: v.map(enc) } };
  return { mapValue: { fields: Object.fromEntries(Object.entries(v).map(([k, x]) => [k, enc(x)])) } };
};

async function createIfMissing(tok, docPath, data) {
  const res = await fetch(`${base}/${docPath}?currentDocument.exists=false`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${tok}`, 'Content-Type': 'application/json', ...(emulator ? {} : { 'x-goog-user-project': project }) },
    body: JSON.stringify({ fields: Object.fromEntries(Object.entries(data).map(([k, v]) => [k, enc(v)])) }),
  });
  if (res.ok) return 'created';
  const t = await res.text();
  if (res.status === 400 && /exist/i.test(t) || res.status === 409 || /ALREADY_EXISTS|FAILED_PRECONDITION/.test(t)) return 'kept';
  throw new Error(`${docPath}: ${res.status} ${t.slice(0, 300)}`);
}

async function addAdmins(tok, phones) {
  if (!phones.length) return;
  const url = `${base}:commit`;
  const body = { writes: [{ transform: { document: `${base.replace(/^https?:\/\/[^/]+\/v1\//, '')}/config/roles`, fieldTransforms: [{ fieldPath: 'adminPhones', appendMissingElements: { values: phones.map(enc) } }] } }] };
  const res = await fetch(url, { method: 'POST', headers: { Authorization: `Bearer ${tok}`, 'Content-Type': 'application/json', ...(emulator ? {} : { 'x-goog-user-project': project }) }, body: JSON.stringify(body) });
  if (!res.ok) throw new Error('roles: ' + res.status + ' ' + (await res.text()).slice(0, 300));
}

(async () => {
  const cat = JSON.parse(fs.readFileSync(path.join(__dirname, '..', '..', 'tools', 'catalog.json'), 'utf8'));
  const tok = await token();
  const now = Date.now();
  const out = {};
  for (const [i, s] of cat.services.entries()) {
    out[s.id] = await createIfMissing(tok, `services/${s.id}`, {
      category: s.category, sort: (i + 1) * 10, active: true,
      title: s.title, listTitle: s.listTitle || s.title, short: s.short, about: s.about, included: s.included, duration: s.duration,
      image: s.img, header: s.header, imageUrl: '', createdAt: now, updatedAt: now,
    });
  }
  const b = cat.business;
  out.business = await createIfMissing(tok, 'config/business', {
    name: b.name, phoneDisplay: b.phoneDisplay, phoneDial: b.phoneDial, whatsapp: b.whatsapp,
    hours: b.hours, hoursNote: b.hoursNote, areas: cat.areas, updatedAt: now,
  });
  out.roles = await createIfMissing(tok, 'config/roles', { adminPhones: ['+923000000001'] });
  out.counter = await createIfMissing(tok, 'counters/orders', { next: 1001 });
  await addAdmins(tok, extraAdmins);
  console.log(emulator ? 'EMULATOR' : project, JSON.stringify(out), extraAdmins.length ? 'admins+' + extraAdmins.join(',') : '');
})().catch((e) => { console.error('SEED FAILED', e.message); process.exit(1); });
