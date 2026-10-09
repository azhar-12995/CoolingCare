// Cooling Care — push notifications for orders.
//  • new order            → every admin device
//  • admin changes status → the customer's devices (in the customer's language)
//  • customer cancels / asks for a new time → every admin device
const { onDocumentCreated, onDocumentUpdated } = require('firebase-functions/v2/firestore');
const { setGlobalOptions, logger } = require('firebase-functions/v2');
const admin = require('firebase-admin');
const { customerMessage, adminMessage, changeKind } = require('./messages');

admin.initializeApp();
setGlobalOptions({ region: 'asia-south1', maxInstances: 5, memory: '256MiB' });
const db = admin.firestore();
const DEAD = new Set(['messaging/registration-token-not-registered', 'messaging/invalid-registration-token', 'messaging/invalid-argument']);

/** [{uid, token}] for every admin (phones listed in config/roles.adminPhones). */
async function adminTargets() {
  const roles = await db.doc('config/roles').get();
  const phones = roles.get('adminPhones') || [];
  const out = [];
  for (let i = 0; i < phones.length; i += 30) {
    const snap = await db.collection('users').where('phone', 'in', phones.slice(i, i + 30)).get();
    // Admins can switch new-order alerts off in the app (users/{uid}.adminAlerts = false).
    snap.forEach((d) => { if (d.get('adminAlerts') !== false) (d.get('tokens') || []).forEach((token) => out.push({ uid: d.id, token })); });
  }
  return out;
}

async function userTargets(uid) {
  const d = await db.doc(`users/${uid}`).get();
  return { lang: d.get('lang') || 'en', targets: (d.get('tokens') || []).map((token) => ({ uid, token })) };
}

/** Sends to every token; removes tokens FCM says are dead. */
async function send(targets, msg, route, channel) {
  if (!targets.length || !msg) return 0;
  const res = await admin.messaging().sendEach(targets.map(({ token }) => ({
    token,
    notification: { title: msg.title, body: msg.body },
    data: { route },
    android: { priority: 'high', notification: { channelId: channel, color: '#0A66C8', icon: 'ic_stat_snowflake', defaultSound: true } },
    apns: { payload: { aps: { sound: 'default' } } },
  })));
  const dead = [];
  res.responses.forEach((r, i) => { if (!r.success && DEAD.has(r.error && r.error.code)) dead.push(targets[i]); });
  for (const { uid, token } of dead) {
    await db.doc(`users/${uid}`).update({ tokens: admin.firestore.FieldValue.arrayRemove(token) }).catch(() => {});
  }
  logger.info('push', { route, sent: res.successCount, failed: res.failureCount, removed: dead.length });
  return res.successCount;
}

exports.onOrderCreated = onDocumentCreated('orders/{id}', async (event) => {
  const order = event.data && event.data.data();
  if (!order) return;
  await send(await adminTargets(), adminMessage('created', order), `admin/order/${event.params.id}`, 'orders');
});

exports.onOrderUpdated = onDocumentUpdated('orders/{id}', async (event) => {
  const before = event.data.before.data();
  const after = event.data.after.data();
  const kind = changeKind(before, after);
  if (!kind) return;
  const id = event.params.id;
  if (kind === 'admin_update') {
    const { lang, targets } = await userTargets(after.uid);
    await send(targets, customerMessage(after, lang, before.status !== after.status), `booking/${id}`, 'visits');
  } else {
    await send(await adminTargets(), adminMessage(kind === 'customer_cancelled' ? 'cancelled' : 'rescheduled', after), `admin/order/${id}`, 'orders');
  }
});
