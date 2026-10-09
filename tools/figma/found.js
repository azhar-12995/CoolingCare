(async (Z) => {
const page = figma.root.children.find(p => p.name === 'Foundations');
for (const n of [...page.children]) if (n.type !== 'SECTION' || n.name !== 'Icons') n.remove();
const icons = page.children.find(n => n.name === 'Icons');
const put = (n, x, y) => { page.appendChild(n); n.x = x; n.y = y; return n; };
const H2 = t => Z.t(t, 'Board/H3', 'text');
// ---------- Design tokens ----------
const sw = k => Z.col({name: 'swatch/' + k, w: 150, gap: 6}, [Z.rect(150, 70, k, 14, {stroke: 'border'}), Z.t(k, 'Label/M', 'text'), Z.t(Z.hex(k).toUpperCase(), 'Caption', 'text-2')]);
const groups = [['Brand', ['primary', 'primary-dark', 'primary-bright', 'primary-tint', 'primary-soft', 'navy', 'tile']],
  ['Neutrals', ['text', 'text-2', 'text-3', 'placeholder', 'border', 'divider', 'bg', 'bg-2', 'surface-2', 'on-primary']],
  ['Status & actions', ['green', 'green-dark', 'green-tint', 'green-text', 'orange', 'orange-tint', 'orange-text', 'danger', 'danger-tint', 'warning', 'warning-tint']]];
const doc = Z.col({name: 'Design Tokens', w: 1700, pad: 56, gap: 36, fill: 'surface', r: 32, stroke: 'border'});
Z.put(doc, Z.col({gap: 8}, [Z.t('Foundations', 'Board/H1', 'text'), Z.t('Colour variables (collection “Color”), type scale for English (Poppins + Inter) and Urdu (Noto Nastaliq Urdu), effects, radius & spacing. Every text colour passes WCAG AA 4.5:1 on its background: primary #0A66C8 = 5.6:1 on white, call green #0C8446 = 4.8:1, urgent orange #C9470C = 4.6:1.', 'Board/Body', 'text-2', {w: 1580})]));
for (const [g, ks] of groups) Z.put(doc, Z.fw(Z.col({gap: 14}, [H2(g), Z.fw(Z.row({gap: 16, wrap: true, wgap: 20}, ks.map(sw)))])));
const grad = (n, p) => Z.col({gap: 6, w: 360}, [Z.rect(360, 70, p, 14), Z.t(n, 'Label/M', 'text', {w: 360})]);
Z.put(doc, Z.fw(Z.col({gap: 14}, [H2('Gradients'), Z.row({gap: 16}, [grad('Brand · logo, app icon  #0A66C8 → #0B3D91', Z.G.brand()), grad('Splash  #1E88E5 → #0A66C8 → #0B2A5B', Z.G.splash()), grad('Hero card  #E3F0FD → #F6FAFF', Z.G.hero())])])));
const ts = await figma.getLocalTextStylesAsync();
const sample = n => n.startsWith('UR/') ? 'کولنگ کیئر — اے سی سروس آپ کی دہلیز پر' : 'Cooling Care — AC service at your doorstep';
const tsRow = s => Z.fw(Z.row({gap: 24, cross: 'CENTER', pad: [6, 0]}, [Z.t(s.name, 'Label/M', 'primary', {w: 150}), Z.t(s.fontName.family + ' ' + s.fontName.style + ' · ' + s.fontSize + '/' + Math.round(s.lineHeight.value), 'Caption', 'text-2', {w: 250}), Z.fw(Z.t(sample(s.name), s.name.replace('UR/', ''), 'text'))]));
Z.put(doc, Z.fw(Z.row({gap: 40, cross: 'MIN'}, [
  Z.fw(Z.col({gap: 6}, [H2('Type · English'), ...ts.filter(s => !s.name.startsWith('Board/') && !s.name.startsWith('UR/')).map(tsRow)])),
  Z.fw(Z.col({gap: 6}, [H2('Type · Urdu (RTL)'), ...ts.filter(s => s.name.startsWith('UR/') && !s.name.startsWith('UR/Board')).map(tsRow)]))])));
const fx = n => Z.col({gap: 10, w: 220, cross: 'CENTER'}, [Z.box(200, 110, {r: 18, fill: 'surface', fx: n, clip: false}), Z.t(n, 'Label/M', 'text')]);
Z.put(doc, Z.fw(Z.col({gap: 14}, [H2('Effects · radius · spacing'), Z.row({gap: 30, pad: [10, 10, 30, 10]}, ['Shadow/Card', 'Shadow/Float', 'Shadow/Nav', 'Shadow/Button'].map(fx)),
  Z.t('Radius: screen 36 · cards 16–20 · inputs & buttons 12 · chips 999 · icon tiles 10–14 · sheets 26 (top).   Spacing: 4-pt grid · gutter 20 · card padding 12–16 · section gap 14.   Screen 375 × 812 · status bar 44 · app bar 54 · bottom nav 64 + 22 home indicator.   Touch targets ≥ 44 pt / 48 dp.', 'Board/Body', 'text-2', {w: 1580})])));
// ---------- Components ----------
const L = Z.L;
const lab = (t, n) => Z.col({gap: 8}, [Z.t(t, 'Caption', 'text-3', {upper: true}), n]);
const comp = Z.col({name: 'Components', w: 1700, pad: 56, gap: 34, fill: 'surface', r: 32, stroke: 'border'});
Z.put(comp, Z.col({gap: 8}, [Z.t('Components', 'Board/H1', 'text'), Z.t('Built once in the screen builder and in Compose (ui/components). Each one mirrors itself for Urdu (RTL).', 'Board/Body', 'text-2', {w: 1580})]));
Z.put(comp, Z.row({gap: 40, cross: 'MAX'}, [lab('Logo mark', Z.logo(96)), lab('App icon', Z.row({w: 120, h: 120, r: 28, main: 'CENTER', cross: 'CENTER', fill: [Z.G.brand()]}, [Z.ic('snowflake', 74, 'on-primary', {sw: 1.8})])),
  lab('Wordmark', Z.wordmark({style: 'Logo/XL'})), lab('On dark', Z.row({pad: 24, r: 20, fill: [Z.G.splash()]}, [Z.logo(64, {fill: 'surface', fg: 'primary'})]))]));
Z.put(comp, lab('Buttons', Z.row({gap: 14, wrap: true, wgap: 14, w: 1580}, [
  Z.btn('Book a Service', 'Primary', {arrow: true}), Z.btn('Secondary', 'Secondary'), Z.btn('Soft', 'Soft'), Z.btn('Light', 'Light'), Z.btn('Ghost link', 'Ghost'),
  Z.btn('Call Now', 'Green', {lead: 'phone'}), Z.btn('WhatsApp', 'Green', {lead: 'whatsapp', sub: 'Chat with us'}), Z.btn('WhatsApp Support', 'GreenOutline', {lead: 'whatsapp'}),
  Z.btn('Call Now', 'Orange', {lead: 'phone', sub: '0300 1234567'}), Z.btn('Cancel Booking', 'Danger'), Z.btn('Cancel booking', 'DangerSoft', {lead: 'ban'}), Z.btn('Small', 'Primary', {small: true, h: 34})])));
Z.put(comp, Z.row({gap: 48, cross: 'MIN'}, [
  lab('Chips', Z.row({gap: 8}, [Z.chip('All', true), Z.chip('AC', false), Z.chip('Refrigeration', false)])),
  lab('Pills & badges', Z.row({gap: 8, cross: 'CENTER'}, [Z.pill('Inspection quote', {icon: 'tag', r: 8, pad: [5, 10]}), Z.badge('Confirmed'), Z.pill('CC-4821', {fill: 'surface-2', color: 'text'})])),
  lab('Selection', Z.row({gap: 14, cross: 'CENTER'}, [Z.radio(true), Z.radio(false), Z.check(true), Z.check(false), Z.toggle(true), Z.toggle(false)]))]));
Z.put(comp, Z.row({gap: 24, cross: 'MIN'}, [
  lab('Field · default', Z.col({w: 300}, [Z.field('Contact Number', '0300 1234567', {icon: 'phone'})])),
  lab('Field · focus', Z.col({w: 300}, [Z.field('Your Name', 'Ali Raza', {icon: 'user', focus: true})])),
  lab('Field · error', Z.col({w: 300}, [Z.field('Contact Number', '0300 12345', {icon: 'phone', error: 'Use 03XX XXXXXXX'})])),
  lab('Dropdown', Z.col({w: 300}, [Z.field('Select Appliance', 'Split AC', {trail: 'chevron-down'})])),
  lab('Add photos', Z.col({w: 300}, [Z.fw(Z.row({h: 58, r: 12, fill: 'bg-2', stroke: 'primary', sop: 0.45, dash: [6, 4], gap: 12, main: 'CENTER', cross: 'CENTER'}, [Z.iconTile('camera', 'blue', 36, 19, {r: 10}), Z.t('Add Photos', 'Title/S', 'primary')]))]))]));
Z.put(comp, Z.row({gap: 24, cross: 'MIN'}, [
  lab('Stepper', Z.col({w: 360}, [Z.stepper(['Requested', 'Confirmed', 'On the way', 'Completed'], 2)])),
  lab('Note', Z.col({w: 360}, [Z.note('info', 'Our technician will contact you before arrival.', 'blue')])),
  lab('List row', Z.col({w: 360}, [Z.listRow({icon: 'clock', title: 'Working hours', sub: 'Mon – Sat · 10:00 AM – 8:00 PM', chevron: true})])),
  lab('Category tile', Z.col({w: 105, h: 88, r: 14, fill: 'tile', pad: [7, 4, 7, 4], gap: 2, cross: 'CENTER', main: 'SPACE_BETWEEN'}, [Z.img('cat_split_ac', 90, 48, 0, {fit: true}), Z.t('Split AC', 'Label/S', 'text')]))]));
Z.put(comp, lab('Bottom navigation (EN / UR)', Z.row({gap: 30}, [Z.col({w: 375}, [Z.nav('home')]), (() => { Z.setLang('ur'); const n = Z.col({w: 375}, [Z.nav('home')]); Z.setLang('en'); return n; })()])));
// ---------- Images ----------
const imgs = Z.col({name: 'Images', w: 1700, pad: 56, gap: 24, fill: 'surface', r: 32, stroke: 'border'});
Z.put(imgs, Z.col({gap: 8}, [Z.t('Images', 'Board/H1', 'text'), Z.t('All photos come from the client board, upscaled 4× with Real-ESRGAN, garbled shirt text replaced with a crisp “Cooling Care” logo, product tiles feathered into the tile colour. Source + pipeline: tools/images (build.py).', 'Board/Body', 'text-2', {w: 1580})]));
const card = k => { const d = Z.H_[k]; const h = 150, w = Math.min(300, Math.round(d.w / d.h * h)); return Z.col({gap: 6, w: Math.max(w, 140)}, [Z.img(k, w, h, 12, k.startsWith('cat_') ? {fit: true, bg: 'tile'} : {}), Z.t(k, 'Label/M', 'text'), Z.t(d.w + ' × ' + d.h + ' px', 'Caption', 'text-3')]); };
Z.put(imgs, Z.fw(Z.row({gap: 22, wrap: true, wgap: 26}, Object.keys(Z.H_).map(card))));
await Z.done();
put(doc, 0, icons.y + icons.height + 120); put(comp, 1800, 0); put(imgs, 1800, comp.height + 120);
return [doc.height, comp.height, imgs.height];
})
