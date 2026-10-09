const test = require('node:test');
const assert = require('node:assert');
const { customerMessage, adminMessage, changeKind } = require('../messages');

const order = {
  code: 'CC-1001', status: 'confirmed', name: 'Ali Raza', phone: '0300 1234567',
  service: { en: 'AC Deep Cleaning', ur: 'اے سی ڈیپ کلیننگ' }, dateLabel: { en: 'Thu, 08 Oct', ur: 'جمعرات، 08 اکتوبر' },
  slot: { en: '10 AM – 12 PM', ur: 'صبح 10 تا دوپہر 12' }, date: '2026-10-08', slotId: '10-12', history: [],
};

test('customer message follows the language', () => {
  assert.match(customerMessage(order, 'en').title, /Visit confirmed .* CC-1001/);
  assert.match(customerMessage(order, 'ur').body, /اے سی ڈیپ کلیننگ/);
  assert.equal(customerMessage({ ...order, status: 'unknown' }, 'en'), null);
});

test('admin message carries the customer contact', () => {
  const m = adminMessage('created', order);
  assert.equal(m.title, 'New order CC-1001');
  assert.match(m.body, /Ali Raza · 0300 1234567/);
});

test('change kind distinguishes who changed what', () => {
  const by = (who) => ({ history: [{ status: 'x', by: who }] });
  assert.equal(changeKind(order, { ...order }), null);
  assert.equal(changeKind({ ...order, status: 'requested' }, { ...order, ...by('admin') }), 'admin_update');
  assert.equal(changeKind(order, { ...order, status: 'cancelled', ...by('customer') }), 'customer_cancelled');
  assert.equal(changeKind(order, { ...order, status: 'requested', date: '2026-10-09', ...by('customer') }), 'customer_rescheduled');
});

test('a time change alone gets its own text, cancellations carry the reason', () => {
  assert.match(customerMessage(order, 'en', false).title, /Visit time changed · CC-1001/);
  const c = customerMessage({ ...order, status: 'cancelled', cancelReason: 'No technician that day' }, 'en');
  assert.match(c.body, /Reason: No technician that day/);
  assert.match(customerMessage({ ...order, status: 'requested' }, 'ur').title, /تصدیق کا انتظار/);
});
