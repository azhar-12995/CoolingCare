// Push texts in English and Urdu. Kept free of Firebase imports so it can be unit-tested.

const STATUS = {
  confirmed: { en: ['Visit confirmed ✓', 'Cooling Care confirmed your visit.'], ur: ['وزٹ کنفرم ہو گیا ✓', 'کولنگ کیئر نے آپ کا وزٹ کنفرم کر دیا۔'] },
  on_the_way: { en: ['Technician on the way', 'Please keep your phone nearby.'], ur: ['ٹیکنیشن راستے میں ہے', 'براہِ کرم اپنا فون قریب رکھیں۔'] },
  completed: { en: ['Service completed', 'Thank you for choosing Cooling Care! Tap to rate your visit.'], ur: ['سروس مکمل ہو گئی', 'کولنگ کیئر کا شکریہ! وزٹ کو ریٹ کرنے کے لیے ٹیپ کریں۔'] },
  cancelled: { en: ['Booking cancelled', 'Cooling Care cancelled this visit. Call us if you have questions.'], ur: ['بکنگ منسوخ', 'کولنگ کیئر نے یہ وزٹ منسوخ کر دیا۔ سوال ہو تو کال کریں۔'] },
  requested: { en: ['Waiting for confirmation', 'Cooling Care will confirm your visit again shortly.'], ur: ['تصدیق کا انتظار', 'کولنگ کیئر جلد آپ کا وزٹ دوبارہ کنفرم کرے گی۔'] },
};
// The shop moved the visit without changing its status.
const RETIMED = { en: ['Visit time changed', 'Please check the new date and time of your visit.'], ur: ['وزٹ کا وقت تبدیل', 'براہِ کرم اپنے وزٹ کی نئی تاریخ اور وقت دیکھیں۔'] };

const pick = (t, lang) => (t && (lang === 'ur' ? t.ur : t.en)) || (t && t.en) || '';

/** One-line summary: "AC Deep Cleaning · Thu, 08 Oct · 10 AM – 12 PM". */
function summary(order, lang) {
  return [pick(order.service, lang), order.dateLabel ? pick(order.dateLabel, lang) : order.date, pick(order.slot, lang)].filter(Boolean).join(' · ');
}

/**
 * Message for the customer when the shop changes the order.
 * [statusChanged] false = only the date / time moved.
 */
function customerMessage(order, lang, statusChanged = true) {
  const s = statusChanged ? STATUS[order.status] : RETIMED;
  if (!s) return null;
  const [title, body] = lang === 'ur' ? s.ur : s.en;
  let text = body;
  if (statusChanged && order.status === 'cancelled' && order.cancelReason) {
    text += (lang === 'ur' ? ' وجہ: ' : ' Reason: ') + order.cancelReason;
  }
  return { title: `${title} · ${order.code}`, body: `${summary(order, lang)}\n${text}` };
}

/** Message for admins when a customer creates / cancels / reschedules. */
function adminMessage(kind, order) {
  const who = `${order.name} · ${order.phone}`;
  if (kind === 'created') return { title: `New order ${order.code}`, body: `${summary(order, 'en')}\n${who}` };
  if (kind === 'cancelled') return { title: `Order ${order.code} cancelled by customer`, body: `${summary(order, 'en')}\n${who}` };
  if (kind === 'rescheduled') return { title: `Order ${order.code} — new time requested`, body: `${summary(order, 'en')}\n${who}` };
  return null;
}

/** What happened between two versions of an order, from the last history entry. */
function changeKind(before, after) {
  const statusChanged = before.status !== after.status;
  const timeChanged = before.date !== after.date || before.slotId !== after.slotId;
  if (!statusChanged && !timeChanged) return null;
  const last = (after.history || [])[(after.history || []).length - 1] || {};
  if (last.by === 'customer') {
    if (after.status === 'cancelled') return 'customer_cancelled';
    if (timeChanged) return 'customer_rescheduled';
    return null;
  }
  return 'admin_update';
}

module.exports = { customerMessage, adminMessage, changeKind, summary };
